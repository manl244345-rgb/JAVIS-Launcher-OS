package com.javis.launcher.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.javis.launcher.ui.alarm.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_ID = "alarm_id"
        const val EXTRA_LABEL = "alarm_label"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, 0)
        val label = intent.getStringExtra(EXTRA_LABEL) ?: "JAVIS Alarm"
        val i = Intent(context, AlarmActivity::class.java).apply {
            putExtra(EXTRA_ID, id); putExtra(EXTRA_LABEL, label)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(i)
    }
}
