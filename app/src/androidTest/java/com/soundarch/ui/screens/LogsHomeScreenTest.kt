package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.logs.LogsHomeScreen
import com.soundarch.ui.testing.UiIds
import com.soundarch.viewmodel.LogsViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for LogsHomeScreen
 *
 * P0-1 Week 1 - UI Screen Tests (14 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Golden Tests Section (4 tests)
 * 3. Benchmarks Section (3 tests)
 * 4. Action Buttons (3 tests)
 * 5. Navigation (2 tests)
 *
 * Total: 14 tests
 */
class LogsHomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup LogsHomeScreen with default params
     */
    private fun setupLogsHomeScreen(
        enabled: Boolean = true,
        onToggle: (Boolean) -> Unit = {},
        goldenTests: List<LogsViewModel.GoldenTest> = emptyList(),
        benchmarks: List<LogsViewModel.ModuleBenchmark> = emptyList(),
        onRunGoldenTests: () -> Unit = {},
        onRunBenchmarks: () -> Unit = {},
        onViewGoldenTests: () -> Unit = {},
        onViewBenchmarks: () -> Unit = {},
        onExportJson: () -> Unit = {},
        onExportZip: () -> Unit = {},
        onCopyToClipboard: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            LogsHomeScreen(
                enabled = enabled,
                onToggle = onToggle,
                goldenTests = goldenTests,
                benchmarks = benchmarks,
                onRunGoldenTests = onRunGoldenTests,
                onRunBenchmarks = onRunBenchmarks,
                onViewGoldenTests = onViewGoldenTests,
                onViewBenchmarks = onViewBenchmarks,
                onExportJson = onExportJson,
                onExportZip = onExportZip,
                onCopyToClipboard = onCopyToClipboard,
                onBack = onBack
            )
        }
    }

    // Helper to create mock golden tests
    private fun createMockGoldenTests(): List<LogsViewModel.GoldenTest> {
        return listOf(
            LogsViewModel.GoldenTest(
                id = "test1",
                name = "Test 1",
                category = LogsViewModel.TestCategory.NOISE_ONLY,
                noiseCancellingEnabled = true,
                noiseCancellingParams = null,
                status = LogsViewModel.TestStatus.PASSED,
                cpuMs = 2.5,
                snrDb = 10.5,
                thresholdSnrDb = 10.0,
                description = "Test noise cancellation",
                timestamp = System.currentTimeMillis()
            ),
            LogsViewModel.GoldenTest(
                id = "test2",
                name = "Test 2",
                category = LogsViewModel.TestCategory.SPEECH_ONLY,
                noiseCancellingEnabled = false,
                noiseCancellingParams = null,
                status = LogsViewModel.TestStatus.FAILED,
                cpuMs = 2.0,
                snrDb = 12.0,
                thresholdSnrDb = 15.0,
                description = "Test speech processing",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Helper to create mock benchmarks
    private fun createMockBenchmarks(): List<LogsViewModel.ModuleBenchmark> {
        return listOf(
            LogsViewModel.ModuleBenchmark(
                moduleName = "Equalizer",
                enabled = true,
                cpuMs = 2.5,
                throughputMbps = 100.0,
                memoryKb = 512.0,
                params = emptyMap()
            ),
            LogsViewModel.ModuleBenchmark(
                moduleName = "Compressor",
                enabled = true,
                cpuMs = 1.8,
                throughputMbps = 150.0,
                memoryKb = 256.0,
                params = emptyMap()
            )
        )
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun toggleHeaderIsDisplayed() {
        setupLogsHomeScreen()

        composeTestRule.onNodeWithText("📊 Logs & Tests")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun descriptionIsDisplayed() {
        setupLogsHomeScreen()

        composeTestRule.onNodeWithText("Centralized QA & performance testing")
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Golden Tests Section (4 tests)
    // =============================================================================

    @Test
    fun goldenTestsSectionIsDisplayed() {
        setupLogsHomeScreen(goldenTests = createMockGoldenTests())

        composeTestRule.onNodeWithText("Golden Tests")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun goldenTestsShowCorrectCounts() {
        val tests = createMockGoldenTests()
        setupLogsHomeScreen(goldenTests = tests)

        // Total count should be 2
        composeTestRule.onNodeWithText("2", substring = true)
            .assertExists()
    }

    @Test
    fun goldenTestsShowPassedAndFailedCounts() {
        val tests = createMockGoldenTests()
        setupLogsHomeScreen(goldenTests = tests)

        // Should show "Passed" and "Failed" labels
        composeTestRule.onNodeWithText("Passed")
            .assertExists()

        composeTestRule.onNodeWithText("Failed")
            .assertExists()
    }

    @Test
    fun runGoldenTestsButtonIsDisplayed() {
        setupLogsHomeScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.RUN_GOLDENS_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 3: Benchmarks Section (3 tests)
    // =============================================================================

    @Test
    fun benchmarksSectionIsDisplayed() {
        setupLogsHomeScreen(benchmarks = createMockBenchmarks())

        composeTestRule.onNodeWithText("Per-Module Benchmarks")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun benchmarksShowModuleCount() {
        val benchmarks = createMockBenchmarks()
        setupLogsHomeScreen(benchmarks = benchmarks)

        // Should show "Modules" label
        composeTestRule.onNodeWithText("Modules")
            .assertExists()
    }

    @Test
    fun runBenchmarksButtonIsDisplayed() {
        setupLogsHomeScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.RUN_BENCH_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 4: Action Buttons (3 tests)
    // =============================================================================

    @Test
    fun runGoldenTestsButtonClickTriggersCallback() {
        var runGoldenTestsClicked = false
        setupLogsHomeScreen(onRunGoldenTests = { runGoldenTestsClicked = true })

        composeTestRule.onNodeWithTag(UiIds.LogsTests.RUN_GOLDENS_BUTTON)
            .performClick()

        assert(runGoldenTestsClicked)
    }

    @Test
    fun runBenchmarksButtonClickTriggersCallback() {
        var runBenchmarksClicked = false
        setupLogsHomeScreen(onRunBenchmarks = { runBenchmarksClicked = true })

        composeTestRule.onNodeWithTag(UiIds.LogsTests.RUN_BENCH_BUTTON)
            .performClick()

        assert(runBenchmarksClicked)
    }

    @Test
    fun goldenTestsCardClickTriggersNavigation() {
        var viewGoldenTestsClicked = false
        setupLogsHomeScreen(
            goldenTests = createMockGoldenTests(),
            onViewGoldenTests = { viewGoldenTestsClicked = true }
        )

        // Click on Golden Tests card (anywhere with "Golden Tests" text)
        composeTestRule.onNodeWithText("Golden Tests")
            .performClick()

        assert(viewGoldenTestsClicked)
    }

    // =============================================================================
    // CATEGORY 5: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupLogsHomeScreen()

        composeTestRule.onNodeWithTag(UiIds.LogsTests.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupLogsHomeScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.LogsTests.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
