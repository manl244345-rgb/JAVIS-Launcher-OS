package com.javis.launcher.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.javis.launcher.databinding.ActivitySettingsBinding
import com.javis.launcher.utils.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferenceManager(this)
        binding.toolbar.setNavigationOnClickListener { finish() }
        loadSettings()
        setupSaveButton()
    }

    private fun loadSettings() {
        binding.etApiKey.setText(prefs.getApiKey())
        binding.etUserName.setText(prefs.getUserName())
        binding.switchWakeWord.isChecked = prefs.isWakeWordEnabled()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            prefs.setApiKey(binding.etApiKey.text.toString().trim())
            prefs.setUserName(binding.etUserName.text.toString().trim().ifBlank { "Sir" })
            prefs.setWakeWordEnabled(binding.switchWakeWord.isChecked)
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
