package com.javis.launcher.ui.missioncontrol

import android.content.Intent
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.javis.launcher.JavisApplication
import com.javis.launcher.databinding.ActivityMissionControlBinding
import com.javis.launcher.services.JavisNotificationListener

class MissionControlActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMissionControlBinding
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() { updateStatus(); handler.postDelayed(this, 5000) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissionControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        updateStatus()
    }

    override fun onResume() { super.onResume(); handler.post(updateRunnable) }
    override fun onPause() { super.onPause(); handler.removeCallbacks(updateRunnable) }

    private fun updateStatus() {
        val app = JavisApplication.instance
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024

        binding.tvAiProvider.text = "AI: ${app.aiProviderManager.getCurrentProviderName()}"
        binding.tvBattery.text = "Battery: $battery%"
        binding.tvMemoryUsage.text = "RAM Used: ${usedMem}MB"
        binding.tvNotifications.text = JavisNotificationListener.getSummary()
    }
}
