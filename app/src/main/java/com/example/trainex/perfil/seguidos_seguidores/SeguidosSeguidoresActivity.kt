package com.example.trainex.perfil.seguidos_seguidores

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.trainex.R
import com.example.trainex.databinding.ActivitySeguidosSeguidoresBinding
import com.example.trainex.utils.LanguageUtils
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Actividad principal que aloja la visualización de listas sociales (seguidores y seguidos).
 * Utiliza un sistema de pestañas (TabLayout) combinado con un ViewPager2 para permitir
 * el desplazamiento lateral entre ambas listas.
 */
class SeguidosSeguidoresActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeguidosSeguidoresBinding

    /**
     * Inicializa la actividad, configura el soporte para diseño de borde a borde,
     * el adaptador de pestañas y la sincronización entre el ViewPager y el TabLayout.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySeguidosSeguidoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarInterfaz()

        // Recuperar parámetros de navegación
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val tabInicial = intent.getIntExtra("START_TAB", 0)

        // Configuración del ViewPager con su adaptador de fragmentos
        val adaptadorPestanas = AdaptadorPestanasSocial(this, userId)
        binding.viewPagerSocial.adapter = adaptadorPestanas

        // Vinculación de TabLayout con ViewPager2 mediante TabLayoutMediator
        TabLayoutMediator(binding.tabLayoutSocial, binding.viewPagerSocial) { tab, position ->
            tab.text = if (position == 0) getString(R.string.followers) else getString(R.string.following)
        }.attach()

        // Sincronizar dinámicamente el título del Toolbar con la pestaña seleccionada
        binding.viewPagerSocial.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                actualizarTitulo(position)
            }
        })

        // Posicionarse en la pestaña solicitada inicialmente (seguidores o seguidos)
        binding.viewPagerSocial.setCurrentItem(tabInicial, false)
        actualizarTitulo(tabInicial)

        binding.btnBackSocial.setOnClickListener { finish() }
    }

    /**
     * Actualiza el texto de la cabecera según la posición de la pestaña actual.
     * @param position Índice de la pestaña (0 para Seguidores, 1 para Seguidos).
     */
    private fun actualizarTitulo(position: Int) {
        binding.tvTitleSocial.text = if (position == 0)
            getString(R.string.followers) else getString(R.string.following)
    }

    /**
     * Ajusta el relleno de la vista raíz para respetar los insets del sistema (barras de estado y navegación).
     */
    private fun configurarInterfaz() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}