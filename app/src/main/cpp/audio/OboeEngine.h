#pragma once

#include <oboe/Oboe.h>
#include <memory>
#include <cstdint>
#include <functional>

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

private:
    std::shared_ptr<oboe::AudioStream> inputStream;
    std::shared_ptr<oboe::AudioStream> outputStream;

    // 🧠 Callback DSP passé depuis le code externe
    std::function<void(float*, float*, int32_t)> audioCallback_;

    bool isRecording = false;
    int64_t lastLogTime = 0;
};
