package com.example.trainex.perfil.seguidos_seguidores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Fragmento especializado en mostrar un listado vertical de usuarios.
 * Reutiliza la misma lógica para mostrar tanto seguidores como seguidos basándose en un "modo".
 */
class ListaUsuariosFragment : Fragment() {

    // Inyectamos el ViewModel usando el delegado 'viewModels()'
    private val viewModel: ListaUsuariosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView

    companion object {
        private const val ARG_UID = "uid"
        private const val ARG_MODO = "modo"

        /**
         * Crea una instancia del fragmento pasando los argumentos necesarios de forma segura.
         * * @param userId El ID del usuario a consultar.
         * @param modo El tipo de lista (0 para Seguidores, 1 para Seguidos).
         */
        fun nuevaInstancia(userId: String, modo: Int): ListaUsuariosFragment {
            return ListaUsuariosFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, userId)
                    putInt(ARG_MODO, modo)
                }
            }
        }
    }

    /**
     * Crea programáticamente la vista del RecyclerView para ocupar todo el espacio disponible.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(context)
        }
        return recyclerView
    }

    /**
     * Configura el observador del ViewModel y dispara la carga de datos tras crear la vista.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString(ARG_UID) ?: return
        val modo = arguments?.getInt(ARG_MODO) ?: 0

        // 1. Observar los cambios en la lista de usuarios y actualizar el adaptador
        viewModel.usuarios.observe(viewLifecycleOwner) { lista ->
            recyclerView.adapter = SeguidosSeguidoresAdapter(lista)
        }

        // 2. Solicitar al ViewModel la carga de datos desde el repositorio
        viewModel.cargarUsuarios(userId, modo)
    }
}