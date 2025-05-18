package com.example.bookcloud

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddBookFragment : Fragment() {

    private lateinit var editId: EditText
    private lateinit var editNombre: EditText
    private lateinit var editAutor: EditText
    private lateinit var editPrecio: EditText
    private lateinit var editDescripcion: EditText
    private lateinit var editCategoria: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnSelectImage: Button
    private lateinit var imagePreview: ImageView

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "user1"
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data!!.data
            imagePreview.setImageURI(selectedImageUri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        editId = view.findViewById(R.id.editId)
        editNombre = view.findViewById(R.id.editNombre)
        editAutor = view.findViewById(R.id.editAutor)
        editPrecio = view.findViewById(R.id.editPrecio)
        editDescripcion = view.findViewById(R.id.editDescripcion)
        editCategoria = view.findViewById(R.id.editCategoria)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        imagePreview = view.findViewById(R.id.imagePreview)

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }

        btnGuardar.setOnClickListener {
            val id = editId.text.toString().trim()
            val nombre = editNombre.text.toString().trim()
            val autor = editAutor.text.toString().trim()
            val precio = editPrecio.text.toString().trim()
            val descripcion = editDescripcion.text.toString().trim()
            val categoria = editCategoria.text.toString().trim()

            if (id.isEmpty() || nombre.isEmpty() || autor.isEmpty() || precio.isEmpty() || descripcion.isEmpty() || categoria.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                val fileRef = storage.reference.child("libros/${UUID.randomUUID()}.jpg")
                fileRef.putFile(selectedImageUri!!)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) throw task.exception ?: Exception("Error al subir imagen")
                        fileRef.downloadUrl
                    }
                    .addOnSuccessListener { uri ->
                        guardarLibro(id, nombre, autor, precio, descripcion, categoria, uri.toString())
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
                    }
            } else {
                guardarLibro(id, nombre, autor, precio, descripcion, categoria, null)
            }
        }
    }

    private fun guardarLibro(
        id: String,
        nombre: String,
        autor: String,
        precio: String,
        descripcion: String,
        categoria: String,
        fotoUrl: String?
    ) {
        val libro = hashMapOf(
            "id" to id,
            "idUsuario" to currentUserId,
            "nombre" to nombre,
            "autor" to autor,
            "precio" to precio,
            "descripcion" to descripcion,
            "categoria" to categoria,
            "foto" to fotoUrl
        )

        db.collection("books").document(id)
            .set(libro)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Libro guardado con imagen", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al guardar el libro", Toast.LENGTH_SHORT).show()
            }
    }
}