package com.soundarch.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.soundarch.ui.components.EqualizerGraph
import com.soundarch.ui.components.EqualizerSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    bands: List<Int>,
    gains: List<Float>,
    onBandChange: (Int, Float) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    Log.i("EqualizerScreen", "🎛️ Écran EQ chargé | ${bands.size} bandes")

    // ✅ Scaffold pour gérer les insets système (status bar + navigation bar)
    Scaffold(
        containerColor = Color.Transparent  // Transparent pour garder ton fond noir
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),  // ✅ Respecte les insets système
            color = Color(0xFF121212)  // Fond noir
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🎛️ Header avec titre
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Égaliseur 10 bandes",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF90CAF9)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 📊 Graphique visuel compact
                        EqualizerGraph(
                            gains = gains,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🎚️ Zone des sliders (scrollable vertical)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                ) {
                    bands.forEachIndexed { index, freq ->
                        EqualizerSlider(
                            frequency = freq.toFloat(),
                            gain = gains[index],
                            onGainChange = { newGain ->
                                Log.i("EqualizerScreen", "🔧 Band $index (${freq}Hz) → ${String.format("%.1f", newGain)}dB")
                                onBandChange(index, newGain)
                            }
                        )

                        // ✅ Séparateur entre sliders (compatible toutes versions)
                        if (index < bands.size - 1) {
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFF2A2A2A),
                                thickness = 1.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 🔘 Boutons footer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                Log.i("EqualizerScreen", "🔄 Reset demandé")
                                onReset()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)  // Rouge
                            ),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("🔄 Reset")
                        }

                        Button(
                            onClick = {
                                Log.i("EqualizerScreen", "⬅️ Retour demandé")
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)  // Bleu
                            ),
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text("⬅️ Retour")
                        }
                    }
                }
            }
        }
    }
}