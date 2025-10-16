package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.noisecancel.NoiseCancellingScreen
import com.soundarch.ui.testing.UiIds
import com.soundarch.viewmodel.NoiseCancellingViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for NoiseCancellingScreen
 *
 * P0-1 Week 1 - UI Screen Tests (25 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Master Toggle (2 tests)
 * 3. Effect Indicator (2 tests)
 * 4. Preset Chips (3 tests)
 * 5. Main Controls (6 tests - 2 per slider: Strength, Spectral Floor, Smoothing)
 * 6. Timing Controls (4 tests - 2 per slider: Attack, Release)
 * 7. Advanced Controls (2 tests - Residual Boost, Artifact Suppress)
 * 8. Backend Selector (2 tests)
 * 9. Navigation (2 tests)
 *
 * Total: 25 tests
 */
class NoiseCancellingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup NoiseCancellingScreen with default params
     */
    private fun setupNoiseCancellingScreen(
        enabled: Boolean = true,
        strength: Float = 50f,
        spectralFloor: Float = -40f,
        smoothing: Float = 0.5f,
        noiseAttack: Float = 100f,
        noiseRelease: Float = 200f,
        residualBoost: Float = 0f,
        artifactSuppress: Boolean = true,
        backend: NoiseCancellingViewModel.Backend = NoiseCancellingViewModel.Backend.CLASSICAL,
        mlAvailable: Boolean = false,
        cpuPerBlock: Double = 0.5,
        snrEstimate: Double = 15.0,
        noiseReduction: Double = 10.0,
        effectStatus: NoiseCancellingViewModel.EffectStatus = NoiseCancellingViewModel.EffectStatus.GOOD,
        cpuStatus: NoiseCancellingViewModel.CpuStatus = NoiseCancellingViewModel.CpuStatus.LOW,
        onToggle: (Boolean) -> Unit = {},
        onStrengthChange: (Float) -> Unit = {},
        onSpectralFloorChange: (Float) -> Unit = {},
        onSmoothingChange: (Float) -> Unit = {},
        onNoiseAttackChange: (Float) -> Unit = {},
        onNoiseReleaseChange: (Float) -> Unit = {},
        onResidualBoostChange: (Float) -> Unit = {},
        onArtifactSuppressToggle: (Boolean) -> Unit = {},
        onBackendChange: (NoiseCancellingViewModel.Backend) -> Unit = {},
        onPresetSelect: (NoiseCancellingViewModel.Preset) -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            NoiseCancellingScreen(
                enabled = enabled,
                onToggle = onToggle,
                strength = strength,
                spectralFloor = spectralFloor,
                smoothing = smoothing,
                noiseAttack = noiseAttack,
                noiseRelease = noiseRelease,
                residualBoost = residualBoost,
                artifactSuppress = artifactSuppress,
                backend = backend,
                mlAvailable = mlAvailable,
                cpuPerBlock = cpuPerBlock,
                snrEstimate = snrEstimate,
                noiseReduction = noiseReduction,
                effectStatus = effectStatus,
                cpuStatus = cpuStatus,
                onStrengthChange = onStrengthChange,
                onSpectralFloorChange = onSpectralFloorChange,
                onSmoothingChange = onSmoothingChange,
                onNoiseAttackChange = onNoiseAttackChange,
                onNoiseReleaseChange = onNoiseReleaseChange,
                onResidualBoostChange = onResidualBoostChange,
                onArtifactSuppressToggle = onArtifactSuppressToggle,
                onBackendChange = onBackendChange,
                onPresetSelect = onPresetSelect,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun masterToggleIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.MASTER_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Master Toggle (2 tests)
    // =============================================================================

    @Test
    fun masterToggleDefaultEnabled() {
        setupNoiseCancellingScreen(enabled = true)

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.MASTER_TOGGLE)
            .assertExists()
    }

    @Test
    fun masterToggleClickTriggersCallback() {
        var toggledTo = false
        setupNoiseCancellingScreen(
            enabled = false,
            onToggle = { toggledTo = it }
        )

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.MASTER_TOGGLE)
            .performClick()

        assert(toggledTo)
    }

    // =============================================================================
    // CATEGORY 3: Effect Indicator (2 tests)
    // =============================================================================

    @Test
    fun effectMeterIsDisplayedWhenEnabled() {
        setupNoiseCancellingScreen(enabled = true)

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.NC_EFFECT_METER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun effectValueTextIsDisplayed() {
        setupNoiseCancellingScreen(enabled = true)

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.NC_EFFECT_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 4: Preset Chips (3 tests)
    // =============================================================================

    @Test
    fun presetVoiceChipIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag("nc_preset_voice")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetOutdoorChipIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag("nc_preset_outdoor")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetOfficeChipIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag("nc_preset_office")
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 5: Main Controls (6 tests)
    // =============================================================================

    @Test
    fun strengthSliderIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.STRENGTH_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun strengthValueTextIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.STRENGTH_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun spectralFloorSliderIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.SPECTRAL_FLOOR_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun spectralFloorValueTextIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.SPECTRAL_FLOOR_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun smoothingSliderIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.SMOOTHING_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun smoothingValueTextIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.SMOOTHING_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 6: Timing Controls (4 tests)
    // =============================================================================

    @Test
    fun attackSliderIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.ATTACK_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun attackValueTextIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.ATTACK_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun releaseSliderIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.RELEASE_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun releaseValueTextIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.RELEASE_VALUE_TEXT)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 7: Advanced Controls (2 tests)
    // =============================================================================

    @Test
    fun residualBoostSliderIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.RESIDUAL_BOOST_SLIDER)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun artifactSuppressToggleIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.ARTIFACT_SUPPRESS_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 8: Backend Selector (2 tests)
    // =============================================================================

    @Test
    fun backendClassicalButtonIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.BACKEND_CLASSICAL)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backendMlButtonIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.BACKEND_ML)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 9: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupNoiseCancellingScreen()

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupNoiseCancellingScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.NoiseCancelling.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
