package com.javis.launcher.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.javis.launcher.R
import com.javis.launcher.ui.home.HomeActivity

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val ALARM_CHANNEL_ID = "javis_alarm_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "JAVIS Alarm"
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, 0)
        showAlarmNotification(context, label, alarmId)
    }

    private fun showAlarmNotification(context: Context, label: String, alarmId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(ALARM_CHANNEL_ID, "JAVIS Alarms", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Alarm notifications"; enableVibration(true) }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, alarmId, Intent(context, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setContentTitle("⏰ $label")
            .setContentText("Your JAVIS alarm is ringing")
            .setSmallIcon(R.drawable.ic_javis_orb)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(alarmId + 2000, notification)
    }
}
