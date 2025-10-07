# SoundArch v2.0 🎧

**Application d'aide auditive professionnelle Android avec latence ultra-basse (<10 ms), traitement audio en temps réel (DSP) et intelligence adaptative (ML).**

---

## 🔥 Caractéristiques principales

* ⏱️ **Latence audio ultra-basse** (< 10 ms round-trip via Oboe + NDK)
* 🎧️ **DSP modulaire**: Égaliseur 10 bandes, compresseur, limiteur, réduction de bruit
* 🧠 **Intelligence adaptative**: Modèle TFLite psychoacoustique et VAD embarqué
* 🎨 **UI moderne**: Jetpack Compose + Material Design 3
* 🎧 **Support Bluetooth**: Routage intelligent vers Buds / casques pros
* 📉 **Monitoring temps réel**: Latence, RMS, visualiseur d’onde

---

## 🧱 Architecture

```
Layer 1: Kotlin + Jetpack Compose (UI, Permissions, Bluetooth)
Layer 2: C++ Oboe + DSP temps réel + JNI
Layer 3: Python (ML training) → TFLite (inference native)
```

---

## 📂 Structure du projet

```
SoundArch/
├── app/
│   ├── src/main/java/com/soundarch/       # Kotlin UI, VM, Repos
│   ├── src/main/cpp/                      # C++ Oboe, DSP, ML
│   ├── res/                               # UI resources
│   └── AndroidManifest.xml
├── ml-training/                           # Scripts Python TFLite
├── build.gradle.kts                       # Config globale
├── settings.gradle.kts
└── README.md                              # ← Ce fichier
```

---

## ⚙️ Stack technique

* **Langages**: Kotlin, C++, Python
* **Libs**:

  * [Google Oboe](https://github.com/google/oboe) (Audio NDK)
  * Jetpack Compose (UI moderne)
  * TensorFlow Lite (ML embarqué)
* **Tools**: NDK r25+, CMake 3.22+, Git, Docker, Android Studio 2025.1

---

## 🚀 Getting Started

### Pré-requis

* Android Studio 2025.1+
* NDK (Side by side) v25+
* CMake 3.22+
* Python 3.9+
* Appareils: Pixel 5 (debug), Samsung S23 FE (release)

### Build

```bash
git clone https://github.com/Baal-1981/SoundArch.git
cd SoundArch
# Ouvrir avec Android Studio
```

---

## 📊 Mesures de performance

| Test                    | Résultat               |
| ----------------------- | ---------------------- |
| Latence audio roundtrip | **< 10 ms** ✅          |
| Stabilisé audio         | **> 1h sans glitch** ✅ |
| UI FrameRate            | **60 FPS** ✅           |

---

## 🧠 Machine Learning

* **Modèles embarqués**:

  * `psychoacoustic_v1.tflite` – profil auditif personnalisé
  * `vad_v1.tflite` – détection voix / bruit
* **Training**: `ml-training/*.py` avec export `.tflite`

---

## 🧺 Tests

* `app/src/test/` – Unit tests (ViewModel, Logic)
* `cpp/` – GoogleTest pour DSP
* Integration tests Android Instrumented

---

## 📄 Licence

Open-source – MIT License.

---

## ✨ Crédits

Projet initié par [@Baal-1981](https://github.com/Baal-1981).
Architecture audio inspirée par les standards militaires & pro audio.
