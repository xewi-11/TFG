package com.example.bookcloud

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcloud.adapter.AdapterMessage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterMessage
    private lateinit var editMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var btnLocation: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val messages = mutableListOf<Map<String, Any>>()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "user1"
    private val otherUserId = "user2" // Este debería ser dinámico (por intent, etc.)

    private fun generateChatId(user1: String, user2: String): String {
        return listOf(user1, user2).sorted().joinToString("_")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContentView(R.layout.fragment_chat)

        recyclerView = findViewById(R.id.recyclerMessages)
        editMessage = findViewById(R.id.editMessage)
        btnSend = findViewById(R.id.btnSend)
        btnLocation = findViewById(R.id.btnLocation)

        adapter = AdapterMessage(messages, currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val chatId = generateChatId(currentUserId, otherUserId)

        // 🔁 Escuchar mensajes en tiempo real
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                messages.clear()
                for (doc in snapshots.documents) {
                    doc.data?.let { messages.add(it) }
                }
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)
            }

        // ✉️ Enviar texto
        btnSend.setOnClickListener {
            val msg = editMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                val messageData = hashMapOf(
                    "text" to msg,
                    "senderId" to currentUserId,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(messageData)

                saveChatReference(currentUserId, otherUserId, chatId)
                saveChatReference(otherUserId, currentUserId, chatId)

                editMessage.text.clear()
            }
        }

        // 📍 Enviar ubicación
        btnLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1001
                )
                return@setOnClickListener
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val locationUrl = "https://maps.google.com/?q=${it.latitude},${it.longitude}"
                    val messageData = hashMapOf(
                        "text" to locationUrl,
                        "senderId" to currentUserId,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .add(messageData)

                    saveChatReference(currentUserId, otherUserId, chatId)
                    saveChatReference(otherUserId, currentUserId, chatId)
                } ?: Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveChatReference(userId: String, otherUserId: String, chatId: String) {
        val ref = hashMapOf(
            "chatId" to chatId,
            "withUser" to otherUserId,
            "lastUpdate" to FieldValue.serverTimestamp()
        )
        db.collection("users")
            .document(userId)
            .collection("chats")
            .document(chatId)
            .set(ref)
    }
}