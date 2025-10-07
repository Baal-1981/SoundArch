package com.soundarch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun EqualizerGraph(
    gains: List<Float>,
    modifier: Modifier = Modifier
) {
    val bandCount = gains.size
    val bandRange = 12f  // ±12 dB
    val pointRadius = 8f
    val curveColor = Color(0xFF80D8FF)

    // 🎯 Labels optimisés pour 10 bandes
    val freqLabels = when (bandCount) {
        10 -> listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
        5 -> listOf("60", "230", "910", "3.6k", "14k")
        else -> (0 until bandCount).map { "${it + 1}" }
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)  // Réduit pour laisser place aux sliders
    ) {
        val spacing = if (bandCount > 1) {
            size.width / (bandCount - 1).toFloat()
        } else {
            size.width / 2f
        }
        val heightPx = size.height

        // 🎨 Fond dégradé
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1B1B1B), Color(0xFF232323))
            ),
            size = size
        )

        // 🧭 Grille horizontale (tous les 6dB)
        val gridStep = 6
        val textStyle = TextStyle(color = Color.Gray, fontSize = 10.sp)

        for (db in -bandRange.toInt()..bandRange.toInt() step gridStep) {
            val y = heightPx / 2f - (db / bandRange) * (heightPx / 2f)

            // Ligne de grille
            drawLine(
                color = Color(0x22FFFFFF),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )

            // Label dB
            val label = if (db > 0) "+$db" else db.toString()
            val textLayout = textMeasurer.measure(label, textStyle)
            drawText(
                textMeasurer,
                label,
                Offset(4f, y - textLayout.size.height / 2f),
                textStyle
            )
        }

        // 🎚️ Points EQ
        val points = gains.mapIndexed { i, gain ->
            val x = i.toFloat() * spacing
            val y = heightPx / 2f - (gain / bandRange) * (heightPx / 2f)
            Offset(x, y)
        }

        // 🎵 Courbe lisse
        if (points.size >= 2) {
            val path = Path()
            path.moveTo(points.first().x, points.first().y)

            if (points.size == 2) {
                path.lineTo(points.last().x, points.last().y)
            } else {
                // Courbe Catmull-Rom pour smooth interpolation
                for (i in 0 until points.size - 1) {
                    val p0 = points[max(0, i - 1)]
                    val p1 = points[i]
                    val p2 = points[min(points.size - 1, i + 1)]
                    val p3 = points[min(points.size - 1, i + 2)]

                    for (t in 0..20) {
                        val tt = t / 20f
                        val tt2 = tt * tt
                        val tt3 = tt2 * tt

                        val x = 0.5f * ((2 * p1.x) + (-p0.x + p2.x) * tt +
                                (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * tt2 +
                                (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * tt3)

                        val y = 0.5f * ((2 * p1.y) + (-p0.y + p2.y) * tt +
                                (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * tt2 +
                                (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * tt3)

                        path.lineTo(x, y)
                    }
                }
            }

            // 💡 Halo glow
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x5520E0FF), Color.Transparent)
                ),
                style = Stroke(width = 10f)
            )

            // 🎧 Courbe principale
            drawPath(
                path = path,
                color = curveColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }

        // 🟢 Points visuels
        points.forEach { point ->
            drawCircle(
                color = Color(0xFF81D4FA),
                radius = pointRadius,
                center = point
            )
        }

        // 📊 Labels fréquences
        val freqStyle = TextStyle(color = Color.LightGray, fontSize = 9.sp)
        freqLabels.take(points.size).forEachIndexed { i, label ->
            val textLayout = textMeasurer.measure(label, freqStyle)
            val x = points[i].x - textLayout.size.width / 2f
            val y = size.height - textLayout.size.height - 2f
            drawText(textMeasurer, label, Offset(x, y), freqStyle)
        }
    }
}