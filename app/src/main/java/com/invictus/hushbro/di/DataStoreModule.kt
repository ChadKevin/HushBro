package com.invictus.hushbro.di

import android.content.Context
import com.invictus.hushbro.data.prefs.SettingsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSettingsStore(@ApplicationContext context: Context): SettingsStore =
        SettingsStore(context)
}
