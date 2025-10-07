// ==============================================================================
// SoundArch Native Bridge v2.0 - Optimized DSP Audio Processing
// CPU Monitoring Fixed - Process-specific measurement
// ==============================================================================

#include <jni.h>
#include <android/log.h>
#include <atomic>
#include <memory>
#include <cstdio>
#include <cstring>
#include <cerrno>
#include <unistd.h>
#include <algorithm>

// Audio Engine
#include "audio/OboeEngine.h"

// DSP Modules
#include "dsp/Equalizer.h"
#include "dsp/AGC.h"
#include "dsp/Compressor.h"
#include "dsp/Limiter.h"

// ==============================================================================
// 🔧 LOGGING MACROS
// ==============================================================================

#define TAG "NativeAudioBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// ==============================================================================
// 🎛️ GLOBAL DSP STATE (Thread-Safe)
// ==============================================================================

using namespace soundarch;

namespace {

// Audio Engine
    OboeEngine gEngine;

// DSP Modules (heap-allocated for controlled lifecycle)
    std::unique_ptr<dsp::Equalizer> gEqualizer;
    std::unique_ptr<dsp::AGC> gAGC;
    std::unique_ptr<dsp::Compressor> gCompressor;
    std::unique_ptr<dsp::Limiter> gLimiter;

// Enable/Disable flags (atomic for thread safety)
    std::atomic<bool> gAGCEnabled{true};
    std::atomic<bool> gCompressorEnabled{true};
    std::atomic<bool> gLimiterEnabled{true};

// JNI Cache
    JavaVM* gJvm = nullptr;
    jobject gActivity = nullptr;

// Performance Monitoring
    std::atomic<uint64_t> gProcessedFrames{0};
    std::atomic<uint32_t> gDroppedFrames{0};

} // anonymous namespace

// ==============================================================================
// ⚡ AUDIO CALLBACK - OPTIMIZED (Zero Allocation)
// ==============================================================================

// NOLINTNEXTLINE(readability-non-const-parameter) - input must be non-const to match OboeEngine std::function signature
static void audioCallback(float* input, float* output, int32_t numFrames) noexcept {
    // ✅ CRITICAL: This runs on real-time audio thread
    // NO malloc, NO new, NO vector, NO mutex, NO system calls

    if (!input || !output) return;  // Safety check for null pointers

    for (int32_t i = 0; i < numFrames; ++i) {
        float sample = input[i];

        // 1️⃣ AGC (Automatic Gain Control)
        if (gAGC && gAGCEnabled.load(std::memory_order_relaxed)) {
            sample = gAGC->process(sample);
        }

        // 2️⃣ Equalizer (Frequency shaping)
        if (gEqualizer) {
            sample = gEqualizer->process(sample);
        }

        // 3️⃣ Compressor (Dynamic control)
        if (gCompressor && gCompressorEnabled.load(std::memory_order_relaxed)) {
            sample = gCompressor->process(sample);
        }

        // 4️⃣ Limiter (Peak protection)
        if (gLimiter && gLimiterEnabled.load(std::memory_order_relaxed)) {
            sample = gLimiter->process(sample);
        }

        output[i] = sample;
    }

    // Update metrics
    gProcessedFrames.fetch_add(numFrames, std::memory_order_relaxed);
}

// ==============================================================================
// 🔧 LIFECYCLE MANAGEMENT
// ==============================================================================

extern "C" {

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    gJvm = vm;
    LOGI("✅ JNI_OnLoad: JavaVM cached");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_startAudio(JNIEnv* env, jobject thiz) {
    // Cache activity reference
    if (!gActivity) {
        gActivity = env->NewGlobalRef(thiz);
        LOGI("✅ Activity reference cached");
    }

    const float sampleRate = 48000.0f; // TODO: Get from Oboe dynamically

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎯 Initialize DSP Modules (if not already created)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    if (!gAGC) {
        gAGC = std::make_unique<dsp::AGC>(sampleRate);
        gAGC->setTargetLevel(-20.0f);
        gAGC->setMaxGain(25.0f);
        gAGC->setMinGain(-10.0f);
        gAGC->setAttackTime(0.1f);   // Fast attack: 100ms
        gAGC->setReleaseTime(0.5f);  // Fast release: 500ms
        gAGC->setNoiseThreshold(-55.0f);
        gAGC->setWindowSize(0.1f);
        LOGI("✅ AGC initialized (Target=-20dB, Attack=100ms, Release=500ms)");
    }

    if (!gEqualizer) {
        gEqualizer = std::make_unique<dsp::Equalizer>(sampleRate);
        LOGI("✅ Equalizer initialized (10 bands, SR=%.0fHz)", sampleRate);
    }

    if (!gCompressor) {
        gCompressor = std::make_unique<dsp::Compressor>(sampleRate);
        gCompressor->setThreshold(-20.0f);
        gCompressor->setRatio(4.0f);
        gCompressor->setAttack(5.0f);
        gCompressor->setRelease(50.0f);
        gCompressor->setMakeupGain(0.0f);
        LOGI("✅ Compressor initialized (Threshold=-20dB, Ratio=4:1)");
    }

    if (!gLimiter) {
        gLimiter = std::make_unique<dsp::Limiter>(sampleRate);
        gLimiter->setThreshold(-1.0f);
        gLimiter->setRelease(50.0f);
        LOGI("✅ Limiter initialized (Threshold=-1dBFS, Release=50ms)");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎧 Start Audio Engine
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    gEngine.setAudioCallback(audioCallback);
    gEngine.start();

    // Reset metrics
    gProcessedFrames.store(0, std::memory_order_relaxed);
    gDroppedFrames.store(0, std::memory_order_relaxed);

    LOGI("🎧 Audio engine STARTED | DSP Chain: AGC → EQ → Comp → Limiter");
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_stopAudio([[maybe_unused]] JNIEnv* env, jobject /*thiz*/) {
    gEngine.stop();

    const uint64_t totalFrames = gProcessedFrames.load(std::memory_order_relaxed);
    const uint32_t drops = gDroppedFrames.load(std::memory_order_relaxed);

    LOGI("🛑 Audio engine STOPPED | Processed: %llu frames | Drops: %u",
         (unsigned long long)totalFrames, drops);
}

// ==============================================================================
// 🎚️ EQUALIZER CONTROLS
// ==============================================================================

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setEqBands(JNIEnv* env, jobject /*thiz*/, jfloatArray gains) {
    if (!gEqualizer) {
        LOGW("⚠️ setEqBands: Equalizer not initialized");
        return;
    }

    jfloat* ptr = env->GetFloatArrayElements(gains, nullptr);
    if (!ptr) {
        LOGE("❌ setEqBands: Invalid float array");
        return;
    }

    const jsize len = env->GetArrayLength(gains);
    const jsize maxBands = std::min(len, static_cast<jsize>(dsp::Equalizer::kNumBands));

    for (jsize i = 0; i < maxBands; ++i) {
        gEqualizer->setBandGain(i, ptr[i]);
    }

    env->ReleaseFloatArrayElements(gains, ptr, JNI_ABORT);
    LOGI("🎚️ EQ updated (%d bands)", maxBands);
}

// ==============================================================================
// 🎯 AGC CONTROLS
// ==============================================================================

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCTargetLevel([[maybe_unused]] JNIEnv* env, jobject /*thiz*/, jfloat targetDb) {
    if (gAGC) {
        gAGC->setTargetLevel(targetDb);
        LOGI("🎯 AGC Target: %.1f dB", targetDb);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCMaxGain([[maybe_unused]] JNIEnv* env, jobject /*thiz*/, jfloat maxGainDb) {
    if (gAGC) {
        gAGC->setMaxGain(maxGainDb);
        LOGI("📈 AGC MaxGain: +%.1f dB", maxGainDb);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCMinGain([[maybe_unused]] JNIEnv* env, jobject /*thiz*/, jfloat minGainDb) {
    if (gAGC) {
        gAGC->setMinGain(minGainDb);
        LOGI("📉 AGC MinGain: %.1f dB", minGainDb);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCAttackTime([[maybe_unused]] JNIEnv* env, jobject /*thiz*/, jfloat seconds) {
    if (gAGC) {
        gAGC->setAttackTime(seconds);
        LOGI("⚡ AGC Attack: %.2f s", seconds);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCReleaseTime([[maybe_unused]] JNIEnv* env, jobject /*thiz*/, jfloat seconds) {
    if (gAGC) {
        gAGC->setReleaseTime(seconds);
        LOGI("🕒 AGC Release: %.2f s", seconds);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCNoiseThreshold([[maybe_unused]] JNIEnv* env, jobject /*thiz*/, jfloat thresholdDb) {
    if (gAGC) {
        gAGC->setNoiseThreshold(thresholdDb);
        LOGI("🔇 AGC NoiseGate: %.1f dB", thresholdDb);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCWindowSize([[maybe_unused]] JNIEnv* env, jobject /*thiz*/, jfloat seconds) {
    if (gAGC) {
        gAGC->setWindowSize(seconds);
        LOGI("⏱️ AGC Window: %.2f s", seconds);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCEnabled([[maybe_unused]] JNIEnv* env, jobject /*thiz*/, jboolean enabled) {
    gAGCEnabled.store(enabled, std::memory_order_relaxed);
    LOGI("%s AGC %s", enabled ? "✅" : "❌", enabled ? "ENABLED" : "DISABLED");
}

[[nodiscard]] JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getAGCCurrentGain([[maybe_unused]] JNIEnv* env, jobject /*thiz*/) {
    return gAGC ? gAGC->getCurrentGain() : 0.0f;
}

[[nodiscard]] JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getAGCCurrentLevel([[maybe_unused]] JNIEnv* env, jobject /*thiz*/) {
    return gAGC ? gAGC->getCurrentLevel() : -60.0f;
}

// ==============================================================================
// 🎛️ COMPRESSOR CONTROLS
// ==============================================================================

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setCompressor(
        [[maybe_unused]] JNIEnv* env, jobject /*thiz*/,
        jfloat threshold, jfloat ratio, jfloat attack, jfloat release, jfloat makeupGain
) {
    if (gCompressor) {
        gCompressor->setThreshold(threshold);
        gCompressor->setRatio(ratio);
        gCompressor->setAttack(attack);
        gCompressor->setRelease(release);
        gCompressor->setMakeupGain(makeupGain);
        LOGI("🎛️ Comp: Thr=%.1f Ratio=%.1f:1 Att=%.1fms Rel=%.1fms Makeup=%.1fdB",
             threshold, ratio, attack, release, makeupGain);
    }
}

// ==============================================================================
// 🚨 LIMITER CONTROLS
// ==============================================================================

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setLimiter(
        [[maybe_unused]] JNIEnv* env, jobject /*thiz*/,
        jfloat threshold, jfloat release, [[maybe_unused]] jfloat lookahead
) {
    if (gLimiter) {
        gLimiter->setThreshold(threshold);
        gLimiter->setRelease(release);
        LOGI("🚨 Limiter: Thr=%.1fdBFS Rel=%.1fms", threshold, release);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setLimiterEnabled([[maybe_unused]] JNIEnv* env, jobject /*thiz*/, jboolean enabled) {
    gLimiterEnabled.store(enabled, std::memory_order_relaxed);
    LOGI("%s Limiter %s", enabled ? "✅" : "❌", enabled ? "ENABLED" : "DISABLED");
}

[[nodiscard]] JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getLimiterGainReduction([[maybe_unused]] JNIEnv* env, jobject /*thiz*/) {
    return gLimiter ? gLimiter->getGainReduction() : 0.0f;
}

// ==============================================================================
// 📊 PERFORMANCE MONITORING - PROCESS CPU (FIXED)
// ==============================================================================

struct ProcessCPUTimes {
    long long utime = 0;   // User mode time
    long long stime = 0;   // Kernel mode time
    long long cutime = 0;  // Child user time
    long long cstime = 0;  // Child system time
};

ProcessCPUTimes gLastProcessCPU;
long long gLastRealTime = 0;  // Monotonic clock time for CPU measurement
bool gFirstProcessRead = true;
int gCPUCallCount = 0;

[[nodiscard]] JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getCPUUsage([[maybe_unused]] JNIEnv* env, jobject /*thiz*/) {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Use simplified process-only CPU measurement (Android compatible)
    // /proc/self/stat is accessible, /proc/stat may be restricted
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    FILE* processFile = fopen("/proc/self/stat", "r");
    if (!processFile) {
        LOGW("⚠️ Cannot open /proc/self/stat");
        return 0.0f;
    }

    ProcessCPUTimes current;

    // Format: pid (comm) state ppid pgrp session tty_nr tpgid flags minflt cminflt majflt cmajflt utime stime cutime cstime
    // We need fields 14-17: utime, stime, cutime, cstime
    // NOLINTNEXTLINE(cert-err34-c) - fscanf is safe here, return value checked below
    int ret = fscanf(processFile,
                 "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %lld %lld %lld %lld",
                 &current.utime, &current.stime, &current.cutime, &current.cstime);
    fclose(processFile);

    if (ret != 4) {
        LOGW("⚠️ Failed to parse /proc/self/stat (ret=%d)", ret);
        return 0.0f;
    }

    // Get current time in clock ticks
    struct timespec ts = {};
    if (clock_gettime(CLOCK_MONOTONIC, &ts) != 0) {
        LOGW("⚠️ clock_gettime failed");
        return 0.0f;
    }
    long long currentRealTime = ts.tv_sec * sysconf(_SC_CLK_TCK) +
                                (ts.tv_nsec * sysconf(_SC_CLK_TCK) / 1000000000LL);

    if (gFirstProcessRead) {
        // First read - initialize baseline
        gLastProcessCPU = current;
        gLastRealTime = currentRealTime;
        gFirstProcessRead = false;
        LOGI("📊 CPU Monitoring initialized | ProcessTime: %lld", current.utime + current.stime);
        return 0.0f;
    }

    // Calculate deltas
    long long processTimeDelta = (current.utime + current.stime) -
                                 (gLastProcessCPU.utime + gLastProcessCPU.stime);
    long long realTimeDelta = currentRealTime - gLastRealTime;

    float cpuUsage = 0.0f;
    if (realTimeDelta > 0) {
        // CPU% = (process_time_delta / real_time_delta) * 100
        cpuUsage = (100.0f * static_cast<float>(processTimeDelta)) / static_cast<float>(realTimeDelta);
    }

    // Clamp to [0, 100]
    cpuUsage = std::max(0.0f, std::min(100.0f, cpuUsage));

    // Update last values
    gLastProcessCPU = current;
    gLastRealTime = currentRealTime;

    // Debug log every 10 calls
    if (++gCPUCallCount % 10 == 0) {
        long numCores = sysconf(_SC_NPROCESSORS_ONLN);
        LOGI("📊 CPU: %.1f%% | ProcessΔ: %lld | TimeΔ: %lld | Cores: %ld",
             cpuUsage, processTimeDelta, realTimeDelta, numCores);
    }

    return cpuUsage;
}

// ==============================================================================
// 💾 MEMORY MONITORING
// ==============================================================================

[[nodiscard]] JNIEXPORT jlong JNICALL
Java_com_soundarch_MainActivity_getMemoryUsage([[maybe_unused]] JNIEnv* env, jobject /*thiz*/) {
    FILE* file = fopen("/proc/self/status", "r");
    if (!file) {
        LOGW("⚠️ Cannot open /proc/self/status");
        return 0;
    }

    char line[256];
    long vmRSS = 0;

    while (fgets(line, sizeof(line), file)) {
        if (strncmp(line, "VmRSS:", 6) == 0) {
            // Use strtol for safer string-to-integer conversion
            char* endptr = nullptr;
            errno = 0;
            long value = strtol(line + 6, &endptr, 10);
            if (errno == 0 && endptr != line + 6) {
                vmRSS = value;
            } else {
                LOGW("⚠️ Failed to parse VmRSS");
                vmRSS = 0;
            }
            break;
        }
    }

    fclose(file);
    return vmRSS; // KB
}

// ==============================================================================
// 🔔 LATENCY CALLBACK
// ==============================================================================

void sendLatencyToJava(double latency) {
    if (!gJvm || !gActivity) return;

    JNIEnv* env = nullptr;
    if (gJvm->AttachCurrentThread(&env, nullptr) != JNI_OK) return;

    jclass cls = env->GetObjectClass(gActivity);
    jmethodID mid = env->GetStaticMethodID(cls, "updateLatencyText", "(D)V");
    if (mid) {
        env->CallStaticVoidMethod(cls, mid, latency);
    }
}

} // extern "C"