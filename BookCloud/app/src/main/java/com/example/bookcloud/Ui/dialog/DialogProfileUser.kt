package com.example.bookcloud.Ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.bookcloud.DAO.UserDAO
import com.example.bookcloud.R
import com.example.bookcloud.databinding.ViewDetailUserBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

/**
 * [DialogProfileUser] es un [DialogFragment] que muestra el perfil del usuario autenticado.
 *
 * Este diálogo obtiene los datos del usuario desde Firebase Realtime Database mediante
 * una llamada suspendida usando corrutinas. Los datos se muestran en campos de texto
 * editables al abrir el diálogo.
 *
 * Se utiliza `lifecycleScope` para lanzar corrutinas vinculadas al ciclo de vida del fragmento,
 * evitando fugas de memoria o ejecuciones innecesarias si el fragmento se destruye.
 */
class DialogProfileUser : DialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var textNombre: EditText
    private lateinit var textApellido: EditText
    private lateinit var textCorreo: EditText
    private lateinit var textPassword: EditText
    private lateinit var vista: View
    private lateinit var dialog: DialogProfileUser
    private lateinit var database: FirebaseDatabase
    private lateinit var UsuarioDao: UserDAO
    private lateinit var listener: onRequestConfirmacion

    /**
     * Se ejecuta cuando el fragmento se adjunta al contexto. Inicializa Firebase y el DAO.
     *
     * @param context El contexto de la actividad que contiene este diálogo.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        dialog = DialogProfileUser()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://bookcloud-440ad-default-rtdb.europe-west1.firebasedatabase.app/")
        vista = LayoutInflater.from(context).inflate(R.layout.view_detail_user, null)
        listener=context as onRequestConfirmacion
        UsuarioDao = UserDAO(context)
    }

    /**
     * Crea y configura el diálogo utilizando una vista personalizada.
     *
     * @param savedInstanceState Estado guardado del diálogo.
     * @return Una instancia de [Dialog] con la vista del perfil de usuario.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builder.setView(vista)
        //builder.setMessage("prueba")
        return builder.create()
    }

    /**
     * Se llama cuando el diálogo ya está visible. Inicializa los campos de texto y
     * lanza la carga del perfil del usuario.
     */
    override fun onStart() {
        super.onStart()
        textNombre = vista.findViewById(R.id.textNombre)
        textApellido = vista.findViewById(R.id.textApellido)
        textCorreo = vista.findViewById(R.id.textCorreo)
        textPassword = vista.findViewById(R.id.editTextPassword)
        cogerPerfil()
    }

    /**
     * Obtiene el perfil del usuario actual desde Firebase y actualiza los campos de texto del diálogo.
     *
     * Se utiliza `lifecycleScope.launch` para ejecutar la operación de forma asíncrona y segura,
     * asegurando que la corrutina se cancele automáticamente si el fragmento se destruye,
     * evitando fugas de memoria o actualizaciones a vistas inexistentes.
     */
    fun cogerPerfil() {
        lifecycleScope.launch {
            val usuario = UsuarioDao.getUser()
            if (usuario != null) {
                textNombre.setText(usuario.nombre)
                textApellido.setText(usuario.apellido)
                textCorreo.setText(usuario.correo)
                textPassword.setText(usuario.password)
                listener.onConfirmacion(true)
            } else {
               listener.onConfirmacion(false)
            }
        }
    }
    interface onRequestConfirmacion{
        fun onConfirmacion(respuesta:Boolean)
    }
}
