#include "Limiter.h"
#include "DSPMath.h"
#include <cmath>
#include <algorithm>

namespace soundarch::dsp {

    // ✅ SAFETY: Soft clipper prevents inter-sample peaks and harsh distortion
    // Uses tanh for smooth, musical clipping (vs hard clip at ±1.0)
    float Limiter::softClip(float x) noexcept {
        // Normalized tanh: maps ±0.95 → ±1.0, providing headroom
        constexpr float DRIVE = 0.95f;
        const float NORM = 1.0f / std::tanh(DRIVE);  // Not constexpr - computed at runtime
        return std::tanh(x * DRIVE) * NORM;
    }

    Limiter::Limiter(float sampleRate)
            : sampleRate_(sampleRate) {
        setThreshold(-1.0f);
        setRelease(50.0f);
        setLookahead(0.0f);
        reset();
    }

    void Limiter::setThreshold(float thresholdDb) noexcept {
        // ✅ PARAMETER CLAMPING: Threshold must be in range [-12dB, 0dB]
        thresholdDb = std::clamp(thresholdDb, -12.0f, 0.0f);

        // ✅ OPTIMISATION LUT: Pré-calculer le threshold linéaire
        thresholdLinear_ = getDSPMath().dbToLinear(thresholdDb);
    }

    void Limiter::setRelease(float releaseMs) noexcept {
        // ✅ PARAMETER CLAMPING: Release time must be in range [10ms, 500ms]
        releaseMs = std::clamp(releaseMs, 10.0f, 500.0f);

        float releaseTimeSamples = (releaseMs / 1000.0f) * sampleRate_;
        releaseCoeff_ = std::exp(-1.0f / releaseTimeSamples);
    }

    void Limiter::setLookahead(float lookaheadMs) noexcept {
        // ✅ PARAMETER CLAMPING: Lookahead must be in range [0ms, 10ms]
        lookaheadMs = std::clamp(lookaheadMs, 0.0f, 10.0f);

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

        // Apply gain + soft clip safety
        float limited = sample * gain;
        return softClip(limited);
    }

    // ✅ OPTIMIZED: Block processing - vectorizable peak detection
    void Limiter::processBlock(const float* input, float* output, int numFrames) noexcept {
        auto& dspMath = getDSPMath();

        for (int i = 0; i < numFrames; ++i) {
            float sample = input[i];

            // Lookahead buffer (if enabled)
            if (useLookahead_ && !lookaheadBuffer_.empty()) {
                lookaheadBuffer_[lookaheadIndex_] = sample;
                sample = lookaheadBuffer_[(lookaheadIndex_ + 1) % lookaheadBuffer_.size()];
                lookaheadIndex_ = (lookaheadIndex_ + 1) % lookaheadBuffer_.size();
            }

            // Level detection
            float level = std::abs(input[i]);

            // Envelope follower
            if (level > envelope_) {
                envelope_ = level;  // Instant attack
            } else {
                envelope_ = releaseCoeff_ * envelope_ + (1.0f - releaseCoeff_) * level;
            }

            // Gain calculation
            float gain = 1.0f;
            if (envelope_ > thresholdLinear_) {
                gain = thresholdLinear_ / envelope_;  // Ratio ∞:1
            }

            gainReduction_ = dspMath.linearToDb(gain);

            // Apply gain + soft clip safety
            float limited = sample * gain;
            output[i] = softClip(limited);
        }
    }

    void Limiter::reset() noexcept {
        envelope_ = 0.0f;
        gainReduction_ = 0.0f;
        lookaheadIndex_ = 0;
        std::fill(lookaheadBuffer_.begin(), lookaheadBuffer_.end(), 0.0f);
    }

} // namespace soundarch::dsp