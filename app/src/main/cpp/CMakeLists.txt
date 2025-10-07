cmake_minimum_required(VERSION 3.22.1)

project("soundarch")

# ✅ Configuration C++
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# ✅ Options de compilation
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -ffast-math -DNDEBUG")
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
set(NATIVE_SRC
        ${CMAKE_SOURCE_DIR}/native-lib.cpp
        ${CMAKE_SOURCE_DIR}/audio/NativeAudioEngine.cpp
        ${CMAKE_SOURCE_DIR}/audio/OboeEngine.cpp
        ${CMAKE_SOURCE_DIR}/dsp/Equalizer.cpp
        ${CMAKE_SOURCE_DIR}/dsp/Compressor.cpp
        ${CMAKE_SOURCE_DIR}/dsp/Limiter.cpp
        ${CMAKE_SOURCE_DIR}/dsp/AGC.cpp
        ${CMAKE_SOURCE_DIR}/utils/RingBuffer.cpp
        # ✅ DSPMath.h est header-only, pas besoin de .cpp
)

# ✅ Création de la bibliothèque partagée
add_library(soundarch SHARED ${NATIVE_SRC})

# ✅ Répertoires d'en-têtes à inclure
target_include_directories(soundarch PRIVATE
        ${CMAKE_SOURCE_DIR}
        ${CMAKE_SOURCE_DIR}/audio
        ${CMAKE_SOURCE_DIR}/dsp
        ${CMAKE_SOURCE_DIR}/utils
)

# ✅ Linking avec les bibliothèques requises
target_link_libraries(soundarch
        oboe
        log
        android
        OpenSLES
)