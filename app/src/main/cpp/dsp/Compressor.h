#pragma once

#include <cmath>
#include <algorithm>

namespace soundarch::dsp {

    class Compressor {
    public:
        explicit Compressor(
                float sampleRate,
                float thresholdDb = -20.0f,
                float ratio = 4.0f,
                float attackMs = 5.0f,
                float releaseMs = 50.0f,
                float kneeDb = 6.0f,
                float makeupGainDb = 0.0f
        );

        float process(float input) noexcept;
        void reset() noexcept;

        void setThreshold(float thresholdDb) noexcept;
        void setRatio(float ratio) noexcept;
        void setAttack(float attackMs) noexcept;
        void setRelease(float releaseMs) noexcept;
        void setMakeupGain(float gainDb) noexcept;

        float getCurrentGainReduction() const noexcept { return gainReductionDb_; }

    private:
        void updateCoefficients() noexcept;
        float computeGain(float inputLevelDb) noexcept;

        float sampleRate_;
        float thresholdDb_;
        float ratio_;
        float kneeDb_;
        float makeupGainDb_;
        float attackCoef_;
        float releaseCoef_;
        float envelope_;
        float gainReductionDb_;
        float makeupGainLin_;

        // ✅ warmupCounter_ supprimé - pas besoin avec envelope_ à -60dB
    };

} // namespace soundarch::dsp