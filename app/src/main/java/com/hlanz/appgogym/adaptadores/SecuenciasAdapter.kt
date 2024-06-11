package com.hlanz.appgogym.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hlanz.appgogym.R
import com.hlanz.appgogym.modelos.Secuencia

// Adaptador para el RecyclerView que muestra una lista de secuencias de ejercicios
class SecuenciasAdapter(
    // Lista de secuencias de ejercicios
    private val secuencias: List<Secuencia>,
    // Función de clic que se invoca cuando se hace clic en un elemento de la lista
    private val onClick: (Secuencia, Int) -> Unit,
    // Tipo de ejercicio, usado para determinar cómo mostrar las secuencias
    private val tipoEjercicio: String
) : RecyclerView.Adapter<SecuenciasAdapter.SecuenciasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecuenciasViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_secuencia, parent, false)
        return SecuenciasViewHolder(view)
    }

    // Vincula los datos de la lista al ViewHolder
    override fun onBindViewHolder(holder: SecuenciasViewHolder, position: Int) {
        val secuencia = secuencias[position]
        holder.bind(secuencia, position, tipoEjercicio)
        holder.itemView.setOnClickListener {
            // Llama a la función de clic cuando se hace clic en un elemento de la lista
            onClick(secuencia, position)
        }
    }

    // Devuelve el número de elementos en la lista
    override fun getItemCount(): Int = secuencias.size

    // ViewHolder que representa cada ítem en el RecyclerView
    class SecuenciasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serieRepeticionesTextView: TextView = itemView.findViewById(R.id.serieRepeticionesTextView)

        // Método para vincular los datos de la secuencia al ViewHolder
        fun bind(secuencia: Secuencia, position: Int, tipoEjercicio: String) {
            // Determina cómo mostrar las secuencias según el tipo de ejercicio
            if (tipoEjercicio == "CARDIO") {
                serieRepeticionesTextView.text = "Duración: ${secuencia.duracion} min"
            } else {
                serieRepeticionesTextView.text = "Serie ${position + 1}: ${secuencia.repeticiones} repeticiones"
            }
        }
    }
}
