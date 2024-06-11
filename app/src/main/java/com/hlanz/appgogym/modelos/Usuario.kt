package com.hlanz.appgogym.modelos

data class Usuario(
    var id: Int,
    var dni: String,
    var nombre: String,
    var apellidos: String,
    var telefono: Int,
    var correo: String,
    var fecha_nacimiento: String,
    var contrasena: String,
    var foto: String?,
    var id_rutina: Int?
)