package com.javis.launcher.memory

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.javis.launcher.database.JavisDatabase
import com.javis.launcher.database.entities.MemoryEntity
import com.javis.launcher.models.MemoryCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "javis_prefs")

class MemoryManager(private val context: Context, private val db: JavisDatabase) {

    private val KEY_USER_NAME = stringPreferencesKey("user_name")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_SESSION_ID = stringPreferencesKey("session_id")
    private val KEY_AI_PROVIDER = stringPreferencesKey("ai_provider")
    private val KEY_AI_MODEL = stringPreferencesKey("ai_model")
    private val KEY_API_KEY = stringPreferencesKey("api_key")
    private val KEY_VOICE_TYPE = stringPreferencesKey("voice_type")
    private val KEY_RESPONSE_MODE = stringPreferencesKey("response_mode")
    private val KEY_WAKE_WORD = booleanPreferencesKey("wake_word_enabled")
    private val KEY_SETUP_DONE = booleanPreferencesKey("setup_done")

    suspend fun initialize() {
        val name = getUserName()
        if (name.isEmpty()) setUserName("Sir")
        if (getUserId().isEmpty()) setUserId(generateId())
        refreshSessionId()
    }

    suspend fun getUserName(): String = context.dataStore.data.map { it[KEY_USER_NAME] ?: "" }.first()
    suspend fun setUserName(name: String) = context.dataStore.edit { it[KEY_USER_NAME] = name }
    suspend fun getUserId(): String = context.dataStore.data.map { it[KEY_USER_ID] ?: "" }.first()
    private suspend fun setUserId(id: String) = context.dataStore.edit { it[KEY_USER_ID] = id }
    suspend fun getSessionId(): String = context.dataStore.data.map { it[KEY_SESSION_ID] ?: "" }.first()
    suspend fun refreshSessionId() = context.dataStore.edit { it[KEY_SESSION_ID] = generateId() }
    suspend fun getAIProvider(): String = context.dataStore.data.map { it[KEY_AI_PROVIDER] ?: "openrouter" }.first()
    suspend fun setAIProvider(p: String) = context.dataStore.edit { it[KEY_AI_PROVIDER] = p }
    suspend fun getAIModel(): String = context.dataStore.data.map { it[KEY_AI_MODEL] ?: "qwen/qwen3-mini:free" }.first()
    suspend fun setAIModel(m: String) = context.dataStore.edit { it[KEY_AI_MODEL] = m }
    suspend fun getApiKey(): String = context.dataStore.data.map { it[KEY_API_KEY] ?: "" }.first()
    suspend fun setApiKey(key: String) = context.dataStore.edit { it[KEY_API_KEY] = key }
    suspend fun getVoiceType(): String = context.dataStore.data.map { it[KEY_VOICE_TYPE] ?: "ANDROID_TTS" }.first()
    suspend fun setVoiceType(v: String) = context.dataStore.edit { it[KEY_VOICE_TYPE] = v }
    suspend fun getResponseMode(): String = context.dataStore.data.map { it[KEY_RESPONSE_MODE] ?: "detailed" }.first()
    suspend fun setResponseMode(m: String) = context.dataStore.edit { it[KEY_RESPONSE_MODE] = m }
    suspend fun isWakeWordEnabled(): Boolean = context.dataStore.data.map { it[KEY_WAKE_WORD] ?: false }.first()
    suspend fun setWakeWordEnabled(e: Boolean) = context.dataStore.edit { it[KEY_WAKE_WORD] = e }
    suspend fun isSetupDone(): Boolean = context.dataStore.data.map { it[KEY_SETUP_DONE] ?: false }.first()
    suspend fun setSetupDone(d: Boolean) = context.dataStore.edit { it[KEY_SETUP_DONE] = d }

    suspend fun remember(key: String, value: String, category: MemoryCategory, confidence: Float = 1.0f) {
        db.memoryDao().upsert(MemoryEntity(key = key, value = value, category = category.name, confidence = confidence))
    }

    suspend fun recall(key: String): String? = db.memoryDao().getByKey(key)?.value

    suspend fun buildContextString(): String {
        val entries = db.memoryDao().getByCategory(MemoryCategory.PERSONAL.name) +
                      db.memoryDao().getByCategory(MemoryCategory.PREFERENCES.name) +
                      db.memoryDao().getByCategory(MemoryCategory.HABITS.name)
        if (entries.isEmpty()) return ""
        return "\n\n[JAVIS Memory Context]\n" + entries.joinToString("\n") { "- ${it.key}: ${it.value}" }
    }

    suspend fun extractAndSaveFromConversation(userText: String, assistantText: String) {
        val lower = userText.lowercase()
        when {
            lower.contains("my name is") || lower.contains("i am ") -> {
                val name = userText.substringAfterLast("is ").substringAfterLast("am ").trim().split(" ").firstOrNull() ?: return
                if (name.length in 2..30) { remember("user_name", name, MemoryCategory.PERSONAL); setUserName(name) }
            }
            lower.contains("i like") || lower.contains("i love") || lower.contains("i prefer") -> {
                val pref = userText.substringAfterLast("like ").substringAfterLast("love ").substringAfterLast("prefer ").trim()
                if (pref.length in 3..100) remember("preference_${System.currentTimeMillis()}", pref, MemoryCategory.PREFERENCES)
            }
            lower.contains("my goal") || lower.contains("i want to") -> {
                val goal = userText.trim()
                if (goal.length in 10..200) remember("goal_${System.currentTimeMillis()}", goal, MemoryCategory.GOALS)
            }
        }
    }

    private fun generateId() = "j_${System.currentTimeMillis()}_${(Math.random() * 9999).toInt()}"
}
