package com.soundarch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    latency: Float,
    cpuUsage: Float,              // NOUVEAU
    memoryUsageMB: Float,          // NOUVEAU
    onStart: () -> Unit,
    onStop: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToCompressor: () -> Unit,
    onNavigateToLimiter: () -> Unit,
    onNavigateToAGC: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SoundArch") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Performance Metrics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Performance Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    // Latency
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Latency",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${String.format("%.2f", latency)} ms",
                                style = MaterialTheme.typography.headlineSmall,
                                color = when {
                                    latency < 10f -> Color(0xFF4CAF50)  // Vert
                                    latency < 20f -> Color(0xFFFFC107)  // Jaune
                                    else -> Color(0xFFF44336)           // Rouge
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Indicateur visuel latence
                        Box(
                            modifier = Modifier
                                .size(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = MaterialTheme.shapes.small,
                                color = when {
                                    latency < 10f -> Color(0xFF4CAF50)
                                    latency < 20f -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                }
                            ) {}
                        }
                    }

                    // CPU Usage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "CPU Usage",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${String.format("%.1f", cpuUsage)}%",
                                style = MaterialTheme.typography.headlineSmall,
                                color = when {
                                    cpuUsage < 30f -> Color(0xFF4CAF50)  // Vert
                                    cpuUsage < 60f -> Color(0xFFFFC107)  // Jaune
                                    else -> Color(0xFFF44336)            // Rouge
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Barre de progression CPU
                        LinearProgressIndicator(
                            progress = (cpuUsage / 100f).coerceIn(0f, 1f),
                            modifier = Modifier
                                .width(80.dp)
                                .height(8.dp),
                            color = when {
                                cpuUsage < 30f -> Color(0xFF4CAF50)
                                cpuUsage < 60f -> Color(0xFFFFC107)
                                else -> Color(0xFFF44336)
                            }
                        )
                    }

                    // Memory Usage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Memory (RSS)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${String.format("%.1f", memoryUsageMB)} MB",
                                style = MaterialTheme.typography.headlineSmall,
                                color = when {
                                    memoryUsageMB < 100f -> Color(0xFF4CAF50)  // Vert
                                    memoryUsageMB < 200f -> Color(0xFFFFC107)  // Jaune
                                    else -> Color(0xFFF44336)                  // Rouge
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Icône mémoire
                        Text(
                            "📊",
                            fontSize = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("▶️ Start")
                }

                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("⏹️ Stop")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DSP Settings
            Text(
                "DSP Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            FilledTonalButton(
                onClick = onNavigateToAGC,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("AGC (Automatic Gain Control)")
            }

            FilledTonalButton(
                onClick = onNavigateToEqualizer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Equalizer")
            }

            FilledTonalButton(
                onClick = onNavigateToCompressor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Compressor")
            }

            FilledTonalButton(
                onClick = onNavigateToLimiter,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limiter")
            }
        }
    }
}