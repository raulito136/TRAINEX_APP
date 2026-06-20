package com.example.trainex.perfil.calendario

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainex.R
import com.example.trainex.databinding.ItemEjercicioResumenExpandidoBinding // Asegúrate de que se genera este Binding
import com.example.trainex.ejercicio.Ejercicio
// Importa tu modelo de Serie si es necesario
// import com.example.trainex.ejercicio.Serie
import com.example.trainex.utils.UnitManager

/**
 * Adaptador para mostrar los ejercicios realizados dentro de una sesión en el resumen.
 * Cada ejercicio se visualiza con su imagen, nombre y una tabla detallada de las series ejecutadas.
 */
class ResumenEjercicioAdapter(
    private val listaEjercicios: List<Ejercicio>,
    // Ya no necesitamos el click listener obligatorio, pero puedes dejarlo si quieres editar
    private val onItemClick: (Ejercicio) -> Unit
) : RecyclerView.Adapter<ResumenEjercicioAdapter.ExpandidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandidoViewHolder {
        // Inflamos el NUEVO layout expandido
        val binding = ItemEjercicioResumenExpandidoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExpandidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpandidoViewHolder, position: Int) {
        val item = listaEjercicios[position]
        holder.render(item, onItemClick)
    }

    override fun getItemCount(): Int = listaEjercicios.size

    /** ViewHolder que gestiona la renderización individual de cada ejercicio y sus series */
    class ExpandidoViewHolder(val binding: ItemEjercicioResumenExpandidoBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = binding.root.context

        /** Obtiene la traducción de una clave desde los recursos strings.xml */
        private fun getStringFromKey(key: String): String {
            val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
            return if (resourceId != 0) context.getString(resourceId) else key
        }

        /**
         * Rellena la vista con los datos del ejercicio.
         * Gestiona la carga de imágenes dinámicas y la inflación manual de filas para la tabla de series.
         */
        fun render(ejercicio: Ejercicio, onClick: (Ejercicio) -> Unit) {
            // 1. Datos básicos del ejercicio
            binding.tvTitulo.text = getStringFromKey(ejercicio.nombre)
            binding.tvSubtitulo.text = getStringFromKey(ejercicio.grupoMuscular)

            // 2. Gestión de imagen con Glide
            val resId = context.resources.getIdentifier(
                ejercicio.imagen, "drawable", context.packageName
            )

            if (resId != 0) {
                Glide.with(context)
                    .load(resId)
                    .error(R.drawable.ic_image_not_found)       // Imagen si el recurso falla
                    .centerCrop()
                    .into(binding.imgEjercicio)
            } else {
                // Carga imagen por defecto si no se encuentra el ID en el JSON
                Glide.with(context)
                    .load(R.drawable.ic_image_not_found)
                    .into(binding.imgEjercicio)
            }

            // 3. RELLENAR LA TABLA DE SERIES
            // Limpiamos vistas previas por si se recicla el viewholder
            binding.llSeriesContainer.removeAllViews()
            val inflater = LayoutInflater.from(context)

            // Asumiendo que 'ejercicio.series' es tu lista de series
            ejercicio.series?.forEachIndexed { index, serie ->
                val rowView = inflater.inflate(R.layout.item_serie_historial, binding.llSeriesContainer, false)

                val tvNum = rowView.findViewById<TextView>(R.id.tvNumeroSerie)
                val tvPeso = rowView.findViewById<TextView>(R.id.tvPesoHistorial)
                val tvReps = rowView.findViewById<TextView>(R.id.tvRepsHistorial)

                tvNum.text = (index + 1).toString()

                // Lógica de conversión de peso
                val unidadPeso = UnitManager.obtener(context, "unidad_peso", "kg")
                val pesoVisual = UnitManager.aVisual(serie.kilogramos, unidadPeso)

                // Formateo para quitar el .0 si es entero
                val pesoTexto = if (pesoVisual % 1.0 == 0.0) pesoVisual.toInt().toString() else pesoVisual.toString()

                tvPeso.text = "$pesoTexto $unidadPeso"
                tvReps.text = "${serie.repeticiones}"

                binding.llSeriesContainer.addView(rowView)
            }

            // Click en toda la tarjeta
            itemView.setOnClickListener { onClick(ejercicio) }
        }
    }
}