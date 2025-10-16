package com.soundarch.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.soundarch.ui.screens.advanced.eq.EqSettingsScreen
import com.soundarch.ui.testing.UiIds
import com.soundarch.viewmodel.EqViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive UI tests for EqSettingsScreen
 *
 * P0-1 Week 1 - UI Screen Tests (20 tests)
 *
 * Test Coverage:
 * 1. Screen Rendering (2 tests)
 * 2. Status Badges (3 tests)
 * 3. Pre-Calc Coefficients Setting (3 tests)
 * 4. Minimal Thermal Dither Setting (3 tests)
 * 5. Section Ordering Selector (7 tests - 4 options + descriptions)
 * 6. Navigation (2 tests)
 *
 * Total: 20 tests
 *
 * **SOLUTION**: EqViewModel does NOT require Hilt injection - it's a standard ViewModel
 * that can be instantiated directly. Tests use real EqViewModel instance.
 */
class EqSettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to setup EqSettingsScreen with real EqViewModel
     */
    private fun setupEqSettingsScreen(
        eqViewModel: EqViewModel = EqViewModel(),
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            EqSettingsScreen(
                eqViewModel = eqViewModel,
                onBack = onBack
            )
        }
    }

    // =============================================================================
    // CATEGORY 1: Screen Rendering (2 tests)
    // =============================================================================

    @Test
    fun screenIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.SCREEN)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.TITLE)
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("EQ Settings")
    }

    // =============================================================================
    // CATEGORY 2: Status Badges Row (3 tests)
    // =============================================================================

    @Test
    fun statusBadgesRowIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.STATUS_BADGES_ROW)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun statusBadgesShowPreCalcWhenEnabled() {
        val viewModel = EqViewModel()
        viewModel.setPreCalcCoefficients(true)
        setupEqSettingsScreen(eqViewModel = viewModel)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Pre-calc ✓")
            .assertExists()
    }

    @Test
    fun statusBadgesShowDitherWhenEnabled() {
        val viewModel = EqViewModel()
        viewModel.setMinimalThermalDither(true)
        setupEqSettingsScreen(eqViewModel = viewModel)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Dither ✓")
            .assertExists()
    }

    // =============================================================================
    // CATEGORY 3: Pre-Calc Coefficients Setting (3 tests)
    // =============================================================================

    @Test
    fun preCalcCoefficientsToggleIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.PRECALC_COEFFS_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun preCalcCoefficientsDescriptionIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.PRECALC_COEFFS_DESCRIPTION)
            .assertExists()
            .assertTextContains("Pre-calculate biquad filter coefficients", substring = true)
    }

    @Test
    fun preCalcCoefficientsToggleChangesState() {
        val viewModel = EqViewModel()
        viewModel.setPreCalcCoefficients(false)
        setupEqSettingsScreen(eqViewModel = viewModel)

        composeTestRule.waitForIdle()

        // Initially disabled
        composeTestRule.onNodeWithTag(UiIds.EqSettings.PRECALC_COEFFS_TOGGLE)
            .assertIsOff()

        // Click toggle
        composeTestRule.onNodeWithTag(UiIds.EqSettings.PRECALC_COEFFS_TOGGLE)
            .performClick()

        composeTestRule.waitForIdle()

        // Now enabled
        composeTestRule.onNodeWithTag(UiIds.EqSettings.PRECALC_COEFFS_TOGGLE)
            .assertIsOn()
    }

    // =============================================================================
    // CATEGORY 4: Minimal Thermal Dither Setting (3 tests)
    // =============================================================================

    @Test
    fun minimalDitherToggleIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.MINIMAL_DITHER_TOGGLE)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun minimalDitherDescriptionIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.MINIMAL_DITHER_DESCRIPTION)
            .assertExists()
            .assertTextContains("ultra-low-level noise", substring = true)
    }

    @Test
    fun minimalDitherToggleChangesState() {
        val viewModel = EqViewModel()
        viewModel.setMinimalThermalDither(false)
        setupEqSettingsScreen(eqViewModel = viewModel)

        composeTestRule.waitForIdle()

        // Initially disabled
        composeTestRule.onNodeWithTag(UiIds.EqSettings.MINIMAL_DITHER_TOGGLE)
            .assertIsOff()

        // Click toggle
        composeTestRule.onNodeWithTag(UiIds.EqSettings.MINIMAL_DITHER_TOGGLE)
            .performClick()

        composeTestRule.waitForIdle()

        // Now enabled
        composeTestRule.onNodeWithTag(UiIds.EqSettings.MINIMAL_DITHER_TOGGLE)
            .assertIsOn()
    }

    // =============================================================================
    // CATEGORY 5: Section Ordering Selector (7 tests)
    // =============================================================================

    @Test
    fun sectionOrderingSelectorIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.SECTION_ORDERING_SELECTOR)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun lowToHighOrderingOptionIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.SECTION_ORDERING_PRE_EQ)
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Low to High")
            .assertExists()
    }

    @Test
    fun highToLowOrderingOptionIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.SECTION_ORDERING_POST_EQ)
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("High to Low")
            .assertExists()
    }

    @Test
    fun gainAscendingOrderingOptionIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.SECTION_ORDERING_PARALLEL)
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Gain Ascending")
            .assertExists()
    }

    @Test
    fun gainDescendingOrderingOptionIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag("${UiIds.EqSettings.SECTION_ORDERING_SELECTOR}_gain_desc")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Gain Descending")
            .assertExists()
    }

    @Test
    fun lowToHighOrderingIsSelectedByDefault() {
        val viewModel = EqViewModel()
        setupEqSettingsScreen(eqViewModel = viewModel)

        composeTestRule.waitForIdle()

        // Default is LOW_TO_HIGH
        composeTestRule.onNodeWithTag(UiIds.EqSettings.SECTION_ORDERING_PRE_EQ)
            .assertIsSelected()
    }

    @Test
    fun sectionOrderingChangesOnSelection() {
        val viewModel = EqViewModel()
        viewModel.setSectionOrdering(EqViewModel.SectionOrdering.LOW_TO_HIGH)
        setupEqSettingsScreen(eqViewModel = viewModel)

        composeTestRule.waitForIdle()

        // Initially LOW_TO_HIGH
        composeTestRule.onNodeWithTag(UiIds.EqSettings.SECTION_ORDERING_PRE_EQ)
            .assertIsSelected()

        // Click HIGH_TO_LOW option
        composeTestRule.onNodeWithTag(UiIds.EqSettings.SECTION_ORDERING_POST_EQ)
            .performClick()

        composeTestRule.waitForIdle()

        // HIGH_TO_LOW should now be selected
        composeTestRule.onNodeWithTag(UiIds.EqSettings.SECTION_ORDERING_POST_EQ)
            .assertIsSelected()

        // LOW_TO_HIGH should no longer be selected
        composeTestRule.onNodeWithTag(UiIds.EqSettings.SECTION_ORDERING_PRE_EQ)
            .assertIsNotSelected()
    }

    // =============================================================================
    // CATEGORY 6: Navigation (2 tests)
    // =============================================================================

    @Test
    fun backButtonIsDisplayed() {
        setupEqSettingsScreen()

        composeTestRule.onNodeWithTag(UiIds.EqSettings.BACK_BUTTON)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButtonClickTriggersCallback() {
        var backClicked = false
        setupEqSettingsScreen(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(UiIds.EqSettings.BACK_BUTTON)
            .performClick()

        assert(backClicked)
    }
}
