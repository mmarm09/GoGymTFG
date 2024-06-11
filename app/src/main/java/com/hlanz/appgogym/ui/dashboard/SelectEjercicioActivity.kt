package com.hlanz.appgogym.ui.dashboard

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.hlanz.appgogym.R
import com.hlanz.appgogym.adaptadores.EjercicioAdapter
import com.hlanz.appgogym.modelos.Ejercicio
import com.hlanz.appgogym.servicios.ServicioEjercicio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.CoroutineContext

class SelectEjercicioActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EjercicioAdapter
    private var ejercicios: MutableList<Ejercicio> = mutableListOf()

    private val url = "http://192.168.1.135:80/xampp/api/crud/ejercicio/"

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ejercicio)

        job = Job()

        // Configuración del RecyclerView
        recyclerView = findViewById(R.id.recyclerViewEjercicios)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EjercicioAdapter(ejercicios) { ejercicio -> onEjercicioSelected(ejercicio) }
        recyclerView.adapter = adapter

        // Obtener los parámetros del intent
        val tipo = intent.getStringExtra("tipo")
        val grupoMuscular = intent.getStringExtra("grupo_muscular")
        val complemento = intent.getStringExtra("complemento")

        // Cargar los ejercicios
        cargarEjercicios(tipo, grupoMuscular, complemento)
    }

    // Método para cargar los ejercicios según el tipo,
    // el grupo muscular y el complemento
    private fun cargarEjercicios(tipo: String?, grupoMuscular: String?, complemento: String?) {
        launch {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val servicioEjercicio = retrofit.create(ServicioEjercicio::class.java)
            val llamada = servicioEjercicio.getEjercicios()

            llamada.enqueue(object : Callback<List<Ejercicio>> {
                override fun onResponse(call: Call<List<Ejercicio>>, response: Response<List<Ejercicio>>) {
                    if (response.isSuccessful) {
                        val todosEjercicios = response.body() ?: emptyList()
                        Log.d("SelectEjercicioActivity", "Ejercicios recibidos: $todosEjercicios")
                        // Filtrar los ejercicios por tipo, grupo muscular y complemento
                        ejercicios.clear()
                        ejercicios.addAll(todosEjercicios.filter {
                            it.tipo == tipo &&
                                    (grupoMuscular == null || it.grupo_muscular == grupoMuscular) &&
                                    (complemento == null || it.complemento == complemento)
                        })
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.e("SelectEjercicioActivity", "Error en la respuesta: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Ejercicio>>, t: Throwable) {
                    Log.e("SelectEjercicioActivity", "Error en la llamada: ${t.message}")
                }
            })
        }
    }

    private fun onEjercicioSelected(ejercicio: Ejercicio) {
        // Lógica para añadir el ejercicio a la rutina
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
