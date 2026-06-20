package com.example.trainex.dietas

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.example.trainex.R
import com.example.trainex.databinding.ItemAlimentoBinding

/**
 * Adaptador para el RecyclerView que muestra la lista de alimentos.
 * @property listaAlimentos Colección de objetos [Alimento] a mostrar.
 * @property onAlimentoClick Callback para el clic simple.
 * @property onAlimentoLongClick Callback para el clic prolongado (borrado).
 */
class AlimentosAdapter(
    private var listaAlimentos: List<Alimento>,
    private val onAlimentoClick: (Alimento) -> Unit,
    private val onAlimentoLongClick: (Alimento) -> Unit
) : RecyclerView.Adapter<AlimentosAdapter.AlimentoViewHolder>() {

    /**
     * ViewHolder que contiene la referencia a la vista del ítem de alimento.
     */
    inner class AlimentoViewHolder(val binding: ItemAlimentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentoViewHolder {
        val binding = ItemAlimentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlimentoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlimentoViewHolder, position: Int) {
        val item = listaAlimentos[position]
        with(holder.binding) {
            tvNombreAlimento.text = item.nombre
            tvMarcaAlimento.text = item.marca
            tvCategoria.text = item.categoria

            // --- LÓGICA DE IMAGEN ---
            try {
                if (item.imagen.isNotEmpty()) {
                    if (item.imagen.startsWith("http")) {
                        // Caso para URLs externas con cabeceras personalizadas
                        val urlConHeaders = GlideUrl(
                            item.imagen,
                            LazyHeaders.Builder()
                                .addHeader("User-Agent", "TrainexApp-Android")
                                .build()
                        )

                        Glide.with(holder.itemView.context)
                            .load(urlConHeaders)
                            .timeout(60000)
                            .apply(RequestOptions().centerCrop())
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(ivAlimentoIcon)
                    } else {
                        // Caso para imágenes en Base64 (Firebase)
                        try {
                            val imageBytes = Base64.decode(item.imagen, Base64.DEFAULT)
                            Glide.with(holder.itemView.context)
                                .load(imageBytes)
                                .apply(RequestOptions().centerCrop())
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(ivAlimentoIcon)
                        } catch (e: Exception) {
                            ivAlimentoIcon.setImageResource(R.drawable.ic_launcher_foreground)
                        }
                    }
                } else {
                    ivAlimentoIcon.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } catch (e: Exception) {
                ivAlimentoIcon.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }

        holder.itemView.setOnClickListener { onAlimentoClick(item) }
        holder.itemView.setOnLongClickListener {
            onAlimentoLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = listaAlimentos.size

    /**
     * Actualiza la lista de alimentos y refresca la interfaz.
     */
    fun actualizarLista(nuevaLista: List<Alimento>) {
        listaAlimentos = nuevaLista
        notifyDataSetChanged()
    }
}