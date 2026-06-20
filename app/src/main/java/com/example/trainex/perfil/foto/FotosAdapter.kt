package com.example.trainex.perfil.foto

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.trainex.databinding.ItemFotoProgresoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adaptador para gestionar y mostrar una lista de objetos [FotoProgreso] en un RecyclerView.
 * * @property lista Colección de fotos a mostrar.
 * @property onFotoClick Función lambda que se ejecuta al hacer un clic simple en una foto.
 * @property onFotoLongClick Función lambda que se ejecuta al mantener presionada una foto.
 */
class FotosAdapter(
    private var lista: List<FotoProgreso> = emptyList(),
    private val onFotoClick: (FotoProgreso) -> Unit,
    private val onFotoLongClick: (FotoProgreso) -> Unit
) : RecyclerView.Adapter<FotosAdapter.ViewHolder>() {

    /**
     * ViewHolder que contiene la referencia a la vista de cada elemento de la lista.
     * Utiliza View Binding para acceder a los componentes del diseño.
     */
    class ViewHolder(val binding: ItemFotoProgresoBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Infla el diseño del ítem y crea una nueva instancia del ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFotoProgresoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Vincula los datos de un objeto [FotoProgreso] con los componentes visuales del ViewHolder.
     * Gestiona el formateo de fecha, la carga de imágenes (URL o Base64) y los listeners de clics.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        val context = holder.itemView.context

        // Formateo de fecha
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.binding.tvFechaFoto.text = sdf.format(Date(item.fecha))

        // Lógica de carga de imagen (URL o Base64)
        try {
            val request = Glide.with(context)

            if (item.url.length > 200 && !item.url.startsWith("http")) {
                val imageBytes = Base64.decode(item.url, Base64.DEFAULT)
                request.asBitmap()
                    .load(imageBytes)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.binding.ivFoto)
            } else {
                request.load(item.url)
                    .centerCrop()
                    .into(holder.binding.ivFoto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.itemView.setOnClickListener { onFotoClick(item) }
        holder.itemView.setOnLongClickListener {
            onFotoLongClick(item)
            true
        }
    }

    /**
     * Retorna la cantidad total de elementos en la lista.
     */
    override fun getItemCount(): Int = lista.size

    /**
     * Actualiza los datos del adaptador de forma segura y notifica los cambios.
     * * @param nuevaLista La nueva lista de [FotoProgreso] que se debe mostrar.
     */
    fun actualizarLista(nuevaLista: List<FotoProgreso>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}