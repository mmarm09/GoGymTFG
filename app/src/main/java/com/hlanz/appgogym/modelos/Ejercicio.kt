package com.hlanz.appgogym.modelos

import java.io.Serializable

data class Ejercicio(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val complemento: String,
    val grupo_muscular: String,
    val foto: String,
    val foto_detalle: String
) : Serializable