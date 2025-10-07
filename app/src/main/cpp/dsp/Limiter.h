#pragma once

#include <cmath>
#include <algorithm>
#include <vector>

namespace soundarch::dsp {

    class Limiter {
    public:
        explicit Limiter(float sampleRate);

        // Configuration
        void setThreshold(float thresholdDb) noexcept;
        void setRelease(float releaseMs) noexcept;
        void setLookahead(float lookaheadMs) noexcept;

        // Traitement temps réel
        float process(float input) noexcept;

        // Reset state
        void reset() noexcept;

        // Getters pour UI (niveau de réduction)
        [[nodiscard]] float getGainReduction() const noexcept { return gainReduction_; }

    private:
        float sampleRate_;

        // Paramètres
        float thresholdLinear_ = 1.0f;  // Threshold en linéaire (0-1)
        float releaseCoeff_ = 0.0f;     // Coefficient de release

        // État interne
        float envelope_ = 0.0f;         // Envelope du signal
        float gainReduction_ = 0.0f;    // Gain réduit (dB) pour affichage

        // Lookahead buffer (optionnel)
        std::vector<float> lookaheadBuffer_;
        size_t lookaheadIndex_ = 0;
        bool useLookahead_ = false;
    };

} // namespace soundarch::dsp