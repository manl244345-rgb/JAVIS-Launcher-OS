package com.javis.launcher.ui.memory

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.javis.launcher.databinding.ActivityMemoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MemoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMemoryBinding
    private val viewModel: MemoryViewModel by viewModels()
    private lateinit var adapter: MemoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        adapter = MemoryAdapter()
        binding.rvMemory.apply {
            layoutManager = LinearLayoutManager(this@MemoryActivity)
            this.adapter = this@MemoryActivity.adapter
        }
        lifecycleScope.launch {
            viewModel.memories.collectLatest { adapter.submitList(it) }
        }
    }
}
