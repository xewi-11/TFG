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

class AdapterMessage(
    private val messages: List<Map<String, Any>>,
    private val currentUserId: String
) : RecyclerView.Adapter<AdapterMessage.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        val layout: LinearLayout = itemView as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val messageMap = messages[position]
        val messageText = messageMap["text"] as? String ?: ""
        val senderId = messageMap["senderId"] as? String ?: ""

        val isSender = senderId == currentUserId
        val params = holder.textMessage.layoutParams as LinearLayout.LayoutParams

        // Estilo y alineación
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
        holder.textMessage.text = messageText

        // Si es una ubicación, abrir en mapa
        if (messageText.contains("maps.google.com")) {
            holder.textMessage.setTextColor(Color.BLUE)
            holder.textMessage.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(messageText))
                it.context.startActivity(intent)
            }
        } else {
            holder.textMessage.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = messages.size
}