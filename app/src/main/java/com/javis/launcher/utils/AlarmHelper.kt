package com.javis.launcher.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.javis.launcher.JavisApplication
import com.javis.launcher.database.entities.AlarmEntity
import com.javis.launcher.services.AlarmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object AlarmHelper {

    suspend fun setAlarm(
        context: Context,
        hour: Int,
        minute: Int,
        label: String,
        isRepeating: Boolean = false,
        repeatDays: List<Int> = emptyList(),
        isWakeUp: Boolean = false
    ): AlarmSetResult = withContext(Dispatchers.IO) {
        val db = JavisApplication.instance.database
        val entity = AlarmEntity(
            label = label, hour = hour, minute = minute,
            isRepeating = isRepeating,
            repeatDays = repeatDays.joinToString(","),
            isWakeUp = isWakeUp
        )
        val id = db.alarmDao().insert(entity).toInt()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }
        scheduleAlarm(context, id, label, calendar.timeInMillis, isRepeating)
        val verified = db.alarmDao().getById(id)
        if (verified != null) {
            AlarmSetResult.Success(id, hour, minute, label)
        } else {
            AlarmSetResult.Failed("Could not verify alarm was saved")
        }
    }

    private fun scheduleAlarm(context: Context, id: Int, label: String, triggerAt: Long, repeating: Boolean) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
        }
        val pi = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    suspend fun deleteAlarm(context: Context, alarmId: Int): Boolean = withContext(Dispatchers.IO) {
        val db = JavisApplication.instance.database
        val alarm = db.alarmDao().getById(alarmId) ?: return@withContext false
        cancelAlarm(context, alarmId)
        db.alarmDao().deleteById(alarmId)
        true
    }

    private fun cancelAlarm(context: Context, alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)
    }

    fun formatTime(hour: Int, minute: Int): String {
        val h = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        val amPm = if (hour >= 12) "PM" else "AM"
        return "$h:${minute.toString().padStart(2, '0')} $amPm"
    }
}

sealed class AlarmSetResult {
    data class Success(val id: Int, val hour: Int, val minute: Int, val label: String) : AlarmSetResult()
    data class Failed(val reason: String) : AlarmSetResult()
}
