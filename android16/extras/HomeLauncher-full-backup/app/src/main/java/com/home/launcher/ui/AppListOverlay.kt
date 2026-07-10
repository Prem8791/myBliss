package com.home.launcher.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.home.launcher.R
import com.home.launcher.data.AppEntry

class AppListOverlay {

    companion object {
        fun show(activity: Activity, anchor: View, title: String, apps: List<AppEntry>, onDismiss: (() -> Unit)? = null) {
            val builder = AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
            val view = LayoutInflater.from(activity).inflate(R.layout.overlay_app_list, null)

            val titleView: TextView = view.findViewById<TextView>(R.id.overlayTitle)!!
            val recyclerView: RecyclerView = view.findViewById<RecyclerView>(R.id.overlayAppList)!!
            val closeButton: View = view.findViewById<View>(R.id.overlayClose)!!

            titleView.text = title

            val dialog = builder.setView(view).create()

            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = AppListAdapter(apps) { entry ->
                try {
                    val intent = activity.packageManager.getLaunchIntentForPackage(entry.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        dialog.dismiss()
                        activity.startActivity(intent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppListOverlay", "Failed to launch ${entry.packageName}", e)
                }
            }

            val loc = IntArray(2)
            anchor.getLocationOnScreen(loc)

            dialog.window?.let { w ->
                val lp = w.attributes
                lp.gravity = Gravity.TOP or Gravity.LEFT
                lp.x = loc[0]
                lp.y = loc[1]
                lp.width = anchor.width
                lp.height = anchor.height
                w.attributes = lp
                w.setBackgroundDrawable(ColorDrawable(Color.parseColor("#CC000000")))
            }

            dialog.setOnDismissListener { onDismiss?.invoke() }
            dialog.show()

            closeButton.setOnClickListener { dialog.dismiss() }
        }
    }

    private class AppListAdapter(
        private val apps: List<AppEntry>,
        private val onTap: (AppEntry) -> Unit
    ) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = apps[position]
            holder.bind(entry, onTap)
        }

        override fun getItemCount(): Int = apps.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val icon: ImageView = itemView.findViewById<ImageView>(R.id.appItemIcon)!!
            private val label: TextView = itemView.findViewById<TextView>(R.id.appItemLabel)!!
            private val favIndicator: View = itemView.findViewById<View>(R.id.appItemFav)!!

            fun bind(entry: AppEntry, onTap: (AppEntry) -> Unit) {
                label.text = entry.label
                if (entry.icon != null) {
                    icon.setImageDrawable(entry.icon)
                }
                favIndicator.visibility = if (entry.isFavourite) View.VISIBLE else View.GONE
                itemView.setOnClickListener { onTap(entry) }
            }
        }
    }
}
