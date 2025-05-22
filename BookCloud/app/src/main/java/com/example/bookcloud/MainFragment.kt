package com.example.bookcloud

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookcloud.adapter.AdapterBook
import com.example.bookcloud.databinding.FragmentLoginBinding
import com.example.bookcloud.databinding.FragmentMainBinding
import com.example.bookcloud.model.Libro
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.ArrayList
import java.util.UUID

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment(){

    private lateinit var binding: FragmentMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var toolbar: Toolbar
    private lateinit var listaLibros: ArrayList<Libro>
    private lateinit var adapterLibros: AdapterBook
    private lateinit var database: FirebaseDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainBinding.inflate(layoutInflater)
        auth=FirebaseAuth.getInstance()
        toolbar = binding.toolbar
        listaLibros = ArrayList()
        database=  FirebaseDatabase.getInstance("https://bookcloud-440ad-default-rtdb.europe-west1.firebasedatabase.app/")
        toolbar.inflateMenu(R.menu.menu_fragment_main)
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        cogerLibros()
        binding.recycledMain.layoutManager= GridLayoutManager(requireContext(),2,LinearLayoutManager.VERTICAL,false)
        adapterLibros = AdapterBook(listaLibros,requireContext())
        binding.recycledMain.adapter=adapterLibros
        adapterLibros.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_perfil -> {
                    findNavController().navigate(R.id.action_mainFragment_to_dialogProfileUser)
                    true
                }
                R.id.menu_carrito -> {
                    findNavController().navigate(R.id.action_mainFragment_to_paymentFragment)
                    true
                }
                R.id.menu_favoritos -> {
                    // Puedes agregar lógica aquí si lo necesitas
                    true
                }
                R.id.menu_chats -> {
                    findNavController().navigate(R.id.action_mainFragment_to_chatListFragment)
                    true
                }
                else -> false
            }
        }


    }
    fun cogerLibros() {
        val db = FirebaseFirestore.getInstance()

        db.collection("books").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                for (dc in snapshots.documentChanges) {
                    val libro = dc.document.toObject(Libro::class.java)
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            listaLibros.add(libro)
                            adapterLibros.notifyDataSetChanged()
                            Log.v("libros", listaLibros.toString())
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val index = listaLibros.indexOfFirst { it.id == libro.id }
                            if (index != -1) {
                                listaLibros[index] = libro
                                adapterLibros.notifyItemChanged(index)
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            val index = listaLibros.indexOfFirst { it.id == libro.id }
                            if (index != -1) {
                                listaLibros.removeAt(index)
                                adapterLibros.notifyItemRemoved(index)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }


}
