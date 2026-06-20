package com.example.trainex.ejercicio

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.R
import com.example.trainex.databinding.ItemEjercicioBinding

/**
 * Adaptador para el RecyclerView que gestiona la visualización de la lista de ejercicios.
 * @param ejercicios Lista inicial de objetos de tipo Ejercicio.
 * @param onEjercicioClick Función lambda (callback) que se ejecuta al hacer clic en un elemento.
 */
class EjercicioAdapter(
    private var ejercicios: List<Ejercicio>,
    private val onEjercicioClick: (Ejercicio) -> Unit = {}
) : RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder>() {

    /**
     * Infla el diseño de cada elemento de la lista (item_ejercicio.xml)
     * utilizando ViewBinding y crea el ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val binding = ItemEjercicioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EjercicioViewHolder(binding)
    }

    /**
     * Conecta los datos de un ejercicio específico con el ViewHolder en una posición dada.
     */
    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        holder.bind(ejercicios[position])
    }

    /**
     * Devuelve la cantidad total de elementos en la lista.
     */
    override fun getItemCount(): Int = ejercicios.size

    /**
     * Permite actualizar la lista de ejercicios (por ejemplo, tras un filtrado)
     * y notifica al RecyclerView para que se redibuje.
     */
    fun actualizarLista(nuevaLista: List<Ejercicio>) {
        ejercicios = nuevaLista
        notifyDataSetChanged()
    }

    /**
     * Clase interna que contiene las referencias a las vistas de cada elemento de la lista.
     */
    inner class EjercicioViewHolder(private val binding: ItemEjercicioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val context: Context = binding.root.context

        /**
         * Asigna los datos del objeto Ejercicio a los componentes visuales del item.
         */
        fun bind(ejercicio: Ejercicio) {
            // Se utiliza traducción dinámica para el título y subtítulo usando las claves guardadas
            binding.tvTitulo.text = getStringFromKey(ejercicio.nombre)
            binding.tvSubtitulo.text = getStringFromKey(ejercicio.grupoMuscular)

            // Se busca el ID del recurso de imagen dinámicamente por su nombre de archivo
            val resourceId = getResourceIdFromName(context, ejercicio.imagen)
            if (resourceId != 0) {
                binding.imgEjercicio.setImageResource(resourceId)
            } else {
                // Imagen por defecto si no se encuentra el recurso especificado
                binding.imgEjercicio.setImageResource(R.drawable.ic_image_not_found)
            }

            // Configuración del evento de clic en toda la celda
            itemView.setOnClickListener {
                // Ejecuta la acción adicional definida al instanciar el adaptador
                onEjercicioClick(ejercicio)

                // Navega a la pantalla de detalle pasando el objeto Ejercicio seleccionado
                val intent = Intent(context, DetalleEjercicioActivity::class.java)
                intent.putExtra("EJERCICIO", ejercicio)
                context.startActivity(intent)
            }
        }

        /**
         * Obtiene un String desde los recursos (strings.xml) usando una clave de texto.
         * Útil para cuando los nombres vienen como Strings desde una base de datos.
         */
        private fun getStringFromKey(key: String): String {
            val resourceId = context.resources.getIdentifier(
                key,
                "string",
                context.packageName
            )
            return if (resourceId != 0) context.getString(resourceId) else key
        }

        /**
         * Obtiene el ID numérico de un recurso drawable a partir de su nombre en formato String.
         */
        private fun getResourceIdFromName(context: Context, resourceName: String): Int {
            return context.resources.getIdentifier(
                resourceName,
                "drawable",
                context.packageName
            )
        }
    }
}