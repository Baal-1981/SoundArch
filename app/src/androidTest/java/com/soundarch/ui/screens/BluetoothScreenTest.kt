package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.bluetooth.BluetoothScreen
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for BluetoothScreen
 *
 * P0-1 Week 1 - UI Screen Tests (22 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Master Toggle (2 tests)
 * 3. Status Badges (3 tests)
 * 4. Profile Selection (3 tests - one per profile)
 * 5. Profile Callbacks (3 tests)
 * 6. Latency Compensation (4 tests)
 * 7. SAFE Mode (2 tests)
 * 8. Ping Test (2 tests)
 * 9. Navigation (2 tests)
 *
 * Total: 22 tests
 */
class BluetoothScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup BluetoothScreen with default params
     */
    private fun setupBluetoothScreen(
        enabled: Boolean = true,
        onEnabledChange: (Boolean) -> Unit = {},
        selectedProfile: String = "A2DP",
        onProfileChange: (String) -> Unit = {},
        compensationMs: Float = 100f,
        onCompensationChange: (Float) -> Unit = {},
        safeEnabled: Boolean = true,
        onSafeToggle: (Boolean) -> Unit = {},
        onPingTest: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            BluetoothScreen(
                enabled = enabled,
                onEnabledChange = onEnabledChange,
                selectedProfile = selectedProfile,
                onProfileChange = onProfileChange,
                compensationMs = compensationMs,
                onCompensationChange = onCompensationChange,
                safeEnabled = safeEnabled,
                onSafeToggle = onSafeToggle,
                onPingTest = onPingTest,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.TITLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Master Toggle (2 tests)
    // =============================================================================

    @Test
    fun masterToggleIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.MASTER_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun masterToggleClickTriggersCallback() {
        var toggledTo = false
        setupBluetoothScreen(
            enabled = false,
            onEnabledChange = { toggledTo = it }
        )

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.MASTER_TOGGLE)
            .performClick()

        assert(toggledTo)
    }

    // =============================================================================
    // CATEGORY 3: Status Badges (3 tests)
    // =============================================================================

    @Test
    fun statusBadgesRowIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.STATUS_BADGES_ROW)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun statusBadgeShowsOnWhenEnabled() {
        setupBluetoothScreen(enabled = true)

        composeTestRule.onNodeWithText("ON")
            .assertExists()
    }

    @Test
    fun statusBadgeShowsOffWhenDisabled() {
        setupBluetoothScreen(enabled = false)

        composeTestRule.onNodeWithText("OFF")
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 4: Profile Selection (3 tests)
    // =============================================================================

    @Test
    fun a2dpProfileButtonIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.PROFILE_A2DP)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun leAudioProfileButtonIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.PROFILE_LE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun scoProfileButtonIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.PROFILE_SCO)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 5: Profile Callbacks (3 tests)
    // =============================================================================

    @Test
    fun a2dpProfileClickTriggersCallback() {
        var selectedProfile = ""
        setupBluetoothScreen(
            selectedProfile = "LE",
            onProfileChange = { selectedProfile = it }
        )

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.PROFILE_A2DP)
            .performClick()

        assert(selectedProfile == "A2DP")
    }

    @Test
    fun leAudioProfileClickTriggersCallback() {
        var selectedProfile = ""
        setupBluetoothScreen(
            selectedProfile = "A2DP",
            onProfileChange = { selectedProfile = it }
        )

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.PROFILE_LE)
            .performClick()

        assert(selectedProfile == "LE")
    }

    @Test
    fun scoProfileClickTriggersCallback() {
        var selectedProfile = ""
        setupBluetoothScreen(
            selectedProfile = "A2DP",
            onProfileChange = { selectedProfile = it }
        )

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.PROFILE_SCO)
            .performClick()

        assert(selectedProfile == "SCO")
    }

    // =============================================================================
    // CATEGORY 6: Latency Compensation (4 tests)
    // =============================================================================

    @Test
    fun compensationSliderIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.COMPENSATION_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun compensationValueTextIsDisplayed() {
        setupBluetoothScreen(compensationMs = 100f)

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.COMPENSATION_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun compensationValueShowsCorrectValue() {
        setupBluetoothScreen(compensationMs = 250f)

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.COMPENSATION_VALUE_TEXT)
            .assertTextContains("250", substring = true)
    }

    @Test
    fun compensationSliderRange() {
        // Test with minimum value
        setupBluetoothScreen(compensationMs = 0f)
        composeTestRule.onNodeWithTag(UiIds.Bluetooth.COMPENSATION_VALUE_TEXT)
            .assertTextContains("0", substring = true)

        // Test with maximum value
        setupBluetoothScreen(compensationMs = 500f)
        composeTestRule.onNodeWithTag(UiIds.Bluetooth.COMPENSATION_VALUE_TEXT)
            .assertTextContains("500", substring = true)
    }

    // =============================================================================
    // CATEGORY 7: SAFE Mode (2 tests)
    // =============================================================================

    @Test
    fun safeToggleIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.SAFE_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun safeToggleClickTriggersCallback() {
        var safeToggled = false
        setupBluetoothScreen(
            safeEnabled = false,
            onSafeToggle = { safeToggled = it }
        )

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.SAFE_TOGGLE)
            .performClick()

        assert(safeToggled)
    }

    // =============================================================================
    // CATEGORY 8: Ping Test (2 tests)
    // =============================================================================

    @Test
    fun pingTestButtonIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.PING_TEST_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun pingTestButtonClickTriggersCallback() {
        var pingTestClicked = false
        setupBluetoothScreen(onPingTest = { pingTestClicked = true })

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.PING_TEST_BUTTON)
            .performClick()

        assert(pingTestClicked)
    }

    // =============================================================================
    // CATEGORY 9: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupBluetoothScreen()

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupBluetoothScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.Bluetooth.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
