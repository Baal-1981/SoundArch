#pragma once

#include <array>
#include <cmath>
#include <algorithm>

namespace soundarch::dsp {

    class AGC {
    public:
        explicit AGC(float sampleRate);

        // Configuration
        void setTargetLevel(float dbfs) noexcept;      // -20 dBFS typique
        void setAttackTime(float seconds) noexcept;    // 2-10s
        void setReleaseTime(float seconds) noexcept;   // 10-30s
        void setMaxGain(float db) noexcept;            // +30 dB max
        void setMinGain(float db) noexcept;            // -20 dB min
        void setNoiseThreshold(float dbfs) noexcept;   // -60 dBFS typique
        void setWindowSize(float seconds) noexcept;    // 0.5-2s

        // Traitement
        float process(float input) noexcept;
        void reset() noexcept;

        // Monitoring (pour UI)
        float getCurrentGain() const noexcept { return currentGainDb_; }
        float getCurrentLevel() const noexcept { return currentLevelDb_; }

    private:
        void updateCoefficients() noexcept;
        float calculateRMS() noexcept;

        // Config
        float sampleRate_;
        float targetLevelDb_{-20.0f};
        float maxGainDb_{30.0f};
        float minGainDb_{-20.0f};
        float noiseThresholdDb_{-60.0f};

        // Timing
        float attackCoef_{0.0f};
        float releaseCoef_{0.0f};

        // RMS Detection
        static constexpr size_t kMaxWindowSize = 96000; // 2s @ 48kHz
        std::array<float, kMaxWindowSize> rmsBuffer_{};
        size_t windowSize_{24000}; // 500ms @ 48kHz
        size_t writeIndex_{0};
        float rmsSum_{0.0f};

        // State
        float currentGainDb_{0.0f};
        float currentLevelDb_{-60.0f};
        bool isFrozen_{false};
    };

} // namespace soundarch::dsp