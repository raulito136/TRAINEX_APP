package com.example.trainex.agregarEjercicios

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.R

/**
 * Adaptador horizontal para mostrar los filtros de categorías o grupos musculares.
 * @param grupos Lista de identificadores de grupos musculares.
 * @param onGrupoSelected Callback que devuelve la clave del grupo seleccionado o null si es "Todos".
 */
class FiltroGrupoAdapter(
    private val grupos: List<String>,
    private val onGrupoSelected: (String?) -> Unit
) : RecyclerView.Adapter<FiltroGrupoAdapter.ViewHolder>() {

    private var selectedPosition = 0 // 0 será "Todos"

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreGrupo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filtro_grupo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val grupoKey = grupos[position]
        val textoMostrar = if (position == 0) "Todos" else getStringFromKey(holder.itemView.context, grupoKey)

        holder.tvNombre.text = textoMostrar

        val isSelected = (selectedPosition == position)
        holder.tvNombre.isSelected = isSelected

        if (isSelected) {
            val colorPrimary = getThemeColor(holder.itemView.context, R.attr.primary)
            holder.tvNombre.setTextColor(colorPrimary)
            holder.tvNombre.typeface = Typeface.DEFAULT_BOLD
        } else {
            holder.tvNombre.setTextColor(Color.WHITE)
            holder.tvNombre.typeface = Typeface.DEFAULT
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            if (position == 0) {
                onGrupoSelected(null)
            } else {
                onGrupoSelected(grupoKey)
            }
        }
    }

    override fun getItemCount() = grupos.size

    /**
     * Obtiene un texto traducido basado en una clave de recurso.
     */
    private fun getStringFromKey(context: Context, key: String): String {
        val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resourceId != 0) context.getString(resourceId) else key
    }

    /**
     * Obtiene un color definido en los atributos del tema de la aplicación.
     * @param attrResId El ID del atributo (ej: R.attr.primary).
     */
    private fun getThemeColor(context: Context, attrResId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }
}