package com.hlanz.appgogym.ui.dashboard

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.hlanz.appgogym.R
import com.hlanz.appgogym.adaptadores.EjercicioAdapter
import com.hlanz.appgogym.modelos.Ejercicio
import com.hlanz.appgogym.modelos.RutinaEjercicio
import com.hlanz.appgogym.modelos.Usuario
import com.hlanz.appgogym.servicios.ServicioEjercicio
import com.hlanz.appgogym.servicios.ServicioRutinaEjercicio
import com.hlanz.appgogym.servicios.ServicioUsuario
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

class AddExerciseActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EjercicioAdapter
    private var ejercicios: MutableList<Ejercicio> = mutableListOf()
    private var rutinaEjercicios: List<RutinaEjercicio> = emptyList()
    // Variable para el modo de edición
    private var deleteMode = false
    private lateinit var sharedPreferencesIdRutina: SharedPreferences

    private val urlRutinaEjercicio = "http://192.168.1.135:80/xampp/api/crud/rutina_ejercicio/"
    private val urlEjercicio = "http://192.168.1.135:80/xampp/api/crud/ejercicio/"

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exercise)

        job = Job()
        recyclerView = findViewById(R.id.recyclerViewEjercicios)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EjercicioAdapter(ejercicios) { ejercicio -> ejercicioSeleccionado(ejercicio) }
        recyclerView.adapter = adapter

        //val idRutina = intent.getIntExtra("id_rutina", 0)
        sharedPreferencesIdRutina = getSharedPreferences("GuardarIdRutina", MODE_PRIVATE)
        val idRutina = sharedPreferencesIdRutina.getInt("id_rutina", -1)
        Log.d("ID RUTINA", "EL ID DE LA RUTINA ES $idRutina")

        if (idRutina == -1) {
            Log.e("ProfileFragment", "ID de rutina no encontrado en SharedPreferencesIdRutina")
            return
        }
        val idEjercicio = intent.getIntExtra("id_ejercicio", 0)

        //cargarEjerciciosPorRutina(idRutina, idEjercicio)
        obtenerDatos(idRutina)

        // Activar/desactivar el modo de eliminación
        findViewById<Button>(R.id.btn_borrarEjercicio).setOnClickListener {
            modoBorrado()
        }

        findViewById<Button>(R.id.btn_anadirEjercicio).setOnClickListener {
            val intent = Intent(this, SelectTipoActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_volverRutina).setOnClickListener {
            val intent = Intent(this, DashboardFragment::class.java)
            startActivity(intent)
        }
    }

    // Método para activar/desactivar el modo de borrado
    private fun modoBorrado() {
        // Cambiar el estado del modo de eliminación
        deleteMode = !deleteMode

        val deleteButton = findViewById<Button>(R.id.btn_borrarEjercicio)
        if (deleteMode) {
            deleteButton.text = "Borrar ejercicio: Activado"
        } else {
            deleteButton.text = "Borrar ejercicio: Desactivado"
        }

        // Notificar al adaptador para actualizar la visualización de los elementos
        adapter.setDeleteMode(deleteMode)
    }

    // Obtener datos de los ejercicios y las rutinas
    private fun obtenerDatos(idRutina: Int) {
        launch {
            obtenerRutinasEjercicios(idRutina)
        }
    }

    // Obtener la lista de ejercicios de una rutina específica
    private fun obtenerRutinasEjercicios(idRutina: Int) {
        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl(urlRutinaEjercicio)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val servicioRutinaEjercicio = retrofit.create(ServicioRutinaEjercicio::class.java)
        val llamadaRutinaEjercicio = servicioRutinaEjercicio.getRutinaEjercicioLista(idRutina, -1)

        llamadaRutinaEjercicio.enqueue(object : Callback<List<RutinaEjercicio>> {
            override fun onResponse(
                call: Call<List<RutinaEjercicio>>,
                response: Response<List<RutinaEjercicio>>
            ) {
                if (response.isSuccessful) {
                    rutinaEjercicios = response.body() ?: emptyList()
                    obtenerEjercicios()
                    Log.e("AddExerciseActivity", "Respuesta: ${response.code()}")
                } else {
                    Log.e("AddExerciseActivity", "Error en la respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<RutinaEjercicio>>, t: Throwable) {
                Log.e("AddExerciseActivity", "Error en la llamada: ${t.message}")
            }
        })
    }

    // Obtener todos los ejercicios
    private fun obtenerEjercicios() {
        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl(urlEjercicio)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val servicioEjercicio = retrofit.create(ServicioEjercicio::class.java)
        val llamada = servicioEjercicio.getEjerciciosTodos(-1)

        llamada.enqueue(object : Callback<List<Ejercicio>> {
            override fun onResponse(
                call: Call<List<Ejercicio>>,
                response: Response<List<Ejercicio>>
            ) {
                if (response.isSuccessful) {
                    val todosEjercicios = response.body() ?: emptyList()
                    ejercicios.clear()
                    ejercicios.addAll(todosEjercicios.filter { ejercicio ->
                        rutinaEjercicios.any { rutinaEjercicio ->
                            rutinaEjercicio.id_ejercicio == ejercicio.id
                        }
                    })
                    adapter.notifyDataSetChanged()
                    Log.e("AddExerciseActivity", "Respuesta: ${response.code()}")
                } else {
                    Log.e("AddExerciseActivity", "Error en la respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Ejercicio>>, t: Throwable) {
                Log.e("AddExerciseActivity", "Error en la llamada: ${t.message}")
            }
        })
    }

    // Método para manejar la selección de un ejercicio
    private fun ejercicioSeleccionado(ejercicio: Ejercicio) {
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
