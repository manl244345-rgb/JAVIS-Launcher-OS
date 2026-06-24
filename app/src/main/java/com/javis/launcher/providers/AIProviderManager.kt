package com.javis.launcher.providers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.javis.launcher.memory.MemoryManager
import com.javis.launcher.models.AIResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class AIProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val defaultModel: String,
    val requiresKey: Boolean = true
)

class AIProviderManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    val providers = listOf(
        AIProvider("openrouter", "OpenRouter", "https://openrouter.ai/api/v1", "qwen/qwen3-mini:free"),
        AIProvider("groq", "Groq", "https://api.groq.com/openai/v1", "llama3-8b-8192"),
        AIProvider("deepseek", "DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"),
        AIProvider("together", "Together AI", "https://api.together.xyz/v1", "meta-llama/Llama-3-8b-chat-hf"),
        AIProvider("fireworks", "Fireworks AI", "https://api.fireworks.ai/inference/v1", "accounts/fireworks/models/llama-v3-8b-instruct")
    )

    companion object {
        const val JAVIS_SYSTEM_PROMPT = """You are JAVIS — Just A Very Intelligent System.
You are a powerful AI assistant embedded as an Android launcher.
You are masculine, professional, calm, and highly capable.
You speak like Jarvis from Iron Man — concise, intelligent, direct.
You always: Understand → Think → Plan → Execute → Verify → Respond.
Never claim to do something you cannot do.
Keep responses brief unless the user asks for detail.
Address the user as "Sir" by default."""
    }

    suspend fun sendMessage(
        messages: List<Map<String, String>>,
        systemPrompt: String = JAVIS_SYSTEM_PROMPT,
        memory: MemoryManager? = null
    ): AIResponse = withContext(Dispatchers.IO) {
        val provider = memory?.getAIProvider() ?: "openrouter"
        val model = memory?.getAIModel() ?: "qwen/qwen3-mini:free"
        val apiKey = memory?.getApiKey() ?: ""

        val providerInfo = providers.find { it.id == provider } ?: providers[0]

        val providerOrder = listOf(provider) + providers.map { it.id }.filter { it != provider }

        for (pid in providerOrder) {
            val p = providers.find { it.id == pid } ?: continue
            val key = if (pid == provider) apiKey else ""
            if (p.requiresKey && key.isBlank()) continue
            try {
                return@withContext callProvider(p, key, model.takeIf { pid == provider } ?: p.defaultModel, systemPrompt, messages)
            } catch (e: Exception) {
                continue
            }
        }
        AIResponse("I'm having trouble connecting to AI services. Please check your API key in Settings.", "none", "none")
    }

    private suspend fun callProvider(
        provider: AIProvider,
        apiKey: String,
        model: String,
        systemPrompt: String,
        messages: List<Map<String, String>>
    ): AIResponse = withContext(Dispatchers.IO) {
        val allMessages = mutableListOf(mapOf("role" to "system", "content" to systemPrompt))
        allMessages.addAll(messages)

        val body = JsonObject().apply {
            addProperty("model", model)
            add("messages", gson.toJsonTree(allMessages))
            addProperty("max_tokens", 1024)
            addProperty("temperature", 0.7)
        }

        val start = System.currentTimeMillis()
        val req = Request.Builder()
            .url("${provider.baseUrl}/chat/completions")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://javis.launcher")
            .build()

        val resp = client.newCall(req).execute()
        val latency = System.currentTimeMillis() - start
        val json = gson.fromJson(resp.body?.string() ?: "{}", JsonObject::class.java)
        val text = json.getAsJsonArray("choices")
            ?.get(0)?.asJsonObject
            ?.getAsJsonObject("message")
            ?.get("content")?.asString ?: throw Exception("No response")
        val tokens = json.getAsJsonObject("usage")?.get("total_tokens")?.asInt ?: 0
        AIResponse(text, provider.id, model, tokens, latency)
    }
}
