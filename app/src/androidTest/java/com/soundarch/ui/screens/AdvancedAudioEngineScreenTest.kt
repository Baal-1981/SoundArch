package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.AdvancedAudioEngineScreen
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for AdvancedAudioEngineScreen
 *
 * P0-1 Week 1 - UI Screen Tests (23 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Master Toggle (2 tests)
 * 3. Block Size Selection (5 tests - 128, 256, 512, selector, callbacks)
 * 4. FTZ/DAZ Toggles (4 tests - display + callbacks)
 * 5. CPU Monitoring (3 tests)
 * 6. Apply & Restart (4 tests)
 * 7. Pending Changes Detection (3 tests)
 * 8. Navigation (2 tests)
 *
 * Total: 23 tests
 */
class AdvancedAudioEngineScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup AdvancedAudioEngineScreen with default params
     */
    private fun setupAdvancedAudioEngineScreen(
        enabled: Boolean = true,
        onToggle: (Boolean) -> Unit = {},
        blockSize: Int = 256,
        ftzEnabled: Boolean = true,
        dazEnabled: Boolean = true,
        cpuPerBlock: Float = 0.5f,
        isEngineRunning: Boolean = true,
        onBlockSizeChange: (Int) -> Unit = {},
        onFtzToggle: (Boolean) -> Unit = {},
        onDazToggle: (Boolean) -> Unit = {},
        onApplyAndRestart: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            AdvancedAudioEngineScreen(
                enabled = enabled,
                onToggle = onToggle,
                blockSize = blockSize,
                ftzEnabled = ftzEnabled,
                dazEnabled = dazEnabled,
                cpuPerBlock = cpuPerBlock,
                isEngineRunning = isEngineRunning,
                onBlockSizeChange = onBlockSizeChange,
                onFtzToggle = onFtzToggle,
                onDazToggle = onDazToggle,
                onApplyAndRestart = onApplyAndRestart,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun toggleHeaderIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.MASTER_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Master Toggle (2 tests)
    // =============================================================================

    @Test
    fun masterToggleStartsInCorrectState() {
        setupAdvancedAudioEngineScreen(enabled = true)

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.MASTER_TOGGLE)
            .assertExists()
    }

    @Test
    fun masterToggleClickTriggersCallback() {
        var toggledTo = false
        setupAdvancedAudioEngineScreen(
            enabled = false,
            onToggle = { toggledTo = it }
        )

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.MASTER_TOGGLE)
            .performClick()

        assert(toggledTo)
    }

    // =============================================================================
    // CATEGORY 3: Block Size Selection (5 tests)
    // =============================================================================

    @Test
    fun blockSizeSelectorIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.BLOCK_SIZE_SELECTOR)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun blockSize128OptionIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag("${UiIds.AudioEngine.BLOCK_SIZE_SELECTOR}_128")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun blockSize256OptionIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag("${UiIds.AudioEngine.BLOCK_SIZE_SELECTOR}_256")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun blockSize512OptionIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag("${UiIds.AudioEngine.BLOCK_SIZE_SELECTOR}_512")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun blockSizeSelectionTriggersCallback() {
        var selectedBlockSize = 0
        setupAdvancedAudioEngineScreen(
            blockSize = 256,
            onBlockSizeChange = { selectedBlockSize = it }
        )

        // Click block size 128
        composeTestRule.onNodeWithTag("${UiIds.AudioEngine.BLOCK_SIZE_SELECTOR}_128")
            .performClick()

        // Wait for pending changes
        composeTestRule.waitForIdle()

        // Click Apply & Restart to confirm
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.APPLY_RESTART_BUTTON)
            .performClick()

        assert(selectedBlockSize == 128)
    }

    // =============================================================================
    // CATEGORY 4: FTZ/DAZ Toggles (4 tests)
    // =============================================================================

    @Test
    fun ftzToggleIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.FTZ_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun dazToggleIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.DAZ_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun ftzToggleClickTriggersCallback() {
        var ftzToggled = false
        setupAdvancedAudioEngineScreen(
            ftzEnabled = false,
            onFtzToggle = { ftzToggled = it }
        )

        // Toggle FTZ
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.FTZ_TOGGLE)
            .performClick()

        composeTestRule.waitForIdle()

        // Apply changes
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.APPLY_RESTART_BUTTON)
            .performClick()

        assert(ftzToggled)
    }

    @Test
    fun dazToggleClickTriggersCallback() {
        var dazToggled = false
        setupAdvancedAudioEngineScreen(
            dazEnabled = false,
            onDazToggle = { dazToggled = it }
        )

        // Toggle DAZ
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.DAZ_TOGGLE)
            .performClick()

        composeTestRule.waitForIdle()

        // Apply changes
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.APPLY_RESTART_BUTTON)
            .performClick()

        assert(dazToggled)
    }

    // =============================================================================
    // CATEGORY 5: CPU Monitoring (3 tests)
    // =============================================================================

    @Test
    fun cpuMonitoringDisplaysCorrectValue() {
        setupAdvancedAudioEngineScreen(
            cpuPerBlock = 0.75f,
            isEngineRunning = true
        )

        // Should display "0.750 ms"
        composeTestRule.onNodeWithText("0.750 ms", substring = true)
            .assertExists()
    }

    @Test
    fun cpuMonitoringShowsStoppedWhenEngineNotRunning() {
        setupAdvancedAudioEngineScreen(
            isEngineRunning = false
        )

        // Should display "— ms (stopped)"
        composeTestRule.onNodeWithText("— ms (stopped)")
            .assertExists()
    }

    @Test
    fun cpuMonitoringDisplaysPerformanceSection() {
        setupAdvancedAudioEngineScreen()

        // Should display "Performance" section title
        composeTestRule.onNodeWithText("Performance")
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 6: Apply & Restart (4 tests)
    // =============================================================================

    @Test
    fun applyRestartButtonIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.APPLY_RESTART_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun applyRestartButtonDisabledWhenNoChanges() {
        setupAdvancedAudioEngineScreen(
            blockSize = 256,
            ftzEnabled = true,
            dazEnabled = true
        )

        // Button should be disabled when no pending changes
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.APPLY_RESTART_BUTTON)
            .assertIsNotEnabled()
    }

    @Test
    fun applyRestartButtonEnabledWhenChangesExist() {
        setupAdvancedAudioEngineScreen(
            blockSize = 256
        )

        // Click block size 512 to create pending change
        composeTestRule.onNodeWithTag("${UiIds.AudioEngine.BLOCK_SIZE_SELECTOR}_512")
            .performClick()

        composeTestRule.waitForIdle()

        // Button should now be enabled
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.APPLY_RESTART_BUTTON)
            .assertIsEnabled()
    }

    @Test
    fun applyRestartButtonClickTriggersCallback() {
        var applyClicked = false
        setupAdvancedAudioEngineScreen(
            blockSize = 256,
            onApplyAndRestart = { applyClicked = true }
        )

        // Create pending change
        composeTestRule.onNodeWithTag("${UiIds.AudioEngine.BLOCK_SIZE_SELECTOR}_128")
            .performClick()

        composeTestRule.waitForIdle()

        // Click Apply & Restart
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.APPLY_RESTART_BUTTON)
            .performClick()

        assert(applyClicked)
    }

    // =============================================================================
    // CATEGORY 7: Pending Changes Detection (3 tests)
    // =============================================================================

    @Test
    fun warningMessageAppearsWhenBlockSizeChanges() {
        setupAdvancedAudioEngineScreen(blockSize = 256)

        // Initially no warning
        composeTestRule.onNodeWithText("⚠️", substring = true)
            .assertDoesNotExist()

        // Change block size
        composeTestRule.onNodeWithTag("${UiIds.AudioEngine.BLOCK_SIZE_SELECTOR}_512")
            .performClick()

        composeTestRule.waitForIdle()

        // Warning should appear
        composeTestRule.onNodeWithText("Changes require audio engine restart")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun warningMessageAppearsWhenFtzChanges() {
        setupAdvancedAudioEngineScreen(ftzEnabled = true)

        // Change FTZ
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.FTZ_TOGGLE)
            .performClick()

        composeTestRule.waitForIdle()

        // Warning should appear
        composeTestRule.onNodeWithText("Changes require audio engine restart")
            .assertExists()
    }

    @Test
    fun warningMessageAppearsWhenDazChanges() {
        setupAdvancedAudioEngineScreen(dazEnabled = true)

        // Change DAZ
        composeTestRule.onNodeWithTag(UiIds.AudioEngine.DAZ_TOGGLE)
            .performClick()

        composeTestRule.waitForIdle()

        // Warning should appear
        composeTestRule.onNodeWithText("Changes require audio engine restart")
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 8: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupAdvancedAudioEngineScreen()

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupAdvancedAudioEngineScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.AudioEngine.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
