package com.example.trainex.agregarEjercicios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.R

/**
 * Adaptador para el RecyclerView que muestra la lista de ejercicios disponibles.
 * @property onItemSelected Callback que se dispara al pulsar sobre un ejercicio, devolviendo su ID.
 */
class AgregarEjercicioAdapter(
    private val onItemSelected: (Int) -> Unit
) : RecyclerView.Adapter<AgregarEjercicioViewHolder>() {

    private var listaEjercicios: List<Ejercicio> = emptyList()
    private var selectedIds: Set<Int> = emptySet()

    /**
     * Actualiza la lista completa de ejercicios y el estado de selección de una sola vez.
     */
    fun submitList(nuevaLista: List<Ejercicio>, nuevosSeleccionados: Set<Int>) {
        listaEjercicios = nuevaLista
        selectedIds = nuevosSeleccionados
        notifyDataSetChanged()
    }

    /**
     * Actualiza únicamente los IDs seleccionados para refrescar la UI sin recargar toda la lista.
     */
    fun updateSelectedIds(nuevosSeleccionados: Set<Int>) {
        selectedIds = nuevosSeleccionados
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgregarEjercicioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ejercicio, parent, false)
        return AgregarEjercicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AgregarEjercicioViewHolder, position: Int) {
        val item = listaEjercicios[position]
        val isSelected = selectedIds.contains(item.id)
        holder.render(item, isSelected) {
            onItemSelected(item.id)
        }
    }

    override fun getItemCount(): Int = listaEjercicios.size
}