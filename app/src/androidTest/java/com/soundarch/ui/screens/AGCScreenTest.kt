package com.soundarch.ui.screens

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.dynamics.AGCScreen
import com.soundarch.ui.testing.UiIds
import com.soundarch.viewmodel.DynamicsViewModelRefactored
import com.soundarch.viewmodel.DynamicsUiState
import com.soundarch.viewmodel.DynamicsMetrics
import com.soundarch.data.AgcState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for AGCScreen
 *
 * P0-1 Week 1 Day 1 - UI Screen Tests (20 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (3 tests)
 * 2. Master Toggle (3 tests)
 * 3. Real-time Monitor (2 tests)
 * 4. Target Level Control (2 tests)
 * 5. Gain Limits Controls (4 tests)
 * 6. Timing Controls (4 tests)
 * 7. Noise Threshold Control (1 test)
 * 8. Window Size Control (1 test)
 * 9. Reset Button (1 test)
 * 10. Navigation (1 test)
 *
 * Total: 22 tests
 */
class AGCScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Default AGC state
    private val defaultAgcState = AgcState(
        enabled = true,
        targetLevel = -20f,
        maxGain = 25f,
        minGain = -10f,
        attackTime = 0.1f,
        releaseTime = 0.5f,
        noiseThreshold = -55f,
        windowSize = 0.1f
    )

    // Default realtime metrics
    private val defaultMetrics = DynamicsMetrics(
        agcCurrentGain = 5.0f,
        agcCurrentLevel = -25.0f
    )

    // Default UI state
    private val defaultUiState = DynamicsUiState(agc = defaultAgcState)

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
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun topBarTitleIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.TITLE)
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("AGC Settings")
    }

    @Test
    fun backButtonIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.BACK_BUTTON)
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
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.MASTER_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun masterToggleDefaultChecked() {
        val viewModel = createMockViewModel(
            uiState = defaultUiState.copy(agc = defaultAgcState.copy(enabled = true))
        )

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.MASTER_TOGGLE)
            .assertIsOn()
    }

    @Test
    fun masterToggleClickTriggersViewModel() {
        val viewModel = createMockViewModel(
            uiState = defaultUiState.copy(agc = defaultAgcState.copy(enabled = false))
        )

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.MASTER_TOGGLE)
            .performClick()

        verify { viewModel.setAgcEnabled(true) }
    }

    // =============================================================================
    // CATEGORY 3: Real-time Monitor (2 tests)
    // =============================================================================

    @Test
    fun monitorCardIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.MONITOR_CARD)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun monitorDisplaysCurrentGainAndLevel() {
        val viewModel = createMockViewModel(
            metrics = DynamicsMetrics(agcCurrentGain = 8.5f, agcCurrentLevel = -30.2f)
        )

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.CURRENT_GAIN_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(UiIds.AGC.INPUT_LEVEL_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 4: Target Level Control (2 tests)
    // =============================================================================

    @Test
    fun targetLevelSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.TARGET_LEVEL_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun targetLevelValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.TARGET_LEVEL_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 5: Gain Limits Controls (4 tests)
    // =============================================================================

    @Test
    fun maxGainSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.MAX_GAIN_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun maxGainValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.MAX_GAIN_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun minGainSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.MIN_GAIN_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun minGainValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.MIN_GAIN_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 6: Timing Controls (4 tests)
    // =============================================================================

    @Test
    fun attackTimeSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.ATTACK_TIME_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun attackTimeValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.ATTACK_TIME_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun releaseTimeSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.RELEASE_TIME_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun releaseTimeValueTextIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.RELEASE_TIME_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 7: Noise Threshold Control (1 test)
    // =============================================================================

    @Test
    fun noiseThresholdSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.NOISE_THRESHOLD_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 8: Window Size Control (1 test)
    // =============================================================================

    @Test
    fun windowSizeSliderIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.WINDOW_SIZE_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 9: Reset Button (1 test)
    // =============================================================================

    @Test
    fun resetButtonIsDisplayed() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.RESET_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 10: Navigation (1 test)
    // =============================================================================

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            AGCScreen(viewModel = viewModel, onBack = { backClicked = true })
        }

        composeTestRule.onNodeWithTag(UiIds.AGC.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
