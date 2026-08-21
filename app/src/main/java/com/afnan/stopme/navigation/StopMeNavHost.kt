package com.afnan.stopme.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.afnan.stopme.domain.repository.SettingsRepository
import com.afnan.stopme.feature.apps.AppsScreen
import com.afnan.stopme.feature.charts.ChartsScreen
import com.afnan.stopme.feature.onboarding.PermissionSetupScreen
import com.afnan.stopme.feature.settings.SettingsScreen
import com.afnan.stopme.feature.settings.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun StopMeNavHost() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val uiState by settingsViewModel.uiState.collectAsState()

    // Determine start destination based on onboarding completion
    val startDestination = if (uiState.settings.onboardingComplete)
        NavRoutes.APPS
    else
        NavRoutes.ONBOARDING

    val bottomNavRoutes = setOf(NavRoutes.APPS, NavRoutes.CHARTS, NavRoutes.SETTINGS)
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavItem.items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (!uiState.isLoading) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(NavRoutes.ONBOARDING) {
                    PermissionSetupScreen(
                        onGetStarted = {
                            navController.navigate(NavRoutes.APPS) {
                                popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavRoutes.APPS) {
                    AppsScreen()
                }

                composable(NavRoutes.CHARTS) {
                    ChartsScreen()
                }

                composable(NavRoutes.SETTINGS) {
                    SettingsScreen()
                }
            }
        }
    }
}
