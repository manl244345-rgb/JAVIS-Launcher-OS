package com.javis.launcher.providers

import android.content.Context
import android.util.Log
import com.javis.launcher.models.AIResponse
import com.javis.launcher.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

enum class AIProvider(val displayName: String, val baseUrl: String) {
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1"),
    GROQ("Groq", "https://api.groq.com/openai/v1"),
    TOGETHER("Together AI", "https://api.together.xyz/v1"),
    FIREWORKS("Fireworks AI", "https://api.fireworks.ai/inference/v1"),
    OFFLINE("Offline (Qwen Mini)", "http://localhost:11434/v1")
}

class AIProviderManager(private val context: Context) {

    private val prefs = PreferenceManager(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val providerOrder = listOf(
        AIProvider.OPENROUTER,
        AIProvider.GROQ,
        AIProvider.DEEPSEEK,
        AIProvider.TOGETHER,
        AIProvider.FIREWORKS
    )

    private var currentProviderIndex = 0
    private val TAG = "AIProviderManager"

    suspend fun sendMessage(
        messages: List<Map<String, String>>,
        systemPrompt: String = JAVIS_SYSTEM_PROMPT
    ): AIResponse = withContext(Dispatchers.IO) {
        val apiKey = prefs.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext AIResponse(
                text = "I need an API key to connect online. You can set one in Settings, or I'll work offline with my built-in AI.",
                provider = "none",
                isOffline = true
            )
        }

        var lastError = ""
        var attempts = 0
        val maxAttempts = providerOrder.size

        while (attempts < maxAttempts) {
            val provider = providerOrder[currentProviderIndex % providerOrder.size]
            try {
                val response = callProvider(provider, messages, systemPrompt, apiKey)
                return@withContext AIResponse(
                    text = response,
                    provider = provider.displayName,
                    isOffline = false
                )
            } catch (e: Exception) {
                lastError = e.message ?: "Unknown error"
                Log.w(TAG, "Provider ${provider.displayName} failed: $lastError — trying next")
                currentProviderIndex++
                attempts++
            }
        }

        AIResponse(
            text = "I'm having trouble connecting right now. Let me answer from memory: $lastError",
            provider = "fallback",
            isOffline = true
        )
    }

    private suspend fun callProvider(
        provider: AIProvider,
        messages: List<Map<String, String>>,
        systemPrompt: String,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        val model = getModelForProvider(provider)
        val messagesArray = JSONArray()
        messagesArray.put(JSONObject().put("role", "system").put("content", systemPrompt))
        messages.forEach { msg ->
            messagesArray.put(JSONObject().put("role", msg["role"]).put("content", msg["content"]))
        }

        val requestBody = JSONObject()
            .put("model", model)
            .put("messages", messagesArray)
            .put("max_tokens", 1024)
            .put("temperature", 0.7)
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${provider.baseUrl}/chat/completions")
            .post(requestBody)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .header("HTTP-Referer", "https://javis-launcher.app")
            .header("X-Title", "JAVIS Launcher OS")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("HTTP ${response.code}: ${response.body?.string()}")
        val json = JSONObject(response.body?.string() ?: throw IOException("Empty response"))
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    private fun getModelForProvider(provider: AIProvider): String = when (provider) {
        AIProvider.OPENROUTER -> "qwen/qwen3-mini:free"
        AIProvider.GROQ -> "llama-3.1-8b-instant"
        AIProvider.DEEPSEEK -> "deepseek-chat"
        AIProvider.TOGETHER -> "meta-llama/Llama-3.2-3B-Instruct-Turbo"
        AIProvider.FIREWORKS -> "accounts/fireworks/models/llama-v3p1-8b-instruct"
        AIProvider.OFFLINE -> "qwen3:0.6b"
    }

    fun getCurrentProviderName(): String = providerOrder[currentProviderIndex % providerOrder.size].displayName

    companion object {
        const val JAVIS_SYSTEM_PROMPT = """You are JAVIS (Just A Very Intelligent System), an AI companion and Android launcher assistant. You are:

- Intelligent, friendly, professional, and occasionally humorous
- Direct and concise — give clear answers without unnecessary padding  
- Honest — never claim to do something you cannot verify
- Context-aware — you remember the conversation and user preferences
- Action-oriented — when asked to perform tasks, describe what you are doing step by step

Your personality: Think Jarvis from Iron Man — calm, competent, with dry wit. You address the user respectfully. You are running on their phone as their personal AI launcher.

CRITICAL RULES:
- Never show raw JSON, package names, or internal commands to the user
- Never claim success unless verified
- For app-launching: describe the action clearly
- For alarms: always confirm the exact time set
- Keep responses natural and conversational
- If you cannot do something, say so clearly and suggest alternatives"""
    }
}
