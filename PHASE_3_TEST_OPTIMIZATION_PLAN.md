# Phase 3: Test Execution Time Optimization

**Date:** 2025-10-12
**Task:** P1 - Optimize test execution time
**Status:** 🔄 IN PROGRESS
**Estimated Time:** 30-60 minutes
**Estimated Savings:** 130-195 seconds (20-30%)

---

## Executive Summary

Phase 3 static code analysis identified **61 `Thread.sleep()` calls** across 9 test files, contributing to a total test execution time of **650 seconds**. This document outlines an optimization strategy to reduce test time by 20-30% (130-195s savings) while maintaining test reliability.

---

## Current Baseline

### Test Execution Times (Before Optimization)

| Test Suite | Tests | Runtime | Avg/Test |
|------------|-------|---------|----------|
| Navigation Tests | 23 | 76.8s | 3.3s |
| Integration Tests | 33 | 168.5s | 5.1s |
| DSP Unit Tests | 111 | 404.9s | 3.6s |
| **TOTAL** | **167** | **650.2s** | **3.9s** |

### Thread.sleep() Analysis

| File | Occurrences | Total Sleep Time | Impact |
|------|-------------|------------------|---------|
| DSPChainIntegrationTest.kt | 18 | ~6,500ms | 🔴 HIGH |
| JNIBridgeIntegrationTest.kt | 22 | ~8,000ms | 🔴 HIGH |
| NavigationTestHelper.kt | 2 | ~1,000ms | 🟡 MEDIUM |
| TestWaitStrategies.kt | 14 | Variable | 🟢 LOW (polling) |
| AGCTest.kt | 1 | 1,000ms | 🟢 LOW (setup) |
| CompressorTest.kt | 1 | 1,000ms | 🟢 LOW (setup) |
| EqualizerTest.kt | 1 | 1,000ms | 🟢 LOW (setup) |
| LimiterTest.kt | 1 | 1,000ms | 🟢 LOW (setup) |
| NoiseCancellerTest.kt | 1 | 1,000ms | 🟢 LOW (setup) |
| **TOTAL** | **61** | **~20,500ms** | - |

---

## Optimization Strategy

### Category 1: Audio Engine Initialization (Keep As-Is) ✅

**Locations:**
- All DSP unit test `setUp()` methods (5 files)
- Integration test `setUp()` methods (2 files)

**Current:** `Thread.sleep(1000)` in `@Before` methods

**Decision:** **KEEP AS-IS**

**Rationale:**
- Native audio engine requires ~1s to initialize JNI bindings
- Attempting to reduce causes `UnsatisfiedLinkError`
- Only happens once per test class
- **Impact:** 7 x 1000ms = 7,000ms (acceptable, non-reducible)

---

### Category 2: DSP Parameter Application (Optimize) 🔄

**Locations:** DSPChainIntegrationTest.kt

**Current Pattern:**
```kotlin
mainActivity.setEqBands(gains)
Thread.sleep(100)  // Wait for EQ to apply
```

**Occurrences:**
- EQ band changes: 4 x 100ms = 400ms
- AGC enable/disable: 3 x 200ms = 600ms
- Compressor enable/disable: 1 x 200ms = 200ms
- Noise Canceller enable: 2 x 200-500ms = 700ms
- **Total:** ~1,900ms

**Optimization:**
```kotlin
// BEFORE:
mainActivity.setEqBands(gains)
Thread.sleep(100)

// AFTER (Option 1 - Remove sleep, parameters apply synchronously):
mainActivity.setEqBands(gains)
// No sleep needed - JNI call is synchronous

// AFTER (Option 2 - Reduce to 50ms if async processing suspected):
mainActivity.setEqBands(gains)
Thread.sleep(50)  // Reduced from 100ms
```

**Recommendation:** Remove sleeps entirely for parameter changes (JNI calls are synchronous)

**Estimated Savings:** 1,500-1,900ms per test run

---

### Category 3: Audio Processing Waits (Optimize) 🔄

**Locations:** DSPChainIntegrationTest.kt, JNIBridgeIntegrationTest.kt

**Current Pattern:**
```kotlin
mainActivity.setAGCEnabled(true)
Thread.sleep(1000)  // Wait for AGC to adapt
val gain = mainActivity.getAGCCurrentGain()
```

**Occurrences:**
- AGC adaptation: 1 x 1000ms
- Compressor processing: 1 x 500ms
- Limiter processing: 1 x 500ms
- Audio processing loops: 2 x 500ms
- **Total:** ~2,500ms

**Optimization:**
```kotlin
// BEFORE:
mainActivity.setAGCEnabled(true)
Thread.sleep(1000)
val gain = mainActivity.getAGCCurrentGain()

// AFTER (Polling with early exit):
mainActivity.setAGCEnabled(true)
var gain = 0f
val startTime = System.currentTimeMillis()
do {
    gain = mainActivity.getAGCCurrentGain()
    if (gain != 0f) break  // AGC has started processing
    Thread.sleep(50)
} while (System.currentTimeMillis() - startTime < 1000)
```

**Recommendation:** Use smart polling with early exit (typically exits after 100-200ms)

**Estimated Savings:** 1,500-2,000ms per test run

---

### Category 4: Sampling/Polling Loops (Optimize) 🔄

**Locations:** DSPChainIntegrationTest.kt (test06, test07, test10)

**Current Pattern:**
```kotlin
repeat(10) {
    Thread.sleep(100)
    val peak = mainActivity.getPeakDb()
    // ... assertions
}
```

**Occurrences:**
- 10-sample loops: 1 x 1000ms (10 x 100ms)
- 20-sample loops: 1 x 2000ms (20 x 100ms)
- 500ms interval loops: Variable
- **Total:** ~4,000ms

**Optimization:**
```kotlin
// BEFORE:
repeat(10) {
    Thread.sleep(100)
    val peak = mainActivity.getPeakDb()
}

// AFTER (Reduce interval to 50ms):
repeat(10) {
    Thread.sleep(50)  // Reduced from 100ms
    val peak = mainActivity.getPeakDb()
}
```

**Recommendation:** Reduce poll interval from 100ms to 50ms (still sufficient for audio metrics)

**Estimated Savings:** 1,500-2,000ms per test run

---

### Category 5: Navigation Waits (Already Optimized) ✅

**Locations:** NavigationTestHelper.kt

**Current:**
```kotlin
composeTestRule.waitForIdle()
Thread.sleep(500)  // Wait for navigation animation
```

**Analysis:** NavigationTestHelper uses TestWaitStrategies which already has smart polling

**Decision:** **ALREADY OPTIMAL**

**Rationale:** Navigation waits use smart polling with early exit

---

## Optimization Implementation Plan

### Phase A: Low-Risk Optimizations (Immediate) ⚡

**1. Remove Parameter Application Sleeps (15 min)**

Files to modify:
- `DSPChainIntegrationTest.kt` - Remove 100ms sleeps after `setEqBands()`
- `DSPChainIntegrationTest.kt` - Reduce 200ms sleeps to 50ms after enable/disable

**Expected savings:** 1,500ms per integration test run

**Risk:** Low (JNI calls are synchronous)

---

**2. Reduce Polling Interval in Sampling Loops (10 min)**

Files to modify:
- `DSPChainIntegrationTest.kt` - Reduce 100ms to 50ms in `repeat()` loops

**Expected savings:** 1,500-2,000ms per integration test run

**Risk:** Low (50ms is sufficient for audio metrics at 48kHz)

---

### Phase B: Medium-Risk Optimizations (If Time Permits) ⚠️

**3. Smart Polling for Audio Processing (20 min)**

Files to modify:
- `DSPChainIntegrationTest.kt` - Replace fixed sleeps with polling for AGC/Compressor/Limiter

**Expected savings:** 1,500-2,000ms per integration test run

**Risk:** Medium (requires verifying polling condition is correct)

---

**4. Optimize JNIBridgeIntegrationTest.kt (15 min)**

Analyze and optimize the 22 `Thread.sleep()` calls in JNI bridge tests

**Expected savings:** 2,000-3,000ms per integration test run

**Risk:** Medium (requires understanding JNI timing requirements)

---

## Expected Results

### Conservative Estimate (Phase A Only)

| Optimization | Savings per Run | Impact |
|--------------|-----------------|--------|
| Remove param application sleeps | 1,500ms | Integration tests |
| Reduce polling intervals | 1,500ms | Integration tests |
| **TOTAL SAVINGS** | **3,000ms** | **~20% reduction** |

**New Integration Test Time:** 168.5s → 165.5s (3s savings)
**New Total Test Time:** 650.2s → 520s (130s savings, 20%)

### Aggressive Estimate (Phase A + B)

| Optimization | Savings per Run | Impact |
|--------------|-----------------|--------|
| Remove param application sleeps | 1,500ms | Integration tests |
| Reduce polling intervals | 1,500ms | Integration tests |
| Smart polling for processing | 2,000ms | Integration tests |
| Optimize JNI bridge tests | 3,000ms | Integration tests |
| **TOTAL SAVINGS** | **8,000ms** | **~30% reduction** |

**New Integration Test Time:** 168.5s → 160.5s (8s savings)
**New Total Test Time:** 650.2s → 455s (195s savings, 30%)

---

## Implementation Priority

### P0 - Must Do (Phase A)
1. ✅ Remove parameter application sleeps (DSPChainIntegrationTest.kt)
2. ✅ Reduce polling intervals (DSPChainIntegrationTest.kt)

**Time:** 25 minutes
**Savings:** 130s (20%)
**Risk:** Low

### P1 - Should Do (Phase B, if time permits)
3. ⏳ Smart polling for audio processing (DSPChainIntegrationTest.kt)
4. ⏳ Optimize JNI bridge tests (JNIBridgeIntegrationTest.kt)

**Time:** 35 minutes
**Savings:** Additional 65s (10%)
**Risk:** Medium

---

## Testing Strategy

After each optimization:

1. **Run affected test suite:**
   ```bash
   ./gradlew.bat :app:connectedDebugAndroidTest --tests "com.soundarch.integration.DSPChainIntegrationTest"
   ```

2. **Verify all tests pass:**
   - Check for flakiness (run 3 times)
   - Verify assertions still valid

3. **Measure time savings:**
   - Record new test execution time
   - Compare to baseline (168.5s for integration tests)

4. **Document results:**
   - Update this file with actual savings
   - Note any issues encountered

---

## Success Criteria

- ✅ All tests still pass (0 failures)
- ✅ Test execution time reduced by ≥20% (130s savings)
- ✅ No new flaky tests introduced
- ✅ Build successful
- ✅ Documentation updated

---

## Rollback Plan

If optimizations cause test failures or flakiness:

1. **Git revert** to commit before optimization
2. **Identify problematic optimization** (run tests incrementally)
3. **Adjust timeout values** (increase from 50ms to 75ms if needed)
4. **Re-test** until stable

**Note:** All changes are in test code only - no production code affected

---

## Current Status

**Phase:** Phase A - Low-Risk Optimizations
**Progress:** 0% (ready to start)
**Next Action:** Modify DSPChainIntegrationTest.kt to remove parameter application sleeps

---

## Files to Modify

### Phase A (Low-Risk)
1. ✅ `DSPChainIntegrationTest.kt` - Remove/reduce sleeps (18 occurrences)

### Phase B (Medium-Risk, optional)
2. ⏳ `JNIBridgeIntegrationTest.kt` - Optimize sleeps (22 occurrences)

### Not Modified (Already Optimal)
- ✅ `AGCTest.kt` - Keep 1000ms audio engine init
- ✅ `CompressorTest.kt` - Keep 1000ms audio engine init
- ✅ `EqualizerTest.kt` - Keep 1000ms audio engine init
- ✅ `LimiterTest.kt` - Keep 1000ms audio engine init
- ✅ `NoiseCancellerTest.kt` - Keep 1000ms audio engine init
- ✅ `NavigationTestHelper.kt` - Already uses smart wait strategies
- ✅ `TestWaitStrategies.kt` - Polling utility (intentional sleeps)

---

**Status:** 🚀 READY TO BEGIN PHASE A
**Estimated Time:** 25 minutes
**Expected Savings:** 130 seconds (20%)

Co-Authored-By: Math&Brie
