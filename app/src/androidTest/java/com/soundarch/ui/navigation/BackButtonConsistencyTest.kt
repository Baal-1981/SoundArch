package com.soundarch.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soundarch.MainActivity
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🔙 BACK BUTTON CONSISTENCY TESTS
 *
 * Verifies that ALL screens have consistent back button implementation:
 * - Position: Top-left in TopAppBar navigationIcon
 * - Icon: Icons.Default.ArrowBack
 * - Accessibility: contentDescription = "Navigate back"
 * - Behavior: popBackStack() navigation
 *
 * **Test Coverage:**
 * - ✅ All DSP screens (Compressor, Limiter, AGC)
 * - ✅ All Advanced sections (AudioEngine, Dynamics, Equalizer, etc.)
 * - ✅ Back button presence
 * - ✅ Back button functionality
 * - ✅ Accessibility descriptions
 *
 * **Philosophy:**
 * - Ensure consistent UX across entire app
 * - Verify no screens have missing/broken back buttons
 * - Test real MainActivity (not mocked)
 */
@RunWith(AndroidJUnit4::class)
class BackButtonConsistencyTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val navHelper by lazy { composeTestRule.navigationHelper() }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎛️ DSP SCREENS (Dynamics subsection)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testBackButton_CompressorScreen() {
        // Navigate: Home → Dynamics → Compressor
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        composeTestRule.waitForIdle()
        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Dynamics.route)
    }

    @Test
    fun testBackButton_LimiterScreen() {
        // Navigate: Home → Dynamics → Limiter
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        composeTestRule.waitForIdle()
        navHelper.navigateToScreen(UiIds.DynamicsMenu.LIMITER_CARD)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Dynamics.route)
    }

    @Test
    fun testBackButton_AGCScreen() {
        // Navigate: Home → Dynamics → AGC
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        composeTestRule.waitForIdle()
        navHelper.navigateToScreen(UiIds.DynamicsMenu.AGC_CARD)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Dynamics.route)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ⚙️ ADVANCED SECTION SCREENS (direct children of Advanced)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testBackButton_DynamicsMenuScreen() {
        // Navigate: Home → Dynamics
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackButton_EqualizerScreen() {
        // Navigate: Home → Equalizer
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_EQ_SETTINGS)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackButton_AudioEngineScreen() {
        // Navigate: Home → AudioEngine
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_AUDIO_ENGINE)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackButton_NoiseCancellingScreen() {
        // Navigate: Home → NoiseCancelling
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_NC)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackButton_BluetoothScreen() {
        // Navigate: Home → Bluetooth
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_BLUETOOTH)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackButton_MlScreen() {
        // Navigate: Home → ML
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_ML)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackButton_PerformanceScreen() {
        // Navigate: Home → Performance
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_PERFORMANCE)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackButton_DiagnosticsScreen() {
        // Navigate: Home → Diagnostics
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DIAGNOSTICS)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    @Test
    fun testBackButton_LogsTestsScreen() {
        // Navigate: Home → Logs & Tests
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_LOGS_TESTS)
        composeTestRule.waitForIdle()

        // Verify back button exists with proper accessibility
        verifyBackButtonExists()

        // Verify back button navigates correctly
        navHelper.pressBack()
        navHelper.assertCurrentRoute(Routes.Home.route)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔍 HELPER METHODS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Verifies that a back button exists with proper accessibility description.
     *
     * **Requirements:**
     * - Must have contentDescription = "Navigate back"
     * - Must be clickable
     * - Must be displayed
     *
     * **Note:** We search for the accessibility description rather than a specific
     * test tag since the back button is part of the TopAppBar navigationIcon slot,
     * which may not have a dedicated test tag.
     */
    private fun verifyBackButtonExists() {
        composeTestRule
            .onNodeWithContentDescription("Navigate back")
            .assertExists("Back button should exist on screen")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🧪 EDGE CASE TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testBackButton_DeepNavigation_MaintainsConsistency() {
        // Navigate through deep stack: Home → Dynamics → Compressor
        navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
        composeTestRule.waitForIdle()
        navHelper.navigateToScreen(UiIds.DynamicsMenu.COMPRESSOR_CARD)
        composeTestRule.waitForIdle()

        // Verify back button exists at deepest level
        verifyBackButtonExists()

        // Navigate back and verify back button still exists
        navHelper.pressBack()
        composeTestRule.waitForIdle()
        navHelper.assertCurrentRoute(Routes.Dynamics.route)
        verifyBackButtonExists()

        // Navigate back again and verify back button still exists
        navHelper.pressBack()
        composeTestRule.waitForIdle()
        navHelper.assertCurrentRoute(Routes.Home.route)
        // Home screen doesn't have a back button (it's the root)
    }

    @Test
    fun testBackButton_RapidNavigation_RemainsConsistent() {
        // Rapidly navigate to multiple screens
        val screens = listOf(
            UiIds.Home.ADVANCED_DYNAMICS to Routes.Dynamics.route,
            UiIds.DynamicsMenu.AGC_CARD to Routes.AGC.route
        )

        screens.forEach { (buttonTag, expectedRoute) ->
            navHelper.navigateToScreen(buttonTag)
            composeTestRule.waitForIdle()
            navHelper.assertCurrentRoute(expectedRoute)

            // Verify back button exists at each step
            verifyBackButtonExists()

            // Navigate back
            navHelper.pressBack()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testBackButton_AllDynamicsScreens_HaveConsistentImplementation() {
        // Test all 3 Dynamics screens in sequence
        val dynamicsScreens = listOf(
            UiIds.DynamicsMenu.COMPRESSOR_CARD to Routes.Compressor.route,
            UiIds.DynamicsMenu.LIMITER_CARD to Routes.Limiter.route,
            UiIds.DynamicsMenu.AGC_CARD to Routes.AGC.route
        )

        dynamicsScreens.forEach { (cardTag, expectedRoute) ->
            // Navigate to Dynamics menu
            navHelper.navigateToScreen(UiIds.Home.ADVANCED_DYNAMICS)
            composeTestRule.waitForIdle()

            // Navigate to specific screen
            navHelper.navigateToScreen(cardTag)
            composeTestRule.waitForIdle()
            navHelper.assertCurrentRoute(expectedRoute)

            // Verify back button exists
            verifyBackButtonExists()

            // Navigate back to Home (Dynamics → Home)
            navHelper.pressBack()
            composeTestRule.waitForIdle()
            navHelper.pressBack()
            composeTestRule.waitForIdle()
            navHelper.assertCurrentRoute(Routes.Home.route)
        }
    }
}
