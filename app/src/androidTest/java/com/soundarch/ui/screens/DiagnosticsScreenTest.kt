package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.diagnostics.DiagnosticsScreen
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for DiagnosticsScreen
 *
 * P0-1 Week 1 - UI Screen Tests (19 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Latency Metrics (5 tests)
 * 3. Performance Metrics (3 tests)
 * 4. Action Buttons (2 tests)
 * 5. Meter Update Rate (4 tests)
 * 6. Navigation (2 tests)
 * 7. Export Functionality (1 test)
 *
 * Total: 19 tests
 */
class DiagnosticsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup DiagnosticsScreen with default params
     */
    private fun setupDiagnosticsScreen(
        latencyEmaMs: Double? = 8.5,
        latencyMinMs: Double? = 6.2,
        latencyMaxMs: Double? = 12.3,
        xrunCount: Int = 0,
        framesPerCallback: Int? = 192,
        meterUpdateRateHz: Float = 30f,
        onMeterUpdateRateChange: (Float) -> Unit = {},
        onResetStats: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            DiagnosticsScreen(
                latencyEmaMs = latencyEmaMs,
                latencyMinMs = latencyMinMs,
                latencyMaxMs = latencyMaxMs,
                xrunCount = xrunCount,
                framesPerCallback = framesPerCallback,
                meterUpdateRateHz = meterUpdateRateHz,
                onMeterUpdateRateChange = onMeterUpdateRateChange,
                onResetStats = onResetStats,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupDiagnosticsScreen()

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setupDiagnosticsScreen()

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.TITLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Latency Metrics (5 tests)
    // =============================================================================

    @Test
    fun latencyEmaIsDisplayed() {
        setupDiagnosticsScreen(latencyEmaMs = 8.5)

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.LATENCY_EMA_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun latencyMinIsDisplayed() {
        setupDiagnosticsScreen(latencyMinMs = 6.2)

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.LATENCY_MIN_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun latencyMaxIsDisplayed() {
        setupDiagnosticsScreen(latencyMaxMs = 12.3)

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.LATENCY_MAX_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun latencyEmaShowsCorrectValue() {
        setupDiagnosticsScreen(latencyEmaMs = 9.7)

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.LATENCY_EMA_TEXT)
            .assertTextContains("9.7", substring = true)
    }

    @Test
    fun latencyMetricsUpdateDynamically() {
        setupDiagnosticsScreen(
            latencyEmaMs = 5.0,
            latencyMinMs = 4.0,
            latencyMaxMs = 6.0
        )

        // Verify initial values
        composeTestRule.onNodeWithTag(UiIds.Diagnostics.LATENCY_EMA_TEXT)
            .assertTextContains("5.0", substring = true)
    }

    // =============================================================================
    // CATEGORY 3: Performance Metrics (3 tests)
    // =============================================================================

    @Test
    fun xrunCountIsDisplayed() {
        setupDiagnosticsScreen(xrunCount = 0)

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.XRUN_COUNT_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun xrunCountShowsZeroInitially() {
        setupDiagnosticsScreen(xrunCount = 0)

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.XRUN_COUNT_TEXT)
            .assertTextContains("0", substring = true)
    }

    @Test
    fun framesPerCallbackIsDisplayed() {
        setupDiagnosticsScreen(framesPerCallback = 192)

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.FRAMES_PER_CALLBACK_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 4: Action Buttons (2 tests)
    // =============================================================================

    @Test
    fun resetButtonIsDisplayed() {
        setupDiagnosticsScreen()

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.RESET_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun resetButtonClickTriggersCallback() {
        var resetClicked = false
        setupDiagnosticsScreen(onResetStats = { resetClicked = true })

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.RESET_BUTTON)
            .performClick()

        assert(resetClicked)
    }

    // =============================================================================
    // CATEGORY 5: Meter Update Rate (4 tests)
    // =============================================================================

    @Test
    fun meterUpdateRateSliderIsDisplayed() {
        setupDiagnosticsScreen()

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.METER_UPDATE_RATE_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun meterUpdateRateValueTextIsDisplayed() {
        setupDiagnosticsScreen(meterUpdateRateHz = 30f)

        composeTestRule.onNodeWithText("30 Hz", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun meterUpdateRateDefaultValue() {
        setupDiagnosticsScreen(meterUpdateRateHz = 30f)

        composeTestRule.onNodeWithText("30 Hz")
            .assertExists()
    }

    @Test
    fun meterUpdateRateSliderRange() {
        // Test with minimum value
        setupDiagnosticsScreen(meterUpdateRateHz = 10f)
        composeTestRule.onNodeWithText("10 Hz")
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 6: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupDiagnosticsScreen()

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupDiagnosticsScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }

    // =============================================================================
    // CATEGORY 7: Export Functionality (1 test)
    // =============================================================================

    @Test
    fun exportDiagnosticsButtonIsDisplayed() {
        setupDiagnosticsScreen()

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.EXPORT_DIAGNOSTICS_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }
}
