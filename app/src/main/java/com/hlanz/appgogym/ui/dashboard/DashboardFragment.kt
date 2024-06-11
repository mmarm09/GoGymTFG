package com.hlanz.appgogym.ui.dashboard

import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.hlanz.appgogym.R
import com.hlanz.appgogym.modelos.Rutina
import com.hlanz.appgogym.modelos.Usuario
import com.hlanz.appgogym.servicios.ServicioRutina
import com.hlanz.appgogym.servicios.ServicioUsuario
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.CoroutineContext
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.File

// Implementamos CuadriculaApapter para cuando hagamos click en una sesión
class DashboardFragment : Fragment(), CoroutineScope, CuadriculaAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var textSemanas: TextView
    private lateinit var adapter: CuadriculaAdapter
    private var cuadros: MutableList<String> = mutableListOf()

    //Usar API para guardar un dato clave-valor para usarlo en otra actividad
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesIdRutina: SharedPreferences

    private val urlRutina = "http://192.168.1.135:80/xampp/api/crud/rutina/"
    private val urlUsuario = "http://192.168.1.135:80/xampp/api/crud/usuario/"
    private lateinit var job: Job

    private var usuarioActual: Usuario? = null
    private var rutinaGenerada: Rutina? = null

    private var listaRutinas: MutableList<Rutina> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        recyclerView = root.findViewById(R.id.recyclerViewCuadricula)
        textSemanas = root.findViewById(R.id.text_semanas)
        val btnPersonalizarRutina = root.findViewById<Button>(R.id.btnPersonalizarRutina)

        // Inicializa SharedPreferences para guardar el ID de la rutina
        sharedPreferencesIdRutina =
            requireContext().getSharedPreferences("GuardarIdRutina", MODE_PRIVATE)

        // Configura el RecyclerView con un GridLayoutManager y el adaptador
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        adapter = CuadriculaAdapter(requireContext(), cuadros, this)
        recyclerView.adapter = adapter

        btnPersonalizarRutina.setOnClickListener {
            showPersonalizarDialog()
        }

        val btnEliminarRutina = root.findViewById<Button>(R.id.btn_eliminar_rutina)
        btnEliminarRutina.setOnClickListener {
            eliminarRutina()
        }

        obtenerUsuarioActual()

        return root
    }

    // Método para manejar el clic en un ítem del RecyclerView
    override fun onItemClick(position: Int) {
        val sesion = cuadros[position]
        Toast.makeText(context, "Clicked: $sesion", Toast.LENGTH_SHORT).show()
        //var numeroSesion=sesion.split(" ").lastOrNull()
        // Navegar a una nueva actividad
        val intent = Intent(context, AddExerciseActivity::class.java)
        intent.putExtra("sesion", sesion)
        startActivity(intent)
    }

    private fun leerRutinas() {
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(urlRutina)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val servicioLeer = retrofit.create(ServicioRutina::class.java)
        val llamada = servicioLeer.getRutina(-1)

        llamada.enqueue(object : Callback<List<Rutina?>> {
            override fun onResponse(call: Call<List<Rutina?>>, response: Response<List<Rutina?>>) {
                if (response.isSuccessful) {
                    val rutinas = response.body()
                    rutinas?.let {
                        listaRutinas.clear()
                        it.forEach { rut ->
                            rut?.let {
                                listaRutinas.add(rut)
                                Log.e("DashboardFragment", "Rutinas: $rut")
                            }
                        }
                        obtenerUltimaRutinaCreada()
                    }
                } else {
                    Log.e("DashboardFragment", "Error en la respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Rutina?>>, t: Throwable) {
                Log.e("DashboardFragment", "Error en la llamada: ${t.message}")
            }
        })
    }

    // Método para obtener la última rutina creada
    private fun obtenerUltimaRutinaCreada() {
        if (listaRutinas.isNotEmpty()) {
            val idUltimaRutina = listaRutinas[listaRutinas.size - 1].id
            Log.d("DashboardFragment", "ID de la última rutina creada: $idUltimaRutina")
            actualizarUsuarioConRutina(idUltimaRutina)
        } else {
            Log.d("DashboardFragment", "No hay rutinas creadas aún.")
        }
    }

    private fun obtenerUsuarioActual() {
        launch(Dispatchers.IO) {
            val context = requireContext()
            sharedPreferences =
                context.getSharedPreferences("GuardarIdUsuario", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("usuario_id", -1)
            Log.e("ID USUARIO", "El id del usuario iniciado de sesion es $userId")

            if (userId == -1) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "ID de usuario no encontrado en SharedPreferences",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(urlUsuario)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            val servicioUsuario = retrofit.create(ServicioUsuario::class.java)

            try {
                val responseUsuario = servicioUsuario.getUsuario(userId).execute()
                if (responseUsuario.isSuccessful) {
                    val usuario = responseUsuario.body()?.firstOrNull()
                    usuarioActual = usuario
                    usuario?.let {
                        it.id_rutina?.let { it1 -> obtenerRutinaUsuario(it1) }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Usuario cargado correctamente", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al obtener el usuario", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error en la llamada: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // Método para obtener la rutina del usuario
    private fun obtenerRutinaUsuario(idRutina: Int) {
        if (idRutina == 0) return

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(urlRutina)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val servicioRutina = retrofit.create(ServicioRutina::class.java)

        val llamada = servicioRutina.getRutina(idRutina)
        llamada.enqueue(object : Callback<List<Rutina?>> {
            override fun onResponse(call: Call<List<Rutina?>>, response: Response<List<Rutina?>>) {
                if (response.isSuccessful) {
                    val rutina = response.body()?.firstOrNull()
                    rutina?.let {
                        rutinaGenerada = it
                        activity?.runOnUiThread {
                            textSemanas.text = "${it.duracion_semanas} semanas"
                            generarCuadros(it.num_sesiones)
                        }
                    }
                } else {
                    Log.e("DashboardFragment", "Error en la respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Rutina?>>, t: Throwable) {
                Log.e("DashboardFragment", "Error en la llamada: ${t.message}")
            }
        })
    }

    // Método para mostrar el Dialog de personalización de rutinas
    private fun showPersonalizarDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialogo_personalizar_rutina, null)
        builder.setView(dialogView)

        val etDuracion = dialogView.findViewById<EditText>(R.id.et_duracion)
        val etSesiones = dialogView.findViewById<EditText>(R.id.et_sesiones)
        val btnContinuar = dialogView.findViewById<Button>(R.id.btn_continuar)

        val alertDialog = builder.create()

        btnContinuar.setOnClickListener {
            val duracion = etDuracion.text.toString().toIntOrNull()
            val sesiones = etSesiones.text.toString().toIntOrNull()

            if (duracion != null && sesiones != null) {
                textSemanas.text = "$duracion semanas"
                generarCuadros(sesiones)

                val rutina = Rutina(
                    0,
                    duracion,
                    sesiones,
                    null
                )

                insertarRutina(rutina)
                Thread.sleep(1000)
                leerRutinas()

                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    // Generar las sesiones
    private fun generarCuadros(numCuadros: Int) {
        cuadros.clear()
        for (i in 1..numCuadros) {
            cuadros.add("Sesión $i")
        }
        adapter.notifyDataSetChanged()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private fun insertarRutina(rutina: Rutina) {
        //launch(Dispatchers.IO) {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(urlRutina)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val servicioRutina = retrofit.create(ServicioRutina::class.java)
        val llamada = servicioRutina.insertarRutina(rutina)

        //withContext(Dispatchers.Main) {
        llamada.enqueue(object : Callback<Rutina> {
            override fun onResponse(call: Call<Rutina>, response: Response<Rutina>) {
                if (response.isSuccessful) {
                    val rutinaCreada = response.body()
                    rutinaCreada?.let {
                        Log.e("DashboardFragment", "Rutina creada: ${response.code()}")
                        Toast.makeText(
                            requireContext(),
                            "Rutina creada exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Guardar el ID de la rutina en SharedPreferences
                        val editor = sharedPreferencesIdRutina.edit()
                        editor.putInt("id_rutina", it.id)
                        editor.apply()
                        leerRutinas()
                    }
                } else {
                    Log.e("DashboardFragment", "Error en la respuesta: ${response.code()}")
                    Toast.makeText(
                        requireContext(),
                        "Error en la creación de la rutina",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Rutina>, t: Throwable) {
                Log.e("DashboardFragment", "Error en la llamada: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "Error en la llamada: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        // }

        //}
    }

    // Actualizar el usuario con la rutina creada
    private fun actualizarUsuarioConRutina(idRutina: Int) {
        launch(Dispatchers.IO) {
            usuarioActual?.let { usuario ->
                Log.d(
                    "DashboardFragment",
                    "Actualizando usuario con ID: ${usuario.id} y nueva rutina ID: $idRutina"
                )

                val usuarioActualizado = Usuario(
                    usuario.id,
                    usuario.dni,
                    usuario.nombre,
                    usuario.apellidos,
                    usuario.telefono,
                    usuario.correo,
                    usuario.fecha_nacimiento,
                    usuario.contrasena,
                    usuario.foto,
                    idRutina
                )

                val gson = GsonBuilder().setLenient().create()
                val retrofit = Retrofit.Builder()
                    .baseUrl(urlUsuario)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                val servicioUsuario = retrofit.create(ServicioUsuario::class.java)

                try {
                    val response = servicioUsuario.editarUsuario(usuarioActualizado).execute()
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Rutina asignada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("DashboardFragment", "Usuario actualizado correctamente.")
                            // Guardar el ID de la rutina en SharedPreferences
                            val editor = sharedPreferencesIdRutina.edit()
                            editor.putInt("id_rutina", idRutina)
                            editor.apply()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error al actualizar el usuario",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(
                                "DashboardFragment",
                                "Error al actualizar el usuario: ${response.errorBody()?.string()}"
                            )
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error en la llamada: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("DashboardFragment", "Error en la llamada: ${e.message}")
                    }
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Usuario no cargado", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("DashboardFragment", "Usuario actual no está cargado.")
                }
            }
        }
    }

    // Eliminar la rutina actual
    private fun eliminarRutina() {
        launch(Dispatchers.IO){
            val context = requireContext()
            sharedPreferencesIdRutina = context.getSharedPreferences("GuardarIdRutina", MODE_PRIVATE)
            val idRutina = sharedPreferencesIdRutina.getInt("id_rutina",-1)
            Log.d("ID RUTINA","EL ID DE LA RUTINA ES $idRutina")

            if(idRutina == -1){
                Log.e("DashboardFragment", "ID de la rutina no encontrado en SharedPreferences")
                return@launch
            }

            val gson = GsonBuilder().setLenient().create()
                val retrofit = Retrofit.Builder()
                    .baseUrl(urlRutina)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                val servicioLeer = retrofit.create(ServicioRutina::class.java)
            val json = """{"id": $idRutina}"""
            val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

            try {
                val response = servicioLeer.borrarRutina(requestBody).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("DashboardFragment", "Rutina eliminada: ${response.code()}")

                        // Mostrar Toast de éxito
                        Toast.makeText(
                            context,
                            "Rutina eliminada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("DashboardFragment", "Error en la respuesta: ${response.code()}")
                        // Mostrar Toast de error
                        Toast.makeText(context, "Error al eliminar la rutina", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardFragment", "Error en la llamada: ${e.message}")
                withContext(Dispatchers.Main) {
                    // Mostrar Toast de error
                    Toast.makeText(context, "Error al eliminar la rutina", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}