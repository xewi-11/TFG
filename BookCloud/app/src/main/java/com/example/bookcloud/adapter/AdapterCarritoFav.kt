package com.example.navegacion.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.example.bookcloud.R
import com.example.bookcloud.adapter.AdapterBook.OnBookListener
import com.example.bookcloud.model.Libro
import com.example.bookcloud.model.Usuario
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdapterCarritoFav(var lista:ArrayList<Libro>,val context: Context,val type:String): Adapter<AdapterCarritoFav.myHolder>() {
    private lateinit var database: FirebaseDatabase
    private lateinit var listener: OnBookSaleListener
    private lateinit var auth: FirebaseAuth
    private lateinit  var listaCompra:ArrayList<Libro>
    inner class myHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imagen: ImageView =itemView.findViewById(R.id.imageCard)
        val text:TextView=itemView.findViewById(R.id.textCarr)
        val boton:Button=itemView.findViewById(R.id.btnDelete)
        val checkBox:Button=itemView.findViewById(R.id.checkBox)
        val botonComprar:Button=itemView.findViewById(R.id.btnComprar)

        init{

            database =
                FirebaseDatabase.getInstance("https://comprasbd-ed8ae-default-rtdb.europe-west1.firebasedatabase.app/")
            auth=FirebaseAuth.getInstance()
            lista= arrayListOf()
            listener = context as OnBookSaleListener;
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myHolder {
        val view= LayoutInflater.from(context).inflate(R.layout.view_recycled_carrito_fav,parent,false)
        return myHolder(view)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: myHolder, position: Int) {
        val producto=lista[position]
        holder.text.setText("${producto.nombre}")
        Glide.with(context).load(producto.foto?.get(0)).into(holder.imagen)
        holder.boton.setOnClickListener {
            lista.removeAt(position)
            notifyDataSetChanged()
        }
        if (type=="carrito") {
            holder.checkBox.isEnabled = true
            holder.botonComprar.isEnabled = true
            holder.checkBox.setOnClickListener {
                // seleccionar para guardar en la lista la cual se va a enviar cuando le den al boton de pagar
                 listaCompra.contains(producto)
            }
            holder.botonComprar.setOnClickListener {
                // aqui se tiene que hacer el envio de la lista a la base de datos
              // callback
               listener.onBookSale(listaCompra)
            }
        }else{
            holder.checkBox.isEnabled = false
            holder.botonComprar.isEnabled = false
        }
    }

    interface OnBookSaleListener {
        fun onBookSale(libros: ArrayList<Libro>)
    }
}