package com.example.trainex.crearRutinas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.R

/**
 * Adaptador para el RecyclerView que muestra los ejercicios seleccionados para la rutina.
 * @property listaEjercicios Lista mutable de objetos [Ejercicio].
 * @property onItemClick Callback invocado cuando se hace clic en un ejercicio.
 */
class CrearRutinaAdapter(
    private var listaEjercicios: MutableList<Ejercicio>,
    private val onItemClick: (Ejercicio) -> Unit
) : RecyclerView.Adapter<CrearRutinaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrearRutinaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CrearRutinaViewHolder(
            inflater.inflate(R.layout.item_ejercicio, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CrearRutinaViewHolder, position: Int) {
        val item = listaEjercicios[position]
        holder.render(item)

        // Gestión del clic en el ítem
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = listaEjercicios.size

    /**
     * Actualiza la lista interna de ejercicios y notifica los cambios al adaptador.
     */
    fun actualizarLista(nuevaLista: MutableList<Ejercicio>) {
        listaEjercicios = nuevaLista
        notifyDataSetChanged()
    }
}