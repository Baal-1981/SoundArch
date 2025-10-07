package com.soundarch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.soundarch.ui.screens.AGCScreen
import com.soundarch.ui.screens.CompressorScreen
import com.soundarch.ui.screens.EqualizerScreen
import com.soundarch.ui.screens.HomeScreen
import com.soundarch.ui.screens.LimiterScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    latency: Float,
    cpuUsage: Float,              // ✅ NOUVEAU
    memoryUsageMB: Float,          // ✅ NOUVEAU
    bands: List<Int>,
    gains: List<Float>,
    onBandChange: (Int, Float) -> Unit,
    onReset: () -> Unit,
    onCompressorChange: (Float, Float, Float, Float, Float) -> Unit,
    onLimiterChange: (Float, Float) -> Unit,
    onLimiterToggle: (Boolean) -> Unit,
    onAGCChange: (Float, Float, Float, Float, Float, Float, Float) -> Unit,
    onAGCToggle: (Boolean) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
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
    limiterRelease: Float
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                latency = latency,
                cpuUsage = cpuUsage,         // ✅ NOUVEAU
                memoryUsageMB = memoryUsageMB, // ✅ NOUVEAU
                onNavigateToEqualizer = {
                    navController.navigate(Routes.Equalizer.route)
                },
                onNavigateToCompressor = {
                    navController.navigate(Routes.Compressor.route)
                },
                onNavigateToLimiter = {
                    navController.navigate(Routes.Limiter.route)
                },
                onNavigateToAGC = {
                    navController.navigate(Routes.AGC.route)
                },
                onStart = onStart,
                onStop = onStop
            )
        }

        composable(Routes.Equalizer.route) {
            EqualizerScreen(
                bands = bands,
                gains = gains,
                onBandChange = onBandChange,
                onReset = onReset,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Compressor.route) {
            CompressorScreen(
                initialThreshold = compressorThreshold,
                initialRatio = compressorRatio,
                initialAttack = compressorAttack,
                initialRelease = compressorRelease,
                initialMakeupGain = compressorMakeupGain,
                onCompressorChange = onCompressorChange,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Limiter.route) {
            LimiterScreen(
                initialEnabled = limiterEnabled,
                initialThreshold = limiterThreshold,
                initialRelease = limiterRelease,
                onLimiterChange = onLimiterChange,
                onLimiterToggle = onLimiterToggle,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AGC.route) {
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
                onBack = { navController.popBackStack() }
            )
        }
    }
}