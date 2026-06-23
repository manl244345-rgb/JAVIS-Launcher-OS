package com.javis.launcher.ui.memory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.javis.launcher.JavisApplication
import com.javis.launcher.database.entities.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MemoryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = JavisApplication.get(application)
    val memories: Flow<List<MemoryEntity>> = app.memoryManager.getMemoriesFlow()
}
