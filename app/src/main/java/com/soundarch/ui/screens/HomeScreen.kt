package com.soundarch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    latency: Float,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToCompressor: () -> Unit,
    onNavigateToLimiter: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SoundArch") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)  // ✅ Respecte les insets système
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),  // ✅ Espace pour navigation bar
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 📊 Latence
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Latency",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${String.format("%.2f", latency)} ms",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // ▶️ Contrôles audio
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("▶️ Start Audio")
            }

            Button(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("⏹️ Stop Audio")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🎛️ DSP Settings
            Text(
                text = "DSP Settings",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = onNavigateToEqualizer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎚️ Equalizer")
            }

            Button(
                onClick = onNavigateToCompressor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎛️ Compressor")
            }

            Button(
                onClick = onNavigateToLimiter,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("🚨 Limiter")
            }
        }
    }
}