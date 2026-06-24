package com.javis.launcher.ui.missioncontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.javis.launcher.JavisApplication
import com.javis.launcher.databinding.ActivityMissionControlBinding
import com.javis.launcher.services.JavisNotificationListener
import com.javis.launcher.ui.chat.ChatActivity
import kotlinx.coroutines.launch

class MissionControlActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMissionControlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissionControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mission Control"
        refresh()
        binding.btnChat.setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }
        binding.btnRefresh.setOnClickListener { refresh() }
    }

    private fun refresh() {
        lifecycleScope.launch {
            val app = JavisApplication.instance
            val provider = app.memoryManager.getAIProvider()
            val model = app.memoryManager.getAIModel()
            val voice = app.memoryManager.getVoiceType()
            val battery = getBattery()
            val notifCount = JavisNotificationListener.getAllNotifications().size
            val memCount = app.database.memoryDao().count()
            val alarmCount = app.database.alarmDao().getActive().size

            binding.tvProvider.text = "AI: $provider / $model"
            binding.tvVoice.text = "Voice: $voice"
            binding.tvBattery.text = "Battery: $battery%"
            binding.tvNotifications.text = "Notifications: $notifCount unread"
            binding.tvMemory.text = "Memory: $memCount entries"
            binding.tvAlarms.text = "Alarms: $alarmCount active"
        }
    }

    private fun getBattery(): Int {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100f / scale).toInt() else -1
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
