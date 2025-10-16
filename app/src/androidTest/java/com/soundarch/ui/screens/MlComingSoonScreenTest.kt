package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.ml.MlComingSoonScreen
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for MlComingSoonScreen
 *
 * P0-1 Week 1 - UI Screen Tests (8 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Content Display (4 tests - icon, title, description, features list)
 * 3. Navigation (2 tests)
 *
 * Total: 8 tests
 *
 * Note: This is a placeholder screen with no functional controls,
 * so only presence/display tests are needed.
 */
class MlComingSoonScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup MlComingSoonScreen
     */
    private fun setupMlComingSoonScreen(
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            MlComingSoonScreen(
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupMlComingSoonScreen()

        composeTestRule.onNodeWithTag(UiIds.ML.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setupMlComingSoonScreen()

        composeTestRule.onNodeWithTag(UiIds.ML.TITLE)
            .assertExists()
            .assertIsDisplayed()
            .assertTextContains("Machine Learning")
    }

    // =============================================================================
    // CATEGORY 2: Content Display (4 tests)
    // =============================================================================

    @Test
    fun largeIconIsDisplayed() {
        setupMlComingSoonScreen()

        // Large 🤖 icon should be displayed
        composeTestRule.onNodeWithText("🤖")
            .assertExists()
    }

    @Test
    fun comingSoonBannerIsDisplayed() {
        setupMlComingSoonScreen()

        composeTestRule.onNodeWithText("COMING SOON")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun descriptionTextIsDisplayed() {
        setupMlComingSoonScreen()

        composeTestRule.onNodeWithTag(UiIds.ComingSoon.DESCRIPTION_TEXT)
            .assertExists()
            .assertTextContains("AI-powered audio enhancement features", substring = true)
    }

    @Test
    fun plannedFeaturesListIsDisplayed() {
        setupMlComingSoonScreen()

        // Verify "Planned Features:" header
        composeTestRule.onNodeWithText("Planned Features:")
            .assertExists()

        // Verify at least one planned feature is displayed
        composeTestRule.onNodeWithText("Voice Enhancement Models", substring = true)
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 3: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupMlComingSoonScreen()

        composeTestRule.onNodeWithTag(UiIds.ML.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupMlComingSoonScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.ML.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
