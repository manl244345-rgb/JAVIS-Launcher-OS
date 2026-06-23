package com.javis.launcher

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.javis.launcher.database.JavisDatabase
import com.javis.launcher.memory.MemoryManager
import com.javis.launcher.providers.AIProviderManager
import com.javis.launcher.voice.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JavisApplication : Application(), Configuration.Provider {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: JavisDatabase
    lateinit var memoryManager: MemoryManager
    lateinit var aiProviderManager: AIProviderManager
    lateinit var voiceManager: VoiceManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeComponents()
    }

    private fun initializeComponents() {
        database = JavisDatabase.getInstance(this)
        memoryManager = MemoryManager(this, database, applicationScope)
        aiProviderManager = AIProviderManager(this)
        voiceManager = VoiceManager(this)
        applicationScope.launch { memoryManager.initialize() }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    companion object {
        lateinit var instance: JavisApplication
            private set
        fun get(context: Context) = context.applicationContext as JavisApplication
    }
}
