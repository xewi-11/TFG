package com.example.bookcloud.model

data class Usuario(
    var uid: String? = null,
    var nombre: String? = null,
    var apellido: String? = null,
    var correo: String? = null,
    var password: String? = null,
    var ftoPerfil: String? = null,
    val librosCarrito: Map<String, Libro>? = null,
    val librosFavoritos: Map<String, Libro>? = null
) {
    constructor() : this(null, null, null, null, null, null, null, null)
}
