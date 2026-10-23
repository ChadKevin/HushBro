package com.invictus.hushbro.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.invictus.hushbro.data.db.converter.RoomConverters
import com.invictus.hushbro.data.db.dao.AppPrefDao
import com.invictus.hushbro.data.db.dao.NotificationDao
import com.invictus.hushbro.data.db.dao.RuleDao
import com.invictus.hushbro.data.db.entity.AppPrefEntity
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.entity.RuleEntity

@Database(
    entities = [
        NotificationEntity::class,
        RuleEntity::class,
        AppPrefEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class HushBroDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao
    abstract fun ruleDao(): RuleDao
    abstract fun appPrefDao(): AppPrefDao

    companion object {
        const val DATABASE_NAME = "hushbro.db"

        @Volatile
        private var INSTANCE: HushBroDatabase? = null

        fun getInstance(context: Context): HushBroDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HushBroDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // Dev-only fallback per build guide
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
