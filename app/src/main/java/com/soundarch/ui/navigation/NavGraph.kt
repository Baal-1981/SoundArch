package com.soundarch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.soundarch.ui.model.UiMode
import com.soundarch.ui.model.UiModeConfig
import com.soundarch.ui.screens.HomeScreenV2
import com.soundarch.ui.screens.advanced.AdvancedAudioEngineScreen
import com.soundarch.ui.screens.advanced.dynamics.AGCScreen
import com.soundarch.ui.screens.advanced.dynamics.CompressorScreen
import com.soundarch.ui.screens.advanced.dynamics.DynamicsMenuScreen
import com.soundarch.ui.screens.advanced.dynamics.LimiterScreen
import com.soundarch.ui.screens.advanced.eq.EqualizerScreen
import com.soundarch.ui.screens.advanced.eq.EqSettingsScreen
import com.soundarch.ui.screens.common.ComingSoonScreen
import com.soundarch.ui.screens.advanced.noisecancel.NoiseCancellingScreen
import com.soundarch.ui.screens.advanced.bluetooth.BluetoothScreen
import com.soundarch.ui.screens.advanced.perf.PerformanceScreen
import com.soundarch.ui.screens.advanced.build.BuildRuntimeScreen
import com.soundarch.ui.screens.advanced.diagnostics.DiagnosticsScreen
import com.soundarch.ui.screens.advanced.logs.LogsScreen
import com.soundarch.ui.screens.advanced.app.AppPermissionsScreen
import com.soundarch.ui.screens.advanced.ml.MlComingSoonScreen
import com.soundarch.viewmodel.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    latency: Float,
    cpuUsage: Float,              // Legacy - replaced by systemCpuPercent
    memoryUsageMB: Float,          // Legacy - replaced by systemRamPercent
    systemCpuPercent: Float,       // System CPU usage percentage (0-100)
    systemRamPercent: Float,       // System RAM usage percentage (0-100)
    systemRamUsedBytes: Long,      // System RAM used in bytes
    systemRamAvailableBytes: Long, // System RAM available in bytes
    peakDb: Float,                 // ✅ Peak meter
    rmsDb: Float,                  // ✅ RMS meter
    bands: List<Int>,
    gains: List<Float>,
    eqMasterEnabled: Boolean,      // ✅ EQ Master Toggle
    voiceGainDb: Float,            // ✅ Voice Gain
    uiMode: UiMode,                // ✅ UI Mode (Friendly/Advanced)
    uiModeConfig: UiModeConfig,    // ✅ UI Mode Configuration
    onToggleUiMode: () -> Unit,    // ✅ Toggle UI Mode
    isEngineRunning: Boolean,      // ✅ Engine running state
    onBandChange: (Int, Float) -> Unit,
    onEqMasterToggle: (Boolean) -> Unit, // ✅ EQ Master Toggle callback
    onEqPresetSelect: (EqViewModel.EqPreset) -> Unit, // ✅ EQ Preset selector
    onReset: () -> Unit,
    onCompressorChange: (Float, Float, Float, Float, Float) -> Unit,
    onLimiterChange: (Float, Float) -> Unit,
    onLimiterToggle: (Boolean) -> Unit,
    onAGCChange: (Float, Float, Float, Float, Float, Float, Float) -> Unit,
    onAGCToggle: (Boolean) -> Unit,
    onVoiceGainChange: (Float) -> Unit, // ✅ Voice Gain
    onVoiceGainReset: () -> Unit,       // ✅ Voice Gain Reset
    onStart: () -> Unit,
    onStop: () -> Unit,
    // Feature toggle states from DataStore
    audioEngineEnabled: Boolean,
    dynamicsEnabled: Boolean,
    noiseCancellingEnabled: Boolean,
    bluetoothEnabled: Boolean,
    eqEnabled: Boolean,
    mlEnabled: Boolean,
    performanceEnabled: Boolean,
    buildRuntimeEnabled: Boolean,
    diagnosticsEnabled: Boolean,
    logsTestsEnabled: Boolean,
    appPermissionsEnabled: Boolean,
    safeEnabled: Boolean,
    // Feature toggle callbacks
    onMlToggle: (Boolean) -> Unit,
    onSafeToggle: (Boolean) -> Unit,
    onNoiseCancellingToggle: (Boolean) -> Unit,
    onBluetoothToggle: (Boolean) -> Unit,
    agcEnabled: Boolean,
    agcTargetLevel: Float,
    agcMaxGain: Float,
    agcMinGain: Float,
    agcAttackTime: Float,
    agcReleaseTime: Float,
    agcNoiseThreshold: Float,
    agcWindowSize: Float,
    compressorThreshold: Float,
    compressorRatio: Float,
    compressorAttack: Float,
    compressorRelease: Float,
    compressorMakeupGain: Float,
    limiterEnabled: Boolean,
    limiterThreshold: Float,
    limiterRelease: Float,
    getAGCCurrentGain: () -> Float,
    getAGCCurrentLevel: () -> Float,
    getLimiterGainReduction: () -> Float,
    getCompressorGainReduction: () -> Float,
    // Bluetooth info
    isBluetoothActive: Boolean,
    bluetoothProfile: String?,
    bluetoothCodec: String?,
    bluetoothLatencyMs: Float,
    bluetoothCompensationMs: Float
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(
            route = Routes.Home.route,
            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://home" })
        ) {
            HomeScreenV2(
                // Latency metrics
                latencyTotalMs = latency.toDouble(),
                latencyInputMs = 0.0,
                latencyOutputMs = 0.0,
                latencyEmaMs = latency.toDouble(),
                latencyMinMs = latency.toDouble(),
                latencyMaxMs = latency.toDouble(),
                bufferSize = 128,
                xRunCount = 0,
                framesPerCallback = 128,

                // Audio metrics (from OboeEngine)
                peakDb = peakDb,
                rmsDb = rmsDb,
                headroomDb = null,

                // System Performance (CPU/RAM)
                cpuPercent = systemCpuPercent,
                ramPercent = systemRamPercent,
                ramUsedMB = systemRamUsedBytes / (1024 * 1024), // Convert bytes to MB
                ramTotalMB = (systemRamUsedBytes + systemRamAvailableBytes) / (1024 * 1024), // Total RAM in MB

                // Voice Gain (Post-EQ, Pre-Dynamics)
                voiceGainDb = voiceGainDb,

                // UI Mode
                uiMode = uiMode,
                uiModeConfig = uiModeConfig,
                onToggleUiMode = onToggleUiMode,

                // Engine state
                isEngineRunning = isEngineRunning,

                // Feature states (from DataStore)
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

                // Bluetooth info
                isBluetoothActive = isBluetoothActive,
                bluetoothProfile = bluetoothProfile,
                bluetoothCodec = bluetoothCodec,
                bluetoothLatencyMs = bluetoothLatencyMs,
                bluetoothCompensationMs = bluetoothCompensationMs,

                // EQ data
                bands = bands,
                gains = gains,

                // Control callbacks
                onStart = onStart,
                onStop = onStop,

                // Voice Gain callbacks
                onVoiceGainChange = onVoiceGainChange,
                onVoiceGainReset = onVoiceGainReset,

                // Toggle callbacks
                onMlToggle = onMlToggle,
                onSafeToggle = onSafeToggle,
                onNoiseCancellingToggle = onNoiseCancellingToggle,

                // Navigation callbacks - Route to existing screens
                onNavigateToEqualizer = {
                    navController.navigate(Routes.Equalizer.route)
                },
                onNavigateToAudioEngine = {
                    navController.navigate(Routes.AudioEngine.route)
                },
                onNavigateToDynamics = {
                    navController.navigate(Routes.Dynamics.route)
                },
                onNavigateToNoiseCancelling = {
                    navController.navigate(Routes.NoiseCancelling.route)
                },
                onNavigateToBluetooth = {
                    navController.navigate(Routes.Bluetooth.route)
                },
                onNavigateToEqSettings = {
                    navController.navigate(Routes.Equalizer.route)
                },
                onNavigateToMl = {
                    navController.navigate(Routes.Ml.route)
                },
                onNavigateToPerformance = {
                    navController.navigate(Routes.Performance.route)
                },
                onNavigateToBuildRuntime = {
                    navController.navigate(Routes.BuildRuntime.route)
                },
                onNavigateToDiagnostics = {
                    navController.navigate(Routes.Diagnostics.route)
                },
                onNavigateToLogsTests = {
                    navController.navigate(Routes.LogsTests.route)
                },
                onNavigateToAppPermissions = {
                    navController.navigate(Routes.AppPermissions.route)
                }
            )
        }

        composable(


            route = Routes.Equalizer.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://equalizer" })


        ) {
            EqualizerScreen(
                enabled = eqMasterEnabled,
                onToggle = onEqMasterToggle,
                bands = bands,
                gains = gains,
                onBandChange = onBandChange,
                onPresetSelect = onEqPresetSelect,
                onNavigateToSettings = { navController.navigate(Routes.EqSettings.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.Compressor.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/dynamics/compressor" })


        ) {
            CompressorScreen(
                initialEnabled = true, // Compressor is always enabled (controlled by Dynamics toggle)
                initialThreshold = compressorThreshold,
                initialRatio = compressorRatio,
                initialAttack = compressorAttack,
                initialRelease = compressorRelease,
                initialMakeupGain = compressorMakeupGain,
                onCompressorToggle = { enabled ->
                    // Note: Individual compressor toggle not implemented yet
                    // Currently controlled by global Dynamics toggle in MainActivity
                    // TODO: Add compressorEnabled to DynamicsViewModel if per-module toggle needed
                },
                onCompressorChange = onCompressorChange,
                getCompressorGainReduction = getCompressorGainReduction,
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.Limiter.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/dynamics/limiter" })


        ) {
            LimiterScreen(
                initialEnabled = limiterEnabled,
                initialThreshold = limiterThreshold,
                initialRelease = limiterRelease,
                onLimiterChange = onLimiterChange,
                onLimiterToggle = onLimiterToggle,
                getLimiterGainReduction = getLimiterGainReduction,
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.AGC.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/dynamics/agc" })


        ) {
            AGCScreen(
                initialEnabled = agcEnabled,
                initialTargetLevel = agcTargetLevel,
                initialMaxGain = agcMaxGain,
                initialMinGain = agcMinGain,
                initialAttackTime = agcAttackTime,
                initialReleaseTime = agcReleaseTime,
                initialNoiseThreshold = agcNoiseThreshold,
                initialWindowSize = agcWindowSize,
                onAGCChange = onAGCChange,
                onAGCToggle = onAGCToggle,
                getAGCCurrentGain = getAGCCurrentGain,
                getAGCCurrentLevel = getAGCCurrentLevel,
                onBack = { navController.popBackStack() }
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // ADVANCED SECTIONS - Using placeholder screens for now
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        composable(


            route = Routes.AudioEngine.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/audio-engine" })


        ) {
            AdvancedAudioEngineScreen(
                enabled = true,
                onToggle = { enabled ->
                    if (enabled) {
                        onStart()
                    } else {
                        onStop()
                    }
                },
                blockSize = 128,
                ftzEnabled = true,
                dazEnabled = true,
                cpuPerBlock = cpuUsage,
                isEngineRunning = isEngineRunning,
                onBlockSizeChange = { size ->
                    // Block size change requires restart
                    onStop()
                    // TODO: Save block size to EngineSettingsViewModel
                    onStart()
                },
                onFtzToggle = { enabled ->
                    // TODO: Save to EngineSettingsViewModel
                },
                onDazToggle = { enabled ->
                    // TODO: Save to EngineSettingsViewModel
                },
                onApplyAndRestart = {
                    onStop()
                    onStart()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.Dynamics.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/dynamics" })


        ) {
            DynamicsMenuScreen(
                onNavigateToCompressor = {
                    navController.navigate(Routes.Compressor.route)
                },
                onNavigateToAGC = {
                    navController.navigate(Routes.AGC.route)
                },
                onNavigateToLimiter = {
                    navController.navigate(Routes.Limiter.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.NoiseCancelling.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/noise-cancelling" })


        ) {
            val viewModel: NoiseCancellingViewModel = hiltViewModel()
            val strength by viewModel.strength.collectAsState()
            val spectralFloor by viewModel.spectralFloor.collectAsState()
            val smoothing by viewModel.smoothing.collectAsState()
            val noiseAttack by viewModel.noiseAttack.collectAsState()
            val noiseRelease by viewModel.noiseRelease.collectAsState()
            val residualBoost by viewModel.residualBoost.collectAsState()
            val artifactSuppress by viewModel.artifactSuppress.collectAsState()
            val backend by viewModel.backend.collectAsState()
            val mlAvailable by viewModel.mlAvailable.collectAsState()
            val cpuPerBlock by viewModel.cpuPerBlock.collectAsState()
            val snrEstimate by viewModel.snrEstimate.collectAsState()
            val noiseReduction by viewModel.noiseReduction.collectAsState()

            NoiseCancellingScreen(
                enabled = noiseCancellingEnabled,
                onToggle = onNoiseCancellingToggle,
                strength = strength,
                spectralFloor = spectralFloor,
                smoothing = smoothing,
                noiseAttack = noiseAttack,
                noiseRelease = noiseRelease,
                residualBoost = residualBoost,
                artifactSuppress = artifactSuppress,
                backend = backend,
                mlAvailable = mlAvailable,
                cpuPerBlock = cpuPerBlock,
                snrEstimate = snrEstimate,
                noiseReduction = noiseReduction,
                effectStatus = viewModel.getEffectStatus(),
                cpuStatus = viewModel.getCpuStatus(),
                onStrengthChange = viewModel::setStrength,
                onSpectralFloorChange = viewModel::setSpectralFloor,
                onSmoothingChange = viewModel::setSmoothing,
                onNoiseAttackChange = viewModel::setNoiseAttack,
                onNoiseReleaseChange = viewModel::setNoiseRelease,
                onResidualBoostChange = viewModel::setResidualBoost,
                onArtifactSuppressToggle = viewModel::setArtifactSuppress,
                onBackendChange = viewModel::setBackend,
                onPresetSelect = viewModel::applyPreset,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.Bluetooth.route,
            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/bluetooth" })
        ) {
            val bluetoothViewModel: BluetoothViewModel = hiltViewModel()
            val btProfile by bluetoothViewModel.profile.collectAsState()
            val btCompensationMs by bluetoothViewModel.latencyCompensationMs.collectAsState()
            val btSafeEnabled by bluetoothViewModel.safeEnabled.collectAsState()

            BluetoothScreen(
                enabled = bluetoothEnabled,
                onEnabledChange = onBluetoothToggle,
                selectedProfile = when (btProfile) {
                    BluetoothViewModel.BluetoothProfile.A2DP -> "A2DP"
                    BluetoothViewModel.BluetoothProfile.LE_AUDIO -> "LE"
                    BluetoothViewModel.BluetoothProfile.SCO_HFP -> "SCO"
                },
                onProfileChange = { profileStr ->
                    val profile = when (profileStr) {
                        "A2DP" -> BluetoothViewModel.BluetoothProfile.A2DP
                        "LE" -> BluetoothViewModel.BluetoothProfile.LE_AUDIO
                        "SCO" -> BluetoothViewModel.BluetoothProfile.SCO_HFP
                        else -> BluetoothViewModel.BluetoothProfile.A2DP
                    }
                    bluetoothViewModel.setProfile(profile)
                },
                compensationMs = btCompensationMs,
                onCompensationChange = bluetoothViewModel::setLatencyCompensation,
                safeEnabled = btSafeEnabled,
                onSafeToggle = bluetoothViewModel::setSafeEnabled,
                onPingTest = bluetoothViewModel::startPingTest,
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.EqSettings.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/eq-settings" })


        ) {
            val eqViewModel: EqViewModel = hiltViewModel()
            EqSettingsScreen(
                eqViewModel = eqViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.Ml.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/ml" })


        ) {
            MlComingSoonScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.Performance.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/performance" })


        ) {
            val performanceViewModel: PerformanceViewModel = hiltViewModel()
            val currentProfile by performanceViewModel.currentProfile.collectAsState()

            PerformanceScreen(
                selectedProfile = currentProfile.name,
                onProfileSelect = { profileName ->
                    val profile = when (profileName) {
                        "Balanced" -> PerformanceViewModel.Profile.BALANCED
                        "Fast" -> PerformanceViewModel.Profile.FAST
                        "Ultra" -> PerformanceViewModel.Profile.ULTRA
                        else -> PerformanceViewModel.Profile.BALANCED
                    }
                    performanceViewModel.selectProfile(profile)
                },
                onApply = {
                    performanceViewModel.applyProfile()
                },
                onRestart = {
                    performanceViewModel.applyProfile()
                    onStop()
                    onStart()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.BuildRuntime.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/build-runtime" })


        ) {
            BuildRuntimeScreen(
                isDebugBuild = true,
                appVersion = "2.0.0",
                buildDate = "2025-10-12",
                splitAbi = "arm64-v8a",
                hasNeon = true,
                jniRulesOk = true,
                deviceModel = android.os.Build.MODEL,
                androidVersion = android.os.Build.VERSION.RELEASE,
                apiLevel = android.os.Build.VERSION.SDK_INT,
                cpuAbi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.Diagnostics.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/diagnostics" })


        ) {
            DiagnosticsScreen(
                latencyEmaMs = latency.toDouble(),
                latencyMinMs = latency.toDouble(),
                latencyMaxMs = latency.toDouble(),
                xrunCount = 0,
                framesPerCallback = 128,
                meterUpdateRateHz = 30f,
                onMeterUpdateRateChange = { /* TODO: Wire to ViewModel */ },
                onResetStats = { /* TODO: Wire to ViewModel */ },
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.LogsTests.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/logs-tests" })


        ) {
            LogsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(


            route = Routes.AppPermissions.route,


            deepLinks = listOf(navDeepLink { uriPattern = "soundarch://advanced/app-permissions" })


        ) {
            AppPermissionsScreen(
                hasAudioPermission = true,
                hasBluetoothPermission = true,
                hasNotificationPermission = true,
                hasStoragePermission = true,
                foregroundServiceEnabled = false,
                stateRestoreEnabled = true,
                strictModeEnabled = false,
                crashReportingEnabled = false,
                onRequestAudioPermission = { /* TODO: Wire to ViewModel */ },
                onRequestBluetoothPermission = { /* TODO: Wire to ViewModel */ },
                onRequestNotificationPermission = { /* TODO: Wire to ViewModel */ },
                onRequestStoragePermission = { /* TODO: Wire to ViewModel */ },
                onForegroundServiceToggle = { /* TODO: Wire to ViewModel */ },
                onStateRestoreToggle = { /* TODO: Wire to ViewModel */ },
                onStrictModeToggle = { /* TODO: Wire to ViewModel */ },
                onCrashReportingToggle = { /* TODO: Wire to ViewModel */ },
                onBack = { navController.popBackStack() }
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // REDIRECT ROUTES (from BottomNavBar legacy routes)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        composable(Routes.Advanced.route) {
            // Redirect to AudioEngine (first advanced section)
            navController.navigate(Routes.AudioEngine.route) {
                popUpTo(Routes.Advanced.route) { inclusive = true }
            }
        }

        composable(Routes.Logs.route) {
            // Redirect to LogsTests
            navController.navigate(Routes.LogsTests.route) {
                popUpTo(Routes.Logs.route) { inclusive = true }
            }
        }
    }
}
