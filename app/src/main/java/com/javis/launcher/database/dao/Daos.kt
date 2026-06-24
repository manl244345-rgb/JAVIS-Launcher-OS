package com.javis.launcher.database.dao

import androidx.room.*
import com.javis.launcher.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_entries ORDER BY lastAccessed DESC")
    fun getAll(): Flow<List<MemoryEntity>>
    @Query("SELECT * FROM memory_entries WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: String): MemoryEntity?
    @Query("SELECT * FROM memory_entries WHERE category = :cat ORDER BY confidence DESC")
    suspend fun getByCategory(cat: String): List<MemoryEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entry: MemoryEntity)
    @Delete suspend fun delete(entry: MemoryEntity)
    @Query("DELETE FROM memory_entries") suspend fun deleteAll()
    @Query("SELECT COUNT(*) FROM memory_entries") suspend fun count(): Int
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE sessionId = :sid ORDER BY timestamp ASC")
    suspend fun getSession(sid: String): List<ConversationEntity>
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 100): List<ConversationEntity>
    @Insert suspend fun insert(msg: ConversationEntity): Long
    @Query("DELETE FROM conversations WHERE timestamp < :before") suspend fun deleteOlderThan(before: Long)
    @Query("SELECT COUNT(*) FROM conversations") suspend fun count(): Int
}

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAll(): Flow<List<AlarmEntity>>
    @Query("SELECT * FROM alarms WHERE isEnabled = 1") suspend fun getActive(): List<AlarmEntity>
    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1") suspend fun getById(id: Int): AlarmEntity?
    @Insert suspend fun insert(alarm: AlarmEntity): Long
    @Update suspend fun update(alarm: AlarmEntity)
    @Query("DELETE FROM alarms WHERE id = :id") suspend fun deleteById(id: Int)
}

@Dao
interface InstalledAppDao {
    @Query("SELECT * FROM installed_apps ORDER BY appName ASC") fun getAll(): Flow<List<InstalledAppEntity>>
    @Query("SELECT * FROM installed_apps WHERE isFavorite = 1 ORDER BY useCount DESC") fun getFavorites(): Flow<List<InstalledAppEntity>>
    @Query("SELECT * FROM installed_apps ORDER BY lastUsed DESC LIMIT :limit") suspend fun getRecent(limit: Int = 8): List<InstalledAppEntity>
    @Query("SELECT * FROM installed_apps WHERE lower(appName) LIKE '%' || lower(:q) || '%'") suspend fun search(q: String): List<InstalledAppEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(app: InstalledAppEntity)
    @Query("UPDATE installed_apps SET isFavorite = :fav WHERE packageName = :pkg") suspend fun setFavorite(pkg: String, fav: Boolean)
    @Query("UPDATE installed_apps SET lastUsed = :t, useCount = useCount + 1 WHERE packageName = :pkg") suspend fun recordUse(pkg: String, t: Long = System.currentTimeMillis())
    @Query("DELETE FROM installed_apps WHERE packageName = :pkg") suspend fun deleteByPackage(pkg: String)
}

@Dao
interface CommandLogDao {
    @Query("SELECT * FROM command_log ORDER BY timestamp DESC LIMIT :limit") fun getRecent(limit: Int = 200): Flow<List<CommandLogEntity>>
    @Insert suspend fun insert(log: CommandLogEntity): Long
    @Query("DELETE FROM command_log WHERE timestamp < :before") suspend fun deleteOlderThan(before: Long)
}

@Dao
interface NotificationCacheDao {
    @Query("SELECT * FROM notifications_cache ORDER BY timestamp DESC LIMIT 50") fun getRecent(): Flow<List<NotificationCacheEntity>>
    @Query("SELECT * FROM notifications_cache WHERE isRead = 0 ORDER BY timestamp DESC") suspend fun getUnread(): List<NotificationCacheEntity>
    @Insert suspend fun insert(n: NotificationCacheEntity): Long
    @Query("UPDATE notifications_cache SET isRead = 1 WHERE packageName = :pkg") suspend fun markRead(pkg: String)
    @Query("DELETE FROM notifications_cache WHERE timestamp < :before") suspend fun deleteOlderThan(before: Long)
}

@Dao
interface VoiceProfileDao {
    @Query("SELECT * FROM voice_profiles ORDER BY createdAt DESC") fun getAll(): Flow<List<VoiceProfileEntity>>
    @Query("SELECT * FROM voice_profiles WHERE isActive = 1 LIMIT 1") suspend fun getActive(): VoiceProfileEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(profile: VoiceProfileEntity)
    @Query("UPDATE voice_profiles SET isActive = 0") suspend fun clearActive()
    @Query("UPDATE voice_profiles SET isActive = 1 WHERE profileId = :id") suspend fun setActive(id: String)
    @Delete suspend fun delete(profile: VoiceProfileEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueTime ASC") fun getActive(): Flow<List<TaskEntity>>
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC") fun getAll(): Flow<List<TaskEntity>>
    @Insert suspend fun insert(task: TaskEntity): Long
    @Update suspend fun update(task: TaskEntity)
    @Query("UPDATE tasks SET isCompleted = 1 WHERE id = :id") suspend fun complete(id: Int)
    @Delete suspend fun delete(task: TaskEntity)
}
