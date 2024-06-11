package com.hlanz.appgogym.servicios

import com.hlanz.appgogym.modelos.Ejercicio
import com.hlanz.appgogym.modelos.RutinaEjercicio
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ServicioEjercicio {
    @GET("leer.php")
    fun getEjercicios(@Query("tipo") tipo: String? = null,
                      @Query("complemento") complemento: String? = null,
                      @Query("grupo_muscular") grupoMuscular: String? = null): Call<List<Ejercicio>>

    @GET("leer.php")
    fun getEjerciciosTodos(@Query("id") id: Int): Call<List<Ejercicio>>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("insertar.php")
    fun insertarEjercicio(@Body e: Ejercicio): Call<Ejercicio>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("borrar.php")
    fun borrarEjercicio(@Body body: RequestBody): Call<Ejercicio>
}