package com.hlanz.appgogym.ui.profile

import android.app.DatePickerDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.hlanz.appgogym.Login
import com.hlanz.appgogym.R
import com.hlanz.appgogym.datePicker.DatePickerFragment
import com.hlanz.appgogym.modelos.Usuario
import com.hlanz.appgogym.servicios.ServicioUsuario
import com.hlanz.appgogym.ui.home.HomeFragment
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext

class ProfileFragment : Fragment(), CoroutineScope {

    private lateinit var btnEdicion: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnEliminarCuenta: Button

    private lateinit var fotoUsuario: ImageView

    private lateinit var textNombre: EditText
    private lateinit var textApellidos: EditText
    private lateinit var textDNI: EditText
    private lateinit var textTelefono: EditText
    private lateinit var textFechaNacimiento: EditText
    private lateinit var textCorreo: EditText
    private lateinit var textContrasena: EditText

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesIdRutina: SharedPreferences

    private var modoEdicion = false

    private var idRutina: Int = 0

    val url = "http://192.168.1.135:80/xampp/api/crud/usuario/"

    // Manejo coroutinas
    private lateinit var job: Job

    // Foto perfil
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        btnEdicion = rootView.findViewById(R.id.btnEditarPerfil)
        btnCerrarSesion = rootView.findViewById(R.id.btnCerrarSesion)
        btnEliminarCuenta = rootView.findViewById(R.id.btnEliminarCuenta)
        fotoUsuario = rootView.findViewById(R.id.fotoUsuario)

        textNombre = rootView.findViewById(R.id.text_editNombre)
        textApellidos = rootView.findViewById(R.id.text_editApellidos)
        textDNI = rootView.findViewById(R.id.text_editDni)
        textTelefono = rootView.findViewById(R.id.text_editTelefono)
        textFechaNacimiento = rootView.findViewById(R.id.text_editFechaNacimiento)
        textCorreo = rootView.findViewById(R.id.text_editCorreo)
        textContrasena = rootView.findViewById(R.id.text_editContrasena)

        // Recuperar el ID de la rutina de SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("GuardarIdRutina", MODE_PRIVATE)
        idRutina = sharedPreferences.getInt("id_rutina", 0)

        btnEliminarCuenta.setOnClickListener {
            eliminarUsuario()
            val intent = Intent(requireActivity(), Login::class.java)
            startActivity(intent)
        }

        // Configurar el click listener para mostrar el DatePicker
        textFechaNacimiento.setOnClickListener { clickShowDatePicker(it) }

        btnEdicion.setOnClickListener {
            alternarModoEdicion()
        }

        btnCerrarSesion.setOnClickListener {
            val intent = Intent(requireActivity(), Login::class.java)
            startActivity(intent)
        }

        fotoUsuario.setOnClickListener {
            if (modoEdicion) {
                openGallery()
            }
        }

        obtenerUsuario()

        return rootView
    }

    // Guardar el estado de la instancia
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("nombre", textNombre.text.toString())
        outState.putString("apellidos", textApellidos.text.toString())
        outState.putString("dni", textDNI.text.toString())
        outState.putString("telefono", textTelefono.text.toString())
        outState.putString("fechaNacimiento", textFechaNacimiento.text.toString())
        outState.putString("correo", textCorreo.text.toString())
        outState.putString("contrasena", textContrasena.text.toString())
        outState.putParcelable("selectedImageUri", selectedImageUri)
    }

    // Restaurar el estado de la instancia
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            textNombre.setText(it.getString("nombre"))
            textApellidos.setText(it.getString("apellidos"))
            textDNI.setText(it.getString("dni"))
            textTelefono.setText(it.getString("telefono"))
            textFechaNacimiento.setText(it.getString("fechaNacimiento"))
            textCorreo.setText(it.getString("correo"))
            textContrasena.setText(it.getString("contrasena"))
            selectedImageUri = it.getParcelable("selectedImageUri")
            selectedImageUri?.let { uri -> fotoUsuario.setImageURI(uri) }
        }
    }

    // Abrir la galería para seleccionar una imagen
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Manejar el resultado de la selección de imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            fotoUsuario.setImageURI(selectedImageUri)

            // Guardar la imagen en el almacenamiento interno con un nombre único con el ID del usuario
            val userId = sharedPreferences.getInt("usuario_id", -1)
            if (userId != -1) {
                val bitmap = (fotoUsuario.drawable as BitmapDrawable).bitmap
                val fileName = "profile_picture_$userId.png"
                val file = File(requireContext().filesDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                // Guardar la ruta del archivo en SharedPreferences
                sharedPreferences.edit().putString("foto_perfil_path_$userId", file.absolutePath).apply()
            }
        }
    }

    private fun alternarModoEdicion() {
        if (modoEdicion) {
            // Guardar los datos y salir del modo de edición
            guardarDatosUsuario()
            deshabilitarCampos()
            Thread.sleep(1000)
            btnEdicion.text = "Editar Perfil"
        } else {
            // Entrar en el modo de edición
            habilitarCampos()
            btnEdicion.text = "Guardar Datos"
        }
        modoEdicion = !modoEdicion
    }

    private fun habilitarCampos() {
        textNombre.isEnabled = true
        textApellidos.isEnabled = true
        textDNI.isEnabled = true
        textTelefono.isEnabled = true
        textFechaNacimiento.isEnabled = true
        textCorreo.isEnabled = true
        textContrasena.isEnabled = true
        fotoUsuario.isEnabled = true
    }

    private fun deshabilitarCampos() {
        textNombre.isEnabled = false
        textApellidos.isEnabled = false
        textDNI.isEnabled = false
        textTelefono.isEnabled = false
        textFechaNacimiento.isEnabled = false
        textCorreo.isEnabled = false
        textContrasena.isEnabled = false
        fotoUsuario.isEnabled = false
    }

    // Mostrar el DatePicker al seleccionar el EditText de la fecha
    private fun clickShowDatePicker(view: View) {
        val newFragment =
            DatePickerFragment.newInstance(DatePickerDialog.OnDateSetListener { _, year, month, day ->
                // +1 porque enero es 0 en el calendario
                val selectedDate = "$year-${month + 1}-$day"
                textFechaNacimiento.setText(selectedDate)
            })

        newFragment.show(parentFragmentManager, "datePicker")
    }

    // Obtener los datos del usuario con el que se ha iniciado sesión
    private fun obtenerUsuario() {
        launch(Dispatchers.IO) {
            val context = requireContext()
            sharedPreferences = context.getSharedPreferences("GuardarIdUsuario", MODE_PRIVATE)
            val userId = sharedPreferences.getInt("usuario_id", -1)
            Log.d("ID USUARIO", "EL ID DEL USUARIO ES $userId")

            if (userId == -1) {
                Log.e("ProfileFragment", "ID de usuario no encontrado en SharedPreferences")
                return@launch
            }

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            val servicioLeer = retrofit.create(ServicioUsuario::class.java)

            try {
                val response = servicioLeer.getUsuario(userId).execute()
                if (response.isSuccessful) {
                    val usuario = response.body()?.firstOrNull()
                    withContext(Dispatchers.Main) {
                        usuario?.let {
                            llenarDatosUsuario(it)
                        } ?: run {
                            Log.e("ProfileFragment", "Usuario no encontrado")
                        }
                    }
                } else {
                    Log.e("ProfileFragment", "Error en la respuesta: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error en la llamada: ${e.message}")
            }
        }
    }

    // Rellenar los EditText con los datos del usuario
    private fun llenarDatosUsuario(usuario: Usuario) {
        textNombre.setText(usuario.nombre ?: "")
        textApellidos.setText(usuario.apellidos ?: "")
        textDNI.setText(usuario.dni ?: "")
        textTelefono.setText(usuario.telefono?.toString() ?: "")
        textFechaNacimiento.setText(usuario.fecha_nacimiento ?: "")
        textCorreo.setText(usuario.correo ?: "")
        textContrasena.setText(usuario.contrasena ?: "")

        // Obtener la ruta del archivo de la imagen desde SharedPreferences usando el ID del usuario y cargarla
        val userId = usuario.id
        val fotoPath = sharedPreferences.getString("foto_perfil_path_$userId", null)
        if (fotoPath != null) {
            val imgFile = File(fotoPath)
            if (imgFile.exists()) {
                val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                fotoUsuario.setImageBitmap(myBitmap)
            }
        }
    }

    // Guardar los datos actualizados del usuario
    private fun guardarDatosUsuario() {
        launch(Dispatchers.IO) {
            val context = requireContext()
            sharedPreferences = context.getSharedPreferences("GuardarIdUsuario", MODE_PRIVATE)
            val userId = sharedPreferences.getInt("usuario_id", -1)
            Log.d("ID USUARIO", "EL ID DEL USUARIO ES $userId")

            if (userId == -1) {
                Log.e("ProfileFragment", "ID de usuario no encontrado en SharedPreferences")
                return@launch
            }

            sharedPreferencesIdRutina = context.getSharedPreferences("GuardarIdRutina", MODE_PRIVATE)
            val idRutina = sharedPreferencesIdRutina.getInt("id_rutina", -1)
            Log.d("ID RUTINA", "EL ID DE LA RUTINA ES $idRutina")

            if (idRutina == -1) {
                Log.e("ProfileFragment", "ID de rutina no encontrado en SharedPreferencesIdRutina")
                return@launch
            }

            val usuarioActualizado = Usuario(
                userId,
                textDNI.text.toString(),
                textNombre.text.toString(),
                textApellidos.text.toString(),
                textTelefono.text.toString().toInt(),
                textCorreo.text.toString(),
                textFechaNacimiento.text.toString(),
                textContrasena.text.toString(),
                selectedImageUri?.toString(),
                idRutina
            )

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            val servicioActualizar = retrofit.create(ServicioUsuario::class.java)

            try {
                val response = servicioActualizar.editarUsuario(usuarioActualizado).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val usuario = response.body()
                        usuario?.let {
                            llenarDatosUsuario(it)
                            Toast.makeText(
                                context,
                                "Datos guardados correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        } ?: run {
                            Log.e("ProfileFragment", "Error al guardar los datos")
                        }
                    } else {
                        Log.e("ProfileFragment", "Error en la respuesta: ${response.code()}")
                        /*Toast.makeText(context, "Error al guardar los datos", Toast.LENGTH_SHORT)
                            .show()*/
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error en la llamada: ${e.message}")
                /*withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                }*/
            }
        }
    }

    // Eliminar usuario de la base de datos
    private fun eliminarUsuario() {
        launch(Dispatchers.IO) {
            val context = requireContext()
            sharedPreferences = context.getSharedPreferences("GuardarIdUsuario", MODE_PRIVATE)
            val userId = sharedPreferences.getInt("usuario_id", -1)
            Log.d("ID USUARIO", "EL ID DEL USUARIO ES $userId")

            if (userId == -1) {
                Log.e("ProfileFragment", "ID de usuario no encontrado en SharedPreferences")
                return@launch
            }

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            val servicioLeer = retrofit.create(ServicioUsuario::class.java)
            val json = """{"id": $userId}"""
            val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

            try {
                val response = servicioLeer.borrarUsuario(requestBody).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("ProfileFragment", "Usuario eliminado: ${response.code()}")

                        // Eliminar la imagen guardada en el almacenamiento interno
                        val fotoPath = sharedPreferences.getString("foto_perfil_path_$userId", null)
                        fotoPath?.let {
                            val imgFile = File(it)
                            if (imgFile.exists()) {
                                imgFile.delete()
                            }
                        }

                        Toast.makeText(
                            context,
                            "Usuario eliminado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("ProfileFragment", "Error en la respuesta: ${response.code()}")
                        Toast.makeText(context, "Error al eliminar el usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error en la llamada: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al eliminar el usuario", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
}
