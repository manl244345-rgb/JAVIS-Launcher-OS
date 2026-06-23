package com.javis.launcher.ui.memory

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javis.launcher.R
import com.javis.launcher.database.entities.MemoryEntity

class MemoryAdapter : ListAdapter<MemoryEntity, MemoryAdapter.VH>(Diff()) {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvKey: TextView = view.findViewById(R.id.tv_memory_key)
        val tvValue: TextView = view.findViewById(R.id.tv_memory_value)
        val tvType: TextView = view.findViewById(R.id.tv_memory_type)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_memory, parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = getItem(position)
        holder.tvKey.text = m.key.replace("_", " ").replaceFirstChar { it.uppercase() }
        holder.tvValue.text = m.value
        holder.tvType.text = m.type.replace("_", " ")
    }
    class Diff : DiffUtil.ItemCallback<MemoryEntity>() {
        override fun areItemsTheSame(a: MemoryEntity, b: MemoryEntity) = a.id == b.id
        override fun areContentsTheSame(a: MemoryEntity, b: MemoryEntity) = a == b
    }
}
