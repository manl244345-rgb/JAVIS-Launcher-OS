package com.javis.launcher.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.javis.launcher.R
import com.javis.launcher.ui.home.HomeActivity

class JavisService : Service() {
    companion object {
        const val NOTIF_CHANNEL = "javis_service"
        const val NOTIF_ID = 1001
        fun start(ctx: Context) = ctx.startForegroundService(Intent(ctx, JavisService::class.java))
        fun stop(ctx: Context) = ctx.stopService(Intent(ctx, JavisService::class.java))
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotif())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    private fun createChannel() {
        val ch = NotificationChannel(NOTIF_CHANNEL, "JAVIS Assistant", NotificationManager.IMPORTANCE_LOW)
            .apply { description = "JAVIS AI Launcher"; setShowBadge(false) }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
    }

    private fun buildNotif() = NotificationCompat.Builder(this, NOTIF_CHANNEL)
        .setContentTitle("JAVIS Active")
        .setContentText("AI Companion running")
        .setSmallIcon(R.drawable.ic_javis_notif)
        .setContentIntent(PendingIntent.getActivity(this, 0,
            Intent(this, HomeActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
        .setOngoing(true)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null
}
