#include "Compressor.h"
#include "DSPMath.h"

namespace soundarch::dsp {

    namespace {
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

    float Compressor::process(float input) noexcept {
        auto& dspMath = getDSPMath();

        const float inputAbs = std::fabs(input);

        // ✅ OPTIMISATION LUT: log10 remplacé par lookup table
        const float inputDb = dspMath.linearToDb(inputAbs);

        const float coef = (inputDb > envelope_) ? attackCoef_ : releaseCoef_;
        envelope_ = coef * envelope_ + (1.0f - coef) * inputDb;

        gainReductionDb_ = computeGain(envelope_);

        // ✅ OPTIMISATION LUT: pow remplacé par lookup table
        const float gainLin = dspMath.dbToLinear(gainReductionDb_);
        const float output = input * gainLin * makeupGainLin_;

        return output;
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

    void Compressor::setMakeupGain(float gainDb) noexcept {
        makeupGainDb_ = std::clamp(gainDb, 0.0f, 24.0f);

        // ✅ OPTIMISATION LUT: Recalculer le makeup gain linéaire
        makeupGainLin_ = getDSPMath().dbToLinear(makeupGainDb_);
    }

} // namespace soundarch::dsp