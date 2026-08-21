package com.afnan.stopme.navigation

import androidx.annotation.DrawableRes
import com.afnan.stopme.R

sealed class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    object Apps : BottomNavItem(NavRoutes.APPS, "Apps", R.drawable.ic_nav_apps)
    object Charts : BottomNavItem(NavRoutes.CHARTS, "Charts", R.drawable.ic_nav_charts)
    object Settings : BottomNavItem(NavRoutes.SETTINGS, "Settings", R.drawable.ic_nav_settings)

    companion object {
        val items = listOf(Apps, Charts, Settings)
    }
}
