package com.javis.launcher.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "Sir",
    val nickname: String = "",
    val preferences: String = "{}",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val userId: Long,
    val title: String,
    val context: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = ConversationEntity::class,
        parentColumns = ["id"],
        childColumns = ["conversationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val role: String, // "user" | "assistant" | "system"
    val content: String,
    val provider: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val type: String, // "short_term" | "long_term" | "routine"
    val key: String,
    val value: String,
    val importance: Int = 5,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val accessedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val userId: Long,
    val intent: String,
    val plan: String, // JSON serialized TaskPlan
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val hour: Int,
    val minute: Int,
    val isRepeating: Boolean = false,
    val repeatDays: String = "", // comma-separated day ints
    val isEnabled: Boolean = true,
    val isWakeUp: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val command: String,
    val result: String = "",
    val success: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notification_history")
data class NotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long? = null,
    val type: String,
    val message: String,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "installed_apps")
data class InstalledAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val category: String = "General",
    val launchCount: Int = 0,
    val lastLaunched: Long? = null,
    val isFavorite: Boolean = false
)
