#include "Limiter.h"
#include <cmath>
#include <algorithm>

namespace soundarch::dsp {

    Limiter::Limiter(float sampleRate)
            : sampleRate_(sampleRate) {
        setThreshold(-1.0f);  // Default: -1 dB
        setRelease(50.0f);     // Default: 50ms release
        setLookahead(0.0f);    // Pas de lookahead par défaut
        reset();
    }

    void Limiter::setThreshold(float thresholdDb) noexcept {
        thresholdLinear_ = dbToLinear(thresholdDb);
    }

    void Limiter::setRelease(float releaseMs) noexcept {
        // Release time constant
        float releaseTimeSamples = (releaseMs / 1000.0f) * sampleRate_;
        releaseCoeff_ = std::exp(-1.0f / releaseTimeSamples);
    }

    void Limiter::setLookahead(float lookaheadMs) noexcept {
        if (lookaheadMs <= 0.0f) {
            useLookahead_ = false;
            lookaheadBuffer_.clear();
            return;
        }

        size_t bufferSize = static_cast<size_t>((lookaheadMs / 1000.0f) * sampleRate_);
        lookaheadBuffer_.resize(bufferSize, 0.0f);
        lookaheadIndex_ = 0;
        useLookahead_ = true;
    }

    float Limiter::process(float input) noexcept {
        float sample = input;

        // Si lookahead activé, buffer le signal
        if (useLookahead_ && !lookaheadBuffer_.empty()) {
            lookaheadBuffer_[lookaheadIndex_] = sample;
            sample = lookaheadBuffer_[(lookaheadIndex_ + 1) % lookaheadBuffer_.size()];
            lookaheadIndex_ = (lookaheadIndex_ + 1) % lookaheadBuffer_.size();
        }

        // Détection du niveau (absolute value)
        float level = std::abs(input);

        // Envelope follower avec release uniquement (attack instantané)
        if (level > envelope_) {
            envelope_ = level;  // Attack instantané (hard limiting)
        } else {
            envelope_ = releaseCoeff_ * envelope_ + (1.0f - releaseCoeff_) * level;
        }

        // Calcul du gain à appliquer
        float gain = 1.0f;
        if (envelope_ > thresholdLinear_) {
            gain = thresholdLinear_ / envelope_;  // Ratio ∞:1
        }

        // Stocke la réduction de gain pour affichage (en dB)
        gainReduction_ = linearToDb(gain);

        // Apply gain
        return sample * gain;
    }

    void Limiter::reset() noexcept {
        envelope_ = 0.0f;
        gainReduction_ = 0.0f;
        lookaheadIndex_ = 0;
        std::fill(lookaheadBuffer_.begin(), lookaheadBuffer_.end(), 0.0f);
    }

    float Limiter::dbToLinear(float db) const noexcept {
        return std::pow(10.0f, db / 20.0f);
    }

    float Limiter::linearToDb(float linear) const noexcept {
        return 20.0f * std::log10(std::max(linear, 1e-6f));
    }

} // namespace soundarch::dsp