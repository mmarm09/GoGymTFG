package com.hlanz.appgogym.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hlanz.appgogym.R
import com.hlanz.appgogym.adaptadores.FiltroAdapter

class SelectComplementoActivity : AppCompatActivity() {

    private lateinit var recyclerViewComplemento: RecyclerView
    private lateinit var complementoAdapter: FiltroAdapter

    // Lista de complementos
    private val complementos = listOf("EQUIPAMIENTO", "PESO CORPORAL", "PESAS", "ACCESORIOS")
    private val complementoImages = mapOf(
        "EQUIPAMIENTO" to R.drawable.fitness_gym_jrjvg1o1p7q4,
        "PESO CORPORAL" to R.drawable.tshirt_2or9jrxthtit,
        "PESAS" to R.drawable.dumbbell_i94183qotbdn,
        "ACCESORIOS" to R.drawable.skipping_rope_uv8eo74xwx4e
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_complemento)

        // Inicialización del RecyclerView
        recyclerViewComplemento = findViewById(R.id.recyclerViewComplemento)
        recyclerViewComplemento.layoutManager = LinearLayoutManager(this)

        // Configuración del adaptador del RecyclerView con la lista de complementos y sus imágenes
        complementoAdapter = FiltroAdapter(complementos, complementoImages) { complemento -> onComplementoSelected(complemento) }
        recyclerViewComplemento.adapter = complementoAdapter
    }

    // Método llamado cuando se selecciona un complemento
    private fun onComplementoSelected(complemento: String) {
        // Obtener el tipo de ejercicio y el grupo muscular seleccionados del Intent
        val tipo = intent.getStringExtra("tipo") ?: return
        val grupoMuscular = intent.getStringExtra("grupo_muscular") ?: return

        val intent = Intent(this, SelectEjercicioActivity::class.java)
        intent.putExtra("tipo", tipo)
        intent.putExtra("grupo_muscular", grupoMuscular)
        intent.putExtra("complemento", complemento)
        startActivity(intent)
    }
}
