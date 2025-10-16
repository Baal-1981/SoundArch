package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.diagnostics.LatencyDetailScreen
import com.soundarch.ui.testing.UiIds
import com.soundarch.viewmodel.LatencyViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for LatencyDetailScreen
 *
 * P0-1 Week 1 - UI Screen Tests (18 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Latency Status Display (3 tests - status badge, value, icon)
 * 3. Statistics Display (6 tests - Current, EMA, Mean, Min, Max, Jitter)
 * 4. EMA Slider (2 tests)
 * 5. Reset Button (2 tests)
 * 6. XRun Monitoring (2 tests)
 * 7. Navigation (1 test)
 *
 * Total: 18 tests
 */
class LatencyDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup LatencyDetailScreen with default params
     */
    private fun setupLatencyDetailScreen(
        enabled: Boolean = true,
        onToggle: (Boolean) -> Unit = {},
        totalLatencyMs: Double = 8.5,
        inputLatencyMs: Double = 3.2,
        outputLatencyMs: Double = 3.1,
        emaLatencyMs: Double = 8.0,
        minLatencyMs: Double = 6.5,
        maxLatencyMs: Double = 12.0,
        meanLatencyMs: Double = 8.2,
        jitterMs: Double = 1.5,
        xRunCount: Int = 0,
        xRunRate: Double = 0.0,
        bufferSize: Int = 192,
        sampleRate: Int = 48000,
        emaAlpha: Float = 0.1f,
        latencyHistory: List<Double> = emptyList(),
        latencyStatus: LatencyViewModel.LatencyStatus = LatencyViewModel.LatencyStatus.EXCELLENT,
        xRunSeverity: LatencyViewModel.XRunSeverity = LatencyViewModel.XRunSeverity.NONE,
        bufferLatencyMs: Double = 4.0,
        headroomMs: Double = 2.0,
        onEmaAlphaChange: (Float) -> Unit = {},
        onResetStatistics: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            LatencyDetailScreen(
                enabled = enabled,
                onToggle = onToggle,
                totalLatencyMs = totalLatencyMs,
                inputLatencyMs = inputLatencyMs,
                outputLatencyMs = outputLatencyMs,
                emaLatencyMs = emaLatencyMs,
                minLatencyMs = minLatencyMs,
                maxLatencyMs = maxLatencyMs,
                meanLatencyMs = meanLatencyMs,
                jitterMs = jitterMs,
                xRunCount = xRunCount,
                xRunRate = xRunRate,
                bufferSize = bufferSize,
                sampleRate = sampleRate,
                emaAlpha = emaAlpha,
                latencyHistory = latencyHistory,
                latencyStatus = latencyStatus,
                xRunSeverity = xRunSeverity,
                bufferLatencyMs = bufferLatencyMs,
                headroomMs = headroomMs,
                onEmaAlphaChange = onEmaAlphaChange,
                onResetStatistics = onResetStatistics,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupLatencyDetailScreen()

        composeTestRule.onNodeWithText("📊 Latency Detail")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun descriptionIsDisplayed() {
        setupLatencyDetailScreen()

        composeTestRule.onNodeWithText(
            "Deep latency analysis with EMA, min/max tracking, and XRun monitoring",
            substring = true
        ).assertExists()
    }

    // =============================================================================
    // CATEGORY 2: Latency Status Display (3 tests)
    // =============================================================================

    @Test
    fun latencyStatusBadgeDisplaysCorrectStatus() {
        setupLatencyDetailScreen(
            latencyStatus = LatencyViewModel.LatencyStatus.EXCELLENT
        )

        composeTestRule.onNodeWithText("EXCELLENT")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun latencyStatusBadgeDisplaysTotalLatency() {
        setupLatencyDetailScreen(totalLatencyMs = 8.5)

        composeTestRule.onNodeWithText("8.50 ms", substring = true)
            .assertExists()
    }

    @Test
    fun latencyStatusBadgeShowsIconForStatus() {
        setupLatencyDetailScreen(
            latencyStatus = LatencyViewModel.LatencyStatus.EXCELLENT
        )

        // Excellent status should show green icon 🟢
        composeTestRule.onNodeWithText("🟢")
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 3: Statistics Display (6 tests)
    // =============================================================================

    @Test
    fun currentLatencyStatisticDisplayed() {
        setupLatencyDetailScreen(totalLatencyMs = 8.5)

        composeTestRule.onNodeWithText("Current")
            .assertExists()

        composeTestRule.onNodeWithText("8.50 ms", substring = true)
            .assertExists()
    }

    @Test
    fun emaLatencyStatisticDisplayed() {
        setupLatencyDetailScreen(emaLatencyMs = 8.0)

        composeTestRule.onNodeWithText("EMA")
            .assertExists()

        composeTestRule.onNodeWithText("8.00 ms", substring = true)
            .assertExists()
    }

    @Test
    fun meanLatencyStatisticDisplayed() {
        setupLatencyDetailScreen(meanLatencyMs = 8.2)

        composeTestRule.onNodeWithText("Mean")
            .assertExists()

        composeTestRule.onNodeWithText("8.20 ms", substring = true)
            .assertExists()
    }

    @Test
    fun minLatencyStatisticDisplayed() {
        setupLatencyDetailScreen(minLatencyMs = 6.5)

        composeTestRule.onNodeWithText("Min")
            .assertExists()

        composeTestRule.onNodeWithText("6.50 ms", substring = true)
            .assertExists()
    }

    @Test
    fun maxLatencyStatisticDisplayed() {
        setupLatencyDetailScreen(maxLatencyMs = 12.0)

        composeTestRule.onNodeWithText("Max")
            .assertExists()

        composeTestRule.onNodeWithText("12.00 ms", substring = true)
            .assertExists()
    }

    @Test
    fun jitterStatisticDisplayed() {
        setupLatencyDetailScreen(jitterMs = 1.5)

        composeTestRule.onNodeWithText("Jitter (StdDev)")
            .assertExists()

        composeTestRule.onNodeWithText("1.50 ms", substring = true)
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 4: EMA Slider (2 tests)
    // =============================================================================

    @Test
    fun emaSliderIsDisplayed() {
        setupLatencyDetailScreen()

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.METER_UPDATE_RATE_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun emaAlphaValueDisplayed() {
        setupLatencyDetailScreen(emaAlpha = 0.15f)

        composeTestRule.onNodeWithText("0.15", substring = true)
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 5: Reset Button (2 tests)
    // =============================================================================

    @Test
    fun resetButtonIsDisplayed() {
        setupLatencyDetailScreen()

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.RESET_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun resetButtonClickTriggersCallback() {
        var resetClicked = false
        setupLatencyDetailScreen(onResetStatistics = { resetClicked = true })

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.RESET_BUTTON)
            .performClick()

        assert(resetClicked)
    }

    // =============================================================================
    // CATEGORY 6: XRun Monitoring (2 tests)
    // =============================================================================

    @Test
    fun xRunMonitoringDisplaysCount() {
        setupLatencyDetailScreen(xRunCount = 5)

        composeTestRule.onNodeWithText("XRun Monitoring")
            .assertExists()

        composeTestRule.onNodeWithText("5", substring = true)
            .assertExists()
    }

    @Test
    fun xRunMonitoringDisplaysSeverity() {
        setupLatencyDetailScreen(
            xRunSeverity = LatencyViewModel.XRunSeverity.NONE
        )

        composeTestRule.onNodeWithText("NONE")
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 7: Navigation (1 test)
    // =============================================================================

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupLatencyDetailScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.Diagnostics.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
