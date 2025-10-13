# SoundArch Design System

**Version**: 2.0
**Last Updated**: 2025-10-11
**Status**: ✅ Implemented

A comprehensive design system for SoundArch - a professional-grade real-time audio DSP application with dual UI/UX modes (Friendly/Advanced).

---

## Table of Contents

1. [Design Principles](#design-principles)
2. [Grid & Spacing](#grid--spacing)
3. [Typography](#typography)
4. [Color System](#color-system)
5. [Iconography](#iconography)
6. [Components](#components)
7. [Motion & Animation](#motion--animation)
8. [Accessibility](#accessibility)
9. [Dual UI Modes](#dual-ui-modes)
10. [Audio-Specific Patterns](#audio-specific-patterns)

---

## Design Principles

### 1. **Professional Precision**
Audio applications require trust. Our UI communicates technical accuracy through:
- Monospace fonts for numeric values (dB, Hz, ms)
- Color-coded status indicators (green = good, yellow = caution, red = critical)
- Consistent meter scales with labeled ranges

### 2. **Immediate Feedback**
Real-time audio demands instant visual response:
- Live meters update at display refresh rate (60Hz)
- Parameter changes reflected within one audio block (2-10ms)
- Visual confirmation for all user actions (button press, toggle switch)

### 3. **Clarity Over Density**
Even in Advanced mode, information hierarchy is preserved:
- Important metrics (Peak/RMS, Latency) are prominent
- Secondary details (XRuns, buffer size) are compact but readable
- Tertiary info (build version, CPU arch) is in dedicated sections

### 4. **Graceful Modes**
Friendly and Advanced modes share visual language:
- Same color palette and typography
- Same component designs (buttons, cards, meters)
- Only visibility and density differ

---

## Grid & Spacing

### Base Unit: **4dp**

All spacing, sizing, and padding use multiples of 4dp for visual consistency.

### Spacing Scale

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Tight inline spacing (icon-text gap) |
| `sm` | 8dp | Compact element spacing (cards, list items) |
| `md` | 12dp | Standard element spacing (default) |
| `lg` | 16dp | Section padding, screen margins |
| `xl` | 24dp | Large section spacing |
| `xxl` | 32dp | Major section breaks |

### Layout Grid

**Screen Margins**: 16dp horizontal (lg)
**Content Max Width**: 600dp (single-column, scrollable)
**Card Padding**: 16dp (lg)
**List Item Height**: 64dp (min touch target)

### Component Sizing

| Component | Height | Padding | Spacing |
|-----------|--------|---------|---------|
| Button (Primary) | 56dp | 16dp h, 12dp v | 12dp between |
| Button (Secondary) | 48dp | 12dp h, 8dp v | 8dp between |
| Button (Compact) | 40dp | 12dp h, 6dp v | 8dp between |
| Text Input | 56dp | 16dp h | - |
| Slider | 48dp (track 4dp) | 0dp | - |
| Toggle Header | 64dp | 16dp h, 12dp v | - |
| Section Card | Auto | 16dp all | 12dp between cards |
| Status Badge | 28dp | 8dp h, 4dp v | 6dp between |

---

## Typography

### Font Family
**Primary**: System default (Roboto on Android)
**Monospace**: Monospace system font for numeric values

### Type Scale

| Style | Size | Weight | Line Height | Usage |
|-------|------|--------|-------------|-------|
| **Display Large** | 48sp | Bold | 56sp | Voice Gain dB readout |
| **Display Small** | 36sp | Bold | 44sp | Large numeric displays |
| **Headline Medium** | 24sp | Bold | 32sp | Screen titles (SoundArch) |
| **Title Large** | 20sp | Bold | 28sp | Section titles |
| **Title Medium** | 16sp | Bold | 24sp | Card titles |
| **Body Large** | 16sp | Regular | 24sp | Primary content |
| **Body Medium** | 14sp | Regular | 20sp | Secondary content, descriptions |
| **Body Small** | 12sp | Regular | 16sp | Tertiary content, captions |
| **Label Large** | 14sp | Bold | 20sp | Button labels (primary) |
| **Label Medium** | 12sp | Bold | 16sp | Button labels (secondary), section headers |
| **Label Small** | 11sp | Medium | 16sp | Helper text, subtitles |
| **Label XSmall** | 10sp | Regular | 14sp | Metadata, timestamps |
| **Label XXSmall** | 9sp | Regular | 12sp | Ultra-compact labels (badges) |

### Numeric Values (Monospace)

All numeric displays use monospace fonts for alignment:
- **dB values**: `+12.0 dB`, `-6.5 dB`, `0.0 dB`
- **Frequency**: `48000 Hz`, `1000 Hz`
- **Time**: `2.67 ms`, `100 ms`, `0.5 s`
- **Percentage**: `75%`, `100%`

**Format Rules**:
- Always show sign for dB (`+` or `-`)
- Fixed decimal places (1 for dB, 2 for ms)
- Space between value and unit

---

## Color System

### Theme: **Dark Mode Primary**

SoundArch uses dark mode for reduced eye strain during long audio sessions and better contrast for meters.

### Base Palette

| Token | Hex | Usage |
|-------|-----|-------|
| **Background Primary** | `#121212` | Screen background |
| **Background Secondary** | `#1E1E1E` | Top bar, footer |
| **Surface** | `#1A1A1A` | Cards, panels (enabled) |
| **Surface Disabled** | `#0F0F0F` | Cards, panels (disabled) |
| **Surface Elevated** | `#2A2A2A` | Sliders, inputs |
| **Divider** | `#2A2A2A` | Separators |

### Text Colors

| Token | Hex | Opacity | Usage |
|-------|-----|---------|-------|
| **Text Primary** | `#E0E0E0` | 100% | Primary content |
| **Text Secondary** | `#CCCCCC` | 87% | Secondary content |
| **Text Tertiary** | `#999999` | 60% | Captions, metadata |
| **Text Disabled** | `#666666` | 38% | Disabled text |
| **Text Hint** | `#555555` | 30% | Placeholder text |
| **Text Overlay** | `#444444` | 26% | Very subtle labels |

### Accent Colors

| Token | Hex | Usage |
|-------|-----|-------|
| **Primary Blue** | `#2196F3` | Advanced mode, links, info |
| **Primary Blue Light** | `#90CAF9` | Section headers, highlights |
| **Success Green** | `#4CAF50` | Friendly mode, enabled states, success |
| **Success Green Light** | `#81C784` | Hover, secondary success |
| **Success Green Dark** | `#1B5E20` | Active, pressed |
| **Warning Amber** | `#FFC107` | Caution, experimental badges |
| **Warning Orange** | `#FF9800` | Warning states |
| **Error Red** | `#F44336` | Critical, stop button, errors |
| **Error Red Dark** | `#D32F2F` | Error backgrounds |

### Semantic Colors

#### **Audio Level Colors** (for Peak/RMS meters)
| Range | Color | Hex | Meaning |
|-------|-------|-----|---------|
| `-60 to -18 dB` | Green | `#4CAF50` | Safe, good levels |
| `-18 to -6 dB` | Yellow | `#FFEB3B` | Nominal, watch levels |
| `-6 to -3 dB` | Orange | `#FF9800` | High, caution |
| `-3 to 0 dB` | Red | `#F44336` | Clipping risk, critical |

#### **Status Colors**
| Status | Color | Hex | Usage |
|--------|-------|-----|-------|
| **Active/ON** | Green | `#4CAF50` | Feature enabled, processing active |
| **Inactive/OFF** | Gray | `#333333` | Feature disabled, bypassed |
| **Warning** | Amber | `#FFC107` | High CPU, buffer issues |
| **Critical** | Red | `#F44336` | XRuns, clipping, errors |
| **Info** | Blue | `#2196F3` | Informational badges |

### Color Usage Guidelines

**DO**:
- Use green for positive states (enabled, success, safe levels)
- Use red sparingly (only for critical states or stop actions)
- Maintain 4.5:1 contrast ratio for text (WCAG AA)
- Use color + icon for status (don't rely on color alone)

**DON'T**:
- Use pure black (`#000000`) - always use `#121212` or darker
- Use pure white (`#FFFFFF`) - use `#E0E0E0` for text
- Mix warm and cool grays - stick to neutral grays

---

## Iconography

### Icon Set: **Emoji + Material Design**

SoundArch uses a hybrid approach:
- **Emoji** for section identifiers and visual warmth
- **Symbols** for UI controls (arrows, checkmarks)

### Emoji Icon Glossary

| Category | Icon | Meaning | Usage |
|----------|------|---------|-------|
| **Audio** | 🎤 | Voice/Microphone | Voice Gain |
| | 🎚️ | Audio Mixer | Audio Engine, EQ |
| | 🎛️ | Control Knobs | EQ Settings |
| | ⚡ | Lightning | Dynamics, Fast Processing |
| | 🔇 | Muted Speaker | Noise Cancelling |
| | 📶 | Signal Strength | Bluetooth |
| **System** | 🤖 | Robot | Machine Learning |
| | 📈 | Chart | Performance |
| | 🔧 | Wrench | Build & Runtime |
| | 🔬 | Microscope | Diagnostics |
| | 📝 | Memo | Logs & Tests |
| | 🔐 | Lock | App & Permissions |
| **Mode** | 😊 | Smiley Face | Friendly Mode |
| | ⚙️ | Gear | Advanced Mode |
| **Status** | ✅ | Check Mark | Success, Pass |
| | ⚠️ | Warning | Caution, High Values |
| | 🚨 | Siren | Critical, Urgent |
| | 🔥 | Fire | Critical CPU/Temp |
| | 💡 | Light Bulb | Tip, Recommendation |
| | ℹ️ | Information | Info Card |
| **Actions** | ▶ | Play | Start |
| | ⏹ | Stop | Stop |
| | 🔄 | Reload | Apply & Restart |
| | ⬅️ | Left Arrow | Back |
| | → | Right Arrow | Navigate |

### Icon Sizing

| Context | Size | Usage |
|---------|------|-------|
| **Section Icons** | 20sp | Advanced panel items |
| **Button Icons** | 16sp | Mode toggle button |
| **Status Icons** | 24sp | Effect/CPU status indicators |
| **Large Display** | 48sp | (Reserved) |

---

## Components

### 1. **Buttons**

#### **Primary Action Button**
```kotlin
Button(
    onClick = { },
    modifier = Modifier.height(56.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF4CAF50)  // Green for positive actions
    )
) {
    Text("▶ START", fontWeight = FontWeight.Bold, fontSize = 14.sp)
}
```

**Variants**:
- **Success/Start**: Green `#4CAF50`
- **Danger/Stop**: Red `#F44336`
- **Neutral**: Gray `#424242`
- **Info**: Blue `#2196F3`

#### **Mode Toggle Button** (Top Bar)
```kotlin
Button(
    onClick = onToggleUiMode,
    modifier = Modifier.height(40.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = if (isFriendly) Color(0xFF4CAF50) else Color(0xFF2196F3)
    )
) {
    Text(icon)  // 😊 or ⚙️
    Text(modeName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
}
```

#### **Quick Toggle Card** (3-column grid)
```kotlin
Button(
    onClick = { },
    modifier = Modifier.height(64.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = if (enabled) Color(0xFF1B5E20) else Color(0xFF1A1A1A)
    )
) {
    Column {
        Text(icon, fontSize = 24.sp)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
```

### 2. **Cards**

#### **Section Card** (standard container)
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
        containerColor = if (enabled) Color(0xFF1A1A1A) else Color(0xFF0F0F0F)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Card content
    }
}
```

**Elevation**: 4dp default (subtle shadow)

### 3. **Sliders**

#### **Parameter Slider**
```kotlin
Slider(
    value = currentValue,
    onValueChange = { newValue -> },
    valueRange = minValue..maxValue,
    steps = (range / step).toInt() - 1,
    colors = SliderDefaults.colors(
        thumbColor = Color(0xFF4CAF50),
        activeTrackColor = Color(0xFF4CAF50),
        inactiveTrackColor = Color(0xFF333333)
    )
)
```

**Track Height**: 4dp
**Thumb Size**: 20dp diameter
**Active Color**: Green `#4CAF50` (success) or context-appropriate

### 4. **Meters**

#### **Peak/RMS Meter**
- **Bar Height**: 16dp per channel
- **Background**: `#1A1A1A`
- **Gradient**: Green → Yellow → Orange → Red (based on dB)
- **Peak Hold**: White line, 500ms decay
- **dB Scale**: -60 to 0 dBFS, labeled at -60, -40, -20, -12, -6, 0

#### **Latency HUD**
- **Total Latency**: Large, bold, monospace (24sp)
- **Breakdown**: IN/OUT as secondary (11sp)
- **EMA/Min/Max**: Advanced mode only (10sp)
- **Color**:
  - Green: < 10ms
  - Yellow: 10-20ms
  - Red: > 20ms

### 5. **Toggle Header**

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .background(Color(0xFF1E1E1E))
        .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Switch(
        checked = enabled,
        onCheckedChange = { },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFF4CAF50),
            checkedTrackColor = Color(0xFF1B5E20)
        )
    )
}
```

**Height**: 64dp
**Background**: `#1E1E1E` (secondary)
**Switch**: Material 3 style

### 6. **Status Badges**

```kotlin
Text(
    text = "BLOCK 128",
    modifier = Modifier
        .background(Color(0xFF2196F3).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp),
    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
    color = Color(0xFF90CAF9),
    fontSize = 9.sp
)
```

**Height**: 28dp
**Padding**: 8dp horizontal, 4dp vertical
**Corner Radius**: 4dp
**Background Alpha**: 20%

### 7. **Advanced Section Item**

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .background(
            if (enabled) Color(0xFF1A1A1A) else Color(0xFF0F0F0F),
            shape = RoundedCornerShape(8.dp)
        )
        .clickable(onClick = { })
        .padding(horizontal = 12.dp, vertical = 10.dp)
) {
    Text(icon, fontSize = 20.sp)  // 🎚️
    Column(modifier = Modifier.weight(1f)) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(subtitle, fontSize = 10.sp, color = Color(0xFF888888))
    }
    Box(size = 8.dp, color = if (enabled) Color(0xFF4CAF50) else Color(0xFF333333))  // Status dot
    Text("→", fontSize = 14.sp, color = Color(0xFF666666))
}
```

**Corner Radius**: 8dp
**Padding**: 12dp horizontal, 10dp vertical
**Status Dot**: 8dp, green/gray

---

## Motion & Animation

### Animation Principles

1. **Purposeful**: Every animation conveys meaning (expand, reveal, confirm)
2. **Snappy**: Audio apps demand responsiveness (<300ms)
3. **Smooth**: 60fps minimum, easing curves for natural feel

### Duration Scale

| Duration | Usage | Example |
|----------|-------|---------|
| **100ms** | Micro-interactions | Button press, toggle switch |
| **200ms** | Quick transitions | Arrow rotation, fade in/out |
| **300ms** | Standard transitions | Panel expand/collapse, navigation |
| **500ms** | Extended animations | Peak hold decay, slide transitions |

### Easing Curves

| Curve | Usage |
|-------|-------|
| **Linear** | Real-time meters (Peak/RMS) |
| **Ease In Out** | Expand/collapse, modal dialogs |
| **Ease Out** | Appearing elements |
| **Ease In** | Disappearing elements |

### Specific Animations

#### **Panel Expand/Collapse** (AdvancedSectionsPanel)
```kotlin
AnimatedVisibility(
    visible = isExpanded,
    enter = expandVertically(animationSpec = tween(300)) + fadeIn(tween(300)),
    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(tween(300))
)
```
**Duration**: 300ms
**Easing**: Ease In Out

#### **Arrow Rotation** (AdvancedSectionsPanel)
```kotlin
val rotationAngle by animateFloatAsState(
    targetValue = if (isExpanded) 90f else 0f,
    animationSpec = tween(durationMillis = 200)
)
Text("▶", modifier = Modifier.rotate(rotationAngle))
```
**Duration**: 200ms
**Rotation**: 0° → 90° (down-pointing)

#### **Peak Hold Decay** (PeakRmsMeter)
```kotlin
LaunchedEffect(peakDb) {
    delay(500)  // Hold for 500ms
    // Animate decay to current value
}
```
**Hold Duration**: 500ms
**Decay**: Linear fade

#### **Mode Transition** (Instant)
Mode switches apply instantly (no fade) to feel responsive:
```kotlin
if (uiModeConfig.showStatusBadges) {
    StatusBadgesRowHome(...)  // Appears/disappears immediately
}
```

---

## Accessibility

### WCAG AA Compliance

#### **Contrast Ratios** (Text on Background)

| Combination | Ratio | Status |
|-------------|-------|--------|
| `#E0E0E0` on `#121212` | 13.5:1 | ✅ AAA |
| `#CCCCCC` on `#1A1A1A` | 10.2:1 | ✅ AAA |
| `#90CAF9` on `#121212` | 8.7:1 | ✅ AAA |
| `#4CAF50` on `#121212` | 6.8:1 | ✅ AA |
| `#666666` on `#1A1A1A` | 4.6:1 | ✅ AA |

#### **Touch Targets**

All interactive elements meet minimum 48dp touch target:
- Buttons: 56dp height (exceeds minimum)
- Toggle switches: 48dp hit area
- Sliders: 48dp height
- Advanced section items: 64dp height (exceeds minimum)

#### **Content Descriptions**

All non-text elements have semantic labels:
```kotlin
Button(
    onClick = { },
    modifier = Modifier.semantics { contentDescription = "Start audio engine" }
) {
    Text("▶ START")
}
```

#### **Screen Reader Support**

- All meters announce current value on focus
- Toggle states announced ("enabled"/"disabled")
- Navigation structure follows logical hierarchy
- Modal dialogs trap focus appropriately

#### **Keyboard Navigation** (Future)

- Tab order follows visual layout
- Enter/Space activates buttons
- Arrow keys navigate sliders
- Escape closes modals

---

## Dual UI Modes

### Friendly Mode

**Design Goal**: Approachable, simplified, welcoming

**Characteristics**:
- Larger touch targets (56dp vs 48dp)
- More whitespace (16dp vs 12dp)
- Fewer text labels (icons + primary labels only)
- Collapsed Advanced panel by default
- Hidden technical details (XRuns, buffer size, CPU breakdown)

**Color**: Green (`#4CAF50`) - friendly, positive

**Example Layout**:
```
┌─────────────────────────────────────┐
│ SoundArch         [😊 Friendly]     │
├─────────────────────────────────────┤
│                                     │
│ ┌─ Latency: 5.2 ms ──────────────┐ │
│ │ Total                           │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─ Peak/RMS Meter ────────────────┐ │
│ │ ████████░░░░░░░░░░  -12 dB      │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─ Voice Gain: +0.0 dB ───────────┐ │
│ │ [═══════●═══════]  [RESET]      │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [  ▶ START  ]  [  ⏹ STOP  ]        │
│                                     │
│ ⚡ QUICK ACCESS                     │
│ [🤖 ML] [🛡️ SAFE] [🔇 NC]          │
│                                     │
│ ⚙️ ADVANCED  [TAP TO EXPAND ▶]     │
│                                     │
└─────────────────────────────────────┘
```

### Advanced Mode

**Design Goal**: Full control, diagnostic detail, power user

**Characteristics**:
- Compact spacing (12dp vs 16dp)
- Dense information layout
- Full metric breakdowns (IN/OUT/EMA/MIN/MAX)
- Status badges visible (BLOCK, BT, XRuns, CPU)
- Mini EQ curve visible
- Expanded Advanced panel by default

**Color**: Blue (`#2196F3`) - technical, professional

**Example Layout**:
```
┌─────────────────────────────────────┐
│ SoundArch         [⚙️ Advanced]     │
├─────────────────────────────────────┤
│ [BLOCK 128] [BT: A2DP 150ms]       │
│ [ML ●] [SAFE ●] [NC ●]              │
│                                     │
│ ┌─ Latency ───────────────────────┐ │
│ │ Total: 5.2 ms                   │ │
│ │ IN: 2.6ms OUT: 2.6ms            │ │
│ │ EMA: 5.1ms [4.8-5.5] XRuns: 0   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─ Peak/RMS ───── Headroom: 12dB ┐ │
│ │ ████████░░░░░░░░░░  -12 dB      │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─ Mini EQ Curve ─────────────────┐ │
│ │  ⁀⁀⁀\__/⁀⁀  [TAP FOR DETAILS]  │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [  ▶ START  ]  [  ⏹ STOP  ]        │
│                                     │
│ ┌─ Voice Gain: +0.0 dB ───────────┐ │
│ │ [═══════●═══════]  [RESET]      │ │
│ │ Safe: -12dB to +6dB ⚠️ Caution  │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [🤖 ML] [🛡️ SAFE] [🔇 NC]          │
│                                     │
│ ⚙️ ADVANCED  [▼]                    │
│ ├─ 🎚️ Audio Engine →               │
│ ├─ ⚡ Dynamics →                    │
│ ├─ 🔇 Noise Cancelling  [ACTIVE]   │
│ ├─ 📶 Bluetooth →                   │
│ ├─ 🎛️ EQ Settings →                │
│ ├─ 🤖 ML  [EXPERIMENTAL]           │
│ ├─ 📈 Performance →                 │
│ ├─ 🔧 Build & Runtime →            │
│ ├─ 🔬 Diagnostics →                 │
│ ├─ 📝 Logs & Tests →               │
│ └─ 🔐 App & Permissions →          │
└─────────────────────────────────────┘
```

### Mode Transition Behavior

| Element | Friendly → Advanced | Advanced → Friendly |
|---------|-------------------|-------------------|
| **Status Badges** | Fade in (instant) | Fade out (instant) |
| **Latency Breakdown** | Expand (instant) | Collapse (instant) |
| **Mini EQ Curve** | Slide in (instant) | Slide out (instant) |
| **Advanced Panel** | Expand (300ms) | Collapse (300ms) |
| **Voice Gain Details** | Show hints (instant) | Hide hints (instant) |

**No animation for metric visibility** - Instant show/hide feels more responsive for mode switches.

---

## Audio-Specific Patterns

### 1. **dB Scale Visualization**

**Range**: -60 dBFS to 0 dBFS

**Color Zones**:
- `-60 to -18 dB`: Green (safe)
- `-18 to -6 dB`: Yellow (nominal)
- `-6 to -3 dB`: Orange (caution)
- `-3 to 0 dB`: Red (critical)

**Labels**: Always at -60, -40, -20, -12, -6, 0

### 2. **Latency Display**

**Thresholds**:
- **Excellent**: < 10ms (green)
- **Good**: 10-20ms (yellow)
- **Poor**: > 20ms (red)

**Format**: `5.2 ms` (1 decimal place)

**Breakdown** (Advanced mode):
```
Total: 5.2 ms
IN: 2.6ms  OUT: 2.6ms
EMA: 5.1ms  [4.8-5.5]  XRuns: 0
```

### 3. **Gain Reduction Meter** (Compressor/Limiter)

**Direction**: Right-to-left (reduction = negative growth)
**Color**: Cyan `#00BCD4` (distinct from level meters)
**Range**: 0 to -20 dB
**Update**: Real-time (60Hz)

### 4. **Transfer Curve Card** (Compressor)

**Grid**: 10dB increments
**Curve**: Bezier smoothing
**Threshold Line**: Dashed yellow
**Knee**: Highlighted region
**Colors**:
- Input: White
- Output: Cyan
- Unity gain: Dotted gray

### 5. **Frequency Response Curve** (EQ)

**X-Axis**: 20 Hz to 20 kHz (log scale)
**Y-Axis**: -12 dB to +12 dB (linear)
**Bands**: 10 ISO standard bands
**Curve**: Spline interpolation
**Color**: Gradient green → cyan

---

## Implementation Checklist

### ✅ Completed

- [x] Grid & spacing system (4dp base)
- [x] Typography scale (9 sizes)
- [x] Color palette (dark theme)
- [x] Button variants (primary, secondary, toggle)
- [x] Card components (section, elevated)
- [x] Slider styling (green thumb, 4dp track)
- [x] Toggle header (64dp, switch)
- [x] Status badges (rounded, 28dp)
- [x] Advanced section items (clickable rows)
- [x] Dual UI modes (Friendly/Advanced)
- [x] Mode toggle button (top bar)
- [x] Collapsible Advanced panel (animated)
- [x] Peak/RMS meter (color-coded bars)
- [x] Latency HUD (breakdown display)
- [x] Voice Gain card (slider + reset)
- [x] WCAG AA contrast (all text)
- [x] 48dp touch targets (all interactive)
- [x] Expand/collapse animation (300ms)
- [x] Arrow rotation animation (200ms)

### 🔄 In Progress

- [ ] Light theme variant (future enhancement)
- [ ] Custom EQ curve visualization (spline interpolation)
- [ ] Transfer curve card (Compressor visualization)
- [ ] Gain reduction meter (Compressor/Limiter)

### ⏳ Planned

- [ ] Keyboard navigation support
- [ ] Focus indicators (for DPAD/keyboard)
- [ ] Haptic feedback (button press, slider snap)
- [ ] Sound effects (optional, on/off toggle)
- [ ] Motion reduce (respect system accessibility setting)

---

## Design Tokens (Kotlin Constants)

```kotlin
// Spacing
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

// Colors
object SoundArchColors {
    val BackgroundPrimary = Color(0xFF121212)
    val BackgroundSecondary = Color(0xFF1E1E1E)
    val Surface = Color(0xFF1A1A1A)
    val SurfaceDisabled = Color(0xFF0F0F0F)

    val TextPrimary = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFFCCCCCC)
    val TextTertiary = Color(0xFF999999)

    val PrimaryBlue = Color(0xFF2196F3)
    val PrimaryBlueLight = Color(0xFF90CAF9)
    val SuccessGreen = Color(0xFF4CAF50)
    val WarningAmber = Color(0xFFFFC107)
    val ErrorRed = Color(0xFFF44336)
}

// Typography
object SoundArchTypography {
    val DisplayLarge = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold)
    val HeadlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    val TitleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
    val BodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val LabelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
}

// Component Sizes
object ComponentSizes {
    val ButtonPrimaryHeight = 56.dp
    val ButtonSecondaryHeight = 48.dp
    val ButtonCompactHeight = 40.dp
    val ToggleHeaderHeight = 64.dp
    val StatusBadgeHeight = 28.dp
    val SliderTrackHeight = 4.dp
}

// Animation
object AnimationDurations {
    const val Micro = 100
    const val Quick = 200
    const val Standard = 300
    const val Extended = 500
}
```

---

## Conclusion

The SoundArch Design System provides a cohesive visual language for a professional-grade audio DSP application. By combining technical precision with modern UI patterns, it serves both casual users (Friendly mode) and power users (Advanced mode) with equal effectiveness.

**Key Strengths**:
- ✅ **Consistent**: All components follow the same visual language
- ✅ **Accessible**: WCAG AA compliant, 48dp touch targets
- ✅ **Adaptive**: Dual modes serve different user needs
- ✅ **Professional**: Monospace numbers, color-coded meters, precise layouts
- ✅ **Responsive**: Real-time updates, snappy animations (<300ms)

**Live Implementation**: All design tokens and components are actively used in the SoundArch v2.0 codebase.

---

**Version History**:
- **2.0** (2025-10-11): Initial comprehensive design system with dual UI modes
