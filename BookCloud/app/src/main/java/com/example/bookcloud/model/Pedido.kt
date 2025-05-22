package com.example.bookcloud.model

data class Pedido(
    var uid: String? = null,
    var libros:ArrayList<Libro>,
    var estado: Boolean// Nueva propiedad
) {
    constructor() : this(null, arrayListOf(), false)
}