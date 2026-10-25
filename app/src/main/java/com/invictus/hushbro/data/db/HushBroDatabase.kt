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
            return try {
                val passphrase = "hushbro-dev-key".toByteArray()
                val factory = net.zetetic.database.sqlcipher.SupportOpenHelperFactory(passphrase)
                INSTANCE ?: synchronized(this) {
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        HushBroDatabase::class.java,
                        DATABASE_NAME
                    )
                        .openHelperFactory(factory)
                        .fallbackToDestructiveMigration()
                        .build()
                    // Force an eager open so SQLCipher native failures happen here,
                    // where they can be caught and handled, rather than on a
                    // background query thread where they crash the app.
                    db.openHelper.writableDatabase
                    INSTANCE = db
                    db
                }
            } catch (e: Throwable) {
                // SQLCipher failed (e.g., native lib missing, passphrase wrong).
                // Delete the potentially partial database and fall back to plain Room.
                context.deleteDatabase(DATABASE_NAME)
                providePlainRoomFallback(context)
            }
        }

        private fun providePlainRoomFallback(context: Context): HushBroDatabase {
            return try {
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Room.databaseBuilder(
                        context.applicationContext,
                        HushBroDatabase::class.java,
                        DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { INSTANCE = it }
                }
            } catch (e: Throwable) {
                // Even plain Room failed. Delete any leftover database and try one final time.
                context.deleteDatabase(DATABASE_NAME)
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Room.databaseBuilder(
                        context.applicationContext,
                        HushBroDatabase::class.java,
                        DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { INSTANCE = it }
                }
            }
        }
    }
}
