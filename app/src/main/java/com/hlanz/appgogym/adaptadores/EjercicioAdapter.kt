package com.hlanz.appgogym.adaptadores

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hlanz.appgogym.R
import com.hlanz.appgogym.modelos.Ejercicio
import com.hlanz.appgogym.ui.dashboard.DetalleEjercicio

// Adaptador para el RecyclerView que muestra una lista de ejercicios
class EjercicioAdapter(
    // Lista de ejercicios a mostrar
    private val items: List<Ejercicio>,
    // Función de callback para manejar clics en los ítems
    private val onClick: (Ejercicio) -> Unit
) : RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder>() {

    private var deleteMode = false

    fun setDeleteMode(mode: Boolean) {
        deleteMode = mode
        // Notificar al adaptador para actualizar la visualización
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ejercicio, parent, false)
        return EjercicioViewHolder(view)
    }

    // Vincula los datos de la lista al ViewHolder
    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val item = items[position]
        holder.textViewNombre.text = item.nombre

        // Cargar la imagen desde la URL
        item.foto?.let {
            val imageUrl = "http://192.168.1.135:80/xampp/api/crud/ejercicio/$it"
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .fitCenter()
                .into(holder.imageViewFoto)
        }

        // Manejador del clic para abrir la actividad de detalles del ejercicio
        holder.itemView.setOnClickListener {
            if (deleteMode) {
                // Si estamos en modo de eliminación, llamar al método onItemClick
                onClick(item)
            } else {
                // Si no estamos en modo de eliminación, abrir la actividad de detalles del ejercicio
                val intent = Intent(holder.itemView.context, DetalleEjercicio::class.java)
                intent.putExtra("ejercicio", item)
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    // Devuelve el número de elementos en la lista
    override fun getItemCount() = items.size

    // ViewHolder que representa cada ítem en el RecyclerView
    class EjercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNombre: TextView = itemView.findViewById(R.id.textViewNombre)
        val imageViewFoto: ImageView = itemView.findViewById(R.id.imageViewFoto)
    }
}
