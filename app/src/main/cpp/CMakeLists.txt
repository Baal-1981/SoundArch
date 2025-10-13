cmake_minimum_required(VERSION 3.22.1)

project("soundarch")

# ✅ Configuration C++
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# ==============================================================================
# 🚀 PRODUCTION OPTIMIZATION FLAGS - Reasonable, not blind nitro
# ==============================================================================
#
# -O3: Aggressive optimization (inlining, loop unrolling, vectorization)
# -ffast-math: Allows approximate math (fine for psychoacoustics)
#   → Enables reciprocal approximations, relaxed IEEE 754 compliance
#   → Safe for audio DSP (no need for strict NaN/Inf handling)
# -funroll-loops: Unroll hot loops in DSP processing
# -ftree-vectorize: Enable auto-vectorization (NEON on ARM)
# -fno-exceptions: Audio code doesn't throw (reduces binary size)
# -fno-rtti: No runtime type info needed (reduces binary size)
# -fvisibility=hidden: Hide symbols by default (smaller binary, faster linking)
#
# NEON (ARM SIMD):
#   - ARMv7 (32-bit): Requires explicit -mfpu=neon
#   - ARM64 (64-bit): NEON always available, no flag needed
#
# ==============================================================================

# Base release flags (common to all architectures)
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -DNDEBUG")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -ffast-math")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -funroll-loops")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -ftree-vectorize")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -fno-exceptions")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -fno-rtti")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -fvisibility=hidden")

# ARM-specific NEON flags
if(ANDROID_ABI STREQUAL "armeabi-v7a")
    # ARMv7 (32-bit): Explicit NEON + VFP
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -mfpu=neon -mfloat-abi=softfp")
    message(STATUS "🎯 ARMv7: Enabling NEON vectorization")
elseif(ANDROID_ABI STREQUAL "arm64-v8a")
    # ARM64 (64-bit): NEON always available, optimize for modern CPUs
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -mcpu=cortex-a53")
    message(STATUS "🎯 ARM64: NEON enabled by default, optimizing for Cortex-A53+")
endif()

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ⏱️ NOISE CANCELLER BENCHMARK FLAG
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Enable lightweight CPU timing and logging for NoiseCanceller
# Usage:
#   - Build with: cmake -DNC_BENCHMARK=ON
#   - Adds ~0.001ms overhead per block (high-resolution clock)
#   - Logs parameter changes, CPU stats, and threshold breaches
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

option(NC_BENCHMARK "Enable NoiseCanceller benchmark timing" ON)

if(NC_BENCHMARK)
    add_definitions(-DNC_BENCHMARK)
    message(STATUS "⏱️  NoiseCanceller benchmark enabled (adds ~0.001ms overhead)")
else()
    message(STATUS "⏱️  NoiseCanceller benchmark disabled (no overhead)")
endif()

# Debug flags (unoptimized, with symbols)
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -O0 -g -DDEBUG")

# ✅ Dépendance Oboe
include(FetchContent)
FetchContent_Declare(
        oboe
        GIT_REPOSITORY https://github.com/google/oboe.git
        GIT_TAG main
)
FetchContent_MakeAvailable(oboe)

# ✅ Sources natives (liste explicite complète)
# NOTE: testing/ directory is NOT included here (separate build for golden tests)
set(NATIVE_SRC
        ${CMAKE_SOURCE_DIR}/native-lib.cpp
        ${CMAKE_SOURCE_DIR}/audio/NativeAudioEngine.cpp
        ${CMAKE_SOURCE_DIR}/audio/OboeEngine.cpp
        ${CMAKE_SOURCE_DIR}/audio/BluetoothRouter.cpp
        ${CMAKE_SOURCE_DIR}/dsp/Equalizer.cpp
        ${CMAKE_SOURCE_DIR}/dsp/Compressor.cpp
        ${CMAKE_SOURCE_DIR}/dsp/Limiter.cpp
        ${CMAKE_SOURCE_DIR}/dsp/AGC.cpp
        ${CMAKE_SOURCE_DIR}/dsp/noisecancel/WindowFFT.cpp
        ${CMAKE_SOURCE_DIR}/dsp/noisecancel/NoiseProfileEstimator.cpp
        ${CMAKE_SOURCE_DIR}/dsp/noisecancel/NoiseCanceller.cpp
        ${CMAKE_SOURCE_DIR}/utils/RingBuffer.cpp
        ${CMAKE_SOURCE_DIR}/ml/TFLiteEngine.cpp
        ${CMAKE_SOURCE_DIR}/jni/BluetoothBridge.cpp
        # ✅ DSPMath.h est header-only, pas besoin de .cpp
        # ✅ testing/ excluded: See testing/CMakeLists.txt for golden test harness
)

# ✅ Création de la bibliothèque partagée
add_library(soundarch SHARED ${NATIVE_SRC})

# ✅ Répertoires d'en-têtes à inclure
target_include_directories(soundarch PRIVATE
        ${CMAKE_SOURCE_DIR}
        ${CMAKE_SOURCE_DIR}/audio
        ${CMAKE_SOURCE_DIR}/dsp
        ${CMAKE_SOURCE_DIR}/utils
        ${CMAKE_SOURCE_DIR}/ml
        ${CMAKE_SOURCE_DIR}/jni
        ${CMAKE_SOURCE_DIR}/third_party  # TensorFlow Lite headers (if downloaded)
)

# ==============================================================================
# 🤖 TENSORFLOW LITE - Optional ML Support
# ==============================================================================
# TFLite is provided via Gradle dependency (org.tensorflow:tensorflow-lite)
# The native library is automatically extracted from the AAR by Gradle
# and placed in the jniLibs directory, making it available at runtime.
#
# IMPORTANT: TensorFlow Lite does NOT support prefab (as of 2.14.0)
# We rely on runtime loading - the library must be present in the APK
# but we don't link against it at build time.
#
# Strategy:
#   1. Gradle extracts libtensorflowlite_jni.so from AAR → jniLibs/
#   2. CMake checks if library will be available (via find_library)
#   3. If found: Define USE_TFLITE, app loads library at runtime
#   4. If not found: Skip TFLite, app builds without ML features
#
# Headers are provided manually in this project:
#   - tensorflow/lite/*.h files are included in the source tree
#   - Or downloaded from TensorFlow repo during build
# ==============================================================================

# Try to find the TensorFlow Lite JNI library
# This will be found if Gradle has extracted it from the AAR
find_library(TENSORFLOWLITE_LIB tensorflowlite_jni)

if(TENSORFLOWLITE_LIB)
    message(STATUS "✅ TensorFlow Lite found: ${TENSORFLOWLITE_LIB}")
    add_definitions(-DUSE_TFLITE)

    # Link against the library
    set(TFLITE_LIBRARIES ${TENSORFLOWLITE_LIB})

    message(STATUS "   TFLite will be available at runtime")
    message(STATUS "   Headers must be provided manually or downloaded")
else()
    message(WARNING "⚠️ TensorFlow Lite library not found - ML features disabled")
    message(WARNING "    ")
    message(WARNING "    To enable ML:")
    message(WARNING "      1. Ensure org.tensorflow:tensorflow-lite:2.14.0 is in build.gradle")
    message(WARNING "      2. Run: ./gradlew clean && ./gradlew assembleDebug")
    message(WARNING "      3. The library will be extracted from AAR during build")
    message(WARNING "    ")
    message(WARNING "    App will build and run, but ML screen will show 'No model available'")
    set(TFLITE_LIBRARIES "")
endif()

# ✅ Linking avec les bibliothèques requises
target_link_libraries(soundarch
        oboe
        ${TFLITE_LIBRARIES}  # TensorFlow Lite (optional)
        log
        android
        OpenSLES
)