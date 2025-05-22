package com.example.bookcloud

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcloud.adapter.AdapterMessage
import com.example.bookcloud.model.Message
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterMessage
    private lateinit var editMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var btnLocation: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val messages = mutableListOf<Message>()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "user1"
    private var otherUserId: String = ""
    private var bookId: String = ""
    private var chatId: String = ""

    private fun generateChatId(user1: String, user2: String): String {
        return listOf(user1, user2).sorted().joinToString("_")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseApp.initializeApp(requireContext())
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.recyclerMessages)
        editMessage = view.findViewById(R.id.editMessage)
        btnSend = view.findViewById(R.id.btnSend)
        btnLocation = view.findViewById(R.id.btnLocation)

        // Obtener argumentos
        val args = arguments
        if (args == null || !args.containsKey("ownerId") || !args.containsKey("bookId")) {
            Toast.makeText(requireContext(), "Faltan argumentos", Toast.LENGTH_LONG).show()
            return
        }

        otherUserId = args.getString("ownerId", "")
        bookId = args.getString("bookId", "")
        chatId = generateChatId(currentUserId, otherUserId)
        Log.d("ChatFragment", "chatId=$chatId, currentUserId=$currentUserId, otherUserId=$otherUserId")

        // Configurar RecyclerView
        adapter = AdapterMessage(messages, currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Escuchar mensajes
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                Log.d("ChatFragment", "Mensajes recibidos: ${snapshots!!.size()}")
                messages.clear()
                for (doc in snapshots.documents) {
                    Log.d("ChatFragment", "Doc: ${doc.data}")
                    doc.toObject(Message::class.java)?.let {
                        messages.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)

            }

        // Enviar mensaje
        btnSend.setOnClickListener {
            Log.d("ChatFragment", "Botón Enviar clicado")

            val msg = editMessage.text.toString().trim()
            if (msg.isEmpty()) {
                Toast.makeText(requireContext(), "Mensaje vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val messageData = hashMapOf(
                "text" to msg,
                "senderId" to currentUserId,
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener {
                    Log.d("ChatFragment", "Mensaje enviado con éxito")
                }
                .addOnFailureListener {
                    Log.e("ChatFragment", "Error al enviar mensaje", it)
                }

            saveChatReference(currentUserId, otherUserId, chatId, bookId)
            saveChatReference(otherUserId, currentUserId, chatId, bookId)

            editMessage.text.clear()
        }

        // Enviar ubicación
        btnLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1001
                )
                return@setOnClickListener
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location == null) {
                    Toast.makeText(requireContext(), "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val locationUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                val messageData = hashMapOf(
                    "text" to locationUrl,
                    "senderId" to currentUserId,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(messageData)

                saveChatReference(currentUserId, otherUserId, chatId, bookId)
                saveChatReference(otherUserId, currentUserId, chatId, bookId)
            }
        }

        Toast.makeText(requireContext(), "Fragmento cargado", Toast.LENGTH_SHORT).show()
    }

    private fun saveChatReference(userId: String, otherUserId: String, chatId: String, bookId: String) {
        // Evita guardar el chat si es contigo mismo
        if (userId == otherUserId) return

        val ref = hashMapOf(
            "chatId" to chatId,
            "withUser" to otherUserId,
            "bookId" to bookId,
            "lastUpdate" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .collection("chats")
            .document(chatId)
            .set(ref)
            .addOnSuccessListener {
                Log.d("ChatFragment", "ChatRef guardado para $userId con $otherUserId")
            }
            .addOnFailureListener {
                Log.e("ChatFragment", "Error al guardar ChatRef para $userId", it)
            }
    }

    companion object {
        fun newInstance(ownerId: String, bookId: String): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle().apply {
                putString("ownerId", ownerId)
                putString("bookId", bookId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}