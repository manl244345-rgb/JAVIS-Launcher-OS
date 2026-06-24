package com.javis.launcher.ui.allapps

import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javis.launcher.R
import com.javis.launcher.models.AppInfo

class AppGridAdapter(private val onClick: (AppInfo) -> Unit) : ListAdapter<AppInfo, AppGridAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(a: AppInfo, b: AppInfo) = a.packageName == b.packageName
            override fun areContentsTheSame(a: AppInfo, b: AppInfo) = a.appName == b.appName && a.isFavorite == b.isFavorite
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_app_grid, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val icon: ImageView = v.findViewById(R.id.iv_app_icon)
        private val name: TextView = v.findViewById(R.id.tv_app_name)
        fun bind(app: AppInfo) {
            icon.setImageDrawable(app.icon)
            name.text = app.appName
            itemView.setOnClickListener { onClick(app) }
        }
    }
}
