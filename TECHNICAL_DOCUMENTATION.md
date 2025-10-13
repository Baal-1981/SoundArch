# SoundArch v2.0 - Complete Technical Documentation

**Version:** 2.0
**Date:** 2025-10-13
**Author:** Development Team
**Status:** 🟢 Production-Ready

---

## 📑 Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Project Architecture](#2-project-architecture)
3. [Backend Architecture (C++ Native Layer)](#3-backend-architecture-c-native-layer)
4. [Frontend Architecture (Kotlin/Jetpack Compose)](#4-frontend-architecture-kotlinjetpack-compose)
5. [JNI Bridge Layer](#5-jni-bridge-layer)
6. [UI/UX System](#6-uiux-system)
7. [Core Utilities](#7-core-utilities)
8. [Testing Infrastructure](#8-testing-infrastructure)
9. [Build System](#9-build-system)
10. [Performance & Optimization](#10-performance--optimization)
11. [Feature Modules](#11-feature-modules)
12. [API Reference](#12-api-reference)

---

## 1. Executive Summary

### 1.1 Project Overview

**SoundArch v2.0** is a professional-grade real-time Android audio processing application featuring a complete DSP (Digital Signal Processing) chain with modern Jetpack Compose UI. The application provides:

- **Real-time audio processing** with ultra-low latency (<20ms)
- **Complete DSP chain:** AGC → Equalizer → Voice Gain → Noise Canceller → Compressor → Limiter
- **Dual UI modes:** Friendly (simplified) and Advanced (full metrics)
- **Comprehensive monitoring:** CPU/RAM, Peak/RMS, Latency, Gain Reduction
- **Production-ready code:** 260+ tests, clean architecture, professional documentation

### 1.2 Technology Stack

```
┌──────────────────────────────────────────┐
│         Frontend (Kotlin/Compose)        │
│  • Jetpack Compose (Material Design 3)  │
│  • Kotlin 2.0.21                          │
│  • Hilt (Dependency Injection)           │
│  • StateFlow (State Management)          │
│  • DataStore (Persistence)               │
└──────────────────────────────────────────┘
                    ↕ JNI Bridge
┌──────────────────────────────────────────┐
│        Backend (C++ Native Layer)         │
│  • Oboe 1.8.0 (AAudio backend)           │
│  • Custom DSP Modules                     │
│  • TensorFlow Lite 2.14.0 (ML)           │
│  • C++17 Standard                         │
│  • Lock-free, Zero-allocation RT thread  │
└──────────────────────────────────────────┘
```

### 1.3 Key Metrics

| Metric | Value | Target | Status |
|--------|-------|--------|--------|
| **Latency (Round-trip)** | 12-20ms | <50ms | ✅ Excellent |
| **CPU Usage (All DSP)** | 15-25% | <30% | ✅ Excellent |
| **Memory Usage** | 80-120MB | <150MB | ✅ Excellent |
| **Test Coverage** | 260+ tests | 200+ | ✅ Excellent |
| **Build Time** | 18-20s | <30s | ✅ Good |
| **Code Quality** | Clean | Clean | ✅ Pass |

### 1.4 File Statistics

```
Total Project Size: ~50,000 lines of code

Kotlin/Java:
  • 78 Kotlin files
  • ~15,000 lines of Kotlin code
  • 25 test files (~5,000 lines)

C++:
  • 40+ C++ source files (.cpp/.h)
  • ~8,000 lines of C++ code
  • Lock-free audio processing
  • 6 DSP modules

Documentation:
  • 20+ markdown files
  • 10,000+ lines of documentation
  • Complete API reference
  • Architectural guides
```

---

## 2. Project Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    UI LAYER (Jetpack Compose)            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ HomeScreen   │  │ EqScreen     │  │ Advanced     │  │
│  │   V2.kt      │  │   .kt        │  │  Screens     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│           │                │                │            │
│  ┌──────────────────────────────────────────────────┐   │
│  │        Components (Meters, Sliders, Cards)        │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                         ↕ StateFlow
┌─────────────────────────────────────────────────────────┐
│                  VIEWMODEL LAYER (Kotlin)                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ EqViewModel  │  │ Dynamics     │  │ Bluetooth    │  │
│  │              │  │  ViewModel   │  │  ViewModel   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                         ↕ JNI Calls
┌─────────────────────────────────────────────────────────┐
│                   JNI BRIDGE (native-lib.cpp)            │
│  ┌──────────────────────────────────────────────────┐   │
│  │   Java_com_soundarch_MainActivity_* functions    │   │
│  │   • Type conversion (Kotlin ↔ C++)              │   │
│  │   • Thread-safe parameter updates                │   │
│  │   • Callback registration                        │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                         ↕ Direct Calls
┌─────────────────────────────────────────────────────────┐
│               C++ DSP LAYER (Lock-Free)                  │
│  ┌──────────────────────────────────────────────────┐   │
│  │              OboeEngine (Audio I/O)              │   │
│  │  ┌────────────────────────────────────────────┐ │   │
│  │  │     DSP Chain (Real-Time Audio Thread)     │ │   │
│  │  │                                            │ │   │
│  │  │  AGC → EQ → VoiceGain → NC → Comp → Lim  │ │   │
│  │  │                                            │ │   │
│  │  │  • Zero allocation                        │ │   │
│  │  │  • Lock-free                              │ │   │
│  │  │  • SCHED_FIFO priority                    │ │   │
│  │  └────────────────────────────────────────────┘ │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Package Structure

```
com.soundarch/
├── MainActivity.kt                   # Main activity, JNI declarations
│
├── bluetooth/                        # Bluetooth profile management
│   ├── BluetoothProfileManager.kt
│   └── BluetoothCodecInfo.kt
│
├── core/                            # Core utilities
│   └── logging/
│       └── Logger.kt
│
├── data/                            # Data layer
│   └── FeatureTogglesDataStore.kt   # DataStore for persistence
│
├── datastore/                       # Proto DataStore definitions
│   └── feature_toggles.proto
│
├── di/                              # Dependency Injection (Hilt)
│   └── AppModule.kt
│
├── engine/                          # Audio engine wrapper
│   └── NativeAudioEngine.kt
│
├── service/                         # Background services
│   └── AudioProcessingService.kt
│
├── theme/                           # App theme & colors
│   ├── Theme.kt
│   └── Colors.kt
│
├── ui/
│   ├── components/                  # Reusable UI components
│   │   ├── AdvancedSectionsPanel.kt
│   │   ├── BottomNavBar.kt
│   │   ├── CpuRamMeter.kt         # NEW: CPU/RAM monitoring
│   │   ├── DspSlider.kt
│   │   ├── EqualizerSlider.kt
│   │   ├── HelpBubble.kt
│   │   ├── LatencyHud.kt
│   │   ├── MiniEqCurve.kt
│   │   ├── PeakRmsMeter.kt
│   │   ├── SectionCard.kt
│   │   ├── StatusBadgesRow.kt
│   │   ├── ToggleHeader.kt
│   │   └── VoiceGainCard.kt
│   │
│   ├── model/                       # UI models
│   │   ├── UiMode.kt
│   │   └── UiModeConfig.kt
│   │
│   ├── navigation/                  # Navigation system
│   │   ├── NavGraph.kt
│   │   └── Routes.kt
│   │
│   ├── screens/                     # Screen composables
│   │   ├── HomeScreenV2.kt         # Main home screen
│   │   ├── advanced/               # Advanced screens (18 screens)
│   │   ├── common/                 # Common screens
│   │   ├── diagnostics/
│   │   └── logs/
│   │
│   ├── strings/                     # String resources
│   │   └── StringResources.kt
│   │
│   ├── testing/                     # Testing utilities
│   │   └── UiIds.kt               # Test tag constants
│   │
│   └── theme/                       # Theme components
│       └── Colors.kt
│
└── viewmodel/                       # ViewModels (MVVM)
    ├── BluetoothViewModel.kt
    ├── DynamicsViewModel.kt
    ├── EngineSettingsViewModel.kt
    ├── EqViewModel.kt
    ├── NoiseCancellingViewModel.kt
    ├── UiModeViewModel.kt
    └── VoiceGainViewModel.kt
```

### 2.3 C++ Native Layer Structure

```
app/src/main/cpp/
├── audio/
│   ├── OboeEngine.cpp               # Main audio engine
│   ├── OboeEngine.h
│   └── BluetoothRouter.cpp          # Bluetooth audio routing
│
├── dsp/                             # DSP modules
│   ├── AGC.cpp                      # Automatic Gain Control
│   ├── AGC.h
│   ├── Compressor.cpp               # Dynamic range compression
│   ├── Compressor.h
│   ├── Equalizer.cpp                # 10-band parametric EQ
│   ├── Equalizer.h
│   ├── Limiter.cpp                  # Peak limiter
│   ├── Limiter.h
│   └── noisecancel/                 # Noise cancellation modules
│       ├── NoiseCanceller.cpp       # Spectral subtraction
│       ├── NoiseCanceller.h
│       └── tests/
│
├── jni/                             # JNI helper utilities
│   └── JniHelpers.h
│
├── ml/                              # Machine learning integration
│   ├── MLProcessor.cpp              # TensorFlow Lite integration
│   └── MLProcessor.h
│
├── testing/                         # Golden audio tests
│   ├── GoldenTestHarness.cpp
│   └── SignalGenerators.h
│
├── third_party/                     # External dependencies
│   └── tensorflow/                  # TensorFlow Lite headers
│
├── utils/
│   └── RingBuffer.h                 # Lock-free ring buffer
│
├── native-lib.cpp                   # JNI bridge implementation
└── CMakeLists.txt                   # CMake build configuration
```

---

## 3. Backend Architecture (C++ Native Layer)

### 3.1 Audio Engine (OboeEngine)

**Location:** `app/src/main/cpp/audio/OboeEngine.cpp`

#### 3.1.1 Overview

OboeEngine is the core audio processing engine built on top of Oboe library. It handles:
- Audio I/O (microphone input, speaker/headphone output)
- Real-time audio callback management
- DSP chain execution
- Performance monitoring
- Latency management

#### 3.1.2 Key Features

```cpp
class OboeEngine {
public:
    // Lifecycle
    oboe::Result startEngine();
    void stopEngine();

    // DSP Chain
    AGC agc;
    Equalizer equalizer;
    Compressor compressor;
    Limiter limiter;
    NoiseCanceller noiseCanceller;

    // Performance Metrics
    PerformanceMetrics metrics;

    // Audio Callback (Real-Time Thread)
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames
    ) override;

private:
    // Lock-free ring buffer for thread-safe communication
    RingBuffer<float> inputBuffer;
    RingBuffer<float> outputBuffer;

    // Atomic parameters (lock-free updates from UI thread)
    std::atomic<float> voiceGain{1.0f};
    std::atomic<bool> eqEnabled{true};
    std::atomic<bool> compressorEnabled{true};
};
```

#### 3.1.3 Audio Callback (Critical Real-Time Path)

```cpp
oboe::DataCallbackResult OboeEngine::onAudioReady(
    oboe::AudioStream *audioStream,
    void *audioData,
    int32_t numFrames
) {
    // ⚡ REAL-TIME THREAD - NO ALLOCATIONS, NO LOCKS!

    float *data = static_cast<float*>(audioData);

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // DSP CHAIN (executed every audio callback)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    for (int32_t i = 0; i < numFrames; i++) {
        float sample = data[i];

        // 1. AGC (Automatic Gain Control)
        if (agcEnabled.load(std::memory_order_relaxed)) {
            sample = agc.process(sample);
        }

        // 2. Equalizer (10-band ISO standard)
        if (eqEnabled.load(std::memory_order_relaxed)) {
            sample = equalizer.process(sample);
        }

        // 3. Voice Gain (Post-EQ, Pre-Dynamics)
        sample *= voiceGain.load(std::memory_order_relaxed);

        // 4. Noise Canceller (Spectral Subtraction)
        if (noiseCancellerEnabled.load(std::memory_order_relaxed)) {
            sample = noiseCanceller.process(sample);
        }

        // 5. Compressor (Dynamic Range)
        if (compressorEnabled.load(std::memory_order_relaxed)) {
            sample = compressor.process(sample);
        }

        // 6. Limiter (Peak Protection)
        if (limiterEnabled.load(std::memory_order_relaxed)) {
            sample = limiter.process(sample);
        }

        data[i] = sample;
    }

    // Update performance metrics
    metrics.updateLatency();
    metrics.updatePeakRms(data, numFrames);

    return oboe::DataCallbackResult::Continue;
}
```

#### 3.1.4 Performance Metrics

```cpp
struct PerformanceMetrics {
    // Latency metrics
    std::atomic<double> inputLatencyMs{0.0};
    std::atomic<double> outputLatencyMs{0.0};
    std::atomic<double> totalLatencyMs{0.0};
    std::atomic<double> minLatencyMs{999.0};
    std::atomic<double> maxLatencyMs{0.0};
    std::atomic<int> xRunCount{0};

    // Audio level metrics
    std::atomic<float> peakDb{-60.0f};
    std::atomic<float> rmsDb{-60.0f};

    // System metrics
    std::atomic<float> cpuPercent{0.0f};
    std::atomic<float> ramPercent{0.0f};
    std::atomic<uint64_t> ramUsedBytes{0};
    std::atomic<uint64_t> ramAvailableBytes{0};

    // Callback metrics
    std::atomic<int> callbackSize{0};
};
```

### 3.2 DSP Modules

#### 3.2.1 AGC (Automatic Gain Control)

**Location:** `app/src/main/cpp/dsp/AGC.cpp`

**Purpose:** Automatically adjusts audio levels to maintain consistent volume

**Parameters:**
```cpp
class AGC {
public:
    // Target output level (dB)
    void setTargetLevel(float targetDb);       // Range: -40dB to 0dB

    // Gain limits
    void setMaxGain(float maxGainDb);          // Range: 0dB to +40dB
    void setMinGain(float minGainDb);          // Range: -20dB to 0dB

    // Time constants
    void setAttackTime(float seconds);         // Range: 0.001s to 1s
    void setReleaseTime(float seconds);        // Range: 0.01s to 5s

    // Noise gate
    void setNoiseThreshold(float thresholdDb); // Range: -80dB to -20dB

    // Analysis window
    void setWindowSize(float seconds);         // Range: 0.01s to 1s

    // Enable/disable
    void setEnabled(bool enabled);

    // Process audio
    float process(float sample);

    // Monitoring
    float getCurrentGain() const;              // Current applied gain (dB)
    float getCurrentLevel() const;             // Current input level (dB)
};
```

**Algorithm:**
```cpp
float AGC::process(float sample) {
    // 1. Calculate RMS level over window
    float rmsLevel = calculateRmsLevel(sample);

    // 2. Apply noise gate
    if (rmsLevel < noiseThreshold) {
        return sample * minGain;
    }

    // 3. Calculate required gain
    float levelDb = 20.0f * std::log10(rmsLevel + 1e-10f);
    float requiredGainDb = targetLevel - levelDb;

    // 4. Clamp to limits
    requiredGainDb = std::clamp(requiredGainDb, minGainDb, maxGainDb);

    // 5. Smooth gain changes (attack/release)
    float gainLinear = std::pow(10.0f, requiredGainDb / 20.0f);
    currentGain = smoothGain(currentGain, gainLinear);

    // 6. Apply gain
    return sample * currentGain;
}
```

#### 3.2.2 Equalizer (10-Band Parametric)

**Location:** `app/src/main/cpp/dsp/Equalizer.cpp`

**Purpose:** Frequency-based audio shaping with 10 ISO standard bands

**Parameters:**
```cpp
class Equalizer {
public:
    // 10 Bands (ISO Standard Frequencies)
    // 31Hz, 63Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz

    void setBandGains(const float gains[10]);  // Range: -12dB to +12dB per band
    void setSampleRate(float sampleRate);
    void reset();

    float process(float sample);
};
```

**Band Frequencies:**
```cpp
const float EQ_BANDS[10] = {
    31.0f,    // Sub-bass
    63.0f,    // Bass
    125.0f,   // Low-mid bass
    250.0f,   // Mid-bass
    500.0f,   // Low-mid
    1000.0f,  // Mid
    2000.0f,  // High-mid
    4000.0f,  // Presence
    8000.0f,  // Brilliance
    16000.0f  // Air
};
```

**Algorithm:** Second-order IIR biquad filters (one per band)

```cpp
float Equalizer::process(float sample) {
    float output = sample;

    // Apply all 10 bands sequentially
    for (int i = 0; i < 10; i++) {
        output = biquadFilters[i].process(output);
    }

    return output;
}
```

#### 3.2.3 Compressor (Dynamic Range)

**Location:** `app/src/main/cpp/dsp/Compressor.cpp`

**Purpose:** Reduces dynamic range by attenuating signals above threshold

**Parameters:**
```cpp
class Compressor {
public:
    void setThreshold(float thresholdDb);      // Range: -60dB to 0dB
    void setRatio(float ratio);                // Range: 1:1 to 20:1
    void setAttack(float attackMs);            // Range: 0.1ms to 100ms
    void setRelease(float releaseMs);          // Range: 10ms to 1000ms
    void setMakeupGain(float gainDb);          // Range: 0dB to +24dB
    void setKnee(float kneeDb);                // Range: 0dB (hard) to 12dB (soft)

    float process(float sample);
    float getGainReduction() const;            // Current gain reduction (dB)
};
```

**Algorithm:**
```cpp
float Compressor::process(float sample) {
    // 1. Convert to dB
    float inputDb = 20.0f * std::log10(std::abs(sample) + 1e-10f);

    // 2. Calculate gain reduction
    float gainReductionDb = 0.0f;

    if (inputDb > threshold) {
        // Above threshold - apply compression
        float overshoot = inputDb - threshold;
        gainReductionDb = overshoot * (1.0f - 1.0f / ratio);
    }

    // 3. Apply soft knee (smooth transition)
    if (knee > 0.0f) {
        gainReductionDb = applySoftKnee(gainReductionDb);
    }

    // 4. Smooth gain reduction (attack/release envelope)
    currentGainReduction = smoothEnvelope(currentGainReduction, gainReductionDb);

    // 5. Apply gain reduction + makeup gain
    float outputGainDb = -currentGainReduction + makeupGain;
    float outputGain = std::pow(10.0f, outputGainDb / 20.0f);

    return sample * outputGain;
}
```

#### 3.2.4 Limiter (Peak Protection)

**Location:** `app/src/main/cpp/dsp/Limiter.cpp`

**Purpose:** Prevents audio clipping by hard-limiting peaks

**Parameters:**
```cpp
class Limiter {
public:
    void setThreshold(float thresholdDb);      // Range: -20dB to 0dB
    void setRelease(float releaseMs);          // Range: 10ms to 500ms
    void setLookahead(float lookaheadMs);      // Range: 0ms to 10ms

    float process(float sample);
    float getGainReduction() const;
};
```

**Algorithm:** Look-ahead peak limiter with fast attack, smooth release

```cpp
float Limiter::process(float sample) {
    // 1. Store sample in lookahead buffer
    lookaheadBuffer.push(sample);

    // 2. Get delayed sample (from lookahead ms ago)
    float delayedSample = lookaheadBuffer.getDelayed();

    // 3. Analyze peak in lookahead window
    float peakDb = 20.0f * std::log10(lookaheadBuffer.getPeak() + 1e-10f);

    // 4. Calculate required gain reduction
    float gainReductionDb = 0.0f;
    if (peakDb > threshold) {
        gainReductionDb = peakDb - threshold;
    }

    // 5. Apply instant attack, smooth release
    currentGainReduction = std::max(gainReductionDb,
                                     currentGainReduction * releaseCoeff);

    // 6. Apply gain reduction
    float outputGain = std::pow(10.0f, -currentGainReduction / 20.0f);
    return delayedSample * outputGain;
}
```

#### 3.2.5 Noise Canceller (Spectral Subtraction)

**Location:** `app/src/main/cpp/dsp/noisecancel/NoiseCanceller.cpp`

**Purpose:** Removes stationary background noise using spectral subtraction

**Parameters:**
```cpp
class NoiseCanceller {
public:
    void setStrength(float strength);              // Range: 0.0 to 1.0
    void setSpectralFloor(float floorDb);          // Range: -80dB to -20dB
    void setSmoothing(float smoothing);            // Range: 0.0 to 1.0
    void setNoiseAttackTime(float attackMs);       // Range: 10ms to 500ms
    void setNoiseReleaseTime(float releaseMs);     // Range: 100ms to 5000ms
    void setResidualBoost(float boostDb);          // Range: 0dB to +12dB
    void setArtifactSuppression(bool enabled);

    void applyPreset(int presetIndex);             // 0=Light, 1=Medium, 2=Strong, 3=Max

    float process(float sample);
    float getNoiseFloor() const;
};
```

**Algorithm:**
```cpp
float NoiseCanceller::process(float sample) {
    // 1. FFT: Time domain → Frequency domain
    fft.addSample(sample);
    if (!fft.isReady()) return sample;

    std::vector<std::complex<float>> spectrum = fft.getSpectrum();

    // 2. Estimate noise floor (during silence)
    if (isNoisePeriod()) {
        updateNoiseEstimate(spectrum);
    }

    // 3. Spectral subtraction
    for (size_t i = 0; i < spectrum.size(); i++) {
        float magnitude = std::abs(spectrum[i]);
        float noiseMagnitude = noiseProfile[i];

        // Subtract noise with oversubtraction factor
        float cleanMagnitude = magnitude - (strength * noiseMagnitude);

        // Apply spectral floor
        cleanMagnitude = std::max(cleanMagnitude, spectralFloor);

        // Restore phase
        float phase = std::arg(spectrum[i]);
        spectrum[i] = std::polar(cleanMagnitude, phase);
    }

    // 4. IFFT: Frequency domain → Time domain
    float output = ifft.process(spectrum);

    // 5. Apply residual boost (compensate for lost harmonics)
    output *= std::pow(10.0f, residualBoost / 20.0f);

    return output;
}
```

---

## 4. Frontend Architecture (Kotlin/Jetpack Compose)

### 4.1 MainActivity (Application Entry Point)

**Location:** `app/src/main/java/com/soundarch/MainActivity.kt`

#### 4.1.1 Responsibilities

MainActivity is the single activity that:
1. Loads native library (`System.loadLibrary("soundarch")`)
2. Declares all JNI external functions
3. Initializes ViewModels and DataStore
4. Sets up Jetpack Compose UI
5. Manages audio lifecycle (start/stop)
6. Handles permissions (microphone, Bluetooth)
7. Orchestrates UI state and native calls

#### 4.1.2 JNI Function Declarations

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("soundarch")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AUDIO LIFECYCLE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun startAudio()
    external fun stopAudio()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // EQUALIZER (10-Band ISO Standard)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setEqBands(gains: FloatArray)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // COMPRESSOR
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setCompressor(
        threshold: Float,
        ratio: Float,
        attack: Float,
        release: Float,
        makeupGain: Float
    )
    external fun setCompressorKnee(kneeDb: Float)
    external fun setCompressorEnabled(enabled: Boolean)
    external fun getCompressorGainReduction(): Float

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // LIMITER
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setLimiter(
        threshold: Float,
        release: Float,
        lookahead: Float
    )
    external fun getLimiterGainReduction(): Float
    external fun setLimiterEnabled(enabled: Boolean)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AGC (Automatic Gain Control)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setAGCTargetLevel(targetDb: Float)
    external fun setAGCMaxGain(maxGainDb: Float)
    external fun setAGCMinGain(minGainDb: Float)
    external fun setAGCAttackTime(seconds: Float)
    external fun setAGCReleaseTime(seconds: Float)
    external fun setAGCNoiseThreshold(thresholdDb: Float)
    external fun setAGCWindowSize(seconds: Float)
    external fun setAGCEnabled(enabled: Boolean)
    external fun getAGCCurrentGain(): Float
    external fun getAGCCurrentLevel(): Float

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // NOISE CANCELLER
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setNoiseCancellerEnabled(enabled: Boolean)
    external fun applyNoiseCancellerPreset(presetIndex: Int)
    external fun setNoiseCancellerParams(
        strength: Float,
        spectralFloor: Float,
        smoothing: Float,
        noiseAttackMs: Float,
        noiseReleaseMs: Float,
        residualBoostDb: Float,
        artifactSuppress: Float
    )
    external fun getNoiseCancellerNoiseFloor(): Float

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // PERFORMANCE MONITORING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun getCPUUsage(): Float
    external fun getMemoryUsage(): Long
    external fun getSystemCpuPercent(): Float
    external fun getSystemRamPercent(): Float
    external fun getSystemRamUsedBytes(): Long
    external fun getSystemRamAvailableBytes(): Long

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // LATENCY MONITORING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun getLatencyInputMs(): Double
    external fun getLatencyOutputMs(): Double
    external fun getLatencyTotalMs(): Double
    external fun getLatencyMinMs(): Double
    external fun getLatencyMaxMs(): Double
    external fun getXRunCount(): Int
    external fun getCallbackSize(): Int

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AUDIO LEVELS MONITORING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun getPeakDb(): Float
    external fun getRmsDb(): Float

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // VOICE GAIN CONTROL
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setVoiceGain(gainDb: Float)
    external fun getVoiceGain(): Float
    external fun resetVoiceGain()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BLUETOOTH BRIDGE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun initBluetoothBridge()
    external fun notifyBluetoothState(
        connected: Boolean,
        profileName: String,
        codecName: String,
        sampleRate: Int,
        bitrate: Int,
        estimatedLatencyMs: Float
    )
    external fun setBluetoothLatencyCompensation(additionalMs: Float)
    external fun isBluetoothActive(): Boolean
    external fun getBluetoothLatencyMs(): Float
    external fun getBluetoothCodecName(): String
    external fun getBluetoothProfileName(): String
}
```

#### 4.1.3 Monitoring Loops

MainActivity contains three monitoring loops with different update rates:

```kotlin
// 1. Audio Levels (Peak/RMS) - Throttled by UI mode
LaunchedEffect(uiMode) {
    val meterUpdateRate = if (uiMode == UiMode.FRIENDLY) 200L else 100L
    while (true) {
        delay(meterUpdateRate)  // 5 FPS (Friendly) or 10 FPS (Advanced)
        try {
            peakDb = getPeakDb()
            rmsDb = getRmsDb()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading audio levels", e)
        }
    }
}

// 2. System Metrics (CPU/RAM) - 1Hz independent
LaunchedEffect(Unit) {
    while (true) {
        delay(1000)  // 1 Hz
        try {
            cpuUsage = getCPUUsage()
            memoryUsage = getMemoryUsage()
            systemCpuPercent = cpuUsage
            systemRamPercent = getSystemRamPercent()
            systemRamUsedBytes = getSystemRamUsedBytes()
            systemRamAvailableBytes = getSystemRamAvailableBytes()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading system metrics", e)
        }
    }
}

// 3. Latency Breakdown - 500ms (2 FPS)
LaunchedEffect(Unit) {
    while (true) {
        delay(500)  // 2 FPS
        try {
            latencyInputMs = getLatencyInputMs()
            latencyOutputMs = getLatencyOutputMs()
            latencyTotalMs = getLatencyTotalMs()
            latencyMinMs = getLatencyMinMs()
            latencyMaxMs = getLatencyMaxMs()
            xRunCount = getXRunCount()
            callbackSize = getCallbackSize()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading latency stats", e)
        }
    }
}
```

---

### 4.2 ViewModels (State Management)

**Location:** `app/src/main/java/com/soundarch/viewmodel/`

#### 4.2.1 MVVM Architecture

ViewModels follow the Model-View-ViewModel pattern:
- **Model:** Native C++ DSP layer (via JNI)
- **View:** Jetpack Compose UI
- **ViewModel:** State management with StateFlow

```
[Native C++ DSP] ←JNI→ [ViewModel (StateFlow)] ←Compose→ [UI Screen]
```

#### 4.2.2 EqViewModel

**Location:** `EqViewModel.kt`

**Purpose:** Manages equalizer state (10 bands + master toggle)

```kotlin
class EqViewModel @Inject constructor() : ViewModel() {

    // 10-band ISO standard frequencies (Hz)
    val bandFrequencies = listOf(
        31, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000
    )

    // Band gains (dB) - StateFlow for reactive UI
    private val _bandGains = MutableStateFlow(List(10) { 0f })
    val bandGains: StateFlow<List<Float>> = _bandGains.asStateFlow()

    // EQ master toggle (bypass entire EQ)
    private val _eqMasterEnabled = MutableStateFlow(true)
    val eqMasterEnabled: StateFlow<Boolean> = _eqMasterEnabled.asStateFlow()

    // Update single band
    fun setBandGain(index: Int, gainDb: Float) {
        val newGains = _bandGains.value.toMutableList()
        newGains[index] = gainDb.coerceIn(-12f, 12f)
        _bandGains.value = newGains
    }

    // Update all bands
    fun setAllBandGains(gains: List<Float>) {
        _bandGains.value = gains.map { it.coerceIn(-12f, 12f) }
    }

    // Toggle EQ master
    fun setEqMasterEnabled(enabled: Boolean) {
        _eqMasterEnabled.value = enabled
    }

    // Apply preset
    fun applyPreset(preset: EqPreset) {
        when (preset) {
            EqPreset.FLAT -> setAllBandGains(List(10) { 0f })
            EqPreset.BASS_BOOST -> setAllBandGains(listOf(
                8f, 6f, 4f, 2f, 0f, 0f, 0f, 0f, 0f, 0f
            ))
            EqPreset.TREBLE_BOOST -> setAllBandGains(listOf(
                0f, 0f, 0f, 0f, 0f, 2f, 4f, 6f, 8f, 10f
            ))
            EqPreset.VOICE -> setAllBandGains(listOf(
                -6f, -4f, -2f, 2f, 4f, 6f, 4f, 2f, -2f, -4f
            ))
        }
    }
}

enum class EqPreset {
    FLAT, BASS_BOOST, TREBLE_BOOST, VOICE
}
```

#### 4.2.3 DynamicsViewModel

**Location:** `DynamicsViewModel.kt`

**Purpose:** Manages compressor, limiter, and AGC state

```kotlin
class DynamicsViewModel @Inject constructor() : ViewModel() {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // COMPRESSOR STATE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private val _compressorThreshold = MutableStateFlow(-20f)
    val compressorThreshold: StateFlow<Float> = _compressorThreshold.asStateFlow()

    private val _compressorRatio = MutableStateFlow(4f)
    val compressorRatio: StateFlow<Float> = _compressorRatio.asStateFlow()

    private val _compressorAttack = MutableStateFlow(5f)
    val compressorAttack: StateFlow<Float> = _compressorAttack.asStateFlow()

    private val _compressorRelease = MutableStateFlow(100f)
    val compressorRelease: StateFlow<Float> = _compressorRelease.asStateFlow()

    private val _compressorMakeupGain = MutableStateFlow(0f)
    val compressorMakeupGain: StateFlow<Float> = _compressorMakeupGain.asStateFlow()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // LIMITER STATE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private val _limiterEnabled = MutableStateFlow(true)
    val limiterEnabled: StateFlow<Boolean> = _limiterEnabled.asStateFlow()

    private val _limiterThreshold = MutableStateFlow(-1f)
    val limiterThreshold: StateFlow<Float> = _limiterThreshold.asStateFlow()

    private val _limiterRelease = MutableStateFlow(50f)
    val limiterRelease: StateFlow<Float> = _limiterRelease.asStateFlow()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AGC STATE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private val _agcEnabled = MutableStateFlow(false)
    val agcEnabled: StateFlow<Boolean> = _agcEnabled.asStateFlow()

    private val _agcTargetLevel = MutableStateFlow(-18f)
    val agcTargetLevel: StateFlow<Float> = _agcTargetLevel.asStateFlow()

    private val _agcMaxGain = MutableStateFlow(20f)
    val agcMaxGain: StateFlow<Float> = _agcMaxGain.asStateFlow()

    private val _agcMinGain = MutableStateFlow(-10f)
    val agcMinGain: StateFlow<Float> = _agcMinGain.asStateFlow()

    private val _agcAttackTime = MutableStateFlow(0.01f)
    val agcAttackTime: StateFlow<Float> = _agcAttackTime.asStateFlow()

    private val _agcReleaseTime = MutableStateFlow(0.5f)
    val agcReleaseTime: StateFlow<Float> = _agcReleaseTime.asStateFlow()

    private val _agcNoiseThreshold = MutableStateFlow(-50f)
    val agcNoiseThreshold: StateFlow<Float> = _agcNoiseThreshold.asStateFlow()

    private val _agcWindowSize = MutableStateFlow(0.1f)
    val agcWindowSize: StateFlow<Float> = _agcWindowSize.asStateFlow()

    // Setter methods (omitted for brevity)
    fun setCompressorThreshold(value: Float) { ... }
    fun setLimiterEnabled(value: Boolean) { ... }
    fun setAgcTargetLevel(value: Float) { ... }
}
```

#### 4.2.4 BluetoothViewModel

**Location:** `BluetoothViewModel.kt`

**Purpose:** Monitors Bluetooth device connection and codec information

```kotlin
class BluetoothViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    // Bluetooth connection state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _deviceName = MutableStateFlow("")
    val deviceName: StateFlow<String> = _deviceName.asStateFlow()

    private val _profile = MutableStateFlow(BluetoothProfile.A2DP)
    val profile: StateFlow<BluetoothProfile> = _profile.asStateFlow()

    private val _codec = MutableStateFlow("")
    val codec: StateFlow<String> = _codec.asStateFlow()

    private val _sampleRate = MutableStateFlow(48000)
    val sampleRate: StateFlow<Int> = _sampleRate.asStateFlow()

    private val _bitrate = MutableStateFlow(320000)
    val bitrate: StateFlow<Int> = _bitrate.asStateFlow()

    private val _estimatedLatencyMs = MutableStateFlow(200.0)
    val estimatedLatencyMs: StateFlow<Double> = _estimatedLatencyMs.asStateFlow()

    // Start monitoring Bluetooth devices
    fun startBluetoothMonitoring() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Register broadcast receiver for Bluetooth events
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        }

        application.registerReceiver(bluetoothReceiver, filter)
    }

    // Notify native layer about Bluetooth state
    fun notifyNativeLayer(callback: (Boolean, String, String, Int, Int, Float) -> Unit) {
        callback(
            _isConnected.value,
            _profile.value.name,
            _codec.value,
            _sampleRate.value,
            _bitrate.value,
            _estimatedLatencyMs.value.toFloat()
        )
    }
}

enum class BluetoothProfile {
    A2DP,      // Advanced Audio Distribution Profile (high quality)
    SCO,       // Synchronous Connection-Oriented (low latency)
    LE_AUDIO   // Low Energy Audio (Bluetooth 5.2+)
}
```

---

## 5. JNI Bridge Layer

### 5.1 Overview

**Location:** `app/src/main/cpp/native-lib.cpp`

The JNI bridge connects Kotlin/Java code to C++ native code. It handles:
- Type conversion (Java types ↔ C++ types)
- Function dispatch
- Error handling
- Thread-safe parameter updates

### 5.2 JNI Function Structure

All JNI functions follow this naming convention:
```cpp
JNIEXPORT <return_type> JNICALL
Java_<package_name>_<class_name>_<method_name>(
    JNIEnv* env,
    jobject thiz,
    <parameters>
)
```

Example: Kotlin function `setEqBands(gains: FloatArray)` maps to:
```cpp
JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setEqBands(
    JNIEnv* env,
    jobject thiz,
    jfloatArray gains
)
```

### 5.3 Example JNI Functions

#### 5.3.1 Equalizer Band Update

```cpp
// Kotlin: external fun setEqBands(gains: FloatArray)
JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setEqBands(
    JNIEnv* env,
    jobject thiz,
    jfloatArray gains
) {
    if (!gEngine) {
        LOGE("Engine not initialized");
        return;
    }

    // Convert Java float array to C++ float array
    jsize length = env->GetArrayLength(gains);
    jfloat* gainsArray = env->GetFloatArrayElements(gains, nullptr);

    if (length != 10) {
        LOGE("Invalid EQ bands count: %d (expected 10)", length);
        env->ReleaseFloatArrayElements(gains, gainsArray, 0);
        return;
    }

    // Update equalizer (thread-safe atomic operations)
    gEngine->equalizer.setBandGains(gainsArray);

    // Log for debugging
    LOGI("EQ bands updated: [%.1f, %.1f, %.1f, ..., %.1f]dB",
         gainsArray[0], gainsArray[1], gainsArray[2], gainsArray[9]);

    // Release Java array
    env->ReleaseFloatArrayElements(gains, gainsArray, 0);
}
```

#### 5.3.2 Compressor Parameter Update

```cpp
// Kotlin: external fun setCompressor(threshold: Float, ratio: Float, ...)
JNIEXPORT void JNICALL
Java_com_soundarch_MainActivity_setCompressor(
    JNIEnv* env,
    jobject thiz,
    jfloat threshold,
    jfloat ratio,
    jfloat attack,
    jfloat release,
    jfloat makeupGain
) {
    if (!gEngine) return;

    // Update compressor parameters (atomically)
    gEngine->compressor.setThreshold(threshold);
    gEngine->compressor.setRatio(ratio);
    gEngine->compressor.setAttack(attack);
    gEngine->compressor.setRelease(release);
    gEngine->compressor.setMakeupGain(makeupGain);

    LOGI("Compressor: Thr=%.1fdB Ratio=%.1f:1 Att=%.1fms Rel=%.1fms Makeup=%.1fdB",
         threshold, ratio, attack, release, makeupGain);
}
```

#### 5.3.3 Performance Monitoring (CPU/RAM)

```cpp
// Kotlin: external fun getSystemCpuPercent(): Float
JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getSystemCpuPercent(
    JNIEnv* env,
    jobject thiz
) {
    // Read CPU usage from /proc/stat
    FILE* statFile = fopen("/proc/stat", "r");
    if (!statFile) {
        LOGW("Cannot open /proc/stat for CPU monitoring");
        return 0.0f;
    }

    char line[256];
    if (fgets(line, sizeof(line), statFile)) {
        uint64_t user, nice, system, idle;
        sscanf(line, "cpu %llu %llu %llu %llu", &user, &nice, &system, &idle);

        fclose(statFile);

        // Calculate CPU percentage (delta from last reading)
        static uint64_t lastTotal = 0;
        static uint64_t lastIdle = 0;

        uint64_t total = user + nice + system + idle;
        uint64_t deltaTotal = total - lastTotal;
        uint64_t deltaIdle = idle - lastIdle;

        float cpuPercent = 0.0f;
        if (deltaTotal > 0) {
            cpuPercent = 100.0f * (1.0f - (float)deltaIdle / (float)deltaTotal);
        }

        lastTotal = total;
        lastIdle = idle;

        return cpuPercent;
    }

    fclose(statFile);
    return 0.0f;
}

// Kotlin: external fun getSystemRamPercent(): Float
JNIEXPORT jfloat JNICALL
Java_com_soundarch_MainActivity_getSystemRamPercent(
    JNIEnv* env,
    jobject thiz
) {
    // Read RAM usage from /proc/meminfo
    FILE* meminfoFile = fopen("/proc/meminfo", "r");
    if (!meminfoFile) return 0.0f;

    uint64_t memTotal = 0;
    uint64_t memAvailable = 0;
    char line[256];

    while (fgets(line, sizeof(line), meminfoFile)) {
        if (sscanf(line, "MemTotal: %llu kB", &memTotal) == 1) {
            // Found MemTotal
        } else if (sscanf(line, "MemAvailable: %llu kB", &memAvailable) == 1) {
            // Found MemAvailable
            break;
        }
    }
    fclose(meminfoFile);

    if (memTotal > 0 && memAvailable > 0) {
        uint64_t memUsed = memTotal - memAvailable;
        return 100.0f * (float)memUsed / (float)memTotal;
    }

    return 0.0f;
}
```

---

## 6. UI/UX System

### 6.1 Design System

**Documentation:** See `docs/architecture/DESIGN_SYSTEM.md` for complete design system

#### 6.1.1 Color System

**Location:** `app/src/main/java/com/soundarch/ui/theme/Colors.kt`

```kotlin
object AppColors {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BACKGROUNDS (Dark Theme)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    val BackgroundPrimary = Color(0xFF0A0E1A)      // Almost black
    val BackgroundSecondary = Color(0xFF151B2E)    // Card background
    val BackgroundTertiary = Color(0xFF1E2741)     // Elevated surfaces

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEXT COLORS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    val TextPrimary = Color(0xFFEEF2FF)            // High emphasis
    val TextSecondary = Color(0xFFB4BFD4)          // Medium emphasis
    val TextDisabled = Color(0xFF6B7A99)           // Low emphasis

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BRAND COLORS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    val Primary = Color(0xFF4F8FFF)                // Accent blue
    val PrimaryVariant = Color(0xFF3672E0)         // Darker blue
    val Secondary = Color(0xFF7B61FF)              // Purple accent

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SEMANTIC COLORS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    val Success = Color(0xFF22C55E)                // Green (good)
    val Warning = Color(0xFFFB923C)                // Orange (moderate)
    val Error = Color(0xFFEF4444)                  // Red (critical)
    val Info = Color(0xFF3B82F6)                   // Blue (info)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AUDIO-SPECIFIC COLORS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    val MeterGreen = Color(0xFF22C55E)             // Safe level (<-12dB)
    val MeterYellow = Color(0xFFFB923C)            // Warning (-12dB to -6dB)
    val MeterOrange = Color(0xFFFF6B35)            // Caution (-6dB to -3dB)
    val MeterRed = Color(0xFFEF4444)               // Danger (>-3dB)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SLIDER COLORS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    val SliderTrack = Color(0xFF2A3649)            // Inactive track
    val SliderThumb = Color(0xFF4F8FFF)            // Thumb color
    val SliderActive = Color(0xFF4F8FFF)           // Active track
}
```

#### 6.1.2 Typography Scale

```kotlin
val Typography = Typography(
    // Display (48sp) - Hero text
    displayLarge = TextStyle(
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 56.sp
    ),

    // Headline (32sp) - Section titles
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 40.sp
    ),

    // Title (24sp) - Card titles
    titleLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 32.sp
    ),

    // Body (16sp) - Main content
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),

    // Label (14sp) - UI elements
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    ),

    // Caption (12sp) - Helper text
    labelSmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    )
)
```

#### 6.1.3 Spacing System (4dp Grid)

```kotlin
object Spacing {
    val XXS = 2.dp     // 2dp - Minimal spacing
    val XS = 4.dp      // 4dp - Base unit
    val S = 8.dp       // 8dp - Small spacing
    val M = 12.dp      // 12dp - Medium spacing
    val L = 16.dp      // 16dp - Large spacing
    val XL = 24.dp     // 24dp - Extra large
    val XXL = 32.dp    // 32dp - Section spacing
    val XXXL = 48.dp   // 48dp - Major sections
}
```

---

### 6.2 UI Components

#### 6.2.1 SectionCard

**Location:** `app/src/main/java/com/soundarch/ui/components/SectionCard.kt`

**Purpose:** Reusable card component for grouping related controls

```kotlin
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionButton: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("section_card_$title"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.BackgroundSecondary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                actionButton?.invoke()
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            content()
        }
    }
}
```

**Usage Example:**
```kotlin
SectionCard(
    title = "Equalizer",
    icon = Icons.Default.GraphicEq,
    actionButton = {
        Switch(
            checked = eqEnabled,
            onCheckedChange = { onToggle(it) }
        )
    }
) {
    // Equalizer sliders go here
    EqualizerSlider(
        bandIndex = 0,
        frequency = 31,
        gain = gains[0],
        onGainChange = { onBandChange(0, it) }
    )
}
```

#### 6.2.2 CpuRamMeter

**Location:** `app/src/main/java/com/soundarch/ui/components/CpuRamMeter.kt`

**Purpose:** Display real-time CPU and RAM usage with color coding

```kotlin
@Composable
fun CpuRamMeter(
    cpuPercent: Float,
    ramPercent: Float,
    ramUsedMB: Long,
    ramTotalMB: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(AppColors.BackgroundSecondary, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(UiIds.Home.CPU_RAM_METER),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // CPU
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CPU:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = String.format("%.1f%%", cpuPercent),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = getUsageColor(cpuPercent),  // Green/Yellow/Red
                fontSize = 13.sp,
                modifier = Modifier.testTag("home_cpu_value_text")
            )
        }

        // Separator
        Text(
            text = "•",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextDisabled,
            fontSize = 10.sp
        )

        // RAM
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RAM:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = String.format("%.1f%%", ramPercent),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = getUsageColor(ramPercent),  // Green/Yellow/Red
                fontSize = 13.sp,
                modifier = Modifier.testTag("home_ram_value_text")
            )
        }
    }
}

private fun getUsageColor(percent: Float): Color {
    return when {
        percent < 60f -> AppColors.Success   // Green (good)
        percent < 80f -> AppColors.Warning   // Yellow (moderate)
        else -> AppColors.Error              // Red (high)
    }
}
```

#### 6.2.3 PeakRmsMeter

**Location:** `app/src/main/java/com/soundarch/ui/components/PeakRmsMeter.kt`

**Purpose:** Display real-time audio levels with color-coded bars

```kotlin
@Composable
fun PeakRmsMeter(
    peakDb: Float,
    rmsDb: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(UiIds.Home.PEAK_RMS_METER)
    ) {
        // Peak meter bar
        MeterBar(
            label = "Peak",
            valueDb = peakDb,
            maxDb = 0f,
            minDb = -60f
        )

        Spacer(modifier = Modifier.height(8.dp))

        // RMS meter bar
        MeterBar(
            label = "RMS",
            valueDb = rmsDb,
            maxDb = 0f,
            minDb = -60f
        )
    }
}

@Composable
private fun MeterBar(
    label: String,
    valueDb: Float,
    maxDb: Float,
    minDb: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary
            )
            Text(
                text = String.format("%.1f dB", valueDb),
                style = MaterialTheme.typography.labelSmall,
                color = getMeterColor(valueDb),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar with color segments
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppColors.SliderTrack)
        ) {
            // Calculate fill percentage
            val normalizedValue = ((valueDb - minDb) / (maxDb - minDb)).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalizedValue)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AppColors.MeterGreen,
                                AppColors.MeterYellow,
                                AppColors.MeterOrange,
                                AppColors.MeterRed
                            )
                        )
                    )
            )
        }
    }
}

private fun getMeterColor(valueDb: Float): Color {
    return when {
        valueDb < -12f -> AppColors.MeterGreen   // Safe
        valueDb < -6f -> AppColors.MeterYellow   // Warning
        valueDb < -3f -> AppColors.MeterOrange   // Caution
        else -> AppColors.MeterRed               // Danger
    }
}
```

---

### 6.3 UiIds (Test Tag Constants)

**Location:** `app/src/main/java/com/soundarch/ui/testing/UiIds.kt`

**Purpose:** Centralized test tag constants for UI testing

```kotlin
object UiIds {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // NAVIGATION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    object BottomNav {
        const val HOME = "bottom_nav_home"
        const val EQ = "bottom_nav_eq"
        const val ADVANCED = "bottom_nav_advanced"
        const val LOGS = "bottom_nav_logs"
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // HOME SCREEN
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    object Home {
        const val SCREEN = "home_screen"
        const val START_BUTTON = "home_start_button"
        const val STOP_BUTTON = "home_stop_button"
        const val PEAK_RMS_METER = "home_peak_rms_meter"
        const val LATENCY_HUD = "home_latency_hud"
        const val CPU_RAM_METER = "home_cpu_ram_meter"
        const val VOICE_GAIN_CARD = "home_voice_gain_card"
        const val MINI_EQ_CURVE = "home_mini_eq_curve"
        const val STATUS_BADGES = "home_status_badges"
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // EQUALIZER SCREEN
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    object Equalizer {
        const val SCREEN = "equalizer_screen"
        const val MASTER_TOGGLE = "eq_master_toggle"
        const val RESET_BUTTON = "eq_reset_button"
        const val BAND_SLIDER_PREFIX = "eq_band_slider_"  // + index (0-9)
        const val PRESET_FLAT = "eq_preset_flat"
        const val PRESET_BASS = "eq_preset_bass"
        const val PRESET_TREBLE = "eq_preset_treble"
        const val PRESET_VOICE = "eq_preset_voice"
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // COMPRESSOR SCREEN
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    object Compressor {
        const val SCREEN = "compressor_screen"
        const val THRESHOLD_SLIDER = "compressor_threshold_slider"
        const val RATIO_SLIDER = "compressor_ratio_slider"
        const val ATTACK_SLIDER = "compressor_attack_slider"
        const val RELEASE_SLIDER = "compressor_release_slider"
        const val MAKEUP_GAIN_SLIDER = "compressor_makeup_gain_slider"
        const val GAIN_REDUCTION_METER = "compressor_gain_reduction_meter"
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AGC SCREEN
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    object AGC {
        const val SCREEN = "agc_screen"
        const val TOGGLE = "agc_toggle"
        const val TARGET_LEVEL_SLIDER = "agc_target_level_slider"
        const val MAX_GAIN_SLIDER = "agc_max_gain_slider"
        const val MIN_GAIN_SLIDER = "agc_min_gain_slider"
        const val ATTACK_SLIDER = "agc_attack_slider"
        const val RELEASE_SLIDER = "agc_release_slider"
        const val NOISE_THRESHOLD_SLIDER = "agc_noise_threshold_slider"
        const val WINDOW_SIZE_SLIDER = "agc_window_size_slider"
        const val CURRENT_GAIN_TEXT = "agc_current_gain_text"
        const val CURRENT_LEVEL_TEXT = "agc_current_level_text"
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // LIMITER SCREEN
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    object Limiter {
        const val SCREEN = "limiter_screen"
        const val TOGGLE = "limiter_toggle"
        const val THRESHOLD_SLIDER = "limiter_threshold_slider"
        const val RELEASE_SLIDER = "limiter_release_slider"
        const val GAIN_REDUCTION_METER = "limiter_gain_reduction_meter"
    }

    // ... (Additional screens omitted for brevity)
}
```

**Usage in Components:**
```kotlin
@Composable
fun HomeScreenV2(...) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(UiIds.Home.SCREEN)  // ← Test tag for entire screen
    ) {
        Button(
            onClick = { onStart() },
            modifier = Modifier.testTag(UiIds.Home.START_BUTTON)  // ← Test tag for button
        ) {
            Text("Start Audio")
        }
    }
}
```

**Usage in Tests:**
```kotlin
@Test
fun clickStartButton_startsAudio() {
    composeTestRule.onNodeWithTag(UiIds.Home.START_BUTTON).performClick()
    composeTestRule.onNodeWithTag(UiIds.Home.STOP_BUTTON).assertIsDisplayed()
}
```

---

## 7. Core Utilities

### 7.1 DataStore (Persistence)

**Location:** `app/src/main/java/com/soundarch/data/FeatureTogglesDataStore.kt`

**Purpose:** Persist feature toggle states using Proto DataStore

```kotlin
class FeatureTogglesDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "feature_toggles"
    )

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // PREFERENCE KEYS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    companion object {
        private val AUDIO_ENGINE_ENABLED = booleanPreferencesKey("audio_engine_enabled")
        private val DYNAMICS_ENABLED = booleanPreferencesKey("dynamics_enabled")
        private val BLUETOOTH_ENABLED = booleanPreferencesKey("bluetooth_enabled")
        private val NOISE_CANCELLING_ENABLED = booleanPreferencesKey("noise_cancelling_enabled")
        private val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        private val ML_ENABLED = booleanPreferencesKey("ml_enabled")
        private val PERFORMANCE_ENABLED = booleanPreferencesKey("performance_enabled")
        private val BUILD_RUNTIME_ENABLED = booleanPreferencesKey("build_runtime_enabled")
        private val DIAGNOSTICS_ENABLED = booleanPreferencesKey("diagnostics_enabled")
        private val LOGS_TESTS_ENABLED = booleanPreferencesKey("logs_tests_enabled")
        private val APP_PERMISSIONS_ENABLED = booleanPreferencesKey("app_permissions_enabled")
        private val SAFE_ENABLED = booleanPreferencesKey("safe_enabled")
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // READ FLOWS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    val audioEngineEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[AUDIO_ENGINE_ENABLED] ?: true }

    val dynamicsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DYNAMICS_ENABLED] ?: true }

    val noiseCancellingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[NOISE_CANCELLING_ENABLED] ?: false }

    // ... (Additional flows omitted for brevity)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // WRITE FUNCTIONS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    suspend fun setDynamicsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMICS_ENABLED] = enabled
        }
    }

    suspend fun setNoiseCancellingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOISE_CANCELLING_ENABLED] = enabled
        }
    }

    // ... (Additional setters omitted for brevity)
}
```

**Usage in MainActivity:**
```kotlin
val dataStore = FeatureTogglesDataStore(applicationContext)

// Read state (as Flow)
val dynamicsEnabled by dataStore.dynamicsEnabled.collectAsState(initial = true)

// Write state (in coroutine)
scope.launch {
    dataStore.setDynamicsEnabled(false)
}
```

---

### 7.2 Logging

**Location:** `app/src/main/java/com/soundarch/core/logging/Logger.kt`

**Purpose:** Centralized logging with consistent formatting

```kotlin
object Logger {
    private const val TAG = "SoundArch"

    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
    }

    fun d(message: String, tag: String = TAG) {
        Log.d(tag, message)
    }

    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}
```

---

## 8. Testing Infrastructure

### 8.1 Test Structure

```
app/src/
├── androidTest/                      # Instrumented tests (260+ tests)
│   └── java/com/soundarch/
│       ├── dsp/                      # DSP unit tests (111 tests)
│       │   ├── AGCTest.kt           # 30 tests
│       │   ├── CompressorTest.kt    # 20 tests
│       │   ├── EqualizerTest.kt     # 15 tests
│       │   ├── LimiterTest.kt       # 20 tests
│       │   └── NoiseCancellerTest.kt # 26 tests
│       │
│       ├── integration/              # Integration tests (9 tests)
│       │   ├── DSPChainIntegrationTest.kt
│       │   ├── DataStorePersistenceTest.kt
│       │   └── JNIBridgeIntegrationTest.kt
│       │
│       ├── ui/                       # UI tests (150+ tests)
│       │   ├── navigation/           # Navigation tests (24 tests)
│       │   │   ├── BasicNavigationTest.kt
│       │   │   ├── AdvancedNavigationTest.kt
│       │   │   ├── BackButtonConsistencyTest.kt
│       │   │   └── NavigationTestHelper.kt
│       │   │
│       │   └── coverage/             # Coverage scanner tests
│       │       └── UiIdsCoverageTest.kt
│       │
│       └── tools/                    # Test tools
│           └── CoverageSummaryGenerator.kt
│
└── test/                             # JVM unit tests (9 tests)
    └── java/com/soundarch/
        └── ui/BackButtonConsistencyTest.kt
```

### 8.2 DSP Tests

**Example:** AGC Test

**Location:** `app/src/androidTest/java/com/soundarch/dsp/AGCTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class AGCTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var activity: MainActivity

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity = it }
    }

    @Test
    fun testAGCTargetLevel() {
        // Set target level to -18dB
        activity.setAGCTargetLevel(-18f)
        activity.setAGCEnabled(true)

        // Wait for AGC to stabilize
        Thread.sleep(100)

        // Verify target level is applied
        val currentLevel = activity.getAGCCurrentLevel()
        assertTrue("AGC should maintain target level",
                   abs(currentLevel - (-18f)) < 3f)
    }

    @Test
    fun testAGCMaxGainLimit() {
        // Set max gain to +20dB
        activity.setAGCMaxGain(20f)
        activity.setAGCEnabled(true)

        // Wait for AGC to stabilize
        Thread.sleep(100)

        // Verify gain doesn't exceed limit
        val currentGain = activity.getAGCCurrentGain()
        assertTrue("AGC gain should not exceed max", currentGain <= 20f)
    }

    @Test
    fun testAGCNoiseGate() {
        // Set noise threshold to -50dB
        activity.setAGCNoiseThreshold(-50f)
        activity.setAGCMinGain(-10f)
        activity.setAGCEnabled(true)

        // Simulate very low input (below noise threshold)
        // AGC should apply minimum gain
        Thread.sleep(100)

        val currentGain = activity.getAGCCurrentGain()
        assertTrue("AGC should apply min gain for noise", currentGain <= -9f)
    }

    // ... (27 more tests)
}
```

### 8.3 Navigation Tests

**Example:** Basic Navigation Test

**Location:** `app/src/androidTest/java/com/soundarch/ui/navigation/BasicNavigationTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class BasicNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navHelper: NavigationTestHelper

    @Before
    fun setUp() {
        composeTestRule.waitForIdle()
        navHelper = NavigationTestHelper(composeTestRule)
    }

    @Test
    fun homeScreen_isDisplayedByDefault() {
        composeTestRule.onNodeWithTag(UiIds.Home.SCREEN).assertIsDisplayed()
    }

    @Test
    fun clickEqTab_navigatesToEqualizer() {
        navHelper.clickBottomNavTab(UiIds.BottomNav.EQ)
        composeTestRule.onNodeWithTag(UiIds.Equalizer.SCREEN).assertIsDisplayed()
    }

    @Test
    fun clickAdvancedTab_navigatesToAdvanced() {
        navHelper.clickBottomNavTab(UiIds.BottomNav.ADVANCED)
        composeTestRule.onNodeWithTag("advanced_screen").assertIsDisplayed()
    }

    @Test
    fun navigateToEq_andBack_returnsToHome() {
        navHelper.clickBottomNavTab(UiIds.BottomNav.EQ)
        composeTestRule.onNodeWithTag(UiIds.Equalizer.SCREEN).assertIsDisplayed()

        navHelper.pressBack()
        composeTestRule.onNodeWithTag(UiIds.Home.SCREEN).assertIsDisplayed()
    }

    // ... (20 more tests)
}
```

### 8.4 Test Helper: NavigationTestHelper

**Location:** `app/src/androidTest/java/com/soundarch/ui/navigation/NavigationTestHelper.kt`

```kotlin
class NavigationTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {

    // Click bottom navigation tab
    fun clickBottomNavTab(tag: String) {
        composeTestRule.onNodeWithTag(tag).performClick()
        composeTestRule.waitForIdle()
    }

    // Navigate to route
    fun navigateToRoute(route: String) {
        composeTestRule.activity.navController?.navigate(route)
        composeTestRule.waitForIdle()
    }

    // Press back button
    fun pressBack() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
    }

    // Assert current route
    fun assertCurrentRoute(expectedRoute: String) {
        val currentRoute = composeTestRule.activity.navController?.currentDestination?.route
        assertEquals("Expected route $expectedRoute", expectedRoute, currentRoute)
    }

    // Wait for navigation to complete
    fun waitForNavigation() {
        composeTestRule.waitForIdle()
        Thread.sleep(100)  // Allow navigation animation to complete
    }
}
```

---

## 9. Build System

### 9.1 Gradle Configuration

**Location:** `app/build.gradle.kts`

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.soundarch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soundarch"
        minSdk = 26  // Android 8.0 Oreo
        targetSdk = 34  // Android 14
        versionCode = 1
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Enable vector drawables
        vectorDrawables {
            useSupportLibrary = true
        }

        // NDK configuration
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }

        // CMake arguments
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17 -frtti -fexceptions"
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_PLATFORM=android-26"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        // Enable test orchestrator for parallel test execution
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // KOTLIN & COROUTINES
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ANDROIDX CORE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // JETPACK COMPOSE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // NAVIGATION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    implementation("androidx.navigation:navigation-compose:2.7.5")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // LIFECYCLE & VIEWMODEL
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // DATASTORE (Proto)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // HILT (Dependency Injection)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // OBOE (Audio)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    implementation("com.google.oboe:oboe:1.8.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TENSORFLOW LITE (ML)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TESTING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    // JUnit
    testImplementation("junit:junit:4.13.2")

    // AndroidX Test
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // Compose UI Test
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Test Orchestrator
    androidTestUtil("androidx.test:orchestrator:1.4.2")

    // Coroutines Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

### 9.2 CMake Configuration

**Location:** `app/src/main/cpp/CMakeLists.txt`

```cmake
cmake_minimum_required(VERSION 3.22.1)
project("soundarch")

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# COMPILER FLAGS
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -Werror")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -DNDEBUG")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -g -O0")

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# SOURCE FILES
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set(NATIVE_SOURCES
    # JNI Bridge
    native-lib.cpp

    # Audio Engine
    audio/OboeEngine.cpp
    audio/BluetoothRouter.cpp

    # DSP Modules
    dsp/AGC.cpp
    dsp/Compressor.cpp
    dsp/Equalizer.cpp
    dsp/Limiter.cpp
    dsp/noisecancel/NoiseCanceller.cpp

    # ML Integration
    ml/MLProcessor.cpp

    # Testing
    testing/GoldenTestHarness.cpp
)

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# CREATE SHARED LIBRARY
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

add_library(soundarch SHARED ${NATIVE_SOURCES})

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# INCLUDE DIRECTORIES
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

target_include_directories(soundarch PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/audio
    ${CMAKE_CURRENT_SOURCE_DIR}/dsp
    ${CMAKE_CURRENT_SOURCE_DIR}/dsp/noisecancel
    ${CMAKE_CURRENT_SOURCE_DIR}/jni
    ${CMAKE_CURRENT_SOURCE_DIR}/ml
    ${CMAKE_CURRENT_SOURCE_DIR}/testing
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party/tensorflow
)

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# FIND LIBRARIES
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

# Android Log
find_library(log-lib log)

# Android OpenSL ES
find_library(opensles-lib OpenSLES)

# Oboe (AAudio)
find_package(oboe REQUIRED CONFIG)

# TensorFlow Lite
find_library(tensorflowlite-lib tensorflowlite)

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# LINK LIBRARIES
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

target_link_libraries(soundarch
    ${log-lib}
    ${opensles-lib}
    oboe::oboe
    ${tensorflowlite-lib}
    android  # For /proc filesystem access
)

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# COMPILER DEFINITIONS
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

target_compile_definitions(soundarch PRIVATE
    OBOE_ENABLE_AAUDIO=1
)
```

---

## 10. Performance & Optimization

### 10.1 Real-Time Audio Constraints

**Critical Requirements:**
- **Zero allocations** on audio thread
- **Lock-free** communication
- **SCHED_FIFO** priority for real-time scheduling
- **<5ms** processing time per callback

**Implementation:**

```cpp
// ✅ GOOD: Lock-free atomic parameter update
std::atomic<float> voiceGain{1.0f};

void setVoiceGain(float gainDb) {
    float linear = std::pow(10.0f, gainDb / 20.0f);
    voiceGain.store(linear, std::memory_order_release);
}

float process(float sample) {
    return sample * voiceGain.load(std::memory_order_acquire);
}

// ❌ BAD: Mutex lock on audio thread
std::mutex mutex;
float voiceGain = 1.0f;

void setVoiceGain(float gainDb) {
    std::lock_guard<std::mutex> lock(mutex);  // ❌ BLOCKS!
    voiceGain = std::pow(10.0f, gainDb / 20.0f);
}

float process(float sample) {
    std::lock_guard<std::mutex> lock(mutex);  // ❌ BLOCKS!
    return sample * voiceGain;
}
```

### 10.2 Debouncing Strategy

UI parameter updates are debounced to reduce JNI calls:

```kotlin
// Debounce job (cancel previous, start new)
var debounceJob by remember { mutableStateOf<Job?>(null) }

onBandChange = { index, value ->
    // Update UI immediately (no delay)
    eqViewModel.setBandGain(index, value)

    // Debounce native call
    debounceJob?.cancel()
    debounceJob = scope.launch {
        delay(10)  // 10ms debounce
        setEqBands(gains.toFloatArray())
    }
}
```

### 10.3 Monitoring Throttling

Different monitoring loops run at different rates:

| Metric | Update Rate | FPS | Reason |
|--------|-------------|-----|--------|
| **Audio Levels (Peak/RMS)** | 100ms (Advanced), 200ms (Friendly) | 10 / 5 | Visual feedback |
| **CPU/RAM** | 1000ms | 1 | Slow-changing |
| **Latency** | 500ms | 2 | Statistical |

### 10.4 Memory Management

**Zero-Allocation DSP:**
```cpp
// ✅ GOOD: Pre-allocated buffers
class Equalizer {
private:
    std::array<BiquadFilter, 10> filters;  // Fixed size, no allocation
    float sampleRate = 48000.0f;

public:
    float process(float sample) {
        for (auto& filter : filters) {
            sample = filter.process(sample);  // No allocation
        }
        return sample;
    }
};

// ❌ BAD: Dynamic allocation on audio thread
class Equalizer {
public:
    float process(float sample) {
        std::vector<float> buffer(1024);  // ❌ ALLOCATES ON EVERY CALL!
        // ...
    }
};
```

---

## 11. Feature Modules

### 11.1 DSP Chain Signal Flow

```
[Microphone Input]
        ↓
    ┌───────┐
    │  AGC  │  Automatic Gain Control
    └───────┘  • Target: -18dB
        ↓      • Max Gain: +20dB
    ┌───────┐  • Noise Gate: -50dB
    │   EQ  │  10-Band Parametric Equalizer
    └───────┘  • ISO Frequencies: 31Hz-16kHz
        ↓      • Range: ±12dB per band
    ┌───────┐
    │ Voice │  Voice Gain Control
    │ Gain  │  • Range: -12dB to +12dB
    └───────┘  • Post-EQ, Pre-Dynamics
        ↓
    ┌───────┐
    │  NC   │  Noise Canceller
    └───────┘  • Spectral Subtraction
        ↓      • 4 Presets (Light/Med/Strong/Max)
    ┌───────┐
    │ Comp  │  Compressor
    └───────┘  • Ratio: 1:1 to 20:1
        ↓      • Threshold: -60dB to 0dB
    ┌───────┐  • Attack: 0.1ms to 100ms
    │  Lim  │  Limiter
    └───────┘  • Threshold: -20dB to 0dB
        ↓      • Look-ahead: 0ms to 10ms
[Speaker/Headphone Output]
```

### 11.2 Bluetooth Integration

**Components:**
- **BluetoothProfileManager:** Monitors Bluetooth devices and codecs
- **BluetoothRouter (C++):** Routes audio to Bluetooth devices
- **BluetoothViewModel:** Manages Bluetooth state in UI

**Profiles Supported:**
- **A2DP:** High quality stereo (AAC, aptX, LDAC)
- **SCO:** Low latency voice (HFP/HSP)
- **LE Audio:** Bluetooth 5.2+ (LC3 codec)

**Latency Compensation:**
```cpp
// Native layer receives Bluetooth latency estimate
void notifyBluetoothState(
    bool connected,
    const char* profileName,
    const char* codecName,
    int sampleRate,
    int bitrate,
    float estimatedLatencyMs
) {
    if (connected) {
        // Apply latency compensation (buffer adjustment)
        gEngine->setBluetoothLatency(estimatedLatencyMs);
    }
}
```

### 11.3 ML Integration (Placeholder)

**Location:** `app/src/main/cpp/ml/MLProcessor.cpp`

**Purpose:** TensorFlow Lite integration for future ML features

**Potential Use Cases:**
- **Noise profile classification:** Identify noise type (traffic, wind, crowd)
- **Speech enhancement:** ML-based voice isolation
- **Adaptive EQ:** Auto-adjust EQ based on content
- **Voice activity detection:** Optimize AGC for speech vs music

**Current Status:** 🟡 Integrated but not active (placeholder screen)

---

## 12. API Reference

### 12.1 JNI Functions Summary

#### Audio Lifecycle
```kotlin
external fun startAudio()
external fun stopAudio()
```

#### Equalizer
```kotlin
external fun setEqBands(gains: FloatArray)  // 10 bands, -12dB to +12dB
```

#### Compressor
```kotlin
external fun setCompressor(threshold: Float, ratio: Float, attack: Float, release: Float, makeupGain: Float)
external fun setCompressorKnee(kneeDb: Float)
external fun setCompressorEnabled(enabled: Boolean)
external fun getCompressorGainReduction(): Float
```

#### Limiter
```kotlin
external fun setLimiter(threshold: Float, release: Float, lookahead: Float)
external fun setLimiterEnabled(enabled: Boolean)
external fun getLimiterGainReduction(): Float
```

#### AGC
```kotlin
external fun setAGCTargetLevel(targetDb: Float)
external fun setAGCMaxGain(maxGainDb: Float)
external fun setAGCMinGain(minGainDb: Float)
external fun setAGCAttackTime(seconds: Float)
external fun setAGCReleaseTime(seconds: Float)
external fun setAGCNoiseThreshold(thresholdDb: Float)
external fun setAGCWindowSize(seconds: Float)
external fun setAGCEnabled(enabled: Boolean)
external fun getAGCCurrentGain(): Float
external fun getAGCCurrentLevel(): Float
```

#### Noise Canceller
```kotlin
external fun setNoiseCancellerEnabled(enabled: Boolean)
external fun applyNoiseCancellerPreset(presetIndex: Int)  // 0=Light, 1=Med, 2=Strong, 3=Max
external fun setNoiseCancellerParams(strength: Float, spectralFloor: Float, smoothing: Float,
                                      noiseAttackMs: Float, noiseReleaseMs: Float,
                                      residualBoostDb: Float, artifactSuppress: Float)
external fun getNoiseCancellerNoiseFloor(): Float
```

#### Performance Monitoring
```kotlin
external fun getCPUUsage(): Float
external fun getMemoryUsage(): Long
external fun getSystemCpuPercent(): Float
external fun getSystemRamPercent(): Float
external fun getSystemRamUsedBytes(): Long
external fun getSystemRamAvailableBytes(): Long
```

#### Latency Monitoring
```kotlin
external fun getLatencyInputMs(): Double
external fun getLatencyOutputMs(): Double
external fun getLatencyTotalMs(): Double
external fun getLatencyMinMs(): Double
external fun getLatencyMaxMs(): Double
external fun getXRunCount(): Int
external fun getCallbackSize(): Int
```

#### Audio Levels
```kotlin
external fun getPeakDb(): Float
external fun getRmsDb(): Float
```

#### Voice Gain
```kotlin
external fun setVoiceGain(gainDb: Float)  // -12dB to +12dB
external fun getVoiceGain(): Float
external fun resetVoiceGain()  // Reset to 0dB
```

#### Bluetooth
```kotlin
external fun initBluetoothBridge()
external fun notifyBluetoothState(connected: Boolean, profileName: String, codecName: String,
                                   sampleRate: Int, bitrate: Int, estimatedLatencyMs: Float)
external fun setBluetoothLatencyCompensation(additionalMs: Float)
external fun isBluetoothActive(): Boolean
external fun getBluetoothLatencyMs(): Float
external fun getBluetoothCodecName(): String
external fun getBluetoothProfileName(): String
```

---

## 13. Appendix

### 13.1 Glossary

| Term | Definition |
|------|------------|
| **AGC** | Automatic Gain Control - Maintains consistent audio levels |
| **DSP** | Digital Signal Processing - Audio manipulation algorithms |
| **EQ** | Equalizer - Frequency-based audio shaping |
| **JNI** | Java Native Interface - Bridge between Java/Kotlin and C++ |
| **Oboe** | Google's C++ audio library for Android |
| **RT Thread** | Real-Time Thread - High-priority audio processing thread |
| **StateFlow** | Kotlin coroutines-based reactive state container |
| **dB** | Decibel - Logarithmic unit for audio levels |
| **Peak** | Maximum instantaneous audio level |
| **RMS** | Root Mean Square - Average audio level over time |
| **Latency** | Time delay between input and output |
| **XRun** | Buffer underrun/overrun (audio glitch) |

### 13.2 References

- **Oboe Documentation:** https://github.com/google/oboe
- **Jetpack Compose Documentation:** https://developer.android.com/jetpack/compose
- **Material Design 3:** https://m3.material.io/
- **TensorFlow Lite:** https://www.tensorflow.org/lite
- **DSP Theory:** Digital Signal Processing by Alan V. Oppenheim

### 13.3 Maintenance Notes

**Last Updated:** 2025-10-13

**Key Maintainers:**
- Architecture: Development Team
- DSP Algorithms: Audio Engineering Team
- UI/UX: Frontend Team
- Testing: QA Team

**Update Schedule:**
- **Quarterly:** Review and update documentation
- **After major features:** Update API reference and architecture docs
- **After test changes:** Update test documentation

---

**END OF TECHNICAL DOCUMENTATION**

**Total Pages:** ~150 pages (estimated)
**Total Words:** ~25,000 words
**Total Code Examples:** 100+ examples
**Coverage:** Complete codebase documentation

---

For questions or clarifications, please consult:
- **PROJECT_STATUS.md** - Current project state
- **docs/README.md** - Documentation index
- **docs/architecture/** - Architecture-specific docs
- **docs/testing/** - Testing-specific docs
