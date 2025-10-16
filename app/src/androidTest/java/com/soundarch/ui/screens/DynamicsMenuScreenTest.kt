package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.dynamics.DynamicsMenuScreen
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for DynamicsMenuScreen
 *
 * P0-1 Week 1 - UI Screen Tests (10 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Module Cards (3 tests - one per card)
 * 3. Navigation (5 tests - 3 card clicks + 2 buttons)
 *
 * Total: 10 tests
 */
class DynamicsMenuScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup DynamicsMenuScreen with default params
     */
    private fun setupDynamicsMenuScreen(
        onNavigateToCompressor: () -> Unit = {},
        onNavigateToAGC: () -> Unit = {},
        onNavigateToLimiter: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            DynamicsMenuScreen(
                onNavigateToCompressor = onNavigateToCompressor,
                onNavigateToAGC = onNavigateToAGC,
                onNavigateToLimiter = onNavigateToLimiter,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupDynamicsMenuScreen()

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setupDynamicsMenuScreen()

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.TITLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Module Cards (3 tests)
    // =============================================================================

    @Test
    fun compressorCardIsDisplayed() {
        setupDynamicsMenuScreen()

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.COMPRESSOR_CARD)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun agcCardIsDisplayed() {
        setupDynamicsMenuScreen()

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.AGC_CARD)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun limiterCardIsDisplayed() {
        setupDynamicsMenuScreen()

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.LIMITER_CARD)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 3: Navigation (5 tests)
    // =============================================================================

    @Test
    fun compressorCardClickTriggersNavigation() {
        var navigatedToCompressor = false
        setupDynamicsMenuScreen(
            onNavigateToCompressor = { navigatedToCompressor = true }
        )

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.COMPRESSOR_CARD)
            .performClick()

        assert(navigatedToCompressor)
    }

    @Test
    fun agcCardClickTriggersNavigation() {
        var navigatedToAGC = false
        setupDynamicsMenuScreen(
            onNavigateToAGC = { navigatedToAGC = true }
        )

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.AGC_CARD)
            .performClick()

        assert(navigatedToAGC)
    }

    @Test
    fun limiterCardClickTriggersNavigation() {
        var navigatedToLimiter = false
        setupDynamicsMenuScreen(
            onNavigateToLimiter = { navigatedToLimiter = true }
        )

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.LIMITER_CARD)
            .performClick()

        assert(navigatedToLimiter)
    }

    @Test
    fun backButtonIsDisplayed() {
        setupDynamicsMenuScreen()

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupDynamicsMenuScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
