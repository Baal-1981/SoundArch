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
    static OboeEngine gEngine;

// DSP Modules (heap-allocated for controlled lifecycle)
    static std::unique_ptr<dsp::Equalizer> gEqualizer;
    static std::unique_ptr<dsp::AGC> gAGC;
    static std::unique_ptr<dsp::Compressor> gCompressor;
    static std::unique_ptr<dsp::Limiter> gLimiter;

// Enable/Disable flags (atomic for thread safety)
    static std::atomic<bool> gAGCEnabled{true};
    static std::atomic<bool> gCompressorEnabled{true};
    static std::atomic<bool> gLimiterEnabled{true};

// JNI Cache
    static JavaVM* gJvm = nullptr;
    static jobject gActivity = nullptr;

// Performance Monitoring
    static std::atomic<uint64_t> gProcessedFrames{0};
    static std::atomic<uint32_t> gDroppedFrames{0};

} // anonymous namespace

// ==============================================================================
// ⚡ AUDIO CALLBACK - OPTIMIZED (Zero Allocation)
// ==============================================================================

static void audioCallback(float* input, float* output, int32_t numFrames) noexcept {
    // ✅ CRITICAL: This runs on real-time audio thread
    // NO malloc, NO new, NO vector, NO mutex, NO system calls

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
        gAGC->setAttackTime(3.0f);
        gAGC->setReleaseTime(15.0f);
        gAGC->setNoiseThreshold(-55.0f);
        gAGC->setWindowSize(0.1f);
        LOGI("✅ AGC initialized (Target=-20dB, MaxGain=+25dB)");
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
Java_com_soundarch_MainActivity_stopAudio(JNIEnv* env, jobject /*thiz*/) {
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
Java_com_soundarch_MainActivity_setAGCTargetLevel(JNIEnv* env, jobject /*thiz*/, jfloat targetDb) {
    if (gAGC) {
        gAGC->setTargetLevel(targetDb);
        LOGI("🎯 AGC Target: %.1f dB", targetDb);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCMaxGain(JNIEnv* env, jobject /*thiz*/, jfloat maxGainDb) {
    if (gAGC) {
        gAGC->setMaxGain(maxGainDb);
        LOGI("📈 AGC MaxGain: +%.1f dB", maxGainDb);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCMinGain(JNIEnv* env, jobject /*thiz*/, jfloat minGainDb) {
    if (gAGC) {
        gAGC->setMinGain(minGainDb);
        LOGI("📉 AGC MinGain: %.1f dB", minGainDb);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCAttackTime(JNIEnv* env, jobject /*thiz*/, jfloat seconds) {
    if (gAGC) {
        gAGC->setAttackTime(seconds);
        LOGI("⚡ AGC Attack: %.2f s", seconds);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCReleaseTime(JNIEnv* env, jobject /*thiz*/, jfloat seconds) {
    if (gAGC) {
        gAGC->setReleaseTime(seconds);
        LOGI("🕒 AGC Release: %.2f s", seconds);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCNoiseThreshold(JNIEnv* env, jobject /*thiz*/, jfloat thresholdDb) {
    if (gAGC) {
        gAGC->setNoiseThreshold(thresholdDb);
        LOGI("🔇 AGC NoiseGate: %.1f dB", thresholdDb);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCWindowSize(JNIEnv* env, jobject /*thiz*/, jfloat seconds) {
    if (gAGC) {
        gAGC->setWindowSize(seconds);
        LOGI("⏱️ AGC Window: %.2f s", seconds);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setAGCEnabled(JNIEnv* env, jobject /*thiz*/, jboolean enabled) {
    gAGCEnabled.store(enabled, std::memory_order_relaxed);
    LOGI("%s AGC %s", enabled ? "✅" : "❌", enabled ? "ENABLED" : "DISABLED");
}

JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getAGCCurrentGain(JNIEnv* env, jobject /*thiz*/) {
    return gAGC ? gAGC->getCurrentGain() : 0.0f;
}

JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getAGCCurrentLevel(JNIEnv* env, jobject /*thiz*/) {
    return gAGC ? gAGC->getCurrentLevel() : -60.0f;
}

// ==============================================================================
// 🎛️ COMPRESSOR CONTROLS
// ==============================================================================

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setCompressor(
        JNIEnv* env, jobject /*thiz*/,
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

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setCompressorEnabled(JNIEnv* env, jobject /*thiz*/, jboolean enabled) {
    gCompressorEnabled.store(enabled, std::memory_order_relaxed);
    LOGI("%s Compressor %s", enabled ? "✅" : "❌", enabled ? "ENABLED" : "DISABLED");
}

// ==============================================================================
// 🚨 LIMITER CONTROLS
// ==============================================================================

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setLimiter(
        JNIEnv* env, jobject /*thiz*/,
        jfloat threshold, jfloat release, jfloat lookahead
) {
    if (gLimiter) {
        gLimiter->setThreshold(threshold);
        gLimiter->setRelease(release);
        LOGI("🚨 Limiter: Thr=%.1fdBFS Rel=%.1fms", threshold, release);
    }
}

JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setLimiterEnabled(JNIEnv* env, jobject /*thiz*/, jboolean enabled) {
    gLimiterEnabled.store(enabled, std::memory_order_relaxed);
    LOGI("%s Limiter %s", enabled ? "✅" : "❌", enabled ? "ENABLED" : "DISABLED");
}

JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getLimiterGainReduction(JNIEnv* env, jobject /*thiz*/) {
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

static ProcessCPUTimes gLastProcessCPU;
static long long gLastTotalCPU = 0;
static bool gFirstProcessRead = true;
static int gCPUCallCount = 0;

JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getCPUUsage(JNIEnv* env, jobject /*thiz*/) {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1️⃣ Read TOTAL system CPU time from /proc/stat
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    FILE* statFile = fopen("/proc/stat", "r");
    if (!statFile) {
        LOGW("⚠️ Cannot open /proc/stat");
        return 0.0f;
    }

    char cpuLabel[16];
    long long user, nice, system, idle, iowait, irq, softirq;

    int ret = fscanf(statFile, "%s %lld %lld %lld %lld %lld %lld %lld",
                     cpuLabel, &user, &nice, &system, &idle, &iowait, &irq, &softirq);
    fclose(statFile);

    if (ret != 8 || strcmp(cpuLabel, "cpu") != 0) {
        LOGW("⚠️ Failed to parse /proc/stat (ret=%d)", ret);
        return 0.0f;
    }

    long long totalCPU = user + nice + system + idle + iowait + irq + softirq;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2️⃣ Read OUR process CPU time from /proc/self/stat
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    FILE* processFile = fopen("/proc/self/stat", "r");
    if (!processFile) {
        LOGW("⚠️ Cannot open /proc/self/stat");
        return 0.0f;
    }

    ProcessCPUTimes current;

    // Format: pid (comm) state ppid pgrp session tty_nr tpgid flags minflt cminflt majflt cmajflt utime stime cutime cstime
    // We need fields 14-17: utime, stime, cutime, cstime
    ret = fscanf(processFile,
                 "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %lld %lld %lld %lld",
                 &current.utime, &current.stime, &current.cutime, &current.cstime);
    fclose(processFile);

    if (ret != 4) {
        LOGW("⚠️ Failed to parse /proc/self/stat (ret=%d)", ret);
        return 0.0f;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3️⃣ Calculate CPU usage percentage
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    if (gFirstProcessRead) {
        // First read - initialize baseline
        gLastProcessCPU = current;
        gLastTotalCPU = totalCPU;
        gFirstProcessRead = false;
        LOGI("📊 CPU Monitoring initialized | TotalCPU: %lld | ProcessTime: %lld",
             totalCPU, current.utime + current.stime);
        return 0.0f;
    }

    // Calculate deltas
    long long processTimeDelta = (current.utime + current.stime) -
                                 (gLastProcessCPU.utime + gLastProcessCPU.stime);
    long long totalTimeDelta = totalCPU - gLastTotalCPU;

    float cpuUsage = 0.0f;
    if (totalTimeDelta > 0) {
        // CPU% = (process_time_delta / total_time_delta) * 100 * num_cores
        long numCores = sysconf(_SC_NPROCESSORS_ONLN);
        if (numCores <= 0) numCores = 1; // Fallback

        cpuUsage = (100.0f * processTimeDelta / totalTimeDelta) * numCores;
    }

    // Clamp to [0, 100]
    cpuUsage = std::max(0.0f, std::min(100.0f, cpuUsage));

    // Update last values
    gLastProcessCPU = current;
    gLastTotalCPU = totalCPU;

    // Debug log every 10 calls
    if (++gCPUCallCount % 10 == 0) {
        long numCores = sysconf(_SC_NPROCESSORS_ONLN);
        LOGI("📊 CPU: %.1f%% | ProcessΔ: %lld | TotalΔ: %lld | Cores: %ld",
             cpuUsage, processTimeDelta, totalTimeDelta, numCores);
    }

    return cpuUsage;
}

// ==============================================================================
// 💾 MEMORY MONITORING
// ==============================================================================

JNIEXPORT jlong JNICALL
Java_com_soundarch_MainActivity_getMemoryUsage(JNIEnv* env, jobject /*thiz*/) {
    FILE* file = fopen("/proc/self/status", "r");
    if (!file) {
        LOGW("⚠️ Cannot open /proc/self/status");
        return 0;
    }

    char line[256];
    long vmRSS = 0;

    while (fgets(line, sizeof(line), file)) {
        if (strncmp(line, "VmRSS:", 6) == 0) {
            sscanf(line + 6, "%ld", &vmRSS);
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