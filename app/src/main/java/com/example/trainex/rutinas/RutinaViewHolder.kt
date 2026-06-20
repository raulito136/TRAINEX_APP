package com.example.trainex.rutinas

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.R
import com.example.trainex.rutinas.Rutina
import com.example.trainex.databinding.ItemRutinaBinding

/**
 * ViewHolder para mostrar cada rutina en la lista de rutinas guardadas.
 * Usa el layout item_rutina.xml para mostrar el título y número de ejercicios.
 */
class RutinaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemRutinaBinding.bind(view)

    // Obtenemos el contexto de la vista del elemento para poder acceder a los recursos
    private val context: Context = view.context

    /**
     * Renderiza los datos de una rutina en la vista.
     * Muestra el nombre (título) de la rutina traducido y la cantidad de ejercicios.
     * Configura el listener de clic para abrir el detalle de la rutina.
     * @param rutina La rutina a mostrar
     * @param onRutinaClick Callback que se ejecuta al hacer clic en la rutina
     * @param onEditarClick Callback que se ejecuta al hacer clic en editar
     * @param onEliminarClick Callback que se ejecuta al hacer clic en eliminar
     */
    fun render(
        rutina: Rutina,
        onRutinaClick: (Rutina) -> Unit,
        onEditarClick: (Rutina) -> Unit,
        onEliminarClick: (Rutina) -> Unit
    ) {
        binding.tvTituloRutina.text =  rutina.titulo


        binding.tvNumeroEjercicios.text = "${rutina.getCantidadEjercicios()} ${context.getString(R.string.ejercicios)}"


        // Configurar el clic en el item completo
        itemView.setOnClickListener {
            onRutinaClick(rutina)
        }

        // Configurar el menú de opciones usando el ivMenu que ya existe
        binding.ivMenu.setOnClickListener { view ->
            mostrarMenuOpciones(view, rutina, onEditarClick, onEliminarClick)
        }
    }

    /**
     * Muestra el menú popup con las opciones de editar y eliminar.
     * @param view Vista desde donde se muestra el menú
     * @param rutina Rutina sobre la que se aplican las opciones
     * @param onEditarClick Callback para editar
     * @param onEliminarClick Callback para eliminar
     */
    private fun mostrarMenuOpciones(
        view: View,
        rutina: Rutina,
        onEditarClick: (Rutina) -> Unit,
        onEliminarClick: (Rutina) -> Unit
    ) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_rutina, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_editar -> {
                    onEditarClick(rutina)
                    true
                }
                R.id.menu_eliminar -> {
                    onEliminarClick(rutina)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}