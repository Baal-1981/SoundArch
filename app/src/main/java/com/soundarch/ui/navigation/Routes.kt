package com.soundarch.ui.navigation

sealed class Routes(val route: String) {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🏠 MAIN NAVIGATION (Bottom Bar)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    data object Home : Routes("home")
    data object Equalizer : Routes("equalizer")

    // ⚙️ REDIRECT ROUTES (used by BottomNavBar, redirect to actual screens)
    data object Advanced : Routes("advanced")  // Redirects to AudioEngine
    data object Logs : Routes("logs")           // Redirects to LogsTests

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎛️ DSP SCREENS (sub-routes of Advanced → Dynamics)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    data object Compressor : Routes("advanced/dynamics/compressor")
    data object Limiter : Routes("advanced/dynamics/limiter")
    data object AGC : Routes("advanced/dynamics/agc")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ⚙️ ADVANCED SECTIONS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    data object AudioEngine : Routes("advanced/audio_engine")
    data object Dynamics : Routes("advanced/dynamics")
    data object NoiseCancelling : Routes("advanced/noise_cancelling")
    data object Bluetooth : Routes("advanced/bluetooth")
    data object EqSettings : Routes("advanced/eq_settings")
    data object Ml : Routes("advanced/ml")
    data object Performance : Routes("advanced/performance")
    data object BuildRuntime : Routes("advanced/build_runtime")
    data object Diagnostics : Routes("advanced/diagnostics")
    data object LogsTests : Routes("advanced/logs_tests")
    data object AppPermissions : Routes("advanced/app_permissions")
}