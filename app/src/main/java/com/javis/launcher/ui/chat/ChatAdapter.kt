package com.javis.launcher.ui.chat

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javis.launcher.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a.timestamp == b.timestamp && a.role == b.role
            override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
        }
        private const val VIEW_USER = 0
        private const val VIEW_AI = 1
        private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    override fun getItemViewType(pos: Int) = if (getItem(pos).role == "user") VIEW_USER else VIEW_AI

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        val layout = if (type == VIEW_USER) R.layout.item_chat_user else R.layout.item_chat_ai
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvContent: TextView = v.findViewById(R.id.tv_content)
        private val tvTime: TextView = v.findViewById(R.id.tv_time)
        fun bind(msg: ChatMessage) {
            tvContent.text = msg.content
            tvTime.text = sdf.format(Date(msg.timestamp))
        }
    }
}
