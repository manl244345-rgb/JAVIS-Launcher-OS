package com.javis.launcher.ui.allapps

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javis.launcher.R
import com.javis.launcher.models.AppInfo

class AppGridAdapter(private val onClick: (AppInfo) -> Unit) : ListAdapter<AppInfo, AppGridAdapter.VH>(Diff()) {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iv_app_icon)
        val name: TextView = view.findViewById(R.id.tv_app_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_app_grid, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = getItem(position)
        holder.name.text = app.appName
        try {
            val pm = holder.itemView.context.packageManager
            holder.icon.setImageDrawable(pm.getApplicationIcon(app.packageName))
        } catch (e: PackageManager.NameNotFoundException) {
            holder.icon.setImageResource(android.R.drawable.sym_def_app_icon)
        }
        holder.itemView.setOnClickListener { onClick(app) }
    }

    class Diff : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(a: AppInfo, b: AppInfo) = a.packageName == b.packageName
        override fun areContentsTheSame(a: AppInfo, b: AppInfo) = a == b
    }
}
