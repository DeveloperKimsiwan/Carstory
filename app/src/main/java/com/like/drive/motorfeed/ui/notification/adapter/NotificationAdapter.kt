package com.like.drive.motorfeed.ui.notification.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.like.drive.motorfeed.data.notification.NotificationSendData
import com.like.drive.motorfeed.ui.notification.holder.NotificationViewHolder

class NotificationAdapter : RecyclerView.Adapter<NotificationViewHolder>() {

    val list = mutableListOf<NotificationSendData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NotificationViewHolder.from(parent)

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(list[position])
    }

}