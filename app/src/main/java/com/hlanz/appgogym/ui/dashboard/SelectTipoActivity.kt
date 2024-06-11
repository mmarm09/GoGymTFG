package com.hlanz.appgogym.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hlanz.appgogym.R
import com.hlanz.appgogym.adaptadores.FiltroAdapter

class SelectTipoActivity : AppCompatActivity() {

    private lateinit var recyclerViewTipoEjercicio: RecyclerView
    private lateinit var tipoAdapter: FiltroAdapter

    // Lista de los tipos de ejercicios
    private val tipos = listOf("FUERZA", "CARDIO", "ESTIRAMIENTO")

    // Map que asocia al tipo de ejercicio con una imagen
    private val tiposImages = mapOf(
        "FUERZA" to R.drawable.muscle_719ftyvsza1k,
        "CARDIO" to R.drawable.heart_regular,
        "ESTIRAMIENTO" to R.drawable.warming_up_6slnhapkp189
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_tipo)

        // Inicializar el recyclerView
        recyclerViewTipoEjercicio = findViewById(R.id.recyclerViewTipoEjercicio)
        recyclerViewTipoEjercicio.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador
        tipoAdapter = FiltroAdapter(tipos, tiposImages) { tipo -> onTipoSelected(tipo) }
        recyclerViewTipoEjercicio.adapter = tipoAdapter
    }

    // Método llamado cuando se selecciona un tipo de ejercicio
    private fun onTipoSelected(tipo: String) {
        val intent = if (tipo == "FUERZA") {
            // Si el intent es de tipo Fuerza, iniciar la actividad para
            // escoger el grupo muscular
            Intent(this, SelectGrupoMuscularActivity::class.java)
        } else {
            // Si no, salen directamente todos los ejercicios
            Intent(this, SelectEjercicioActivity::class.java)
        }
        intent.putExtra("tipo", tipo)
        startActivity(intent)
    }
}
