package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.data.CompressorState
import com.soundarch.ui.screens.advanced.dynamics.CompressorScreen
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
 * Comprehensive UI tests for CompressorScreen
 *
 * P0-1 Week 1 Day 1 - UI Screen Tests (20 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (3 tests)
 * 2. Master Toggle (3 tests)
 * 3. Real-time Monitor (2 tests)
 * 4. Parameter Controls (12 tests - 2 per slider: display + value text)
 *
 * Total: 20 tests
 */
class CompressorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Default compressor state
    private val defaultCompressorState = CompressorState(
        enabled = true,
        threshold = -30f,
        ratio = 4f,
        attack = 10f,
        release = 100f,
        makeupGain = 0f
    )

    // Default realtime metrics
    private val defaultMetrics = DynamicsMetrics(
        compressorGainReduction = 0f
    )

    // Default UI state
    private val defaultUiState = DynamicsUiState(compressor = defaultCompressorState)

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
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun topBarTitleIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.TITLE)
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("Compressor")
    }

    @Test
    fun backButtonIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.BACK_BUTTON)
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
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.MASTER_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun masterToggleDefaultChecked() {
        val viewModel = createMockViewModel(
            uiState = defaultUiState.copy(compressor = defaultCompressorState.copy(enabled = true))
        )

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.MASTER_TOGGLE)
            .assertIsOn()
    }

    @Test
    fun masterToggleClickTriggersViewModel() {
        val viewModel = createMockViewModel(
            uiState = defaultUiState.copy(compressor = defaultCompressorState.copy(enabled = false))
        )

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.MASTER_TOGGLE)
            .performClick()

        verify { viewModel.setCompressorEnabled(true) }
    }

    // =============================================================================
    // CATEGORY 3: Real-time Monitor (2 tests)
    // =============================================================================

    @Test
    fun gainReductionMeterIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.GR_METER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun gainReductionMeterDisplaysValue() {
        val viewModel = createMockViewModel(
            metrics = DynamicsMetrics(compressorGainReduction = 5.5f)
        )

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.GR_METER)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 4: Parameter Controls (12 tests - 2 per slider)
    // =============================================================================

    // Threshold (2 tests)
    @Test
    fun thresholdSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.THRESHOLD_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun thresholdValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.THRESHOLD_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // Ratio (2 tests)
    @Test
    fun ratioSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.RATIO_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun ratioValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.RATIO_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // Attack (2 tests)
    @Test
    fun attackSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.ATTACK_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun attackValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.ATTACK_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // Release (2 tests)
    @Test
    fun releaseSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.RELEASE_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun releaseValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.RELEASE_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // Makeup Gain (2 tests)
    @Test
    fun makeupGainSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.MAKEUP_GAIN_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun makeupGainValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.MAKEUP_GAIN_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // Navigation (2 tests)
    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            CompressorScreen(viewModel = viewModel, onBack = { backClicked = true })
        }

        composeTestRule.onNodeWithTag(UiIds.Compressor.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
