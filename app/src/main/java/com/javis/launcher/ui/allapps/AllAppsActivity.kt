package com.javis.launcher.ui.allapps

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.javis.launcher.databinding.ActivityAllAppsBinding
import com.javis.launcher.models.AppInfo
import com.javis.launcher.utils.AppLauncher
import kotlinx.coroutines.launch

class AllAppsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllAppsBinding
    private lateinit var adapter: AppGridAdapter
    private var allApps = listOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        setupSearch()
        loadApps()
    }

    private fun setupRecyclerView() {
        adapter = AppGridAdapter { app ->
            lifecycleScope.launch {
                AppLauncher.findAndLaunchApp(this@AllAppsActivity, app.appName)
                finish()
            }
        }
        binding.rvApps.apply {
            layoutManager = GridLayoutManager(this@AllAppsActivity, 4)
            this.adapter = this@AllAppsActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                adapter.submitList(if (query.isBlank()) allApps else allApps.filter { it.appName.lowercase().contains(query) })
            }
        })
    }

    private fun loadApps() {
        lifecycleScope.launch {
            allApps = AppLauncher.getInstalledApps(this@AllAppsActivity)
            adapter.submitList(allApps)
        }
    }

    override fun onBackPressed() { finish() }
}
