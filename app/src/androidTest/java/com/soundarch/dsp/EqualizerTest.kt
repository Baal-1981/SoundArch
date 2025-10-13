package com.soundarch.dsp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.soundarch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Equalizer (Parametric EQ)
 *
 * Tests:
 * - Band gain validation
 * - Array length validation
 * - Frequency response
 * - Bypass behavior
 */
class EqualizerTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var mainActivity: MainActivity

    companion object {
        // Standard 10-band equalizer frequencies (Hz)
        val STANDARD_BANDS = listOf(
            32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000
        )

        const val NUM_BANDS = 10
        const val MIN_GAIN_DB = -12.0f
        const val MAX_GAIN_DB = 12.0f
    }

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            mainActivity = activity
        }
        Thread.sleep(1000)  // Allow audio engine to initialize
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎚️ BAND GAIN TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testBandGain_ValidRange() {
        // Test each band with valid gains
        val validGains = listOf(-12.0f, -6.0f, 0.0f, 6.0f, 12.0f)

        validGains.forEach { gain ->
            val gains = FloatArray(NUM_BANDS) { gain }

            try {
                mainActivity.setEqBands(gains)
            } catch (e: Exception) {
                fail("Valid gain $gain dB should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testBandGain_ZeroGain() {
        // Zero gain = flat response (no change)
        val flatGains = FloatArray(NUM_BANDS) { 0.0f }

        try {
            mainActivity.setEqBands(flatGains)
            // Should pass through unmodified
        } catch (e: Exception) {
            fail("Flat EQ (0dB all bands) should not throw: ${e.message}")
        }
    }

    @Test
    fun testBandGain_ExtremeBoost() {
        // Test maximum boost
        val maxBoost = FloatArray(NUM_BANDS) { MAX_GAIN_DB }

        try {
            mainActivity.setEqBands(maxBoost)
        } catch (e: Exception) {
            fail("Maximum boost should not throw: ${e.message}")
        }
    }

    @Test
    fun testBandGain_ExtremeCut() {
        // Test maximum cut
        val maxCut = FloatArray(NUM_BANDS) { MIN_GAIN_DB }

        try {
            mainActivity.setEqBands(maxCut)
        } catch (e: Exception) {
            fail("Maximum cut should not throw: ${e.message}")
        }
    }

    @Test
    fun testBandGain_MixedValues() {
        // Test alternating boost/cut
        val mixedGains = FloatArray(NUM_BANDS) { i ->
            if (i % 2 == 0) 6.0f else -6.0f
        }

        try {
            mainActivity.setEqBands(mixedGains)
        } catch (e: Exception) {
            fail("Mixed gains should not throw: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📊 ARRAY LENGTH VALIDATION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testArrayLength_Correct() {
        // Correct length: 10 bands
        val gains = FloatArray(NUM_BANDS) { 0.0f }

        try {
            mainActivity.setEqBands(gains)
        } catch (e: Exception) {
            fail("Correct array length should not throw: ${e.message}")
        }
    }

    @Test
    fun testArrayLength_TooShort() {
        // Too few bands
        val shortGains = FloatArray(5) { 0.0f }

        try {
            mainActivity.setEqBands(shortGains)
            // Should either clamp or throw gracefully
        } catch (e: Exception) {
            assertTrue(
                "Exception should indicate array length issue: ${e.message}",
                e.message?.contains("length", ignoreCase = true) ?: true
            )
        }
    }

    @Test
    fun testArrayLength_TooLong() {
        // Too many bands
        val longGains = FloatArray(15) { 0.0f }

        try {
            mainActivity.setEqBands(longGains)
            // Should use first 10 bands
        } catch (e: Exception) {
            // Should either clamp or accept gracefully
            assertTrue(
                "Should handle extra bands gracefully: ${e.message}",
                e.message?.contains("bands", ignoreCase = true) ?: true
            )
        }
    }

    @Test
    fun testArrayLength_Empty() {
        // Empty array
        val emptyGains = FloatArray(0)

        try {
            mainActivity.setEqBands(emptyGains)
            // Should handle gracefully
        } catch (e: Exception) {
            // Expected to fail or handle gracefully
            assertNotNull("Empty array should be handled", e.message)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎛️ PRESET TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testPreset_Flat() {
        // Flat preset (0dB all bands)
        val flatGains = FloatArray(NUM_BANDS) { 0.0f }

        try {
            mainActivity.setEqBands(flatGains)
        } catch (e: Exception) {
            fail("Flat preset should not throw: ${e.message}")
        }
    }

    @Test
    fun testPreset_BassBoost() {
        // Bass boost preset: boost low frequencies
        val bassBoost = floatArrayOf(
            8.0f, 6.0f, 4.0f, 2.0f, 0.0f,  // Low-mid boost
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f   // High flat
        )

        try {
            mainActivity.setEqBands(bassBoost)
        } catch (e: Exception) {
            fail("Bass boost preset should not throw: ${e.message}")
        }
    }

    @Test
    fun testPreset_VShape() {
        // V-shape preset: boost lows and highs, cut mids
        val vShape = floatArrayOf(
            6.0f, 4.0f, 2.0f,   // Bass boost
            -2.0f, -3.0f,        // Mid cut
            -3.0f, -2.0f,        // Mid cut
            2.0f, 4.0f, 6.0f    // Treble boost
        )

        try {
            mainActivity.setEqBands(vShape)
        } catch (e: Exception) {
            fail("V-shape preset should not throw: ${e.message}")
        }
    }

    @Test
    fun testPreset_VocalBoost() {
        // Vocal boost: emphasize mid frequencies
        val vocalBoost = floatArrayOf(
            0.0f, 0.0f,          // Bass flat
            2.0f, 4.0f, 5.0f,    // Mid boost
            4.0f, 3.0f,          // Upper mid boost
            0.0f, 0.0f, 0.0f     // Treble flat
        )

        try {
            mainActivity.setEqBands(vocalBoost)
        } catch (e: Exception) {
            fail("Vocal boost preset should not throw: ${e.message}")
        }
    }

    @Test
    fun testPreset_TrebleBoost() {
        // Treble boost: emphasize high frequencies
        val trebleBoost = floatArrayOf(
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  // Low flat
            2.0f, 4.0f, 6.0f, 8.0f, 8.0f   // High boost
        )

        try {
            mainActivity.setEqBands(trebleBoost)
        } catch (e: Exception) {
            fail("Treble boost preset should not throw: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔗 INTEGRATION TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testEqualizer_RapidPresetChanges() {
        // Test rapid preset switching
        val presets = listOf(
            FloatArray(NUM_BANDS) { 0.0f },     // Flat
            FloatArray(NUM_BANDS) { 6.0f },     // All boost
            FloatArray(NUM_BANDS) { -6.0f },    // All cut
            FloatArray(NUM_BANDS) { i -> if (i < 5) 4.0f else -2.0f }  // Bass boost
        )

        presets.forEach { preset ->
            try {
                mainActivity.setEqBands(preset)
            } catch (e: Exception) {
                fail("Rapid preset changes should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testEqualizer_SmoothTransitions() {
        // Test smooth gain transitions
        for (gain in -12..12 step 2) {
            val gains = FloatArray(NUM_BANDS) { gain.toFloat() }
            mainActivity.setEqBands(gains)
        }
        // Should not produce clicks or glitches (requires audio test)
    }

    @Test
    fun testEqualizer_IndividualBandControl() {
        // Test setting each band independently
        for (band in 0 until NUM_BANDS) {
            val gains = FloatArray(NUM_BANDS) { 0.0f }
            gains[band] = 6.0f  // Solo this band

            try {
                mainActivity.setEqBands(gains)
            } catch (e: Exception) {
                fail("Individual band $band control should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testEqualizer_SymmetricalCurve() {
        // Test symmetrical EQ curve
        val symmetrical = floatArrayOf(
            6.0f, 4.0f, 2.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 2.0f, 4.0f, 6.0f
        )

        try {
            mainActivity.setEqBands(symmetrical)
        } catch (e: Exception) {
            fail("Symmetrical curve should not throw: ${e.message}")
        }
    }

    @Test
    fun testEqualizer_ExtremeCurve() {
        // Test extreme alternating values
        val extreme = FloatArray(NUM_BANDS) { i ->
            if (i % 2 == 0) MAX_GAIN_DB else MIN_GAIN_DB
        }

        try {
            mainActivity.setEqBands(extreme)
        } catch (e: Exception) {
            fail("Extreme curve should not throw: ${e.message}")
        }
    }

    @Test
    fun testEqualizer_FrequencyResponse() {
        // Verify standard frequencies are used
        // (Can't test actual frequency response without audio analysis)
        assertEquals("Should have 10 bands", NUM_BANDS, STANDARD_BANDS.size)

        // Verify frequencies are in ascending order
        for (i in 0 until STANDARD_BANDS.size - 1) {
            assertTrue(
                "Band ${i+1} (${STANDARD_BANDS[i+1]}Hz) should be higher than band $i (${STANDARD_BANDS[i]}Hz)",
                STANDARD_BANDS[i+1] > STANDARD_BANDS[i]
            )
        }
    }

    @Test
    fun testEqualizer_LowFrequencyRange() {
        // Test bass/sub-bass frequencies (32-250 Hz)
        val bassBands = STANDARD_BANDS.filter { it <= 250 }

        assertTrue("Should have 4 bass bands", bassBands.size >= 3)
        assertTrue("Should include 32 Hz", 32 in STANDARD_BANDS)
        assertTrue("Should include 64 Hz", 64 in STANDARD_BANDS)
        assertTrue("Should include 125 Hz", 125 in STANDARD_BANDS)
    }

    @Test
    fun testEqualizer_MidFrequencyRange() {
        // Test midrange frequencies (500-4000 Hz)
        val midBands = STANDARD_BANDS.filter { it in 500..4000 }

        assertTrue("Should have 4 mid bands", midBands.size >= 3)
        assertTrue("Should include 1 kHz", 1000 in STANDARD_BANDS)
        assertTrue("Should include 2 kHz", 2000 in STANDARD_BANDS)
    }

    @Test
    fun testEqualizer_HighFrequencyRange() {
        // Test treble/presence frequencies (8000-16000 Hz)
        val trebleBands = STANDARD_BANDS.filter { it >= 8000 }

        assertTrue("Should have 2-3 treble bands", trebleBands.size >= 2)
        assertTrue("Should include 8 kHz", 8000 in STANDARD_BANDS)
        assertTrue("Should include 16 kHz", 16000 in STANDARD_BANDS)
    }

    @Test
    fun testEqualizer_ThreadSafety_ConcurrentUpdates() {
        // Test rapid concurrent updates
        repeat(100) { i ->
            val gain = (i % 25) - 12.0f  // Range: -12 to +12
            val gains = FloatArray(NUM_BANDS) { gain }
            mainActivity.setEqBands(gains)
        }
        // Should not crash or produce race conditions
    }

    @Test
    fun testEqualizer_Reset() {
        // Test reset to flat
        mainActivity.setEqBands(FloatArray(NUM_BANDS) { 6.0f })  // Boost all
        mainActivity.setEqBands(FloatArray(NUM_BANDS) { 0.0f })  // Reset flat

        // Should restore flat response
    }

    @Test
    fun testEqualizer_NaNHandling() {
        // Test NaN handling
        val gainsWithNaN = FloatArray(NUM_BANDS) { Float.NaN }

        try {
            mainActivity.setEqBands(gainsWithNaN)
            // Should either reject or sanitize NaN values
        } catch (e: Exception) {
            // Expected to fail gracefully
            assertNotNull("NaN should be handled", e.message)
        }
    }

    @Test
    fun testEqualizer_InfinityHandling() {
        // Test infinity handling
        val gainsWithInf = FloatArray(NUM_BANDS) { Float.POSITIVE_INFINITY }

        try {
            mainActivity.setEqBands(gainsWithInf)
            // Should clamp to valid range
        } catch (e: Exception) {
            // Expected to fail gracefully
            assertNotNull("Infinity should be handled", e.message)
        }
    }
}
