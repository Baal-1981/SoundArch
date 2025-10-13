package com.soundarch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundarch.core.logging.UiActionLogger
import com.soundarch.ui.components.*
import com.soundarch.ui.model.UiMode
import com.soundarch.ui.model.UiModeConfig
import com.soundarch.ui.testing.UiIds
import com.soundarch.ui.theme.AppColors

/**
 * HomeScreen V2 - Refactored with Advanced integration
 *
 * Changes from V1:
 * - Integrated Advanced sections panel
 * - New StatusBadgesRowHome with BLOCK, BT, ML, SAFE, NC
 * - LatencyHud with engine running state
 * - PeakRmsMeter with optional headroom
 * - All navigation routes through Advanced
 * - ViewModels for metrics (passed as parameters)
 *
 * Layout:
 * 1. Status badges (BLOCK, BT, ML, SAFE, NC)
 * 2. Latency HUD (with engine state)
 * 3. Peak/RMS Meter
 * 4. Mini EQ Curve (if available)
 * 5. Start/Stop buttons
 * 6. Quick toggles (ML, SAFE, NC)
 * 7. Advanced sections panel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenV2(
    // Metrics
    latencyTotalMs: Double,
    latencyInputMs: Double,
    latencyOutputMs: Double,
    latencyEmaMs: Double?,
    latencyMinMs: Double?,
    latencyMaxMs: Double?,
    bufferSize: Int,
    xRunCount: Int,
    framesPerCallback: Int?,
    peakDb: Float,
    rmsDb: Float,
    headroomDb: Float?,

    // System Performance (CPU/RAM)
    cpuPercent: Float,
    ramPercent: Float,
    ramUsedMB: Long,
    ramTotalMB: Long,

    // Voice Gain (Post-EQ, Pre-Dynamics)
    voiceGainDb: Float,

    // UI Mode (Friendly/Advanced)
    uiMode: UiMode,
    uiModeConfig: UiModeConfig,
    onToggleUiMode: () -> Unit,

    // Engine state
    isEngineRunning: Boolean,

    // Feature states
    audioEngineEnabled: Boolean,
    dynamicsEnabled: Boolean,
    noiseCancellingEnabled: Boolean,
    bluetoothEnabled: Boolean,
    eqEnabled: Boolean,
    mlEnabled: Boolean,
    performanceEnabled: Boolean,
    buildRuntimeEnabled: Boolean,
    diagnosticsEnabled: Boolean,
    logsTestsEnabled: Boolean,
    appPermissionsEnabled: Boolean,
    safeEnabled: Boolean,

    // Bluetooth info
    isBluetoothActive: Boolean,
    bluetoothProfile: String?,
    bluetoothCodec: String?,
    bluetoothLatencyMs: Float,
    bluetoothCompensationMs: Float,

    // EQ data
    bands: List<Int>,
    gains: List<Float>,

    // Engine controls
    onStart: () -> Unit,
    onStop: () -> Unit,

    // Voice Gain controls
    onVoiceGainChange: (Float) -> Unit,
    onVoiceGainReset: () -> Unit,

    // Quick toggles
    onMlToggle: (Boolean) -> Unit,
    onSafeToggle: (Boolean) -> Unit,
    onNoiseCancellingToggle: (Boolean) -> Unit,

    // Navigation to Advanced sections
    onNavigateToEqualizer: () -> Unit,
    onNavigateToAudioEngine: () -> Unit,
    onNavigateToDynamics: () -> Unit,
    onNavigateToNoiseCancelling: () -> Unit,
    onNavigateToBluetooth: () -> Unit,
    onNavigateToEqSettings: () -> Unit,
    onNavigateToMl: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToBuildRuntime: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToLogsTests: () -> Unit,
    onNavigateToAppPermissions: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SoundArch",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.testTag(UiIds.Home.TITLE)
                        )
                    }
                },
                actions = {
                    // UI Mode Toggle Button
                    Button(
                        onClick = {
                            val newMode = if (uiMode == UiMode.FRIENDLY) UiMode.ADVANCED else UiMode.FRIENDLY
                            UiActionLogger.logSuccess(
                                screen = "home",
                                controlId = UiIds.Home.UI_MODE_TOGGLE,
                                action = "toggle",
                                value = "Switching to ${newMode.displayName} mode"
                            )
                            onToggleUiMode()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(40.dp)
                            .testTag(UiIds.Home.UI_MODE_TOGGLE)
                            .semantics { contentDescription = "Toggle UI mode between Friendly and Advanced" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (uiMode) {
                                UiMode.FRIENDLY -> AppColors.Success
                                UiMode.ADVANCED -> AppColors.Info
                            }
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiMode.icon,
                                fontSize = 16.sp
                            )
                            Text(
                                text = uiMode.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.BackgroundSecondary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .testTag(UiIds.Home.SCREEN)
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 🎯 STATUS BADGES (BLOCK, BT, ML, SAFE, NC)
            // Only show in Advanced mode
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            if (uiModeConfig.showStatusBadges) {
                StatusBadgesRowHome(
                    blockSize = bufferSize,
                    bluetoothProfile = bluetoothProfile,
                    bluetoothCompensationMs = bluetoothCompensationMs,
                    mlEnabled = mlEnabled,
                    safeEnabled = safeEnabled,
                    noiseCancellingEnabled = noiseCancellingEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(UiIds.Home.STATUS_BADGES_ROW)
                )
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 🎯 LATENCY HUD (with engine state + Bluetooth indicator)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            LatencyHud(
                totalMs = latencyTotalMs,
                inputMs = latencyInputMs,
                outputMs = latencyOutputMs,
                bufferSize = bufferSize,
                xRunCount = xRunCount,
                emaMs = latencyEmaMs,
                minMs = latencyMinMs,
                maxMs = latencyMaxMs,
                framesPerCallback = framesPerCallback,
                isEngineRunning = isEngineRunning,
                isBluetoothActive = isBluetoothActive && bluetoothEnabled,
                bluetoothCodec = bluetoothCodec,
                bluetoothLatencyMs = bluetoothLatencyMs,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(UiIds.Home.LATENCY_HUD)
            )

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 📊 PEAK/RMS METER (with headroom)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            PeakRmsMeter(
                peakDb = peakDb,
                rmsDb = rmsDb,
                headroomDb = headroomDb,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(UiIds.Home.PEAK_RMS_METER)
            )

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 💻 CPU/RAM METER
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            CpuRamMeter(
                cpuPercent = cpuPercent,
                ramPercent = ramPercent,
                ramUsedMB = ramUsedMB,
                ramTotalMB = ramTotalMB,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(UiIds.Home.CPU_RAM_METER)
            )

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 🎚️ MINI EQ CURVE - REMOVED per user request
            // EQ curve not needed on home screen
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // Removed: User reported EQ graph on home screen not needed

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 🎛️ START/STOP BUTTONS
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            UiActionLogger.logSuccess(
                                screen = "home",
                                controlId = UiIds.Home.START_BUTTON,
                                action = "click",
                                value = "Starting audio engine"
                            )
                            onStart()
                        } catch (e: Exception) {
                            UiActionLogger.logError(
                                screen = "home",
                                controlId = UiIds.Home.START_BUTTON,
                                action = "click",
                                exception = e,
                                notes = "Failed to start audio engine"
                            )
                            throw e
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag(UiIds.Home.START_BUTTON)
                        .semantics { contentDescription = "Start audio engine" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Success
                    )
                    // TODO: Add enabled = !isEngineRunning when engine state is properly tracked
                ) {
                    Text(
                        "▶ START",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        try {
                            UiActionLogger.logSuccess(
                                screen = "home",
                                controlId = UiIds.Home.STOP_BUTTON,
                                action = "click",
                                value = "Stopping audio engine"
                            )
                            onStop()
                        } catch (e: Exception) {
                            UiActionLogger.logError(
                                screen = "home",
                                controlId = UiIds.Home.STOP_BUTTON,
                                action = "click",
                                exception = e,
                                notes = "Failed to stop audio engine"
                            )
                            throw e
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag(UiIds.Home.STOP_BUTTON)
                        .semantics { contentDescription = "Stop audio engine" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Error
                    )
                    // TODO: Add enabled = isEngineRunning when engine state is properly tracked
                ) {
                    Text(
                        "⏹ STOP",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        fontSize = 14.sp
                    )
                }
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 🎤 VOICE GAIN CONTROL (Post-EQ, Pre-Dynamics)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            VoiceGainCard(
                voiceGainDb = voiceGainDb,
                onGainChange = onVoiceGainChange,
                onReset = onVoiceGainReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(UiIds.Home.VOICE_GAIN_CARD)
            )

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // ⚡ QUICK TOGGLES (ML, SAFE, NC)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Text(
                "⚡ QUICK ACCESS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AppColors.Info,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickToggleCard(
                    icon = "🤖",
                    label = "ML",
                    enabled = mlEnabled,
                    onToggle = {
                        UiActionLogger.logSuccess(
                            screen = "home",
                            controlId = UiIds.Home.QUICK_ML_TOGGLE,
                            action = "toggle",
                            value = if (mlEnabled) "OFF" else "ON"
                        )
                        onMlToggle(!mlEnabled)
                    },
                    testTag = UiIds.Home.QUICK_ML_TOGGLE,
                    contentDesc = "Toggle machine learning processing",
                    modifier = Modifier.weight(1f)
                )

                QuickToggleCard(
                    icon = "🛡️",
                    label = "SAFE",
                    enabled = safeEnabled,
                    onToggle = {
                        UiActionLogger.logSuccess(
                            screen = "home",
                            controlId = UiIds.Home.QUICK_SAFE_TOGGLE,
                            action = "toggle",
                            value = if (safeEnabled) "OFF" else "ON"
                        )
                        onSafeToggle(!safeEnabled)
                    },
                    testTag = UiIds.Home.QUICK_SAFE_TOGGLE,
                    contentDesc = "Toggle SAFE mode for hearing protection",
                    modifier = Modifier.weight(1f)
                )

                QuickToggleCard(
                    icon = "🔇",
                    label = "NC",
                    enabled = noiseCancellingEnabled,
                    onToggle = {
                        UiActionLogger.logSuccess(
                            screen = "home",
                            controlId = UiIds.Home.QUICK_NC_TOGGLE,
                            action = "toggle",
                            value = if (noiseCancellingEnabled) "OFF" else "ON"
                        )
                        onNoiseCancellingToggle(!noiseCancellingEnabled)
                    },
                    testTag = UiIds.Home.QUICK_NC_TOGGLE,
                    contentDesc = "Toggle noise cancelling",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // ⚙️ ADVANCED SECTIONS PANEL
            // Collapsible in Friendly mode, expanded in Advanced mode
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            AdvancedSectionsPanel(
                audioEngineEnabled = audioEngineEnabled,
                dynamicsEnabled = dynamicsEnabled,
                noiseCancellingEnabled = noiseCancellingEnabled,
                bluetoothEnabled = bluetoothEnabled,
                eqEnabled = eqEnabled,
                mlEnabled = mlEnabled,
                performanceEnabled = performanceEnabled,
                buildRuntimeEnabled = buildRuntimeEnabled,
                diagnosticsEnabled = diagnosticsEnabled,
                logsTestsEnabled = logsTestsEnabled,
                appPermissionsEnabled = appPermissionsEnabled,
                onNavigateToAudioEngine = onNavigateToAudioEngine,
                onNavigateToDynamics = onNavigateToDynamics,
                onNavigateToNoiseCancelling = onNavigateToNoiseCancelling,
                onNavigateToBluetooth = onNavigateToBluetooth,
                onNavigateToEqSettings = onNavigateToEqSettings,
                onNavigateToMl = onNavigateToMl,
                onNavigateToPerformance = onNavigateToPerformance,
                onNavigateToBuildRuntime = onNavigateToBuildRuntime,
                onNavigateToDiagnostics = onNavigateToDiagnostics,
                onNavigateToLogsTests = onNavigateToLogsTests,
                onNavigateToAppPermissions = onNavigateToAppPermissions,
                uiMode = uiMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(UiIds.Home.ADVANCED_PANEL)
            )

            // Bottom padding
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Quick toggle card (compact)
 */
@Composable
private fun QuickToggleCard(
    icon: String,
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
    testTag: String = "",
    contentDesc: String = "",
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onToggle,
        modifier = modifier
            .height(64.dp)
            .testTag(testTag)
            .semantics { contentDescription = contentDesc },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) AppColors.Success.copy(alpha = 0.3f) else AppColors.BackgroundSecondary
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (enabled) AppColors.Success else AppColors.TextDisabled,
                fontSize = 10.sp
            )
        }
    }
}
