package com.soundarch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AGCScreen(
    initialEnabled: Boolean = true,
    initialTargetLevel: Float = -20f,
    initialMaxGain: Float = 25f,
    initialMinGain: Float = -10f,
    initialAttackTime: Float = 3f,
    initialReleaseTime: Float = 15f,
    initialNoiseThreshold: Float = -55f,
    initialWindowSize: Float = 0.1f,
    onAGCChange: (Float, Float, Float, Float, Float, Float, Float) -> Unit,
    onAGCToggle: (Boolean) -> Unit,
    getAGCCurrentGain: () -> Float,
    getAGCCurrentLevel: () -> Float,
    onBack: () -> Unit
) {
    // ✅ UTILISER les valeurs passées en paramètres
    var enabled by remember { mutableStateOf(initialEnabled) }
    var targetLevel by remember { mutableStateOf(initialTargetLevel) }
    var maxGain by remember { mutableStateOf(initialMaxGain) }
    var minGain by remember { mutableStateOf(initialMinGain) }
    var attackTime by remember { mutableStateOf(initialAttackTime) }
    var releaseTime by remember { mutableStateOf(initialReleaseTime) }
    var noiseThreshold by remember { mutableStateOf(initialNoiseThreshold) }
    var windowSize by remember { mutableStateOf(initialWindowSize) }

    // Real-time monitoring
    var currentGain by remember { mutableStateOf(0f) }
    var currentLevel by remember { mutableStateOf(-60f) }

    // Poll every 100ms for real-time display
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            try {
                currentGain = getAGCCurrentGain()
                currentLevel = getAGCCurrentLevel()
            } catch (_: Exception) {
                // Ignore errors (e.g., when audio is not running)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AGC Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Automatic Gain Control",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                enabled = it
                                onAGCToggle(it)
                            }
                        )
                    }
                    Text(
                        "Maintains consistent output level by automatically adjusting gain based on input signal strength.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "📊 Real-time Monitor",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Current Gain:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "${String.format("%+.1f", currentGain)} dB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Input Level:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "${String.format("%.1f", currentLevel)} dBFS",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Target Level: ${String.format("%.1f", targetLevel)} dBFS",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Desired output level (lower = quieter, higher = louder)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = targetLevel,
                        onValueChange = {
                            targetLevel = it
                            onAGCChange(
                                targetLevel,
                                maxGain,
                                minGain,
                                attackTime,
                                releaseTime,
                                noiseThreshold,
                                windowSize
                            )
                        },
                        valueRange = -40f..0f,
                        enabled = enabled
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("-40 dB", style = MaterialTheme.typography.labelSmall)
                        Text("0 dB", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Gain Limits",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Maximum and minimum gain adjustment range",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Max Gain: +${String.format("%.1f", maxGain)} dB",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Limited to +30 dB for safety (prevents audio overload)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                    Slider(
                        value = maxGain,
                        onValueChange = {
                            maxGain = it
                            onAGCChange(
                                targetLevel,
                                maxGain,
                                minGain,
                                attackTime,
                                releaseTime,
                                noiseThreshold,
                                windowSize
                            )
                        },
                        valueRange = 0f..30f,
                        enabled = enabled
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0 dB", style = MaterialTheme.typography.labelSmall)
                        Text("+30 dB", style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Min Gain: ${String.format("%.1f", minGain)} dB",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = minGain,
                        onValueChange = {
                            minGain = it
                            onAGCChange(
                                targetLevel,
                                maxGain,
                                minGain,
                                attackTime,
                                releaseTime,
                                noiseThreshold,
                                windowSize
                            )
                        },
                        valueRange = -40f..0f,
                        enabled = enabled
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("-40 dB", style = MaterialTheme.typography.labelSmall)
                        Text("0 dB", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Timing",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "How fast AGC reacts to level changes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Attack Time: ${String.format("%.2f", attackTime)} s",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Time to increase gain (faster = more responsive)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = attackTime,
                        onValueChange = {
                            attackTime = it
                            onAGCChange(
                                targetLevel,
                                maxGain,
                                minGain,
                                attackTime,
                                releaseTime,
                                noiseThreshold,
                                windowSize
                            )
                        },
                        valueRange = 0.01f..20f,
                        enabled = enabled
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.01 s", style = MaterialTheme.typography.labelSmall)
                        Text("20 s", style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Release Time: ${String.format("%.2f", releaseTime)} s",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Time to decrease gain (slower = more stable)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = releaseTime,
                        onValueChange = {
                            releaseTime = it
                            onAGCChange(
                                targetLevel,
                                maxGain,
                                minGain,
                                attackTime,
                                releaseTime,
                                noiseThreshold,
                                windowSize
                            )
                        },
                        valueRange = 0.1f..30f,
                        enabled = enabled
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.1 s", style = MaterialTheme.typography.labelSmall)
                        Text("30 s", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Noise Threshold: ${String.format("%.1f", noiseThreshold)} dBFS",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "AGC freezes below this level to avoid amplifying silence/noise",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = noiseThreshold,
                        onValueChange = {
                            noiseThreshold = it
                            onAGCChange(
                                targetLevel,
                                maxGain,
                                minGain,
                                attackTime,
                                releaseTime,
                                noiseThreshold,
                                windowSize
                            )
                        },
                        valueRange = -80f..-30f,
                        enabled = enabled
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("-80 dB", style = MaterialTheme.typography.labelSmall)
                        Text("-30 dB", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Analysis Window: ${String.format("%.2f", windowSize)} s",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Time window for RMS level detection (shorter = faster response)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = windowSize,
                        onValueChange = {
                            windowSize = it
                            onAGCChange(
                                targetLevel,
                                maxGain,
                                minGain,
                                attackTime,
                                releaseTime,
                                noiseThreshold,
                                windowSize
                            )
                        },
                        valueRange = 0.1f..2.0f,
                        enabled = enabled
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.1 s", style = MaterialTheme.typography.labelSmall)
                        Text("2.0 s", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Button(
                onClick = {
                    targetLevel = -20f
                    maxGain = 25f
                    minGain = -10f
                    attackTime = 0.1f  // Fast attack: 100ms
                    releaseTime = 0.5f  // Fast release: 500ms
                    noiseThreshold = -55f
                    windowSize = 0.1f
                    onAGCChange(
                        targetLevel,
                        maxGain,
                        minGain,
                        attackTime,
                        releaseTime,
                        noiseThreshold,
                        windowSize
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Text("Reset to Default")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}