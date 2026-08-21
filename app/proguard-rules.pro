# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep Room entities and DAOs
-keep class com.afnan.stopme.data.local.entities.** { *; }
-keep class com.afnan.stopme.data.local.dao.** { *; }

# Keep serialization models for backup
-keep class com.afnan.stopme.data.backup.** { *; }
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.afnan.stopme.**$$serializer { *; }
-keepclassmembers class com.afnan.stopme.** {
    *** Companion;
}
-keepclasseswithmembers class com.afnan.stopme.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Accessibility service
-keep class com.afnan.stopme.service.accessibility.StopMeAccessibilityService { *; }
