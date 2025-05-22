package com.example.bookcloud

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcloud.DAO.UserDAO
import com.example.bookcloud.databinding.ViewRecycledBinding
import com.example.bookcloud.model.Libro
import com.example.navegacion.adapter.AdapterCarritoFav
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID


class FavoritosFragment: Fragment() {
    private lateinit var adapterCarritoFav: AdapterCarritoFav
    private lateinit var recycledBinding: RecyclerView
    private lateinit var bnt:Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var listaFavoritos:ArrayList<Libro>
    private lateinit var userDAO: UserDAO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_carrito_fav, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
          recycledBinding=view.findViewById(R.id.recycledCarritoFav)
          bnt=view.findViewById(R.id.btnComprar)
        listaFavoritos= arrayListOf()
        userDAO= UserDAO(requireContext())
        auth=FirebaseAuth.getInstance()
        database= FirebaseDatabase.getInstance("https://bookcloud-440ad-default-rtdb.europe-west1.firebasedatabase.app/")
        recycledBinding.layoutManager=
            GridLayoutManager(requireContext(),2, LinearLayoutManager.VERTICAL,false)
        adapterCarritoFav= AdapterCarritoFav(listaFavoritos,requireContext(),"")
        recycledBinding.adapter=adapterCarritoFav
        adapterCarritoFav.notifyDataSetChanged()
        cogerLibrosFavoritos()

        bnt.isEnabled=false
    }

    fun cogerLibrosFavoritos() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("usuarios").child(userId).child("librosFavoritos").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        for (libroSnapshot in snapshot.children) {
                            val libro = libroSnapshot.getValue(Libro::class.java)
                            if (libro != null) {
                                listaFavoritos.add(libro)
                                adapterCarritoFav.notifyDataSetChanged()
                            }
                        }
                        adapterCarritoFav.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "No hay libros en favoritos", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al cargar los libros de favoritos", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }


}