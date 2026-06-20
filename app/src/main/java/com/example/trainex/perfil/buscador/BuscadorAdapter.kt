package com.example.trainex.perfil.buscador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainex.R
import com.example.trainex.perfil.Usuario

/** Adaptador para gestionar la visualización de la lista de usuarios en el buscador */
class BuscadorAdapter(
    private var usuarios: List<Usuario>,
    private val onUsuarioClick: (Usuario) -> Unit
) : RecyclerView.Adapter<BuscadorAdapter.ViewHolder>() {

    /** ViewHolder que contiene las referencias a las vistas de cada elemento de la lista */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuario)
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatarUsuario)
    }

    /** Infla el layout específico para cada ítem de usuario */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_busqueda, parent, false)
        return ViewHolder(view)
    }

    /** Vincula los datos del usuario con la vista y configura el listener de clic */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.tvNombre.text = usuario.username

        // Cargar foto de perfil con Glide
        cargarFotoPerfil(holder.ivAvatar, usuario.foto)

        holder.itemView.setOnClickListener { onUsuarioClick(usuario) }
    }

    /**
     * Carga la foto de perfil del usuario usando Glide.
     * Soporta URLs (http/https) y Base64.
     */
    private fun cargarFotoPerfil(imageView: ImageView, foto: String?) {
        if (foto.isNullOrEmpty()) {
            // Si no hay foto, mostrar la imagen por defecto
            imageView.setImageResource(R.drawable.imagen_default_persona)
            return
        }

        val context = imageView.context

        // Si es URL (Firebase Storage o similar)
        if (foto.startsWith("http")) {
            Glide.with(context)
                .load(foto)
                .placeholder(R.drawable.imagen_default_persona)
                .error(R.drawable.imagen_default_persona)
                .circleCrop()
                .into(imageView)
        } else {
            // Si es Base64, convertirla
            try {
                val imageBytes = android.util.Base64.decode(foto, android.util.Base64.DEFAULT)
                Glide.with(context)
                    .load(imageBytes)
                    .placeholder(R.drawable.imagen_default_persona)
                    .error(R.drawable.imagen_default_persona)
                    .circleCrop()
                    .into(imageView)
            } catch (e: Exception) {
                // Si hay error en la conversión Base64, mostrar imagen por defecto
                imageView.setImageResource(R.drawable.imagen_default_persona)
            }
        }
    }

    /** Retorna la cantidad total de elementos en la lista */
    override fun getItemCount() = usuarios.size

    /** Actualiza la lista de usuarios y refresca el RecyclerView */
    fun actualizarLista(nuevaLista: List<Usuario>) {
        this.usuarios = nuevaLista
        notifyDataSetChanged()
    }
}
