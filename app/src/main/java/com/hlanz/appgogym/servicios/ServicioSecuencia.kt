package com.hlanz.appgogym.servicios

import com.hlanz.appgogym.modelos.Secuencia
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ServicioSecuencia {
    @GET("leer.php")
    fun obtenerSecuencias(): Call<List<Secuencia>>

    @GET("leer.php/{id_ejercicio}")
    fun obtenerSecuenciasPorEjercicio(@Path("id_ejercicio") idEjercicio: Int): Call<List<Secuencia>>

    @POST("insertar.php")
    fun agregarSecuencia(@Body secuencia: Secuencia): Call<Secuencia>

    @PUT("actualizar.php/{id}")
    fun actualizarSecuencia(@Path("id") id: Int, @Body secuencia: Secuencia): Call<Secuencia>

    @DELETE("borrar.php/{id}")
    fun eliminarSecuencia(@Path("id") id: Int): Call<Void>
}