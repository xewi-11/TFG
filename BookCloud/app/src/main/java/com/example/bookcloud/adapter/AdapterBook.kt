package com.example.bookcloud.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookcloud.R
import com.example.bookcloud.model.Libro

class AdapterBook(var listaProductos:ArrayList<Libro>,val context: Context):
    RecyclerView.Adapter<AdapterBook.myHolder>() {
    private lateinit var listener: OnBookListener

    inner class myHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imagen: ImageView =itemView.findViewById(R.id.imageCard)
        val toolbar: Toolbar =itemView.findViewById(R.id.toolbarCard)
        init {
            toolbar.inflateMenu(R.menu.menu_card)
            listener = context as OnBookListener;
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
                    // Implementar la lógica para agregar el libro al carrito
                }
                R.id.menu_favoritosCard->{
                    // Implementar la lógica para agregar el libro a favoritos
                }
                R.id.menu_detallesCard->{
                    listener.onBookClick(libro)
                }
            }
            true
        }
    }

    interface OnBookListener {
        fun onBookClick(libro: Libro)
    }
}