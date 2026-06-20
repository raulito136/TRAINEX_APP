package com.example.trainex.perfil.ajustes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainex.R
import com.example.trainex.databinding.ActivityModoBinding
import com.example.trainex.utils.LanguageUtils

/**
 * Actividad que permite al usuario alternar entre el Modo Oscuro y el Modo Claro.
 */
class ModoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModoBinding
    private val viewModel: ModoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityModoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.lytOscuro.setOnClickListener { viewModel.cambiarModo(0) }
        binding.lytClaro.setOnClickListener { viewModel.cambiarModo(1) }

        binding.tvBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Se suscribe a los cambios de estado en el ViewModel.
     */
    private fun setupObservers() {
        viewModel.modoActual.observe(this) { modo ->
            actualizarCheck(modo)
        }
    }

    /**
     * Gestiona la visibilidad de los indicadores (checks) según la elección del usuario.
     * @param modo 0 para Oscuro, 1 para Claro.
     */
    private fun actualizarCheck(modo: Int) {
        binding.ivCheckOscuro.visibility = if (modo == 0) View.VISIBLE else View.GONE
        binding.ivCheckClaro.visibility = if (modo == 1) View.VISIBLE else View.GONE
    }
}