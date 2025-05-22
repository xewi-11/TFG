package com.example.bookcloud.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Libro(
    var id: String? = null,
    var idUsuario: String? = null,
    var nombre: String? = null,
    var autor: String? = null,
    var precio: String? = null,
    var foto: String? = null,
    var descripcion: String? = null,
    var categoria: String? = null,
): Parcelable {
    constructor() : this(null, null, null, null, null, null, null, null)
}