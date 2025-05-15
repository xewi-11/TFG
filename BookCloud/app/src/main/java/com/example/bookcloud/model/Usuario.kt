package com.example.bookcloud.model

class Usuario(var uid:String?=null,var nombre:String?=null, var apellido:String?=null,var correo:String?=null,var password:String?=null,var ftoPerfil:String?=null,var libros:ArrayList<Libro?>,var librosCarrito:ArrayList<Libro?>,var librosFavoritos:ArrayList<Libro?>) {
    constructor() : this(null, null, null, null, null,null, arrayListOf(), arrayListOf(), arrayListOf())
}