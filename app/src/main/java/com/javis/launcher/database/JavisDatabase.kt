package com.javis.launcher.database

import android.content.Context
import androidx.room.*
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.javis.launcher.database.dao.*
import com.javis.launcher.database.entities.*
import java.io.File

@Database(
    entities = [
        UserEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        TaskEntity::class,
        AlarmEntity::class,
        CommandHistoryEntity::class,
        NotificationHistoryEntity::class,
        LogEntity::class,
        InstalledAppEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class JavisDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun taskDao(): TaskDao
    abstract fun alarmDao(): AlarmDao
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun notificationHistoryDao(): NotificationHistoryDao
    abstract fun logDao(): LogDao
    abstract fun installedAppDao(): InstalledAppDao

    companion object {
        @Volatile private var INSTANCE: JavisDatabase? = null

        fun getInstance(context: Context): JavisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JavisDatabase::class.java,
                    "javis_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter fun fromList(list: List<String>): String = list.joinToString(",")
    @TypeConverter fun toList(str: String): List<String> = if (str.isEmpty()) emptyList() else str.split(",")
}
