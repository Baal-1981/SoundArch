#include "Compressor.h"
#include "DSPMath.h"

namespace soundarch::dsp {

    namespace {
        /**
         * ✅ FRAME-INDEPENDENT: Time constant calculation
         *
         * Converts attack/release time in milliseconds to exponential coefficient.
         * Formula: coef = exp(-1 / (time_ms * 0.001 * sampleRate))
         *
         * This ensures attack/release behavior is IDENTICAL across:
         * - Different sample rates (44.1kHz, 48kHz, 96kHz)
         * - Different buffer sizes (64, 128, 256, 512 frames)
         * - Different devices (phones, tablets, desktops)
         *
         * Example: 10ms attack @ 48kHz = same behavior as 10ms @ 44.1kHz
         */
        inline float calcCoef(float timeMs, float sampleRate) {
            return std::exp(-1.0f / (timeMs * 0.001f * sampleRate));
        }
    }

    Compressor::Compressor(
            float sampleRate,
            float thresholdDb,
            float ratio,
            float attackMs,
            float releaseMs,
            float kneeDb,
            float makeupGainDb
    )
            : sampleRate_(sampleRate)
            , thresholdDb_(thresholdDb)
            , ratio_(ratio)
            , kneeDb_(kneeDb)
            , makeupGainDb_(makeupGainDb)
            , envelope_(-60.0f)
            , gainReductionDb_(0.0f)
    {
        attackCoef_ = calcCoef(attackMs, sampleRate_);
        releaseCoef_ = calcCoef(releaseMs, sampleRate_);

        // ✅ OPTIMISATION LUT: Pré-calculer le makeup gain linéaire
        makeupGainLin_ = getDSPMath().dbToLinear(makeupGainDb_);
    }

    float Compressor::detectLevel(float input) noexcept {
        if (detectionMode_ == DetectionMode::PEAK) {
            // Peak detection: instant absolute value
            return std::fabs(input);
        } else {
            // RMS detection: windowed root-mean-square
            const float oldSample = rmsBuffer_[rmsWriteIndex_];
            const float newSample = input * input;

            rmsBuffer_[rmsWriteIndex_] = newSample;
            rmsSum_ += (newSample - oldSample);

            // Safety: prevent negative sum due to float precision
            if (rmsSum_ < 0.0f) rmsSum_ = 0.0f;

            rmsWriteIndex_ = (rmsWriteIndex_ + 1) % rmsWindowSize_;

            return std::sqrt(rmsSum_ / static_cast<float>(rmsWindowSize_) + 1e-10f);
        }
    }

    float Compressor::process(float input) noexcept {
        auto& dspMath = getDSPMath();

        const float inputLevel = detectLevel(input);

        // ✅ OPTIMISATION LUT: log10 remplacé par lookup table
        const float inputDb = dspMath.linearToDb(inputLevel);

        const float coef = (inputDb > envelope_) ? attackCoef_ : releaseCoef_;
        envelope_ = coef * envelope_ + (1.0f - coef) * inputDb;

        gainReductionDb_ = computeGain(envelope_);

        // ✅ OPTIMISATION LUT: pow remplacé par lookup table
        const float gainLin = dspMath.dbToLinear(gainReductionDb_);
        const float output = input * gainLin * makeupGainLin_;

        return output;
    }

    // ✅ OPTIMIZED: Block processing - vectorizable envelope detection
    void Compressor::processBlock(const float* input, float* output, int numFrames) noexcept {
        auto& dspMath = getDSPMath();

        for (int i = 0; i < numFrames; ++i) {
            const float inputLevel = detectLevel(input[i]);
            const float inputDb = dspMath.linearToDb(inputLevel);

            const float coef = (inputDb > envelope_) ? attackCoef_ : releaseCoef_;
            envelope_ = coef * envelope_ + (1.0f - coef) * inputDb;

            gainReductionDb_ = computeGain(envelope_);

            const float gainLin = dspMath.dbToLinear(gainReductionDb_);
            output[i] = input[i] * gainLin * makeupGainLin_;
        }
    }

    void Compressor::setRMSWindowSize(float ms) noexcept {
        const float clampedMs = std::clamp(ms, 1.0f, 100.0f);
        rmsWindowSize_ = static_cast<size_t>(clampedMs * 0.001f * sampleRate_);
        rmsWindowSize_ = std::min(rmsWindowSize_, kMaxRMSWindowSize);

        // Reset RMS buffer
        rmsBuffer_.fill(0.0f);
        rmsSum_ = 0.0f;
        rmsWriteIndex_ = 0;
    }

    float Compressor::computeGain(float inputLevelDb) noexcept {
        const float overThreshold = inputLevelDb - thresholdDb_;

        float gainReductionDb = 0.0f;

        if (overThreshold <= -kneeDb_ / 2.0f) {
            gainReductionDb = 0.0f;
        }
        else if (overThreshold >= kneeDb_ / 2.0f) {
            gainReductionDb = overThreshold * (1.0f - 1.0f / ratio_);
        }
        else {
            const float x = overThreshold + kneeDb_ / 2.0f;
            gainReductionDb = x * x / (2.0f * kneeDb_) * (1.0f - 1.0f / ratio_);
        }

        return -gainReductionDb;
    }

    void Compressor::reset() noexcept {
        envelope_ = -60.0f;
        gainReductionDb_ = 0.0f;

        // Reset RMS detection
        rmsBuffer_.fill(0.0f);
        rmsSum_ = 0.0f;
        rmsWriteIndex_ = 0;
    }

    void Compressor::setThreshold(float thresholdDb) noexcept {
        thresholdDb_ = std::clamp(thresholdDb, -60.0f, 0.0f);
    }

    void Compressor::setRatio(float ratio) noexcept {
        ratio_ = std::clamp(ratio, 1.0f, 20.0f);
    }

    void Compressor::setAttack(float attackMs) noexcept {
        attackMs = std::clamp(attackMs, 0.1f, 100.0f);
        attackCoef_ = calcCoef(attackMs, sampleRate_);
    }

    void Compressor::setRelease(float releaseMs) noexcept {
        releaseMs = std::clamp(releaseMs, 10.0f, 1000.0f);
        releaseCoef_ = calcCoef(releaseMs, sampleRate_);
    }

    void Compressor::setKnee(float kneeDb) noexcept {
        kneeDb_ = std::clamp(kneeDb, 0.0f, 12.0f);
    }

    void Compressor::setMakeupGain(float gainDb) noexcept {
        makeupGainDb_ = std::clamp(gainDb, 0.0f, 24.0f);

        // ✅ OPTIMISATION LUT: Recalculer le makeup gain linéaire
        makeupGainLin_ = getDSPMath().dbToLinear(makeupGainDb_);
    }

    float Compressor::calculateAutoMakeupGain() const noexcept {
        /**
         * ✅ AUTO MAKEUP GAIN CALCULATION
         *
         * Estimates optimal makeup gain to compensate for average gain reduction.
         * Formula: makeup ≈ |threshold| × (1 - 1/ratio) / 2
         *
         * Example:
         * - Threshold = -20dB, Ratio = 4:1
         * - Gain reduction ≈ 20 × (1 - 0.25) = 15dB at full compression
         * - Makeup = 15 / 2 = 7.5dB (average compensation)
         *
         * Note: This is an estimate. Actual reduction depends on signal level.
         */
        const float avgReduction = std::abs(thresholdDb_) * (1.0f - 1.0f / ratio_);
        return avgReduction * 0.5f;  // Average over signal range
    }

    void Compressor::enableAutoMakeupGain(bool enable) noexcept {
        autoMakeupGain_ = enable;

        if (enable) {
            const float autoGain = calculateAutoMakeupGain();
            setMakeupGain(autoGain);
        }
    }

} // namespace soundarch::dsp