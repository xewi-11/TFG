package com.example.bookcloud.model

class Libro(
    var id: String? = null,
    var nombre: String? = null,
    var autor: String? = null,
    var precio: String? = null,
    var imagen: String? = null,
    var descripcion: String? = null,
    var categoria: String? = null,
    var cantidad: Int? = null
) {
    constructor() : this(null, null, null, null, null, null, null, 0)
}