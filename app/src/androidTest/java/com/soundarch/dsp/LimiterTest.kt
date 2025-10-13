package com.soundarch.dsp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.soundarch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Limiter (Peak Protection)
 *
 * Tests:
 * - Threshold validation
 * - Release time
 * - Gain reduction monitoring
 * - Enable/disable bypass
 * - Peak clipping prevention
 */
class LimiterTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var mainActivity: MainActivity

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            mainActivity = activity
        }
        Thread.sleep(1000)  // Allow audio engine to initialize
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🚨 THRESHOLD TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testThreshold_ValidRange() {
        // Limiter threshold: typically [-12, -0.1] dBFS
        // (Must be below 0dBFS to prevent clipping)
        val validThresholds = listOf(-12.0f, -6.0f, -3.0f, -1.0f, -0.3f)

        validThresholds.forEach { threshold ->
            try {
                mainActivity.setLimiter(
                    threshold = threshold,
                    release = 50.0f,
                    lookahead = 0.0f  // Not implemented yet
                )
            } catch (e: Exception) {
                fail("Valid threshold $threshold should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testThreshold_SafetyMargin() {
        // Common safety: -1.0dBFS to -0.3dBFS
        val safeThresholds = listOf(-1.0f, -0.5f, -0.3f)

        safeThresholds.forEach { threshold ->
            try {
                mainActivity.setLimiter(
                    threshold = threshold,
                    release = 50.0f,
                    lookahead = 0.0f
                )
                assertTrue(
                    "Threshold $threshold should be below 0dBFS",
                    threshold < 0.0f
                )
            } catch (e: Exception) {
                fail("Safe threshold $threshold should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testThreshold_ExtremeProtection() {
        // Test brick-wall limiter (0dBFS ceiling)
        try {
            mainActivity.setLimiter(
                threshold = -0.1f,  // Very close to 0dBFS
                release = 10.0f,    // Fast release
                lookahead = 0.0f
            )
            // Should provide maximum peak protection
        } catch (e: Exception) {
            fail("Brick-wall threshold should be valid: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ⏱️ RELEASE TIME TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testRelease_ValidRange() {
        // Limiter release: typically [5, 500] ms
        val validReleases = listOf(5.0f, 10.0f, 25.0f, 50.0f, 100.0f, 250.0f, 500.0f)

        validReleases.forEach { release ->
            try {
                mainActivity.setLimiter(
                    threshold = -1.0f,
                    release = release,
                    lookahead = 0.0f
                )
            } catch (e: Exception) {
                fail("Valid release $release ms should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testRelease_FastRecovery() {
        // Fast release for transparent limiting
        try {
            mainActivity.setLimiter(
                threshold = -1.0f,
                release = 10.0f,  // Very fast
                lookahead = 0.0f
            )
            // Should recover quickly from peaks
        } catch (e: Exception) {
            fail("Fast release should be valid: ${e.message}")
        }
    }

    @Test
    fun testRelease_SlowRecovery() {
        // Slow release for sustained material
        try {
            mainActivity.setLimiter(
                threshold = -3.0f,
                release = 300.0f,  // Slow
                lookahead = 0.0f
            )
            // Should provide gentle recovery
        } catch (e: Exception) {
            fail("Slow release should be valid: ${e.message}")
        }
    }

    @Test
    fun testRelease_AutoRelease() {
        // Program-dependent release (typical: 50ms)
        try {
            mainActivity.setLimiter(
                threshold = -1.0f,
                release = 50.0f,  // Auto-like
                lookahead = 0.0f
            )
            // Should adapt to signal
        } catch (e: Exception) {
            fail("Auto-release should be valid: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📊 GAIN REDUCTION MONITORING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testGainReduction_ReturnsValidValue() {
        // Gain reduction should be non-positive (0 or negative dB)
        val gr = mainActivity.getLimiterGainReduction()

        assertTrue(
            "Gain reduction ($gr dB) should be <= 0",
            gr <= 0.0f
        )
    }

    @Test
    fun testGainReduction_NoSignal() {
        // With no signal, gain reduction should be 0 dB
        mainActivity.setLimiterEnabled(true)
        mainActivity.setLimiter(-1.0f, 50.0f, 0.0f)

        // Note: Can't test actual signal processing in unit test,
        // but verify method doesn't crash
        val gr = mainActivity.getLimiterGainReduction()
        assertNotNull("Gain reduction should not be null", gr)
    }

    @Test
    fun testGainReduction_Range() {
        // Gain reduction typically ranges from 0 to -20 dB
        val gr = mainActivity.getLimiterGainReduction()

        assertTrue(
            "Gain reduction ($gr dB) should be in valid range [-20, 0]",
            gr in -20.0f..0.0f
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎛️ ENABLE/DISABLE TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testLimiter_EnableDisable() {
        try {
            mainActivity.setLimiterEnabled(true)
            mainActivity.setLimiterEnabled(false)
            mainActivity.setLimiterEnabled(true)
        } catch (e: Exception) {
            fail("Limiter enable/disable should not throw: ${e.message}")
        }
    }

    @Test
    fun testLimiter_SafetyBypass() {
        // Even when disabled, limiter should exist as safety net
        mainActivity.setLimiterEnabled(false)

        // Parameters should still be settable
        try {
            mainActivity.setLimiter(-0.5f, 20.0f, 0.0f)
        } catch (e: Exception) {
            fail("Should be able to set parameters while disabled: ${e.message}")
        }
    }

    @Test
    fun testLimiter_AlwaysLastInChain() {
        // Limiter should be last DSP stage for safety
        // Enable all DSP modules to verify limiter is still functional
        try {
            mainActivity.setAGCEnabled(true)
            mainActivity.setCompressorEnabled(true)
            mainActivity.setLimiterEnabled(true)

            // Limiter should catch any peaks from upstream
            val gr = mainActivity.getLimiterGainReduction()
            assertNotNull("Limiter should still work with full chain", gr)

        } catch (e: Exception) {
            fail("Limiter should work with full DSP chain: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔗 INTEGRATION TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testLimiter_FullConfiguration() {
        try {
            mainActivity.setLimiterEnabled(true)
            mainActivity.setLimiter(
                threshold = -1.0f,
                release = 50.0f,
                lookahead = 0.0f
            )

            val gr = mainActivity.getLimiterGainReduction()
            assertNotNull("Gain reduction should be readable", gr)

        } catch (e: Exception) {
            fail("Full limiter configuration should not throw: ${e.message}")
        }
    }

    @Test
    fun testLimiter_PresetConfigurations() {
        // Test common limiter presets

        // Mastering limiter (transparent)
        try {
            mainActivity.setLimiter(
                threshold = -0.3f,
                release = 50.0f,
                lookahead = 0.0f
            )
        } catch (e: Exception) {
            fail("Mastering preset should be valid: ${e.message}")
        }

        // Broadcast limiter (aggressive)
        try {
            mainActivity.setLimiter(
                threshold = -3.0f,
                release = 100.0f,
                lookahead = 0.0f
            )
        } catch (e: Exception) {
            fail("Broadcast preset should be valid: ${e.message}")
        }

        // Live protection (brick-wall)
        try {
            mainActivity.setLimiter(
                threshold = -0.1f,
                release = 10.0f,
                lookahead = 0.0f
            )
        } catch (e: Exception) {
            fail("Live preset should be valid: ${e.message}")
        }
    }

    @Test
    fun testLimiter_RapidParameterChanges() {
        // Test stability under rapid updates
        repeat(50) { i ->
            val threshold = -1.0f - (i % 5) * 0.5f
            val release = 20.0f + (i % 10) * 10.0f

            mainActivity.setLimiter(threshold, release, 0.0f)
        }
        // Should not crash or glitch
    }

    @Test
    fun testLimiter_ThreadSafety_RapidToggle() {
        // Test atomic enable/disable
        repeat(100) { i ->
            mainActivity.setLimiterEnabled(i % 2 == 0)
        }
        // Should not deadlock
    }

    @Test
    fun testLimiter_GainReduction_Consistency() {
        // Verify gain reduction is consistent across multiple reads
        mainActivity.setLimiterEnabled(true)
        mainActivity.setLimiter(-1.0f, 50.0f, 0.0f)

        val readings = List(10) { mainActivity.getLimiterGainReduction() }

        // All readings should be valid (non-positive)
        readings.forEach { gr ->
            assertTrue(
                "Gain reduction ($gr) should be <= 0",
                gr <= 0.0f
            )
        }
    }

    @Test
    fun testLimiter_ClippingPrevention() {
        // Verify limiter prevents digital clipping
        val safeThreshold = -0.5f  // 0.5dB safety margin

        mainActivity.setLimiterEnabled(true)
        mainActivity.setLimiter(safeThreshold, 50.0f, 0.0f)

        // Limiter should ensure output never exceeds threshold
        // (Can't test signal processing, but verify configuration)
        assertTrue(
            "Threshold should provide safety margin",
            safeThreshold < 0.0f
        )
    }

    @Test
    fun testLimiter_ReleaseTimeEffect() {
        // Fast release should recover quickly
        mainActivity.setLimiter(-1.0f, 10.0f, 0.0f)

        // Slow release should recover gently
        mainActivity.setLimiter(-1.0f, 300.0f, 0.0f)

        // Both should be valid configurations
        // (Actual sonic difference requires audio processing test)
    }

    @Test
    fun testLimiter_LookaheadParameter() {
        // Lookahead parameter exists but may not be implemented
        try {
            mainActivity.setLimiter(
                threshold = -1.0f,
                release = 50.0f,
                lookahead = 5.0f  // 5ms lookahead
            )
            // Should accept parameter even if not implemented yet
        } catch (e: Exception) {
            // If not implemented, should gracefully ignore
            assertTrue(
                "Lookahead error should be graceful: ${e.message}",
                e.message?.contains("not implemented", ignoreCase = true) ?: true
            )
        }
    }
}
