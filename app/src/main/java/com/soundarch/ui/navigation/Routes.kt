package com.soundarch.ui.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Equalizer : Routes("equalizer")
    data object Compressor : Routes("compressor")
    data object Limiter : Routes("limiter")
    data object AGC : Routes("agc")  // 🎯 NOUVEAU
}