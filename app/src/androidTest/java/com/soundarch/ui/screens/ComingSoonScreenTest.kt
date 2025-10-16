package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.common.ComingSoonScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for ComingSoonScreen
 *
 * P0-1 Week 1 - UI Screen Tests (5 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (3 tests)
 * 2. Navigation (1 test)
 * 3. Custom Content (1 test)
 *
 * Total: 5 tests
 */
class ComingSoonScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screenIsDisplayed() {
        composeTestRule.setContent {
            ComingSoonScreen(
                title = "Feature Name",
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("🚧")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        composeTestRule.setContent {
            ComingSoonScreen(
                title = "Test Feature",
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Test Feature")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun defaultDescriptionIsDisplayed() {
        composeTestRule.setContent {
            ComingSoonScreen(
                title = "Feature",
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText(
            "This feature is coming soon. It will be fully implemented in a future update.",
            substring = true
        )
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun customDescriptionIsDisplayed() {
        val customDesc = "Custom description text"

        composeTestRule.setContent {
            ComingSoonScreen(
                title = "Feature",
                description = customDesc,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText(customDesc)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false

        composeTestRule.setContent {
            ComingSoonScreen(
                title = "Feature",
                onBack = { backClicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Navigate back")
            .performClick()

        assert(backClicked)
    }
}
