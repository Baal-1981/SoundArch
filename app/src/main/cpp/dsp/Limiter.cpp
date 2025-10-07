#include "Limiter.h"
#include "DSPMath.h"
#include <cmath>
#include <algorithm>

namespace soundarch::dsp {

    Limiter::Limiter(float sampleRate)
            : sampleRate_(sampleRate) {
        setThreshold(-1.0f);
        setRelease(50.0f);
        setLookahead(0.0f);
        reset();
    }

    void Limiter::setThreshold(float thresholdDb) noexcept {
        // ✅ OPTIMISATION LUT: Pré-calculer le threshold linéaire
        thresholdLinear_ = getDSPMath().dbToLinear(thresholdDb);
    }

    void Limiter::setRelease(float releaseMs) noexcept {
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

        // Lookahead buffer (si activé)
        if (useLookahead_ && !lookaheadBuffer_.empty()) {
            lookaheadBuffer_[lookaheadIndex_] = sample;
            sample = lookaheadBuffer_[(lookaheadIndex_ + 1) % lookaheadBuffer_.size()];
            lookaheadIndex_ = (lookaheadIndex_ + 1) % lookaheadBuffer_.size();
        }

        // Détection du niveau
        float level = std::abs(input);

        // Envelope follower
        if (level > envelope_) {
            envelope_ = level;  // Attack instantané
        } else {
            envelope_ = releaseCoeff_ * envelope_ + (1.0f - releaseCoeff_) * level;
        }

        // Calcul du gain
        float gain = 1.0f;
        if (envelope_ > thresholdLinear_) {
            gain = thresholdLinear_ / envelope_;  // Ratio ∞:1
        }

        // ✅ OPTIMISATION LUT: Stocker la réduction en dB
        gainReduction_ = getDSPMath().linearToDb(gain);

        // Apply gain
        return sample * gain;
    }

    void Limiter::reset() noexcept {
        envelope_ = 0.0f;
        gainReduction_ = 0.0f;
        lookaheadIndex_ = 0;
        std::fill(lookaheadBuffer_.begin(), lookaheadBuffer_.end(), 0.0f);
    }

} // namespace soundarch::dsp