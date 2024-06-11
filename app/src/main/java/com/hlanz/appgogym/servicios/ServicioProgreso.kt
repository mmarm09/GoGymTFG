package com.hlanz.appgogym.servicios

import com.hlanz.appgogym.modelos.Progreso
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ServicioProgreso {
    @GET("leer.php")
    fun getProgreso(@Query("id") id: Int): Call<List<Progreso>>


    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("insertar.php")
    fun insertarProgreso(@Body p: Progreso): Call<Progreso>
}