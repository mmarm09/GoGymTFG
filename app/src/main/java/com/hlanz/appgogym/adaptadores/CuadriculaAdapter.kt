package com.hlanz.appgogym.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hlanz.appgogym.R

class CuadriculaAdapter(
    // Contexto de la actividad o fragmento
    private val context: Context,
    // Lista de elementos a mostrar en la cuadrícula
    private val cuadros: List<String>,
    // Interfaz para manejar clics en los ítems
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<CuadriculaAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // ViewHolder que representa cada ítem en el RecyclerView
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewCuadro)

        init {
            // Configuración del listener para el clic en el ítem
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_rutina_cuadricula, parent, false)
        return ViewHolder(view)
    }

    // Método para enlazar datos a un ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = cuadros[position]
    }

    // Método para obtener el número de ítems en la lista
    override fun getItemCount(): Int {
        return cuadros.size
    }
}
