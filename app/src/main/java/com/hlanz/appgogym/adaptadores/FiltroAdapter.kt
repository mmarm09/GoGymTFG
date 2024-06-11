package com.hlanz.appgogym.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hlanz.appgogym.R

// Adaptador para el RecyclerView que muestra una lista de filtros con imágenes
class FiltroAdapter(
    // Lista de nombres de filtros
    private val items: List<String>,
    // Map de nombres de filtros a recursos de imagen
    private val images: Map<String, Int>,
    // Función de callback para manejar clics en los ítems
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FiltroAdapter.FiltroViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiltroViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filtro, parent, false)
        return FiltroViewHolder(view)
    }

    // Vincula los datos de la lista al ViewHolder
    override fun onBindViewHolder(holder: FiltroViewHolder, position: Int) {
        // Obtiene el nombre del filtro en la posición actual
        val item = items[position]
        // Establece el nombre del filtro en el TextView
        holder.textViewFiltro.text = item
        // Establece la imagen correspondiente o una por defecto
        holder.imageViewFiltro.setImageResource(images[item] ?: R.drawable.pordefecto)
        // Configura el clic en el ítem
        holder.itemView.setOnClickListener { onClick(item) }
    }

    // Devuelve el número de elementos en la lista
    override fun getItemCount() = items.size

    // ViewHolder que representa cada ítem en el RecyclerView
    class FiltroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewFiltro: TextView = itemView.findViewById(R.id.textViewFiltro)
        val imageViewFiltro: ImageView = itemView.findViewById(R.id.imageViewFiltro)
    }
}
