package com.javis.launcher.ui.memory

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javis.launcher.JavisApplication
import com.javis.launcher.R
import com.javis.launcher.database.entities.MemoryEntity
import com.javis.launcher.databinding.ActivityMemoryBinding
import kotlinx.coroutines.launch

class MemoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMemoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Memory Bank"
        val adapter = MemoryAdapter()
        binding.rvMemory.layoutManager = LinearLayoutManager(this)
        binding.rvMemory.adapter = adapter
        val db = JavisApplication.instance.database
        lifecycleScope.launch {
            db.memoryDao().getAll().collect { adapter.submitList(it) }
        }
        binding.btnClearMemory.setOnClickListener {
            lifecycleScope.launch { db.memoryDao().deleteAll() }
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}

class MemoryAdapter : ListAdapter<MemoryEntity, MemoryAdapter.VH>(object : DiffUtil.ItemCallback<MemoryEntity>() {
    override fun areItemsTheSame(a: MemoryEntity, b: MemoryEntity) = a.id == b.id
    override fun areContentsTheSame(a: MemoryEntity, b: MemoryEntity) = a == b
}) {
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_memory, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvKey: TextView = v.findViewById(R.id.tv_key)
        private val tvValue: TextView = v.findViewById(R.id.tv_value)
        private val tvCategory: TextView = v.findViewById(R.id.tv_category)
        fun bind(m: MemoryEntity) { tvKey.text = m.key; tvValue.text = m.value; tvCategory.text = m.category }
    }
}
