package com.afnan.stopme.feature.overlay

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afnan.stopme.core.common.utils.toCountdownString
import com.afnan.stopme.domain.model.CountdownStyle
import com.afnan.stopme.service.overlay.WarningOverlayState

/**
 * Compact warning banner displayed during the last 30 seconds.
 * Appears at the top of the screen without blocking interaction.
 */
@Composable
fun WarningOverlayContent(
    state: WarningOverlayState,
    modifier: Modifier = Modifier
) {
    when (state.countdownStyle) {
        CountdownStyle.PILL -> PillCountdown(state.remainingMillis, modifier)
        CountdownStyle.MINIMAL -> MinimalCountdown(state.remainingMillis, modifier)
        CountdownStyle.BOLD -> BoldCountdown(state.remainingMillis, modifier)
    }
}

@Composable
private fun PillCountdown(remainingMillis: Long, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(top = 16.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f),
        tonalElevation = 4.dp
    ) {
        Text(
            text = remainingMillis.toCountdownString(),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun MinimalCountdown(remainingMillis: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 20.dp, end = 16.dp)
            .widthIn(min = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = remainingMillis.toCountdownString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun BoldCountdown(remainingMillis: Long, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(top = 12.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Text(
            text = remainingMillis.toCountdownString(),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onError,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
    }
}
