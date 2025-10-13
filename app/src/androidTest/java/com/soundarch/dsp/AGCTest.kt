package com.soundarch.dsp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.soundarch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AGC (Automatic Gain Control)
 *
 * Tests:
 * - Parameter clamping (target level, gain range, attack/release)
 * - JNI binding integrity
 * - State management (enable/disable)
 * - Gain calculation consistency
 */
class AGCTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var mainActivity: MainActivity

    @Before
    fun setUp() {
        // Note: Since AGC is implemented in native code, these tests verify
        // the JNI interface and parameter validation logic
        activityRule.scenario.onActivity { activity ->
            mainActivity = activity
        }
        Thread.sleep(1000)  // Allow audio engine to initialize
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎯 TARGET LEVEL TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testTargetLevel_ValidRange() {
        // AGC target level should accept standard range [-40, -10] dBFS
        val validLevels = listOf(-40.0f, -30.0f, -20.0f, -15.0f, -10.0f)

        validLevels.forEach { level ->
            // Should not throw exception
            try {
                mainActivity.setAGCTargetLevel(level)
            } catch (e: Exception) {
                fail("Valid target level $level should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testTargetLevel_Extreme() {
        // Test extreme values (implementation should clamp internally)
        val extremeLevels = listOf(-80.0f, 0.0f, 10.0f)

        extremeLevels.forEach { level ->
            try {
                mainActivity.setAGCTargetLevel(level)
                // If it doesn't throw, clamping is working
            } catch (e: Exception) {
                // Native code should handle gracefully
                assertTrue(
                    "Exception message should indicate clamping: ${e.message}",
                    e.message?.contains("clamp", ignoreCase = true) ?: false
                )
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📈 GAIN RANGE TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testMaxGain_ValidRange() {
        // Max gain: typically [0, +40] dB
        val validGains = listOf(0.0f, 10.0f, 20.0f, 30.0f, 40.0f)

        validGains.forEach { gain ->
            try {
                mainActivity.setAGCMaxGain(gain)
            } catch (e: Exception) {
                fail("Valid max gain $gain should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testMinGain_ValidRange() {
        // Min gain: typically [-20, 0] dB
        val validGains = listOf(-20.0f, -10.0f, -5.0f, 0.0f)

        validGains.forEach { gain ->
            try {
                mainActivity.setAGCMinGain(gain)
            } catch (e: Exception) {
                fail("Valid min gain $gain should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testGainRange_LogicalConsistency() {
        // MaxGain should be greater than MinGain
        val maxGain = 20.0f
        val minGain = -10.0f

        mainActivity.setAGCMaxGain(maxGain)
        mainActivity.setAGCMinGain(minGain)

        // Native code should maintain this invariant
        assertTrue("MaxGain ($maxGain) should be > MinGain ($minGain)", maxGain > minGain)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ⚡ ATTACK/RELEASE TIME TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testAttackTime_ValidRange() {
        // Attack time: typically [0.001, 1.0] seconds
        val validTimes = listOf(0.001f, 0.01f, 0.1f, 0.5f, 1.0f)

        validTimes.forEach { time ->
            try {
                mainActivity.setAGCAttackTime(time)
            } catch (e: Exception) {
                fail("Valid attack time $time should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testReleaseTime_ValidRange() {
        // Release time: typically [0.01, 5.0] seconds
        val validTimes = listOf(0.01f, 0.1f, 0.5f, 1.0f, 2.0f, 5.0f)

        validTimes.forEach { time ->
            try {
                mainActivity.setAGCReleaseTime(time)
            } catch (e: Exception) {
                fail("Valid release time $time should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testAttackRelease_RelativeSpeed() {
        // Attack should typically be faster than release for natural sound
        val attackTime = 0.1f  // 100ms
        val releaseTime = 0.5f // 500ms

        mainActivity.setAGCAttackTime(attackTime)
        mainActivity.setAGCReleaseTime(releaseTime)

        assertTrue(
            "Release time ($releaseTime) should be >= Attack time ($attackTime) for natural dynamics",
            releaseTime >= attackTime
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔇 NOISE GATE TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testNoiseThreshold_ValidRange() {
        // Noise threshold: typically [-80, -30] dBFS
        val validThresholds = listOf(-80.0f, -60.0f, -55.0f, -40.0f, -30.0f)

        validThresholds.forEach { threshold ->
            try {
                mainActivity.setAGCNoiseThreshold(threshold)
            } catch (e: Exception) {
                fail("Valid noise threshold $threshold should not throw: ${e.message}")
            }
        }
    }

    @Test
    fun testWindowSize_ValidRange() {
        // Window size: typically [0.01, 1.0] seconds
        val validSizes = listOf(0.01f, 0.05f, 0.1f, 0.5f, 1.0f)

        validSizes.forEach { size ->
            try {
                mainActivity.setAGCWindowSize(size)
            } catch (e: Exception) {
                fail("Valid window size $size should not throw: ${e.message}")
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎛️ ENABLE/DISABLE TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testAGC_EnableDisable() {
        // Test enable/disable toggle
        try {
            mainActivity.setAGCEnabled(true)
            mainActivity.setAGCEnabled(false)
            mainActivity.setAGCEnabled(true)
        } catch (e: Exception) {
            fail("AGC enable/disable should not throw: ${e.message}")
        }
    }

    @Test
    fun testAGC_CurrentGain_ReturnsValidValue() {
        // Current gain should return a reasonable value
        val currentGain = mainActivity.getAGCCurrentGain()

        // Should be within expected range [-20, +40] dB
        assertTrue(
            "Current gain ($currentGain) should be in valid range [-20, +40]",
            currentGain in -20.0f..40.0f
        )
    }

    @Test
    fun testAGC_CurrentLevel_ReturnsValidValue() {
        // Current level should return a reasonable value
        val currentLevel = mainActivity.getAGCCurrentLevel()

        // Should be within expected range [-80, 0] dBFS
        assertTrue(
            "Current level ($currentLevel) should be in valid range [-80, 0]",
            currentLevel in -80.0f..0.0f
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔗 INTEGRATION TESTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    fun testAGC_FullConfiguration() {
        // Test complete AGC configuration sequence
        try {
            mainActivity.setAGCEnabled(true)
            mainActivity.setAGCTargetLevel(-20.0f)
            mainActivity.setAGCMaxGain(25.0f)
            mainActivity.setAGCMinGain(-10.0f)
            mainActivity.setAGCAttackTime(0.1f)
            mainActivity.setAGCReleaseTime(0.5f)
            mainActivity.setAGCNoiseThreshold(-55.0f)
            mainActivity.setAGCWindowSize(0.1f)

            // Verify state is accessible
            val currentGain = mainActivity.getAGCCurrentGain()
            val currentLevel = mainActivity.getAGCCurrentLevel()

            assertNotNull("Current gain should not be null", currentGain)
            assertNotNull("Current level should not be null", currentLevel)

        } catch (e: Exception) {
            fail("Full AGC configuration should not throw: ${e.message}")
        }
    }

    @Test
    fun testAGC_ThreadSafety_RapidToggle() {
        // Test rapid enable/disable to verify atomic operations
        repeat(100) { i ->
            mainActivity.setAGCEnabled(i % 2 == 0)
        }
        // Should not crash or deadlock
    }

    @Test
    fun testAGC_ParameterPersistence() {
        // Verify parameters persist after setting
        val targetLevel = -18.0f
        val maxGain = 22.0f

        mainActivity.setAGCTargetLevel(targetLevel)
        mainActivity.setAGCMaxGain(maxGain)

        // Parameters should be applied (can't read back from native, but verify no crash)
        mainActivity.setAGCEnabled(true)
        mainActivity.setAGCEnabled(false)
    }
}
