package com.hlanz.appgogym

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.hlanz.appgogym.datePicker.DatePickerFragment
import com.hlanz.appgogym.modelos.Usuario
import com.hlanz.appgogym.servicios.ServicioUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext


class Registro : AppCompatActivity(), CoroutineScope{

    lateinit var txtDNI: EditText
    lateinit var txtNombre: EditText
    lateinit var txtApellidos: EditText
    lateinit var txtTelefono: EditText
    lateinit var txtCorreo: EditText
    lateinit var txtFechaNac: EditText
    lateinit var txtContrasena: EditText

    lateinit var btnRegistrar: Button

    val url = "http://192.168.1.135:80/xampp/api/crud/usuario/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        txtNombre =findViewById(R.id.text_peso)
        txtApellidos =findViewById(R.id.text_apellidos)
        txtDNI =findViewById(R.id.text_dni)
        txtTelefono =findViewById(R.id.text_telefono)
        txtCorreo =findViewById(R.id.text_correo)
        txtFechaNac =findViewById(R.id.text_fecha_nacim)
        txtContrasena =findViewById(R.id.text_contrasena)

        btnRegistrar = findViewById(R.id.btn_registro)
    }

    fun leerUsuarios(){
        val gson = GsonBuilder().setLenient().create()
        //Instancia a retrofit agregando la baseURL y el convertidor GSON
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val servicioLeer = retrofit.create(ServicioUsuario::class.java)
        val llamada = servicioLeer.getUsuario(-1)

        llamada.enqueue(object : Callback<List<Usuario?>> {
            override fun onResponse(call: Call<List<Usuario?>>, response: Response<List<Usuario?>>) {
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    usuarios?.forEach { usuario ->
                        Log.d("RegistroActivity", "Usuario: $usuario")
                    }
                } else {
                    Log.e("RegistroActivity", "Error en la respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Usuario?>>, t: Throwable) {
                Log.e("RegistroActivity", "Error en la llamada: ${t.message}")
            }
        })
    }

    // Método para insertar un usuario usando retrofit
    fun insertarUsuario(usuario: Usuario){
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val servicioInsertar = retrofit.create(ServicioUsuario::class.java)

        launch {
            try {
                val response = withContext(Dispatchers.IO) { servicioInsertar.insertarUsuario(usuario).execute() }
                if (response.isSuccessful) {
                    val usuarioCreado = response.body()
                    Log.e("RegistroActivity", "Usuario creado: ${response.code()}")
                    Toast.makeText(this@Registro, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Registro, Login::class.java)
                    startActivity(intent)
                } else {
                    Log.e("RegistroActivity", "Error en la respuesta: ${response.code()}")
                    Toast.makeText(this@Registro, "Error en la creación del usuario", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("RegistroActivity", "Error en la llamada: ${e.message}")
                Toast.makeText(this@Registro, "Error en la llamada: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Validar los campos del registro
    fun validarCampos(): Boolean {
        var validador= true
        // Todos los campos son obligatorios
        if (txtDNI.text.isEmpty() || txtNombre.text.isEmpty() || txtApellidos.text.isEmpty() ||
            txtTelefono.text.isEmpty() || txtCorreo.text.isEmpty() ||
            txtFechaNac.text.isEmpty() || txtContrasena.text.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
            validador = false
        }

        // Validar dni: 8 primeros caracteres son números y el último una
        // letra mayúscula
        val dniPattern = "^[0-9]{8}[A-Z]$"
        if (!Pattern.matches(dniPattern, txtDNI.text.toString())) {
            Toast.makeText(this, "El DNI debe tener 8 números y una letra mayúscula", Toast.LENGTH_LONG).show()
            validador = false
        }

        // Validar teléfono: 9 dígitos
        val telefonoPattern = "^[0-9]{9}$"
        if (!Pattern.matches(telefonoPattern, txtTelefono.text.toString())) {
            Toast.makeText(this, "El teléfono debe tener 9 números", Toast.LENGTH_LONG).show()
            validador = false
        }

        // Validar correo: cadena seguida de una @ y termina con un dominio
        val correoPattern = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$"
        if (!Pattern.matches(correoPattern, txtCorreo.text.toString())) {
            Toast.makeText(this, "El correo debe ser válido y contener un dominio", Toast.LENGTH_LONG).show()
            validador = false
        }

        //Validar contraseña: mínimos 8 caracteres
        if (txtContrasena.text.length < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_LONG).show()
            validador = false
        }

        return validador
    }

    // Método para mostrar el DatePicker
    fun clickShowDatePicker(View: View){
        val newFragment = DatePickerFragment.newInstance(DatePickerDialog.OnDateSetListener { _, year, month, day ->
            // +1 porque enero es 0 en el calendario
            val selectedDate = year.toString() + "-" + (month + 1) + "-" + day
            txtFechaNac.setText(selectedDate)
        })

        newFragment.show(supportFragmentManager, "datePicker")
    }

    // Método onclick para insertar usuario
    fun clickBtnCrearUsuario(View: View){
        if (!validarCampos()) return
        val usuario = Usuario(
            0,
            txtDNI.text.toString().uppercase(),
            txtNombre.text.toString(),
            txtApellidos.text.toString(),
            txtTelefono.text.toString().toInt(),
            txtCorreo.text.toString().lowercase(),
            txtFechaNac.text.toString(),
            txtContrasena.text.toString(),
            " ",
            1
        )
        insertarUsuario(usuario)
    }

    // Cancelar el trabajo de las coroutines cuando la actividad se destruye
    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()
}

