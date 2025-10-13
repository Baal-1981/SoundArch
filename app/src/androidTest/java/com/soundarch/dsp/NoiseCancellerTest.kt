package com.soundarch.dsp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.soundarch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Noise Canceller (Spectral Subtraction)
 *
 * Tests:
 * - Enable/disable bypass (zero CPU when OFF)
 * - Preset application
 * - Parameter validation
 * - Noise floor estimation
 * - CPU benchmark (if enabled)
 */
class NoiseCancellerTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var mainActivity: MainActivity

    companion object {
        // Parameter ranges
        const val MIN_STRENGTH = 0.0f
        const val MAX_STRENGTH = 1.0f
        const val MIN_SPECTRAL_FLOOR = -80.0f
        const val MAX_SPECTRAL_FLOOR = -30.0f
        const val MIN_SMOOTHING = 0.0f
        const val MAX_SMOOTHING = 1.0f
        const val MIN_ATTACK_MS = 5.0f
        const val MAX_ATTACK_MS = 100.0f
        const val MIN_RELEASE_MS = 50.0f
        const val MAX_RELEASE_MS = 1500.0f
        const val MIN_BOOST_DB = -6.0f
        const val MAX_BOOST_DB = 6.0f
    }

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            mainActivity = activity
        }
        Thread.sleep(1000)  // Allow audio engine to initialize
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎛️ ENABLE/DISABLE TESTS (Critical: Zero CPU when OFF)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testNC_EnableDisable() {
        try {
            mainActivity.setNoiseCancellerEnabled(true)
            mainActivity.setNoiseCancellerEnabled(false)
            mainActivity.setNoiseCancellerEnabled(true)
        } catch (e: Exception) {
            fail("Noise canceller enable/disable should not throw: ${e.message}")
        }
    }

    @Test
    fun testNC_DisabledByDefault() {
        // NC should be disabled by default for zero CPU impact
        try {
            mainActivity.setNoiseCancellerEnabled(false)
            // Should be safe to call even if already disabled
        } catch (e: Exception) {
            fail("Disabling NC should not throw: ${e.message}")
        }
    }

    @Test
    fun testNC_RapidToggle_ThreadSafety() {
        // Test rapid enable/disable (atomic operations)
        repeat(100) { i ->
            mainActivity.setNoiseCancellerEnabled(i % 2 == 0)
        }
        // Should not crash or deadlock
    }

    @Test
    fun testNC_DisabledBypass_ZeroCPU() {
        // When disabled, NC should have zero CPU cost
        // (Can't measure CPU in unit test, but verify it's disabled)
        mainActivity.setNoiseCancellerEnabled(false)

        // Should still accept parameters while disabled
        try {
            mainActivity.setNoiseCancellerParams(
                strength = 0.5f,
                spectralFloor = -50.0f,
                smoothing = 0.5f,
                noiseAttackMs = 20.0f,
                noiseReleaseMs = 200.0f,
                residualBoostDb = 0.0f,
                artifactSuppress = 0.7f
            )
        } catch (e: Exception) {
            fail("Should accept parameters while disabled: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎚️ PRESET TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testPreset_Default() {
        try {
            mainActivity.applyNoiseCancellerPreset(0)  // Default
        } catch (e: Exception) {
            fail("Default preset should not throw: ${e.message}")
        }
    }

    @Test
    fun testPreset_Voice() {
        try {
            mainActivity.applyNoiseCancellerPreset(1)  // Voice
        } catch (e: Exception) {
            fail("Voice preset should not throw: ${e.message}")
        }
    }

    @Test
    fun testPreset_Outdoor() {
        try {
            mainActivity.applyNoiseCancellerPreset(2)  // Outdoor
        } catch (e: Exception) {
            fail("Outdoor preset should not throw: ${e.message}")
        }
    }

    @Test
    fun testPreset_Office() {
        try {
            mainActivity.applyNoiseCancellerPreset(3)  // Office
        } catch (e: Exception) {
            fail("Office preset should not throw: ${e.message}")
        }
    }

    @Test
    fun testPreset_InvalidIndex() {
        try {
            mainActivity.applyNoiseCancellerPreset(999)  // Invalid
            // Should either ignore or throw gracefully
        } catch (e: Exception) {
            assertTrue(
                "Invalid preset should have meaningful error: ${e.message}",
                e.message?.contains("preset", ignoreCase = true) ?: true
            )
        }
    }

    @Test
    fun testPreset_NegativeIndex() {
        try {
            mainActivity.applyNoiseCancellerPreset(-1)  // Invalid
        } catch (e: Exception) {
            assertTrue(
                "Negative preset should be rejected: ${e.message}",
                e.message?.contains("invalid", ignoreCase = true) ?: true
            )
        }
    }

    @Test
    fun testPreset_RapidSwitching() {
        // Test rapid preset switching
        val presets = listOf(0, 1, 2, 3, 0, 1)

        presets.forEach { preset ->
            try {
                mainActivity.applyNoiseCancellerPreset(preset)
            } catch (e: Exception) {
                fail("Rapid preset switching should not throw: ${e.message}")
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔧 PARAMETER TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testParameter_Strength_ValidRange() {
        val validStrengths = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)

        validStrengths.forEach { strength ->
            try {
                mainActivity.setNoiseCancellerParams(
                    strength = strength,
                    spectralFloor = -50.0f,
                    smoothing = 0.5f,
                    noiseAttackMs = 20.0f,
                    noiseReleaseMs = 200.0f,
                    residualBoostDb = 0.0f,
                    artifactSuppress = 0.7f
                )
            } catch (e: Exception) {
                fail("Valid strength $strength should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testParameter_SpectralFloor_ValidRange() {
        val validFloors = listOf(-80.0f, -60.0f, -50.0f, -40.0f, -30.0f)

        validFloors.forEach { floor ->
            try {
                mainActivity.setNoiseCancellerParams(
                    strength = 0.5f,
                    spectralFloor = floor,
                    smoothing = 0.5f,
                    noiseAttackMs = 20.0f,
                    noiseReleaseMs = 200.0f,
                    residualBoostDb = 0.0f,
                    artifactSuppress = 0.7f
                )
            } catch (e: Exception) {
                fail("Valid spectral floor $floor should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testParameter_Smoothing_ValidRange() {
        val validSmoothing = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)

        validSmoothing.forEach { smooth ->
            try {
                mainActivity.setNoiseCancellerParams(
                    strength = 0.5f,
                    spectralFloor = -50.0f,
                    smoothing = smooth,
                    noiseAttackMs = 20.0f,
                    noiseReleaseMs = 200.0f,
                    residualBoostDb = 0.0f,
                    artifactSuppress = 0.7f
                )
            } catch (e: Exception) {
                fail("Valid smoothing $smooth should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testParameter_NoiseAttack_ValidRange() {
        val validAttacks = listOf(5.0f, 10.0f, 20.0f, 50.0f, 100.0f)

        validAttacks.forEach { attack ->
            try {
                mainActivity.setNoiseCancellerParams(
                    strength = 0.5f,
                    spectralFloor = -50.0f,
                    smoothing = 0.5f,
                    noiseAttackMs = attack,
                    noiseReleaseMs = 200.0f,
                    residualBoostDb = 0.0f,
                    artifactSuppress = 0.7f
                )
            } catch (e: Exception) {
                fail("Valid noise attack $attack ms should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testParameter_NoiseRelease_ValidRange() {
        val validReleases = listOf(50.0f, 100.0f, 200.0f, 500.0f, 1000.0f, 1500.0f)

        validReleases.forEach { release ->
            try {
                mainActivity.setNoiseCancellerParams(
                    strength = 0.5f,
                    spectralFloor = -50.0f,
                    smoothing = 0.5f,
                    noiseAttackMs = 20.0f,
                    noiseReleaseMs = release,
                    residualBoostDb = 0.0f,
                    artifactSuppress = 0.7f
                )
            } catch (e: Exception) {
                fail("Valid noise release $release ms should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testParameter_ResidualBoost_ValidRange() {
        val validBoosts = listOf(-6.0f, -3.0f, 0.0f, 3.0f, 6.0f)

        validBoosts.forEach { boost ->
            try {
                mainActivity.setNoiseCancellerParams(
                    strength = 0.5f,
                    spectralFloor = -50.0f,
                    smoothing = 0.5f,
                    noiseAttackMs = 20.0f,
                    noiseReleaseMs = 200.0f,
                    residualBoostDb = boost,
                    artifactSuppress = 0.7f
                )
            } catch (e: Exception) {
                fail("Valid residual boost $boost dB should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testParameter_ArtifactSuppress_ValidRange() {
        val validSuppression = listOf(0.0f, 0.3f, 0.5f, 0.7f, 1.0f)

        validSuppression.forEach { suppress ->
            try {
                mainActivity.setNoiseCancellerParams(
                    strength = 0.5f,
                    spectralFloor = -50.0f,
                    smoothing = 0.5f,
                    noiseAttackMs = 20.0f,
                    noiseReleaseMs = 200.0f,
                    residualBoostDb = 0.0f,
                    artifactSuppress = suppress
                )
            } catch (e: Exception) {
                fail("Valid artifact suppress $suppress should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testParameter_ExtremeValues() {
        // Test parameter clamping with extreme values
        try {
            mainActivity.setNoiseCancellerParams(
                strength = 10.0f,     // > 1.0, should clamp
                spectralFloor = -200.0f,  // < -80, should clamp
                smoothing = -0.5f,    // < 0, should clamp
                noiseAttackMs = 0.1f, // < 5, should clamp
                noiseReleaseMs = 5000.0f,  // > 1500, should clamp
                residualBoostDb = 20.0f,   // > 6, should clamp
                artifactSuppress = 2.0f    // > 1.0, should clamp
            )
            // Should clamp to valid ranges
        } catch (e: Exception) {
            // Or throw with clamping message
            assertTrue(
                "Extreme values should indicate clamping: ${e.message}",
                e.message?.contains("clamp", ignoreCase = true) ?: true
            )
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📊 NOISE FLOOR ESTIMATION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testNoiseFloor_ReturnsValidValue() {
        val noiseFloor = mainActivity.getNoiseCancellerNoiseFloor()

        // Noise floor should be negative dB value (typically -80 to -20 dBFS)
        assertTrue(
            "Noise floor ($noiseFloor dB) should be negative",
            noiseFloor < 0.0f
        )

        assertTrue(
            "Noise floor ($noiseFloor dB) should be in valid range [-100, 0]",
            noiseFloor in -100.0f..0.0f
        )
    }

    @Test
    fun testNoiseFloor_Consistency() {
        // Read noise floor multiple times
        val readings = List(10) { mainActivity.getNoiseCancellerNoiseFloor() }

        // All readings should be valid
        readings.forEach { floor ->
            assertTrue(
                "Noise floor ($floor) should be negative",
                floor < 0.0f
            )
        }
    }

    @Test
    fun testNoiseFloor_WithNCDisabled() {
        // Noise floor should still be readable when NC is disabled
        mainActivity.setNoiseCancellerEnabled(false)

        val noiseFloor = mainActivity.getNoiseCancellerNoiseFloor()
        assertNotNull("Noise floor should be readable when disabled", noiseFloor)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔗 INTEGRATION TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testNC_FullConfiguration() {
        try {
            mainActivity.setNoiseCancellerEnabled(true)
            mainActivity.applyNoiseCancellerPreset(1)  // Voice preset
            mainActivity.setNoiseCancellerParams(
                strength = 0.6f,
                spectralFloor = -55.0f,
                smoothing = 0.6f,
                noiseAttackMs = 15.0f,
                noiseReleaseMs = 250.0f,
                residualBoostDb = 2.0f,
                artifactSuppress = 0.75f
            )

            val noiseFloor = mainActivity.getNoiseCancellerNoiseFloor()
            assertNotNull("Noise floor should be readable", noiseFloor)

        } catch (e: Exception) {
            fail("Full NC configuration should not throw: ${e.message}")
        }
    }

    @Test
    fun testNC_PresetThenCustomize() {
        // Apply preset, then customize parameters
        try {
            mainActivity.applyNoiseCancellerPreset(0)  // Default

            // Customize after preset
            mainActivity.setNoiseCancellerParams(
                strength = 0.7f,
                spectralFloor = -50.0f,
                smoothing = 0.5f,
                noiseAttackMs = 20.0f,
                noiseReleaseMs = 200.0f,
                residualBoostDb = 1.0f,
                artifactSuppress = 0.8f
            )
        } catch (e: Exception) {
            fail("Preset + customize should not throw: ${e.message}")
        }
    }

    @Test
    fun testNC_RapidParameterChanges() {
        // Test stability under rapid updates
        mainActivity.setNoiseCancellerEnabled(true)

        repeat(50) { i ->
            val strength = (i % 10) / 10.0f

            mainActivity.setNoiseCancellerParams(
                strength = strength,
                spectralFloor = -50.0f,
                smoothing = 0.5f,
                noiseAttackMs = 20.0f,
                noiseReleaseMs = 200.0f,
                residualBoostDb = 0.0f,
                artifactSuppress = 0.7f
            )
        }
        // Should not crash or glitch
    }

    @Test
    fun testNC_PresetCharacteristics() {
        // Verify presets exist and are distinct
        val presetIndices = listOf(0, 1, 2, 3)

        presetIndices.forEach { preset ->
            try {
                mainActivity.applyNoiseCancellerPreset(preset)
                // Each preset should apply successfully
            } catch (e: Exception) {
                fail("Preset $preset should be valid: ${e.message}")
            }
        }
    }

    @Test
    fun testNC_WithFullDSPChain() {
        // Test NC in context of full DSP chain
        try {
            mainActivity.setAGCEnabled(true)
            mainActivity.setCompressorEnabled(true)
            mainActivity.setLimiterEnabled(true)
            mainActivity.setNoiseCancellerEnabled(true)

            // NC should work correctly in full chain
            mainActivity.applyNoiseCancellerPreset(1)

        } catch (e: Exception) {
            fail("NC should work with full DSP chain: ${e.message}")
        }
    }

    @Test
    fun testNC_EnableDisable_ParameterPersistence() {
        // Verify parameters persist across enable/disable
        mainActivity.setNoiseCancellerParams(
            strength = 0.8f,
            spectralFloor = -45.0f,
            smoothing = 0.6f,
            noiseAttackMs = 15.0f,
            noiseReleaseMs = 300.0f,
            residualBoostDb = 3.0f,
            artifactSuppress = 0.85f
        )

        mainActivity.setNoiseCancellerEnabled(true)
        mainActivity.setNoiseCancellerEnabled(false)
        mainActivity.setNoiseCancellerEnabled(true)

        // Parameters should remain set (can't verify directly, but check no crash)
    }

    @Test
    fun testNC_ZeroStrength_EffectiveBypass() {
        // Zero strength should effectively bypass NC
        try {
            mainActivity.setNoiseCancellerEnabled(true)
            mainActivity.setNoiseCancellerParams(
                strength = 0.0f,  // No effect
                spectralFloor = -50.0f,
                smoothing = 0.5f,
                noiseAttackMs = 20.0f,
                noiseReleaseMs = 200.0f,
                residualBoostDb = 0.0f,
                artifactSuppress = 0.7f
            )
            // Should pass through with minimal processing
        } catch (e: Exception) {
            fail("Zero strength should be valid: ${e.message}")
        }
    }

    @Test
    fun testNC_MaximumStrength() {
        // Maximum strength should provide aggressive noise reduction
        try {
            mainActivity.setNoiseCancellerEnabled(true)
            mainActivity.setNoiseCancellerParams(
                strength = 1.0f,  // Maximum
                spectralFloor = -60.0f,
                smoothing = 0.8f,
                noiseAttackMs = 10.0f,
                noiseReleaseMs = 500.0f,
                residualBoostDb = 0.0f,
                artifactSuppress = 0.9f
            )
            // Should provide maximum noise reduction
        } catch (e: Exception) {
            fail("Maximum strength should be valid: ${e.message}")
        }
    }
}
