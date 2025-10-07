package com.soundarch.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EqualizerSlider(
    gain: Float,
    frequency: Float,
    onGainChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 🎵 Label fréquence (gauche)
        val freqLabel = when {
            frequency >= 1000 -> "${(frequency / 1000).toInt()}k Hz"
            else -> "${frequency.toInt()} Hz"
        }

        Text(
            text = freqLabel,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            color = Color(0xFF90CAF9),  // Bleu clair
            modifier = Modifier.width(70.dp)
        )

        // 🎚️ SLIDER HORIZONTAL (pleine largeur)
        Slider(
            value = gain,
            onValueChange = { newValue ->
                Log.i("EqualizerSlider", "📊 ${frequency.toInt()}Hz → ${String.format("%.1f", newValue)}dB")
                onGainChange(newValue)
            },
            valueRange = -12f..12f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF2196F3),        // Bleu
                activeTrackColor = Color(0xFF64B5F6),  // Bleu clair
                inactiveTrackColor = Color(0xFF424242) // Gris foncé
            ),
            modifier = Modifier.weight(1f)  // Prend tout l'espace disponible
        )

        // 📊 Label gain (droite)
        val gainText = when {
            gain > 0 -> "+${String.format("%.1f", gain)}"
            else -> String.format("%.1f", gain)
        }

        Text(
            text = "$gainText dB",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            color = when {
                gain > 0 -> Color(0xFF4CAF50)   // Vert si boost
                gain < 0 -> Color(0xFFF44336)   // Rouge si cut
                else -> Color.Gray               // Gris si 0dB
            },
            modifier = Modifier.width(60.dp)
        )
    }
}