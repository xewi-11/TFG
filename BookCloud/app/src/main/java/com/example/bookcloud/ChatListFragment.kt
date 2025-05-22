package com.example.bookcloud

import AdapterChatList
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcloud.DAO.UserDAO
import com.example.bookcloud.model.ChatPreview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ChatListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterChatList
    private val chats = mutableListOf<ChatPreview>()
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var userDAO : UserDAO

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userDAO = UserDAO(requireContext())
        recyclerView = view.findViewById(R.id.recyclerChatList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AdapterChatList(chats) { otherUserId, bookId ->
            val bundle = Bundle().apply {
                putString("ownerId", otherUserId)
                putString("bookId", bookId)
            }
            findNavController().navigate(R.id.action_chatListFragment_to_chatFragment, bundle)
        }

        recyclerView.adapter = adapter

        if (currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(currentUserId)
            .collection("chats")
            .orderBy("lastUpdate")
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                chats.clear()

                for (doc in snapshots.documents) {
                    val withUser = doc.getString("withUser") ?: continue
                    val bookId = doc.getString("bookId") ?: continue
                    val chatId = doc.getString("chatId") ?: continue

                    val preview = ChatPreview(chatId, withUser, bookId)
                    chats.add(preview)
                    val index = chats.lastIndex

                    // Usamos corrutina para obtener el usuario por UID
                    lifecycleScope.launch {
                        val user = userDAO.getUserById(withUser)
                        if (user != null) {
                            chats[index].userName = user.nombre ?: "Desconocido"
                            adapter.notifyItemChanged(index)
                        }
                    }

                    // 🔹 Obtener foto desde Firestore
                    db.collection("usuarios").document(withUser).get()
                        .addOnSuccessListener { userDoc ->
                            val foto = userDoc.getString("fotoPerfil")
                            chats[index].userPhotoUrl = foto
                            adapter.notifyItemChanged(index)
                        }

                    // 🔹 Obtener título del libro
                    db.collection("books").document(bookId).get()
                        .addOnSuccessListener { bookDoc ->
                            val titulo = bookDoc.getString("nombre")
                            chats[index].bookTitle = titulo ?: "(sin título)"
                            adapter.notifyItemChanged(index)
                        }
                }

                adapter.notifyDataSetChanged()
            }
    }
}