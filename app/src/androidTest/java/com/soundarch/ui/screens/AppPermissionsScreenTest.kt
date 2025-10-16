package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.app.AppPermissionsScreen
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for AppPermissionsScreen
 *
 * P0-1 Week 1 - UI Screen Tests (20 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Audio Recording Permission (3 tests)
 * 3. Bluetooth Permission (2 tests)
 * 4. Notification Permission (2 tests)
 * 5. Storage Permission (2 tests)
 * 6. Foreground Service (3 tests)
 * 7. App Behavior Toggles (3 tests - State Restore, StrictMode, Crash Reporting)
 * 8. Privacy Note (1 test)
 * 9. Navigation (2 tests)
 *
 * Total: 20 tests
 */
class AppPermissionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup AppPermissionsScreen with default params
     */
    private fun setupAppPermissionsScreen(
        hasAudioPermission: Boolean = false,
        hasBluetoothPermission: Boolean = false,
        hasNotificationPermission: Boolean = false,
        hasStoragePermission: Boolean = false,
        foregroundServiceEnabled: Boolean = false,
        stateRestoreEnabled: Boolean = true,
        strictModeEnabled: Boolean = false,
        crashReportingEnabled: Boolean = true,
        onRequestAudioPermission: () -> Unit = {},
        onRequestBluetoothPermission: () -> Unit = {},
        onRequestNotificationPermission: () -> Unit = {},
        onRequestStoragePermission: () -> Unit = {},
        onForegroundServiceToggle: (Boolean) -> Unit = {},
        onStateRestoreToggle: (Boolean) -> Unit = {},
        onStrictModeToggle: (Boolean) -> Unit = {},
        onCrashReportingToggle: (Boolean) -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            AppPermissionsScreen(
                hasAudioPermission = hasAudioPermission,
                hasBluetoothPermission = hasBluetoothPermission,
                hasNotificationPermission = hasNotificationPermission,
                hasStoragePermission = hasStoragePermission,
                foregroundServiceEnabled = foregroundServiceEnabled,
                stateRestoreEnabled = stateRestoreEnabled,
                strictModeEnabled = strictModeEnabled,
                crashReportingEnabled = crashReportingEnabled,
                onRequestAudioPermission = onRequestAudioPermission,
                onRequestBluetoothPermission = onRequestBluetoothPermission,
                onRequestNotificationPermission = onRequestNotificationPermission,
                onRequestStoragePermission = onRequestStoragePermission,
                onForegroundServiceToggle = onForegroundServiceToggle,
                onStateRestoreToggle = onStateRestoreToggle,
                onStrictModeToggle = onStrictModeToggle,
                onCrashReportingToggle = onCrashReportingToggle,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.TITLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Audio Recording Permission (3 tests)
    // =============================================================================

    @Test
    fun audioPermissionRowIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithText("Audio Recording")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun audioPermissionShowsGrantButtonWhenNotGranted() {
        setupAppPermissionsScreen(hasAudioPermission = false)

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.PERMISSION_AUDIO_RECORD)
            .assertExists()
            .assertTextContains("Grant")
    }

    @Test
    fun audioPermissionShowsCheckmarkWhenGranted() {
        setupAppPermissionsScreen(hasAudioPermission = true)

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.PERMISSION_AUDIO_RECORD)
            .assertExists()
            .assertTextContains("✓")
    }

    // =============================================================================
    // CATEGORY 3: Bluetooth Permission (2 tests)
    // =============================================================================

    @Test
    fun bluetoothPermissionRowIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithText("Bluetooth")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun bluetoothPermissionRequestTriggersCallback() {
        var requested = false
        setupAppPermissionsScreen(
            hasBluetoothPermission = false,
            onRequestBluetoothPermission = { requested = true }
        )

        // Find Bluetooth permission row's Grant button
        // Note: Using text search since testTag might be shared
        composeTestRule.onAllNodesWithText("Grant")[0]
            .performClick()

        assert(requested)
    }

    // =============================================================================
    // CATEGORY 4: Notification Permission (2 tests)
    // =============================================================================

    @Test
    fun notificationPermissionRowIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithText("Notifications")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun notificationPermissionRequestTriggersCallback() {
        var requested = false
        setupAppPermissionsScreen(
            hasNotificationPermission = false,
            onRequestNotificationPermission = { requested = true }
        )

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.PERMISSION_NOTIFICATIONS)
            .performClick()

        assert(requested)
    }

    // =============================================================================
    // CATEGORY 5: Storage Permission (2 tests)
    // =============================================================================

    @Test
    fun storagePermissionRowIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithText("Storage")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun storagePermissionRequestTriggersCallback() {
        var requested = false
        setupAppPermissionsScreen(
            hasStoragePermission = false,
            onRequestStoragePermission = { requested = true }
        )

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.PERMISSION_STORAGE)
            .performClick()

        assert(requested)
    }

    // =============================================================================
    // CATEGORY 6: Foreground Service (3 tests)
    // =============================================================================

    @Test
    fun foregroundServiceToggleIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.FOREGROUND_SERVICE_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun foregroundServiceStatusShowsRunningWhenEnabled() {
        setupAppPermissionsScreen(foregroundServiceEnabled = true)

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.FOREGROUND_SERVICE_STATUS)
            .assertExists()
            .assertTextContains("✓ Service is running")
    }

    @Test
    fun foregroundServiceStatusShowsStoppedWhenDisabled() {
        setupAppPermissionsScreen(foregroundServiceEnabled = false)

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.FOREGROUND_SERVICE_STATUS)
            .assertExists()
            .assertTextContains("✗ Service is stopped")
    }

    // =============================================================================
    // CATEGORY 7: App Behavior Toggles (3 tests)
    // =============================================================================

    @Test
    fun strictModeToggleIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.STRICT_MODE_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun strictModeToggleClickTriggersCallback() {
        var toggled = false
        setupAppPermissionsScreen(
            strictModeEnabled = false,
            onStrictModeToggle = { toggled = true }
        )

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.STRICT_MODE_TOGGLE)
            .performClick()

        assert(toggled)
    }

    @Test
    fun crashReportingToggleClickTriggersCallback() {
        var toggled = false
        setupAppPermissionsScreen(
            crashReportingEnabled = false,
            onCrashReportingToggle = { toggled = true }
        )

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.CRASH_REPORTING_TOGGLE)
            .performClick()

        assert(toggled)
    }

    // =============================================================================
    // CATEGORY 8: Privacy Note (1 test)
    // =============================================================================

    @Test
    fun privacyNoteIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithText("Privacy")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(
            "SoundArch processes all audio locally on your device",
            substring = true
        ).assertExists()
    }

    // =============================================================================
    // CATEGORY 9: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupAppPermissionsScreen()

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupAppPermissionsScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.AppPermissions.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
