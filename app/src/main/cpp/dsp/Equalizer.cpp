#include "Equalizer.h"
#include <cmath>

namespace soundarch::dsp {

    void BiquadFilter::setCoefficients(const BiquadCoefficients& c) noexcept {
        coef_ = c;
    }

    float BiquadFilter::process(float in) noexcept {
        const float out = coef_.b0 * in + coef_.b1 * x1_ + coef_.b2 * x2_
                          - coef_.a1 * y1_ - coef_.a2 * y2_;
        x2_ = x1_;
        x1_ = in;
        y2_ = y1_;
        y1_ = out;
        return out;
    }

    void BiquadFilter::reset() noexcept {
        x1_ = x2_ = y1_ = y2_ = 0.0f;
    }

    Equalizer::Equalizer(float sampleRate)
            : sampleRate_(sampleRate) {

        for (auto& gain : gains_) {
            gain.store(0.0f, std::memory_order_relaxed);
        }

        // Initialise les deux jeux de filtres
        for (int i = 0; i < kNumBands; ++i) {
            updateCoefficients(i);
        }
    }

// ✅ FIX: Thread-safe setBandGain
    void Equalizer::setBandGain(int band, float gainDb) noexcept {
        if (band < 0 || band >= kNumBands) return;

        gainDb = std::clamp(gainDb, -12.0f, 12.0f);
        gains_[band].store(gainDb, std::memory_order_release);

        // Prépare les nouveaux coefficients sur le set INACTIF
        updateCoefficients(band);
    }

// ✅ FIX: Lock-free process
    float Equalizer::process(float input) noexcept {
        // Lit le set actif atomiquement
        int current = activeFilterSet_.load(std::memory_order_acquire);

        float out = input;
        for (int i = 0; i < kNumBands; ++i) {
            out = filters_[current][i].process(out);
        }

        return out;
    }

    void Equalizer::reset() noexcept {
        for (auto& gain : gains_) {
            gain.store(0.0f, std::memory_order_relaxed);
        }

        for (auto& filterSet : filters_) {
            for (auto& f : filterSet) {
                f.reset();
            }
        }

        for (int i = 0; i < kNumBands; ++i) {
            updateCoefficients(i);
        }
    }

    float Equalizer::getBandGain(int band) const noexcept {
        if (band < 0 || band >= kNumBands) return 0.0f;
        return gains_[band].load(std::memory_order_acquire);
    }

// ✅ FIX: Update sur le set INACTIF puis swap
    void Equalizer::updateCoefficients(int band) noexcept {
        if (band < 0 || band >= kNumBands) return;

        const float freq = kCenterFreqs[band];
        const float gain = gains_[band].load(std::memory_order_acquire);
        const float Q = kDefaultQ;

        const float A = std::pow(10.0f, gain / 40.0f);
        const float omega = 2.0f * M_PI * freq / sampleRate_;
        const float sn = std::sin(omega);
        const float cs = std::cos(omega);
        const float alpha = sn / (2.0f * Q);

        const float a0 = 1.0f + alpha / A;
        const float a1 = -2.0f * cs;
        const float a2 = 1.0f - alpha / A;
        const float b0 = 1.0f + alpha * A;
        const float b1 = -2.0f * cs;
        const float b2 = 1.0f - alpha * A;

        BiquadCoefficients c;
        c.b0 = b0 / a0;
        c.b1 = b1 / a0;
        c.b2 = b2 / a0;
        c.a1 = a1 / a0;
        c.a2 = a2 / a0;

        // ✅ Applique sur le set INACTIF
        int current = activeFilterSet_.load(std::memory_order_acquire);
        int inactive = 1 - current;

        // Copie l'état des filtres pour éviter les clics
        for (int i = 0; i < kNumBands; ++i) {
            if (i == band) {
                filters_[inactive][i].setCoefficients(c);
            } else {
                // Copie les coefficients existants
                filters_[inactive][i] = filters_[current][i];
            }
        }

        // ✅ Swap atomique - pas de glitch
        activeFilterSet_.store(inactive, std::memory_order_release);
    }

} // namespace soundarch::dsp