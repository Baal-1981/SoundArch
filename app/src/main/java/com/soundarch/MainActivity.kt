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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.soundarch.data.FeatureTogglesDataStore
import com.soundarch.ui.navigation.NavGraph
import com.soundarch.theme.SoundArchTheme
import com.soundarch.viewmodel.BluetoothViewModel
import com.soundarch.viewmodel.DynamicsViewModel
import com.soundarch.viewmodel.EngineSettingsViewModel
import com.soundarch.viewmodel.EqViewModel
import com.soundarch.viewmodel.NoiseCancellingViewModel
import com.soundarch.viewmodel.UiModeViewModel
import com.soundarch.viewmodel.VoiceGainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Expose NavController for testing
    var navController: androidx.navigation.NavHostController? = null
        private set

    // Permission launchers (must be registered before onCreate)
    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val msg = if (granted) "Microphone permission granted" else "Microphone permission denied"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private val bluetoothConnectPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val msg = if (granted) {
            "Bluetooth permission granted"
        } else {
            "Bluetooth permission denied - Bluetooth features will be disabled"
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        Log.i(TAG, "📻 BLUETOOTH_CONNECT permission ${if (granted) "GRANTED" else "DENIED"}")

        // Start monitoring if granted
        if (granted) {
            bluetoothViewModel?.startBluetoothMonitoring()
            Log.i(TAG, "📻 Bluetooth monitoring started")
        }
    }

    // Reference to BluetoothViewModel (set in setContent)
    private var bluetoothViewModel: BluetoothViewModel? = null

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
    external fun setCompressorKnee(kneeDb: Float)
    external fun setCompressorEnabled(enabled: Boolean)
    external fun getCompressorGainReduction(): Float

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
    // NOISE CANCELLER
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setNoiseCancellerEnabled(enabled: Boolean)
    external fun applyNoiseCancellerPreset(presetIndex: Int)
    external fun setNoiseCancellerParams(
        strength: Float,
        spectralFloor: Float,
        smoothing: Float,
        noiseAttackMs: Float,
        noiseReleaseMs: Float,
        residualBoostDb: Float,
        artifactSuppress: Float
    )
    external fun getNoiseCancellerNoiseFloor(): Float
    external fun getNoiseCancellerCpuMs(): Float
    external fun resetNoiseCancellerCpuStats()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // PERFORMANCE MONITORING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun getCPUUsage(): Float
    external fun getMemoryUsage(): Long

    // System-wide metrics (from OboeEngine PerformanceMetrics)
    external fun getSystemCpuPercent(): Float
    external fun getSystemRamPercent(): Float
    external fun getSystemRamUsedBytes(): Long
    external fun getSystemRamAvailableBytes(): Long

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // LATENCY MONITORING - Detailed Breakdown
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun getLatencyInputMs(): Double
    external fun getLatencyOutputMs(): Double
    external fun getLatencyTotalMs(): Double
    external fun getLatencyEmaMs(): Double
    external fun getLatencyMinMs(): Double
    external fun getLatencyMaxMs(): Double
    external fun getXRunCount(): Int
    external fun getCallbackSize(): Int

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AUDIO LEVELS MONITORING (Peak/RMS Meter)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun getPeakDb(): Float
    external fun getRmsDb(): Float

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // VOICE GAIN CONTROL (Post-EQ, Pre-Dynamics)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    external fun setVoiceGain(gainDb: Float)
    external fun getVoiceGain(): Float
    external fun resetVoiceGain()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BLUETOOTH BRIDGE (JNI)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Initialize Bluetooth bridge callbacks
     * Must be called once in onCreate()
     */
    external fun initBluetoothBridge()

    /**
     * Notify native layer of Bluetooth state change
     * Called from BluetoothProfileManager when connection/codec changes
     */
    external fun notifyBluetoothState(
        connected: Boolean,
        profileName: String,
        codecName: String,
        sampleRate: Int,
        bitrate: Int,
        estimatedLatencyMs: Float
    )

    /**
     * Set latency compensation offset
     * Called from BluetoothScreen when user adjusts latency slider
     */
    external fun setBluetoothLatencyCompensation(additionalMs: Float)

    /**
     * Get current Bluetooth active state
     */
    external fun isBluetoothActive(): Boolean

    /**
     * Get total Bluetooth latency (estimated + compensation)
     */
    external fun getBluetoothLatencyMs(): Float

    /**
     * Get Bluetooth codec name
     */
    external fun getBluetoothCodecName(): String

    /**
     * Get Bluetooth profile name
     */
    external fun getBluetoothProfileName(): String

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BLUETOOTH CALLBACKS (C++ → Kotlin)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Called from native layer when profile switch is requested
     * @param targetProfile - "SCO", "A2DP", or "LE_AUDIO"
     * @param reason - "LOW_LATENCY", "QUALITY", or "STABLE"
     */
    fun onRequestProfileSwitch(targetProfile: String, reason: String) {
        Log.i(TAG, "📻 Native requested profile switch: $targetProfile (reason: $reason)")
        runOnUiThread {
            Toast.makeText(
                this,
                "Bluetooth: Switch to $targetProfile requested ($reason)",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: Trigger BluetoothProfileManager to switch profile
        }
    }

    /**
     * Called from native layer when Safe Mode is activated due to underruns
     */
    fun onBluetoothSafeMode() {
        Log.w(TAG, "⚠️ Bluetooth Safe Mode activated (underruns detected)")
        runOnUiThread {
            Toast.makeText(
                this,
                "Bluetooth Safe Mode: Buffer increased for stability",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Called from native layer when codec change is detected
     */
    fun onCodecChanged() {
        Log.i(TAG, "📻 Bluetooth codec changed - querying new codec")
        runOnUiThread {
            val codecName = getBluetoothCodecName()
            val profileName = getBluetoothProfileName()
            Toast.makeText(
                this,
                "Bluetooth: $profileName / $codecName",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Bluetooth JNI bridge
        initBluetoothBridge()
        Log.i(TAG, "📻 BluetoothBridge initialized")

        // Request permissions (must be done before accessing Bluetooth or microphone)
        requestMicPermission()
        requestBluetoothPermission()

        // Initialize DataStore
        val dataStore = FeatureTogglesDataStore(applicationContext)

        setContent {
            SoundArchTheme {
                val navController = rememberNavController()
                // Expose navController to tests
                SideEffect {
                    this@MainActivity.navController = navController
                }
                val uiModeViewModel: UiModeViewModel = viewModel()
                val engineSettingsViewModel: EngineSettingsViewModel = viewModel()
                val dynamicsViewModel: DynamicsViewModel = viewModel()
                val eqViewModel: EqViewModel = viewModel()
                val voiceGainViewModel: VoiceGainViewModel = viewModel()
                val noiseCancellingViewModel: NoiseCancellingViewModel = viewModel()
                val bluetoothViewModel: BluetoothViewModel = viewModel()
                val uiMode by uiModeViewModel.uiMode.collectAsState()
                val uiModeConfig by uiModeViewModel.config.collectAsState()
                val isEngineRunning by engineSettingsViewModel.isEngineRunning.collectAsState()

                // Save reference for permission callback
                LaunchedEffect(Unit) {
                    this@MainActivity.bluetoothViewModel = bluetoothViewModel

                    // If permission already granted, start monitoring immediately
                    if (hasBluetoothConnectPermission()) {
                        bluetoothViewModel.startBluetoothMonitoring()
                        Log.i(TAG, "📻 Bluetooth monitoring started (permission already granted)")
                    }
                }

                var latency by remember { mutableStateOf(0f) }

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 🔄 FEATURE TOGGLES - Persistent State
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                val audioEngineEnabled by dataStore.audioEngineEnabled.collectAsState(initial = true)
                val dynamicsEnabled by dataStore.dynamicsEnabled.collectAsState(initial = true)
                val bluetoothEnabled by dataStore.bluetoothEnabled.collectAsState(initial = false)
                val noiseCancellingEnabled by dataStore.noiseCancellingEnabled.collectAsState(initial = false)
                val eqEnabled by dataStore.eqEnabled.collectAsState(initial = true)
                val mlEnabled by dataStore.mlEnabled.collectAsState(initial = false)
                val performanceEnabled by dataStore.performanceEnabled.collectAsState(initial = true)
                val buildRuntimeEnabled by dataStore.buildRuntimeEnabled.collectAsState(initial = true)
                val diagnosticsEnabled by dataStore.diagnosticsEnabled.collectAsState(initial = true)
                val logsTestsEnabled by dataStore.logsTestsEnabled.collectAsState(initial = true)
                val appPermissionsEnabled by dataStore.appPermissionsEnabled.collectAsState(initial = true)
                val safeEnabled by dataStore.safeEnabled.collectAsState(initial = false)

                val scope = rememberCoroutineScope()

                // Performance monitoring states
                var cpuUsage by remember { mutableStateOf(0f) }
                var memoryUsage by remember { mutableStateOf(0L) }
                var systemCpuPercent by remember { mutableStateOf(0f) }
                var systemRamPercent by remember { mutableStateOf(0f) }
                var systemRamUsedBytes by remember { mutableStateOf(0L) }
                var systemRamAvailableBytes by remember { mutableStateOf(0L) }

                // Audio level monitoring states
                var peakDb by remember { mutableStateOf(-60f) }
                var rmsDb by remember { mutableStateOf(-60f) }

                // Latency breakdown states
                var latencyInputMs by remember { mutableStateOf(0.0) }
                var latencyOutputMs by remember { mutableStateOf(0.0) }
                var latencyTotalMs by remember { mutableStateOf(0.0) }
                var latencyMinMs by remember { mutableStateOf(0.0) }
                var latencyMaxMs by remember { mutableStateOf(0.0) }
                var xRunCount by remember { mutableStateOf(0) }
                var callbackSize by remember { mutableStateOf(0) }

                // 10 bandes ISO standard (Hz)
                val bands = eqViewModel.bandFrequencies

                // Gains par bande (from ViewModel)
                val gains by eqViewModel.bandGains.collectAsState()

                // EQ Master Toggle (from ViewModel)
                val eqMasterEnabled by eqViewModel.eqMasterEnabled.collectAsState()

                // État Compressor (from ViewModel)
                val compressorThreshold by dynamicsViewModel.compressorThreshold.collectAsState()
                val compressorRatio by dynamicsViewModel.compressorRatio.collectAsState()
                val compressorAttack by dynamicsViewModel.compressorAttack.collectAsState()
                val compressorRelease by dynamicsViewModel.compressorRelease.collectAsState()
                val compressorMakeupGain by dynamicsViewModel.compressorMakeupGain.collectAsState()

                // État Limiter (from ViewModel)
                val limiterEnabled by dynamicsViewModel.limiterEnabled.collectAsState()
                val limiterThreshold by dynamicsViewModel.limiterThreshold.collectAsState()
                val limiterRelease by dynamicsViewModel.limiterRelease.collectAsState()

                // État AGC (from ViewModel)
                val agcEnabled by dynamicsViewModel.agcEnabled.collectAsState()
                val agcTargetLevel by dynamicsViewModel.agcTargetLevel.collectAsState()
                val agcMaxGain by dynamicsViewModel.agcMaxGain.collectAsState()
                val agcMinGain by dynamicsViewModel.agcMinGain.collectAsState()
                val agcAttackTime by dynamicsViewModel.agcAttackTime.collectAsState()
                val agcReleaseTime by dynamicsViewModel.agcReleaseTime.collectAsState()
                val agcNoiseThreshold by dynamicsViewModel.agcNoiseThreshold.collectAsState()
                val agcWindowSize by dynamicsViewModel.agcWindowSize.collectAsState()

                // État Voice Gain (from ViewModel, Post-EQ, Pre-Dynamics)
                val voiceGainDb by voiceGainViewModel.voiceGainDb.collectAsState()

                // Coroutine scope pour debounce optimisé
                var debounceJob by remember { mutableStateOf<Job?>(null) }

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 🔄 SYNC FEATURE TOGGLES TO NATIVE LAYER
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 📻 BLUETOOTH STATE SYNC TO NATIVE LAYER
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                // Observe Bluetooth connection state from ViewModel
                val btIsConnected by bluetoothViewModel.isConnected.collectAsState()
                val btDeviceName by bluetoothViewModel.deviceName.collectAsState()
                val btProfile by bluetoothViewModel.profile.collectAsState()
                val btCodec by bluetoothViewModel.codec.collectAsState()
                val btSampleRate by bluetoothViewModel.sampleRate.collectAsState()
                val btBitrate by bluetoothViewModel.bitrate.collectAsState()
                val btEstimatedLatencyMs by bluetoothViewModel.estimatedLatencyMs.collectAsState()

                // Notify native layer when Bluetooth state changes
                // Always notify actual state - toggle controls DSP processing, not state reporting
                LaunchedEffect(btIsConnected, btProfile, btCodec, btSampleRate, btBitrate, btEstimatedLatencyMs) {
                    bluetoothViewModel.notifyNativeLayer { connected, profileName, codecName, sampleRate, bitrate, latencyMs ->
                        notifyBluetoothState(connected, profileName, codecName, sampleRate, bitrate, latencyMs)
                        Log.i(TAG, "📻 Notified native: BT=$connected Profile=$profileName Codec=$codecName SR=${sampleRate}Hz BR=${bitrate}bps Latency=${latencyMs}ms")
                    }
                }

                // Log Bluetooth device connection
                LaunchedEffect(btIsConnected, btDeviceName) {
                    if (btIsConnected) {
                        Log.i(TAG, "📻 Bluetooth device connected: $btDeviceName")
                    } else {
                        Log.i(TAG, "📵 Bluetooth device disconnected")
                    }
                }

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                // Sync EQ Master Toggle (independent of section visibility toggle)
                LaunchedEffect(eqMasterEnabled, gains) {
                    Log.i(TAG, "🎚️ EQ Master ${if (eqMasterEnabled) "ENABLED" else "DISABLED"}")
                    // When EQ master is OFF, set all bands to 0 (bypass entire EQ)
                    if (!eqMasterEnabled) {
                        setEqBands(FloatArray(gains.size) { 0f })
                    } else {
                        setEqBands(gains.toFloatArray())
                    }
                }

                // Sync EQ section visibility toggle (for Advanced screen)
                LaunchedEffect(eqEnabled) {
                    Log.i(TAG, "🎚️ EQ Section ${if (eqEnabled) "VISIBLE" else "HIDDEN"}")
                    // Note: eqEnabled controls section visibility, eqMasterEnabled controls DSP bypass
                }

                // Sync Dynamics toggle (Compressor + Limiter)
                LaunchedEffect(dynamicsEnabled) {
                    Log.i(TAG, "⚡ Dynamics ${if (dynamicsEnabled) "ENABLED" else "DISABLED"}")
                    setCompressorEnabled(dynamicsEnabled)
                    setLimiterEnabled(dynamicsEnabled && limiterEnabled)
                }

                // Sync Noise Cancelling toggle
                LaunchedEffect(noiseCancellingEnabled) {
                    Log.i(TAG, "🔇 Noise Cancellation ${if (noiseCancellingEnabled) "ENABLED" else "DISABLED"}")
                    setNoiseCancellerEnabled(noiseCancellingEnabled)
                }

                // Sync Noise Cancelling parameters to native layer
                val ncStrength by noiseCancellingViewModel.strength.collectAsState()
                val ncSpectralFloor by noiseCancellingViewModel.spectralFloor.collectAsState()
                val ncSmoothing by noiseCancellingViewModel.smoothing.collectAsState()
                val ncNoiseAttack by noiseCancellingViewModel.noiseAttack.collectAsState()
                val ncNoiseRelease by noiseCancellingViewModel.noiseRelease.collectAsState()
                val ncResidualBoost by noiseCancellingViewModel.residualBoost.collectAsState()
                val ncArtifactSuppress by noiseCancellingViewModel.artifactSuppress.collectAsState()

                LaunchedEffect(noiseCancellingEnabled, ncStrength, ncSpectralFloor, ncSmoothing,
                               ncNoiseAttack, ncNoiseRelease, ncResidualBoost, ncArtifactSuppress) {
                    if (noiseCancellingEnabled) {
                        setNoiseCancellerParams(
                            strength = ncStrength,
                            spectralFloor = ncSpectralFloor,
                            smoothing = ncSmoothing,
                            noiseAttackMs = ncNoiseAttack,
                            noiseReleaseMs = ncNoiseRelease,
                            residualBoostDb = ncResidualBoost,
                            artifactSuppress = if (ncArtifactSuppress) 1.0f else 0.0f
                        )
                        Log.i(TAG, "🔇 NC Params: Str=${String.format("%.2f", ncStrength)} Floor=${String.format("%.1f", ncSpectralFloor)}dB")
                    }
                }

                SideEffect {
                    updateLatencyCallback = { latency = it }
                }

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 📊 MONITORING - Throttled by UI Mode
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // Friendly mode: 200ms (5 FPS) for audio meters
                // Advanced mode: 100ms (10 FPS) for audio meters
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                // Update audio levels (Peak/RMS) - throttled by UI mode
                LaunchedEffect(uiMode) {
                    val meterUpdateRate = if (uiMode == com.soundarch.ui.model.UiMode.FRIENDLY) 200L else 100L
                    while (true) {
                        delay(meterUpdateRate)
                        try {
                            peakDb = getPeakDb()
                            rmsDb = getRmsDb()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error reading audio levels", e)
                        }
                    }
                }

                // Update system metrics (CPU/Memory) - 1Hz independent
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(1000) // 1Hz for CPU/RAM
                        try {
                            // Use process CPU (getCPUUsage) instead of system CPU (getSystemCpuPercent)
                            // because /proc/stat is restricted on Android 10+
                            cpuUsage = getCPUUsage()
                            memoryUsage = getMemoryUsage()
                            systemCpuPercent = cpuUsage  // Use process CPU for display
                            systemRamPercent = getSystemRamPercent()
                            systemRamUsedBytes = getSystemRamUsedBytes()
                            systemRamAvailableBytes = getSystemRamAvailableBytes()
                            Log.i("PerformanceMonitor", "📊 CPU: ${String.format("%.1f", systemCpuPercent)}% | RAM: ${String.format("%.1f", systemRamPercent)}% (${systemRamUsedBytes / 1024 / 1024}MB / ${(systemRamUsedBytes + systemRamAvailableBytes) / 1024 / 1024}MB)")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error reading system metrics", e)
                        }
                    }
                }

                // Update latency breakdown - 500ms (2 FPS) sufficient for monitoring
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(500) // 2 FPS for latency metrics
                        try {
                            latencyInputMs = getLatencyInputMs()
                            latencyOutputMs = getLatencyOutputMs()
                            latencyTotalMs = getLatencyTotalMs()
                            latencyMinMs = getLatencyMinMs()
                            latencyMaxMs = getLatencyMaxMs()
                            xRunCount = getXRunCount()
                            callbackSize = getCallbackSize()
                            Log.i("LatencyMonitor", "🎯 IN=%.2fms OUT=%.2fms Total=%.2fms [%.2f-%.2f] XRuns=%d CB=%d".format(
                                latencyInputMs, latencyOutputMs, latencyTotalMs, latencyMinMs, latencyMaxMs, xRunCount, callbackSize
                            ))
                        } catch (e: Exception) {
                            Log.e(TAG, "Error reading latency stats", e)
                        }
                    }
                }

                NavGraph(
                    navController = navController,
                    latency = latency,
                    cpuUsage = cpuUsage,
                    memoryUsageMB = memoryUsage / 1024f,
                    systemCpuPercent = systemCpuPercent,
                    systemRamPercent = systemRamPercent,
                    systemRamUsedBytes = systemRamUsedBytes,
                    systemRamAvailableBytes = systemRamAvailableBytes,
                    peakDb = peakDb,
                    rmsDb = rmsDb,
                    bands = bands,
                    gains = gains,
                    eqMasterEnabled = eqMasterEnabled,
                    voiceGainDb = voiceGainDb,
                    uiMode = uiMode,
                    uiModeConfig = uiModeConfig,
                    onToggleUiMode = { uiModeViewModel.toggleMode() },
                    isEngineRunning = isEngineRunning,

                    onBandChange = { index, value ->
                        Log.d(TAG, "Band $index -> ${String.format("%.1f", value)}dB")
                        eqViewModel.setBandGain(index, value)
                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(10)
                            Log.i(TAG, "EQ update: Band$index=${String.format("%.1f", value)}dB")
                            setEqBands(gains.toFloatArray())
                        }
                    },

                    onEqMasterToggle = { enabled ->
                        Log.i(TAG, "🎚️ EQ Master ${if (enabled) "ENABLED" else "DISABLED"}")
                        eqViewModel.setEqMasterEnabled(enabled)
                    },

                    onEqPresetSelect = { preset ->
                        Log.i(TAG, "🎵 EQ Preset: $preset")
                        eqViewModel.applyPreset(preset)
                    },

                    onReset = {
                        Log.i(TAG, "Reset all bands to 0dB")
                        eqViewModel.setAllBandGains(List(bands.size) { 0f })
                        debounceJob?.cancel()
                        setEqBands(FloatArray(bands.size) { 0f })
                    },

                    onCompressorChange = { threshold, ratio, attack, release, makeupGain ->
                        Log.i(TAG, "Compressor: Thr=${String.format("%.1f", threshold)}dB Ratio=${String.format("%.1f", ratio)}:1")
                        dynamicsViewModel.setCompressorThreshold(threshold)
                        dynamicsViewModel.setCompressorRatio(ratio)
                        dynamicsViewModel.setCompressorAttack(attack)
                        dynamicsViewModel.setCompressorRelease(release)
                        dynamicsViewModel.setCompressorMakeupGain(makeupGain)
                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(10)
                            setCompressor(threshold, ratio, attack, release, makeupGain)
                        }
                    },

                    onLimiterChange = { threshold, release ->
                        Log.i(TAG, "Limiter: Thr=${String.format("%.1f", threshold)}dB Release=${String.format("%.1f", release)}ms")
                        dynamicsViewModel.setLimiterThreshold(threshold)
                        dynamicsViewModel.setLimiterRelease(release)
                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(10)
                            setLimiter(threshold, release, 0.0f)
                        }
                    },

                    onLimiterToggle = { enabled ->
                        Log.i(TAG, if (enabled) "Limiter ENABLED" else "Limiter DISABLED")
                        dynamicsViewModel.setLimiterEnabled(enabled)
                        setLimiterEnabled(enabled)
                    },

                    onAGCChange = { targetLevel, maxGain, minGain, attackTime, releaseTime, noiseThreshold, windowSize ->
                        Log.i(TAG, "AGC: Target=${String.format("%.1f", targetLevel)}dB MaxGain=${String.format("%.1f", maxGain)}dB")
                        dynamicsViewModel.setAgcTargetLevel(targetLevel)
                        dynamicsViewModel.setAgcMaxGain(maxGain)
                        dynamicsViewModel.setAgcMinGain(minGain)
                        dynamicsViewModel.setAgcAttackTime(attackTime)
                        dynamicsViewModel.setAgcReleaseTime(releaseTime)
                        dynamicsViewModel.setAgcNoiseThreshold(noiseThreshold)
                        dynamicsViewModel.setAgcWindowSize(windowSize)
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
                        dynamicsViewModel.setAgcEnabled(enabled)
                        setAGCEnabled(enabled)
                    },

                    onVoiceGainChange = { gainDb ->
                        Log.i(TAG, "Voice Gain: ${String.format("%+.1f", gainDb)}dB")
                        voiceGainViewModel.setVoiceGainDb(gainDb)
                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(10)
                            setVoiceGain(gainDb)
                        }
                    },

                    onVoiceGainReset = {
                        Log.i(TAG, "Voice Gain: RESET to 0dB")
                        voiceGainViewModel.resetVoiceGain()
                        debounceJob?.cancel()
                        resetVoiceGain()
                    },

                    onStart = {
                        if (hasMicrophonePermission()) {
                            Log.i(TAG, "Starting audio engine...")
                            startAudio()
                            engineSettingsViewModel.setEngineRunning(true)

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
                        engineSettingsViewModel.setEngineRunning(false)
                        Toast.makeText(
                            this@MainActivity,
                            "Audio stopped",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    // Feature toggle states
                    audioEngineEnabled = audioEngineEnabled,
                    dynamicsEnabled = dynamicsEnabled,
                    noiseCancellingEnabled = noiseCancellingEnabled,
                    bluetoothEnabled = bluetoothEnabled,
                    eqEnabled = eqEnabled,
                    mlEnabled = mlEnabled,
                    performanceEnabled = performanceEnabled,
                    buildRuntimeEnabled = buildRuntimeEnabled,
                    diagnosticsEnabled = diagnosticsEnabled,
                    logsTestsEnabled = logsTestsEnabled,
                    appPermissionsEnabled = appPermissionsEnabled,
                    safeEnabled = safeEnabled,

                    // Feature toggle callbacks
                    onMlToggle = { enabled ->
                        scope.launch {
                            dataStore.setMlEnabled(enabled)
                            Log.i(TAG, "🤖 ML ${if (enabled) "ENABLED" else "DISABLED"}")
                        }
                    },
                    onSafeToggle = { enabled ->
                        scope.launch {
                            dataStore.setSafeEnabled(enabled)
                            Log.i(TAG, "🛡️ SAFE MODE ${if (enabled) "ENABLED" else "DISABLED"}")
                        }
                    },
                    onNoiseCancellingToggle = { enabled ->
                        scope.launch {
                            dataStore.setNoiseCancellingEnabled(enabled)
                            Log.i(TAG, "🔇 Noise Cancelling ${if (enabled) "ENABLED" else "DISABLED"}")
                        }
                    },
                    onBluetoothToggle = { enabled ->
                        scope.launch {
                            dataStore.setBluetoothEnabled(enabled)
                            Log.i(TAG, "📻 Bluetooth ${if (enabled) "ENABLED" else "DISABLED"}")
                        }
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
                    getLimiterGainReduction = { getLimiterGainReduction() },
                    getCompressorGainReduction = { getCompressorGainReduction() },

                    // Bluetooth info
                    isBluetoothActive = btIsConnected && bluetoothEnabled,
                    bluetoothProfile = if (btIsConnected) btProfile.name else null,
                    bluetoothCodec = if (btIsConnected) btCodec else null,
                    bluetoothLatencyMs = if (btIsConnected) btEstimatedLatencyMs.toFloat() else 0f,
                    bluetoothCompensationMs = 0f // TODO: Get from BluetoothViewModel when compensation slider is wired
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
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        // BLUETOOTH_CONNECT is only required on Android 12+ (API 31+)
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On Android 11 and below, BLUETOOTH_CONNECT doesn't exist, so return true
            true
        }
    }

    private fun requestBluetoothPermission() {
        // BLUETOOTH_CONNECT is only required on Android 12+ (API 31+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!hasBluetoothConnectPermission()) {
                Log.i(TAG, "📻 Requesting BLUETOOTH_CONNECT permission...")
                bluetoothConnectPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                Log.i(TAG, "📻 BLUETOOTH_CONNECT permission already granted")
            }
        } else {
            Log.i(TAG, "📻 BLUETOOTH_CONNECT not required on Android ${android.os.Build.VERSION.SDK_INT}")
        }
    }
}