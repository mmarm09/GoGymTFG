package com.hlanz.appgogym.servicios

import com.hlanz.appgogym.modelos.Novedades
import com.hlanz.appgogym.modelos.Usuario
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ServicioNovedades {
    @GET("leer.php")
    fun getNovedades(@Query("id") id: Int): Call<List<Novedades>>
}