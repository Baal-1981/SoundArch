#include <jni.h>
#include <android/log.h>
#include "Equalizer.h"

#define LOG_TAG "NativeAudioEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static soundarch::dsp::Equalizer* equalizer = nullptr;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_soundarch_engine_NativeAudioEngine_initialize(JNIEnv* env, jobject /* this */, jint sampleRate) {
    equalizer = new soundarch::dsp::Equalizer(static_cast<float>(sampleRate));
    LOGI("Equalizer initialized with sampleRate: %d", sampleRate);
    return equalizer != nullptr;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_soundarch_engine_NativeAudioEngine_start(JNIEnv* env, jobject /* this */) {
    LOGI("Audio started (stub)");
    return true; // Implémente ton démarrage audio ici
}

extern "C"
JNIEXPORT void JNICALL
Java_com_soundarch_engine_NativeAudioEngine_stop(JNIEnv* env, jobject /* this */) {
    LOGI("Audio stopped (stub)");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_soundarch_engine_NativeAudioEngine_release(JNIEnv* env, jobject /* this */) {
    delete equalizer;
    equalizer = nullptr;
    LOGI("Equalizer released");
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_soundarch_engine_NativeAudioEngine_getCurrentLatency(JNIEnv* env, jobject /* this */) {
    return 0.0; // Retourne la latence réelle si tu peux
}

extern "C"
JNIEXPORT void JNICALL
Java_com_soundarch_engine_NativeAudioEngine_setEqBands(JNIEnv* env, jobject /* this */, jfloatArray gains) {
    if (!equalizer) return;

    jsize length = env->GetArrayLength(gains);
    jfloat* nativeGains = env->GetFloatArrayElements(gains, nullptr);

    for (int i = 0; i < length; ++i) {
        equalizer->setBandGain(i, nativeGains[i]);
    }

    env->ReleaseFloatArrayElements(gains, nativeGains, 0);
    LOGI("EQ bands updated");
}
