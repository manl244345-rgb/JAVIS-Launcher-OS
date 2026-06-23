package com.javis.launcher.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.javis.launcher.R
import com.javis.launcher.ui.home.HomeActivity

class JavisService : Service() {
    companion object {
        const val CHANNEL_ID = "javis_service_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.javis.launcher.STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JAVIS Active")
            .setContentText("Your AI companion is ready")
            .setSmallIcon(R.drawable.ic_javis_orb)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "JAVIS Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Keeps JAVIS running in the background" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
