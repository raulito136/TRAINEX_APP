// HistorialMedidasAdapter.kt
package com.example.trainex.perfil.medidas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.databinding.ItemHistorialMedidaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adaptador para el RecyclerView que muestra el listado histórico de mediciones.
 * * @property lista Colección de registros de medidas (RegistroMedida) a mostrar.
 * @property unidad Cadena de texto que representa la unidad de medida (kg, lbs, cm, %, etc.).
 * @property onItemClick Función lambda que se ejecuta al realizar un clic simple en un elemento.
 */
class HistorialMedidasAdapter(
    private val lista: List<RegistroMedida>,
    private val unidad: String,                // Unidad a mostrar (kg, lbs, cm, %)
    private val onItemClick: (RegistroMedida) -> Unit   // Click normal para eliminar
) : RecyclerView.Adapter<HistorialMedidasAdapter.ViewHolder>() {

    /**
     * ViewHolder que mantiene la referencia a la vista del ítem mediante View Binding.
     */
    class ViewHolder(val binding: ItemHistorialMedidaBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Infla el diseño XML del ítem y crea el ViewHolder correspondiente.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorialMedidaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Vincula los datos del registro en la posición dada con las vistas del ViewHolder.
     * Formatea la fecha y el valor numérico adjuntando la unidad correspondiente.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.binding.tvFecha.text = sdf.format(Date(item.fecha))

        // Mostrar el valor con la unidad recibida (ya convertida si es necesario)
        holder.binding.tvValor.text = String.format(Locale.getDefault(), "%.1f %s", item.valor, unidad)

        // Click normal (no long click) para eliminar
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    /**
     * Devuelve la cantidad total de elementos en la lista.
     */
    override fun getItemCount(): Int = lista.size
}