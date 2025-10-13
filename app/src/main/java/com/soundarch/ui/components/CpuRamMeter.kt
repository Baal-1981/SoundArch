package com.soundarch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundarch.ui.testing.UiIds
import com.soundarch.ui.theme.AppColors

/**
 * CPU & RAM Performance Meter - Compact bar format
 *
 * Displays system CPU and RAM usage with color-coded values:
 * - Green: < 60% (good)
 * - Yellow: 60-80% (moderate)
 * - Red: > 80% (high)
 *
 * @param cpuPercent CPU usage percentage (0-100)
 * @param ramPercent RAM usage percentage (0-100)
 * @param ramUsedMB RAM used in megabytes (unused in compact format)
 * @param ramTotalMB Total RAM in megabytes (unused in compact format)
 * @param modifier Optional modifier
 */
@Composable
fun CpuRamMeter(
    cpuPercent: Float,
    ramPercent: Float,
    ramUsedMB: Long,
    ramTotalMB: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(AppColors.BackgroundSecondary, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(UiIds.Home.CPU_RAM_METER),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // CPU
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CPU:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = String.format("%.1f%%", cpuPercent),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = getUsageColor(cpuPercent),
                fontSize = 13.sp,
                modifier = Modifier.testTag("home_cpu_value_text")
            )
        }

        // Separator
        Text(
            text = "•",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextDisabled,
            fontSize = 10.sp
        )

        // RAM
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RAM:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = String.format("%.1f%%", ramPercent),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = getUsageColor(ramPercent),
                fontSize = 13.sp,
                modifier = Modifier.testTag("home_ram_value_text")
            )
        }
    }
}

/**
 * Get color based on usage percentage
 * - Green: < 60%
 * - Yellow: 60-80%
 * - Red: > 80%
 */
private fun getUsageColor(percent: Float): Color {
    return when {
        percent < 60f -> AppColors.Success   // Green (good)
        percent < 80f -> AppColors.Warning   // Yellow (moderate)
        else -> AppColors.Error               // Red (high)
    }
}
