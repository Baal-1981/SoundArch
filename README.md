# SoundArch v2.0 🎧

## 🇬🇧 English Version

**SoundArch** is a professional hearing-assistance Android app with ultra-low audio latency (<10 ms), real-time native DSP processing, smart Bluetooth routing, and embedded on-device ML models.

---

### 🔥 Key Features

* ⏱️ Ultra-low latency audio I/O via Oboe (NDK)
* 🎛️ Modular DSP chain: Equalizer, Compressor, Limiter (planned), Noise Reduction (planned)
* 🧠 Embedded ML: Psychoacoustic adaptation and Voice Activity Detection via TFLite
* 🎨 Jetpack Compose modern UI with Material 3
* 🎧 Smart Bluetooth routing to supported devices (Buds, Jabra, etc.)
* 📉 Real-time monitoring: latency, RMS, EQ state

---

### 🧱 Native Audio Pipeline

```
Microphone
   ↓
OboeEngine (C++)
   ↓
Audio Thread Callback
   ├─ Equalizer (10-band Biquad IIR)
   ├─ Compressor
   ├─ [Limiter] (planned)
   ├─ [NoiseReduction, Beamforming] (prepared)
   └─ [TFLite ML]: Psychoacoustic + VAD
   ↓
Oboe OutputStream → Headphones / Bluetooth
```

* Thread-safe: `noexcept`, lock-free buffers
* Real-time safe: no memory allocation in callback

---

### 📂 Project Structure

```
SoundArch/
├── app/
│   ├── java/com/soundarch/    → Kotlin UI, ViewModels
│   ├── cpp/                   → OboeEngine, DSP, JNI, ML
│   └── res/                   → UI Resources
├── ml-training/               → Python scripts (TFLite export)
├── build.gradle.kts
└── README.md
```

---

### ⚙️ Native Modules

* **OboeEngine**: Low-latency I/O, buffer sync, internal latency tracker
* **Equalizer**: 10-band biquad, real-time coeff update
* **Compressor**: RMS follower, threshold, gain
* **Limiter / NR / Beamforming**: Staged, not yet active
* **ML**: `TFLiteEngine` runs embedded psychoacoustic & VAD models
* **JNI**: Optimized bridge with batch `float[]` for EQ, no per-frame calls

---

### 🖥️ Kotlin UI

* Home screen with latency meter, RMS visualizer
* Equalizer screen with 10 sliders synced to native DSP
* ViewModel observes native metrics via StateFlow

---

### 🚀 Build Instructions

* Android Studio 2025.1+
* NDK r25+, CMake 3.22+
* Python 3.9+ for ML models

```bash
git clone https://github.com/Baal-1981/SoundArch.git
cd SoundArch
# Open with Android Studio and build
```

---

### 📊 Runtime Metrics

| Metric         | Source                    |
| -------------- | ------------------------- |
| Latency (ms)   | `getCurrentLatency()` JNI |
| RMS Level (dB) | Audio callback            |
| EQ State       | `float[10]` batch JNI     |

---

### 🤖 TFLite Models

* `psychoacoustic_v1.tflite`: Personalized EQ adaptation
* `vad_v1.tflite`: Voice Activity Detection for gating
* `TFLiteEngine` handles real-time inference in C++

---

### 🧪 Testing

* Kotlin unit tests for ViewModel
* JNI and DSP tested manually
* GoogleTest planned for C++ modules

---

### 📄 License

MIT License – open source.

---

### 🙌 Author

Maintained by [@Baal-1981](https://github.com/Baal-1981). Inspired by professional audio and defense-grade latency systems.

---

## 🇫🇷 Version Française

**SoundArch** est une application Android d'assistance auditive professionnelle avec latence ultra-basse (<10 ms), traitement DSP natif en temps réel, routage Bluetooth intelligent, et modèles ML embarqués sur l'appareil.

---

### 🔥 Fonctionnalités principales

* ⏱️ Latence audio ultra-faible via Oboe (NDK)
* 🎛️ Chaîne DSP modulaire : Égaliseur, compresseur, limiteur (prévu), réduction de bruit (préparée)
* 🧠 ML embarqué : adaptation psychoacoustique et détection vocale (TFLite)
* 🎨 UI moderne Jetpack Compose + Material 3
* 🎧 Routage Bluetooth intelligent vers appareils compatibles (Buds, Jabra, etc.)
* 📉 Monitoring temps réel : latence, RMS, égaliseur

---

### 🧱 Pipeline audio natif

```
Entrée micro
   ↓
OboeEngine (C++)
   ↓
Thread audio temps réel
   ├─ Equalizer (10 bandes Biquad IIR)
   ├─ Compresseur
   ├─ [Limiteur] (prévu)
   ├─ [Réduction de bruit, Beamforming] (préparé)
   └─ [TFLite ML] : Psychoacoustique + VAD
   ↓
OutputStream Oboe → Casque / Bluetooth
```

* Thread-safe : `noexcept`, buffers lock-free
* Safe temps réel : aucune allocation mémoire en callback

---

### 📂 Structure du projet

```
SoundArch/
├── app/
│   ├── java/com/soundarch/    → UI Kotlin, ViewModels
│   ├── cpp/                   → Moteur audio, DSP, JNI, ML
│   └── res/                   → Ressources UI
├── ml-training/               → Scripts Python (export TFLite)
├── build.gradle.kts
└── README.md
```

---

### ⚙️ Modules natifs

* **OboeEngine** : I/O faible latence, suivi de latence
* **Equalizer** : 10 bandes, biquad, recalcul dynamique
* **Compresseur** : suivi RMS, seuil, gain
* **Limiteur / NR / Beamforming** : en attente d’intégration
* **ML** : `TFLiteEngine` → psychoacoustique + VAD embarqués
* **JNI** : passerelle optimisée (`float[10]`, sans appel frame-par-frame)

---

### 🖥️ UI Kotlin Compose

* Écran d’accueil : latence, visualiseur RMS, Start/Stop
* Écran égaliseur : 10 sliders liés au DSP natif
* ViewModel observe les métriques via StateFlow

---

### 🚀 Instructions de build

* Android Studio 2025.1+
* NDK r25+, CMake 3.22+
* Python 3.9+ (pour export ML)

```bash
git clone https://github.com/Baal-1981/SoundArch.git
cd SoundArch
# Ouvrir avec Android Studio et compiler
```

---

### 📊 Métriques runtime

| Indicateur   | Source                      |
| ------------ | --------------------------- |
| Latence (ms) | `getCurrentLatency()` JNI   |
| RMS (dB)     | Calculé en C++              |
| Égaliseur    | Tableau `float[10]` via JNI |

---

### 🤖 Modèles TFLite

* `psychoacoustic_v1.tflite` : adaptation auditif personnalisé
* `vad_v1.tflite` : détection voix vs bruit (gating)
* `TFLiteEngine` : inférence native C++ temps réel

---

### 🧪 Tests

* Tests unitaires ViewModel Kotlin
* Tests JNI/DSP manuels OK
* GoogleTest prévu pour modules C++

---

### 📄 Licence

Licence MIT – open source

---

### 🙌 Auteur

Développé par [@Baal-1981](https://github.com/Baal-1981). Inspiré par les systèmes audio pro et défense (latence <10 ms).
