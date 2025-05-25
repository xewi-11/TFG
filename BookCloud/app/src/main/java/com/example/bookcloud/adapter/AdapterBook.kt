package com.example.bookcloud.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookcloud.ChatFragment
import com.example.bookcloud.DAO.UserDAO
import com.example.bookcloud.R
import com.example.bookcloud.model.Libro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterBook(var listaProductos:ArrayList<Libro>,val context: Context):
    RecyclerView.Adapter<AdapterBook.myHolder>() {
    private lateinit var listener: OnBookListener
    private lateinit var UsuarioDAO: UserDAO
    private lateinit var auth: FirebaseAuth

    inner class myHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imagen: ImageView =itemView.findViewById(R.id.imageCard)
        val toolbar: Toolbar =itemView.findViewById(R.id.toolbarCard)
        init {
            toolbar.inflateMenu(R.menu.menu_card)
            listener = context as OnBookListener;
            UsuarioDAO= UserDAO(context)
            auth= FirebaseAuth.getInstance()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myHolder {
        val view= LayoutInflater.from(context).inflate(R.layout.view_recycled,parent,false)
        return myHolder(view)
    }

    override fun getItemCount(): Int {
        return listaProductos.size
    }

    override fun onBindViewHolder(holder: myHolder, position: Int) {
        val libro=listaProductos[position]
        Glide.with(context).load(libro.foto).into(holder.imagen)
        holder.toolbar.setTitle("${libro.nombre}")
        holder.toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.menu_carritoCard->{
                    // uso de corrutinas ya que intento llamar a un metodo suspend desde otro que no lo es,
                    // y para hacer eso posible tengo que usar corrutinas
                    CoroutineScope(Dispatchers.IO).launch {
                        UsuarioDAO.añadirLibrosCarrito(auth.uid.toString(), libro)
                    }
                }
                R.id.menu_favoritosCard->{
                    CoroutineScope(Dispatchers.IO).launch {
                        UsuarioDAO.añadirLibroAFavoritos(auth.uid.toString(), libro)
                    }
                }
                R.id.menu_detallesCard->{
                    listener.onBookDetailClicK(libro)
                }
                R.id.menu_Chat->{
                    // Aquí puedes agregar la lógica para abrir el chat
                    val intent = Intent(context, ChatFragment::class.java)
                    intent.putExtra("ownerId", libro.idUsuario) // el vendedor
                    intent.putExtra("bookId", libro.id)  // el libro
                    listener.onBookClick(libro)
                }

            }
            true
        }
    }

    interface OnBookListener {
        fun onBookClick(libro: Libro)
        fun onBookDetailClicK(libro: Libro)
    }
}