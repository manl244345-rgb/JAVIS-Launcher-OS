package com.javis.launcher.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.javis.launcher.JavisApplication
import com.javis.launcher.database.entities.ConversationEntity
import com.javis.launcher.models.AIResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val content: String, val timestamp: Long = System.currentTimeMillis(), val provider: String = "")

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val app = JavisApplication.get(application)
    private val db = app.database
    private val ai = app.aiProviderManager
    private val memory = app.memoryManager
    private val voice = app.voiceManager

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val history = ArrayDeque<Map<String, String>>(100)

    fun loadHistory() {
        viewModelScope.launch {
            val sid = memory.getSessionId()
            val rows = db.conversationDao().getSession(sid)
            _messages.value = rows.map { ChatMessage(it.role, it.content, it.timestamp, it.provider) }
            rows.forEach { history.addLast(mapOf("role" to it.role, "content" to it.content)) }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage("user", text)
        _messages.value = _messages.value + userMsg
        history.addLast(mapOf("role" to "user", "content" to text))
        if (history.size > 40) history.removeFirst()
        viewModelScope.launch {
            _isTyping.value = true
            val ctx = memory.buildContextString()
            val sys = com.javis.launcher.providers.AIProviderManager.JAVIS_SYSTEM_PROMPT + ctx
            val resp = ai.sendMessage(history.toList(), sys, memory)
            history.addLast(mapOf("role" to "assistant", "content" to resp.text))
            val aiMsg = ChatMessage("assistant", resp.text, provider = resp.provider)
            _messages.value = _messages.value + aiMsg
            _isTyping.value = false
            memory.extractAndSaveFromConversation(text, resp.text)
            val sid = memory.getSessionId()
            db.conversationDao().insert(ConversationEntity(sessionId = sid, role = "user", content = text))
            db.conversationDao().insert(ConversationEntity(sessionId = sid, role = "assistant", content = resp.text, provider = resp.provider))
        }
    }

    fun toggleVoice() {
        if (_isListening.value) {
            voice.stopListening()
            _isListening.value = false
        } else {
            _isListening.value = true
            voice.startListening(
                onResult = { text -> _isListening.value = false; sendMessage(text) },
                onError = { _isListening.value = false }
            )
        }
    }

    fun speakLast() {
        val last = _messages.value.lastOrNull { it.role == "assistant" } ?: return
        voice.speak(last.content)
    }

    fun clearHistory() {
        viewModelScope.launch {
            val sid = memory.getSessionId()
            _messages.value = emptyList()
            history.clear()
        }
    }
}
