#include "OboeEngine.h"
#include <android/log.h>
#include <chrono>
#include <cstring>
#include <unistd.h>
#include <pthread.h>
#include <sched.h>
#include <cerrno>
#include <mutex>
#include "../utils/RingBuffer.h"

#define TAG "OboeEngine"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

extern "C" void sendLatencyToJava(double latency);

static std::once_flag pinThreadFlag;
static RingBuffer<float, 16384> ringBuffer;  // ✅ FIX: 4x plus grand pour Bluetooth

// ✅ FIX: Compteurs de debug
static std::atomic<int> overflowCount{0};
static std::atomic<int> underflowCount{0};

OboeEngine::OboeEngine() noexcept : isRecording(false) {}

OboeEngine::~OboeEngine() {
    stop();
}

void OboeEngine::start() {
    if (isRecording) return;
    isRecording = true;

    auto inputBuilder = std::make_shared<oboe::AudioStreamBuilder>();
    auto outputBuilder = std::make_shared<oboe::AudioStreamBuilder>();

    inputBuilder->setDirection(oboe::Direction::Input)
            ->setFormat(oboe::AudioFormat::Float)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setChannelCount(oboe::ChannelCount::Mono)
            ->setCallback(nullptr);

    outputBuilder->setDirection(oboe::Direction::Output)
            ->setFormat(oboe::AudioFormat::Float)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setChannelCount(oboe::ChannelCount::Mono)
            ->setCallback(this);

    oboe::Result result = inputBuilder->openStream(inputStream);
    if (result != oboe::Result::OK || !inputStream) {
        LOGE("❌ Input stream error: %s", oboe::convertToText(result));
        return;
    }

    result = outputBuilder->openStream(outputStream);
    if (result != oboe::Result::OK || !outputStream) {
        LOGE("❌ Output stream error: %s", oboe::convertToText(result));
        inputStream->close();
        inputStream.reset();
        return;
    }

    int32_t burst = outputStream->getFramesPerBurst();
    oboe::Result bufferResult = outputStream->setBufferSizeInFrames(burst * 2);  // ✅ 2x burst pour Bluetooth
    if (bufferResult != oboe::Result::OK) {
        LOGE("⚠️ setBufferSizeInFrames failed: %s", oboe::convertToText(bufferResult));
    }

    inputStream->requestStart();
    outputStream->requestStart();

    // ✅ Reset des compteurs
    overflowCount.store(0);
    underflowCount.store(0);

    LOGI("🎧 Audio STARTED | SR=%d | Burst=%d | Buffer=%d frames",
         outputStream->getSampleRate(), burst, outputStream->getBufferSizeInFrames());
}

void OboeEngine::stop() {
    if (!isRecording) return;
    isRecording = false;

    LOGI("🛑 Stopping audio...");

    if (inputStream) {
        inputStream->requestStop();
        inputStream->close();
        inputStream.reset();
    }

    if (outputStream) {
        outputStream->requestStop();
        usleep(20000);
        outputStream->close();
        outputStream.reset();
    }

    // ✅ FIX: Affiche stats finales
    LOGI("📊 Final stats: Overflows=%d Underflows=%d",
         overflowCount.load(), underflowCount.load());
}

oboe::DataCallbackResult OboeEngine::onAudioReady(oboe::AudioStream *stream, void *audioData, int32_t numFrames) {
    if (!isRecording) return oboe::DataCallbackResult::Stop;

    std::call_once(pinThreadFlag, []() {
        struct sched_param schParams = {};
        schParams.sched_priority = 18;
        pid_t tid = gettid();
        if (sched_setscheduler(tid, SCHED_FIFO, &schParams) == 0) {
            LOGI("✅ Audio thread pinned (SCHED_FIFO prio %d)", schParams.sched_priority);
        }
    });

    float *output = static_cast<float *>(audioData);
    int32_t numSamples = numFrames * stream->getChannelCount();

    // Lecture micro → buffer
    if (inputStream) {
        static float inputTemp[4096];
        inputStream->read(inputTemp, numFrames, 0);

        // ✅ FIX: Log overflow avec limite
        if (!ringBuffer.push(inputTemp, numSamples)) {
            int count = overflowCount.fetch_add(1) + 1;
            if (count % 100 == 0) {
                LOGE("⚠️ RingBuffer OVERFLOW x%d", count);
            }
        }
    }

    // Traitement DSP
    // ✅ FIX: Log underflow avec limite
    if (!ringBuffer.pop(output, numSamples)) {
        int count = underflowCount.fetch_add(1) + 1;
        if (count % 100 == 0) {
            LOGE("⚠️ RingBuffer UNDERFLOW x%d", count);
        }
        memset(output, 0, numSamples * sizeof(float));
    } else if (audioCallback_) {
        audioCallback_(output, output, numFrames);
    }

    // ✅ FIX: Détection Bluetooth + latence corrigée
    static int64_t frameCounter = 0;
    frameCounter += numFrames;

    if (frameCounter >= stream->getSampleRate() / 10) {
        frameCounter = 0;

        // Détecte si Bluetooth
        bool isBluetooth = (outputStream->getDeviceId() != oboe::kUnspecified);

        if (isBluetooth) {
            // Latence Bluetooth estimée (AAC codec typique)
            LOGI("[BT Mode] Estimated latency: ~160-180ms (codec-dependent)");
            sendLatencyToJava(170.0);
        } else {
            // Mode normal (speaker/filaire)
            auto inLatency = inputStream->calculateLatencyMillis();
            auto outLatency = outputStream->calculateLatencyMillis();
            double inMs = inLatency.error() == oboe::Result::OK ? inLatency.value() : 0.0;
            double outMs = outLatency.error() == oboe::Result::OK ? outLatency.value() : 0.0;
            double total = inMs + outMs;
            LOGI("[Wired] IN=%.2f ms | OUT=%.2f ms | Total=%.2f ms", inMs, outMs, total);
            sendLatencyToJava(total);
        }
    }

    return oboe::DataCallbackResult::Continue;
}

void OboeEngine::setAudioCallback(std::function<void(float*, float*, int32_t)> cb) noexcept {
    audioCallback_ = std::move(cb);
}