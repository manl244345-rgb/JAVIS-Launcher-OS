package com.javis.launcher.models

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val category: String = "General",
    val launchCount: Int = 0
)

data class ContactInfo(
    val id: String,
    val name: String,
    val phone: String,
    val email: String = "",
    val isFavorite: Boolean = false
)

data class NotificationSummary(
    val packageName: String,
    val appName: String,
    val count: Int,
    val latestText: String,
    val timestamp: Long
)

data class TaskPlan(
    val id: String,
    val intent: String,
    val steps: List<TaskStep>,
    val priority: Int = 0,
    val status: TaskStatus = TaskStatus.PENDING
)

data class TaskStep(
    val id: String,
    val description: String,
    val action: String,
    val params: Map<String, String> = emptyMap(),
    val status: StepStatus = StepStatus.PENDING
)

enum class TaskStatus { PENDING, RUNNING, COMPLETED, FAILED }
enum class StepStatus { PENDING, RUNNING, COMPLETED, FAILED, SKIPPED }

data class AIResponse(
    val text: String,
    val provider: String,
    val isOffline: Boolean,
    val taskPlan: TaskPlan? = null,
    val shouldSpeak: Boolean = true
)

data class VoiceProfile(
    val id: String,
    val name: String,
    val type: VoiceType,
    val filePath: String? = null,
    val isDownloaded: Boolean = false
)

enum class VoiceType { OFFLINE_PIPER, OFFLINE_KOKORO, ONLINE_FISH_SPEECH, ANDROID_TTS, CLONED }

data class AlarmInfo(
    val id: Int,
    val label: String,
    val hour: Int,
    val minute: Int,
    val isRepeating: Boolean,
    val repeatDays: List<Int> = emptyList(),
    val isEnabled: Boolean = true,
    val isWakeUp: Boolean = false
)

data class OrbState(
    val state: OrbAnimationState,
    val message: String = ""
)

enum class OrbAnimationState {
    IDLE, LISTENING, THINKING, EXECUTING, SPEAKING, COMPLETED, ERROR
}

data class SystemStatus(
    val batteryPercent: Int,
    val networkConnected: Boolean,
    val currentAI: String,
    val currentVoice: String,
    val memoryUsageMb: Long,
    val activeTaskCount: Int
)
