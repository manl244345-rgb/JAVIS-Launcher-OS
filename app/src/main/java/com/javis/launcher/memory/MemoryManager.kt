package com.javis.launcher.memory

import android.content.Context
import com.javis.launcher.database.JavisDatabase
import com.javis.launcher.database.entities.MemoryEntity
import com.javis.launcher.database.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MemoryManager(
    private val context: Context,
    private val db: JavisDatabase,
    private val scope: CoroutineScope
) {
    private var currentUserId: Long = 1L
    private var cachedUserName: String = "Sir"

    suspend fun initialize() = withContext(Dispatchers.IO) {
        var user = db.userDao().getUser()
        if (user == null) {
            val id = db.userDao().insert(UserEntity(name = "Sir", nickname = ""))
            currentUserId = id
        } else {
            currentUserId = user.id
            cachedUserName = user.nickname.ifBlank { user.name }
        }
    }

    suspend fun getUserName(): String = withContext(Dispatchers.IO) {
        db.userDao().getUser()?.let { u -> u.nickname.ifBlank { u.name } } ?: "Sir"
    }

    suspend fun setUserName(name: String) = withContext(Dispatchers.IO) {
        val user = db.userDao().getUser() ?: UserEntity()
        db.userDao().update(user.copy(name = name))
        cachedUserName = name
    }

    suspend fun remember(key: String, value: String, type: String = "long_term", importance: Int = 5) =
        withContext(Dispatchers.IO) {
            val existing = db.memoryDao().getByKey(currentUserId, key)
            if (existing != null) {
                db.memoryDao().update(existing.copy(value = value, importance = importance, accessedAt = System.currentTimeMillis()))
            } else {
                db.memoryDao().insert(MemoryEntity(userId = currentUserId, type = type, key = key, value = value, importance = importance))
            }
        }

    suspend fun recall(key: String): String? = withContext(Dispatchers.IO) {
        val memory = db.memoryDao().getByKey(currentUserId, key)
        memory?.also { db.memoryDao().updateAccess(it.id) }?.value
    }

    suspend fun search(query: String): List<MemoryEntity> = withContext(Dispatchers.IO) {
        db.memoryDao().search(currentUserId, "%$query%")
    }

    suspend fun getAllMemories(): List<MemoryEntity> = withContext(Dispatchers.IO) {
        db.memoryDao().getByType(currentUserId, "long_term") +
        db.memoryDao().getByType(currentUserId, "short_term")
    }

    fun getMemoriesFlow(): Flow<List<MemoryEntity>> = db.memoryDao().getAll(currentUserId)

    suspend fun buildContextString(): String = withContext(Dispatchers.IO) {
        val memories = getAllMemories().take(10)
        if (memories.isEmpty()) return@withContext ""
        val sb = StringBuilder("\n\nUser Memory Context:\n")
        memories.forEach { m -> sb.append("- ${m.key}: ${m.value}\n") }
        sb.toString()
    }

    suspend fun extractAndSaveFromConversation(userText: String, aiResponse: String) {
        scope.launch(Dispatchers.IO) {
            val lower = userText.lowercase()
            val nameMatch = Regex("(?:my name is|call me|i am)\\s+(\\w+)", RegexOption.IGNORE_CASE).find(lower)
            nameMatch?.let { remember("user_name", it.groupValues[1], importance = 10) }
            val prefMatch = Regex("i (?:like|prefer|love|enjoy)\\s+(.+?)(?:\\.|$)", RegexOption.IGNORE_CASE).find(lower)
            prefMatch?.let { remember("preference_${System.currentTimeMillis()}", it.groupValues[1], importance = 6) }
        }
    }

    fun getUserId(): Long = currentUserId
    fun getCachedUserName(): String = cachedUserName
}
