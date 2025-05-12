package com.example.bookcloud.model

class Usuario(var uid:String?=null,var nombre:String?=null, var apellido:String?=null,var correo:String?=null,var password:String?=null,libros:ArrayList<Libro?>) {
    constructor() : this(null, null, null, null, null, arrayListOf())
}