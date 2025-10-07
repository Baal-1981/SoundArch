package com.soundarch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimiterScreen(
    initialEnabled: Boolean = true,
    initialThreshold: Float = -1.0f,
    initialRelease: Float = 50.0f,
    onLimiterChange: (Float, Float) -> Unit,
    onLimiterToggle: (Boolean) -> Unit,
    getLimiterGainReduction: () -> Float,
    onBack: () -> Unit
) {
    // ✅ UTILISER les valeurs passées en paramètres
    var enabled by remember { mutableStateOf(initialEnabled) }
    var threshold by remember { mutableStateOf(initialThreshold) }
    var release by remember { mutableStateOf(initialRelease) }

    // Real-time monitoring
    var gainReduction by remember { mutableStateOf(0f) }

    // Poll every 100ms for real-time display
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            try {
                gainReduction = getLimiterGainReduction()
            } catch (_: Exception) {
                // Ignore errors (e.g., when audio is not running)
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Limiter") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Protection auditive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Empêche le signal de dépasser un seuil absolu. Protège vos oreilles des pics sonores.",
                        style = MaterialTheme.typography.bodyMedium,
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
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Gain Reduction:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "${String.format("%.1f", gainReduction)} dB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (gainReduction > 0.1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        if (gainReduction > 0.1f) "⚠️ Limiting active" else "✅ No limiting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Limiter activé",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (enabled) "Protection active" else "Désactivé",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (enabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            onLimiterToggle(it)
                        }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Threshold",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${String.format("%.1f", threshold)} dB",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = threshold,
                        onValueChange = {
                            threshold = it
                            onLimiterChange(threshold, release)
                        },
                        valueRange = -12f..0f,
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "-12 dB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "0 dB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Text(
                        text = "Niveau maximum autorisé (-1 dB recommandé)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Release",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${String.format("%.0f", release)} ms",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = release,
                        onValueChange = {
                            release = it
                            onLimiterChange(threshold, release)
                        },
                        valueRange = 10f..200f,
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "10 ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "200 ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Text(
                        text = "Vitesse de retour après limitation (50 ms recommandé)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Column {
                        Text(
                            text = "Important",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Le Limiter empêche le signal de dépasser le threshold. Il est toujours recommandé de le garder activé pour protéger votre audition.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}