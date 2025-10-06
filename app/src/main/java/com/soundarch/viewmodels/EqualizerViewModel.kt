package com.soundarch.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EqualizerViewModel : ViewModel() {

    companion object {
        val centerFreqs = floatArrayOf(
            60f, 170f, 310f, 600f, 1000f,
            3000f, 6000f, 12000f, 14000f, 16000f
        )

        private const val BAND_COUNT = 10
        private const val MIN_GAIN = -12f
        private const val MAX_GAIN = 12f
    }

    private val _bandGains = MutableStateFlow(FloatArray(BAND_COUNT) { 0f })
    val bandGains: StateFlow<FloatArray> = _bandGains

    fun setBandGain(index: Int, gain: Float) {
        if (index in 0 until BAND_COUNT) {
            _bandGains.value = _bandGains.value.copyOf().also {
                it[index] = gain.coerceIn(MIN_GAIN, MAX_GAIN)
            }
            sendGainsToNative()
        }
    }

    fun reset() {
        _bandGains.value = FloatArray(BAND_COUNT) { 0f }
        sendGainsToNative()
    }

    fun setBassBoost() {
        val gains = FloatArray(BAND_COUNT) { 0f }
        gains[0] = 8f   // 60 Hz
        gains[1] = 6f   // 170 Hz
        _bandGains.value = gains
        sendGainsToNative()
    }

    // TODO: Hook JNI
    private fun sendGainsToNative() {
        // Appeler ici : NativeBridge.setEqBands(_bandGains.value)
    }
}
