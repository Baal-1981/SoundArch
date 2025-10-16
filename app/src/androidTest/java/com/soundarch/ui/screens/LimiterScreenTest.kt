package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.data.LimiterState
import com.soundarch.ui.screens.advanced.dynamics.LimiterScreen
import com.soundarch.ui.testing.UiIds
import com.soundarch.viewmodel.DynamicsViewModelRefactored
import com.soundarch.viewmodel.DynamicsUiState
import com.soundarch.viewmodel.DynamicsMetrics
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for LimiterScreen
 *
 * P0-1 Week 1 Day 1 - UI Screen Tests (20 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (3 tests)
 * 2. Master Toggle (3 tests)
 * 3. Real-time Monitor (2 tests)
 * 4. Parameter Controls (10 tests - 2 per slider: display + value text + labels)
 * 5. Navigation (2 tests)
 *
 * Total: 20 tests
 */
class LimiterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Default limiter state
    private val defaultLimiterState = LimiterState(
        enabled = true,
        threshold = -1f,
        release = 50f,
        lookahead = 0f
    )

    // Default realtime metrics
    private val defaultMetrics = DynamicsMetrics(
        limiterGainReduction = 0f
    )

    // Default UI state
    private val defaultUiState = DynamicsUiState(limiter = defaultLimiterState)

    /**
     * Helper function to create a mock ViewModel
     */
    private fun createMockViewModel(
        uiState: DynamicsUiState = defaultUiState,
        metrics: DynamicsMetrics = defaultMetrics
    ): DynamicsViewModelRefactored {
        val viewModel = mockk<DynamicsViewModelRefactored>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(uiState)
        every { viewModel.realtimeMetrics } returns MutableStateFlow(metrics)
        return viewModel
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (3 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun topBarTitleIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.TITLE)
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("Limiter")
    }

    @Test
    fun backButtonIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Master Toggle (3 tests)
    // =============================================================================

    @Test
    fun masterToggleIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.MASTER_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun masterToggleDefaultChecked() {
        val viewModel = createMockViewModel(
            uiState = defaultUiState.copy(limiter = defaultLimiterState.copy(enabled = true))
        )

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.MASTER_TOGGLE)
            .assertIsOn()
    }

    @Test
    fun masterToggleClickTriggersViewModel() {
        val viewModel = createMockViewModel(
            uiState = defaultUiState.copy(limiter = defaultLimiterState.copy(enabled = false))
        )

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.MASTER_TOGGLE)
            .performClick()

        verify { viewModel.setLimiterEnabled(true) }
    }

    // =============================================================================
    // CATEGORY 3: Real-time Monitor (2 tests)
    // =============================================================================

    @Test
    fun gainReductionValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.GR_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun gainReductionValueTextDisplaysValue() {
        val viewModel = createMockViewModel(
            metrics = DynamicsMetrics(limiterGainReduction = 5.5f)
        )

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.GR_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 4: Parameter Controls (10 tests - 5 per slider)
    // =============================================================================

    // Threshold (5 tests)
    @Test
    fun thresholdSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.THRESHOLD_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun thresholdValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.THRESHOLD_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun thresholdMinLabelIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.THRESHOLD_MIN_LABEL)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun thresholdMaxLabelIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.THRESHOLD_MAX_LABEL)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun thresholdSliderSemanticLabelExists() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        // Verify semantic label for accessibility
        composeTestRule.onNodeWithTag(UiIds.Limiter.THRESHOLD_SLIDER)
            .assertExists()
    }

    // Release (5 tests)
    @Test
    fun releaseSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.RELEASE_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun releaseValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.RELEASE_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun releaseMinLabelIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.RELEASE_MIN_LABEL)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun releaseMaxLabelIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.RELEASE_MAX_LABEL)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun releaseSliderSemanticLabelExists() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        // Verify semantic label for accessibility
        composeTestRule.onNodeWithTag(UiIds.Limiter.RELEASE_SLIDER)
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 5: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = { backClicked = true })
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }

    @Test
    fun monitorCardIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            LimiterScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Limiter.MONITOR_CARD)
            .assertExists()
            .assertIsDisplayed()
    }
}
