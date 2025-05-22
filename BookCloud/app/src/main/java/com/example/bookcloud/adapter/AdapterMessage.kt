package com.example.bookcloud.adapter

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcloud.R
import com.example.bookcloud.model.Message
import java.text.SimpleDateFormat
import java.util.*

class AdapterMessage(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<AdapterMessage.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        val textTimestamp: TextView = itemView.findViewById(R.id.textTimestamp)
        val layout: LinearLayout = itemView as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        val isSender = message.senderId == currentUserId

        val params = holder.textMessage.layoutParams as LinearLayout.LayoutParams

        if (isSender) {
            params.gravity = Gravity.END
            holder.textMessage.setBackgroundColor(Color.parseColor("#03A9F4"))
            holder.textMessage.setTextColor(Color.WHITE)
        } else {
            params.gravity = Gravity.START
            holder.textMessage.setBackgroundColor(Color.LTGRAY)
            holder.textMessage.setTextColor(Color.BLACK)
        }

        holder.textMessage.layoutParams = params
        holder.textMessage.text = message.text

        // Mostrar hora si existe timestamp
        message.timestamp?.let {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = it.toDate()
            holder.textTimestamp.text = sdf.format(date)
        } ?: run {
            holder.textTimestamp.text = ""
        }

        // Enlace a ubicación
        if (message.text.contains("maps.google.com")) {
            holder.textMessage.setTextColor(Color.BLUE)
            holder.textMessage.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.text))
                it.context.startActivity(intent)
            }
        } else {
            holder.textMessage.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = messages.size
}