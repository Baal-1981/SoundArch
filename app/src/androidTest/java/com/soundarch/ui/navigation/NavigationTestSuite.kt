package com.soundarch.ui.navigation

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * 🧭 NAVIGATION TEST SUITE
 *
 * Comprehensive test suite for all navigation-related tests.
 *
 * **Purpose:**
 * - Run all navigation tests in a single execution
 * - Verify navigation flows, back button, and back stack management
 * - Ensure consistent navigation behavior across the app
 *
 * **Usage:**
 * ```bash
 * # Run all navigation tests
 * ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.soundarch.ui.navigation.NavigationTestSuite
 *
 * # Or run via Android Studio:
 * Right-click on this file → Run 'NavigationTestSuite'
 * ```
 *
 * **Test Coverage:**
 * - ✅ Basic navigation (forward/back)
 * - ✅ Multi-level navigation chains
 * - ✅ Back stack verification
 * - ✅ Edge cases and complex scenarios
 * - ✅ All routes verified
 * - ✅ State preservation
 *
 * **Test Classes:**
 * 1. **BasicNavigationTest** - Fundamental navigation flows
 *    - Home → Dynamics → Compressor
 *    - Home → Equalizer
 *    - Home → Audio Engine
 *    - Back button from each screen
 *    - Back stack verification
 *
 * 2. **AdvancedNavigationTest** - Complex scenarios
 *    - Equalizer → EQ Settings → Back
 *    - Dynamics menu to all DSP screens
 *    - Multiple back presses
 *    - Nested navigation paths
 *    - State preservation
 *
 * **CI Integration:**
 * This suite is designed to be included in CI/CD pipelines to catch
 * navigation regressions early.
 *
 * **Expected Duration:** ~3-5 minutes (depends on device/emulator speed)
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    BasicNavigationTest::class,
    AdvancedNavigationTest::class
)
class NavigationTestSuite {
    companion object {
        /**
         * Total number of navigation tests
         */
        const val TOTAL_TESTS = 22  // 11 basic + 11 advanced

        /**
         * Test categories
         */
        enum class TestCategory {
            BASIC_FORWARD,       // Forward navigation tests
            BASIC_BACK,          // Back button tests
            BACK_STACK,          // Back stack verification
            MULTI_LEVEL,         // Multi-level navigation
            EDGE_CASES,          // Edge cases and complex scenarios
            STATE_PRESERVATION   // State preservation tests
        }
    }
}
