package com.example.bookcloud

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.bookcloud.model.Libro

class DetailFragment : Fragment(R.layout.fragment_book_detail) {

    private lateinit var libro: Libro

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el objeto Libro del bundle
        libro = arguments?.getParcelable("libro") ?: return

        val imageView = view.findViewById<ImageView>(R.id.bookCoverImage)
        val titleView = view.findViewById<TextView>(R.id.bookTitle)
        val authorView = view.findViewById<TextView>(R.id.bookAuthor)
        val priceView = view.findViewById<TextView>(R.id.bookPrice)
        val descriptionView = view.findViewById<TextView>(R.id.bookDescription)
        // Mostrar datos
        titleView.text = libro.nombre
        authorView.text = libro.autor
        descriptionView.text = libro.descripcion
        priceView.text = "${libro.precio} €" // o "Precio: ${libro.precio} €" si prefieres

        Glide.with(this)
            .load(libro.foto)
            .into(imageView)
    }
}
