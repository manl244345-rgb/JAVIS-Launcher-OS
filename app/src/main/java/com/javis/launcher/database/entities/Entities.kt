package com.javis.launcher.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_entries")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val key: String,
    val value: String,
    val category: String,
    val confidence: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "",
    val model: String = ""
)

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val isRepeating: Boolean = false,
    val repeatDays: String = "",
    val isWakeUp: Boolean = false,
    val ringtoneUri: String = "",
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "installed_apps")
data class InstalledAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isFavorite: Boolean = false,
    val lastUsed: Long = 0L,
    val useCount: Int = 0,
    val category: String = "other"
)

@Entity(tableName = "command_log")
data class CommandLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val command: String,
    val intentType: String,
    val result: String,
    val success: Boolean,
    val provider: String = "",
    val latencyMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications_cache")
data class NotificationCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "voice_profiles")
data class VoiceProfileEntity(
    @PrimaryKey val profileId: String,
    val name: String,
    val type: String,
    val audioPath: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val dueTime: Long = 0L,
    val isCompleted: Boolean = false,
    val priority: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
