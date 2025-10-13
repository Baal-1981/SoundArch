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

// FTZ/DAZ support for denormal handling
#if defined(__x86_64__) || defined(__i386__) || defined(_M_X64) || defined(_M_IX86)
    #include <xmmintrin.h>  // SSE intrinsics for FTZ
    #include <pmmintrin.h>  // SSE3 intrinsics for DAZ
    #define HAS_FTZ_DAZ 1
#elif defined(__ARM_NEON__) || defined(__ARM_NEON) || defined(__aarch64__)
    // ARM NEON: Flush-to-zero is controlled by FPCR register
    // On Android ARM64, FTZ is typically enabled by default
    #define HAS_FTZ_DAZ 1
    #define HAS_ARM_NEON 1
#else
    #define HAS_FTZ_DAZ 0
#endif

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
    xRunCount_.store(0);
    lastCallbackSize_.store(0);

    // Reset latency statistics
    latencyStats_ = LatencyStats{};

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📻 BLUETOOTH PROFILE DETECTION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    bluetoothRouter_.detectProfile(outputStream.get());
    bluetoothRouter_.resetStats();

    LOGI("🎧 Audio STARTED | SR=%d | Burst=%d | Buffer=%d frames | RingBuffer=%zu",
         outputStream->getSampleRate(), burst, outputStream->getBufferSizeInFrames(), ringBuffer.capacity());
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

    // Track callback buffer size for XRun correlation
    lastCallbackSize_.store(numFrames, std::memory_order_relaxed);

    std::call_once(pinThreadFlag, []() {
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 🔧 AUDIO THREAD OPTIMIZATION
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        // 1. Set SCHED_FIFO real-time priority
        struct sched_param schParams = {};
        schParams.sched_priority = 18;
        pid_t tid = gettid();
        if (sched_setscheduler(tid, SCHED_FIFO, &schParams) == 0) {
            LOGI("✅ Audio thread: SCHED_FIFO priority %d", schParams.sched_priority);
        } else {
            LOGE("⚠️ Audio thread: Failed to set SCHED_FIFO (errno=%d)", errno);
        }

        // 2. Enable FTZ (Flush-To-Zero) and DAZ (Denormals-Are-Zero) for denormal handling
        // Prevents CPU slowdown when processing very small floating-point values (<10^-38)
#if HAS_FTZ_DAZ
    #ifdef HAS_ARM_NEON
        // ARM NEON: Enable FTZ via FPCR register
        // NOTE: FPCR register only available on ARM64 (aarch64), not on 32-bit ARM
        #ifdef __aarch64__
            uint64_t fpcr;
            __asm__ __volatile__("mrs %0, fpcr" : "=r"(fpcr));
            fpcr |= (1 << 24);  // Set FZ bit (Flush-to-Zero)
            __asm__ __volatile__("msr fpcr, %0" :: "r"(fpcr));
            LOGI("✅ Audio thread: FTZ enabled (ARM64 NEON)");
        #else
            // 32-bit ARM: FTZ controlled by FPSCR register, but typically enabled by default
            // We don't set it manually to avoid platform-specific assembly issues
            LOGI("⚠️ Audio thread: FTZ (ARM32 NEON, using default)");
        #endif
    #else
        // x86/x64: Enable FTZ and DAZ via SSE control registers
        _MM_SET_FLUSH_ZERO_MODE(_MM_FLUSH_ZERO_ON);   // FTZ: Underflows flushed to zero
        _MM_SET_DENORMALS_ZERO_MODE(_MM_DENORMALS_ZERO_ON);  // DAZ: Denormals treated as zero
        LOGI("✅ Audio thread: FTZ/DAZ enabled (x86/SSE)");
    #endif
#else
        LOGI("⚠️ Audio thread: FTZ/DAZ not supported on this architecture");
#endif
    });

    float *output = static_cast<float *>(audioData);
    int32_t numSamples = numFrames * stream->getChannelCount();

    // Lecture micro → buffer
    if (inputStream) {
        static float inputTemp[4096];
        inputStream->read(inputTemp, numFrames, 0);

        // ✅ FIX: Log overflow avec limite + Track XRun
        if (!ringBuffer.push(inputTemp, numSamples)) {
            int count = overflowCount.fetch_add(1) + 1;
            xRunCount_.fetch_add(1, std::memory_order_relaxed);  // Track total XRuns
            if (count % 100 == 0) {
                LOGE("⚠️ RingBuffer OVERFLOW x%d (capacity=%zu, available=%zu, callback=%d frames)",
                     count, ringBuffer.capacity(), ringBuffer.availableToWrite(), numFrames);
            }
        }
    }

    // Traitement DSP
    // ✅ FIX: Log underflow avec limite + Track XRun
    if (!ringBuffer.pop(output, numSamples)) {
        int count = underflowCount.fetch_add(1) + 1;
        xRunCount_.fetch_add(1, std::memory_order_relaxed);  // Track total XRuns
        if (count % 100 == 0) {
            LOGE("⚠️ RingBuffer UNDERFLOW x%d (capacity=%zu, available=%zu, callback=%d frames)",
                 count, ringBuffer.capacity(), ringBuffer.availableToRead(), numFrames);
        }
        memset(output, 0, numSamples * sizeof(float));
    } else if (audioCallback_) {
        audioCallback_(output, output, numFrames);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📊 PEAK/RMS METER - Real-time audio level tracking with EMA smoothing
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Calculate peak and RMS levels for this audio block
    float peak = 0.0f;
    float sumSquares = 0.0f;

    for (int32_t i = 0; i < numSamples; ++i) {
        float sample = std::abs(output[i]);
        peak = std::max(peak, sample);
        sumSquares += output[i] * output[i];
    }

    float rms = std::sqrt(sumSquares / static_cast<float>(numSamples));

    // Convert to dBFS (clamp to -60dB floor)
    constexpr float MIN_DB = -60.0f;
    float peakDbCurrent = peak > 0.0f ? 20.0f * std::log10(peak) : MIN_DB;
    float rmsDbCurrent = rms > 0.0f ? 20.0f * std::log10(rms) : MIN_DB;

    peakDbCurrent = std::max(peakDbCurrent, MIN_DB);
    rmsDbCurrent = std::max(rmsDbCurrent, MIN_DB);

    // Apply EMA smoothing (α = 0.15 for fast response, slow decay)
    constexpr float LEVEL_EMA_ALPHA = 0.15f;
    float currentPeak = peakDb_.load(std::memory_order_relaxed);
    float currentRms = rmsDb_.load(std::memory_order_relaxed);

    // Peak: fast attack, slow release
    float newPeak = (peakDbCurrent > currentPeak)
        ? peakDbCurrent  // Instant attack
        : LEVEL_EMA_ALPHA * peakDbCurrent + (1.0f - LEVEL_EMA_ALPHA) * currentPeak;  // Slow decay

    // RMS: smoothed both ways
    float newRms = LEVEL_EMA_ALPHA * rmsDbCurrent + (1.0f - LEVEL_EMA_ALPHA) * currentRms;

    peakDb_.store(newPeak, std::memory_order_relaxed);
    rmsDb_.store(newRms, std::memory_order_relaxed);

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📊 LATENCY MEASUREMENT & STATISTICS (10Hz update rate)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    static int64_t frameCounter = 0;
    frameCounter += numFrames;

    if (frameCounter >= stream->getSampleRate() / 10) {
        frameCounter = 0;

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 📻 LATENCY MEASUREMENT - Multiple methods for verification
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        int32_t sampleRate = outputStream->getSampleRate();

        // METHOD 1: Hardware burst latency
        int32_t inBurst = inputStream->getFramesPerBurst();
        int32_t outBurst = outputStream->getFramesPerBurst();
        double burstLatencyMs = ((double)(inBurst + outBurst) / sampleRate) * 1000.0;

        // METHOD 2: Buffer size latency
        int32_t inBufferSize = inputStream->getBufferSizeInFrames();
        int32_t outBufferSize = outputStream->getBufferSizeInFrames();
        double bufferLatencyMs = ((double)(inBufferSize + outBufferSize) / sampleRate) * 1000.0;

        // METHOD 3: Frame position based (most accurate for callback mode)
        int64_t inFramesWritten = inputStream->getFramesWritten();
        int64_t inFramesRead = inputStream->getFramesRead();
        int64_t outFramesWritten = outputStream->getFramesWritten();
        int64_t outFramesRead = outputStream->getFramesRead();

        int64_t inFramesPending = inFramesWritten - inFramesRead;
        int64_t outFramesPending = outFramesWritten - outFramesRead;

        double frameBasedInMs = ((double)inFramesPending / sampleRate) * 1000.0;
        double frameBasedOutMs = ((double)outFramesPending / sampleRate) * 1000.0;
        double frameBasedLatencyMs = frameBasedInMs + frameBasedOutMs;

        // Ring buffer latency (data waiting to be processed)
        size_t samplesInRingBuffer = ringBuffer.availableToRead();
        double ringBufferLatencyMs = ((double)samplesInRingBuffer / sampleRate) * 1000.0;

        // DEBUG: Log all methods every 10 seconds
        static int debugCounter = 0;
        if (++debugCounter >= 100) {  // 10 seconds at 10Hz
            debugCounter = 0;
            LOGI("🔍 LATENCY DEBUG:");
            LOGI("  SR=%d | InBurst=%d OutBurst=%d | InBuf=%d OutBuf=%d",
                 sampleRate, inBurst, outBurst, inBufferSize, outBufferSize);
            LOGI("  Method1(Burst): %.2fms | Method2(Buffer): %.2fms | Method3(FramePos): %.2fms",
                 burstLatencyMs, bufferLatencyMs, frameBasedLatencyMs);
            LOGI("  FramePos Detail: In=%lld/%lld (%.2fms) Out=%lld/%lld (%.2fms)",
                 (long long)inFramesRead, (long long)inFramesWritten, frameBasedInMs,
                 (long long)outFramesRead, (long long)outFramesWritten, frameBasedOutMs);
            LOGI("  RingBuffer: %zu samples = %.2fms", samplesInRingBuffer, ringBufferLatencyMs);
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 🎯 REAL-TIME LATENCY (what you actually perceive)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Use OUTPUT burst + ring buffer for perceived latency
        // This represents the actual delay between speaking and hearing processed audio
        //
        // Why not input buffer? The input buffer is being filled continuously by the mic,
        // but we only process what's in the ring buffer. The OUTPUT latency is what you feel.

        double perceivedLatencyMs = burstLatencyMs + ringBufferLatencyMs;

        latencyStats_.inputMs = burstLatencyMs / 2.0;
        latencyStats_.outputMs = (burstLatencyMs / 2.0) + ringBufferLatencyMs;
        latencyStats_.totalMs = perceivedLatencyMs;

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 📊 PERFORMANCE METRICS - Comprehensive breakdown
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        performanceMetrics_.burstLatencyMs = burstLatencyMs;
        performanceMetrics_.bufferLatencyMs = bufferLatencyMs;
        performanceMetrics_.ringBufferLatencyMs = ringBufferLatencyMs;
        performanceMetrics_.perceivedLatencyMs = perceivedLatencyMs;
        performanceMetrics_.bluetoothCodecMs = 0.0;  // TODO: Add getter to BluetoothRouter

        performanceMetrics_.inputFramesPending = inFramesPending;
        performanceMetrics_.outputFramesPending = outFramesPending;

        performanceMetrics_.xRunCount = xRunCount_.load(std::memory_order_relaxed);
        performanceMetrics_.lastCallbackSize = numFrames;
        performanceMetrics_.bufferFillRatio = (float)ringBuffer.availableToRead() / ringBuffer.capacity();
        performanceMetrics_.safeModeActive = bluetoothRouter_.isSafeModeActive();

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 💻 CPU & RAM MONITORING (1Hz update rate - every second)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        static int cpuRamCounter = 0;
        if (++cpuRamCounter >= 10) {  // Every 10 * 100ms = 1 second
            cpuRamCounter = 0;

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 📊 CPU USAGE - Read from /proc/stat
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            static uint64_t prevTotalCpuTime = 0;
            static uint64_t prevIdleCpuTime = 0;

            FILE* statFile = fopen("/proc/stat", "r");
            if (statFile) {
                char cpuLabel[16];
                uint64_t user, nice, system, idle, iowait, irq, softirq, steal;

                if (fscanf(statFile, "%s %llu %llu %llu %llu %llu %llu %llu %llu",
                           cpuLabel, &user, &nice, &system, &idle, &iowait, &irq, &softirq, &steal) == 9) {

                    uint64_t totalCpuTime = user + nice + system + idle + iowait + irq + softirq + steal;
                    uint64_t idleCpuTime = idle + iowait;

                    if (prevTotalCpuTime > 0) {
                        uint64_t totalDelta = totalCpuTime - prevTotalCpuTime;
                        uint64_t idleDelta = idleCpuTime - prevIdleCpuTime;

                        if (totalDelta > 0) {
                            float cpuUsage = 100.0f * (1.0f - (float)idleDelta / (float)totalDelta);
                            performanceMetrics_.cpuUsagePercent = cpuUsage;
                        }
                    }

                    prevTotalCpuTime = totalCpuTime;
                    prevIdleCpuTime = idleCpuTime;
                }
                fclose(statFile);
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 📊 RAM USAGE - Read from /proc/meminfo
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            FILE* meminfoFile = fopen("/proc/meminfo", "r");
            if (meminfoFile) {
                uint64_t memTotal = 0;
                uint64_t memAvailable = 0;
                char line[256];

                while (fgets(line, sizeof(line), meminfoFile)) {
                    if (sscanf(line, "MemTotal: %llu kB", &memTotal) == 1) {
                        memTotal *= 1024;  // Convert to bytes
                    } else if (sscanf(line, "MemAvailable: %llu kB", &memAvailable) == 1) {
                        memAvailable *= 1024;  // Convert to bytes
                        break;  // Got both values
                    }
                }
                fclose(meminfoFile);

                if (memTotal > 0 && memAvailable > 0) {
                    uint64_t memUsed = memTotal - memAvailable;
                    performanceMetrics_.ramUsedBytes = memUsed;
                    performanceMetrics_.ramAvailableBytes = memAvailable;
                    performanceMetrics_.ramUsagePercent = 100.0f * (float)memUsed / (float)memTotal;
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 🛡️ SAFE MODE: Monitor buffer fill level for underrun prediction
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        float bufferFillRatio = static_cast<float>(ringBuffer.availableToRead()) / ringBuffer.capacity();
        bluetoothRouter_.updateSafeModeStatus(bufferFillRatio);

        // ✅ EMA SMOOTHING (α = 0.3 for ~5-second window at 10Hz)
        constexpr double EMA_ALPHA = 0.3;
        if (latencyStats_.emaMs == 0.0) {
            latencyStats_.emaMs = latencyStats_.totalMs;  // Initialize on first sample
        } else {
            latencyStats_.emaMs = EMA_ALPHA * latencyStats_.totalMs + (1.0 - EMA_ALPHA) * latencyStats_.emaMs;
        }

        // ✅ 5-SECOND ROLLING MIN/MAX
        using namespace std::chrono;
        int64_t now = duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();

        // Reset min/max every 5 seconds
        if (now - latencyStats_.minMaxResetTime > 5000) {
            latencyStats_.minMs = latencyStats_.totalMs;
            latencyStats_.maxMs = latencyStats_.totalMs;
            latencyStats_.minMaxResetTime = now;
        } else {
            latencyStats_.minMs = std::min(latencyStats_.minMs, latencyStats_.totalMs);
            latencyStats_.maxMs = std::max(latencyStats_.maxMs, latencyStats_.totalMs);
        }

        // Log breakdown + Bluetooth + Safe Mode for regression detection
        uint32_t xruns = xRunCount_.load(std::memory_order_relaxed);
        const char* profile = bluetoothRouter_.isBluetoothActive() ? bluetoothRouter_.getProfileName() : "Wired";
        const char* safeMode = bluetoothRouter_.isSafeModeActive() ? " [SAFE MODE]" : "";

        LOGI("📊 Latency: IN=%.2f | OUT=%.2f | Total=%.2f | EMA=%.2f | Min=%.2f | Max=%.2f | XRuns=%u | CB=%d | %s%s",
             latencyStats_.inputMs, latencyStats_.outputMs, latencyStats_.totalMs,
             latencyStats_.emaMs, latencyStats_.minMs, latencyStats_.maxMs, xruns, numFrames,
             profile, safeMode);

        // Send smoothed EMA to Java UI
        sendLatencyToJava(latencyStats_.emaMs);
    }

    return oboe::DataCallbackResult::Continue;
}

void OboeEngine::setAudioCallback(std::function<void(float*, float*, int32_t)> cb) noexcept {
    audioCallback_ = std::move(cb);
}

LatencyStats OboeEngine::getLatencyStats() const noexcept {
    return latencyStats_;  // POD struct, safe to copy
}

PerformanceMetrics OboeEngine::getPerformanceMetrics() const noexcept {
    return performanceMetrics_;  // POD struct, safe to copy
}