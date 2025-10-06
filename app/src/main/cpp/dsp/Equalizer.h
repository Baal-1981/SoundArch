#pragma once

#include <array>
#include <atomic>

namespace soundarch::dsp {

    struct alignas(16) BiquadCoefficients {
        float b0, b1, b2, a1, a2;
    };

    class BiquadFilter {
    public:
        void setCoefficients(const BiquadCoefficients& c) noexcept;
        float process(float input) noexcept;
        void reset() noexcept;

    private:
        BiquadCoefficients coef_{};
        float x1_ = 0.0f, x2_ = 0.0f;
        float y1_ = 0.0f, y2_ = 0.0f;
    };

// ✅ FIX: Thread-safe Equalizer avec double buffering
    class Equalizer {
    public:
        static constexpr int kNumBands = 10;
        static constexpr std::array<float, kNumBands> kCenterFreqs = {
                31.25f, 62.5f, 125.0f, 250.0f, 500.0f,
                1000.0f, 2000.0f, 4000.0f, 8000.0f, 16000.0f
        };
        static constexpr float kDefaultQ = 1.4142f;

        explicit Equalizer(float sampleRate);

        // ✅ Thread-safe: peut être appelé depuis UI thread
        void setBandGain(int band, float gainDb) noexcept;

        // ✅ Lock-free: appelé depuis audio thread
        float process(float input) noexcept;

        void reset() noexcept;
        float getBandGain(int band) const noexcept;

    private:
        void updateCoefficients(int band) noexcept;

        float sampleRate_;

        // ✅ FIX: Double buffering pour éviter glitches
        // Deux jeux de filtres - on swap entre eux atomiquement
        std::array<BiquadFilter, kNumBands> filters_[2];
        std::atomic<int> activeFilterSet_{0};  // 0 ou 1

        // Gains stockés pour recalcul
        std::array<std::atomic<float>, kNumBands> gains_;

        // Flag pour indiquer qu'un update est nécessaire
        std::atomic<bool> needsUpdate_{false};
    };

} // namespace soundarch::dsp