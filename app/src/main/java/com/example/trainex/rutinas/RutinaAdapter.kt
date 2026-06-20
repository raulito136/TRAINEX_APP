package com.example.trainex.rutinas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.R
import com.example.trainex.rutinas.Rutina

/**
 * Adaptador para gestionar y visualizar una lista de objetos [Rutina] en un RecyclerView.
 * * @property listaRutinas Fuente de datos que contiene las rutinas a mostrar.
 * @property onRutinaClick Callback ejecutado al seleccionar una rutina para ver sus detalles.
 * @property onEditarClick Callback ejecutado al pulsar el botón de edición de una rutina.
 * @property onEliminarClick Callback ejecutado al pulsar el botón de borrado de una rutina.
 */
class RutinaAdapter(
    private var listaRutinas: List<Rutina>,
    private val onRutinaClick: (Rutina) -> Unit,
    private val onEditarClick: (Rutina) -> Unit,
    private val onEliminarClick: (Rutina) -> Unit
) : RecyclerView.Adapter<RutinaViewHolder>() {

    /**
     * Infla el diseño XML para cada ítem de rutina y crea el ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rutina, parent, false)
        return RutinaViewHolder(view)
    }

    /**
     * Vincula los datos de una rutina en una posición específica con la vista del ViewHolder.
     */
    override fun onBindViewHolder(holder: RutinaViewHolder, position: Int) {
        holder.render(
            listaRutinas[position],
            onRutinaClick,
            onEditarClick,
            onEliminarClick
        )
    }

    /**
     * Retorna el tamaño actual de la lista de datos.
     */
    override fun getItemCount(): Int = listaRutinas.size

    /**
     * Reemplaza la lista de datos actual por una nueva y refresca la interfaz.
     * @param nuevaLista La nueva colección de rutinas a representar.
     */
    fun actualizarLista(nuevaLista: List<Rutina>) {
        listaRutinas = nuevaLista
        notifyDataSetChanged()
    }

    /**
     * Recupera la colección de rutinas que el adaptador está gestionando actualmente.
     * @return Lista inmutable de rutinas actuales.
     */
    fun getListaActual(): List<Rutina> = listaRutinas
}