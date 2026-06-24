package com.javis.launcher.ai

import com.javis.launcher.models.IntentType
import com.javis.launcher.models.ParsedIntent

object IntentAnalyzer {

    private val openPatterns = listOf(
        Regex("(?:open|launch|start|run)\\s+(.+)", RegexOption.IGNORE_CASE),
        Regex("(.+)\\s+(?:open|launch)", RegexOption.IGNORE_CASE)
    )
    private val callPatterns = listOf(
        Regex("(?:call|ring|dial|phone|contact)\\s+(.+)", RegexOption.IGNORE_CASE),
        Regex("(.+)\\s+ko (?:call|ring|dial)", RegexOption.IGNORE_CASE)
    )
    private val alarmPatterns = listOf(
        Regex("(?:set|create|add|make)\\s+(?:an?\\s+)?alarm(?:\\s+for)?\\s+(.+)", RegexOption.IGNORE_CASE),
        Regex("wake me(?:\\s+up)?(?:\\s+at)?\\s+(.+)", RegexOption.IGNORE_CASE),
        Regex("alarm\\s+(?:at\\s+)?(.+)", RegexOption.IGNORE_CASE),
        Regex("remind me(?:\\s+at)?\\s+(.+)", RegexOption.IGNORE_CASE)
    )
    private val deleteAlarmPatterns = listOf(
        Regex("(?:delete|remove|cancel|dismiss)\\s+(?:the\\s+)?alarm(.+)?", RegexOption.IGNORE_CASE)
    )
    private val notifKeywords = listOf("notification", "notify", "messages", "unread", "inbox", "what did i miss", "new message")
    private val settingsKeywords = listOf("settings", "preferences", "configure", "setup")
    private val memoryKeywords = listOf("remember", "recall", "what do you know", "my info", "forget")

    fun analyze(text: String): ParsedIntent {
        val lower = text.lowercase().trim()

        for (p in deleteAlarmPatterns) {
            if (p.containsMatchIn(lower)) return ParsedIntent(IntentType.DELETE_ALARM, emptyMap(), text)
        }
        if (lower.contains("list alarm") || lower.contains("show alarm") || lower.contains("my alarms")) {
            return ParsedIntent(IntentType.LIST_ALARMS, emptyMap(), text)
        }
        for (p in alarmPatterns) {
            p.find(lower)?.let { m ->
                val timeStr = m.groupValues.getOrElse(1) { "" }
                val params = parseTime(timeStr)
                if (params.isNotEmpty()) return ParsedIntent(IntentType.SET_ALARM, params, text)
            }
        }
        for (p in callPatterns) {
            p.find(lower)?.let { m ->
                val contact = m.groupValues.getOrElse(1) { "" }.trim()
                if (contact.length in 2..50)
                    return ParsedIntent(IntentType.CALL_CONTACT, mapOf("contact" to contact), text)
            }
        }
        for (p in openPatterns) {
            p.find(lower)?.let { m ->
                val app = m.groupValues.getOrElse(1) { "" }.trim()
                if (app.length in 2..40 && !app.contains("app") || app.length > 4)
                    return ParsedIntent(IntentType.OPEN_APP, mapOf("app" to app), text)
            }
        }
        if (notifKeywords.any { lower.contains(it) })
            return ParsedIntent(IntentType.GET_NOTIFICATIONS, emptyMap(), text)
        if (settingsKeywords.any { lower.contains(it) })
            return ParsedIntent(IntentType.OPEN_SETTINGS, emptyMap(), text)
        if (memoryKeywords.any { lower.contains(it) })
            return ParsedIntent(IntentType.GET_MEMORY, emptyMap(), text)
        if (lower.contains("search") && (lower.contains("app") || lower.contains("find")))
            return ParsedIntent(IntentType.SEARCH_APP, mapOf("query" to text), text)

        return ParsedIntent(IntentType.CHAT_GENERAL, emptyMap(), text)
    }

    private fun parseTime(timeStr: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val lower = timeStr.lowercase()

        val hourMin = Regex("(\\d{1,2}):(\\d{2})\\s*([ap]m)?").find(lower)
        if (hourMin != null) {
            var h = hourMin.groupValues[1].toIntOrNull() ?: return emptyMap()
            val m = hourMin.groupValues[2].toIntOrNull() ?: 0
            val ampm = hourMin.groupValues[3]
            if (ampm == "pm" && h < 12) h += 12
            if (ampm == "am" && h == 12) h = 0
            params["hour"] = h.toString()
            params["minute"] = m.toString()
            return params
        }

        val hourOnly = Regex("(\\d{1,2})\\s*([ap]m)").find(lower)
        if (hourOnly != null) {
            var h = hourOnly.groupValues[1].toIntOrNull() ?: return emptyMap()
            val ampm = hourOnly.groupValues[2]
            if (ampm == "pm" && h < 12) h += 12
            if (ampm == "am" && h == 12) h = 0
            params["hour"] = h.toString()
            params["minute"] = "0"
            return params
        }

        val wordMap = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
            "eleven" to 11, "twelve" to 12
        )
        for ((word, num) in wordMap) {
            if (lower.contains(word)) {
                var h = num
                if (lower.contains("pm") && h < 12) h += 12
                if (lower.contains("am") && h == 12) h = 0
                params["hour"] = h.toString()
                params["minute"] = "0"
                return params
            }
        }
        return emptyMap()
    }
}
