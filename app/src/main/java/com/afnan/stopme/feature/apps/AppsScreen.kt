package com.afnan.stopme.feature.apps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afnan.stopme.feature.overlay.TwoPhaseChallengeDialog
import com.afnan.stopme.core.common.utils.toMinuteString
import com.afnan.stopme.core.common.utils.toRemainingString
import com.afnan.stopme.core.ui.AppIconView
import com.afnan.stopme.domain.model.AppWithUsage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    viewModel: AppsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Apps",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.totalUsedMillisToday > 0) {
                            Text(
                                text = "Today · ${uiState.totalUsedMillisToday.toMinuteString()} total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAppSelector() }) {
                Icon(Icons.Default.Add, contentDescription = "Select apps")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.apps.isEmpty() && !uiState.isLoading) {
                EmptyAppsState(onSelectApps = { viewModel.showAppSelector() })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(
                        items = uiState.apps,
                        key = { it.packageName }
                    ) { appWithUsage ->
                        AppUsageRow(
                            appWithUsage = appWithUsage,
                            onLongPress = {
                                viewModel.onAppLongClick(appWithUsage)
                            }
                        )
                    }
                }
            }
        }
    }

    // App selector sheet
    if (uiState.showAppSelector) {
        AppSelectorSheet(
            onDismiss = { viewModel.hideAppSelector() },
            onPackageSelected = { pkg -> viewModel.addPackage(pkg) }
        )
    }

    // Reset Countdown Two-Phase Challenge Dialog (150 taps + writing pledge) on Long Press
    uiState.selectedAppForReset?.let { app ->
        TwoPhaseChallengeDialog(
            packageLabel = app.label,
            onSuccess = { viewModel.confirmResetCountdown() },
            onDismiss = { viewModel.dismissResetDialog() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppUsageRow(
    appWithUsage: AppWithUsage,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fractionAnim by animateFloatAsState(
        targetValue = appWithUsage.usage.fractionUsed,
        animationSpec = spring(),
        label = "progress_${appWithUsage.packageName}"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Real App Icon
            AppIconView(
                packageName = appWithUsage.packageName,
                size = 44.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appWithUsage.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = appWithUsage.usage.remainingMillis.toRemainingString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (appWithUsage.usage.isExhausted)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { fractionAnim },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (appWithUsage.usage.isExhausted)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${appWithUsage.usage.usedMillis.toMinuteString()} used",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyAppsState(onSelectApps: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No apps selected",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Select the apps you want\nStop me to limit.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onSelectApps) {
            Text("Select apps")
        }
    }
}
