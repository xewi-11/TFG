package com.example.bookcloud.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcloud.R

class AdapterMessage(private val messages: List<String>) :
    RecyclerView.Adapter<AdapterMessage.MessageViewHolder>() {
    inner class MessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textMessage: TextView = itemView.findViewById(R.id.textMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterMessage.MessageViewHolder, position: Int) {
       holder.textMessage.text = messages[position]
    }

    override fun getItemCount(): Int = messages.size
}