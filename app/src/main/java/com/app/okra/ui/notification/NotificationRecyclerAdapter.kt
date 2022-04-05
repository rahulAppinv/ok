package com.app.okra.ui.notification

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.ui.notification.NotificationRecyclerAdapter.MyViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.app.okra.R
import com.app.okra.models.Notification
import com.app.okra.utils.getPastTimeString
import kotlinx.android.synthetic.main.row_notification.view.*

class NotificationRecyclerAdapter internal constructor(
    private val mContext: Context,
    private val list: List<Notification>
) : RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_notification, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
         holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        fun onBind(position: Int) {

            itemView.tvNotification.text = list[position].title
            itemView.tvTime.text = getPastTimeString(list[position].created)
        }
    }
}