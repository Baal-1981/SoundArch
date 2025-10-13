# Phase 3: Performance Optimization Plan

**Date:** 2025-10-12
**Phase:** Performance & Memory Optimization
**Status:** 🚀 READY TO START
**Estimated Time:** 6-10 hours

---

## Executive Summary

Phase 3 focuses on **performance optimization** to reduce latency, memory usage, and improve overall app responsiveness. This builds on the solid foundation of Phase 1-2 (infrastructure, tests).

**Prerequisites:**
- ✅ Phase 1: Test infrastructure complete
- ✅ Phase 2: All tests passing (182 tests)
- ✅ Logging integrated (all 15 screens)
- ✅ Build successful

**Phase 3 Goals:**
1. Analyze and fix memory leaks
2. Optimize CPU usage and reduce latency
3. Improve build and test execution times
4. Profile and optimize composables
5. Reduce APK size

---

## 📋 Phase 3 Tasks Overview

| # | Task | Est. Time | Priority | Status |
|---|------|-----------|----------|--------|
| 1 | Memory leak detection & analysis | 1-2 hours | P0 | ⏳ Pending |
| 2 | Fix identified memory leaks | 2-3 hours | P0 | ⏳ Pending |
| 3 | Profile CPU usage & optimize hotspots | 2-3 hours | P1 | ⏳ Pending |
| 4 | Optimize Compose recompositions | 1-2 hours | P1 | ⏳ Pending |
| 5 | Build time optimization | 30-60 min | P2 | ⏳ Pending |
| 6 | Test execution optimization | 30-60 min | P2 | ⏳ Pending |
| 7 | APK size reduction | 30-60 min | P2 | ⏳ Pending |

**Total Estimated Time:** 6-10 hours

---

## Task 1: Memory Leak Detection & Analysis

**Est. Time:** 1-2 hours
**Priority:** P0 - CRITICAL
**Status:** ⏳ PENDING

### Objective
Identify all memory leaks using LeakCanary and Android Profiler.

### Implementation Steps

**1. Add LeakCanary Dependency**
```kotlin
// app/build.gradle.kts
dependencies {
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
```

**2. Run App with LeakCanary**
```bash
./gradlew.bat installDebug
# Launch app, navigate through all screens
# Wait for LeakCanary notifications
```

**3. Profile with Android Profiler**
- Open Android Studio → Profiler
- Record Memory allocation
- Navigate through all screens
- Check for:
  - Growing heap size
  - Unreleased Activity instances
  - Leaked ViewModels
  - Leaked Composables

**4. Document Findings**
Create `MEMORY_LEAK_ANALYSIS.md` with:
- List of all leaks found
- Heap dumps
- Suspect allocations
- Priority (P0/P1/P2)

### Success Criteria
- ✅ LeakCanary installed and running
- ✅ All screens profiled
- ✅ Memory leaks documented
- ✅ Prioritized fix list created

---

## Task 2: Fix Identified Memory Leaks

**Est. Time:** 2-3 hours
**Priority:** P0 - CRITICAL
**Status:** ⏳ PENDING

### Common Memory Leak Patterns in Android

**1. Activity/Fragment Leaks**
```kotlin
// BEFORE (LEAKS):
class MyViewModel : ViewModel() {
    private val activity: Activity // Leaked reference
}

// AFTER (FIXED):
class MyViewModel : ViewModel() {
    private val applicationContext: Context // Use application context
}
```

**2. Listener Leaks**
```kotlin
// BEFORE (LEAKS):
override fun onCreate() {
    someService.addListener(this) // Never removed
}

// AFTER (FIXED):
override fun onCreate() {
    someService.addListener(listener)
}

override fun onDestroy() {
    someService.removeListener(listener) // Remove on destroy
}
```

**3. Coroutine Leaks**
```kotlin
// BEFORE (LEAKS):
GlobalScope.launch {
    // Runs forever, holds references
}

// AFTER (FIXED):
viewModelScope.launch {
    // Cancelled when ViewModel cleared
}
```

**4. Compose Remember Leaks**
```kotlin
// BEFORE (POTENTIAL LEAK):
@Composable
fun MyScreen() {
    val heavyObject = remember { HeavyObject() } // Never released
}

// AFTER (FIXED):
@Composable
fun MyScreen() {
    val heavyObject = remember { HeavyObject() }
    DisposableEffect(Unit) {
        onDispose {
            heavyObject.cleanup() // Cleanup on dispose
        }
    }
}
```

### Fix Process
1. Start with P0 leaks (Activity/ViewModel leaks)
2. Fix P1 leaks (Resource leaks - bitmaps, streams)
3. Fix P2 leaks (Minor leaks with low impact)
4. Re-profile after each fix
5. Verify no new leaks introduced

### Success Criteria
- ✅ All P0 memory leaks fixed
- ✅ Re-profiled with LeakCanary (0 leaks)
- ✅ Heap size stable after navigation
- ✅ No Activity/ViewModel leaks

---

## Task 3: Profile CPU Usage & Optimize Hotspots

**Est. Time:** 2-3 hours
**Priority:** P1 - IMPORTANT
**Status:** ⏳ PENDING

### Objective
Identify CPU hotspots and optimize expensive operations.

### Implementation Steps

**1. CPU Profiling**
```bash
# Profile app with Android Studio CPU Profiler
# Focus on:
# - UI thread hotspots
# - Main thread blocking
# - Heavy DSP operations on UI thread
```

**2. Common Optimizations**

**Move DSP Processing Off UI Thread:**
```kotlin
// BEFORE (BAD):
@Composable
fun EqualizerScreen() {
    val eqCurve = calculateEqCurve(bands) // Heavy computation on UI thread
}

// AFTER (GOOD):
@Composable
fun EqualizerScreen() {
    val eqCurve by produceState(initialValue = emptyList(), bands) {
        value = withContext(Dispatchers.Default) {
            calculateEqCurve(bands) // Off UI thread
        }
    }
}
```

**Debounce Frequent Updates:**
```kotlin
// BEFORE (BAD):
Slider(
    onValueChange = { value ->
        viewModel.updateThreshold(value) // Every frame!
    }
)

// AFTER (GOOD):
val debouncedUpdate = rememberDebouncedCallback(300) { value: Float ->
    viewModel.updateThreshold(value)
}

Slider(
    onValueChange = debouncedUpdate
)
```

**Cache Expensive Calculations:**
```kotlin
// BEFORE (BAD):
@Composable
fun StatusBadge() {
    val color = calculateComplexColor() // Recalculated every recomposition
}

// AFTER (GOOD):
@Composable
fun StatusBadge() {
    val color = remember(key1) { calculateComplexColor() } // Cached
}
```

### Success Criteria
- ✅ UI thread usage < 50% during normal operation
- ✅ No frame drops during slider interactions
- ✅ DSP calculations moved off UI thread
- ✅ Frequent updates debounced

---

## Task 4: Optimize Compose Recompositions

**Est. Time:** 1-2 hours
**Priority:** P1 - IMPORTANT
**Status:** ⏳ PENDING

### Objective
Reduce unnecessary recompositions using Compose best practices.

### Implementation Steps

**1. Use derivedStateOf for Computed Values**
```kotlin
// BEFORE (RECOMPOSES OFTEN):
@Composable
fun Screen(items: List<Item>) {
    val filteredItems = items.filter { it.isActive } // Every recomposition
}

// AFTER (OPTIMIZED):
@Composable
fun Screen(items: List<Item>) {
    val filteredItems = remember(items) {
        derivedStateOf { items.filter { it.isActive } }
    }.value // Only when items change
}
```

**2. Use Stable Data Classes**
```kotlin
// BEFORE (UNSTABLE):
data class UiState(
    val items: List<Item> // Unstable
)

// AFTER (STABLE):
@Immutable
data class UiState(
    val items: ImmutableList<Item> // Stable, won't trigger recomposition
)
```

**3. Split Large Composables**
```kotlin
// BEFORE (LARGE COMPOSABLE):
@Composable
fun HomeScreen(state: UiState) {
    // 500 lines of UI code
    // Small state change recomposes everything
}

// AFTER (SPLIT):
@Composable
fun HomeScreen(state: UiState) {
    Header(state.header) // Only recomposes if header changes
    Body(state.body)     // Only recomposes if body changes
    Footer(state.footer) // Only recomposes if footer changes
}
```

**4. Use Composition Locals Wisely**
```kotlin
// BEFORE (PROP DRILLING):
@Composable
fun Screen(theme: Theme, locale: Locale, ...) {
    // Pass to every child
}

// AFTER (COMPOSITION LOCAL):
val LocalTheme = compositionLocalOf { LightTheme }

@Composable
fun Screen() {
    CompositionLocalProvider(LocalTheme provides theme) {
        // Children access theme directly
    }
}
```

### Success Criteria
- ✅ Recomposition rate reduced by 50%+
- ✅ No full-screen recompositions on slider changes
- ✅ Large composables split into smaller functions
- ✅ Stable data classes used for UI state

---

## Task 5: Build Time Optimization

**Est. Time:** 30-60 min
**Priority:** P2 - NICE TO HAVE
**Status:** ⏳ PENDING

### Objective
Reduce clean build time from 18s to < 12s.

### Implementation Steps

**1. Enable Gradle Build Cache**
```kotlin
// gradle.properties
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configureondemand=true
```

**2. Optimize Kapt (Hilt/Dagger)**
```kotlin
// app/build.gradle.kts
kapt {
    correctErrorTypes = true
    useBuildCache = true
    arguments {
        arg("dagger.fastInit", "enabled")
    }
}
```

**3. Use Kotlin IR Compiler** (if not already enabled)
```kotlin
// gradle.properties
kotlin.useIR=true
```

**4. Modularize If Beneficial**
- Consider splitting large modules
- Parallel build execution

### Success Criteria
- ✅ Clean build time < 12s
- ✅ Incremental build time < 3s
- ✅ Build cache enabled

---

## Task 6: Test Execution Optimization

**Est. Time:** 30-60 min
**Priority:** P2 - NICE TO HAVE
**Status:** ⏳ PENDING

### Objective
Reduce test execution time from 650s to < 400s.

### Implementation Steps

**1. Parallelize Tests**
```kotlin
// app/build.gradle.kts
android {
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}
```

**2. Use Fake Dependencies**
```kotlin
// BEFORE (SLOW):
class IntegrationTest {
    val realDatabase = Room.databaseBuilder(...) // Slow
}

// AFTER (FAST):
class IntegrationTest {
    val fakeDatabase = FakeDatabase() // In-memory, fast
}
```

**3. Reduce Thread.sleep() in Tests**
```kotlin
// BEFORE (SLOW):
Thread.sleep(1000) // 1 second per test

// AFTER (FAST):
composeTestRule.waitForIdle() // As fast as needed
advanceTimeBy(1000) // Virtual time
```

### Success Criteria
- ✅ Test execution time < 400s
- ✅ Tests run in parallel
- ✅ Minimal Thread.sleep() usage

---

## Task 7: APK Size Reduction

**Est. Time:** 30-60 min
**Priority:** P2 - NICE TO HAVE
**Status:** ⏳ PENDING

### Objective
Reduce APK size by optimizing resources and dependencies.

### Implementation Steps

**1. Enable R8 Full Mode**
```kotlin
// app/build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    }
}
```

**2. Remove Unused Resources**
```bash
./gradlew.bat :app:analyzeDebugResources
# Review unused resources
```

**3. Enable Vector Drawable Support**
```kotlin
defaultConfig {
    vectorDrawables.useSupportLibrary = true
}
```

**4. Use App Bundles**
```bash
./gradlew.bat :app:bundleRelease
```

### Success Criteria
- ✅ APK size reduced by 10%+
- ✅ R8 full mode enabled
- ✅ Unused resources removed

---

## Phase 3 Success Criteria

| Criterion | Status |
|-----------|--------|
| Zero P0 memory leaks | ⏳ Pending |
| UI thread usage < 50% | ⏳ Pending |
| Build time < 12s | ⏳ Pending |
| Test execution < 400s | ⏳ Pending |
| Recomposition rate reduced 50% | ⏳ Pending |
| APK size reduced 10% | ⏳ Pending |

---

## Phase 3 Metrics

### Before Optimization (Baseline)
- Memory leaks: TBD
- UI thread usage: TBD
- Build time: 18s (clean)
- Test execution: 650s
- APK size: ~15MB

### After Optimization (Target)
- Memory leaks: 0
- UI thread usage: < 50%
- Build time: < 12s
- Test execution: < 400s
- APK size: < 13.5MB

---

**Status:** 🚀 READY TO START PHASE 3
**Next Action:** Task 1 - Install LeakCanary and profile memory
**Dependencies:** Phase 1 ✅ Complete, Phase 2 ✅ Complete

Co-Authored-By: Math&Brie
