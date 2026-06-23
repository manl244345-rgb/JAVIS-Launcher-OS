package com.javis.launcher.database.dao

import androidx.room.*
import com.javis.launcher.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1") suspend fun getUser(): UserEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(user: UserEntity): Long
    @Update suspend fun update(user: UserEntity)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC") fun getAll(): Flow<List<ConversationEntity>>
    @Query("SELECT * FROM conversations WHERE id = :id") suspend fun getById(id: String): ConversationEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(c: ConversationEntity)
    @Update suspend fun update(c: ConversationEntity)
    @Delete suspend fun delete(c: ConversationEntity)
    @Query("DELETE FROM conversations WHERE id = :id") suspend fun deleteById(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getByConversation(convId: String): Flow<List<MessageEntity>>
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(convId: String, limit: Int = 20): List<MessageEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(msg: MessageEntity): Long
    @Query("DELETE FROM messages WHERE conversationId = :convId") suspend fun deleteByConversation(convId: String)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory WHERE userId = :userId ORDER BY importance DESC, accessedAt DESC")
    fun getAll(userId: Long): Flow<List<MemoryEntity>>
    @Query("SELECT * FROM memory WHERE userId = :userId AND type = :type ORDER BY importance DESC")
    suspend fun getByType(userId: Long, type: String): List<MemoryEntity>
    @Query("SELECT * FROM memory WHERE userId = :userId AND (key LIKE :query OR value LIKE :query) LIMIT 10")
    suspend fun search(userId: Long, query: String): List<MemoryEntity>
    @Query("SELECT * FROM memory WHERE userId = :userId AND key = :key LIMIT 1")
    suspend fun getByKey(userId: Long, key: String): MemoryEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(m: MemoryEntity): Long
    @Update suspend fun update(m: MemoryEntity)
    @Delete suspend fun delete(m: MemoryEntity)
    @Query("UPDATE memory SET accessedAt = :time WHERE id = :id") suspend fun updateAccess(id: Long, time: Long = System.currentTimeMillis())
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC") fun getAll(): Flow<List<TaskEntity>>
    @Query("SELECT * FROM tasks WHERE status = :status") suspend fun getByStatus(status: String): List<TaskEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(t: TaskEntity)
    @Update suspend fun update(t: TaskEntity)
    @Query("UPDATE tasks SET status = :status WHERE id = :id") suspend fun updateStatus(id: String, status: String)
}

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute") fun getAll(): Flow<List<AlarmEntity>>
    @Query("SELECT * FROM alarms WHERE id = :id") suspend fun getById(id: Int): AlarmEntity?
    @Query("SELECT * FROM alarms WHERE isEnabled = 1") suspend fun getEnabled(): List<AlarmEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(a: AlarmEntity): Long
    @Update suspend fun update(a: AlarmEntity)
    @Delete suspend fun delete(a: AlarmEntity)
    @Query("DELETE FROM alarms WHERE id = :id") suspend fun deleteById(id: Int)
}

@Dao
interface CommandHistoryDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<CommandHistoryEntity>>
    @Insert suspend fun insert(c: CommandHistoryEntity): Long
    @Query("DELETE FROM command_history WHERE timestamp < :before") suspend fun cleanOld(before: Long)
}

@Dao
interface NotificationHistoryDao {
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 100): Flow<List<NotificationHistoryEntity>>
    @Insert suspend fun insert(n: NotificationHistoryEntity): Long
    @Query("DELETE FROM notification_history WHERE timestamp < :before") suspend fun cleanOld(before: Long)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit") fun getRecent(limit: Int = 200): Flow<List<LogEntity>>
    @Insert suspend fun insert(l: LogEntity): Long
    @Query("DELETE FROM logs WHERE timestamp < :before") suspend fun cleanOld(before: Long)
}

@Dao
interface InstalledAppDao {
    @Query("SELECT * FROM installed_apps WHERE isSystemApp = 0 ORDER BY appName ASC")
    fun getUserApps(): Flow<List<InstalledAppEntity>>
    @Query("SELECT * FROM installed_apps ORDER BY launchCount DESC LIMIT :limit")
    suspend fun getFrequent(limit: Int = 6): List<InstalledAppEntity>
    @Query("SELECT * FROM installed_apps WHERE isFavorite = 1") fun getFavorites(): Flow<List<InstalledAppEntity>>
    @Query("SELECT * FROM installed_apps WHERE appName LIKE :query OR packageName LIKE :query")
    suspend fun search(query: String): List<InstalledAppEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(apps: List<InstalledAppEntity>)
    @Update suspend fun update(app: InstalledAppEntity)
    @Query("UPDATE installed_apps SET launchCount = launchCount + 1, lastLaunched = :time WHERE packageName = :pkg")
    suspend fun incrementLaunchCount(pkg: String, time: Long = System.currentTimeMillis())
    @Query("DELETE FROM installed_apps WHERE packageName NOT IN (:packages)") suspend fun removeUninstalled(packages: List<String>)
}
