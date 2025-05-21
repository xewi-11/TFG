package com.example.bookcloud.model

data class Usuario(
    var uid: String? = null,
    var nombre: String? = null,
    var apellido: String? = null,
    var correo: String? = null,
    var password: String? = null,
    var ftoPerfil: String? = null,
    var librosCarrito: Map<String, Libro>? = null,
    var librosFavoritos: Map<String, Libro>? = null,
    var librosPublicados: Map<String, Libro>? = null // Nueva propiedad
) {
    constructor() : this(null, null, null, null, null, null, null, null, null)
}

