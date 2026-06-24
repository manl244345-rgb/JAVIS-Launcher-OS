package com.javis.launcher.ui.allapps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.javis.launcher.JavisApplication
import com.javis.launcher.databinding.ActivityAllAppsBinding
import com.javis.launcher.models.AppInfo
import com.javis.launcher.utils.AppLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllAppsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllAppsBinding
    private lateinit var adapter: AppGridAdapter
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AppGridAdapter { app ->
            lifecycleScope.launch {
                JavisApplication.instance.database.installedAppDao().recordUse(app.packageName)
            }
            AppLauncher.findAndLaunchApp(this, app.appName)
        }
        binding.rvApps.layoutManager = GridLayoutManager(this, 4)
        binding.rvApps.adapter = adapter
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { filterApps(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
        loadApps()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadApps() {
        lifecycleScope.launch {
            allApps = withContext(Dispatchers.IO) { AppLauncher.getAllApps(this@AllAppsActivity) }
            adapter.submitList(allApps)
        }
    }

    private fun filterApps(q: String) {
        val filtered = if (q.isBlank()) allApps else allApps.filter { it.appName.contains(q, ignoreCase = true) }
        adapter.submitList(filtered)
    }

    override fun onBackPressed() { super.onBackPressed(); overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) }
}
