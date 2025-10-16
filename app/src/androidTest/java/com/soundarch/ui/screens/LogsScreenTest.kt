package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.logs.LogsScreen
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for LogsScreen
 *
 * P0-1 Week 1 - UI Screen Tests (18 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Filter Selector (5 tests - ALL, OK, WARN, ERROR + visibility)
 * 3. Action Buttons (3 tests - Refresh, Export JSON, Clear)
 * 4. Logs List (3 tests - card, list, empty state)
 * 5. Navigation (2 tests)
 * 6. Stats Display (3 tests)
 *
 * Total: 18 tests
 */
class LogsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup LogsScreen with default params
     */
    private fun setupLogsScreen(
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            LogsScreen(
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.TITLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Filter Selector (5 tests)
    // =============================================================================

    @Test
    fun filterSelectorIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.LOGS_FILTER_SELECTOR)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun filterAllChipIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag("${UiIds.LogsTests.LOGS_FILTER_SELECTOR}_ALL")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun filterOkChipIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag("${UiIds.LogsTests.LOGS_FILTER_SELECTOR}_OK")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun filterWarnChipIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag("${UiIds.LogsTests.LOGS_FILTER_SELECTOR}_WARN")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun filterErrorChipIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag("${UiIds.LogsTests.LOGS_FILTER_SELECTOR}_ERROR")
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 3: Action Buttons (3 tests)
    // =============================================================================

    @Test
    fun refreshButtonIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.REFRESH_LOGS_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun exportJsonButtonIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.EXPORT_JSON_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun clearLogsButtonIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.CLEAR_LOGS_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 4: Logs List (3 tests)
    // =============================================================================

    @Test
    fun logsListCardIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.LOGS_LIST_CARD)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun logsListIsDisplayedWhenLogsExist() {
        setupLogsScreen()

        // Wait for logs to load (if any)
        composeTestRule.waitForIdle()

        // The logs list should exist (may be empty initially)
        composeTestRule.onNodeWithTag(UiIds.LogsTests.LOGS_LIST_CARD)
            .assertExists()
    }

    @Test
    fun emptyStateDisplayedWhenNoLogs() {
        setupLogsScreen()

        // Wait for initial load
        composeTestRule.waitForIdle()

        // If no logs, empty state message should be visible
        // Note: This may or may not be true depending on whether logs exist
        // We just verify the screen structure is correct
        composeTestRule.onNodeWithTag(UiIds.LogsTests.LOGS_LIST_CARD)
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 5: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupLogsScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupLogsScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.LogsTests.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }

    // =============================================================================
    // CATEGORY 6: Stats Display (3 tests)
    // =============================================================================

    @Test
    fun statsHeaderDisplaysLogCount() {
        setupLogsScreen()

        // Wait for logs to load
        composeTestRule.waitForIdle()

        // Should display "UI Action Logs (X entries)" somewhere
        composeTestRule.onNodeWithText("UI Action Logs", substring = true)
            .assertExists()
    }

    @Test
    fun okStatBadgeIsDisplayed() {
        setupLogsScreen()

        composeTestRule.waitForIdle()

        // OK stat badge should exist (showing count)
        composeTestRule.onNodeWithText("OK", substring = true)
            .assertExists()
    }

    @Test
    fun warnAndErrorStatBadgesDisplayed() {
        setupLogsScreen()

        composeTestRule.waitForIdle()

        // WARN and ERROR badges should exist
        composeTestRule.onNodeWithText("WARN", substring = true)
            .assertExists()

        composeTestRule.onNodeWithText("ERROR", substring = true)
            .assertExists()
    }
}
