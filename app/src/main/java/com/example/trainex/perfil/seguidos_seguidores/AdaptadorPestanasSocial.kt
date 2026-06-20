package com.example.trainex.perfil.seguidos_seguidores

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adaptador para el ViewPager2 que gestiona las pestañas de la sección social.
 * Se encarga de alternar entre la lista de seguidores y la de seguidos.
 * * @param activity La actividad donde se hospeda el adaptador.
 * @property uid Identificador único del usuario del que se consultarán los datos.
 */
class AdaptadorPestanasSocial(
    activity: AppCompatActivity,
    private val uid: String
) : FragmentStateAdapter(activity) {

    /**
     * Define el número total de pestañas (0: Seguidores, 1: Seguidos).
     */
    override fun getItemCount(): Int = 2

    /**
     * Crea una nueva instancia del Fragment correspondiente según la posición de la pestaña.
     * * @param position Índice de la pestaña actual.
     * @return Una instancia de [ListaUsuariosFragment] configurada con el UID y el modo.
     */
    override fun createFragment(position: Int): Fragment {
        return ListaUsuariosFragment.nuevaInstancia(uid, position)
    }
}