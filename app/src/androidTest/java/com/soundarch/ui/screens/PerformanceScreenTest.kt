package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.perf.PerformanceScreen
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for PerformanceScreen
 *
 * P0-1 Week 1 - UI Screen Tests (20 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Profile Cards (3 tests - one per profile)
 * 3. Profile Selection (3 tests - one per profile click)
 * 4. Profile State Display (3 tests)
 * 5. Action Buttons (4 tests - Apply & Restart visibility/clicks)
 * 6. Pending Changes Detection (3 tests)
 * 7. Navigation (2 tests)
 *
 * Total: 20 tests
 */
class PerformanceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup PerformanceScreen with default params
     */
    private fun setupPerformanceScreen(
        selectedProfile: String = "Balanced",
        onProfileSelect: (String) -> Unit = {},
        onApply: () -> Unit = {},
        onRestart: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            PerformanceScreen(
                selectedProfile = selectedProfile,
                onProfileSelect = onProfileSelect,
                onApply = onApply,
                onRestart = onRestart,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupPerformanceScreen()

        composeTestRule.onNodeWithTag(UiIds.Performance.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setupPerformanceScreen()

        composeTestRule.onNodeWithTag(UiIds.Performance.TITLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Profile Cards (3 tests)
    // =============================================================================

    @Test
    fun balancedProfileCardIsDisplayed() {
        setupPerformanceScreen()

        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_BALANCED)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun fastProfileCardIsDisplayed() {
        setupPerformanceScreen()

        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_FAST)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun ultraProfileCardIsDisplayed() {
        setupPerformanceScreen()

        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_ULTRA)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 3: Profile Selection (3 tests)
    // =============================================================================

    @Test
    fun balancedProfileClickTriggersSelection() {
        var selectedProfile = ""
        setupPerformanceScreen(
            selectedProfile = "Fast",
            onProfileSelect = { selectedProfile = it },
            onApply = { }
        )

        // Click Balanced profile
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_BALANCED)
            .performClick()

        // Wait for pending changes to appear
        composeTestRule.waitForIdle()

        // Click Apply button to confirm selection
        composeTestRule.onNodeWithTag(UiIds.Performance.APPLY_BUTTON)
            .performClick()

        assert(selectedProfile == "Balanced")
    }

    @Test
    fun fastProfileClickTriggersSelection() {
        var selectedProfile = ""
        setupPerformanceScreen(
            selectedProfile = "Balanced",
            onProfileSelect = { selectedProfile = it },
            onApply = { }
        )

        // Click Fast profile
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_FAST)
            .performClick()

        // Wait for pending changes
        composeTestRule.waitForIdle()

        // Apply changes
        composeTestRule.onNodeWithTag(UiIds.Performance.APPLY_BUTTON)
            .performClick()

        assert(selectedProfile == "Fast")
    }

    @Test
    fun ultraProfileClickTriggersSelection() {
        var selectedProfile = ""
        setupPerformanceScreen(
            selectedProfile = "Balanced",
            onProfileSelect = { selectedProfile = it },
            onApply = { }
        )

        // Click Ultra profile
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_ULTRA)
            .performClick()

        // Wait for pending changes
        composeTestRule.waitForIdle()

        // Apply changes
        composeTestRule.onNodeWithTag(UiIds.Performance.APPLY_BUTTON)
            .performClick()

        assert(selectedProfile == "Ultra")
    }

    // =============================================================================
    // CATEGORY 4: Profile State Display (3 tests)
    // =============================================================================

    @Test
    fun statusBadgesRowDisplaysCurrentProfile() {
        setupPerformanceScreen(selectedProfile = "Balanced")

        composeTestRule.onNodeWithTag(UiIds.Performance.STATUS_BADGES_ROW)
            .assertExists()
            .assertIsDisplayed()

        // Verify "Current: Balanced" text is displayed
        composeTestRule.onNodeWithText("Current: Balanced", substring = true)
            .assertExists()
    }

    @Test
    fun currentProfileShownForFastProfile() {
        setupPerformanceScreen(selectedProfile = "Fast")

        composeTestRule.onNodeWithText("Current: Fast", substring = true)
            .assertExists()
    }

    @Test
    fun currentProfileShownForUltraProfile() {
        setupPerformanceScreen(selectedProfile = "Ultra")

        composeTestRule.onNodeWithText("Current: Ultra", substring = true)
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 5: Action Buttons (4 tests)
    // =============================================================================

    @Test
    fun applyButtonAppearsWhenProfileChanges() {
        setupPerformanceScreen(selectedProfile = "Balanced")

        // Initially, Apply button should not be visible
        composeTestRule.onNodeWithTag(UiIds.Performance.APPLY_BUTTON)
            .assertDoesNotExist()

        // Click Fast profile to trigger pending changes
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_FAST)
            .performClick()

        // Wait for UI to update
        composeTestRule.waitForIdle()

        // Apply button should now be visible
        composeTestRule.onNodeWithTag(UiIds.Performance.APPLY_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun restartButtonAppearsWhenProfileChanges() {
        setupPerformanceScreen(selectedProfile = "Balanced")

        // Initially, Restart button should not be visible
        composeTestRule.onNodeWithTag(UiIds.Performance.RESTART_BUTTON)
            .assertDoesNotExist()

        // Click Ultra profile
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_ULTRA)
            .performClick()

        // Wait for UI
        composeTestRule.waitForIdle()

        // Restart button should now be visible
        composeTestRule.onNodeWithTag(UiIds.Performance.RESTART_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun applyButtonClickTriggersCallback() {
        var applyClicked = false
        setupPerformanceScreen(
            selectedProfile = "Balanced",
            onApply = { applyClicked = true }
        )

        // Select different profile
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_FAST)
            .performClick()

        composeTestRule.waitForIdle()

        // Click Apply
        composeTestRule.onNodeWithTag(UiIds.Performance.APPLY_BUTTON)
            .performClick()

        assert(applyClicked)
    }

    @Test
    fun restartButtonClickTriggersCallback() {
        var restartClicked = false
        setupPerformanceScreen(
            selectedProfile = "Balanced",
            onRestart = { restartClicked = true }
        )

        // Select different profile
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_ULTRA)
            .performClick()

        composeTestRule.waitForIdle()

        // Click Restart
        composeTestRule.onNodeWithTag(UiIds.Performance.RESTART_BUTTON)
            .performClick()

        assert(restartClicked)
    }

    // =============================================================================
    // CATEGORY 6: Pending Changes Detection (3 tests)
    // =============================================================================

    @Test
    fun pendingBadgeAppearsWhenProfileChanges() {
        setupPerformanceScreen(selectedProfile = "Balanced")

        // Initially, no "PENDING" badge
        composeTestRule.onNodeWithText("PENDING")
            .assertDoesNotExist()

        // Click Fast profile
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_FAST)
            .performClick()

        composeTestRule.waitForIdle()

        // "PENDING" badge should appear
        composeTestRule.onNodeWithText("PENDING")
            .assertExists()
    }

    @Test
    fun warningMessageAppearsWhenProfileChanges() {
        setupPerformanceScreen(selectedProfile = "Balanced")

        // Click Ultra profile
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_ULTRA)
            .performClick()

        composeTestRule.waitForIdle()

        // Warning message should appear
        composeTestRule.onNodeWithText("⚠️ Changes Pending")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun noPendingChangesWhenSameProfileSelected() {
        setupPerformanceScreen(selectedProfile = "Balanced")

        // Click same profile (Balanced)
        composeTestRule.onNodeWithTag(UiIds.Performance.PROFILE_BALANCED)
            .performClick()

        composeTestRule.waitForIdle()

        // No "PENDING" badge or Apply button should appear
        composeTestRule.onNodeWithText("PENDING")
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(UiIds.Performance.APPLY_BUTTON)
            .assertDoesNotExist()
    }

    // =============================================================================
    // CATEGORY 7: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupPerformanceScreen()

        composeTestRule.onNodeWithTag(UiIds.Performance.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupPerformanceScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.Performance.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
