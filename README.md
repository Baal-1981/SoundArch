# SoundArch v2.0 - Professional Real-Time Audio DSP for Android

A professional-grade, low-latency audio processing application featuring lock-free DSP modules, dual UI/UX modes, and comprehensive real-time metrics.

## Overview

SoundArch is an advanced Android audio processing application built with:
- Native C++ DSP (Oboe 1.8.0) for ultra-low latency
- Lock-free real-time audio (zero allocation, zero mutex)
- Jetpack Compose UI with Material Design 3
- Dual UI modes (Friendly for casual users, Advanced for power users)
- Comprehensive metrics (Peak/RMS, Latency breakdown, CPU/Memory)
- ML-ready architecture (TensorFlow Lite 2.14.0 integration)

Target Devices: Android 10+ (API 29+), ARM64/ARMv7 physical devices

## Features

### Audio Processing (DSP Chain)
1. AGC (Automatic Gain Control) - Dynamic level normalization
2. Equalizer - 10-band parametric EQ (31 Hz - 16 kHz ISO standard)
3. Voice Gain - Post-EQ gain stage (±12 dB range)
4. Noise Canceller - Spectral subtraction with 4 presets
5. Compressor - Dynamic range compression
6. Limiter - Peak protection (-1 dBFS default threshold)

### UI Features
- Dual UI Modes:
  - Friendly Mode (😊): Simplified controls, larger touch targets
  - Advanced Mode (⚙️): Full metrics, status badges, mini EQ curve
- Real-Time Meters: Peak/RMS, Latency HUD, Gain reduction
- Quick Toggles: ML, SAFE, NC
- Status Badges: BLOCK size, Bluetooth profile, XRuns, CPU

### Advanced Sections
- Audio Engine, Dynamics, Noise Cancelling
- Bluetooth, EQ Settings, ML
- Performance, Build & Runtime, Diagnostics
- Logs & Tests, App & Permissions

## Architecture

3-Layer Architecture:
1. Android App Layer (Jetpack Compose UI + ViewModels)
2. Native C++ Layer (JNI Bridge + DSP Chain)
3. Oboe Audio Engine (AAudio backend)

Threading Model:
- Audio RT Thread (SCHED_FIFO, zero allocation, lock-free)
- UI Thread (JNI calls, parameter updates)
- Latency Reporter Thread (10Hz metrics)

## Build Instructions

### Prerequisites
- Android Studio: Hedgehog (2023.1.1) or later
- Android SDK: API 34
- NDK: r26b (26.1.10909125)
- CMake: 3.22.1
- Gradle: 8.13
- Kotlin: 2.0.21
- Java: JDK 11+

### Quick Start

```bash
# Clone the repository
git clone https://github.com/Baal-1981/SoundArch.git
cd soundarch

# Build debug APK
./gradlew assembleDebug

# Build release APK (R8 optimized, 60% smaller)
./gradlew assembleRelease

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# Run instrumented UI tests
./gradlew connectedDebugAndroidTest
```

### Build Outputs
- Debug APK: app/build/outputs/apk/debug/app-debug.apk (67 MB)
- Release APK: app/build/outputs/apk/release/app-release-unsigned.apk (27 MB)

## Project Structure

```
SoundArch/
├── app/
│   ├── src/main/cpp/                # Native C++ DSP code
│   │   ├── audio/OboeEngine.cpp
│   │   ├── dsp/AGC.cpp, Equalizer.cpp, etc.
│   │   └── native-lib.cpp           # JNI bridge
│   ├── src/main/java/com/soundarch/ # Kotlin UI/ViewModels
│   │   ├── MainActivity.kt
│   │   ├── ui/screens/HomeScreenV2.kt
│   │   └── viewmodel/
│   └── src/androidTest/             # Tests (DSP + UI)
├── README.md                        # This file
├── DESIGN_SYSTEM.md                 # UI/UX design system
├── DSP_TEST_SUITE.md                # DSP tests documentation
├── UI_TEST_SUITE.md                 # UI tests documentation
└── BUILD_SUCCESS_REPORT.md          # Build verification
```

## DSP Modules

All DSP modules are in app/src/main/cpp/dsp/.

### 1. AGC (Automatic Gain Control)
- File: dsp/AGC.cpp/h
- Purpose: Dynamic level normalization
- Test Coverage: 19 tests (AGCTest.kt)

### 2. Equalizer (10-Band Parametric)
- File: dsp/Equalizer.cpp/h
- Bands: 31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000 Hz
- Test Coverage: 29 tests (EqualizerTest.kt)

### 3. Compressor (Dynamic Range)
- File: dsp/Compressor.cpp/h
- Test Coverage: 20 tests (CompressorTest.kt)

### 4. Limiter (Peak Protection)
- File: dsp/Limiter.cpp/h
- Test Coverage: 24 tests (LimiterTest.kt)

### 5. Noise Canceller (Spectral Subtraction)
- File: dsp/noisecancel/NoiseCanceller.cpp/h
- Presets: Default, Voice, Outdoor, Office
- Test Coverage: 30 tests (NoiseCancellerTest.kt)

### 6. Voice Gain
- Location: native-lib.cpp (lines 201-209)
- Range: -12 dB to +12 dB

## UI/UX System

### Dual UI Modes
- Friendly Mode (😊): Simplified, green, collapsed panel
- Advanced Mode (⚙️): Full metrics, blue, expanded panel

### Key Components
- PeakRmsMeter: Color-coded audio level meter
- LatencyHud: Latency display with breakdown
- VoiceGainCard: Voice Gain slider
- AdvancedSectionsPanel: 11 Advanced sections

## Native Audio Engine

OboeEngine (audio/OboeEngine.cpp/h): Oboe wrapper for low-latency audio I/O using AAudio backend.

Lock-Free Communication: Uses std::atomic for UI → Audio thread communication.

## ViewModels & State Management

All ViewModels use Hilt + StateFlow:
- UiModeViewModel: Mode toggle
- AudioMetricsViewModel: Peak/RMS/Latency
- DynamicsViewModel: AGC/Compressor/Limiter
- EqViewModel, NoiseCancellingViewModel, etc.

## Testing

### DSP Unit Tests (122 tests, 2000+ lines)
Files: AGCTest, EqualizerTest, CompressorTest, LimiterTest, NoiseCancellerTest

Run: ./gradlew connectedDebugAndroidTest

Documentation: DSP_TEST_SUITE.md

### UI Instrumented Tests (57 tests, 800+ lines)
Files: NavigationTest, UiModeToggleTest, DspToggleTest

Documentation: UI_TEST_SUITE.md

## Performance

### Latency
- Target: <10ms
- Achieved (Pixel 7): 5-7 ms

### CPU Benchmarks
- Total DSP: ~3.5 ms (70% utilization)

## Development Guide

### Adding a New DSP Module
1. Create C++ header/source
2. Implement in native-lib.cpp (JNI + audio callback)
3. Declare JNI in MainActivity.kt
4. Create ViewModel
5. Create UI Screen
6. Add to navigation
7. Write tests

### Debugging
```bash
adb logcat -s NativeAudioBridge:I
```

## License

Copyright © 2025 SoundArch Contributors

Licensed under the MIT License.

Third-Party: Oboe, TensorFlow Lite, Compose, Hilt (Apache 2.0)

## Contact & Support

- GitHub Issues: https://github.com/Baal-1981/SoundArch.git
- Documentation: See DESIGN_SYSTEM.md, DSP_TEST_SUITE.md, UI_TEST_SUITE.md

---

SoundArch v2.0 - Professional Real-Time Audio DSP for Android

Status: ✅ Production-Ready (after signing release APK)

Last Updated: October 11, 2025
