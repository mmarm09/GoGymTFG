package com.hlanz.appgogym.ui.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import com.hlanz.appgogym.R
import com.hlanz.appgogym.adaptadores.SecuenciasAdapter
import com.hlanz.appgogym.modelos.Ejercicio
import com.hlanz.appgogym.modelos.RutinaEjercicio
import com.hlanz.appgogym.modelos.Secuencia
import com.hlanz.appgogym.servicios.ServicioRutinaEjercicio
import com.hlanz.appgogym.servicios.ServicioSecuencia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.CoroutineContext

class DetalleEjercicio : AppCompatActivity(), CoroutineScope {

    private lateinit var exerciseImageView: ImageView
    private lateinit var exerciseNameTextView: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SecuenciasAdapter
    private val secuencias: MutableList<Secuencia> = mutableListOf()
    private var sesion: Int = 1
    private lateinit var ejercicio: Ejercicio
    private var idRutina: Int = 0
    private var tipoEjercicio: String = ""
    private var maxSeries: Int = 8

    private val url = "http://192.168.1.135:80/xampp/api/crud"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_ejercicio)

        // Recuperar el ID de la rutina de SharedPreferences
        val sharedPreferences = getSharedPreferences("GuardarIdRutina", MODE_PRIVATE)
        idRutina = sharedPreferences.getInt("id_rutina", 0)

        // Obtener datos del Intent
        ejercicio = intent.getSerializableExtra("ejercicio") as Ejercicio
        sesion = intent.getIntExtra("sesion", 1)
        tipoEjercicio = ejercicio.tipo
        maxSeries = if (tipoEjercicio == "CARDIO") 1 else 8

        exerciseNameTextView = findViewById(R.id.exerciseNameTextView)
        exerciseImageView = findViewById(R.id.exerciseImageView)

        exerciseNameTextView.text = ejercicio.nombre

        // Cargar imagen del ejercicio
        ejercicio.foto_detalle?.let {
            val imageUrl = "$url/ejercicio/$it"
            Glide.with(this)
                .load(imageUrl)
                .fitCenter()
                .into(exerciseImageView)
        }

        // Configurar el RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSeries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SecuenciasAdapter(secuencias, { secuencia, position -> onSecuenciaSelected(secuencia, position) }, tipoEjercicio)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.addSeriesButton).setOnClickListener {
            addNewSecuencia()
        }

        // Crear objeto de RutinaEjercicio para agregar el ejercicio a la rutina
        val ejercicioRutina=RutinaEjercicio(
            idRutina,
            ejercicio.id,
            sesion
        )

        findViewById<Button>(R.id.addEjercicioRutina).setOnClickListener {
            insertarEjercicioRutina(ejercicioRutina)
        }

        //cargarSecuencias()
    }

    private fun cargarSecuencias() {
        launch {
            withContext(Dispatchers.IO) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                val httpClient = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()

                val gson = GsonBuilder().setLenient().create()
                val retrofit = Retrofit.Builder()
                    .baseUrl("$url/secuencia/")
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

                val servicioSecuencia = retrofit.create(ServicioSecuencia::class.java)
                val llamada = servicioSecuencia.obtenerSecuenciasPorEjercicio(ejercicio.id)

                llamada.enqueue(object : Callback<List<Secuencia>> {
                    override fun onResponse(call: Call<List<Secuencia>>, response: Response<List<Secuencia>>) {
                        if (response.isSuccessful) {
                            secuencias.clear()
                            response.body()?.let { secuencias.addAll(it) }
                            launch(Dispatchers.Main) {
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            launch(Dispatchers.Main) {
                                Toast.makeText(this@DetalleEjercicio, "Error al cargar las secuencias", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<List<Secuencia>>, t: Throwable) {
                        launch(Dispatchers.Main) {
                            Toast.makeText(this@DetalleEjercicio, "Error en la llamada: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }
    }

    // Método para agregar una nueva secuencia
    private fun addNewSecuencia() {
        // Agregar una nueva secuencia si no se ha alcanzado el límite
        if (secuencias.size < maxSeries) {
            val nuevaSecuencia = Secuencia(
                0,
                if (tipoEjercicio == "CARDIO") 1 else secuencias.size + 1,
                if (tipoEjercicio == "CARDIO") 0 else 12,
                if (tipoEjercicio == "CARDIO") 20 else 0,
                ejercicio.id
            )
            secuencias.add(nuevaSecuencia)
            adapter.notifyItemInserted(secuencias.size - 1)

            /*val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl("$url/secuencia/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val servicioSecuencia = retrofit.create(ServicioSecuencia::class.java)
            val llamada = servicioSecuencia.agregarSecuencia(nuevaSecuencia)

            // Ejecutar en un hilo secundario utilizando coroutines
            launch {
                withContext(Dispatchers.IO) {
                    llamada.enqueue(object : Callback<Secuencia> {
                        override fun onResponse(call: Call<Secuencia>, response: Response<Secuencia>) {
                            if (response.isSuccessful) {
                                response.body()?.let {
                                    secuencias.add(it)
                                    runOnUiThread {
                                        adapter.notifyItemInserted(secuencias.size - 1)
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@DetalleEjercicio, "Error al añadir la secuencia", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<Secuencia>, t: Throwable) {
                            runOnUiThread {
                                Toast.makeText(this@DetalleEjercicio, "Error en la llamada: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
            }*/
        } else {
            Toast.makeText(this, "Se ha alcanzado el límite de series para este ejercicio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSecuenciaSelected(secuencia: Secuencia, position: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_secuencia, null)
        val numberPickerRepeticiones = dialogView.findViewById<NumberPicker>(R.id.numberPickerRepeticiones)

        // Configuracion del NumberPicker para las repeticiones de las series
        numberPickerRepeticiones.minValue = 1
        numberPickerRepeticiones.maxValue = 100
        numberPickerRepeticiones.value = secuencia.repeticiones

        // Crear y mostrar el diálogo
        AlertDialog.Builder(this)
            .setTitle("Editar Serie")
            .setView(dialogView)
            .setPositiveButton("Aceptar") { _, _ ->
                // Actualizar el valor de repeticiones en la secuencia
                secuencia.repeticiones = numberPickerRepeticiones.value
                // Notificar al adaptador que los datos han cambiado en la posición especificada
                adapter.notifyItemChanged(position)

                /*val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                val httpClient = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()

                val gson = GsonBuilder().setLenient().create()
                val retrofit = Retrofit.Builder()
                    .baseUrl("$url/secuencia/")
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

                val servicioSecuencia = retrofit.create(ServicioSecuencia::class.java)
                val llamada = servicioSecuencia.actualizarSecuencia(secuencia.id, secuencia)

                // Ejecutar en un hilo secundario utilizando coroutines
                launch {
                    withContext(Dispatchers.IO) {
                        llamada.enqueue(object : Callback<Secuencia> {
                            override fun onResponse(call: Call<Secuencia>, response: Response<Secuencia>) {
                                if (response.isSuccessful) {
                                    runOnUiThread {
                                        adapter.notifyItemChanged(position)
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@DetalleEjercicio, "Error al actualizar la secuencia", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Secuencia>, t: Throwable) {
                                runOnUiThread {
                                    Toast.makeText(this@DetalleEjercicio, "Error en la llamada: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                }*/
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Eliminar") { _, _ ->
                // Eliminar la secuencia de la lista y notificar al adaptador
                secuencias.removeAt(position)
                adapter.notifyItemRemoved(position)
                /*val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                val httpClient = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()

                val gson = GsonBuilder().setLenient().create()
                val retrofit = Retrofit.Builder()
                    .baseUrl("$url/secuencia/")
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

                val servicioSecuencia = retrofit.create(ServicioSecuencia::class.java)
                val llamada = servicioSecuencia.eliminarSecuencia(secuencia.id)

                // Ejecutar en un hilo secundario utilizando coroutines
                launch {
                    withContext(Dispatchers.IO) {
                        llamada.enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    runOnUiThread {
                                        secuencias.removeAt(position)
                                        adapter.notifyItemRemoved(position)
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@DetalleEjercicio, "Error al eliminar la secuencia", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                runOnUiThread {
                                    Toast.makeText(this@DetalleEjercicio, "Error en la llamada: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                }*/
            }
            .show()
    }

    // Método para insertar un ejercicio en la rutina
    private fun insertarEjercicioRutina(rutiEjer: RutinaEjercicio) {
        val idEjercicio = ejercicio.id
        val dia = sesion

        Log.d("SESION","SESION: $dia")

        Log.d("DetalleEjercicio", "idRutina: $idRutina, idEjercicio: $idEjercicio, dia: $dia")

        if (idRutina == 0) {
            Toast.makeText(this, "ID de rutina no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("$url/rutina_ejercicio/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val servicioRutinaEjercicio = retrofit.create(ServicioRutinaEjercicio::class.java)
        val llamada = servicioRutinaEjercicio.insertarRutinaEjercicio(rutiEjer)

        // Ejecutar en un hilo secundario utilizando coroutines
        launch {
            withContext(Dispatchers.IO) {
                llamada.enqueue(object : Callback<RutinaEjercicio> {
                    override fun onResponse(call: Call<RutinaEjercicio>, response: Response<RutinaEjercicio>) {
                        if (response.isSuccessful) {
                            val rutinaEjercicioCreado = response.body()
                            runOnUiThread {
                                Toast.makeText(this@DetalleEjercicio, "Ejercicio añadido correctamente", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@DetalleEjercicio, AddExerciseActivity::class.java)
                                intent.putExtra("sesion", dia)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Log.e("DetalleEjercicio", "Error en la respuesta: ${response.code()}")
                            Log.e("DetalleEjercicio", "Error en la respuesta: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<RutinaEjercicio>, t: Throwable) {
                        Log.e("DetalleEjercicio", "Error en la llamada: ${t.message}")
                    }
                })
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}
