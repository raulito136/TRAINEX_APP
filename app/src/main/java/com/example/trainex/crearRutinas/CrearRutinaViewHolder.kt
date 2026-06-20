package com.example.trainex.crearRutinas

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.R
import com.example.trainex.databinding.ItemEjercicioBinding

/**
 * ViewHolder que vincula los datos de un [Ejercicio] con su representación gráfica.
 * Reutiliza el diseño visual definido en item_ejercicio.xml.
 */
class CrearRutinaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = ItemEjercicioBinding.bind(view)
    private val context: Context = view.context

    /**
     * Renderiza la información del ejercicio, traduciendo nombres y grupos musculares.
     * @param item El objeto [Ejercicio] con los datos a mostrar.
     */
    fun render(item: Ejercicio) {
        // Traducción dinámica del nombre y grupo muscular
        binding.tvTitulo.text = getStringFromKey(item.nombre)
        binding.tvSubtitulo.text = getStringFromKey(item.grupoMuscular)

        // Gestión de la imagen del ejercicio
        val resId = context.resources.getIdentifier(
            item.imagen,
            "drawable",
            context.packageName
        )
        binding.imgEjercicio.setImageResource(if (resId != 0) resId else R.drawable.ic_image_not_found)

        // Estilización visual del elemento
        binding.cardView.setCardBackgroundColor(
            ContextCompat.getColor(context, R.color.backgroundCardView)
        )
        binding.tvTitulo.setTextColor(ContextCompat.getColor(context, R.color.text))
    }

    /**
     * Obtiene el valor traducido de una cadena desde los recursos usando su clave string.
     */
    private fun getStringFromKey(key: String): String {
        val resourceId = context.resources.getIdentifier(
            key,
            "string",
            context.packageName
        )
        return if (resourceId != 0) context.getString(resourceId) else key
    }
}