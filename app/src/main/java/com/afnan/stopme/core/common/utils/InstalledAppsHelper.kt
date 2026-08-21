package com.afnan.stopme.core.common.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

/**
 * Lightweight wrapper for querying installed applications.
 */
object InstalledAppsHelper {

    /**
     * Returns all user-accessible applications (apps with launcher icons + user-installed apps),
     * excluding Stop-me itself, sorted alphabetically by label.
     */
    fun getInstalledUserApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val selfPackage = context.packageName

        val appsMap = mutableMapOf<String, InstalledAppInfo>()

        // 1. Query all apps that have a launcher activity (covers YouTube, Chrome, Instagram, etc. even if system-installed)
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == selfPackage) continue
            try {
                val label = resolveInfo.loadLabel(pm).toString()
                val icon = resolveInfo.loadIcon(pm)
                appsMap[pkg] = InstalledAppInfo(packageName = pkg, label = label, icon = icon)
            } catch (e: Exception) {
                // Ignore failure for individual app
            }
        }

        // 2. Query other non-system installed apps that might not have a standard launcher intent
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (appInfo in installedApps) {
            val pkg = appInfo.packageName
            if (pkg == selfPackage || appsMap.containsKey(pkg)) continue
            if (isUserApp(appInfo)) {
                try {
                    val label = pm.getApplicationLabel(appInfo).toString()
                    val icon = pm.getApplicationIcon(pkg)
                    appsMap[pkg] = InstalledAppInfo(packageName = pkg, label = label, icon = icon)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        return appsMap.values.sortedBy { it.label.lowercase() }
    }

    /**
     * Returns the display name for a package, or null if not installed.
     */
    fun getAppLabel(context: Context, packageName: String): String? {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Returns the app icon for a package, or null if not installed.
     */
    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Returns true if the given package is currently installed.
     */
    fun isInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * A valid Android package name: alphanumeric segments separated by dots,
     * at least two segments, each segment starts with a letter.
     */
    fun isValidPackageName(packageName: String): Boolean {
        val segments = packageName.split(".")
        if (segments.size < 2) return false
        return segments.all { segment ->
            segment.isNotEmpty() && segment[0].isLetter() &&
                segment.all { it.isLetterOrDigit() || it == '_' }
        }
    }

    private fun isUserApp(appInfo: ApplicationInfo): Boolean {
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdated = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return !isSystem || isUpdated
    }
}

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

