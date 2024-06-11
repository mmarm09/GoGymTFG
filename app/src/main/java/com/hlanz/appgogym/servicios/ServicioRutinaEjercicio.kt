package com.hlanz.appgogym.servicios

import com.hlanz.appgogym.modelos.Rutina
import com.hlanz.appgogym.modelos.RutinaEjercicio
import com.hlanz.appgogym.modelos.Usuario
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ServicioRutinaEjercicio {
    @GET("leer.php")
    fun getRutinaEjercicioLista(@Query("id_rutina") id_rutina: Int, @Query("id_ejercicio") id_ejercicio: Int): Call<List<RutinaEjercicio>>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("insertar.php")
    fun insertarRutinaEjercicio(@Body re: RutinaEjercicio): Call<RutinaEjercicio>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("borrar.php")
    fun borrarRutinaEjercicio(@Body body: RequestBody): Call<RutinaEjercicio>

    @GET("leer.php")
    fun getRutinaEjercicioPorDia(@Query("dia") dia: Int): Call<List<RutinaEjercicio>>

}