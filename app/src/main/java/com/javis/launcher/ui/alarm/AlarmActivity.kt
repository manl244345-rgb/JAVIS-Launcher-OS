package com.javis.launcher.ui.alarm

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.javis.launcher.JavisApplication
import com.javis.launcher.databinding.ActivityAlarmBinding
import com.javis.launcher.services.AlarmReceiver
import com.javis.launcher.utils.AlarmHelper
import kotlinx.coroutines.launch

class AlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val label = intent.getStringExtra(AlarmReceiver.EXTRA_LABEL) ?: "JAVIS Alarm"
        val id = intent.getIntExtra(AlarmReceiver.EXTRA_ID, 0)
        binding.tvAlarmLabel.text = label
        binding.tvAlarmTime.text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        binding.btnDismiss.setOnClickListener { dismissAlarm(id); finish() }
        binding.btnSnooze.setOnClickListener { snoozeAlarm(id, label); finish() }
        JavisApplication.instance.voiceManager.speak("Good morning Sir. It's time. $label.")
    }

    private fun dismissAlarm(id: Int) {
        lifecycleScope.launch { if (id > 0) AlarmHelper.deleteAlarm(this@AlarmActivity, id) }
    }

    private fun snoozeAlarm(id: Int, label: String) {
        lifecycleScope.launch {
            val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MINUTE, 10) }
            AlarmHelper.setAlarm(this@AlarmActivity, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), "Snooze: $label")
        }
    }
}
