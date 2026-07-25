package com.linhnt.notifications.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.linhnt.notifications.R
import com.linhnt.notifications.model.DeliveryState
import com.linhnt.notifications.model.NotifyItem
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    val list = ArrayList<NotifyItem>()
    private val amountFormatter = NumberFormat.getIntegerInstance(Locale("vi", "VN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val amountValue = item.amount.filter(Char::isDigit)
            .toBigIntegerOrNull()
            ?: BigInteger.ZERO
        val amountText = amountFormatter.format(amountValue)

        holder.tvTitle.text = holder.itemView.context.getString(
            R.string.notify_item_title,
            amountText,
            item.app
        )
        holder.tvDesc.text = holder.itemView.context.getString(
            R.string.notify_item_desc,
            item.account,
            item.source
        )
        holder.tvTime.text = item.time
        holder.imgStatus.setImageResource(
            if (item.deliveryState == DeliveryState.SENT || item.status) {
                R.drawable.ic_success
            } else {
                R.drawable.ic_fail
            }
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun replaceAll(items: Collection<NotifyItem>) {
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
    }
}
