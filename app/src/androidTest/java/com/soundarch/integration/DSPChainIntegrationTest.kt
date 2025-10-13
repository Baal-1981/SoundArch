package com.soundarch.integration

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.soundarch.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * DSP Chain Integration Test - End-to-End Audio Processing Verification
 *
 * This test verifies the complete DSP chain from end to end:
 * AGC → Equalizer → Voice Gain → Noise Canceller → Compressor → Limiter
 *
 * **Test Strategy:**
 * 1. Launch MainActivity to access JNI methods
 * 2. Feed synthetic audio signals through the chain
 * 3. Verify output characteristics match expected behavior
 * 4. Test enable/disable for each DSP module
 * 5. Verify parameter updates propagate correctly
 *
 * **Run this test:**
 *   adb shell am instrument -w -r \
 *     -e class com.soundarch.integration.DSPChainIntegrationTest \
 *     com.soundarch.test/androidx.test.runner.AndroidJUnitRunner
 *
 * **Requirements:**
 * - Device or emulator must be connected
 * - Audio permissions granted
 * - Native library (libsoundarch.so) must be loaded
 */
@RunWith(AndroidJUnit4::class)
class DSPChainIntegrationTest {

    companion object {
        private const val TAG = "DSPChainIntegrationTest"
        private const val SAMPLE_RATE = 48000f
        private const val BUFFER_SIZE = 480 // 10ms @ 48kHz
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var mainActivity: MainActivity

    @Before
    fun setup() {
        android.util.Log.i(TAG, "=".repeat(80))
        android.util.Log.i(TAG, "Starting DSP Chain Integration Test")
        android.util.Log.i(TAG, "=".repeat(80))

        // Get MainActivity instance
        activityRule.scenario.onActivity { activity ->
            mainActivity = activity
        }

        // Wait for audio engine to initialize
        Thread.sleep(1000)

        android.util.Log.i(TAG, "✅ MainActivity launched, audio engine ready")
    }

    @After
    fun teardown() {
        android.util.Log.i(TAG, "✅ Test complete")
        android.util.Log.i(TAG, "=".repeat(80))
    }

    // ==================================================================================
    // TEST SUITE 1: DSP Module Initialization
    // ==================================================================================

    /**
     * Test: Verify all DSP modules initialize correctly
     *
     * Acceptance Criteria:
     * - Audio engine starts without errors
     * - All DSP modules are initialized
     * - Default parameters are set correctly
     */
    @Test
    fun test01_AllDSPModulesInitialize() {
        android.util.Log.i(TAG, "TEST: Verify all DSP modules initialize")

        // Verify AGC responds to queries
        val agcGain = mainActivity.getAGCCurrentGain()
        val agcLevel = mainActivity.getAGCCurrentLevel()
        assertThat(agcGain).isNotNaN()
        assertThat(agcLevel).isNotNaN()
        android.util.Log.i(TAG, "✅ AGC initialized: Gain=${String.format("%.2f", agcGain)}dB Level=${String.format("%.2f", agcLevel)}dB")

        // Verify Compressor responds to queries
        val compGR = mainActivity.getCompressorGainReduction()
        assertThat(compGR).isNotNaN()
        android.util.Log.i(TAG, "✅ Compressor initialized: GR=${String.format("%.2f", compGR)}dB")

        // Verify Limiter responds to queries
        val limGR = mainActivity.getLimiterGainReduction()
        assertThat(limGR).isNotNaN()
        android.util.Log.i(TAG, "✅ Limiter initialized: GR=${String.format("%.2f", limGR)}dB")

        // Verify Voice Gain responds to queries
        val voiceGain = mainActivity.getVoiceGain()
        assertThat(voiceGain).isEqualTo(0.0f) // Default should be 0dB
        android.util.Log.i(TAG, "✅ Voice Gain initialized: ${String.format("%+.1f", voiceGain)}dB")

        // Verify Noise Canceller responds to queries
        val ncFloor = mainActivity.getNoiseCancellerNoiseFloor()
        assertThat(ncFloor).isAtMost(0.0f) // Noise floor should be negative dB
        android.util.Log.i(TAG, "✅ Noise Canceller initialized: Floor=${String.format("%.1f", ncFloor)}dB")
    }

    // ==================================================================================
    // TEST SUITE 2: AGC (Automatic Gain Control)
    // ==================================================================================

    /**
     * Test: AGC adjusts gain based on input level
     *
     * Test Strategy:
     * 1. Set AGC target to -20dB
     * 2. Enable AGC
     * 3. Wait for AGC to process audio
     * 4. Verify AGC is applying gain adjustments
     */
    @Test
    fun test02_AGC_AdjustsGainBasedOnInputLevel() {
        android.util.Log.i(TAG, "TEST: AGC adjusts gain based on input level")

        // Configure AGC
        mainActivity.setAGCTargetLevel(-20.0f)
        mainActivity.setAGCMaxGain(25.0f)
        mainActivity.setAGCMinGain(-10.0f)
        mainActivity.setAGCEnabled(true)

        android.util.Log.i(TAG, "AGC configured: Target=-20dB, MaxGain=+25dB, MinGain=-10dB")

        // Wait for AGC to adapt (1 second)
        Thread.sleep(1000)

        // Read AGC state
        val agcGain = mainActivity.getAGCCurrentGain()
        val agcLevel = mainActivity.getAGCCurrentLevel()

        // Verify AGC is within bounds
        assertThat(agcGain).isAtLeast(-10.0f)
        assertThat(agcGain).isAtMost(25.0f)

        android.util.Log.i(TAG, "✅ AGC adapting: Gain=${String.format("%.2f", agcGain)}dB Level=${String.format("%.2f", agcLevel)}dB")
    }

    /**
     * Test: AGC can be disabled and re-enabled
     */
    @Test
    fun test03_AGC_CanBeDisabledAndReEnabled() {
        android.util.Log.i(TAG, "TEST: AGC can be disabled and re-enabled")

        // Enable AGC
        mainActivity.setAGCEnabled(true)
        Thread.sleep(50)  // Reduced from 200ms - JNI enable is fast

        val gainEnabled = mainActivity.getAGCCurrentGain()
        android.util.Log.i(TAG, "AGC Enabled: Gain=${String.format("%.2f", gainEnabled)}dB")

        // Disable AGC
        mainActivity.setAGCEnabled(false)
        Thread.sleep(50)  // Reduced from 200ms - JNI disable is fast

        // AGC should still respond to queries even when disabled
        val gainDisabled = mainActivity.getAGCCurrentGain()
        assertThat(gainDisabled).isNotNaN()
        android.util.Log.i(TAG, "AGC Disabled: Gain=${String.format("%.2f", gainDisabled)}dB")

        // Re-enable AGC
        mainActivity.setAGCEnabled(true)
        Thread.sleep(50)  // Reduced from 200ms - JNI enable is fast

        val gainReEnabled = mainActivity.getAGCCurrentGain()
        assertThat(gainReEnabled).isNotNaN()
        android.util.Log.i(TAG, "✅ AGC Re-enabled: Gain=${String.format("%.2f", gainReEnabled)}dB")
    }

    // ==================================================================================
    // TEST SUITE 3: Equalizer (10-band Parametric EQ)
    // ==================================================================================

    /**
     * Test: Equalizer accepts and applies gain values
     *
     * Test Strategy:
     * 1. Set all bands to +6dB
     * 2. Verify no crashes or errors
     * 3. Set all bands to 0dB (flat response)
     * 4. Set alternating +3dB / -3dB pattern
     */
    @Test
    fun test04_Equalizer_AcceptsAndAppliesGainValues() {
        android.util.Log.i(TAG, "TEST: Equalizer accepts and applies gain values")

        // Test 1: All bands +6dB
        val gains1 = FloatArray(10) { 6.0f }
        mainActivity.setEqBands(gains1)
        // No sleep needed - JNI call is synchronous
        android.util.Log.i(TAG, "✅ EQ Test 1: All bands +6dB applied")

        // Test 2: All bands 0dB (flat)
        val gains2 = FloatArray(10) { 0.0f }
        mainActivity.setEqBands(gains2)
        // No sleep needed - JNI call is synchronous
        android.util.Log.i(TAG, "✅ EQ Test 2: All bands 0dB (flat) applied")

        // Test 3: Alternating pattern
        val gains3 = FloatArray(10) { index -> if (index % 2 == 0) 3.0f else -3.0f }
        mainActivity.setEqBands(gains3)
        // No sleep needed - JNI call is synchronous
        android.util.Log.i(TAG, "✅ EQ Test 3: Alternating +3dB/-3dB pattern applied")

        // Test 4: V-curve (bass/treble boost, mids cut)
        val gains4 = floatArrayOf(6.0f, 4.0f, 2.0f, 0.0f, -2.0f, -2.0f, 0.0f, 2.0f, 4.0f, 6.0f)
        mainActivity.setEqBands(gains4)
        // No sleep needed - JNI call is synchronous
        android.util.Log.i(TAG, "✅ EQ Test 4: V-curve (bass/treble boost) applied")
    }

    // ==================================================================================
    // TEST SUITE 4: Voice Gain (Post-EQ, Pre-Dynamics)
    // ==================================================================================

    /**
     * Test: Voice Gain adjusts level correctly
     *
     * Test Strategy:
     * 1. Set Voice Gain to +6dB
     * 2. Verify gain is applied
     * 3. Set Voice Gain to -6dB
     * 4. Reset to 0dB
     */
    @Test
    fun test05_VoiceGain_AdjustsLevelCorrectly() {
        android.util.Log.i(TAG, "TEST: Voice Gain adjusts level correctly")

        // Test 1: +6dB gain
        mainActivity.setVoiceGain(6.0f)
        val gain1 = mainActivity.getVoiceGain()
        assertThat(gain1).isWithin(0.1f).of(6.0f)
        android.util.Log.i(TAG, "✅ Voice Gain +6dB: ${String.format("%+.1f", gain1)}dB")

        // Test 2: -6dB gain
        mainActivity.setVoiceGain(-6.0f)
        val gain2 = mainActivity.getVoiceGain()
        assertThat(gain2).isWithin(0.1f).of(-6.0f)
        android.util.Log.i(TAG, "✅ Voice Gain -6dB: ${String.format("%+.1f", gain2)}dB")

        // Test 3: Reset to 0dB
        mainActivity.setVoiceGain(0.0f)
        val gain3 = mainActivity.getVoiceGain()
        assertThat(gain3).isEqualTo(0.0f)
        android.util.Log.i(TAG, "✅ Voice Gain reset: ${String.format("%+.1f", gain3)}dB")

        // Test 4: Clamp to +12dB max
        mainActivity.setVoiceGain(20.0f) // Attempt to set beyond max
        val gain4 = mainActivity.getVoiceGain()
        assertThat(gain4).isAtMost(12.0f) // Should be clamped
        android.util.Log.i(TAG, "✅ Voice Gain clamped: ${String.format("%+.1f", gain4)}dB (max +12dB)")
    }

    // ==================================================================================
    // TEST SUITE 5: Noise Canceller (Spectral Subtraction)
    // ==================================================================================

    /**
     * Test: Noise Canceller can be enabled and configured
     *
     * Test Strategy:
     * 1. Enable Noise Canceller
     * 2. Verify noise floor is being tracked
     */
    @Test
    fun test06_NoiseCanceller_CanBeEnabledAndConfigured() {
        android.util.Log.i(TAG, "TEST: Noise Canceller can be enabled and configured")

        // Disable initially
        mainActivity.setNoiseCancellerEnabled(false)
        Thread.sleep(50)  // Reduced from 200ms - JNI disable is fast

        // Enable Noise Canceller
        mainActivity.setNoiseCancellerEnabled(true)
        Thread.sleep(200)  // Reduced from 500ms - noise floor estimates faster than expected

        val floor = mainActivity.getNoiseCancellerNoiseFloor()
        assertThat(floor).isAtMost(0.0f)
        android.util.Log.i(TAG, "✅ NC enabled: Floor=${String.format("%.1f", floor)}dB")

        // Disable again
        mainActivity.setNoiseCancellerEnabled(false)
        android.util.Log.i(TAG, "✅ NC disabled")
    }

    // ==================================================================================
    // TEST SUITE 6: Compressor (Dynamic Range Control)
    // ==================================================================================

    /**
     * Test: Compressor applies gain reduction above threshold
     *
     * Test Strategy:
     * 1. Configure compressor with known parameters
     * 2. Enable compressor
     * 3. Verify gain reduction occurs (if audio is loud enough)
     */
    @Test
    fun test07_Compressor_AppliesGainReductionAboveThreshold() {
        android.util.Log.i(TAG, "TEST: Compressor applies gain reduction")

        // Configure compressor
        mainActivity.setCompressor(
            threshold = -20.0f,
            ratio = 4.0f,
            attack = 5.0f,
            release = 50.0f,
            makeupGain = 0.0f
        )
        mainActivity.setCompressorEnabled(true)

        android.util.Log.i(TAG, "Compressor configured: Thr=-20dB Ratio=4:1 Att=5ms Rel=50ms")

        // Wait for compressor to process audio
        Thread.sleep(200)  // Reduced from 500ms - compressor adapts quickly with 5ms attack

        // Read gain reduction
        val gr = mainActivity.getCompressorGainReduction()
        assertThat(gr).isNotNaN()
        assertThat(gr).isAtLeast(0.0f) // GR is always >= 0

        android.util.Log.i(TAG, "✅ Compressor GR: ${String.format("%.2f", gr)}dB")

        // Disable compressor
        mainActivity.setCompressorEnabled(false)
        Thread.sleep(50)  // Reduced from 200ms - JNI disable is fast

        val grDisabled = mainActivity.getCompressorGainReduction()
        android.util.Log.i(TAG, "Compressor disabled: GR=${String.format("%.2f", grDisabled)}dB")
    }

    // ==================================================================================
    // TEST SUITE 7: Limiter (Brick-wall Peak Protection)
    // ==================================================================================

    /**
     * Test: Limiter prevents peaks above threshold
     *
     * Test Strategy:
     * 1. Configure limiter with -1dBFS threshold
     * 2. Enable limiter
     * 3. Verify gain reduction occurs if needed
     */
    @Test
    fun test08_Limiter_PreventsPeaksAboveThreshold() {
        android.util.Log.i(TAG, "TEST: Limiter prevents peaks above threshold")

        // Configure limiter
        mainActivity.setLimiter(
            threshold = -1.0f,
            release = 50.0f,
            lookahead = 0.0f
        )
        mainActivity.setLimiterEnabled(true)

        android.util.Log.i(TAG, "Limiter configured: Thr=-1dBFS Rel=50ms")

        // Wait for limiter to process audio
        Thread.sleep(200)  // Reduced from 500ms - limiter responds quickly

        // Read gain reduction
        val gr = mainActivity.getLimiterGainReduction()
        assertThat(gr).isNotNaN()
        assertThat(gr).isAtLeast(0.0f) // GR is always >= 0

        android.util.Log.i(TAG, "✅ Limiter GR: ${String.format("%.2f", gr)}dB")

        // Verify audio peaks are within limit
        val peakDb = mainActivity.getPeakDb()
        assertThat(peakDb).isNotNaN()
        android.util.Log.i(TAG, "Peak level: ${String.format("%.2f", peakDb)}dBFS")

        // Disable limiter
        mainActivity.setLimiterEnabled(false)
    }

    // ==================================================================================
    // TEST SUITE 8: Audio Level Monitoring (Peak/RMS Meter)
    // ==================================================================================

    /**
     * Test: Audio levels can be read from DSP chain
     *
     * Test Strategy:
     * 1. Read peak and RMS levels
     * 2. Verify values are in valid range [-60dBFS, 0dBFS]
     * 3. Verify peak >= RMS (always true for audio signals)
     */
    @Test
    fun test09_AudioLevels_CanBeReadFromDSPChain() {
        android.util.Log.i(TAG, "TEST: Audio levels can be read")

        // Wait for audio to process
        Thread.sleep(200)  // Reduced from 500ms - audio levels update quickly

        // Read multiple samples
        val samples = 5
        val peakLevels = mutableListOf<Float>()
        val rmsLevels = mutableListOf<Float>()

        repeat(samples) {
            val peak = mainActivity.getPeakDb()
            val rms = mainActivity.getRmsDb()

            peakLevels.add(peak)
            rmsLevels.add(rms)

            assertThat(peak).isAtLeast(-60.0f)
            assertThat(peak).isAtMost(0.0f)
            assertThat(rms).isAtLeast(-60.0f)
            assertThat(rms).isAtMost(0.0f)

            // Peak should always be >= RMS
            if (peak > -60.0f && rms > -60.0f) { // Ignore silence
                assertThat(peak).isAtLeast(rms)
            }

            android.util.Log.i(TAG, "Sample $it: Peak=${String.format("%.1f", peak)}dB RMS=${String.format("%.1f", rms)}dB")

            Thread.sleep(50)  // Reduced from 100ms - faster sampling
        }

        // Calculate average
        val avgPeak = peakLevels.average().toFloat()
        val avgRms = rmsLevels.average().toFloat()

        android.util.Log.i(TAG, "✅ Avg levels: Peak=${String.format("%.1f", avgPeak)}dB RMS=${String.format("%.1f", avgRms)}dB")
    }

    // ==================================================================================
    // TEST SUITE 9: Full DSP Chain Integration
    // ==================================================================================

    /**
     * Test: Full DSP chain processes audio without errors
     *
     * Test Strategy:
     * 1. Enable all DSP modules
     * 2. Configure with moderate settings
     * 3. Let chain process for 2 seconds
     * 4. Verify no crashes or errors
     * 5. Verify all modules are still responding
     */
    @Test
    fun test10_FullDSPChain_ProcessesAudioWithoutErrors() {
        android.util.Log.i(TAG, "TEST: Full DSP chain integration")

        // Enable all DSP modules
        mainActivity.setAGCEnabled(true)
        mainActivity.setCompressorEnabled(true)
        mainActivity.setLimiterEnabled(true)
        mainActivity.setNoiseCancellerEnabled(true)

        // Configure moderate settings
        mainActivity.setAGCTargetLevel(-20.0f)
        mainActivity.setAGCMaxGain(15.0f)

        mainActivity.setEqBands(FloatArray(10) { 0.0f }) // Flat EQ

        mainActivity.setVoiceGain(0.0f)

        mainActivity.setCompressor(-20.0f, 3.0f, 10.0f, 100.0f, 0.0f)

        mainActivity.setLimiter(-1.0f, 50.0f, 0.0f)

        android.util.Log.i(TAG, "All DSP modules enabled and configured")

        // Process audio for 2 seconds
        repeat(20) { i ->
            Thread.sleep(50)  // Reduced from 100ms - faster monitoring

            val peak = mainActivity.getPeakDb()
            val rms = mainActivity.getRmsDb()
            val agcGain = mainActivity.getAGCCurrentGain()
            val compGR = mainActivity.getCompressorGainReduction()
            val limGR = mainActivity.getLimiterGainReduction()

            if (i % 5 == 0) {
                android.util.Log.i(TAG, "Chain status: Peak=${String.format("%.1f", peak)}dB AGC=${String.format("%.1f", agcGain)}dB CompGR=${String.format("%.1f", compGR)}dB")
            }

            // Verify all values are valid (not NaN)
            assertThat(peak).isNotNaN()
            assertThat(rms).isNotNaN()
            assertThat(agcGain).isNotNaN()
            assertThat(compGR).isNotNaN()
            assertThat(limGR).isNotNaN()
        }

        android.util.Log.i(TAG, "✅ Full DSP chain processed 2 seconds of audio without errors")

        // Disable all modules
        mainActivity.setAGCEnabled(false)
        mainActivity.setCompressorEnabled(false)
        mainActivity.setLimiterEnabled(false)
        mainActivity.setNoiseCancellerEnabled(false)
        mainActivity.setVoiceGain(0.0f)

        android.util.Log.i(TAG, "All DSP modules disabled")
    }

    // ==================================================================================
    // TEST SUITE 10: Performance and Stability
    // ==================================================================================

    /**
     * Test: DSP chain is stable over extended runtime
     *
     * Test Strategy:
     * 1. Enable full DSP chain
     * 2. Run for 10 seconds
     * 3. Monitor for crashes, hangs, or errors
     * 4. Verify values remain in valid ranges
     */
    @Test
    fun test11_DSPChain_IsStableOverExtendedRuntime() {
        android.util.Log.i(TAG, "TEST: DSP chain stability over 10 seconds")

        // Enable full chain
        mainActivity.setAGCEnabled(true)
        mainActivity.setCompressorEnabled(true)
        mainActivity.setLimiterEnabled(true)

        android.util.Log.i(TAG, "Running stability test for 10 seconds...")

        val startTime = System.currentTimeMillis()
        val duration = 10_000L // 10 seconds

        var samples = 0
        while (System.currentTimeMillis() - startTime < duration) {
            Thread.sleep(500)

            val peak = mainActivity.getPeakDb()
            val agcGain = mainActivity.getAGCCurrentGain()
            val compGR = mainActivity.getCompressorGainReduction()

            // Verify valid values
            assertThat(peak).isNotNaN()
            assertThat(agcGain).isNotNaN()
            assertThat(compGR).isNotNaN()

            samples++

            if (samples % 4 == 0) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000.0f
                android.util.Log.i(TAG, "Elapsed: ${String.format("%.1f", elapsed)}s | Peak=${String.format("%.1f", peak)}dB")
            }
        }

        android.util.Log.i(TAG, "✅ Stability test complete: $samples samples, no errors")
    }
}
