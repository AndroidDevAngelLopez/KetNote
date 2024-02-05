package com.complexsoft.ketnote.ui.screen.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.model.NotificationItem
import com.google.android.material.textview.MaterialTextView

class NotificationItemAdapter(
    private var notificationItems: List<NotificationItem>,
    private val onNotificationItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationItemAdapter.NotificationItemViewHolder>() {

    fun updateList(newNotificationItems: List<NotificationItem>) {
        val noteDiff = NotificationItemDiffUtil(notificationItems, newNotificationItems)
        val result = DiffUtil.calculateDiff(noteDiff)
        notificationItems = newNotificationItems
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): NotificationItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item_layout, parent, false)
        return NotificationItemViewHolder(view)
    }

    class NotificationItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val notificationTitle: MaterialTextView
        val notificationText: MaterialTextView

        init {
            notificationTitle = view.findViewById(R.id.notification_title)
            notificationText = view.findViewById(R.id.notification_subtitle)
        }
    }

    override fun getItemCount(): Int = notificationItems.size


    override fun onBindViewHolder(
        holder: NotificationItemViewHolder, position: Int
    ) {
        holder.itemView.setOnClickListener {
            onNotificationItemClick(notificationItems[position])
        }
        holder.notificationTitle.text = notificationItems[position].title
        holder.notificationText.text = notificationItems[position].description
    }

}