package com.example.trainex.historial

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainex.R
import com.example.trainex.databinding.ItemHistorialEjercicioBinding
import com.example.trainex.ejercicio.Ejercicio

/**
 * Adaptador para el RecyclerView que muestra la lista de ejercicios realizados en el historial.
 * Gestiona la visualización de nombres traducidos, grupos musculares e imágenes dinámicas.
 */
class HistorialAdapter(
    private var listaEjercicios: List<Ejercicio>,
    private val onItemClick: (Ejercicio) -> Unit
) : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    /**
     * ViewHolder interno que vincula los datos del modelo Ejercicio con la vista del ítem.
     */
    inner class ViewHolder(val binding: ItemHistorialEjercicioBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos del ejercicio a las vistas correspondientes.
         */
        fun bind(ejercicio: Ejercicio) {
            val context = binding.root.context

            /** 1. Configuración de textos: Obtiene las traducciones de nombre y grupo muscular. */
            binding.tvNombre.text = getStringFromKey(context, ejercicio.nombre)
            binding.tvGrupoMuscular.text = getStringFromKey(context, ejercicio.grupoMuscular)

            /** 2. Obtención dinámica del ID del recurso de imagen desde el nombre guardado. */
            val resourceId = context.resources.getIdentifier(ejercicio.imagen, "drawable", context.packageName)

            /** 3. Carga de imagen con Glide: Implementa un fallback si el recurso no existe. */
            if (resourceId != 0) {
                Glide.with(context)
                    .load(resourceId)
                    .error(R.drawable.ic_image_not_found)
                    .centerCrop()
                    .into(binding.imgEjercicio)
            } else {
                Glide.with(context)
                    .load(R.drawable.ic_image_not_found)
                    .into(binding.imgEjercicio)
            }

            /** 4. Configura el listener de clic para navegar al detalle del ejercicio. */
            binding.root.setOnClickListener { onItemClick(ejercicio) }
        }

        /**
         * Busca una cadena traducida en los recursos del sistema a partir de una clave de texto.
         */
        private fun getStringFromKey(context: Context, key: String): String {
            val resId = context.resources.getIdentifier(key, "string", context.packageName)
            return if (resId != 0) context.getString(resId) else key
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorialEjercicioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(listaEjercicios[position])

    override fun getItemCount() = listaEjercicios.size

    /**
     * Actualiza los datos del adaptador y refresca la vista completa.
     */
    fun actualizarLista(nuevaLista: List<Ejercicio>) {
        listaEjercicios = nuevaLista
        notifyDataSetChanged()
    }
}