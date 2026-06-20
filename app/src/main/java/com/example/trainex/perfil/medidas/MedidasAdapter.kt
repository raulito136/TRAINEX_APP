package com.example.trainex.perfil.medidas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.R
import com.example.trainex.databinding.ItemMedidaBinding

/**
 * Adaptador para mostrar la lista de categorías de medidas en un RecyclerView.
 * * @property listaMedidas Lista de objetos [TipoMedida] a representar.
 * @property onItemClick Acción que se ejecuta al seleccionar una medida de la lista.
 */
class MedidasAdapter(
    private val listaMedidas: List<TipoMedida>,
    private val onItemClick: (TipoMedida) -> Unit
) : RecyclerView.Adapter<MedidasAdapter.MedidaViewHolder>() {

    /**
     * Infla la vista de cada elemento del listado.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedidaViewHolder {
        val binding = ItemMedidaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedidaViewHolder(binding)
    }

    /**
     * Vincula los datos de la posición actual con el ViewHolder.
     */
    override fun onBindViewHolder(holder: MedidaViewHolder, position: Int) {
        holder.bind(listaMedidas[position], onItemClick)
    }

    /**
     * Indica el número de elementos disponibles en la lista.
     */
    override fun getItemCount(): Int = listaMedidas.size

    /**
     * ViewHolder que gestiona la lógica de vinculación de datos para un ítem de medida.
     */
    class MedidaViewHolder(private val binding: ItemMedidaBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Asigna los textos, iconos y eventos de clic al componente visual.
         * * @param item El objeto de tipo medida con la información a mostrar.
         * @param onClick Callback para el evento de selección.
         */
        fun bind(item: TipoMedida, onClick: (TipoMedida) -> Unit) {
            binding.tvNombre.setText(item.nombreResId)

            // Lógica simple para cargar imagen o default
            if (item.iconoResId != 0) {
                binding.imgIcono.setImageResource(item.iconoResId)
            } else {
                binding.imgIcono.setImageResource(R.drawable.ic_launcher_foreground)
            }

            // Click listener en la raíz del item
            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }
}