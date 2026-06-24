package com.javis.launcher.models

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isFavorite: Boolean = false,
    val lastUsed: Long = 0L
)

data class ContactInfo(
    val id: String,
    val name: String,
    val phone: String,
    val photoUri: String? = null
)

data class OrbState(
    val animState: OrbAnimationState,
    val statusText: String = "",
    val pulseIntensity: Float = 0.5f
)

enum class OrbAnimationState { IDLE, LISTENING, THINKING, EXECUTING, SPEAKING, COMPLETED, ERROR }

data class AIResponse(
    val text: String,
    val provider: String,
    val model: String,
    val tokensUsed: Int = 0,
    val latencyMs: Long = 0L
)

data class ParsedIntent(
    val type: IntentType,
    val params: Map<String, String> = emptyMap(),
    val rawText: String,
    val confidence: Float = 1.0f
)

enum class IntentType {
    OPEN_APP, CALL_CONTACT, SEND_MESSAGE, SET_ALARM, DELETE_ALARM, LIST_ALARMS,
    GET_NOTIFICATIONS, SEARCH_CONTACT, CHAT_GENERAL, SEARCH_APP,
    GET_MEMORY, SET_MEMORY, OPEN_SETTINGS, OPEN_SCREEN,
    SYSTEM_STATUS, HELP, UNKNOWN
}

data class NotificationItem(
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

sealed class LaunchResult {
    data class Success(val appName: String) : LaunchResult()
    data class NotFound(val query: String) : LaunchResult()
    data class NotLaunchable(val appName: String) : LaunchResult()
}

sealed class AlarmResult {
    data class Success(val id: Int, val hour: Int, val minute: Int, val label: String) : AlarmResult()
    data class Failed(val reason: String) : AlarmResult()
}

data class VoiceProfile(
    val id: String,
    val name: String,
    val type: VoiceType,
    val audioPath: String? = null
)

enum class VoiceType { KOKORO_MALE, KOKORO_FEMALE, PIPER_MALE, PIPER_FEMALE, CLONED }

data class MemoryEntry(
    val key: String,
    val value: String,
    val category: MemoryCategory,
    val confidence: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MemoryCategory {
    PERSONAL, PREFERENCES, HABITS, GOALS, CONTACTS, APPS, CONVERSATION, ROUTINE
}

data class TaskPlan(
    val steps: List<TaskStep>,
    val description: String,
    val estimatedDuration: Int = 0
)

data class TaskStep(
    val action: String,
    val params: Map<String, String>,
    val order: Int,
    val isVerifiable: Boolean = false
)
