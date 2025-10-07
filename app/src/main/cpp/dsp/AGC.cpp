#include "AGC.h"
#include "DSPMath.h"
#include <cmath>
#include <algorithm>
#include <android/log.h>

#define TAG "AGC"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

namespace soundarch::dsp {

    AGC::AGC(float sampleRate)
            : sampleRate_(sampleRate) {
        reset();
        updateCoefficients();
    }

    void AGC::setTargetLevel(float dbfs) noexcept {
        targetLevelDb_ = std::clamp(dbfs, -60.0f, 0.0f);
    }

    void AGC::setAttackTime(float seconds) noexcept {
        const float tau = std::max(0.1f, seconds);
        attackCoef_ = std::exp(-1.0f / (tau * sampleRate_));
    }

    void AGC::setReleaseTime(float seconds) noexcept {
        const float tau = std::max(0.5f, seconds);
        releaseCoef_ = std::exp(-1.0f / (tau * sampleRate_));
    }

    void AGC::setMaxGain(float db) noexcept {
        maxGainDb_ = std::clamp(db, 0.0f, 30.0f);
        LOGI("🎯 AGC Max Gain set to %.1f dB (limited to 30 dB for safety)", maxGainDb_);
    }

    void AGC::setMinGain(float db) noexcept {
        minGainDb_ = std::clamp(db, -40.0f, 0.0f);
    }

    void AGC::setNoiseThreshold(float dbfs) noexcept {
        noiseThresholdDb_ = std::clamp(dbfs, -80.0f, -30.0f);
    }

    void AGC::setWindowSize(float seconds) noexcept {
        const float clampedSeconds = std::clamp(seconds, 0.1f, 2.0f);
        windowSize_ = static_cast<size_t>(clampedSeconds * sampleRate_);
        windowSize_ = std::min(windowSize_, kMaxWindowSize);
        reset();
    }

    float AGC::calculateRMS() noexcept {
        if (windowSize_ == 0) return 0.0f;

        const float meanSquare = rmsSum_ / static_cast<float>(windowSize_);
        return std::sqrt(std::max(0.0f, meanSquare));
    }

    float AGC::process(float input) noexcept {
        // ✅ PROTECTION 1: Input clamp
        input = std::clamp(input, -1.0f, 1.0f);

        // ✅ RMS sliding window
        const float oldSample = rmsBuffer_[writeIndex_];
        const float newSample = input * input;

        rmsBuffer_[writeIndex_] = newSample;
        rmsSum_ += (newSample - oldSample);

        // ✅ PROTECTION 2: RMS sum sécurité
        if (rmsSum_ < 0.0f) rmsSum_ = 0.0f;

        writeIndex_ = (writeIndex_ + 1) % windowSize_;

        // Calculate current level (epsilon intégré)
        const float rms = std::sqrt(rmsSum_ / static_cast<float>(windowSize_) + 1e-10f);

        // ✅ OPTIMISATION LUT: log10 remplacé par lookup table
        auto& dspMath = getDSPMath();
        currentLevelDb_ = dspMath.linearToDb(rms);

        // Noise gate
        if (currentLevelDb_ < noiseThresholdDb_) {
            isFrozen_ = true;

            // ✅ OPTIMISATION LUT: pow remplacé par lookup table
            const float linearGain = dspMath.dbToLinear(currentGainDb_);
            const float output = input * linearGain;

            return std::clamp(output, -0.95f, 0.95f);
        }

        isFrozen_ = false;

        // Calculate target gain
        const float error = targetLevelDb_ - currentLevelDb_;
        float targetGainDb = std::clamp(error, minGainDb_, maxGainDb_);

        // Smooth gain changes
        const float coef = (targetGainDb > currentGainDb_) ? attackCoef_ : releaseCoef_;
        currentGainDb_ = coef * currentGainDb_ + (1.0f - coef) * targetGainDb;

        // ✅ OPTIMISATION LUT: pow remplacé par lookup table
        const float linearGain = dspMath.dbToLinear(currentGainDb_);
        const float output = input * linearGain;

        // ✅ PROTECTION 3: Hard limit final
        return std::clamp(output, -0.95f, 0.95f);
    }

    void AGC::reset() noexcept {
        rmsBuffer_.fill(0.0f);
        rmsSum_ = 0.0f;
        writeIndex_ = 0;
        currentGainDb_ = 0.0f;
        currentLevelDb_ = -60.0f;
        isFrozen_ = false;
        LOGI("🔄 AGC reset");
    }

    void AGC::updateCoefficients() noexcept {
        setAttackTime(5.0f);
        setReleaseTime(20.0f);
    }

} // namespace soundarch::dsp