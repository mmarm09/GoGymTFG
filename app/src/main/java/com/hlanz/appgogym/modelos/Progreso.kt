package com.hlanz.appgogym.modelos

data class Progreso(
    var id: Int,
    var peso_kg: Double,
    var altura_cm: Int,
    var imc: Double,
    var fecha: String,
    var id_usuario: Int
)