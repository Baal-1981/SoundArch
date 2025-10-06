package com.soundarch.engine

class NativeAudioEngine {

    external fun initialize(sampleRate: Int): Boolean
    external fun start(): Boolean
    external fun stop()
    external fun release()
    external fun getCurrentLatency(): Double
    external fun setEqBands(gains: FloatArray)

    companion object {
        init {
            System.loadLibrary("soundarch")
        }
    }
}
