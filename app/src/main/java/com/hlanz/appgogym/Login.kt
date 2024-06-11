package com.hlanz.appgogym

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.hlanz.appgogym.modelos.Usuario
import com.hlanz.appgogym.servicios.ServicioUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.CoroutineContext

class Login : AppCompatActivity(), CoroutineScope {

    //private lateinit var iniciar_sesion:Button
    lateinit var txtCorreo: EditText
    lateinit var txtContrasena: EditText
    //Usar API para guardar un dato clave-valor para usarlo en otra actividad
    private lateinit var sharedPreferences: SharedPreferences

    val url = "http://192.168.1.135:80/xampp/api/crud/usuario/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        txtCorreo =findViewById(R.id.inicio_correo)
        txtContrasena =findViewById(R.id.inicio_contrasena)
        sharedPreferences = getSharedPreferences("GuardarIdUsuario", MODE_PRIVATE)

        val inicio= findViewById<Button>(R.id.iniciar_sesion)
        inicio.setOnClickListener {
            obtenerUsuarios()
        }

        val registro= findViewById<Button>(R.id.crear_cuenta)
        registro.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }
    }

    // Método para obtener los usuarios de la base de datos
    private fun obtenerUsuarios() {
        val gson = GsonBuilder().setLenient().create()

        // Instancia a retrofit agregando la baseURL y el convertidor GSON
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val servicioLeer = retrofit.create(ServicioUsuario::class.java)

        launch {
            try {
                val response = withContext(Dispatchers.IO) { servicioLeer.getUsuario(-1).execute() }
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    // Filtra los elementos no nulos y los pasa a la función procesarUsuarios()
                    usuarios?.let {
                        procesarUsuarios(it.filterNotNull())
                    }
                } else {
                    Log.e("LoginActivity", "Error en la respuesta: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error en la llamada: ${e.message}")
            }
        }
    }

    // Método para procesar los usuarios para el inicio de sesión
    private fun procesarUsuarios(usuarios: List<Usuario>) {
        // Verificar si el correo y la contraseña coinciden con algún usuario de la lista
        val usuario = usuarios.find { it.correo.equals(txtCorreo.text.toString()) && it.contrasena.equals(txtContrasena.text.toString()) }

        if (usuario != null) {
            // Si se encuentra el usuario, mostrar mensaje de bienvenida
            Toast.makeText(this, "¡Bienvenid@ ${usuario.nombre}!", Toast.LENGTH_SHORT).show()

            // Guardar el ID del usuario en SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putInt("usuario_id", usuario.id)
            editor.putString("usuario_foto",usuario.foto)
            usuario.id_rutina?.let { editor.putInt("usuario_idRutina", it) }
            editor.apply()

            // Iniciar sesión con el usuario encontrado
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // Si no se encuentra el usuario, mostrar mensaje de error
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
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