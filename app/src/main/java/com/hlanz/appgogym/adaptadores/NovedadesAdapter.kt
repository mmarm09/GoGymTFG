package com.hlanz.appgogym.adaptadores

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hlanz.appgogym.R
import com.hlanz.appgogym.modelos.Novedades

// Adaptador para el RecyclerView que muestra una lista de novedades
class NovedadesAdapter(private val novedades: List<Novedades>) : RecyclerView.Adapter<NovedadesAdapter.NovedadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NovedadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_novedades, parent, false)
        return NovedadViewHolder(view)
    }

    // Vincula los datos de la lista al ViewHolder
    override fun onBindViewHolder(holder: NovedadViewHolder, position: Int) {
        val novedad = novedades[position]
        holder.bind(novedad)
    }

    // Devuelve el número de elementos en la lista
    override fun getItemCount(): Int = novedades.size

    // ViewHolder que representa cada ítem en el RecyclerView
    class NovedadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val imageViewTipo: ImageView = itemView.findViewById(R.id.imageViewTipo)

        // Método para vincular los datos de la novedad al ViewHolder
        fun bind(novedad: Novedades) {
            tvTitulo.text = novedad.tipo + ": " + novedad.titulo
            tvDescripcion.text = novedad.descripcion

            // Colores para el fondo degradado según el tipo de novedad
            val colorStart = when (novedad.tipo) {
                "BENEFICIO" -> ContextCompat.getColor(itemView.context, R.color.colorBeneficioStart)
                "CONSEJO" -> ContextCompat.getColor(itemView.context, R.color.colorConsejoStart)
                "NOTICIA" -> ContextCompat.getColor(itemView.context, R.color.colorNoticiaStart)
                else -> Color.WHITE
            }

            val colorEnd = when (novedad.tipo) {
                "BENEFICIO" -> ContextCompat.getColor(itemView.context, R.color.colorBeneficioEnd)
                "CONSEJO" -> ContextCompat.getColor(itemView.context, R.color.colorConsejoEnd)
                "NOTICIA" -> ContextCompat.getColor(itemView.context, R.color.colorNoticiaEnd)
                else -> Color.WHITE
            }

            // Crea un Drawable degradado con los colores obtenidos
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(colorStart, colorEnd)
            )

            itemView.background = gradientDrawable

            // Asigna una imagen al tipo de novedad
            val imageResId = when (novedad.tipo) {
                "BENEFICIO" -> R.drawable.beneficio
                "CONSEJO" -> R.drawable.consejo
                "NOTICIA" -> R.drawable.noticia
                else -> R.drawable.interrogacion
            }
            // Establece la imagen en el ImageView
            imageViewTipo.setImageResource(imageResId)
        }

    }
}
