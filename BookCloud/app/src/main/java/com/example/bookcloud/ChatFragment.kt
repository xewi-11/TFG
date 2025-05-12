package com.example.bookcloud

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcloud.adapter.AdapterMessage
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment: AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterMessage
    private lateinit var editMessage: EditText
    private lateinit var btnSend: Button
    private val messages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()
        setContentView(R.layout.fragment_chat)
        recyclerView = findViewById(R.id.recyclerMessages)
        editMessage =  findViewById(R.id.editMessage)
        btnSend = findViewById(R.id.btnSend)
        adapter = AdapterMessage(messages)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView.adapter = adapter
        btnSend.setOnClickListener {
            val msg = editMessage.text.toString()
            if(msg.isNotEmpty()){
                val messageData = hashMapOf(
                    "text" to msg,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                db.collection("chats")
                    .document("chat_general") // o usa un ID dinámico por usuario
                    .collection("messages")
                    .add(messageData)
                editMessage.text.clear()
            }
            db.collection("chats")
                .document("chat_general")
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshots, e ->
                    if (e != null || snapshots == null) return@addSnapshotListener

                    messages.clear()
                    for (doc in snapshots.documents) {
                        doc.getString("text")?.let { messages.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
        }
    }
}