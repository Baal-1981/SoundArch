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
import com.soundarch.theme.SoundArchTheme
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

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AUDIO LIFECYCLE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun startAudio()
    external fun stopAudio()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // EQUALIZER
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setEqBands(gains: FloatArray)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // COMPRESSOR
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setCompressor(
        threshold: Float,
        ratio: Float,
        attack: Float,
        release: Float,
        makeupGain: Float
    )

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // LIMITER
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setLimiter(
        threshold: Float,
        release: Float,
        lookahead: Float
    )
    external fun getLimiterGainReduction(): Float
    external fun setLimiterEnabled(enabled: Boolean)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AGC CONTROL
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setAGCTargetLevel(targetDb: Float)
    external fun setAGCMaxGain(maxGainDb: Float)
    external fun setAGCMinGain(minGainDb: Float)
    external fun setAGCAttackTime(seconds: Float)
    external fun setAGCReleaseTime(seconds: Float)
    external fun setAGCNoiseThreshold(thresholdDb: Float)
    external fun setAGCWindowSize(seconds: Float)
    external fun setAGCEnabled(enabled: Boolean)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AGC MONITORING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun getAGCCurrentGain(): Float
    external fun getAGCCurrentLevel(): Float

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // PERFORMANCE MONITORING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun getCPUUsage(): Float
    external fun getMemoryUsage(): Long

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestMicPermission()

        setContent {
            SoundArchTheme {
                val navController = rememberNavController()
                var latency by remember { mutableStateOf(0f) }

                // Performance monitoring states
                var cpuUsage by remember { mutableStateOf(0f) }
                var memoryUsage by remember { mutableStateOf(0L) }

                // 10 bandes ISO standard (Hz)
                val bands = listOf(
                    31, 62, 125, 250, 500,
                    1000, 2000, 4000, 8000, 16000
                )

                // Gains par bande (initialisés à 0 dB)
                var gains by remember { mutableStateOf(List(bands.size) { 0f }) }

                // État Compressor
                var compressorThreshold by remember { mutableStateOf(-20f) }
                var compressorRatio by remember { mutableStateOf(4f) }
                var compressorAttack by remember { mutableStateOf(5f) }
                var compressorRelease by remember { mutableStateOf(50f) }
                var compressorMakeupGain by remember { mutableStateOf(0f) }

                // État Limiter
                var limiterEnabled by remember { mutableStateOf(true) }
                var limiterThreshold by remember { mutableStateOf(-1.0f) }
                var limiterRelease by remember { mutableStateOf(50.0f) }

                // État AGC
                var agcEnabled by remember { mutableStateOf(true) }
                var agcTargetLevel by remember { mutableStateOf(-20f) }
                var agcMaxGain by remember { mutableStateOf(25f) }
                var agcMinGain by remember { mutableStateOf(-10f) }
                var agcAttackTime by remember { mutableStateOf(0.1f) }  // Fast attack: 100ms
                var agcReleaseTime by remember { mutableStateOf(0.5f) }  // Fast release: 500ms
                var agcNoiseThreshold by remember { mutableStateOf(-55f) }
                var agcWindowSize by remember { mutableStateOf(0.1f) }

                // Coroutine scope pour debounce optimisé
                val scope = rememberCoroutineScope()
                var debounceJob by remember { mutableStateOf<Job?>(null) }

                SideEffect {
                    updateLatencyCallback = { latency = it }
                }
                // Update monitoring périodique
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(1000) // Update every second for better CPU delta measurement
                        try {
                            cpuUsage = getCPUUsage()
                            memoryUsage = getMemoryUsage()
                            Log.i("PerformanceMonitor", "📊 CPU: ${String.format("%.2f", cpuUsage)}% | MEM: ${memoryUsage / 1024}MB")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error reading system stats", e)
                        }
                    }
                }

                NavGraph(
                    navController = navController,
                    latency = latency,
                    cpuUsage = cpuUsage,
                    memoryUsageMB = memoryUsage / 1024f,
                    bands = bands,
                    gains = gains,

                    onBandChange = { index, value ->
                        Log.d(TAG, "Band $index -> ${String.format("%.1f", value)}dB")
                        gains = gains.toMutableList().also { it[index] = value }
                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(10)
                            Log.i(TAG, "EQ update: Band$index=${String.format("%.1f", value)}dB")
                            setEqBands(gains.toFloatArray())
                        }
                    },

                    onReset = {
                        Log.i(TAG, "Reset all bands to 0dB")
                        gains = List(bands.size) { 0f }
                        debounceJob?.cancel()
                        setEqBands(FloatArray(bands.size) { 0f })
                    },

                    onCompressorChange = { threshold, ratio, attack, release, makeupGain ->
                        Log.i(TAG, "Compressor: Thr=${String.format("%.1f", threshold)}dB Ratio=${String.format("%.1f", ratio)}:1")
                        compressorThreshold = threshold
                        compressorRatio = ratio
                        compressorAttack = attack
                        compressorRelease = release
                        compressorMakeupGain = makeupGain
                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(10)
                            setCompressor(threshold, ratio, attack, release, makeupGain)
                        }
                    },

                    onLimiterChange = { threshold, release ->
                        Log.i(TAG, "Limiter: Thr=${String.format("%.1f", threshold)}dB Release=${String.format("%.1f", release)}ms")
                        limiterThreshold = threshold
                        limiterRelease = release
                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(10)
                            setLimiter(threshold, release, 0.0f)
                        }
                    },

                    onLimiterToggle = { enabled ->
                        Log.i(TAG, if (enabled) "Limiter ENABLED" else "Limiter DISABLED")
                        limiterEnabled = enabled
                        setLimiterEnabled(enabled)
                    },

                    onAGCChange = { targetLevel, maxGain, minGain, attackTime, releaseTime, noiseThreshold, windowSize ->
                        Log.i(TAG, "AGC: Target=${String.format("%.1f", targetLevel)}dB MaxGain=${String.format("%.1f", maxGain)}dB")
                        agcTargetLevel = targetLevel
                        agcMaxGain = maxGain
                        agcMinGain = minGain
                        agcAttackTime = attackTime
                        agcReleaseTime = releaseTime
                        agcNoiseThreshold = noiseThreshold
                        agcWindowSize = windowSize
                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(10)
                            setAGCTargetLevel(targetLevel)
                            setAGCMaxGain(maxGain)
                            setAGCMinGain(minGain)
                            setAGCAttackTime(attackTime)
                            setAGCReleaseTime(releaseTime)
                            setAGCNoiseThreshold(noiseThreshold)
                            setAGCWindowSize(windowSize)
                        }
                    },

                    onAGCToggle = { enabled ->
                        Log.i(TAG, if (enabled) "AGC ENABLED" else "AGC DISABLED")
                        agcEnabled = enabled
                        setAGCEnabled(enabled)
                    },

                    onStart = {
                        if (hasMicrophonePermission()) {
                            Log.i(TAG, "Starting audio engine...")
                            startAudio()

                            setEqBands(gains.toFloatArray())
                            setCompressor(
                                compressorThreshold,
                                compressorRatio,
                                compressorAttack,
                                compressorRelease,
                                compressorMakeupGain
                            )

                            if (limiterEnabled) {
                                setLimiter(limiterThreshold, limiterRelease, 0.0f)
                                setLimiterEnabled(true)
                            }

                            if (agcEnabled) {
                                setAGCTargetLevel(agcTargetLevel)
                                setAGCMaxGain(agcMaxGain)
                                setAGCMinGain(agcMinGain)
                                setAGCAttackTime(agcAttackTime)
                                setAGCReleaseTime(agcReleaseTime)
                                setAGCNoiseThreshold(agcNoiseThreshold)
                                setAGCWindowSize(agcWindowSize)
                                setAGCEnabled(true)
                            }

                            Toast.makeText(
                                this@MainActivity,
                                "Audio started - Full DSP chain active",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Microphone permission required",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },

                    onStop = {
                        Log.i(TAG, "Stopping audio engine...")
                        debounceJob?.cancel()
                        stopAudio()
                        Toast.makeText(
                            this@MainActivity,
                            "Audio stopped",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    agcEnabled = agcEnabled,
                    agcTargetLevel = agcTargetLevel,
                    agcMaxGain = agcMaxGain,
                    agcMinGain = agcMinGain,
                    agcAttackTime = agcAttackTime,
                    agcReleaseTime = agcReleaseTime,
                    agcNoiseThreshold = agcNoiseThreshold,
                    agcWindowSize = agcWindowSize,
                    compressorThreshold = compressorThreshold,
                    compressorRatio = compressorRatio,
                    compressorAttack = compressorAttack,
                    compressorRelease = compressorRelease,
                    compressorMakeupGain = compressorMakeupGain,
                    limiterEnabled = limiterEnabled,
                    limiterThreshold = limiterThreshold,
                    limiterRelease = limiterRelease,
                    getAGCCurrentGain = { getAGCCurrentGain() },
                    getAGCCurrentLevel = { getAGCCurrentLevel() },
                    getLimiterGainReduction = { getLimiterGainReduction() }
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
                    "Microphone permission granted"
                else
                    "Microphone permission denied"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}