package com.soundarch.engine

/**
 * Native Audio Engine - JNI bridge to C++ audio processing
 */
class NativeAudioEngine {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎛️ AUDIO ENGINE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun initialize(sampleRate: Int): Boolean
    external fun start(): Boolean
    external fun stop()
    external fun release()
    external fun getCurrentLatency(): Double
    external fun setEqBands(gains: FloatArray)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔇 NOISE CANCELLER
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Enable or disable noise cancellation
     *
     * @param enabled true to enable, false to disable (zero CPU cost when disabled)
     */
    external fun setNoiseCancellerEnabled(enabled: Boolean)

    /**
     * Apply a noise cancellation preset
     *
     * @param presetIndex Preset index:
     *   - 0: Default (balanced)
     *   - 1: Voice (moderate strength, quick attack, +2dB boost)
     *   - 2: Outdoor (high strength, fast attack, long release)
     *   - 3: Office (medium strength, low artifacts)
     */
    external fun applyNoiseCancellerPreset(presetIndex: Int)

    /**
     * Set noise canceller parameters
     *
     * @param strength          Noise reduction strength [0.0, 1.0]
     * @param spectralFloor     Spectral floor [-80, -30] dB
     * @param smoothing         Temporal smoothing [0.0, 1.0]
     * @param noiseAttackMs     Noise attack time [5, 100] ms
     * @param noiseReleaseMs    Noise release time [50, 1500] ms
     * @param residualBoostDb   Residual boost [-6, +6] dB
     * @param artifactSuppress  Artifact suppression [0.0, 1.0]
     */
    external fun setNoiseCancellerParams(
        strength: Float,
        spectralFloor: Float,
        smoothing: Float,
        noiseAttackMs: Float,
        noiseReleaseMs: Float,
        residualBoostDb: Float,
        artifactSuppress: Float
    )

    /**
     * Get current noise floor estimate
     *
     * @return Noise floor in dB (e.g., -42.3)
     */
    external fun getNoiseCancellerNoiseFloor(): Float

    /**
     * Get average CPU time per block (requires NC_BENCHMARK build flag)
     *
     * @return Average CPU time in milliseconds (e.g., 0.015)
     * @note Only available when compiled with -DNC_BENCHMARK flag
     */
    external fun getNoiseCancellerCpuMs(): Float

    /**
     * Reset CPU statistics (requires NC_BENCHMARK build flag)
     *
     * @note Only available when compiled with -DNC_BENCHMARK flag
     */
    external fun resetNoiseCancellerCpuStats()

    companion object {
        init {
            System.loadLibrary("soundarch")
        }

        /**
         * Noise cancellation presets
         */
        object NoisePreset {
            const val DEFAULT = 0
            const val VOICE = 1
            const val OUTDOOR = 2
            const val OFFICE = 3
        }
    }
}
