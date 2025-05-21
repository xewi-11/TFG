package com.example.bookcloud.DAO

import android.content.Context
import com.example.bookcloud.model.Libro
import com.example.bookcloud.model.Pedido
import com.example.bookcloud.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
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

    suspend fun updateUser(uid: String, nombre: String? = null, apellido: String? = null, correo: String? = null, password: String? = null): Boolean {
        return try {
            val updates = mutableMapOf<String, Any?>()

            nombre?.let { updates["nombre"] = it }
            apellido?.let { updates["apellido"] = it }
            correo?.let { updates["correo"] = it }
            password?.let { updates["password"] = it }

            if (updates.isNotEmpty()) {
                database.reference
                    .child("usuarios")
                    .child(uid)
                    .updateChildren(updates)
                    .await()
                true
            } else {
                // No hay campos para actualizar
                false
            }
        } catch (e: Exception) {
            println("Error al actualizar usuario: ${e.message}")
            false
        }
    }

    suspend fun añadirLibrosCarrito(uid: String, libro: Libro) {
        try {
            database.reference
                .child("usuarios")
                .child(uid)
                .child("librosCarrito")
                .child(libro.id.toString())
                .setValue(libro)
                .await()
        } catch (e: Exception) {
            println("Error al añadir libro al carrito: ${e.message}")
        }
    }

    suspend fun añadirLibroAFavoritos(uid: String, libro: Libro) {
        try {
            database.reference
                .child("usuarios")
                .child(uid)
                .child("librosFavoritos")
                .child(libro.id.toString())
                .setValue(libro)
                .await()
        } catch (e: Exception) {
            println("Error al añadir libro a favoritos: ${e.message}")
        }
    }
    suspend fun añadirLibroPublicado(uid: String, libro: Libro) {
        try {
            if (uid.equals(libro.idUsuario)) {
                database.reference
                    .child("usuarios")
                    .child(uid)
                    .child("librosPublicados")
                    .setValue(libro.id.toString())
                    .await()
            }
        } catch (e: Exception) {
            println("Error al añadir libro publicado: ${e.message}")
        }
    }
    suspend fun añadirPedidoRealizado(uid: String, libros: ArrayList<Libro>) {
        var numeroPedidos=0
        try {
            database.reference
                .child("usuarios")
                .child(uid)
                .child("pedidosRealizados")
                .child((numeroPedidos+1).toString())
                .setValue(libros)
                .await()
        } catch (e: Exception) {
            println("Error al añadir pedido realizado: ${e.message}")
        }
    }
    suspend fun cogerPedidosRealizados(uid: String): ArrayList<Pedido>? {
        return try {
            val snapshot = database.reference
                .child("usuarios")
                .child(uid)
                .child("pedidosRealizados")
                .get()
                .await()

            val listaPedidos = ArrayList<Pedido>()

            for (pedidoSnapshot in snapshot.children) {
                val libros = pedidoSnapshot.getValue(object : GenericTypeIndicator<ArrayList<Libro>>() {})
                if (libros != null) {
                    listaPedidos.add(Pedido(uid, libros, false))
                }
            }

            listaPedidos
        } catch (e: Exception) {
            println("Error al obtener pedidos realizados: ${e.message}")
            null
        }
    }
}
