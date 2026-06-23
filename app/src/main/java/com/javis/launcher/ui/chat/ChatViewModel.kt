package com.javis.launcher.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.javis.launcher.JavisApplication
import com.javis.launcher.ai.IntentAnalyzer
import com.javis.launcher.ai.IntentType
import com.javis.launcher.database.entities.ConversationEntity
import com.javis.launcher.database.entities.MessageEntity
import com.javis.launcher.models.ChatMessage
import com.javis.launcher.services.JavisNotificationListener
import com.javis.launcher.utils.AlarmHelper
import com.javis.launcher.utils.AlarmSetResult
import com.javis.launcher.utils.AppLauncher
import com.javis.launcher.voice.VoiceState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val app = JavisApplication.get(application)
    private val db = app.database
    private val memoryManager = app.memoryManager
    private val aiProvider = app.aiProviderManager
    private val voiceManager = app.voiceManager

    private val conversationId = UUID.randomUUID().toString()
    private val conversationHistory = mutableListOf<Map<String, String>>()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    val voiceState = voiceManager.voiceState
    val recognizedText = voiceManager.recognizedText

    init {
        viewModelScope.launch {
            db.conversationDao().insert(ConversationEntity(id = conversationId, userId = memoryManager.getUserId(), title = "JAVIS Chat"))
            addMessage("Hello! I'm JAVIS, your AI companion. How can I help you today?", false)
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            addMessage(text, true)
            _isTyping.value = true
            db.messageDao().insert(MessageEntity(conversationId = conversationId, role = "user", content = text))
            val parsed = IntentAnalyzer.analyze(text)
            val response = when (parsed.type) {
                IntentType.OPEN_APP -> {
                    val result = AppLauncher.findAndLaunchApp(getApplication(), parsed.params["app"] ?: "")
                    when (result) {
                        is com.javis.launcher.utils.LaunchResult.Success -> "Opening ${result.appName} for you."
                        is com.javis.launcher.utils.LaunchResult.NotFound -> "I couldn't find \"${result.query}\". Check the App Store or try a different name."
                        is com.javis.launcher.utils.LaunchResult.NotLaunchable -> "That app can't be launched from here."
                    }
                }
                IntentType.CALL_CONTACT -> {
                    val contacts = AppLauncher.searchContacts(getApplication(), parsed.params["contact"] ?: "")
                    when {
                        contacts.isEmpty() -> "I couldn't find that contact."
                        contacts.size == 1 -> { AppLauncher.callContact(getApplication(), contacts[0].phone); "Calling ${contacts[0].name}." }
                        else -> "Found ${contacts.size} contacts: ${contacts.take(3).joinToString(", ") { it.name }}. Which one?"
                    }
                }
                IntentType.SET_ALARM -> {
                    val h = parsed.params["hour"]?.toIntOrNull() ?: -1
                    val m = parsed.params["minute"]?.toIntOrNull() ?: 0
                    if (h < 0) "I couldn't understand that time. Could you say it differently?"
                    else {
                        val result = AlarmHelper.setAlarm(getApplication(), h, m, "JAVIS Alarm")
                        when (result) {
                            is AlarmSetResult.Success -> "✓ Alarm set for ${AlarmHelper.formatTime(h, m)}. I'll make sure you're up."
                            is AlarmSetResult.Failed -> "I had trouble setting that alarm. ${result.reason}"
                        }
                    }
                }
                IntentType.GET_NOTIFICATIONS -> JavisNotificationListener.getSummary()
                IntentType.GET_MEMORY -> {
                    val memories = memoryManager.search(text)
                    if (memories.isEmpty()) "I don't have anything specific stored about that yet."
                    else memories.joinToString("\n") { "• ${it.key}: ${it.value}" }
                }
                else -> {
                    conversationHistory.add(mapOf("role" to "user", "content" to text))
                    val ctx = memoryManager.buildContextString()
                    val aiResp = aiProvider.sendMessage(conversationHistory, com.javis.launcher.providers.AIProviderManager.JAVIS_SYSTEM_PROMPT + ctx)
                    conversationHistory.add(mapOf("role" to "assistant", "content" to aiResp.text))
                    if (conversationHistory.size > 40) conversationHistory.removeAt(0)
                    memoryManager.extractAndSaveFromConversation(text, aiResp.text)
                    aiResp.text
                }
            }
            _isTyping.value = false
            addMessage(response, false)
            db.messageDao().insert(MessageEntity(conversationId = conversationId, role = "assistant", content = response))
            voiceManager.speak(response)
        }
    }

    fun toggleVoiceInput() {
        if (voiceManager.voiceState.value == VoiceState.LISTENING) {
            voiceManager.stopListening()
        } else {
            voiceManager.startListening(
                onResult = { text -> sendMessage(text) },
                onError = { addMessage("Could not understand. Please try again.", false) }
            )
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        val current = _messages.value.toMutableList()
        current.add(ChatMessage(UUID.randomUUID().toString(), text, isUser, System.currentTimeMillis()))
        _messages.value = current
    }
}
