package com.example.trainex.perfil.ajustes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainex.databinding.ActivityCambiarNombreUsuarioBinding
import com.example.trainex.utils.LanguageUtils

/**
 * Actividad encargada de la actualización del nombre de usuario público.
 */
class CambiarNombreUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCambiarNombreUsuarioBinding
    private val viewModel: CambiarNombreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarNombreUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupObservers()
        setupClickListeners()

        // Carga el nombre de usuario actual al iniciar la pantalla
        viewModel.cargarNombre()
    }

    /**
     * Suscripción a los datos de nombre actual y resultados de la operación de cambio.
     */
    private fun setupObservers() {
        // Actualiza el texto con el nombre actual recuperado del perfil
        viewModel.nombreActual.observe(this) {
            binding.tvNombreUsuarioActual.text = it
        }

        // Gestiona el resultado del intento de cambio de nombre
        viewModel.resultado.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, getString(com.example.trainex.R.string.mensaje_nombre_actualizado), Toast.LENGTH_SHORT).show()
                finish() // Cierra la pantalla en caso de éxito
            }.onFailure {
                // Muestra el error directamente en el campo de texto (ej: "Nombre ya en uso")
                binding.etNuevoNombreUsuario.error = it.message
            }
        }
    }

    /**
     * Configura los botones de la interfaz.
     */
    private fun setupClickListeners() {
        binding.tvBack.setOnClickListener { finish() }

        binding.btnGuardarNombre.setOnClickListener {
            val nuevo = binding.etNuevoNombreUsuario.text.toString().trim()
            if (nuevo.isNotEmpty()) {
                // Solicita el cambio al ViewModel para validar disponibilidad
                viewModel.intentarCambioNombre(nuevo)
            } else {
                binding.etNuevoNombreUsuario.error = getString(com.example.trainex.R.string.error_nombre_usuario_vacio)
            }
        }
    }

    /**
     * Aplicación de insets para el diseño del sistema.
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}