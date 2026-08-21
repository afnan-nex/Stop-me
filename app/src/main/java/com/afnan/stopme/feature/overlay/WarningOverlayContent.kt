package com.afnan.stopme.feature.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afnan.stopme.core.common.utils.toCountdownString
import com.afnan.stopme.domain.model.CountdownStyle
import com.afnan.stopme.service.overlay.WarningOverlayState

/**
 * Warning banner displayed during the last 30 seconds.
 * Always centered at the top of the screen in proper compact Pill, Minimal, or Bold styles.
 */
@Composable
fun WarningOverlayContent(
    state: WarningOverlayState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        when (state.countdownStyle) {
            CountdownStyle.PILL -> PillCountdown(state.remainingMillis)
            CountdownStyle.MINIMAL -> MinimalCountdown(state.remainingMillis)
            CountdownStyle.BOLD -> BoldCountdown(state.remainingMillis)
        }
    }
}

@Composable
private fun PillCountdown(remainingMillis: Long) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
        shadowElevation = 6.dp,
        tonalElevation = 6.dp
    ) {
        Text(
            text = remainingMillis.toCountdownString(),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MinimalCountdown(remainingMillis: Long) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        shadowElevation = 2.dp
    ) {
        Text(
            text = remainingMillis.toCountdownString(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BoldCountdown(remainingMillis: Long) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.error,
        shadowElevation = 8.dp,
        tonalElevation = 8.dp
    ) {
        Text(
            text = remainingMillis.toCountdownString(),
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onError,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center
        )
    }
}
