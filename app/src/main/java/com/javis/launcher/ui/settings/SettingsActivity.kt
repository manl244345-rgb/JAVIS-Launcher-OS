package com.javis.launcher.ui.settings

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.javis.launcher.JavisApplication
import com.javis.launcher.databinding.ActivitySettingsBinding
import com.javis.launcher.providers.AIProviderManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val memory get() = JavisApplication.instance.memoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        val providerNames = AIProviderManager(this).providers.map { it.name }
        binding.spinnerProvider.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providerNames)

        lifecycleScope.launch {
            binding.etName.setText(memory.getUserName())
            binding.etApiKey.setText(memory.getApiKey())
            binding.switchWakeWord.isChecked = memory.isWakeWordEnabled()
            val currentProvider = memory.getAIProvider()
            val idx = AIProviderManager(this@SettingsActivity).providers.indexOfFirst { it.id == currentProvider }
            if (idx >= 0) binding.spinnerProvider.setSelection(idx)
        }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                memory.setUserName(binding.etName.text.toString().trim().ifBlank { "Sir" })
                memory.setApiKey(binding.etApiKey.text.toString().trim())
                memory.setWakeWordEnabled(binding.switchWakeWord.isChecked)
                val selectedProvider = AIProviderManager(this@SettingsActivity).providers[binding.spinnerProvider.selectedItemPosition]
                memory.setAIProvider(selectedProvider.id)
                memory.setAIModel(selectedProvider.defaultModel)
                Toast.makeText(this@SettingsActivity, "Settings saved, Sir.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
