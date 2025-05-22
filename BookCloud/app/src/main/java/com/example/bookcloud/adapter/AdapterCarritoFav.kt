package com.example.navegacion.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookcloud.R
import com.example.bookcloud.model.Libro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdapterCarritoFav(
    var lista: ArrayList<Libro>,
    val context: Context,
    val type: String
) : RecyclerView.Adapter<AdapterCarritoFav.myHolder>() {

    val listaSeleccionados = mutableListOf<Libro>()  // Lista de seleccionados

    inner class myHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imageCard)
        val text: TextView = itemView.findViewById(R.id.textCarr)
        val boton: Button = itemView.findViewById(R.id.btnDelete)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)  // Usa un CheckBox de verdad
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_recycled_carrito_fav, parent, false)
        return myHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: myHolder, position: Int) {
        val producto = lista[position]

        holder.text.text = producto.nombre
        Glide.with(context).load(producto.foto).into(holder.imagen)

        holder.boton.setOnClickListener {
            lista.removeAt(position)
            listaSeleccionados.remove(producto) // también lo quitamos de los seleccionados si se elimina
            notifyDataSetChanged()
        }

        if (type == "carrito") {
            holder.checkBox.visibility = View.VISIBLE
            holder.checkBox.setOnCheckedChangeListener(null)  // evitamos reciclaje incorrecto
            holder.checkBox.isChecked = listaSeleccionados.contains(producto)

            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!listaSeleccionados.contains(producto)) {
                        listaSeleccionados.add(producto)
                    }
                } else {
                    listaSeleccionados.remove(producto)
                }
            }
        } else {
            holder.checkBox.visibility = View.GONE
        }
    }
}
