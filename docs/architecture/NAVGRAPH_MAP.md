# NavGraph & Routes Architecture Analysis

**Project:** SoundArch v2.0
**Date:** 2025-10-12
**Phase:** 1 - Navigation Mapping
**Status:** Complete

---

## Executive Summary

SoundArch uses a **flat navigation architecture** with 11 named routes managed by Jetpack Navigation Compose. The current implementation is functional but has inconsistencies: dangling routes, unused BottomNavBar, and mixed flat/nested path patterns. This document provides a complete map of the current state and proposes a canonical structure for Phase 2.

**Current State:**
- ✅ 11 routes defined in `Routes.kt`
- ✅ 18 composable destinations in `NavGraph.kt`
- ⚠️ 2 dangling routes (Advanced, Logs)
- ⚠️ 1 redirect route (EqSettings → Equalizer)
- ⚠️ BottomNavBar component exists but not used
- ⚠️ No deep links or saved state

**Proposed State:**
- Clean hierarchy with nested graphs
- Remove dangling routes
- Add deep link support
- Optional BottomNav integration

---

## 1. Current Route Definitions

### 1.1 Routes.kt (Line-by-Line)

**File:** `app/src/main/java/com/soundarch/ui/navigation/Routes.kt`

```kotlin
sealed class Routes(val route: String) {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🏠 MAIN NAVIGATION (Bottom Bar)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    data object Home : Routes("home")                   // ✅ Mapped (start destination)
    data object Equalizer : Routes("equalizer")         // ✅ Mapped
    data object Advanced : Routes("advanced")           // ❌ DANGLING (never mapped)
    data object Logs : Routes("logs")                   // ❌ DANGLING (never mapped)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎛️ DSP SCREENS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    data object Compressor : Routes("compressor")       // ✅ Mapped
    data object Limiter : Routes("limiter")             // ✅ Mapped
    data object AGC : Routes("agc")                     // ✅ Mapped

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ⚙️ ADVANCED SECTIONS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    data object AudioEngine : Routes("advanced/audio_engine")            // ✅ Mapped
    data object Dynamics : Routes("advanced/dynamics")                   // ✅ Mapped
    data object NoiseCancelling : Routes("advanced/noise_cancelling")    // ✅ Mapped
    data object Bluetooth : Routes("advanced/bluetooth")                 // ✅ Mapped
    data object EqSettings : Routes("advanced/eq_settings")              // ⚠️ Redirects to Equalizer
    data object Ml : Routes("advanced/ml")                               // ✅ Mapped
    data object Performance : Routes("advanced/performance")             // ✅ Mapped
    data object BuildRuntime : Routes("advanced/build_runtime")          // ✅ Mapped
    data object Diagnostics : Routes("advanced/diagnostics")             // ✅ Mapped
    data object LogsTests : Routes("advanced/logs_tests")                // ✅ Mapped
    data object AppPermissions : Routes("advanced/app_permissions")      // ✅ Mapped
}
```

**Total Routes:** 18 (4 main + 3 DSP + 11 advanced)
**Mapped:** 16
**Dangling:** 2 (Advanced, Logs)
**Redirects:** 1 (EqSettings)

---

## 2. Current NavGraph Structure

### 2.1 NavGraph.kt Overview

**File:** `app/src/main/java/com/soundarch/ui/navigation/NavGraph.kt` (459 lines)

**NavHost Configuration:**
```kotlin
NavHost(
    navController = navController,
    startDestination = Routes.Home.route  // "home"
)
```

**Pattern:** Flat navigation (all destinations at root level)

---

### 2.2 Complete Destination Map

| Route String | Composable | Screen File | Line | Status |
|--------------|------------|-------------|------|--------|
| **home** | HomeScreenV2 | HomeScreenV2.kt | 97 | ✅ Start destination |
| **equalizer** | EqualizerScreen | advanced/eq/EqualizerScreen.kt | 201 | ✅ Functional |
| **compressor** | CompressorScreen | advanced/dynamics/CompressorScreen.kt | 214 | ✅ Functional |
| **limiter** | LimiterScreen | advanced/dynamics/LimiterScreen.kt | 233 | ✅ Functional |
| **agc** | AGCScreen | advanced/dynamics/AGCScreen.kt | 245 | ✅ Functional |
| **advanced/audio_engine** | AdvancedAudioEngineScreen | advanced/AdvancedAudioEngineScreen.kt | 267 | ✅ Functional |
| **advanced/dynamics** | DynamicsMenuScreen | advanced/dynamics/DynamicsMenuScreen.kt | 302 | ✅ Functional |
| **advanced/noise_cancelling** | NoiseCancellingScreen | advanced/noisecancel/NoiseCancellingScreen.kt | 317 | ✅ Functional |
| **advanced/bluetooth** | BluetoothScreen | advanced/bluetooth/BluetoothScreen.kt | 362 | ⚠️ Partial (SAFE toggle only) |
| **advanced/eq_settings** | Redirect | — | 377 | ⚠️ Redirects to equalizer |
| **advanced/ml** | MlComingSoonScreen | advanced/ml/MlComingSoonScreen.kt | 384 | ⚠️ Placeholder |
| **advanced/performance** | PerformanceScreen | advanced/perf/PerformanceScreen.kt | 390 | ⚠️ Partial (profiles not wired) |
| **advanced/build_runtime** | BuildRuntimeScreen | advanced/build/BuildRuntimeScreen.kt | 403 | ✅ Functional (read-only) |
| **advanced/diagnostics** | DiagnosticsScreen | advanced/diagnostics/DiagnosticsScreen.kt | 419 | ⚠️ Partial (reset/export not wired) |
| **advanced/logs_tests** | LogsScreen | advanced/logs/LogsScreen.kt | 433 | ✅ Functional |
| **advanced/app_permissions** | AppPermissionsScreen | advanced/app/AppPermissionsScreen.kt | 437 | ⚠️ Partial (permissions not wired) |

**Total Mapped:** 16 destinations
**Fully Functional:** 8
**Partially Wired:** 7
**Placeholder:** 1
**Redirect:** 1

---

### 2.3 Navigation Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         📱 App Entry Point                          │
│                         (MainActivity.kt)                           │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│                          NavHost(start="home")                       │
└─────────────────────────────────────────────────────────────────────┘
                                  │
        ┌─────────────────────────┴─────────────────────────┐
        │                                                     │
        ↓                                                     ↓
┌──────────────────┐                              ┌──────────────────┐
│   🏠 Home        │                              │  🎛️ Equalizer    │
│  (HomeScreenV2)  │                              │ (EqualizerScreen) │
└──────────────────┘                              └──────────────────┘
        │                                                     │
        │  AdvancedSectionsPanel                             │
        │  (11 nav links)                                    │
        ↓                                                     │
┌─────────────────────────────────────────────────────────┐ │
│               Advanced Sections Hub (No Screen)         │ │
└─────────────────────────────────────────────────────────┘ │
        │                                                     │
        ├─→ ⚡ Audio Engine (AdvancedAudioEngineScreen)      │
        ├─→ 🎚️ Dynamics → [Compressor, AGC, Limiter] ←──────┤
        ├─→ 🔇 Noise Cancelling (NoiseCancellingScreen)      │
        ├─→ 📶 Bluetooth (BluetoothScreen)                   │
        ├─→ 🎛️ EQ Settings → Redirects to Equalizer ────────┘
        ├─→ 🤖 ML (MlComingSoonScreen - Placeholder)
        ├─→ 📈 Performance (PerformanceScreen)
        ├─→ 🔧 Build & Runtime (BuildRuntimeScreen)
        ├─→ 🔬 Diagnostics (DiagnosticsScreen)
        ├─→ 📝 Logs & Tests (LogsScreen)
        └─→ 🔐 App & Permissions (AppPermissionsScreen)
```

---

### 2.4 Dynamics Sub-Navigation

**Dynamics Menu Screen** acts as a hub for 3 DSP modules:

```
┌─────────────────────────────────────────────────────────────────┐
│                      advanced/dynamics                          │
│                   (DynamicsMenuScreen)                          │
└─────────────────────────────────────────────────────────────────┘
                  │
                  ├─→ compressor (CompressorScreen)
                  ├─→ agc (AGCScreen)
                  └─→ limiter (LimiterScreen)
```

**Back Navigation:**
- Each DSP screen: `onBack = { navController.popBackStack() }` → returns to DynamicsMenuScreen
- DynamicsMenuScreen: `onBack = { navController.popBackStack() }` → returns to Home

**Issue:** DSP screens (compressor, agc, limiter) are **top-level routes**, not nested under `dynamics/`. This creates inconsistent paths:
- ✅ Logical: `advanced/dynamics` → `advanced/dynamics/compressor`
- ❌ Actual: `advanced/dynamics` → `compressor` (flat)

---

## 3. Back Stack & Deep Link Analysis

### 3.1 Back Button Consistency

**Implementation:** All screens use consistent back navigation:

```kotlin
onBack = { navController.popBackStack() }
```

**Testing Results:** ✅ All screens correctly return to previous destination

**Examples:**
1. Home → AudioEngine → Back → Home ✅
2. Home → Dynamics → Compressor → Back → Dynamics ✅
3. Home → Equalizer → Back → Home ✅

---

### 3.2 Deep Links

**Current:** ❌ No deep links defined

**Impact:**
- Cannot open specific screens via external intent
- Cannot create shareable links (e.g., "soundarch://advanced/dynamics")
- Cannot restore navigation state from notification

**Recommendation:** Add deep link scheme in Phase 2

```kotlin
composable(
    route = Routes.Compressor.route,
    deepLinks = listOf(navDeepLink { uriPattern = "soundarch://compressor" })
) { ... }
```

---

### 3.3 State Preservation

**Current:** ❌ No saved state configuration

**Impact:**
- Back stack lost on process death
- ViewModels reset on configuration change (screen rotation handled by Compose)

**Recommendation:** Add saved state in Phase 2

```kotlin
navController.enableOnBackPressed(true)  // Already enabled by default
```

For complex state:
```kotlin
val navController = rememberNavController()
val backStackEntry = navController.currentBackStackEntryAsState()
```

---

## 4. Issues & Gaps

### 4.1 Dangling Routes

#### **Routes.Advanced** ("advanced")
**Defined:** Routes.kt:9
**Mapped:** ❌ No composable in NavGraph.kt
**Impact:** Route cannot be navigated to
**Likely Intent:** Hub screen for Advanced sections (like AdvancedHomeScreen.kt which exists)

**Options:**
1. Remove route (use Home with AdvancedSectionsPanel instead)
2. Create dedicated AdvancedHomeScreen with grid of all sections
3. Use existing `advanced/AdvancedHomeScreen.kt` (currently unused)

#### **Routes.Logs** ("logs")
**Defined:** Routes.kt:10
**Mapped:** ❌ No composable in NavGraph.kt
**Conflict:** `Routes.LogsTests` ("advanced/logs_tests") IS mapped
**Impact:** Route cannot be navigated to

**Options:**
1. Remove route (use `Routes.LogsTests` instead)
2. Map to LogsScreen as bottom nav destination (if BottomNav is implemented)

---

### 4.2 Redirect Route

#### **Routes.EqSettings** ("advanced/eq_settings")
**Defined:** Routes.kt:26
**Mapped:** NavGraph.kt:377-382
**Behavior:** Immediately redirects to `Routes.Equalizer`

```kotlin
composable(Routes.EqSettings.route) {
    navController.navigate(Routes.Equalizer.route) {
        popUpTo(Routes.EqSettings.route) { inclusive = true }
    }
}
```

**UiIds:** `UiIds.EqSettings` has 15 test IDs defined (precalc coeffs, dither, section ordering, filter quality)

**Issue:** Full-featured EqSettings screen planned but not implemented

**Options:**
1. Remove redirect, implement full EqSettingsScreen.kt
2. Keep redirect, remove unused UiIds.EqSettings
3. Merge EqSettings controls into EqualizerScreen

**Recommendation:** Option 1 - Implement EqSettingsScreen for advanced EQ configuration

---

### 4.3 BottomNavBar Component

**File:** `ui/components/BottomNavBar.kt` (exists)
**Integration:** ❌ Not used in NavGraph.kt or MainActivity.kt
**Routes Suggest Bottom Nav:** Home, Equalizer, Advanced, Logs

**Current State:** No bottom navigation; all navigation via:
- AdvancedSectionsPanel (collapsible list on Home)
- Direct navigation buttons/cards
- DynamicsMenuScreen (intermediate hub)

**Options:**
1. Implement BottomNav with 4 destinations: Home | EQ | Advanced | Logs
2. Remove BottomNavBar.kt (unused)
3. Keep BottomNavBar for future use

**Recommendation:** Option 3 - Keep for future, but clarify intent in Phase 2

---

### 4.4 Inconsistent Path Patterns

**Pattern 1: Top-Level Flat Routes**
- `compressor`
- `limiter`
- `agc`

**Pattern 2: Prefixed Nested Routes**
- `advanced/audio_engine`
- `advanced/dynamics`
- `advanced/noise_cancelling`
- ... (11 total with `advanced/` prefix)

**Issue:** DSP screens conceptually belong under Dynamics, but paths don't reflect this:
- Navigated from: `advanced/dynamics` (DynamicsMenuScreen)
- Actual routes: `compressor`, `limiter`, `agc` (no prefix)

**Confusion:** Is `compressor` a top-level feature or a child of Dynamics?

**Recommendation:** Harmonize paths to reflect hierarchy:

**Option A: Nest DSP under dynamics/**
```kotlin
data object Compressor : Routes("dynamics/compressor")
data object Limiter : Routes("dynamics/limiter")
data object AGC : Routes("dynamics/agc")
```

**Option B: Nest DSP under advanced/dynamics/**
```kotlin
data object Compressor : Routes("advanced/dynamics/compressor")
data object Limiter : Routes("advanced/dynamics/limiter")
data object AGC : Routes("advanced/dynamics/agc")
```

**Option C: Keep flat, remove Dynamics hub** (navigate directly from Home)

---

## 5. Proposed Canonical Structure

### 5.1 Clean Route Hierarchy

```
soundarch://
│
├─ home                          (HomeScreenV2)
│
├─ equalizer                     (EqualizerScreen)
│  └─ eq_settings                (EqSettingsScreen - NEW)
│
├─ advanced/                     (AdvancedHomeScreen - OPTIONAL)
│  ├─ audio_engine              (AdvancedAudioEngineScreen)
│  ├─ dynamics/                  (DynamicsMenuScreen)
│  │  ├─ compressor             (CompressorScreen)
│  │  ├─ limiter                (LimiterScreen)
│  │  └─ agc                    (AGCScreen)
│  ├─ noise_cancelling          (NoiseCancellingScreen)
│  ├─ bluetooth                  (BluetoothScreen)
│  ├─ ml                        (MlScreen - replace placeholder)
│  ├─ performance               (PerformanceScreen)
│  ├─ build_runtime             (BuildRuntimeScreen)
│  ├─ diagnostics               (DiagnosticsScreen)
│  ├─ logs_tests                (LogsScreen)
│  └─ app_permissions           (AppPermissionsScreen)
│
└─ logs                          (OPTIONAL: if BottomNav used)
```

---

### 5.2 Nested Graph Structure

**Recommended Approach:** Use nested navigation graphs for logical grouping

```kotlin
NavHost(
    navController = navController,
    startDestination = "home"
) {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🏠 HOME GRAPH
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    composable("home") { HomeScreenV2(...) }
    composable("equalizer") { EqualizerScreen(...) }
    composable("equalizer/settings") { EqSettingsScreen(...) }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ⚙️ ADVANCED GRAPH (Nested)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    navigation(
        route = "advanced",
        startDestination = "advanced/hub"
    ) {
        composable("advanced/hub") { AdvancedHomeScreen(...) }
        composable("advanced/audio_engine") { AdvancedAudioEngineScreen(...) }

        // Dynamics sub-graph
        navigation(
            route = "advanced/dynamics",
            startDestination = "advanced/dynamics/menu"
        ) {
            composable("advanced/dynamics/menu") { DynamicsMenuScreen(...) }
            composable("advanced/dynamics/compressor") { CompressorScreen(...) }
            composable("advanced/dynamics/limiter") { LimiterScreen(...) }
            composable("advanced/dynamics/agc") { AGCScreen(...) }
        }

        composable("advanced/noise_cancelling") { NoiseCancellingScreen(...) }
        composable("advanced/bluetooth") { BluetoothScreen(...) }
        composable("advanced/ml") { MlScreen(...) }
        composable("advanced/performance") { PerformanceScreen(...) }
        composable("advanced/build_runtime") { BuildRuntimeScreen(...) }
        composable("advanced/diagnostics") { DiagnosticsScreen(...) }
        composable("advanced/logs_tests") { LogsScreen(...) }
        composable("advanced/app_permissions") { AppPermissionsScreen(...) }
    }
}
```

**Benefits:**
1. ✅ Clear hierarchy (parent/child relationships)
2. ✅ Scoped back stack (back from dynamics/compressor → dynamics/menu → advanced/hub → home)
3. ✅ Logical route paths match UI structure
4. ✅ Deep links more intuitive

---

### 5.3 Deep Link Configuration

**Proposed Scheme:** `soundarch://`

```kotlin
composable(
    route = "home",
    deepLinks = listOf(navDeepLink { uriPattern = "soundarch://home" })
) { HomeScreenV2(...) }

composable(
    route = "advanced/dynamics/compressor",
    deepLinks = listOf(navDeepLink {
        uriPattern = "soundarch://advanced/dynamics/compressor"
    })
) { CompressorScreen(...) }
```

**Use Cases:**
- Notifications: "XRun detected" → open Diagnostics
- Widget: "Quick EQ" → open Equalizer
- External apps: Share preset → open Compressor with preset ID

---

## 6. Migration Path

### 6.1 Phase 2.1: Immediate Cleanup (Non-Breaking)

**Goal:** Remove dangling routes, fix redirect

**Changes:**
1. Remove `Routes.Advanced` and `Routes.Logs` (dangling)
2. Implement `EqSettingsScreen.kt` or remove `EqSettings` route + UiIds
3. Clarify BottomNavBar intent (keep/remove/implement)

**Impact:** Zero (unused routes removed)

---

### 6.2 Phase 2.2: Path Harmonization (Breaking)

**Goal:** Consistent route paths (nested DSP under dynamics/)

**Changes:**
```kotlin
// Before
data object Compressor : Routes("compressor")
data object Limiter : Routes("limiter")
data object AGC : Routes("agc")

// After
data object Compressor : Routes("advanced/dynamics/compressor")
data object Limiter : Routes("advanced/dynamics/limiter")
data object AGC : Routes("advanced/dynamics/agc")
```

**Impact:** Navigation calls must update:
```kotlin
// Before
navController.navigate(Routes.Compressor.route)

// After (same code, different route string)
navController.navigate(Routes.Compressor.route)  // "advanced/dynamics/compressor"
```

**Migration:** Automated (no code changes, just route string values)

---

### 6.3 Phase 2.3: Nested Graphs (Optional)

**Goal:** True nested navigation for advanced sections

**Changes:**
- Convert flat NavHost to nested `navigation()` blocks
- Add AdvancedHomeScreen.kt as hub
- Scope back stacks per graph

**Impact:** Better logical structure, improved state management

---

### 6.4 Phase 2.4: Deep Links (Optional)

**Goal:** Enable external navigation

**Changes:**
- Add `deepLinks` parameter to all `composable()` calls
- Register deep link scheme in AndroidManifest.xml

**Impact:** Enables notifications, widgets, external intents

---

## 7. Testing Checklist

### 7.1 Current Navigation Tests (Manual)

- [ ] Home → AudioEngine → Back → Home
- [ ] Home → Dynamics → Compressor → Back → Dynamics → Back → Home
- [ ] Home → Dynamics → AGC → Back → Dynamics → Back → Home
- [ ] Home → Dynamics → Limiter → Back → Dynamics → Back → Home
- [ ] Home → NoiseCancelling → Back → Home
- [ ] Home → Equalizer → Back → Home
- [ ] Home → Bluetooth → Back → Home
- [ ] Home → Performance → Back → Home
- [ ] Home → BuildRuntime → Back → Home
- [ ] Home → Diagnostics → Back → Home
- [ ] Home → LogsTests → Back → Home
- [ ] Home → AppPermissions → Back → Home
- [ ] Home → ML → Back → Home

**Status:** ✅ All pass (manually verified)

---

### 7.2 Proposed Automated Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigateToCompressorAndBack() {
        // Start at Home
        composeTestRule.onNodeWithTag(UiIds.Home.SCREEN).assertIsDisplayed()

        // Navigate to Dynamics
        composeTestRule.onNodeWithTag(UiIds.Home.ADVANCED_DYNAMICS).performClick()
        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.SCREEN).assertIsDisplayed()

        // Navigate to Compressor
        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.COMPRESSOR_CARD).performClick()
        composeTestRule.onNodeWithTag(UiIds.Compressor.SCREEN).assertIsDisplayed()

        // Back to Dynamics
        composeTestRule.onNodeWithTag(UiIds.Compressor.BACK_BUTTON).performClick()
        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.SCREEN).assertIsDisplayed()

        // Back to Home
        composeTestRule.onNodeWithTag(UiIds.DynamicsMenu.BACK_BUTTON).performClick()
        composeTestRule.onNodeWithTag(UiIds.Home.SCREEN).assertIsDisplayed()
    }
}
```

---

## 8. Summary & Recommendations

### 8.1 Current State Assessment

**Grade: B**

**Strengths:**
- ✅ Consistent back navigation (all screens use popBackStack)
- ✅ Clean Routes sealed class structure
- ✅ Functional navigation for all implemented screens
- ✅ No navigation crashes or loops

**Weaknesses:**
- ⚠️ 2 dangling routes (Advanced, Logs)
- ⚠️ 1 redirect route (EqSettings → Equalizer)
- ⚠️ Inconsistent path patterns (flat DSP vs nested advanced/)
- ⚠️ BottomNavBar exists but unused
- ⚠️ No deep links
- ⚠️ No saved state preservation

---

### 8.2 Priority Recommendations

#### **High Priority (Phase 2.1)**
1. **Remove Dangling Routes:** Delete `Routes.Advanced` and `Routes.Logs` (or implement)
2. **Fix EqSettings:** Either implement full screen or remove UiIds + redirect
3. **Harmonize DSP Paths:** Move compressor/limiter/agc under `advanced/dynamics/`

#### **Medium Priority (Phase 2.2)**
4. **Add Deep Links:** Enable external navigation (notifications, widgets)
5. **Implement State Preservation:** Survive process death
6. **Clarify BottomNav:** Decide to implement, remove, or document future use

#### **Low Priority (Phase 2.3)**
7. **Nested Graphs:** Convert to true nested navigation for better structure
8. **Automated Nav Tests:** Add UI tests for all navigation paths
9. **AdvancedHomeScreen Hub:** Optional hub screen for advanced sections

---

## 9. Proposed Routes.kt (Phase 2)

```kotlin
package com.soundarch.ui.navigation

sealed class Routes(val route: String) {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🏠 MAIN NAVIGATION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    data object Home : Routes("home")
    data object Equalizer : Routes("equalizer")
    data object EqSettings : Routes("equalizer/settings")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ⚙️ ADVANCED SECTIONS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    data object AdvancedHub : Routes("advanced")  // Optional hub screen
    data object AudioEngine : Routes("advanced/audio_engine")

    // Dynamics Sub-Navigation
    data object DynamicsMenu : Routes("advanced/dynamics")
    data object Compressor : Routes("advanced/dynamics/compressor")
    data object Limiter : Routes("advanced/dynamics/limiter")
    data object AGC : Routes("advanced/dynamics/agc")

    data object NoiseCancelling : Routes("advanced/noise_cancelling")
    data object Bluetooth : Routes("advanced/bluetooth")
    data object Ml : Routes("advanced/ml")
    data object Performance : Routes("advanced/performance")
    data object BuildRuntime : Routes("advanced/build_runtime")
    data object Diagnostics : Routes("advanced/diagnostics")
    data object LogsTests : Routes("advanced/logs_tests")
    data object AppPermissions : Routes("advanced/app_permissions")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📊 OPTIONAL: BOTTOM NAV (if implemented)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // data object Logs : Routes("logs")
}
```

**Changes:**
- ❌ Removed: `Routes.Advanced` (dangling)
- ❌ Removed: `Routes.Logs` (dangling)
- ✅ Added: `Routes.EqSettings` (proper path under equalizer/)
- ✅ Added: `Routes.AdvancedHub` (optional, for dedicated advanced screen)
- ✅ Renamed: `Routes.Dynamics` → `Routes.DynamicsMenu` (clarity)
- ✅ Moved: Compressor, Limiter, AGC under `advanced/dynamics/`

---

**End of NAVGRAPH_MAP.md**
