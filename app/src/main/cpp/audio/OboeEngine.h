#pragma once

#include <oboe/Oboe.h>
#include <memory>
#include <cstdint>
#include <functional>
#include <atomic>
#include "BluetoothRouter.h"

// ==============================================================================
// 📊 LATENCY STATISTICS - EMA Smoothing + 5s Min/Max
// ==============================================================================
struct LatencyStats {
    double inputMs = 0.0;        // Input stream latency
    double outputMs = 0.0;       // Output stream latency
    double totalMs = 0.0;        // Total measured latency (perceived)
    double emaMs = 0.0;          // Exponential moving average (smoothed)
    double minMs = 999999.0;     // 5-second rolling minimum
    double maxMs = 0.0;          // 5-second rolling maximum
    int64_t minMaxResetTime = 0; // Timestamp for 5s window reset
};

// ==============================================================================
// 📊 PERFORMANCE METRICS - CPU, RAM, and detailed latency breakdown
// ==============================================================================
struct PerformanceMetrics {
    // Latency breakdown
    double burstLatencyMs = 0.0;      // Hardware burst latency (minimum possible)
    double bufferLatencyMs = 0.0;     // Current buffer usage latency
    double ringBufferLatencyMs = 0.0; // Ring buffer latency
    double perceivedLatencyMs = 0.0;  // Total perceived latency
    double bluetoothCodecMs = 0.0;    // Bluetooth codec transmission delay

    // Frame tracking (for advanced analysis)
    int64_t inputFramesPending = 0;
    int64_t outputFramesPending = 0;

    // CPU usage (percentage)
    float cpuUsagePercent = 0.0f;
    float cpuAudioThreadPercent = 0.0f;

    // RAM usage
    uint64_t ramUsedBytes = 0;
    uint64_t ramAvailableBytes = 0;
    float ramUsagePercent = 0.0f;

    // Buffer health
    uint32_t xRunCount = 0;
    int32_t lastCallbackSize = 0;
    float bufferFillRatio = 0.0f;
    bool safeModeActive = false;
};

class OboeEngine : public oboe::AudioStreamCallback {
public:
    OboeEngine() noexcept;
    ~OboeEngine();

    void start();
    void stop();

    // 🔧 Injection du traitement DSP temps réel
    void setAudioCallback(std::function<void(float*, float*, int32_t)> cb) noexcept;

    // 🔁 Callback Oboe temps réel (output)
    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream* stream,
            void* audioData,
            int32_t numFrames) override;

    // 📊 Latency monitoring getters (thread-safe)
    LatencyStats getLatencyStats() const noexcept;
    PerformanceMetrics getPerformanceMetrics() const noexcept;
    uint32_t getXRunCount() const noexcept { return xRunCount_.load(std::memory_order_relaxed); }
    int32_t getLastCallbackSize() const noexcept { return lastCallbackSize_.load(std::memory_order_relaxed); }

    // 🎵 Audio stream properties
    float getSampleRate() const noexcept {
        return outputStream ? static_cast<float>(outputStream->getSampleRate()) : 48000.0f;
    }
    int32_t getBufferSize() const noexcept {
        return outputStream ? outputStream->getBufferSizeInFrames() : 128;
    }

    // 📻 Bluetooth monitoring getters
    const soundarch::audio::BluetoothRouter& getBluetoothRouter() const noexcept { return bluetoothRouter_; }
    bool isBluetoothActive() const noexcept { return bluetoothRouter_.isBluetoothActive(); }
    bool isSafeModeActive() const noexcept { return bluetoothRouter_.isSafeModeActive(); }

    // 📻 Bluetooth profile update (called from JNI when BluetoothBridge notifies change)
    void updateBluetoothProfile(
        const char* profileName,
        const char* codecName,
        int sampleRate,
        float estimatedLatencyMs
    ) noexcept {
        bluetoothRouter_.setActiveProfile(profileName, codecName, sampleRate, estimatedLatencyMs);
    }

    // 📊 Audio levels monitoring (Peak/RMS Meter)
    // ✅ IMPLEMENTED: Full peak/RMS tracking with EMA smoothing in onAudioReady()
    // Peak: Instant attack, slow decay (0.15 EMA alpha)
    // RMS: Bidirectional smoothing (0.15 EMA alpha)
    // Returns levels in dBFS [-60.0, 0.0]
    float getPeakDb() const noexcept { return peakDb_.load(std::memory_order_relaxed); }
    float getRmsDb() const noexcept { return rmsDb_.load(std::memory_order_relaxed); }

private:
    std::shared_ptr<oboe::AudioStream> inputStream;
    std::shared_ptr<oboe::AudioStream> outputStream;

    // 🧠 Callback DSP passé depuis le code externe
    std::function<void(float*, float*, int32_t)> audioCallback_;

    bool isRecording = false;
    int64_t lastLogTime = 0;

    // 📊 Latency statistics (updated at 10Hz)
    LatencyStats latencyStats_{};

    // 📊 Performance metrics (updated at 10Hz)
    PerformanceMetrics performanceMetrics_{};

    // ⚠️ XRun tracking (overflow + underflow)
    std::atomic<uint32_t> xRunCount_{0};

    // 📏 Callback size tracking (for correlation with XRuns)
    std::atomic<int32_t> lastCallbackSize_{0};

    // 📻 Bluetooth profile router (profile detection + Safe Mode)
    soundarch::audio::BluetoothRouter bluetoothRouter_{48000.0f};  // Default SR, updated in start()

    // 📊 Audio levels (Peak/RMS) - Lock-free atomics
    // ✅ IMPLEMENTED: Updated in onAudioReady() every audio block with EMA smoothing
    // Peak: Instant attack (>current) or 0.15 EMA decay
    // RMS: 0.15 EMA smoothing both directions
    mutable std::atomic<float> peakDb_{-60.0f};
    mutable std::atomic<float> rmsDb_{-60.0f};
};
