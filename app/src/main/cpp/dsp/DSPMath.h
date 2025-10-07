#pragma once

#include <cmath>
#include <array>
#include <algorithm>

namespace soundarch::dsp {

/**
 * Lookup tables pour optimiser les conversions dB <-> linéaire
 * Économise ~10-20% CPU vs std::pow/log10
 */
    class DSPMath {
    public:
        // Range: -60 dB à +60 dB (couvre tous les besoins AGC/Comp/Limiter)
        static constexpr float DB_MIN = -60.0f;
        static constexpr float DB_MAX = 60.0f;
        static constexpr int LUT_SIZE = 2400;  // 0.05 dB steps
        static constexpr float DB_STEP = (DB_MAX - DB_MIN) / (LUT_SIZE - 1);

        // Range linéaire: 0.001 à 1000 (correspond à -60 dB à +60 dB)
        static constexpr float LIN_MIN = 0.001f;
        static constexpr float LIN_MAX = 1000.0f;
        static constexpr int LIN_LUT_SIZE = 2400;

        DSPMath() {
            initializeLUT();
        }

        /**
         * Conversion dB vers linéaire optimisée
         * Équivalent à: pow(10, db/20)
         */
        [[nodiscard]] inline float dbToLinear(float db) const noexcept {
            // Clamp au range de la table
            db = std::clamp(db, DB_MIN, DB_MAX);

            // Trouver l'index dans la table
            float idx = (db - DB_MIN) / DB_STEP;
            int i0 = static_cast<int>(idx);
            int i1 = std::min(i0 + 1, LUT_SIZE - 1);

            // Interpolation linéaire
            float frac = idx - i0;
            return dbToLinLUT_[i0] * (1.0f - frac) + dbToLinLUT_[i1] * frac;
        }

        /**
         * Conversion linéaire vers dB optimisée
         * Équivalent à: 20 * log10(linear)
         */
        [[nodiscard]] inline float linearToDb(float linear) const noexcept {
            // Protection contre valeurs invalides
            if (linear <= 1e-10f) return DB_MIN;

            // Clamp au range de la table
            linear = std::clamp(linear, LIN_MIN, LIN_MAX);

            // Échelle logarithmique pour l'index
            float logVal = std::log10(linear);
            float idx = (logVal - std::log10(LIN_MIN)) /
                        (std::log10(LIN_MAX) - std::log10(LIN_MIN)) * (LIN_LUT_SIZE - 1);

            int i0 = static_cast<int>(idx);
            int i1 = std::min(i0 + 1, LIN_LUT_SIZE - 1);

            // Interpolation linéaire
            float frac = idx - i0;
            return linToDbLUT_[i0] * (1.0f - frac) + linToDbLUT_[i1] * frac;
        }

        /**
         * Version rapide sans interpolation (légèrement moins précis)
         * Utiliser seulement si la précision à 0.05 dB suffit
         */
        [[nodiscard]] inline float dbToLinearFast(float db) const noexcept {
            db = std::clamp(db, DB_MIN, DB_MAX);
            int idx = static_cast<int>((db - DB_MIN) / DB_STEP + 0.5f);
            return dbToLinLUT_[std::min(idx, LUT_SIZE - 1)];
        }

    private:
        void initializeLUT() noexcept {
            // Initialiser dB -> linéaire
            for (int i = 0; i < LUT_SIZE; ++i) {
                float db = DB_MIN + i * DB_STEP;
                dbToLinLUT_[i] = std::pow(10.0f, db / 20.0f);
            }

            // Initialiser linéaire -> dB
            for (int i = 0; i < LIN_LUT_SIZE; ++i) {
                float t = static_cast<float>(i) / (LIN_LUT_SIZE - 1);
                // Échelle logarithmique
                float linear = LIN_MIN * std::pow(LIN_MAX / LIN_MIN, t);
                linToDbLUT_[i] = 20.0f * std::log10(linear);
            }
        }

        std::array<float, LUT_SIZE> dbToLinLUT_;
        std::array<float, LIN_LUT_SIZE> linToDbLUT_;
    };

// Instance globale (initialisée une seule fois au démarrage)
    inline DSPMath& getDSPMath() {
        static DSPMath instance;
        return instance;
    }

} // namespace soundarch::dsp