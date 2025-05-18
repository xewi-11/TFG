package com.example.bookcloud.Ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bookcloud.DAO.UserDAO
import com.example.bookcloud.R
import com.example.bookcloud.model.Usuario
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.*

class DialogProfileUser : DialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var textNombre: EditText
    private lateinit var textApellido: EditText
    private lateinit var textCorreo: EditText
    private lateinit var textPassword: EditText
    private lateinit var vista: View
    private lateinit var usuarioDao: UserDAO
    private lateinit var listener: onRequestConfirmacion
    private lateinit var btnFoto: ShapeableImageView
    private lateinit var btnActualizar: Button
    var passwordVisible = false

    private var imageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                imageUri = selectedImageUri

                // Mostrar imagen
                Glide.with(requireContext()).load(imageUri).into(btnFoto)

                // Subir a Firebase
                subirImagenAFirebase(imageUri!!)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        auth = FirebaseAuth.getInstance()
        vista = LayoutInflater.from(context).inflate(R.layout.view_detail_user, null)
        listener = context as onRequestConfirmacion
        usuarioDao = UserDAO(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(vista)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        textNombre = vista.findViewById(R.id.textNombre)
        textApellido = vista.findViewById(R.id.textApellido)
        textCorreo = vista.findViewById(R.id.textCorreo)
        textPassword = vista.findViewById(R.id.editTextPassword)
        btnFoto = vista.findViewById(R.id.btnFoto)
        btnActualizar = vista.findViewById(R.id.btnGuardar)

        btnFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }
        btnActualizar.setOnClickListener {
            actualizarPerfil()
        }
        cogerPerfil()
        textPassword.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // 0=left, 1=top, 2=right, 3=bottom
                val drawable = textPassword.compoundDrawables[drawableEnd]
                if (drawable != null && event.rawX >= (textPassword.right - drawable.bounds.width() - textPassword.paddingEnd)) {
                    passwordVisible = !passwordVisible
                    if (passwordVisible) {
                        textPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        textPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ojo_abierto, 0)
                    } else {
                        textPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        textPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ojo, 0)
                    }
                    textPassword.setSelection(textPassword.text.length)

                    // ✔ Llamada para accesibilidad
                    v.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun subirImagenAFirebase(uri: Uri) {
        // 1. Obtener la referencia al Storage y al archivo específico
        // Puedes usar getInstance() sin el gs:// URL si es el bucket por defecto
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("profile_images/${UUID.randomUUID()}.jpg")

        Log.d("FIREBASE_STORAGE", "Iniciando subida de imagen a: ${fileRef.path}")

        // 2. Iniciar la tarea de subida
        fileRef.putFile(uri)
            // 3. Agregar listener para el éxito de la subida
            .addOnSuccessListener { taskSnapshot ->
                Log.d("FIREBASE_STORAGE", "Imagen subida con éxito.")

                // 4. Después de subir la imagen, obtener la URL de descarga
                fileRef.downloadUrl
                    // 5. Agregar listener para el éxito de obtener la URL
                    .addOnSuccessListener { downloadUri ->
                        // 6. Llamar a la función para guardar la URL en Firestore
                        // Esta función (guardarUrlEnFirestore) es donde se define userRef
                        // y se usa SetOptions.merge si es necesario.
                        guardarUrlEnFirestore(downloadUri.toString())
                    }
                    // 7. Agregar listener para el fallo al obtener la URL
                    .addOnFailureListener { e ->
                        Log.e("FIREBASE_STORAGE", "Error al obtener la URL de descarga", e)
                        // Aquí podrías notificar al usuario que la imagen se subió,
                        // pero no se pudo guardar la referencia (URL).
                    }
            }
            // 8. Agregar listener para el fallo de la subida inicial
            .addOnFailureListener { e ->
                Log.e("FIREBASE_STORAGE", "Error al subir imagen a Storage", e)
                // Aquí podrías notificar al usuario que la subida falló completamente.
            }
    }

    private fun guardarUrlEnFirestore(downloadUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("usuarios").document(userId)

        userRef.update("fotoPerfil", downloadUrl)
            .addOnSuccessListener {
                Log.d("FIRESTORE", "URL actualizada en Firestore")
            }
            .addOnFailureListener {
                // Si el documento no existe aún
                userRef.set(mapOf("fotoPerfil" to downloadUrl))
            }
    }

    private fun cogerPerfil() {
        lifecycleScope.launch {
            val usuario = usuarioDao.getUser()
            if (usuario != null) {
                textNombre.setText(usuario.nombre)
                textApellido.setText(usuario.apellido)
                textCorreo.setText(usuario.correo)
                textPassword.setText(usuario.password)

                // Cargar imagen si ya hay una
                if (!usuario.ftoPerfil.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(usuario.ftoPerfil)
                        .placeholder(R.drawable.perfil)
                        .into(btnFoto)
                }

                listener.onConfirmacion(true)
            } else {
                listener.onConfirmacion(false)
            }
        }
    }
    private fun actualizarPerfil() {
        val nombre = textNombre.text.toString().takeIf { it.isNotBlank() }
        val apellido = textApellido.text.toString().takeIf { it.isNotBlank() }
        val correo = textCorreo.text.toString().takeIf { it.isNotBlank() }
        val password = textPassword.text.toString().takeIf { it.isNotBlank() }


        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            val actualizado = usuarioDao.updateUser(
                uid = userId,
                nombre = nombre,
                apellido = apellido,
                correo = correo,
                password = password,
            )

            listener.onConfirmacion(actualizado)
        }
    }



    interface onRequestConfirmacion {
        fun onConfirmacion(respuesta: Boolean)
    }
}