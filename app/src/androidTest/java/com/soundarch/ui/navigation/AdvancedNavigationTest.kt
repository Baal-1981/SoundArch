package com.soundarch.ui.navigation

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soundarch.MainActivity
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🧭 ADVANCED NAVIGATION TESTS
 *
 * Tests complex navigation scenarios, edge cases, and back stack management.
 *
 * **Coverage:**
 * - ✅ Multi-level navigation chains
 * - ✅ Back button edge cases
 * - ✅ Back stack popping to specific routes
 * - ✅ Navigation from nested screens
 * - ✅ Equalizer → EQ Settings → Back
 * - ✅ Multiple back presses (return to start)
 * - ✅ Navigation state preservation
 */
@RunWith(AndroidJUnit4::class)
class AdvancedNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val navHelper by lazy { composeTestRule.navigationHelper() }

    @Test
    fun testEqualizerToEqSettings_AndBack() {
        // Navigate: Home → Equalizer
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_EQ_SETTINGS)
        navHelper.assertCurrentRoute(Routes.Equalizer.route)

        // Navigate: Equalizer → EQ Settings
        navHelper.navigateToScreen(UiIds.Equalizer.SETTINGS_BUTTON)
        composeTestRule.waitForIdle()

        navHelper.assertCurrentRoute(Routes.EqSettings.route)
        navHelper.assertScreenDisplayed(UiIds.EqSettings.SCREEN)

        // Back: EQ Settings → Equalizer
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Equalizer.route)

        // Back: Equalizer → Home
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testDynamicsMenu_NavigateToAllDspScreens() {
        // Navigate to Dynamics menu
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        navHelper.assertCurrentRoute(Routes.Dynamics.route)

        // Test navigation to Compressor
        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)
        navHelper.assertCurrentRoute(Routes.Compressor.route)
        navHelper.pressBack()

        // Test navigation to AGC
        navHelper.navigateToScreen(UiIds.DynamicsMenu.AGC_CARD)
        navHelper.assertCurrentRoute(Routes.AGC.route)
        navHelper.pressBack()

        // Test navigation to Limiter
        navHelper.navigateToScreen(UiIds.DynamicsMenu.LIMITER_CARD)
        navHelper.assertCurrentRoute(Routes.Limiter.route)
        navHelper.pressBack()

        // Verify back at Dynamics menu
        navHelper.assertCurrentRoute(Routes.Dynamics.route)
    }

    @Test
    fun testMultipleBackPresses_ReturnToHome() {
        // Navigate deep: Home → Dynamics → Compressor
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)

        // Press back multiple times until Home
        var currentRoute = navHelper.getCurrentRoute()
        var pressCount = 0
        val maxPresses = 10  // Safety limit

        while (currentRoute != Routes.Home.route && pressCount < maxPresses) {
            navHelper.pressBack()
            currentRoute = navHelper.getCurrentRoute()
            pressCount++
        }

        // Verify ended at Home
        navHelper.assertCurrentRoute(Routes.Home.route)

        // Verify took exactly 2 back presses (Compressor → Dynamics → Home)
        assert(pressCount == 2) {
            "Expected 2 back presses, but took $pressCount"
        }
    }

    @Test
    fun testBackStack_AfterMultipleNavigations() {
        // Navigate through multiple screens
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        composeTestRule.waitForIdle()

        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)
        composeTestRule.waitForIdle()

        // Back to Dynamics
        navHelper.pressBack()
        composeTestRule.waitForIdle()

        // Navigate to AGC
        navHelper.navigateToScreen(UiIds.DynamicsMenu.AGC_CARD)
        composeTestRule.waitForIdle()

        // Verify back stack order
        val backStack = navHelper.getBackStack()

        // Should contain: Home, Dynamics, AGC (Compressor was removed by back)
        assert(backStack.contains(Routes.Home.route)) {
            "Back stack should contain Home"
        }
        assert(backStack.contains(Routes.Dynamics.route)) {
            "Back stack should contain Dynamics"
        }
        assert(backStack.contains(Routes.AGC.route)) {
            "Back stack should contain AGC"
        }
        assert(!backStack.contains(Routes.Compressor.route)) {
            "Back stack should NOT contain Compressor (was removed by back)"
        }
    }

    @Test
    fun testNavigationFromHome_ToAllAdvancedSections() {
        // Test all navigation buttons from Home screen
        val advancedSections = listOf(
            Triple(UiIds.Home.ADVANCED_AUDIO_ENGINE, Routes.AudioEngine.route, UiIds.AudioEngine.SCREEN),
            Triple(UiIds.Home.ADVANCED_DYNAMICS, Routes.Dynamics.route, UiIds.DynamicsMenu.SCREEN),
            Triple(UiIds.Home.ADVANCED_NC, Routes.NoiseCancelling.route, UiIds.NoiseCancelling.SCREEN),
            Triple(UiIds.Home.ADVANCED_BLUETOOTH, Routes.Bluetooth.route, UiIds.Bluetooth.SCREEN),
            Triple(UiIds.Home.ADVANCED_ML, Routes.Ml.route, UiIds.ML.SCREEN),
            Triple(UiIds.Home.ADVANCED_PERFORMANCE, Routes.Performance.route, UiIds.Performance.SCREEN),
            Triple(UiIds.Home.ADVANCED_BUILD_RUNTIME, Routes.BuildRuntime.route, UiIds.BuildRuntime.SCREEN),
            Triple(UiIds.Home.ADVANCED_DIAGNOSTICS, Routes.Diagnostics.route, UiIds.Diagnostics.SCREEN),
            Triple(UiIds.Home.ADVANCED_LOGS_TESTS, Routes.LogsTests.route, UiIds.LogsTests.SCREEN),
            Triple(UiIds.Home.ADVANCED_APP_PERMISSIONS, Routes.AppPermissions.route, UiIds.AppPermissions.SCREEN)
        )

        advancedSections.forEach { (buttonTag, expectedRoute, screenTag) ->
            // Navigate to section
            navHelper.navigateToScreen(buttonTag)
            composeTestRule.waitForIdle()

            // Verify navigation succeeded
            navHelper.assertCurrentRoute(expectedRoute)
            navHelper.assertScreenDisplayed(screenTag)

            // Return to Home
            navHelper.pressBack()
            composeTestRule.waitForIdle()

            navHelper.assertCurrentRoute(Routes.Home.route)
        }
    }

    @Test
    fun testBackButton_ClicksMatchNavigationDepth() {
        // Navigate: Home → Dynamics → Compressor
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)

        // Get initial back stack size
        val initialStackSize = navHelper.getBackStack().size

        // Press back once
        navHelper.pressBack()
        val afterOneBack = navHelper.getBackStack().size

        // Verify stack decreased by 1
        assert(initialStackSize == afterOneBack + 1) {
            "Back stack should decrease by 1 after one back press"
        }

        // Press back again
        navHelper.pressBack()
        val afterTwoBack = navHelper.getBackStack().size

        // Verify stack decreased by 1 again
        assert(afterOneBack == afterTwoBack + 1) {
            "Back stack should decrease by 1 after second back press"
        }

        // Verify at Home
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testNavigationState_PreservedAcrossScreens() {
        // Navigate: Home → Equalizer
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_EQ_SETTINGS)
        navHelper.assertCurrentRoute(Routes.Equalizer.route)

        // Navigate: Equalizer → Home (using back)
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)

        // Navigate back to Equalizer
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_EQ_SETTINGS)

        // Verify Equalizer screen is displayed (state should be preserved/restored)
        navHelper.assertCurrentRoute(Routes.Equalizer.route)
        navHelper.assertScreenDisplayed(UiIds.Equalizer.SCREEN)
    }

    @Test
    fun testNestedNavigation_DynamicsToCompressor_ThenNewPath() {
        // Navigate: Home → Dynamics → Compressor
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)

        // Back to Dynamics
        navHelper.pressBack()

        // Navigate to different screen: Dynamics → Limiter
        navHelper.navigateToScreen(UiIds.DynamicsMenu.LIMITER_CARD)
        navHelper.assertCurrentRoute(Routes.Limiter.route)

        // Verify back stack
        val backStack = navHelper.getBackStack()

        // Should contain: Home, Dynamics, Limiter (NOT Compressor)
        assert(backStack.contains(Routes.Home.route))
        assert(backStack.contains(Routes.Dynamics.route))
        assert(backStack.contains(Routes.Limiter.route))
        assert(!backStack.contains(Routes.Compressor.route)) {
            "Compressor should not be in back stack after navigating to different path"
        }
    }

    @Test
    fun testBackStackOrder_AfterComplexNavigation() {
        // Complex navigation path:
        // Home → Dynamics → AGC → Back → Limiter → Back → Back → Equalizer

        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        navHelper.navigateToScreen(UiIds.DynamicsMenu.AGC_CARD)
        navHelper.pressBack()
        navHelper.navigateToScreen(UiIds.DynamicsMenu.LIMITER_CARD)
        navHelper.pressBack()
        navHelper.pressBack()
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_EQ_SETTINGS)

        // Verify final state
        navHelper.assertCurrentRoute(Routes.Equalizer.route)

        // Verify back stack
        val backStack = navHelper.getBackStack()

        // Should contain: Home, Equalizer (all Dynamics-related routes removed by backs)
        assert(backStack.contains(Routes.Home.route))
        assert(backStack.contains(Routes.Equalizer.route))
        assert(!backStack.contains(Routes.Dynamics.route))
        assert(!backStack.contains(Routes.AGC.route))
        assert(!backStack.contains(Routes.Limiter.route))
    }

    @Test
    fun testPrintNavigationState_DebugHelper() {
        // Navigate through screens
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)

        // Print state for debugging (visual verification in logs)
        navHelper.printNavigationState()

        // Verify output contains expected information (console output)
        // This is primarily for developer debugging, not automated verification
        val currentRoute = navHelper.getCurrentRoute()
        val backStack = navHelper.getBackStack()

        assert(currentRoute == Routes.Compressor.route)
        assert(backStack.size >= 3) // Home, Dynamics, Compressor
    }
}
