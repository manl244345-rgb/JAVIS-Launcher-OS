package com.javis.launcher.ai

import com.javis.launcher.models.*

enum class IntentType {
    OPEN_APP, CALL_CONTACT, SEND_MESSAGE, SET_ALARM, DELETE_ALARM,
    GET_NOTIFICATIONS, SEARCH_CONTACT, CHAT_GENERAL, SEARCH_APP,
    GET_MEMORY, SET_MEMORY, GENERATE_IMAGE, SHOW_APPS,
    SHOW_MEMORY, SHOW_SETTINGS, SYSTEM_STATUS, UNKNOWN
}

data class ParsedIntent(
    val type: IntentType,
    val params: Map<String, String> = emptyMap(),
    val rawText: String,
    val confidence: Float = 1.0f
)

object IntentAnalyzer {

    private val appOpenPatterns = listOf(
        Regex("(?:open|launch|start|run|start up)\\s+(.+)", RegexOption.IGNORE_CASE),
        Regex("(.+)\\s+(?:khul|open karein|launch karein)", RegexOption.IGNORE_CASE)
    )
    private val callPatterns = listOf(
        Regex("(?:call|ring|dial|phone)\\s+(.+)", RegexOption.IGNORE_CASE),
        Regex("(.+)\\s+(?:ko call|ko ring|ko phone)", RegexOption.IGNORE_CASE)
    )
    private val alarmPatterns = listOf(
        Regex("(?:set|create|add|make)\\s+(?:an?\\s+)?alarm(?:\\s+for)?\\s+(.+)", RegexOption.IGNORE_CASE),
        Regex("wake me (?:up )?(?:at )?(.+)", RegexOption.IGNORE_CASE),
        Regex("alarm\\s+(?:at )?(.+)", RegexOption.IGNORE_CASE)
    )
    private val deleteAlarmPatterns = listOf(
        Regex("(?:delete|remove|cancel|dismiss)\\s+(?:the )?alarm(?:\\s+for)?\\s*(.+)?", RegexOption.IGNORE_CASE),
        Regex("alarm\\s+(?:band|delete|cancel|hatao)\\s*(.+)?", RegexOption.IGNORE_CASE)
    )
    private val messagePatterns = listOf(
        Regex("(?:send|message|text|whatsapp)\\s+(.+?)\\s+(?:saying|that|:)?\\s*(.+)?", RegexOption.IGNORE_CASE)
    )
    private val notificationKeywords = listOf("notification", "notify", "message", "messages", "unread", "inbox")
    private val contactKeywords = listOf("find contact", "search contact", "who is", "contact")
    private val memoryKeywords = listOf("remember", "recall", "what do you know", "my name", "my preference")

    fun analyze(text: String): ParsedIntent {
        val lower = text.lowercase().trim()

        // App opening
        for (pattern in appOpenPatterns) {
            pattern.find(lower)?.let { match ->
                val appName = match.groupValues[1].trim()
                if (appName.isNotBlank()) {
                    return ParsedIntent(IntentType.OPEN_APP, mapOf("app" to appName), text)
                }
            }
        }

        // Call contact
        for (pattern in callPatterns) {
            pattern.find(lower)?.let { match ->
                val contact = match.groupValues[1].trim()
                if (contact.isNotBlank()) {
                    return ParsedIntent(IntentType.CALL_CONTACT, mapOf("contact" to contact), text)
                }
            }
        }

        // Set alarm
        for (pattern in alarmPatterns) {
            pattern.find(lower)?.let { match ->
                val timeStr = match.groupValues[1].trim()
                if (timeStr.isNotBlank()) {
                    val (hour, minute) = parseTime(timeStr)
                    if (hour >= 0) {
                        return ParsedIntent(
                            IntentType.SET_ALARM,
                            mapOf("time" to timeStr, "hour" to hour.toString(), "minute" to minute.toString()),
                            text
                        )
                    }
                }
            }
        }

        // Delete alarm
        for (pattern in deleteAlarmPatterns) {
            if (pattern.containsMatchIn(lower)) {
                val match = pattern.find(lower)
                return ParsedIntent(IntentType.DELETE_ALARM, mapOf("time" to (match?.groupValues?.getOrNull(1) ?: "")), text)
            }
        }

        // Send message
        for (pattern in messagePatterns) {
            pattern.find(lower)?.let { match ->
                return ParsedIntent(
                    IntentType.SEND_MESSAGE,
                    mapOf("contact" to match.groupValues[1].trim(), "message" to (match.groupValues.getOrNull(2) ?: "").trim()),
                    text
                )
            }
        }

        // Notifications
        if (notificationKeywords.any { lower.contains(it) }) {
            return ParsedIntent(IntentType.GET_NOTIFICATIONS, emptyMap(), text)
        }

        // Contact search
        if (contactKeywords.any { lower.contains(it) }) {
            return ParsedIntent(IntentType.SEARCH_CONTACT, mapOf("query" to text), text)
        }

        // Memory
        if (memoryKeywords.any { lower.contains(it) }) {
            return ParsedIntent(IntentType.GET_MEMORY, mapOf("query" to text), text)
        }

        // App list
        if (lower.contains("all app") || lower.contains("show app") || lower.contains("installed")) {
            return ParsedIntent(IntentType.SHOW_APPS, emptyMap(), text)
        }

        return ParsedIntent(IntentType.CHAT_GENERAL, emptyMap(), text)
    }

    fun parseTime(timeStr: String): Pair<Int, Int> {
        val str = timeStr.lowercase().trim()
        val timeRegex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        val match = timeRegex.find(str) ?: return Pair(-1, 0)
        var hour = match.groupValues[1].toIntOrNull() ?: return Pair(-1, 0)
        val minute = match.groupValues[2].toIntOrNull() ?: 0
        val amPm = match.groupValues[3]
        when {
            amPm == "pm" && hour != 12 -> hour += 12
            amPm == "am" && hour == 12 -> hour = 0
            amPm.isEmpty() && hour < 7 -> hour += 12 // assume PM for ambiguous times
        }
        return Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }
}
