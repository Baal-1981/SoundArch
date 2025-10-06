package com.soundarch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.soundarch.ui.navigation.NavGraph
import com.soundarch.ui.theme.SoundArchTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private var updateLatencyCallback: ((Float) -> Unit)? = null

        init {
            System.loadLibrary("soundarch")
        }

        @JvmStatic
        fun updateLatencyText(latency: Double) {
            updateLatencyCallback?.invoke(latency.toFloat())
        }
    }

    // 🎧 Native JNI functions
    external fun startAudio()
    external fun stopAudio()
    external fun setEqBands(gains: FloatArray)
    external fun setCompressor(
        threshold: Float,
        ratio: Float,
        attack: Float,
        release: Float,
        makeupGain: Float
    )

    // 🔸 Limiter JNI (NOUVEAU)
    external fun setLimiter(
        threshold: Float,
        release: Float,
        lookahead: Float
    )
    external fun getLimiterGainReduction(): Float
    external fun setLimiterEnabled(enabled: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestMicPermission()

        setContent {
            SoundArchTheme {
                val navController = rememberNavController()
                var latency by remember { mutableStateOf(0f) }

                // 🎛️ 10 bandes ISO standard (Hz)
                val bands = listOf(
                    31,    // Sub-bass
                    62,    // Bass
                    125,   // Low-mid bass
                    250,   // Low-mid
                    500,   // Midrange
                    1000,  // Upper-mid
                    2000,  // Presence
                    4000,  // Brilliance
                    8000,  // Air
                    16000  // Sparkle
                )

                // 🎚️ Gains par bande (initialisés à 0 dB)
                var gains by remember { mutableStateOf(List(bands.size) { 0f }) }

                // 🔸 État Limiter (NOUVEAU)
                var limiterEnabled by remember { mutableStateOf(true) }
                var limiterThreshold by remember { mutableStateOf(-1.0f) }
                var limiterRelease by remember { mutableStateOf(50.0f) }

                // 🚀 Coroutine scope pour debounce optimisé
                val scope = rememberCoroutineScope()
                var debounceJob by remember { mutableStateOf<Job?>(null) }

                SideEffect {
                    updateLatencyCallback = { latency = it }
                }

                NavGraph(
                    navController = navController,
                    latency = latency,
                    bands = bands,
                    gains = gains,

                    // 🎛️ Callback EQ slider
                    onBandChange = { index, value ->
                        Log.d(TAG, "🎚️ Band $index → ${String.format("%.1f", value)}dB")

                        gains = gains.toMutableList().also { it[index] = value }

                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(50)
                            Log.i(TAG, "📤 EQ update: Band$index=${String.format("%.1f", value)}dB")
                            setEqBands(gains.toFloatArray())
                        }
                    },

                    // 🔄 Reset EQ
                    onReset = {
                        Log.i(TAG, "🔄 Reset all bands to 0dB")
                        gains = List(bands.size) { 0f }
                        debounceJob?.cancel()
                        setEqBands(FloatArray(bands.size) { 0f })
                    },

                    // 🎚️ Callback Compressor
                    onCompressorChange = { threshold, ratio, attack, release, makeupGain ->
                        Log.i(TAG, "🎚️ Compressor: Thr=${String.format("%.1f", threshold)}dB Ratio=${String.format("%.1f", ratio)}:1")

                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(50)
                            setCompressor(threshold, ratio, attack, release, makeupGain)
                        }
                    },

                    // 🔸 Callback Limiter (NOUVEAU)
                    onLimiterChange = { threshold, release ->
                        Log.i(TAG, "🚨 Limiter: Thr=${String.format("%.1f", threshold)}dB Release=${String.format("%.1f", release)}ms")

                        limiterThreshold = threshold
                        limiterRelease = release

                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(50)
                            setLimiter(threshold, release, 0.0f) // Pas de lookahead
                        }
                    },

                    // 🔸 Toggle Limiter ON/OFF (NOUVEAU)
                    onLimiterToggle = { enabled ->
                        Log.i(TAG, if (enabled) "✅ Limiter ENABLED" else "❌ Limiter DISABLED")
                        limiterEnabled = enabled
                        setLimiterEnabled(enabled)
                    },

                    // ▶️ Start audio
                    onStart = {
                        if (hasMicrophonePermission()) {
                            Log.i(TAG, "▶️ Starting audio engine...")
                            startAudio()

                            // Sync initial EQ state
                            setEqBands(gains.toFloatArray())

                            // 🔸 Sync initial Limiter state (NOUVEAU)
                            if (limiterEnabled) {
                                setLimiter(limiterThreshold, limiterRelease, 0.0f)
                            }

                            Toast.makeText(
                                this@MainActivity,
                                "🎙️ Audio started - EQ + Compressor + Limiter active",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "⚠️ Microphone permission required",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },

                    // ⏹️ Stop audio
                    onStop = {
                        Log.i(TAG, "⏹️ Stopping audio engine...")
                        debounceJob?.cancel()
                        stopAudio()
                        Toast.makeText(
                            this@MainActivity,
                            "🛑 Audio stopped",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }

    private fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMicPermission() {
        if (!hasMicrophonePermission()) {
            val launcher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                val msg = if (granted)
                    "✅ Microphone permission granted"
                else
                    "❌ Microphone permission denied"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}