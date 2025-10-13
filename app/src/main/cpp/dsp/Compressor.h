#pragma once

#include <cmath>
#include <algorithm>
#include <array>

namespace soundarch::dsp {

    // Detection mode for level measurement
    enum class DetectionMode {
        PEAK,   // Instant peak detection (fast, aggressive)
        RMS     // RMS with sliding window (smooth, musical)
    };

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

        // ✅ OPTIMIZED: Block processing for better performance
        void processBlock(const float* input, float* output, int numFrames) noexcept;

        void reset() noexcept;

        void setThreshold(float thresholdDb) noexcept;
        void setRatio(float ratio) noexcept;
        void setAttack(float attackMs) noexcept;
        void setRelease(float releaseMs) noexcept;
        void setKnee(float kneeDb) noexcept;
        void setMakeupGain(float gainDb) noexcept;
        void setDetectionMode(DetectionMode mode) noexcept { detectionMode_ = mode; }
        void setRMSWindowSize(float ms) noexcept;

        // ✅ Auto makeup gain: calculates optimal gain based on threshold/ratio
        // Formula: makeup ≈ threshold × (1 - 1/ratio) / 2
        void enableAutoMakeupGain(bool enable) noexcept;
        float calculateAutoMakeupGain() const noexcept;

        float getCurrentGainReduction() const noexcept { return gainReductionDb_; }
        DetectionMode getDetectionMode() const noexcept { return detectionMode_; }

    private:
        void updateCoefficients() noexcept;
        float computeGain(float inputLevelDb) noexcept;
        float detectLevel(float input) noexcept;  // Peak or RMS detection

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

        // RMS Detection
        DetectionMode detectionMode_ = DetectionMode::PEAK;
        static constexpr size_t kMaxRMSWindowSize = 4800;  // 100ms @ 48kHz
        std::array<float, kMaxRMSWindowSize> rmsBuffer_{};
        size_t rmsWindowSize_ = 480;  // 10ms @ 48kHz
        size_t rmsWriteIndex_ = 0;
        float rmsSum_ = 0.0f;

        // Auto makeup gain
        bool autoMakeupGain_ = false;
    };

} // namespace soundarch::dsp