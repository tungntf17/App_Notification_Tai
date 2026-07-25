package com.linhnt.notifications.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.linhnt.notifications.R
import com.linhnt.notifications.model.NotifyItem
import java.text.DecimalFormat

class NotificationAdapter() : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    var list = ArrayList<NotifyItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (position >= list.size) return
        val item = list[position]

        var amount = item.amount
        if (amount == "") {
            amount = "0"
        }
        val amountFormat = DecimalFormat("#,###,###").format(amount.toInt())
        holder.tvTitle.text = String.format(holder.itemView.context.getString(R.string.notify_item_title), amountFormat, item.app)
        holder.tvDesc.text = String.format(holder.itemView.context.getString(R.string.notify_item_desc), item.account, item.source)
        holder.tvTime.text = item.time

        if (item.status) {
            holder.imgStatus.setImageResource(R.drawable.ic_success)
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_fail)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun add(item: NotifyItem): Boolean {
        var duplicate = false
        for (data in list) {
            if (data.app == item.app && data.source == item.source && data.amount == item.amount && data.account == item.account && data.time == item.time) {
                duplicate = true
                break
            }
        }

        if (!duplicate) {
            list.add(0, item)
            notifyItemInserted(0)
        }
        return duplicate
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
    }
}