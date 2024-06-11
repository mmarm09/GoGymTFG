package com.hlanz.appgogym.servicios

import com.hlanz.appgogym.modelos.Rutina
import com.hlanz.appgogym.modelos.Usuario
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ServicioRutina {

    @GET("leer.php")
    fun getRutina(@Query("id") id: Int): Call<List<Rutina?>>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("insertar.php")
    fun insertarRutina(@Body r: Rutina): Call<Rutina>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("borrar.php")
    fun borrarRutina(@Body body: RequestBody): Call<Rutina>
}