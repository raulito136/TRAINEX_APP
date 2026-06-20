package com.example.trainex.perfil.calendario

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.databinding.ItemDiaCalendarioBinding
import com.example.trainex.databinding.ItemMesCalendarioBinding

/**
 * Adaptador dinámico para el RecyclerView del calendario.
 * Gestiona tres tipos de vista: Cabeceras de mes, celdas de días y espacios vacíos.
 */
class CalendarioAdapter(private val items: List<CalendarioItem>, private val onDayClick: (java.time.LocalDate) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_DAY = 1
        const val TYPE_EMPTY = 2
    }

    /** ViewHolder que vincula la vista del nombre del mes mediante ViewBinding */
    class HeaderViewHolder(val binding: ItemMesCalendarioBinding) : RecyclerView.ViewHolder(binding.root)

    /** ViewHolder que vincula la vista de la celda del día mediante ViewBinding */
    class DayViewHolder(val binding: ItemDiaCalendarioBinding) : RecyclerView.ViewHolder(binding.root)

    /** ViewHolder para espacios en blanco (relleno al inicio de los meses) */
    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    /** Determina el tipo de vista según la clase del objeto en la posición actual */
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CalendarioItem.MonthHeader -> TYPE_HEADER
            is CalendarioItem.Day -> TYPE_DAY
            is CalendarioItem.Empty -> TYPE_EMPTY
        }
    }

    /** Infla el layout correspondiente según el tipo de vista detectado */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemMesCalendarioBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            TYPE_DAY -> {
                val binding = ItemDiaCalendarioBinding.inflate(inflater, parent, false)
                DayViewHolder(binding)
            }
            else -> {
                /** Crea una vista vacía con una altura fija para mantener la cuadrícula */
                EmptyViewHolder(View(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50)
                })
            }
        }
    }

    /** * Asigna los datos a la vista.
     * En el caso de los días, gestiona la visibilidad del subrayado si existe entrenamiento.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CalendarioItem.MonthHeader -> {
                (holder as HeaderViewHolder).binding.tvMonthName.text = item.name
            }
            is CalendarioItem.Day -> {
                val dayHolder = holder as DayViewHolder
                dayHolder.binding.tvDayNumber.text = item.date.dayOfMonth.toString()

                /** Si el día tiene un entrenamiento registrado, se muestra el indicador visual */
                if (item.tieneEntreno) {
                    dayHolder.binding.viewUnderline.visibility = View.VISIBLE
                } else {
                    dayHolder.binding.viewUnderline.visibility = View.INVISIBLE
                }

                holder.itemView.setOnClickListener {
                    onDayClick(item.date)
                }
            }
            is CalendarioItem.Empty -> { /* Sin acción requerida para espacios vacíos */ }
        }
    }

    override fun getItemCount(): Int = items.size
}