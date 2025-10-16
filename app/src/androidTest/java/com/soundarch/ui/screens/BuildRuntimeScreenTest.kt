package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.build.BuildRuntimeScreen
import com.soundarch.ui.testing.UiIds
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for BuildRuntimeScreen
 *
 * P0-1 Week 1 - UI Screen Tests (16 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Build Configuration (3 tests - badge, debug/release)
 * 3. Build Information (4 tests - version, build date, ABI, NEON, JNI)
 * 4. Runtime Information (3 tests - device, Android, CPU)
 * 5. Build Flags (2 tests)
 * 6. Navigation (2 tests)
 *
 * Total: 16 tests
 */
class BuildRuntimeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup BuildRuntimeScreen with default params
     */
    private fun setupBuildRuntimeScreen(
        isDebugBuild: Boolean = true,
        appVersion: String = "2.0.0",
        buildDate: String = "2025-01-15",
        splitAbi: String = "arm64-v8a",
        hasNeon: Boolean = true,
        jniRulesOk: Boolean = true,
        deviceModel: String = "Pixel 7",
        androidVersion: String = "14",
        apiLevel: Int = 34,
        cpuAbi: String = "arm64-v8a",
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            BuildRuntimeScreen(
                isDebugBuild = isDebugBuild,
                appVersion = appVersion,
                buildDate = buildDate,
                splitAbi = splitAbi,
                hasNeon = hasNeon,
                jniRulesOk = jniRulesOk,
                deviceModel = deviceModel,
                androidVersion = androidVersion,
                apiLevel = apiLevel,
                cpuAbi = cpuAbi,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupBuildRuntimeScreen()

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setupBuildRuntimeScreen()

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.TITLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Build Configuration (3 tests)
    // =============================================================================

    @Test
    fun statusBadgeShowsDebugWhenDebugBuild() {
        setupBuildRuntimeScreen(isDebugBuild = true)

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.STATUS_BADGES_ROW)
            .assertExists()
            .assertTextContains("DEBUG")
    }

    @Test
    fun statusBadgeShowsReleaseWhenReleaseBuild() {
        setupBuildRuntimeScreen(isDebugBuild = false)

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.STATUS_BADGES_ROW)
            .assertExists()
            .assertTextContains("RELEASE")
    }

    @Test
    fun buildConfigurationHeaderIsDisplayed() {
        setupBuildRuntimeScreen()

        composeTestRule.onNodeWithText("Build Configuration")
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 3: Build Information (4 tests)
    // =============================================================================

    @Test
    fun versionTextIsDisplayed() {
        setupBuildRuntimeScreen(appVersion = "2.0.0")

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.VERSION_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun buildDateTextIsDisplayed() {
        setupBuildRuntimeScreen(buildDate = "2025-01-15")

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.BUILD_DATE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun splitAbiTextIsDisplayed() {
        setupBuildRuntimeScreen(splitAbi = "arm64-v8a")

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.SPLIT_ABI_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun neonTextIsDisplayed() {
        setupBuildRuntimeScreen(hasNeon = true)

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.NEON_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 4: Runtime Information (3 tests)
    // =============================================================================

    @Test
    fun deviceInfoTextIsDisplayed() {
        setupBuildRuntimeScreen(deviceModel = "Pixel 7")

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.DEVICE_INFO_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun runtimeSectionIsDisplayed() {
        setupBuildRuntimeScreen()

        composeTestRule.onNodeWithText("📱 RUNTIME INFORMATION")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun androidVersionAndApiLevelDisplayed() {
        setupBuildRuntimeScreen(
            androidVersion = "14",
            apiLevel = 34
        )

        // Should display "14 (API 34)" somewhere
        composeTestRule.onNodeWithText("14 (API 34)", substring = true)
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 5: Build Flags (2 tests)
    // =============================================================================

    @Test
    fun buildFlagsSectionIsDisplayed() {
        setupBuildRuntimeScreen()

        composeTestRule.onNodeWithText("ℹ️ BUILD FLAGS")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun neonFlagDisplayedWhenNeonEnabled() {
        setupBuildRuntimeScreen(hasNeon = true)

        composeTestRule.onNodeWithText("-mfpu=neon", substring = true)
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 6: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupBuildRuntimeScreen()

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupBuildRuntimeScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.BuildRuntime.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
