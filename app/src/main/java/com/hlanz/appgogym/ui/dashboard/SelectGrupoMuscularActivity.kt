package com.hlanz.appgogym.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hlanz.appgogym.R
import com.hlanz.appgogym.adaptadores.FiltroAdapter

class SelectGrupoMuscularActivity : AppCompatActivity() {

    private lateinit var recyclerViewGrupoMuscular: RecyclerView
    private lateinit var grupoMuscularAdapter: FiltroAdapter

    private val tipos = mapOf(
        "FUERZA" to listOf("TREN SUPERIOR", "TRONCO", "TREN INFERIOR"),
        "CARDIO" to listOf("General"),
        "ESTIRAMIENTO" to listOf("General")
    )

    private val grupoMuscularImages = mapOf(
        "TREN SUPERIOR" to R.drawable.trensuperior,
        "TRONCO" to R.drawable.tronco,
        "TREN INFERIOR" to R.drawable.treninferior,
        "General" to R.drawable.pordefecto
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_grupo_muscular)

        recyclerViewGrupoMuscular = findViewById(R.id.recyclerViewGrupoMuscular)
        recyclerViewGrupoMuscular.layoutManager = LinearLayoutManager(this)

        // Obtenemos el tipo de ejercicio seleccionado
        val tipo = intent.getStringExtra("tipo") ?: return
        // Obtenemos la lista de tipos de ejercicio asociada al tipo de ejercicio seleccionado
        val grupos = tipos[tipo] ?: emptyList()

        // Configuramos el adaptador del RecyclerView con la lista de grupos musculares
        grupoMuscularAdapter = FiltroAdapter(grupos, grupoMuscularImages) { grupo -> onGrupoMuscularSelected(tipo, grupo) }
        recyclerViewGrupoMuscular.adapter = grupoMuscularAdapter
    }

    // Método llamado cuando se selecciona un grupo muscular
    private fun onGrupoMuscularSelected(tipo: String, grupo: String) {
        val intent = Intent(this, SelectComplementoActivity::class.java)
        intent.putExtra("tipo", tipo)
        intent.putExtra("grupo_muscular", grupo)
        startActivity(intent)
    }
}
