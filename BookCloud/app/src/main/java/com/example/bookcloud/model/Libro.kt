package com.example.bookcloud.model

class Libro(
    var id: String? = null,
    var idUsuario: String? = null,
    var nombre: String? = null,
    var autor: String? = null,
    var precio: String? = null,
    var foto: String? = null,
    var descripcion: String? = null,
    var categoria: String? = null,
) {
    constructor() : this(null, null, null, null, null, null, null, null)
}