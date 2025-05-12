package com.example.bookcloud.DAO

import android.content.Context
import com.example.bookcloud.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * Data Access Object (DAO) responsable de gestionar las operaciones relacionadas
 * con los usuarios en la base de datos de Firebase.
 *
 * @property context Contexto de la aplicación o componente que lo utiliza.
 *
 * Este DAO encapsula la lógica de acceso a la base de datos de usuarios en Firebase,
 * proporcionando una interfaz simple y segura para recuperar los datos del usuario actual.
 */
class UserDAO(val context: Context) {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    /**
     * Inicializa las instancias de autenticación y base de datos de Firebase.
     */
    init {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://bookcloud-440ad-default-rtdb.europe-west1.firebasedatabase.app/")
    }

    /**
     * Recupera el objeto [Usuario] correspondiente al usuario actualmente autenticado.
     *
     * @return [Usuario] si se encuentra un usuario con el UID actual en la base de datos, o `null` si no existe o hay un error.
     *
     * Esta función es `suspend`, por lo que debe ser llamada desde una corrutina o función `suspend`.
     */
    suspend fun getUser(): Usuario? {
        return try {
            val snapshot = database.reference
                .child("usuarios")
                .child(auth.currentUser!!.uid)
                .get()
                .await()
            snapshot.getValue(Usuario::class.java)
        } catch (e: Exception) {
            println("Error al obtener usuario: ${e.message}")
            null
        }
    }
}
