package com.javis.launcher.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.javis.launcher.database.dao.*
import com.javis.launcher.database.entities.*

@Database(
    entities = [
        MemoryEntity::class,
        ConversationEntity::class,
        AlarmEntity::class,
        InstalledAppEntity::class,
        CommandLogEntity::class,
        NotificationCacheEntity::class,
        VoiceProfileEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JavisDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun conversationDao(): ConversationDao
    abstract fun alarmDao(): AlarmDao
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun commandLogDao(): CommandLogDao
    abstract fun notificationCacheDao(): NotificationCacheDao
    abstract fun voiceProfileDao(): VoiceProfileDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var INSTANCE: JavisDatabase? = null

        fun getInstance(context: Context): JavisDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    JavisDatabase::class.java,
                    "javis_v06.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
    }
}
