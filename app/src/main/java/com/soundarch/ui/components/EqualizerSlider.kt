package com.soundarch.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundarch.ui.testing.UiIds
import com.soundarch.ui.theme.AppColors

@Composable
fun EqualizerSlider(
    gain: Float,
    frequency: Float,
    onGainChange: (Float) -> Unit,
    bandIndex: Int = -1,  // Optional: band index for test tags
    // ⭐ NEW: Optional test tag parameter for value text
    valueTextTestTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .then(
                if (bandIndex >= 0) {
                    Modifier.testTag("eq_band_slider_$bandIndex")
                } else {
                    Modifier
                }
            ),
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
            color = AppColors.Accent,  // Bleu clair
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
                thumbColor = AppColors.Info,        // Bleu
                activeTrackColor = AppColors.Info.copy(alpha = 0.7f),  // Bleu clair
                inactiveTrackColor = AppColors.GridLines // Gris foncé
            ),
            modifier = Modifier.weight(1f)  // Prend tout l'espace disponible
        )

        // 📊 Label gain (droite) - VALUE TEXT
        val gainText = when {
            gain > 0 -> "+${String.format("%.1f", gain)}"
            else -> String.format("%.1f", gain)
        }

        Text(
            text = "$gainText dB",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            color = when {
                gain > 0 -> AppColors.Success   // Vert si boost
                gain < 0 -> AppColors.Error   // Rouge si cut
                else -> AppColors.TextSecondary               // Gris si 0dB
            },
            modifier = Modifier
                .width(60.dp)
                .then(
                    if (valueTextTestTag != null) {
                        Modifier.testTag(valueTextTestTag)
                    } else {
                        Modifier
                    }
                )
        )
    }
}