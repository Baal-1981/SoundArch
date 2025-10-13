package com.soundarch.dsp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.soundarch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Compressor (Dynamic Range Compression)
 *
 * Tests:
 * - Threshold, ratio, attack/release parameters
 * - Makeup gain
 * - Enable/disable bypass
 * - Parameter validation
 */
class CompressorTest {

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
    // 🎚️ THRESHOLD TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testThreshold_ValidRange() {
        // Compressor threshold: typically [-60, 0] dBFS
        val validThresholds = listOf(-60.0f, -40.0f, -20.0f, -10.0f, -1.0f)

        validThresholds.forEach { threshold ->
            try {
                mainActivity.setCompressor(
                    threshold = threshold,
                    ratio = 4.0f,
                    attack = 5.0f,
                    release = 50.0f,
                    makeupGain = 0.0f
                )
            } catch (e: Exception) {
                fail("Valid threshold $threshold should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testThreshold_Extreme() {
        // Test extreme values
        val extremeThresholds = listOf(-100.0f, 10.0f)

        extremeThresholds.forEach { threshold ->
            try {
                mainActivity.setCompressor(
                    threshold = threshold,
                    ratio = 4.0f,
                    attack = 5.0f,
                    release = 50.0f,
                    makeupGain = 0.0f
                )
                // Should either clamp or throw with meaningful message
            } catch (e: Exception) {
                assertTrue(
                    "Exception should indicate invalid threshold: ${e.message}",
                    e.message?.contains("threshold", ignoreCase = true) ?: false
                )
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📊 RATIO TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testRatio_ValidRange() {
        // Compressor ratio: typically [1, 20]:1
        val validRatios = listOf(1.0f, 2.0f, 4.0f, 8.0f, 10.0f, 20.0f)

        validRatios.forEach { ratio ->
            try {
                mainActivity.setCompressor(
                    threshold = -20.0f,
                    ratio = ratio,
                    attack = 5.0f,
                    release = 50.0f,
                    makeupGain = 0.0f
                )
            } catch (e: Exception) {
                fail("Valid ratio $ratio should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testRatio_LimiterMode() {
        // Ratio >= 10:1 approaches limiter behavior
        val limiterRatio = 20.0f

        try {
            mainActivity.setCompressor(
                threshold = -3.0f,
                ratio = limiterRatio,
                attack = 1.0f,
                release = 50.0f,
                makeupGain = 0.0f
            )
            // Should work as a hard limiter
        } catch (e: Exception) {
            fail("Limiter-mode ratio should be valid: ${e.message}")
        }
    }

    @Test
    fun testRatio_UnityGain() {
        // Ratio 1:1 = no compression (unity gain)
        try {
            mainActivity.setCompressor(
                threshold = -20.0f,
                ratio = 1.0f,
                attack = 5.0f,
                release = 50.0f,
                makeupGain = 0.0f
            )
            // Should effectively bypass compression
        } catch (e: Exception) {
            fail("Unity ratio (1:1) should be valid: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ⚡ ATTACK/RELEASE TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testAttack_ValidRange() {
        // Attack time: typically [0.1, 100] ms
        val validAttacks = listOf(0.1f, 1.0f, 5.0f, 10.0f, 50.0f, 100.0f)

        validAttacks.forEach { attack ->
            try {
                mainActivity.setCompressor(
                    threshold = -20.0f,
                    ratio = 4.0f,
                    attack = attack,
                    release = 50.0f,
                    makeupGain = 0.0f
                )
            } catch (e: Exception) {
                fail("Valid attack $attack ms should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testRelease_ValidRange() {
        // Release time: typically [10, 500] ms
        val validReleases = listOf(10.0f, 25.0f, 50.0f, 100.0f, 250.0f, 500.0f)

        validReleases.forEach { release ->
            try {
                mainActivity.setCompressor(
                    threshold = -20.0f,
                    ratio = 4.0f,
                    attack = 5.0f,
                    release = release,
                    makeupGain = 0.0f
                )
            } catch (e: Exception) {
                fail("Valid release $release ms should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testAttackRelease_FastTransients() {
        // Fast attack/release for transient control
        try {
            mainActivity.setCompressor(
                threshold = -15.0f,
                ratio = 6.0f,
                attack = 0.5f,  // Very fast
                release = 20.0f, // Short
                makeupGain = 0.0f
            )
            // Should handle fast transients
        } catch (e: Exception) {
            fail("Fast attack/release should be valid: ${e.message}")
        }
    }

    @Test
    fun testAttackRelease_SmoothCompression() {
        // Slower attack/release for smooth, musical compression
        try {
            mainActivity.setCompressor(
                threshold = -20.0f,
                ratio = 3.0f,
                attack = 10.0f,
                release = 150.0f,
                makeupGain = 0.0f
            )
            // Should provide smooth compression
        } catch (e: Exception) {
            fail("Slow attack/release should be valid: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔊 MAKEUP GAIN TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testMakeupGain_ValidRange() {
        // Makeup gain: typically [-12, +24] dB
        val validGains = listOf(-12.0f, -6.0f, 0.0f, 6.0f, 12.0f, 18.0f, 24.0f)

        validGains.forEach { gain ->
            try {
                mainActivity.setCompressor(
                    threshold = -20.0f,
                    ratio = 4.0f,
                    attack = 5.0f,
                    release = 50.0f,
                    makeupGain = gain
                )
            } catch (e: Exception) {
                fail("Valid makeup gain $gain dB should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testMakeupGain_AutoCompensation() {
        // Calculate approximate auto makeup gain
        // For 4:1 ratio at -20dB threshold, typical makeup is ~5-8dB
        val threshold = -20.0f
        val ratio = 4.0f
        val autoMakeup = 6.0f  // Approximate

        try {
            mainActivity.setCompressor(
                threshold = threshold,
                ratio = ratio,
                attack = 5.0f,
                release = 50.0f,
                makeupGain = autoMakeup
            )
            // Should compensate for gain reduction
        } catch (e: Exception) {
            fail("Auto makeup gain should be valid: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎛️ ENABLE/DISABLE TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testCompressor_EnableDisable() {
        try {
            mainActivity.setCompressorEnabled(true)
            mainActivity.setCompressorEnabled(false)
            mainActivity.setCompressorEnabled(true)
        } catch (e: Exception) {
            fail("Compressor enable/disable should not throw: ${e.message}")
        }
    }

    @Test
    fun testCompressor_DisabledBypass() {
        // When disabled, should have zero CPU cost
        mainActivity.setCompressorEnabled(false)

        // Parameters should still be settable even when disabled
        try {
            mainActivity.setCompressor(
                threshold = -15.0f,
                ratio = 5.0f,
                attack = 3.0f,
                release = 75.0f,
                makeupGain = 4.0f
            )
        } catch (e: Exception) {
            fail("Should be able to set parameters while disabled: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔗 INTEGRATION TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testCompressor_FullConfiguration() {
        try {
            mainActivity.setCompressorEnabled(true)
            mainActivity.setCompressor(
                threshold = -20.0f,
                ratio = 4.0f,
                attack = 5.0f,
                release = 50.0f,
                makeupGain = 6.0f
            )
        } catch (e: Exception) {
            fail("Full compressor configuration should not throw: ${e.message}")
        }
    }

    @Test
    fun testCompressor_PresetConfigurations() {
        // Test common compressor presets

        // Vocal compression
        try {
            mainActivity.setCompressor(
                threshold = -18.0f,
                ratio = 3.0f,
                attack = 5.0f,
                release = 100.0f,
                makeupGain = 5.0f
            )
        } catch (e: Exception) {
            fail("Vocal preset should be valid: ${e.message}")
        }

        // Drum bus compression
        try {
            mainActivity.setCompressor(
                threshold = -10.0f,
                ratio = 2.0f,
                attack = 10.0f,
                release = 150.0f,
                makeupGain = 3.0f
            )
        } catch (e: Exception) {
            fail("Drum bus preset should be valid: ${e.message}")
        }

        // Mastering compression
        try {
            mainActivity.setCompressor(
                threshold = -12.0f,
                ratio = 1.5f,
                attack = 30.0f,
                release = 300.0f,
                makeupGain = 2.0f
            )
        } catch (e: Exception) {
            fail("Mastering preset should be valid: ${e.message}")
        }
    }

    @Test
    fun testCompressor_RapidParameterChanges() {
        // Test stability under rapid parameter updates
        repeat(50) { i ->
            val threshold = -20.0f - (i % 10)
            val ratio = 2.0f + (i % 8)

            mainActivity.setCompressor(
                threshold = threshold,
                ratio = ratio,
                attack = 5.0f,
                release = 50.0f,
                makeupGain = 0.0f
            )
        }
        // Should not crash or produce glitches
    }

    @Test
    fun testCompressor_ParameterConsistency() {
        // Verify parameters can be set in any order
        try {
            mainActivity.setCompressor(-20.0f, 4.0f, 5.0f, 50.0f, 6.0f)
            mainActivity.setCompressor(-15.0f, 3.0f, 10.0f, 100.0f, 4.0f)
            mainActivity.setCompressor(-25.0f, 6.0f, 2.0f, 25.0f, 8.0f)
        } catch (e: Exception) {
            fail("Multiple parameter sets should not throw: ${e.message}")
        }
    }
}
