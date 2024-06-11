package com.hlanz.appgogym.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.hlanz.appgogym.R
import com.hlanz.appgogym.adaptadores.NovedadesAdapter
import com.hlanz.appgogym.modelos.Novedades
import com.hlanz.appgogym.servicios.ServicioNovedades
import com.hlanz.appgogym.servicios.ServicioUsuario
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.CoroutineContext

class HomeFragment : Fragment(), CoroutineScope {

    val url = "http://192.168.1.135:80/xampp/api/crud/novedades/"

    private lateinit var recyclerView: RecyclerView
    private lateinit var novedadesAdapter: NovedadesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = root.findViewById(R.id.recyclerViewNovedades)
        recyclerView.layoutManager = LinearLayoutManager(context)
        novedadesAdapter = NovedadesAdapter(emptyList())
        recyclerView.adapter = novedadesAdapter

        obtenerNovedades()

        return root
    }

    private fun obtenerNovedades() {
        launch(Dispatchers.IO){
            val gson = GsonBuilder().setLenient().create()
            //Instancia a retrofit agregando la baseURL y el convertidor GSON
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            val servicioLeer = retrofit.create(ServicioNovedades::class.java)
            val llamada = servicioLeer.getNovedades(-1)

            llamada.enqueue(object : Callback<List<Novedades>> {
                override fun onResponse(call: Call<List<Novedades>>, response: Response<List<Novedades>>) {
                    if (response.isSuccessful) {
                        val novedades = response.body() ?: emptyList()
                        activity?.runOnUiThread {
                            novedadesAdapter = NovedadesAdapter(novedades)
                            recyclerView.adapter = novedadesAdapter
                        }
                    } else {
                        Log.e("HomeFragment", "Error en la respuesta: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Novedades>>, t: Throwable) {
                    Log.e("HomeFragment", "Error en la llamada: ${t.message}")
                }
            })
        }
    }

    // Cancelar el trabajo de las coroutines cuando la actividad se destruye
    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()
}