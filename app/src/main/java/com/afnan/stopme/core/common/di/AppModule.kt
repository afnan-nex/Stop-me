package com.afnan.stopme.core.common.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.afnan.stopme.data.local.database.StopMeDatabase
import com.afnan.stopme.data.repository.ProtectedAppRepositoryImpl
import com.afnan.stopme.data.repository.SettingsRepositoryImpl
import com.afnan.stopme.data.repository.UsageRepositoryImpl
import com.afnan.stopme.domain.repository.ProtectedAppRepository
import com.afnan.stopme.domain.repository.SettingsRepository
import com.afnan.stopme.domain.repository.UsageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stop_me_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StopMeDatabase =
        Room.databaseBuilder(context, StopMeDatabase::class.java, "stop_me.db")
            .fallbackToDestructiveMigrationFrom() // Safe for v1, replace with proper migrations later
            .build()

    @Provides
    @Singleton
    fun provideProtectedAppDao(db: StopMeDatabase) = db.protectedAppDao()

    @Provides
    @Singleton
    fun provideDailyUsageDao(db: StopMeDatabase) = db.dailyUsageDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideProtectedAppRepository(
        impl: ProtectedAppRepositoryImpl
    ): ProtectedAppRepository = impl

    @Provides
    @Singleton
    fun provideUsageRepository(
        impl: UsageRepositoryImpl
    ): UsageRepository = impl

    @Provides
    @Singleton
    fun provideSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository = impl
}
