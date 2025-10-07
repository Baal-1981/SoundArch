# SoundArch v2.0 🎧

> **A professional-grade Android hearing assistant app with native DSP, ultra-low latency, Bluetooth routing, and embedded ML.**

<p align="center">
  <img src="https://img.shields.io/badge/Latency-%3C10ms-green"/>
  <img src="https://img.shields.io/badge/Audio-DSP%20Real--Time-blue"/>
  <img src="https://img.shields.io/badge/ML-TFLite%20on--device-orange"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-purple"/>
</p>

---

## 🇬🇧 ENGLISH VERSION — TECHNICAL OVERVIEW

### 🎯 Purpose

SoundArch is designed for:

* Assisting hearing-impaired users with **adaptive, low-latency audio correction**
* **Realtime signal processing**: Equalization, Compression, Psychoacoustic tuning
* **Smart routing**: Bluetooth beamforming and mic-array directionality (planned)
* **Edge ML**: personalized gain profiles with `.tflite` inference

🔧 It targets professional use cases (medical, industrial, defense) with latency under 10ms and modular design.

📍 **Hardware targeted**: Pixel 5, S23 FE, Galaxy Buds 3 Pro, Jabra Evolve 2 — with support for mic arrays and custom audio HAL in future.

---

### 📁 Full Project Structure

```
SoundArch/
├── app/
│   ├── src/main/java/com/soundarch/
│   │   ├── MainActivity.kt                  # Entry point + permissions
│   │   ├── ui/
│   │   │   ├── screens/                    # HomeScreen, EqualizerScreen, etc.
│   │   │   ├── components/                 # LatencyIndicator, AudioVisualizer
│   │   │   └── navigation/                 # NavGraph.kt
│   │   ├── viewmodels/                     # AudioViewModel, etc.
│   │   ├── data/                           # Repositories, models
│   │   ├── utils/                          # Helpers, Bluetooth manager
│   │   └── native/NativeAudioEngine.kt     # JNI wrapper to C++
│   ├── src/main/cpp/
│   │   ├── native-lib.cpp                  # JNI Entry point
│   │   ├── audio/                          # OboeEngine, Buffer, Latency
│   │   ├── dsp/                            # Equalizer, Compressor, Limiter
│   │   ├── ml/                             # TFLiteEngine, VAD, PsychoAcoustic
│   │   └── utils/                          # Logger, RingBuffer
├── ml-training/                            # Python: train + export .tflite
├── build.gradle.kts                        # Kotlin DSL build config
└── README.md                               # This file
```

---

### 🧠 Audio Engine Breakdown (C++)

#### `OboeEngine.h/.cpp`

* `initialize(int sampleRate)` – Builds input and output Oboe streams at given sample rate, configures low-latency exclusive mode.
* `start()` – Starts both audio streams and sets `isRunning_ = true`
* `stop()` – Stops both streams safely
* `release()` – Releases streams and buffers
* `onAudioReady()` – Oboe callback: reads input, calls processing chain, writes to output stream
* `setAudioCallback()` – Assigns DSP processing callback
* `getCurrentLatencyMs()` – Returns current estimated latency (output frame position - input frame position)

#### `LatencyMonitor.h/.cpp`

* Tracks input/output timestamps to compute round-trip latency over time
* Uses `atomic<double>` to avoid locks

#### `AudioBuffer.h`

* Defines a lock-free circular buffer (single-producer single-consumer) for safe transfer between threads

#### `BluetoothRouter.cpp`

* Manages detection and routing to Bluetooth output devices (planned)

---

### 🎛️ DSP Modules

#### `Equalizer.h/.cpp`

* 10-band Biquad filter bank
* `setBandGain(band, gainDb)` – Updates coefficients live
* `process(float)` – Sequentially applies each filter
* `reset()` – Resets all filters’ states

#### `Compressor.h/.cpp`

* `setThreshold()`, `setRatio()`, `setAttack()`, `setRelease()` – Sets compression parameters
* `process(float)` – Applies RMS envelope tracking and dynamic gain reduction

---

### 🤖 ML C++ Engine

#### `TFLiteEngine.cpp`

* Loads `.tflite` models from asset path
* Sets up TensorFlow Lite interpreter in C++
* `runInference(input, output)` – Runs prediction using allocated tensors

#### `PsychoAcoustic.cpp`, `VAD.cpp`

* Wrap logic for psychoacoustic filtering and speech detection (WIP)

---

### 🔗 JNI Bridge (native-lib.cpp)

* Exposes `initialize`, `start`, `stop`, `release`, `setEqBands(float[])` to Kotlin
* All methods use batch processing and avoid local ref leaks

```cpp
JNIEXPORT jboolean JNICALL initialize(JNIEnv*, jobject, jint sampleRate);
JNIEXPORT jboolean JNICALL start(JNIEnv*, jobject);
JNIEXPORT void JNICALL stop(JNIEnv*, jobject);
JNIEXPORT void JNICALL release(JNIEnv*, jobject);
JNIEXPORT void JNICALL setEqBands(JNIEnv*, jobject, jfloatArray);
JNIEXPORT jdouble JNICALL getCurrentLatency(JNIEnv*, jobject);
```

---

### 🖼️ Kotlin UI (Jetpack Compose)

#### `MainActivity.kt`

* Entry point, requests permissions
* Hosts navigation and content setup

#### `ui/screens/`

* `HomeScreen.kt` → Displays latency, EQ access
* `EqualizerScreen.kt` → 10-band EQ sliders
* `BluetoothScreen.kt`, `AudioTestScreen.kt` → placeholders

#### `components/`

* `LatencyIndicator` – visual ring showing ms delay
* `AudioVisualizer` – simple waveform based on RMS
* `MetricsCard` – summary of runtime audio stats

#### `viewmodels/AudioViewModel.kt`

* Maintains state using `StateFlow`
* Triggers native methods (start, stop, update EQ)

```kotlin
val latency: StateFlow<Double>
val rmsLevel: StateFlow<Float>
val eqProfile: StateFlow<FloatArray>
```

---

### 📊 Runtime Metrics

* `latency` – JNI call
* `rmsLevel` – computed per block
* `eqProfile` – pushed to C++

---

### 🧪 Testing

* `AudioViewModelTest.kt` – Compose lifecycle + toggle
* C++ planned: GoogleTest for DSP correctness
* JNI: instrumentation via AndroidTest (future)

---

### 📅 Roadmap

* [x] EQ + Compressor
* [ ] Limiter + Psycho/VAD
* [ ] Adaptive ML tuning
* [ ] Bluetooth routing + beamforming

---

### 🙏 Credits

Developed by **Baal-1981**, in collaboration with GPT-4o, targeting audio DSP for hearing sciences, embedded ML and real-time mobile apps.

---

## 🇫🇷 VERSION FRANÇAISE — SPÉCIFICATION TECHNIQUE

[Traduction complète identique à venir dans le bloc suivant pour assurer la continuité, complète et aussi détaillée.]
