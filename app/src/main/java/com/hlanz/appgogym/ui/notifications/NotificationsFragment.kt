package com.hlanz.appgogym.ui.notifications

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormatSymbols
import java.util.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.echo.holographlibrary.Bar
import com.echo.holographlibrary.BarGraph
import com.google.gson.GsonBuilder
import com.hlanz.appgogym.R
import com.hlanz.appgogym.modelos.Progreso
import com.hlanz.appgogym.servicios.ServicioProgreso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class NotificationsFragment : Fragment(), CoroutineScope {

    lateinit var btnCalcular: Button
    lateinit var peso: EditText
    lateinit var altura: EditText
    lateinit var grafica: BarGraph
    private val puntos = ArrayList<Bar>()
    private lateinit var servicioProgreso: ServicioProgreso

    private lateinit var sharedPreferences: SharedPreferences

    val url = "http://192.168.1.135:80/xampp/api/crud/progreso/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_notifications, container, false)

        btnCalcular = rootView.findViewById(R.id.btn_calcular)
        peso = rootView.findViewById(R.id.text_peso)
        altura = rootView.findViewById(R.id.text_altura)
        grafica = rootView.findViewById(R.id.graphBar)

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        servicioProgreso = retrofit.create(ServicioProgreso::class.java)

        // Leer progresos existentes y mostrarlos en la gráfica
        leerProgresos()

        btnCalcular.setOnClickListener {
            calcularYGuardarProgreso()
        }

        return rootView
    }

    private fun leerProgresos() {
        launch(Dispatchers.IO) {
            try {
                val respuesta = servicioProgreso.getProgreso(-1).execute()

                if (respuesta.isSuccessful) {
                    val progresos = respuesta.body()
                    if (progresos != null) {
                        val context = requireContext()
                        sharedPreferences = context.getSharedPreferences("GuardarIdUsuario", Context.MODE_PRIVATE)
                        val userId = sharedPreferences.getInt("usuario_id", -1)

                        // Filtrar progresos para incluir solo los del usuario actual
                        val progresosUsuario = progresos.filter { it.id_usuario == userId }

                        for (progreso in progresosUsuario) {
                            if (progreso != null) {
                                agregarBarra(progreso)
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        grafica.bars = puntos
                        // Actualizar la gráfica
                        grafica.invalidate()
                    }
                } else {
                    //withContext(Dispatchers.Main) {
                        Log.e("NotificationsFragment", "Error en la respuesta: ${respuesta.code()}")
                        //Toast.makeText(context, "Error al leer los progresos", Toast.LENGTH_SHORT).show()
                    //}
                }
            } catch (e: Exception) {
                Log.e("NotificationsFragment", "Error al leer los progresos", e)
                /*withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al leer los progresos", Toast.LENGTH_SHORT).show()
                }*/
            }
        }
    }

    // Método para calcular el IMC y guardar el progreso
    private fun calcularYGuardarProgreso() {
        val pesoValue = peso.text.toString().toDoubleOrNull()
        val alturaValue = altura.text.toString().toIntOrNull()

        if (pesoValue != null && alturaValue != null && alturaValue > 0) {
            val alturaEnMetros = alturaValue / 100.0
            val resultado = pesoValue / (alturaEnMetros * alturaEnMetros)

            // Formatear el IMC a un decimal utilizando DecimalFormat y los símbolos apropiados
            val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                decimalSeparator = '.'
            }
            val decimalFormat = DecimalFormat("#.0", symbols)
            val imcRedondeado = decimalFormat.format(resultado).toDouble()

            // Obtener la fecha actual
            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val context = requireContext()
            sharedPreferences = context.getSharedPreferences("GuardarIdUsuario",
                Context.MODE_PRIVATE
            )
            val userId = sharedPreferences.getInt("usuario_id", -1)
            Log.d("ID USUARIO", "EL ID DEL USUARIO ES $userId")

            val progreso = Progreso(0, pesoValue, alturaValue, imcRedondeado, fechaActual, userId) // Ajusta el id_usuario según corresponda

            // Insertar progreso en la base de datos
            insertarProgreso(progreso)

            // Limpiar campos de texto
            peso.text.clear()
            altura.text.clear()
        } else {
            Toast.makeText(context, "Error: Ingresa un valor válido para peso y altura", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertarProgreso(progreso: Progreso) {
        launch(Dispatchers.IO) {
            try {
                val respuesta = servicioProgreso.insertarProgreso(progreso).execute()

                if (respuesta.isSuccessful) {
                    // Agregar el nuevo progreso a la gráfica
                    withContext(Dispatchers.Main) {
                        agregarBarra(progreso)
                        grafica.bars = puntos
                        // Actualizar la gráfica
                        grafica.invalidate()
                        Toast.makeText(context, "Progreso guardado correctamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al guardar el progreso", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationsFragment", "Error al insertar el progreso", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al guardar el progreso", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun agregarBarra(progreso: Progreso) {
        val color = getColorFromIMC(progreso.imc)
        val barra = Bar().apply {
            this.color = Color.parseColor(color)
            // Mostrar la fecha en la parte inferior de la barra
            name = progreso.fecha
            value = progreso.imc.toFloat()
            // Mostrar el valor de IMC en la parte superior de la barra
            setValueString(progreso.imc.toString())
        }
        puntos.add(barra)
    }

    // Elegir los colores de las barras
    private fun getColorFromIMC(imc: Double): String {
        return when {
            imc > 30.0 -> "#FF0000"
            imc in 25.0..29.9 -> "#FFD700"
            imc in 18.5..24.9 -> "#008000"
            else -> "#FFA500"
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}