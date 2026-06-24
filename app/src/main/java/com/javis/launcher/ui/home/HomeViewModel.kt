package com.javis.launcher.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.javis.launcher.JavisApplication
import com.javis.launcher.ai.IntentAnalyzer
import com.javis.launcher.database.entities.CommandLogEntity
import com.javis.launcher.models.*
import com.javis.launcher.services.JavisNotificationListener
import com.javis.launcher.utils.AlarmHelper
import com.javis.launcher.utils.AppLauncher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = JavisApplication.get(application)
    private val db = app.database
    private val memory = app.memoryManager
    private val ai = app.aiProviderManager
    private val voice = app.voiceManager

    private val _orbState = MutableStateFlow(OrbState(OrbAnimationState.IDLE, "Ready, Sir"))
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _greeting = MutableStateFlow("Hello, Sir")
    val greeting: StateFlow<String> = _greeting.asStateFlow()

    private val _statusText = MutableStateFlow("Tap orb to speak")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    val favoriteApps = db.installedAppDao().getFavorites()

    private val conversationHistory = ArrayDeque<Map<String, String>>(100)
    private var isListening = false

    fun initialize() {
        viewModelScope.launch {
            refreshGreeting()
            syncInstalledApps()
        }
    }

    private suspend fun refreshGreeting() {
        val name = memory.getUserName().ifBlank { "Sir" }
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val time = when { hour < 12 -> "morning"; hour < 17 -> "afternoon"; hour < 21 -> "evening"; else -> "night" }
        _greeting.value = "Good $time, $name"
    }

    private suspend fun syncInstalledApps() {
        val apps = AppLauncher.getAllApps(getApplication())
        apps.forEach { app ->
            db.installedAppDao().upsert(
                com.javis.launcher.database.entities.InstalledAppEntity(
                    packageName = app.packageName, appName = app.appName
                )
            )
        }
    }

    fun toggleListening() {
        if (isListening) stopListening() else startListening()
    }

    private fun startListening() {
        isListening = true
        _orbState.value = OrbState(OrbAnimationState.LISTENING, "Listening...")
        _statusText.value = "Listening..."
        voice.startListening(
            onResult = { text ->
                isListening = false
                _statusText.value = "Processing..."
                handleInput(text)
            },
            onError = { error ->
                isListening = false
                _orbState.value = OrbState(OrbAnimationState.IDLE, error)
                _statusText.value = "Tap orb to speak"
                voice.speak(error)
            }
        )
    }

    private fun stopListening() {
        isListening = false
        voice.stopListening()
        _orbState.value = OrbState(OrbAnimationState.IDLE, "Stopped")
        _statusText.value = "Tap orb to speak"
    }

    fun handleInput(text: String) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _orbState.value = OrbState(OrbAnimationState.THINKING, "Thinking...")
            val parsed = IntentAnalyzer.analyze(text)
            var response = ""
            var success = true
            try {
                response = when (parsed.type) {
                    IntentType.OPEN_APP -> handleOpenApp(parsed.params["app"] ?: text)
                    IntentType.CALL_CONTACT -> handleCall(parsed.params["contact"] ?: text)
                    IntentType.SET_ALARM -> handleSetAlarm(parsed)
                    IntentType.DELETE_ALARM -> "Which alarm should I delete, Sir? Please say a time or label."
                    IntentType.LIST_ALARMS -> handleListAlarms()
                    IntentType.GET_NOTIFICATIONS -> JavisNotificationListener.getSummary()
                    IntentType.OPEN_SETTINGS -> { AppLauncher.openSettings(getApplication()); "Opening settings for you, Sir." }
                    IntentType.GET_MEMORY -> handleMemoryQuery()
                    else -> handleChat(text)
                }
            } catch (e: Exception) {
                success = false
                response = "I encountered an error: ${e.message}. Please try again, Sir."
            }
            val latency = System.currentTimeMillis() - startTime
            logCommand(text, parsed.type.name, response, success, latency)
            speak(response)
        }
    }

    private suspend fun handleOpenApp(name: String): String {
        _orbState.value = OrbState(OrbAnimationState.EXECUTING, "Opening $name...")
        return when (val r = AppLauncher.findAndLaunchApp(getApplication(), name)) {
            is LaunchResult.Success -> "Opening ${r.appName} now."
            is LaunchResult.NotFound -> "I couldn't find \"${r.query}\". Try searching in All Apps."
            is LaunchResult.NotLaunchable -> "${r.appName} cannot be launched directly."
        }
    }

    private suspend fun handleCall(name: String): String {
        _orbState.value = OrbState(OrbAnimationState.EXECUTING, "Searching contacts...")
        val contacts = AppLauncher.searchContacts(getApplication(), name)
        return when {
            contacts.isEmpty() -> "No contact found named \"$name\", Sir."
            contacts.size == 1 -> { AppLauncher.callContact(getApplication(), contacts[0].phone); "Calling ${contacts[0].name} now." }
            else -> "Found ${contacts.size} contacts: ${contacts.take(3).joinToString(", ") { it.name }}. Which one, Sir?"
        }
    }

    private suspend fun handleSetAlarm(parsed: ParsedIntent): String {
        val hour = parsed.params["hour"]?.toIntOrNull() ?: return "I didn't catch the time. Please say a time like '7 AM' or '7:30 PM'."
        val minute = parsed.params["minute"]?.toIntOrNull() ?: 0
        _orbState.value = OrbState(OrbAnimationState.EXECUTING, "Setting alarm...")
        return when (val r = AlarmHelper.setAlarm(getApplication(), hour, minute, "JAVIS Alarm")) {
            is AlarmResult.Success -> "Alarm set for ${AlarmHelper.formatTime(hour, minute)}, Sir. I'll make sure you're up."
            is AlarmResult.Failed -> "Failed to set alarm: ${r.reason}"
        }
    }

    private suspend fun handleListAlarms(): String {
        val alarms = db.alarmDao().getActive()
        return if (alarms.isEmpty()) "No alarms set, Sir."
        else "You have ${alarms.size} alarm${if (alarms.size > 1) "s" else ""}: ${alarms.joinToString(", ") { AlarmHelper.formatTime(it.hour, it.minute) }}."
    }

    private suspend fun handleMemoryQuery(): String {
        val count = db.memoryDao().count()
        val name = memory.getUserName()
        return "I know $count things about you, Sir. Your name is $name. ${memory.buildContextString().take(200)}"
    }

    private suspend fun handleChat(text: String): String {
        conversationHistory.addLast(mapOf("role" to "user", "content" to text))
        if (conversationHistory.size > 40) conversationHistory.removeFirst()
        val ctx = memory.buildContextString()
        val sysPrompt = com.javis.launcher.providers.AIProviderManager.JAVIS_SYSTEM_PROMPT + ctx
        val resp = ai.sendMessage(conversationHistory.toList(), sysPrompt, memory)
        conversationHistory.addLast(mapOf("role" to "assistant", "content" to resp.text))
        memory.extractAndSaveFromConversation(text, resp.text)
        return resp.text
    }

    private fun speak(text: String) {
        _orbState.value = OrbState(OrbAnimationState.SPEAKING, text.take(60) + if (text.length > 60) "..." else "")
        _statusText.value = "Speaking..."
        voice.speak(text) { _orbState.value = OrbState(OrbAnimationState.IDLE, "Ready, Sir"); _statusText.value = "Tap orb to speak" }
    }

    private suspend fun logCommand(cmd: String, type: String, result: String, success: Boolean, latency: Long) {
        db.commandLogDao().insert(CommandLogEntity(command = cmd, intentType = type, result = result.take(200), success = success, latency = latency))
    }
}
