package com.example.trainex.dietas

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainex.R
import com.example.trainex.databinding.ItemAlimentoDiarioBinding

/**
 * Adaptador para el RecyclerView que lista los consumos del diario nutricional.
 * @property listaComidas Lista de alimentos registrados en el día.
 * @property onCheckChanged Callback para manejar la selección/deselección de un ítem.
 * @property onLongClick Callback para manejar el borrado o acciones extra mediante pulsación larga.
 */
class DiarioAdapter(
    private var listaComidas: List<AlimentoDiario>,
    private val onCheckChanged: (AlimentoDiario, Boolean) -> Unit,
    private val onLongClick: (AlimentoDiario) -> Unit
) : RecyclerView.Adapter<DiarioAdapter.DiarioViewHolder>() {

    /**
     * Contenedor de vistas para cada elemento del diario.
     */
    inner class DiarioViewHolder(val binding: ItemAlimentoDiarioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiarioViewHolder {
        val binding = ItemAlimentoDiarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiarioViewHolder, position: Int) {
        val item = listaComidas[position]

        with(holder.binding) {
            tvNombreAlimento.text = item.nombre
            tvMarcaAlimento.text = item.marca

            // --- LÓGICA VISUAL DE UNIDADES (G VS ML) ---
            val unidad = if (item.esLiquido) "ml" else "g"
            tvCategoria.text = "${item.cantidadGramos.toInt()} $unidad"

            // --- LÓGICA DEL CHECKBOX ---
            // Se limpia el listener antes de asignar para evitar disparos accidentales durante el reciclaje
            cbSeleccion.setOnCheckedChangeListener(null)
            cbSeleccion.isChecked = item.completado

            // Notifica cambios al listener externo
            cbSeleccion.setOnCheckedChangeListener { _, isChecked ->
                onCheckChanged(item, isChecked)
            }

            // --- CARGA DE IMAGEN ---
            try {
                if (item.imagen.startsWith("http")) {
                    // Carga normal para URLs
                    Glide.with(holder.itemView.context)
                        .load(item.imagen)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivAlimentoIcon)
                } else {
                    // Carga para imágenes en Base64
                    val imageBytes = Base64.decode(item.imagen, Base64.DEFAULT)
                    Glide.with(holder.itemView.context)
                        .load(imageBytes)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivAlimentoIcon)
                }
            } catch (e: Exception) {
                ivAlimentoIcon.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }

        // Listener para la interacción de borrado o edición
        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = listaComidas.size

    /**
     * Actualiza la lista de datos del adaptador y notifica los cambios.
     * @param nuevaLista La nueva lista de [AlimentoDiario] a mostrar.
     */
    fun actualizarLista(nuevaLista: List<AlimentoDiario>) {
        listaComidas = nuevaLista
        notifyDataSetChanged()
    }
}