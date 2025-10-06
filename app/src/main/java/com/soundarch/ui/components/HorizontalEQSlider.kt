package com.soundarch.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Palette harmonisée
private val AccentPrimary = Color(0xFF6C63FF)
private val AccentSecondary = Color(0xFF4ECDC4)
private val TextPrimary = Color(0xFFE8E8E8)
private val TextSecondary = Color(0xFF9CA3AF)
private val TrackInactive = Color(0xFF2A3142)

@Composable
fun HorizontalEQSlider(
    frequency: Float,
    gain: Float,
    onGainChange: (Float) -> Unit,
    enabled: Boolean = true,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Label fréquence à gauche
        Text(
            text = formatFrequency(frequency),
            color = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(55.dp)
        )

        // Colonne : gain + slider
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = formatGain(gain),
                color = if (enabled) AccentSecondary else TextSecondary.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Slider horizontal avec couleurs douces
            Slider(
                value = gain,
                onValueChange = onGainChange,
                valueRange = -12f..12f,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = if (enabled) AccentPrimary else TextSecondary.copy(alpha = 0.3f),
                    activeTrackColor = if (enabled) {
                        if (gain > 0) AccentPrimary else AccentSecondary
                    } else TextSecondary.copy(alpha = 0.2f),
                    inactiveTrackColor = TrackInactive
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Icône poubelle à droite
        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = TextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(36.dp))
        }
    }
}

// Format fréquence (31.5 Hz, 1 k, 14 k)
private fun formatFrequency(freq: Float): String {
    return when {
        freq >= 1000 -> "${(freq / 1000).toInt()} kHz"
        freq % 1 == 0f -> "${freq.toInt()} Hz"
        else -> "${"%.1f".format(freq)} Hz"
    }
}

// Format gain (+12.0 dB, -11.8 dB, 0.0 dB)
private fun formatGain(gain: Float): String {
    val sign = if (gain > 0) "+" else ""
    return "$sign${"%.1f".format(gain)} dB"
}