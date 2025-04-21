package com.example.bookcloud

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookcloud.adapter.AdapterBook
import com.example.bookcloud.databinding.FragmentLoginBinding
import com.example.bookcloud.databinding.FragmentMainBinding
import com.example.bookcloud.model.Libro
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.util.ArrayList

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var toolbar: Toolbar
    private lateinit var listaLibros: ArrayList<Libro>
    private lateinit var adapterLibros: AdapterBook
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainBinding.inflate(layoutInflater)
        auth=FirebaseAuth.getInstance()
        toolbar = binding.toolbar

        toolbar.inflateMenu(R.menu.menu_fragment_main)
        return binding.root

    }
    private fun instancias() {
        listaLibros = ArrayList()
        binding.recycledMain.layoutManager= LinearLayoutManager(requireContext())
        adapterLibros = AdapterBook(listaLibros,requireContext())
        binding.recycledMain.adapter=adapterLibros
        adapterLibros.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.menu_perfil -> {
                    true
                }
                R.id.menu_carrito -> {
                    true
                }
                R.id.menu_favoritos -> {
                    true
                }
                else -> false
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}