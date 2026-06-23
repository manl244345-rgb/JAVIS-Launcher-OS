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
import com.javis.launcher.database.entities.CommandHistoryEntity
import com.javis.launcher.databinding.ActivityCommandLogBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CommandLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommandLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommandLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        val adapter = LogAdapter()
        binding.rvLogs.apply {
            layoutManager = LinearLayoutManager(this@CommandLogActivity)
            this.adapter = adapter
        }
        lifecycleScope.launch {
            JavisApplication.instance.database.commandHistoryDao().getRecent(100).collectLatest {
                adapter.submitList(it)
            }
        }
    }

    class LogAdapter : ListAdapter<CommandHistoryEntity, LogAdapter.VH>(Diff()) {
        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvCmd: TextView = view.findViewById(R.id.tv_command)
            val tvTime: TextView = view.findViewById(R.id.tv_time)
            val tvResult: TextView = view.findViewById(R.id.tv_result)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_command_log, parent, false))
        override fun onBindViewHolder(holder: VH, pos: Int) {
            val item = getItem(pos)
            holder.tvCmd.text = item.command
            holder.tvTime.text = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()).format(Date(item.timestamp))
            holder.tvResult.text = if (item.success) "✓" else "✗"
        }
        class Diff : DiffUtil.ItemCallback<CommandHistoryEntity>() {
            override fun areItemsTheSame(a: CommandHistoryEntity, b: CommandHistoryEntity) = a.id == b.id
            override fun areContentsTheSame(a: CommandHistoryEntity, b: CommandHistoryEntity) = a == b
        }
    }
}
