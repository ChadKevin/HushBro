package com.invictus.hushbro.di

import android.content.Context
import com.invictus.hushbro.data.db.HushBroDatabase
import com.invictus.hushbro.data.db.dao.AppPrefDao
import com.invictus.hushbro.data.db.dao.NotificationDao
import com.invictus.hushbro.data.db.dao.RuleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HushBroDatabase =
        HushBroDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideNotificationDao(database: HushBroDatabase): NotificationDao =
        database.notificationDao()

    @Provides
    @Singleton
    fun provideRuleDao(database: HushBroDatabase): RuleDao =
        database.ruleDao()

    @Provides
    @Singleton
    fun provideAppPrefDao(database: HushBroDatabase): AppPrefDao =
        database.appPrefDao()
}
