package com.afnan.stopme.feature.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afnan.stopme.core.common.utils.InstalledAppsHelper
import com.afnan.stopme.core.common.utils.DAILY_LIMIT_MILLIS
import com.afnan.stopme.core.common.utils.toMinuteString
import com.afnan.stopme.domain.model.DailyUsage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    viewModel: ChartsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Charts",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // ─── Today section ────────────────────────────────────────────
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            val totalToday = uiState.todayUsage.sumOf { it.usedMillis }
            Text(
                text = totalToday.toMinuteString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(20.dp))

            if (uiState.todayUsage.isEmpty()) {
                Text(
                    text = "No usage data yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxMillis = uiState.todayUsage.maxOf { it.usedMillis }.coerceAtLeast(1L)
                uiState.todayUsage.sortedByDescending { it.usedMillis }.forEach { usage ->
                    val label = InstalledAppsHelper.getAppLabel(context, usage.packageName)
                        ?: usage.packageName
                    HorizontalBar(
                        label = label,
                        usedMillis = usage.usedMillis,
                        maxMillis = DAILY_LIMIT_MILLIS,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(32.dp))

            // ─── Weekly section ───────────────────────────────────────────
            Text(
                text = "This week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            if (uiState.weeklyUsage.isEmpty()) {
                Text(
                    text = "No weekly data yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                WeeklyBarChart(
                    weeklyData = uiState.weeklyUsage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HorizontalBar(
    label: String,
    usedMillis: Long,
    maxMillis: Long,
    modifier: Modifier = Modifier
) {
    val fraction = (usedMillis.toFloat() / maxMillis.toFloat()).coerceIn(0f, 1f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = usedMillis.toMinuteString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            val trackWidth = size.width
            val barHeight = size.height
            // Track
            drawRect(
                color = surfaceVariantColor,
                topLeft = Offset(0f, 0f),
                size = Size(trackWidth, barHeight)
            )
            // Fill
            drawRect(
                color = primaryColor,
                topLeft = Offset(0f, 0f),
                size = Size(trackWidth * fraction, barHeight)
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(
    weeklyData: Map<String, List<DailyUsage>>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val dates = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val dayFormatter = DateTimeFormatter.ofPattern("EEE")

    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val maxDayMillis = dates.maxOf { date ->
        weeklyData[date.toString()]?.sumOf { it.usedMillis } ?: 0L
    }.coerceAtLeast(DAILY_LIMIT_MILLIS)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        dates.forEach { date ->
            val dayTotal = weeklyData[date.toString()]?.sumOf { it.usedMillis } ?: 0L
            val fraction = (dayTotal.toFloat() / maxDayMillis.toFloat()).coerceIn(0f, 1f)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Canvas(
                    modifier = Modifier
                        .width(24.dp)
                        .height(140.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    // Track
                    drawRect(color = trackColor, topLeft = Offset(0f, 0f), size = Size(w, h))
                    // Bar from bottom
                    val barH = h * fraction
                    drawRect(
                        color = barColor,
                        topLeft = Offset(0f, h - barH),
                        size = Size(w, barH)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = date.format(dayFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }
        }
    }
}
