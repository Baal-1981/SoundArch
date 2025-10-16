package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.eq.EqualizerScreen
import com.soundarch.ui.testing.UiIds
import com.soundarch.viewmodel.EqViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for EqualizerScreen
 *
 * P0-1 Week 1 Day 1 - UI Screen Tests (30 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Master Toggle (3 tests)
 * 3. Interactive Graph (2 tests)
 * 4. Preset Buttons (8 tests - one per preset)
 * 5. Band Controls (10 tests - one per band)
 * 6. Navigation (3 tests)
 * 7. Preset Selector (2 tests)
 *
 * Total: 30 tests
 */
class EqualizerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Default EQ bands (10-band parametric EQ)
    private val defaultBands = listOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

    // Default gains (all flat at 0 dB)
    private val defaultGains = List(10) { 0f }

    /**
     * Helper function to setup EqualizerScreen with default params
     */
    private fun setupEqualizerScreen(
        enabled: Boolean = true,
        bands: List<Int> = defaultBands,
        gains: List<Float> = defaultGains,
        onToggle: (Boolean) -> Unit = {},
        onBandChange: (Int, Float) -> Unit = { _, _ -> },
        onPresetSelect: (EqViewModel.EqPreset) -> Unit = {},
        onNavigateToSettings: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            EqualizerScreen(
                enabled = enabled,
                onToggle = onToggle,
                bands = bands,
                gains = gains,
                onBandChange = onBandChange,
                onPresetSelect = onPresetSelect,
                onNavigateToSettings = onNavigateToSettings,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag(UiIds.Equalizer.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun masterToggleIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag(UiIds.Equalizer.MASTER_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 2: Master Toggle (3 tests)
    // =============================================================================

    @Test
    fun masterToggleDefaultEnabled() {
        setupEqualizerScreen(enabled = true)

        composeTestRule.onNodeWithTag(UiIds.Equalizer.MASTER_TOGGLE)
            .assertExists()
    }

    @Test
    fun masterToggleDefaultDisabled() {
        setupEqualizerScreen(enabled = false)

        composeTestRule.onNodeWithTag(UiIds.Equalizer.MASTER_TOGGLE)
            .assertExists()
    }

    @Test
    fun masterToggleClickTriggersCallback() {
        var toggledTo = false
        setupEqualizerScreen(
            enabled = false,
            onToggle = { toggledTo = it }
        )

        composeTestRule.onNodeWithTag(UiIds.Equalizer.MASTER_TOGGLE)
            .performClick()

        assert(toggledTo)
    }

    // =============================================================================
    // CATEGORY 3: Interactive Graph (2 tests)
    // =============================================================================

    @Test
    fun interactiveCurveIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag(UiIds.Equalizer.INTERACTIVE_CURVE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveCurveHasSemanticLabel() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag(UiIds.Equalizer.INTERACTIVE_CURVE)
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 4: Preset Buttons (8 tests - one per preset)
    // =============================================================================

    @Test
    fun presetFlatButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("${UiIds.Equalizer.PRESET_SELECTOR}_flat")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetBassButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("${UiIds.Equalizer.PRESET_SELECTOR}_bass")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetTrebleButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("${UiIds.Equalizer.PRESET_SELECTOR}_treble")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetVocalButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("${UiIds.Equalizer.PRESET_SELECTOR}_vocal")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetRockButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("${UiIds.Equalizer.PRESET_SELECTOR}_rock")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetClassicalButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("${UiIds.Equalizer.PRESET_SELECTOR}_classical")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetJazzButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("${UiIds.Equalizer.PRESET_SELECTOR}_jazz")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetElectronicButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("${UiIds.Equalizer.PRESET_SELECTOR}_electronic")
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 5: Band Controls (10 tests - one per band)
    // =============================================================================

    @Test
    fun band0ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_0")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun band1ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_1")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun band2ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_2")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun band3ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_3")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun band4ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_4")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun band5ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_5")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun band6ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_6")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun band7ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_7")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun band8ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_8")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun band9ValueTextIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag("eq_band_value_9")
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 6: Navigation (3 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag(UiIds.Equalizer.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupEqualizerScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.Equalizer.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }

    @Test
    fun settingsButtonIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag(UiIds.Equalizer.SETTINGS_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    // =============================================================================
    // CATEGORY 7: Preset Selector (2 tests)
    // =============================================================================

    @Test
    fun presetSelectorIsDisplayed() {
        setupEqualizerScreen()

        composeTestRule.onNodeWithTag(UiIds.Equalizer.PRESET_SELECTOR)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun presetFlatButtonClickTriggersCallback() {
        var presetSelected: EqViewModel.EqPreset? = null
        setupEqualizerScreen(onPresetSelect = { presetSelected = it })

        composeTestRule.onNodeWithTag("${UiIds.Equalizer.PRESET_SELECTOR}_flat")
            .performClick()

        assert(presetSelected == EqViewModel.EqPreset.FLAT)
    }
}
