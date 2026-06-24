package com.javis.launcher

import android.app.Application
import android.content.Context
import com.javis.launcher.database.JavisDatabase
import com.javis.launcher.memory.MemoryManager
import com.javis.launcher.providers.AIProviderManager
import com.javis.launcher.voice.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JavisApplication : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: JavisDatabase by lazy { JavisDatabase.getInstance(this) }
    val memoryManager: MemoryManager by lazy { MemoryManager(this, database) }
    val aiProviderManager: AIProviderManager by lazy { AIProviderManager(this) }
    val voiceManager: VoiceManager by lazy { VoiceManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appScope.launch { memoryManager.initialize() }
    }

    companion object {
        lateinit var instance: JavisApplication
            private set

        fun get(context: Context): JavisApplication =
            context.applicationContext as JavisApplication
    }
}
