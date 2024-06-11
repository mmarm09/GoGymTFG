package com.hlanz.appgogym.servicios

import com.hlanz.appgogym.modelos.Usuario
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ServicioUsuario {
    @GET("leer.php")
    fun getUsuario(@Query("id") id: Int): Call<List<Usuario?>>


    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("insertar.php")
    fun insertarUsuario(@Body u: Usuario): Call<Usuario>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("actualizar.php")
    fun editarUsuario(@Body u: Usuario): Call<Usuario>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("borrar.php")
    fun borrarUsuario(@Body body: RequestBody): Call<Usuario>
}