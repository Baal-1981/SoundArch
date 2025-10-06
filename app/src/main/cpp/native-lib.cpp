#include <jni.h>
#include <android/log.h>
#include "audio/OboeEngine.h"
#include "dsp/Equalizer.h"
#include "dsp/Compressor.h"
#include "dsp/Limiter.h"

#define TAG "native-lib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

using namespace soundarch;

// === GLOBALS ===
static OboeEngine engine;
static std::unique_ptr<dsp::Equalizer> gEqualizer;
static std::unique_ptr<dsp::Compressor> gCompressor;
static std::unique_ptr<dsp::Limiter> gLimiter;  // 🔸 NOUVEAU

static JavaVM* gJvm = nullptr;
static jobject gActivity = nullptr;

// === CALLBACK AUDIO - PIPELINE DSP ===
static void process(float* in, float* out, int32_t frames) noexcept {
    for (int i = 0; i < frames; ++i) {
        float sample = in[i];

        // 🎚️ Chaîne DSP : EQ → Compressor → Limiter
        if (gEqualizer) {
            sample = gEqualizer->process(sample);
        }

        if (gCompressor) {
            sample = gCompressor->process(sample);
        }

        if (gLimiter) {
            sample = gLimiter->process(sample);  // 🔸 Protection finale
        }

        out[i] = sample;
    }
}

// === JNI ===
extern "C" {

// 📌 Au chargement
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    gJvm = vm;
    return JNI_VERSION_1_6;
}

// 📌 Start audio depuis Kotlin
JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_startAudio(JNIEnv* env, jobject thiz) {
    if (!gActivity) {
        gActivity = env->NewGlobalRef(thiz);
    }

    // Initialisation du pipeline DSP (sampleRate = 48000 par défaut)
    const float sampleRate = 48000.0f;

    if (!gEqualizer) {
        gEqualizer = std::make_unique<dsp::Equalizer>(sampleRate);
        LOGI("✅ Equalizer initialized");
    }

    if (!gCompressor) {
        gCompressor = std::make_unique<dsp::Compressor>(sampleRate);
        LOGI("✅ Compressor initialized");
    }

    if (!gLimiter) {
        gLimiter = std::make_unique<dsp::Limiter>(sampleRate);
        gLimiter->setThreshold(-1.0f);   // -1 dB par défaut
        gLimiter->setRelease(50.0f);     // 50ms release
        gLimiter->setLookahead(0.0f);    // Pas de lookahead (latence minimale)
        LOGI("✅ Limiter initialized (threshold=-1dB, release=50ms)");
    }

    engine.setAudioCallback(process);
    engine.start();
    LOGI("🎧 Audio pipeline started: EQ → Compressor → Limiter");
}

// 📌 Stop audio
JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_stopAudio(JNIEnv* env, jobject /*thiz*/) {
    engine.stop();
    LOGI("🛑 Audio stopped");
}

// 📌 Update EQ depuis Kotlin
JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setEqBands(JNIEnv* env, jobject /*thiz*/, jfloatArray gains) {
    if (!gEqualizer) return;

    jfloat* ptr = env->GetFloatArrayElements(gains, nullptr);
    if (!ptr) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "Invalid float array");
        return;
    }

    jsize len = env->GetArrayLength(gains);
    for (jsize i = 0; i < len && i < dsp::Equalizer::kNumBands; ++i) {
        gEqualizer->setBandGain(i, ptr[i]);
    }

    env->ReleaseFloatArrayElements(gains, ptr, JNI_ABORT);
}

// 📌 Update Compressor depuis Kotlin
JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setCompressor(
        JNIEnv* env, jobject /*thiz*/,
        jfloat threshold, jfloat ratio, jfloat attack, jfloat release, jfloat makeupGain
) {
    if (!gCompressor) return;

    gCompressor->setThreshold(threshold);
    gCompressor->setRatio(ratio);
    gCompressor->setAttack(attack);
    gCompressor->setRelease(release);
    // Note: setKnee() retiré car non implémenté dans Compressor
    // TODO: Ajouter setKnee() si nécessaire

    LOGI("🎛️ Compressor updated: T=%.1fdB R=%.1f:1 A=%.1fms Rel=%.1fms",
         threshold, ratio, attack, release);
}

// 📌 Update Limiter depuis Kotlin (NOUVEAU)
JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setLimiter(
        JNIEnv* env, jobject /*thiz*/,
        jfloat threshold, jfloat release, jfloat lookahead
) {
    if (!gLimiter) return;

    gLimiter->setThreshold(threshold);
    gLimiter->setRelease(release);
    gLimiter->setLookahead(lookahead);

    LOGI("🚨 Limiter updated: Threshold=%.1fdB Release=%.1fms Lookahead=%.1fms",
         threshold, release, lookahead);
}

// 📌 Get Limiter Gain Reduction (pour affichage UI)
JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getLimiterGainReduction(JNIEnv* env, jobject /*thiz*/) {
    if (!gLimiter) return 0.0f;
    return gLimiter->getGainReduction();
}

// 📌 Enable/Disable Limiter
JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setLimiterEnabled(JNIEnv* env, jobject /*thiz*/, jboolean enabled) {
    if (enabled && !gLimiter) {
        gLimiter = std::make_unique<dsp::Limiter>(48000.0f);
        LOGI("✅ Limiter enabled");
    } else if (!enabled && gLimiter) {
        gLimiter.reset();
        LOGI("❌ Limiter disabled");
    }
}

// 📌 Envoie latence vers Java
void sendLatencyToJava(double latency) {
    if (!gJvm || !gActivity) return;
    JNIEnv* env = nullptr;
    if (gJvm->AttachCurrentThread(&env, nullptr) != JNI_OK) return;

    jclass cls = env->GetObjectClass(gActivity);
    jmethodID mid = env->GetStaticMethodID(cls, "updateLatencyText", "(D)V");
    if (mid) env->CallStaticVoidMethod(cls, mid, latency);
}

}