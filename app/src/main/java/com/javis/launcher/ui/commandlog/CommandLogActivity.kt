package com.javis.launcher.ui.commandlog

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
import com.javis.launcher.database.entities.CommandLogEntity
import com.javis.launcher.databinding.ActivityCommandLogBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CommandLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommandLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommandLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Command Log"
        val adapter = LogAdapter()
        binding.rvLog.layoutManager = LinearLayoutManager(this)
        binding.rvLog.adapter = adapter
        lifecycleScope.launch {
            JavisApplication.instance.database.commandLogDao().getRecent().collect { adapter.submitList(it) }
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}

class LogAdapter : ListAdapter<CommandLogEntity, LogAdapter.VH>(object : DiffUtil.ItemCallback<CommandLogEntity>() {
    override fun areItemsTheSame(a: CommandLogEntity, b: CommandLogEntity) = a.id == b.id
    override fun areContentsTheSame(a: CommandLogEntity, b: CommandLogEntity) = a == b
}) {
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_log, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvCmd: TextView = v.findViewById(R.id.tv_command)
        private val tvResult: TextView = v.findViewById(R.id.tv_result)
        private val tvTime: TextView = v.findViewById(R.id.tv_time)
        fun bind(e: CommandLogEntity) {
            tvCmd.text = "[${e.intentType}] ${e.command}"
            tvResult.text = e.result
            tvTime.text = sdf.format(Date(e.timestamp))
            tvResult.setTextColor(if (e.success) 0xFF00CC44.toInt() else 0xFFCC2200.toInt())
        }
    }
}
