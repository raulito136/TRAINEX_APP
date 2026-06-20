package com.example.trainex.perfil.ajustes

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.trainex.PerfilActivity
import com.example.trainex.R
import com.example.trainex.databinding.ActivityIdiomaBinding
import com.example.trainex.utils.LanguageUtils
import com.example.trainex.utils.TraductorUniversal

/**
 * Activity que permite al usuario cambiar el idioma de la aplicación (Español/Inglés).
 */
class IdiomaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdiomaBinding
    private val viewModel: IdiomaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdiomaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Marcar visualmente el idioma que ya está activo al abrir la pantalla para feedback inicial
        actualizarSeleccionVisual(LanguageUtils.getIdiomaActual(this))

        setupObservers()

        // Configurar clics en los contenedores de los idiomas
        binding.lytIngles.setOnClickListener {
            viewModel.seleccionarIdioma("en")
        }

        binding.lytEspanol.setOnClickListener {
            viewModel.seleccionarIdioma("es")
        }

        binding.lytFrances.setOnClickListener {
            viewModel.seleccionarIdioma("fr")
        }

        binding.lytJapones.setOnClickListener {
            viewModel.seleccionarIdioma("ja")
        }

        binding.lytCatalan.setOnClickListener {
            viewModel.seleccionarIdioma("ca")
        }

        // Configurar el botón de retroceso si existe en el layout
        binding.tvBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Escucha los cambios del ViewModel para reaccionar ante la confirmación del cambio de idioma.
     */
    private fun setupObservers() {
        // Se dispara cuando el ViewModel procesa el cambio y pide reiniciar la app
        viewModel.reiniciarApp.observe(this) { codigo ->
            // Actualizar la UI antes del reinicio
            actualizarSeleccionVisual(codigo)

            // Sincronizar el motor de traducción dinámico (si se usa ML Kit u otro)
            TraductorUniversal.configurarIdioma(codigo)

            // Para aplicar un cambio de idioma en Android, es necesario reiniciar las actividades.
            // Creamos un Intent hacia la pantalla principal limpiando el stack de actividades.
            val intent = Intent(this, PerfilActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish() // Cerrar esta actividad
        }
    }

    /**
     * Gestiona el aspecto visual de la lista de idiomas para indicar cuál está seleccionado.
     * Muestra checks y cambia fondos según el código ("es" o "en").
     */
    private fun actualizarSeleccionVisual(codigo: String) {
        // Reset all views
        binding.ivCheckEspanol.visibility = android.view.View.GONE
        binding.ivCheckIngles.visibility = android.view.View.GONE
        binding.ivCheckFrances.visibility = android.view.View.GONE
        binding.ivCheckJapones.visibility = android.view.View.GONE
        binding.ivCheckCatalan.visibility = android.view.View.GONE
        binding.lytEspanol.setBackgroundResource(0)
        binding.lytIngles.setBackgroundResource(0)
        binding.lytFrances.setBackgroundResource(0)
        binding.lytJapones.setBackgroundResource(0)
        binding.lytCatalan.setBackgroundResource(0)

        when (codigo) {
            "es" -> {
                binding.ivCheckEspanol.visibility = android.view.View.VISIBLE
                binding.lytEspanol.setBackgroundResource(R.drawable.bg_ajustes_item)
            }
            "en" -> {
                binding.ivCheckIngles.visibility = android.view.View.VISIBLE
                binding.lytIngles.setBackgroundResource(R.drawable.bg_ajustes_item)
            }
            "fr" -> {
                binding.ivCheckFrances.visibility = android.view.View.VISIBLE
                binding.lytFrances.setBackgroundResource(R.drawable.bg_ajustes_item)
            }
            "ja" -> {
                binding.ivCheckJapones.visibility = android.view.View.VISIBLE
                binding.lytJapones.setBackgroundResource(R.drawable.bg_ajustes_item)
            }
            "ca" -> {
                binding.ivCheckCatalan.visibility = android.view.View.VISIBLE
                binding.lytCatalan.setBackgroundResource(R.drawable.bg_ajustes_item)
            }
        }
    }
}