package com.javis.launcher.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context, "javis_secure_prefs", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("javis_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        const val KEY_API_KEY = "api_key"
        const val KEY_VOICE_PROFILE = "voice_profile"
        const val KEY_AI_PROVIDER = "ai_provider"
        const val KEY_USER_NAME = "user_name"
        const val KEY_VOICE_SPEED = "voice_speed"
        const val KEY_VOICE_PITCH = "voice_pitch"
        const val KEY_WAKE_WORD = "wake_word_enabled"
        const val KEY_ONBOARDED = "onboarding_complete"
        const val KEY_WALLPAPER = "wallpaper_path"
    }

    fun getApiKey(): String = prefs.getString(KEY_API_KEY, "") ?: ""
    fun setApiKey(key: String) = prefs.edit().putString(KEY_API_KEY, key).apply()
    fun getVoiceProfile(): String = prefs.getString(KEY_VOICE_PROFILE, "jarvis_male") ?: "jarvis_male"
    fun setVoiceProfile(v: String) = prefs.edit().putString(KEY_VOICE_PROFILE, v).apply()
    fun getAIProvider(): String = prefs.getString(KEY_AI_PROVIDER, "openrouter") ?: "openrouter"
    fun setAIProvider(p: String) = prefs.edit().putString(KEY_AI_PROVIDER, p).apply()
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Sir") ?: "Sir"
    fun setUserName(n: String) = prefs.edit().putString(KEY_USER_NAME, n).apply()
    fun isOnboarded(): Boolean = prefs.getBoolean(KEY_ONBOARDED, false)
    fun setOnboarded(v: Boolean) = prefs.edit().putBoolean(KEY_ONBOARDED, v).apply()
    fun isWakeWordEnabled(): Boolean = prefs.getBoolean(KEY_WAKE_WORD, false)
    fun setWakeWordEnabled(v: Boolean) = prefs.edit().putBoolean(KEY_WAKE_WORD, v).apply()
    fun getVoiceSpeed(): Float = prefs.getFloat(KEY_VOICE_SPEED, 0.95f)
    fun setVoiceSpeed(v: Float) = prefs.edit().putFloat(KEY_VOICE_SPEED, v).apply()
    fun getVoicePitch(): Float = prefs.getFloat(KEY_VOICE_PITCH, 0.9f)
    fun setVoicePitch(v: Float) = prefs.edit().putFloat(KEY_VOICE_PITCH, v).apply()
}
