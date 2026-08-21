package com.afnan.stopme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.afnan.stopme.core.designsystem.theme.StopMeTheme
import com.afnan.stopme.domain.model.AppTheme
import com.afnan.stopme.feature.settings.SettingsViewModel
import com.afnan.stopme.navigation.StopMeNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val uiState by settingsViewModel.uiState.collectAsState()

            StopMeTheme(
                theme = uiState.settings.theme
            ) {
                StopMeNavHost()
            }
        }
    }
}
