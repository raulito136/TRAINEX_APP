package com.example.trainex.agregarEjercicios

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.R
import com.example.trainex.databinding.ItemEjercicioBinding

/**
 * Holder que gestiona la vista individual de cada ejercicio.
 */
class AgregarEjercicioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = ItemEjercicioBinding.bind(view)
    private val context = view.context

    /**
     * Obtiene una cadena de texto desde los recursos usando su clave string.
     */
    private fun getStringFromKey(key: String): String {
        val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resourceId != 0) context.getString(resourceId) else key
    }

    /**
     * Vincula los datos del ejercicio con la vista y gestiona el aspecto visual de la selección.
     * @param item El objeto [Ejercicio] a mostrar.
     * @param isSelected Define si el ejercicio debe mostrarse con el fondo de selección.
     * @param onClick Acción a ejecutar al pulsar el elemento.
     */
    fun render(item: Ejercicio, isSelected: Boolean, onClick: () -> Unit) {
        val colorRes = if (isSelected) R.color.selection else R.color.background
        binding.cardView.setCardBackgroundColor(context.getColor(colorRes))

        itemView.setOnClickListener { onClick() }

        binding.tvTitulo.text = getStringFromKey(item.nombre)
        binding.tvSubtitulo.text = getStringFromKey(item.grupoMuscular)

        val resId = context.resources.getIdentifier(item.imagen, "drawable", context.packageName)
        binding.imgEjercicio.setImageResource(if (resId != 0) resId else R.drawable.ic_image_not_found)
    }
}