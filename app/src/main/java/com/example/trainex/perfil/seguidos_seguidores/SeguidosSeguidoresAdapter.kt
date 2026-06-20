package com.example.trainex.perfil.seguidos_seguidores

import android.content.Intent
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainex.R
import com.example.trainex.databinding.ItemUsuarioSocialBinding
import com.example.trainex.perfil.Usuario
import com.example.trainex.perfil.buscador.PerfilPublicoActivity

/**
 * Adaptador para el RecyclerView que muestra ítems individuales de usuarios en las listas sociales.
 * Permite visualizar el nombre de usuario, su avatar y navegar a su perfil público.
 * * @property usuarios Lista de objetos [Usuario] que se mostrarán.
 */
class SeguidosSeguidoresAdapter(private val usuarios: List<Usuario>) :
    RecyclerView.Adapter<SeguidosSeguidoresAdapter.ViewHolderUsuario>() {

    /**
     * ViewHolder que contiene la referencia al View Binding del ítem de usuario.
     */
    class ViewHolderUsuario(val binding: ItemUsuarioSocialBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Infla el diseño XML para cada ítem de usuario.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderUsuario {
        return ViewHolderUsuario(
            ItemUsuarioSocialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    /**
     * Vincula los datos de un usuario con las vistas correspondientes.
     * Gestiona la decodificación de la imagen en Base64 y la navegación al perfil del usuario.
     */
    override fun onBindViewHolder(holder: ViewHolderUsuario, position: Int) {
        val usuario = usuarios[position]

        holder.binding.tvUsernameSocial.text = usuario.username

        // Lógica de gestión de la imagen de perfil
        val fotoRes = usuario.foto
        if (fotoRes.isNullOrEmpty()) {
            // Carga de imagen por defecto si no existe foto
            holder.binding.ivAvatarSocial.setImageResource(R.drawable.imagen_default_persona)
        } else {
            try {
                // Decodificación de cadena Base64 a bytes para carga con Glide
                val imageBytes = Base64.decode(fotoRes, Base64.DEFAULT)
                Glide.with(holder.itemView.context)
                    .load(imageBytes)
                    .placeholder(R.drawable.imagen_default_persona)
                    .circleCrop()
                    .into(holder.binding.ivAvatarSocial)
            } catch (e: Exception) {
                // En caso de error en la decodificación, se muestra la imagen por defecto
                holder.binding.ivAvatarSocial.setImageResource(R.drawable.imagen_default_persona)
            }
        }

        /**
         * Al hacer clic en el usuario, se inicia la actividad [PerfilPublicoActivity]
         * pasando el ID del usuario seleccionado.
         */
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, PerfilPublicoActivity::class.java).apply {
                putExtra("USER_ID", usuario.id)
            }
            it.context.startActivity(intent)
        }
    }

    /**
     * Retorna la cantidad de usuarios en la lista.
     */
    override fun getItemCount(): Int = usuarios.size
}