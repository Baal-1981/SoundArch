package com.soundarch.ui.navigation

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soundarch.MainActivity
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🧭 BASIC NAVIGATION TESTS
 *
 * Tests fundamental navigation flows (forward navigation, back button).
 *
 * **Coverage:**
 * - ✅ Home → Dynamics → Compressor
 * - ✅ Home → Equalizer
 * - ✅ Home → Audio Engine
 * - ✅ Home → Noise Cancelling
 * - ✅ Back button from each screen
 * - ✅ Back stack verification
 *
 * **Test Philosophy:**
 * - Use UiIds for stable selectors
 * - Verify route changes
 * - Verify back stack state
 * - Test real MainActivity (not mocked)
 */
@RunWith(AndroidJUnit4::class)
class BasicNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val navHelper by lazy { composeTestRule.navigationHelper() }

    @Test
    fun testHomeScreen_IsStartDestination() {
        // Verify Home screen is displayed at launch
        navHelper.assertScreenDisplayed(UiIds.Home.SCREEN)
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testNavigate_HomeToDynamics_Success() {
        // Navigate from Home to Dynamics menu
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)

        // Verify navigation succeeded
        navHelper.assertCurrentRoute(Routes.Dynamics.route)
        navHelper.assertScreenDisplayed(UiIds.DynamicsMenu.SCREEN)

        // Verify back stack contains Home
        navHelper.assertBackStackContains(Routes.Home.route)
    }

    @Test
    fun testNavigate_HomeToDynamicsToCompressor_Success() {
        // Navigate: Home → Dynamics → Compressor
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        composeTestRule.waitForIdle()

        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)
        composeTestRule.waitForIdle()

        // Verify final destination
        navHelper.assertCurrentRoute(Routes.Compressor.route)
        navHelper.assertScreenDisplayed(UiIds.Compressor.SCREEN)

        // Verify back stack
        val backStack = navHelper.getBackStack()
        assert(backStack.contains(Routes.Home.route)) {
            "Back stack should contain Home route"
        }
        assert(backStack.contains(Routes.Dynamics.route)) {
            "Back stack should contain Dynamics route"
        }
    }

    @Test
    fun testNavigate_HomeToEqualizer_Success() {
        // Navigate from Home to Equalizer
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_EQ_SETTINGS)

        // Verify navigation succeeded
        navHelper.assertCurrentRoute(Routes.Equalizer.route)
        navHelper.assertScreenDisplayed(UiIds.Equalizer.SCREEN)
    }

    @Test
    fun testNavigate_HomeToAudioEngine_Success() {
        // Navigate from Home to Audio Engine
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_AUDIO_ENGINE)

        // Verify navigation succeeded
        navHelper.assertCurrentRoute(Routes.AudioEngine.route)
        navHelper.assertScreenDisplayed(UiIds.AudioEngine.SCREEN)
    }

    @Test
    fun testNavigate_HomeToNoiseCancelling_Success() {
        // Navigate from Home to Noise Cancelling
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_NC)

        // Verify navigation succeeded
        navHelper.assertCurrentRoute(Routes.NoiseCancelling.route)
        navHelper.assertScreenDisplayed(UiIds.NoiseCancelling.SCREEN)
    }

    @Test
    fun testNavigate_HomeToBluetooth_Success() {
        // Navigate from Home to Bluetooth
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_BLUETOOTH)

        // Verify navigation succeeded
        navHelper.assertCurrentRoute(Routes.Bluetooth.route)
        navHelper.assertScreenDisplayed(UiIds.Bluetooth.SCREEN)
    }

    @Test
    fun testBackButton_FromDynamicsToHome_Success() {
        // Navigate to Dynamics
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        navHelper.assertCurrentRoute(Routes.Dynamics.route)

        // Press back
        navHelper.pressBack()

        // Verify returned to Home
        navHelper.assertCurrentRoute(Routes.Home.route)
        navHelper.assertScreenDisplayed(UiIds.Home.SCREEN)
    }

    @Test
    fun testBackButton_FromCompressorToDynamicsToHome_Success() {
        // Navigate: Home → Dynamics → Compressor
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        composeTestRule.waitForIdle()

        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)
        composeTestRule.waitForIdle()

        navHelper.assertCurrentRoute(Routes.Compressor.route)

        // Press back (Compressor → Dynamics)
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Dynamics.route)

        // Press back again (Dynamics → Home)
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackButton_FromEqualizerToHome_Success() {
        // Navigate to Equalizer
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_EQ_SETTINGS)
        navHelper.assertCurrentRoute(Routes.Equalizer.route)

        // Press back
        navHelper.pressBack()

        // Verify returned to Home
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackStack_VerifyStackOrder() {
        // Navigate: Home → Dynamics → Compressor
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)

        // Get back stack
        val backStack = navHelper.getBackStack()

        // Verify order (oldest to newest)
        assert(backStack.indexOf(Routes.Home.route) < backStack.indexOf(Routes.Dynamics.route)) {
            "Home should be before Dynamics in back stack"
        }
        assert(backStack.indexOf(Routes.Dynamics.route) < backStack.indexOf(Routes.Compressor.route)) {
            "Dynamics should be before Compressor in back stack"
        }

        // Verify current route is last in stack
        val currentRoute = navHelper.getCurrentRoute()
        assert(backStack.last() == currentRoute) {
            "Current route should be last in back stack"
        }
    }

    @Test
    fun testBackStack_DoesNotContainRoutesAfterBack() {
        // Navigate: Home → Dynamics → Compressor
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)

        // Press back (Compressor → Dynamics)
        navHelper.pressBack()

        // Verify Compressor route is no longer in back stack
        navHelper.assertBackStackDoesNotContain(Routes.Compressor.route)

        // Verify Dynamics and Home are still in stack
        navHelper.assertBackStackContains(Routes.Home.route)
        navHelper.assertBackStackContains(Routes.Dynamics.route)
    }

    @Test
    fun testMultipleScreens_VerifyBackNavigation() {
        // Navigate through multiple screens
        val screens = listOf(
            UiIds.Home.ADVANCED_DYNAMICS to Routes.Dynamics.route,
            UiIds.DynamicsMenu.AGC_CARD to Routes.AGC.route
        )

        // Navigate forward
        screens.forEach { (buttonTag, expectedRoute) ->
            navHelper.navigateToScreen(buttonTag)
            navHelper.assertCurrentRoute(expectedRoute)
        }

        // Navigate back (should return in reverse order)
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Dynamics.route)

        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }
}
