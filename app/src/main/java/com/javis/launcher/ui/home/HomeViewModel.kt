package com.javis.launcher.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.javis.launcher.JavisApplication
import com.javis.launcher.ai.IntentAnalyzer
import com.javis.launcher.ai.IntentType
import com.javis.launcher.database.entities.CommandHistoryEntity
import com.javis.launcher.models.*
import com.javis.launcher.services.JavisNotificationListener
import com.javis.launcher.utils.AlarmHelper
import com.javis.launcher.utils.AppLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = JavisApplication.get(application)
    private val db = app.database
    private val memoryManager = app.memoryManager
    private val aiProvider = app.aiProviderManager
    private val voiceManager = app.voiceManager

    private val _greeting = MutableStateFlow("Good morning, Sir")
    val greeting: StateFlow<String> = _greeting.asStateFlow()

    private val _orbState = MutableStateFlow(OrbState(OrbAnimationState.IDLE))
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _voiceText = MutableStateFlow("")
    val voiceText: StateFlow<String> = _voiceText.asStateFlow()

    val favoriteApps = db.installedAppDao().getFavorites()

    private val conversationHistory = mutableListOf<Map<String, String>>()
    private var isListening = false

    fun refreshGreeting() {
        viewModelScope.launch {
            val name = memoryManager.getUserName()
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val timeOfDay = when {
                hour < 12 -> "morning"
                hour < 17 -> "afternoon"
                hour < 21 -> "evening"
                else -> "night"
            }
            _greeting.value = "Good $timeOfDay, $name"
        }
    }

    fun toggleListening() {
        if (isListening) {
            voiceManager.stopListening()
            isListening = false
            _orbState.value = OrbState(OrbAnimationState.IDLE)
        } else {
            startListening()
        }
    }

    private fun startListening() {
        isListening = true
        _orbState.value = OrbState(OrbAnimationState.LISTENING)
        voiceManager.startListening(
            onResult = { text ->
                isListening = false
                _voiceText.value = text
                handleTextInput(text)
            },
            onError = { error ->
                isListening = false
                _orbState.value = OrbState(OrbAnimationState.IDLE)
                voiceManager.speak(error)
            }
        )
    }

    fun handleTextInput(text: String) {
        viewModelScope.launch {
            _orbState.value = OrbState(OrbAnimationState.THINKING)
            val parsed = IntentAnalyzer.analyze(text)
            when (parsed.type) {
                IntentType.OPEN_APP -> handleOpenApp(parsed.params["app"] ?: "")
                IntentType.CALL_CONTACT -> handleCall(parsed.params["contact"] ?: "")
                IntentType.SET_ALARM -> handleSetAlarm(parsed)
                IntentType.GET_NOTIFICATIONS -> handleNotifications()
                IntentType.CHAT_GENERAL -> handleChat(text)
                else -> handleChat(text)
            }
            logCommand(text, true)
        }
    }

    private suspend fun handleOpenApp(appName: String) {
        _orbState.value = OrbState(OrbAnimationState.EXECUTING)
        val result = AppLauncher.findAndLaunchApp(getApplication(), appName)
        val response = when (result) {
            is com.javis.launcher.utils.LaunchResult.Success -> "Opening ${result.appName} for you."
            is com.javis.launcher.utils.LaunchResult.NotFound -> "I couldn't find an app called \"${result.query}\". Check the spelling or try the app search."
            is com.javis.launcher.utils.LaunchResult.NotLaunchable -> "${result.appName} cannot be launched directly."
        }
        speak(response)
    }

    private suspend fun handleCall(contactName: String) {
        _orbState.value = OrbState(OrbAnimationState.EXECUTING)
        val contacts = AppLauncher.searchContacts(getApplication(), contactName)
        when {
            contacts.isEmpty() -> speak("I couldn't find a contact named $contactName in your phone.")
            contacts.size == 1 -> {
                speak("Calling ${contacts[0].name}.")
                AppLauncher.callContact(getApplication(), contacts[0].phone)
            }
            else -> {
                val names = contacts.take(3).joinToString(", ") { it.name }
                speak("I found ${contacts.size} contacts matching \"$contactName\": $names. Which one should I call?")
            }
        }
    }

    private suspend fun handleSetAlarm(parsed: ParsedIntent) {
        val hour = parsed.params["hour"]?.toIntOrNull() ?: return speak("I couldn't understand that time. Please say it again.")
        val minute = parsed.params["minute"]?.toIntOrNull() ?: 0
        val label = "JAVIS Alarm"
        _orbState.value = OrbState(OrbAnimationState.EXECUTING)
        val result = AlarmHelper.setAlarm(getApplication(), hour, minute, label)
        val response = when (result) {
            is AlarmSetResult.Success -> "Alarm set for ${AlarmHelper.formatTime(hour, minute)}. I'll wake you up on time."
            is AlarmSetResult.Failed -> "I had trouble setting the alarm: ${result.reason}. Please try again."
        }
        speak(response)
    }

    private fun handleNotifications() {
        val summary = JavisNotificationListener.getSummary()
        speak(summary)
    }

    private suspend fun handleChat(text: String) {
        conversationHistory.add(mapOf("role" to "user", "content" to text))
        val context = memoryManager.buildContextString()
        val systemWithContext = com.javis.launcher.providers.AIProviderManager.JAVIS_SYSTEM_PROMPT + context
        val response = aiProvider.sendMessage(conversationHistory, systemWithContext)
        conversationHistory.add(mapOf("role" to "assistant", "content" to response.text))
        if (conversationHistory.size > 40) conversationHistory.removeAt(0)
        memoryManager.extractAndSaveFromConversation(text, response.text)
        speak(response.text)
    }

    private fun speak(text: String) {
        _orbState.value = OrbState(OrbAnimationState.SPEAKING, text)
        voiceManager.speak(text) { _orbState.value = OrbState(OrbAnimationState.IDLE) }
    }

    private suspend fun logCommand(command: String, success: Boolean) {
        db.commandHistoryDao().insert(
            CommandHistoryEntity(userId = memoryManager.getUserId(), command = command, success = success)
        )
    }
}
