#ifndef SOUNDARCH_NATIVEAUDIOENGINE_H
#define SOUNDARCH_NATIVEAUDIOENGINE_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL
Java_com_soundarch_engine_NativeAudioEngine_initialize(JNIEnv* env, jobject obj, jint sampleRate);

JNIEXPORT jboolean JNICALL
Java_com_soundarch_engine_NativeAudioEngine_start(JNIEnv* env, jobject obj);

JNIEXPORT void JNICALL
Java_com_soundarch_engine_NativeAudioEngine_stop(JNIEnv* env, jobject obj);

JNIEXPORT void JNICALL
Java_com_soundarch_engine_NativeAudioEngine_release(JNIEnv* env, jobject obj);

JNIEXPORT jdouble JNICALL
Java_com_soundarch_engine_NativeAudioEngine_getCurrentLatency(JNIEnv* env, jobject obj);

JNIEXPORT void JNICALL
Java_com_soundarch_engine_NativeAudioEngine_setEqBands(JNIEnv* env, jobject obj, jfloatArray gains);

#ifdef __cplusplus
}
#endif

#endif //SOUNDARCH_NATIVEAUDIOENGINE_H
