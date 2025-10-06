package com.soundarch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.soundarch.ui.screens.CompressorScreen
import com.soundarch.ui.screens.EqualizerScreen
import com.soundarch.ui.screens.HomeScreen
import com.soundarch.ui.screens.LimiterScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    latency: Float,
    bands: List<Int>,
    gains: List<Float>,
    onBandChange: (Int, Float) -> Unit,
    onReset: () -> Unit,
    onCompressorChange: (Float, Float, Float, Float, Float) -> Unit,
    onLimiterChange: (Float, Float) -> Unit,  // 🔸 NOUVEAU
    onLimiterToggle: (Boolean) -> Unit,       // 🔸 NOUVEAU
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                latency = latency,
                onNavigateToEqualizer = {
                    navController.navigate(Routes.Equalizer.route)
                },
                onNavigateToCompressor = {
                    navController.navigate(Routes.Compressor.route)
                },
                onNavigateToLimiter = {  // 🔸 NOUVEAU
                    navController.navigate(Routes.Limiter.route)
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
                onCompressorChange = onCompressorChange,
                onBack = { navController.popBackStack() }
            )
        }

        // 🔸 NOUVEAU : Route Limiter
        composable(Routes.Limiter.route) {
            LimiterScreen(
                onLimiterChange = onLimiterChange,
                onLimiterToggle = onLimiterToggle,
                onBack = { navController.popBackStack() }
            )
        }
    }
}