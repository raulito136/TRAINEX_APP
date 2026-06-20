package com.example.trainex.perfil.ajustes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainex.databinding.ActivityCambiarContrasenaBinding
import com.example.trainex.utils.LanguageUtils

/**
 * Actividad que gestiona el cambio de contraseña del usuario.
 */
class CambiarContrasenaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCambiarContrasenaBinding
    // ViewModel que contiene la lógica de validación con Firebase Auth
    private val viewModel: CambiarCuentaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupClickListeners()
        setupObservers()
    }

    /**
     * Observa el estado de la operación desde el ViewModel.
     */
    private fun setupObservers() {
        viewModel.status.observe(this) { (mensaje, esError) ->
            mensaje?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                // Si no hay error, se cierra la actividad (operación exitosa)
                if (!esError) finish()
            }
        }
    }

    /**
     * Configura los eventos de clic, capturando los datos de los campos de texto.
     */
    private fun setupClickListeners() {
        binding.tvBack.setOnClickListener { finish() }

        binding.btnGuardarContrasena.setOnClickListener {
            val actual = binding.etContrasenaActual.text.toString().trim()
            val nueva = binding.etNuevaContrasena.text.toString().trim()
            val confirmar = binding.etConfirmarContrasena.text.toString().trim()

            // Solo procede si las validaciones locales son exitosas
            if (validarCampos(actual, nueva, confirmar)) {
                viewModel.actualizarContrasena(actual, nueva)
            }
        }
    }

    /**
     * Valida que los campos cumplan con los requisitos mínimos de seguridad y formato.
     */
    private fun validarCampos(actual: String, nueva: String, confirmar: String): Boolean {
        var valido = true
        // Verifica que se haya ingresado la contraseña actual
        if (actual.isEmpty()) {
            binding.etContrasenaActual.error = getString(com.example.trainex.R.string.error_contrasena_actual_obligatoria_ajustes_cuenta)
            valido = false
        }
        // Verifica que la nueva contraseña tenga al menos 6 caracteres
        if (nueva.length < 6) {
            binding.etNuevaContrasena.error = getString(com.example.trainex.R.string.error_contrasena_corta)
            valido = false
        }
        // Verifica que la nueva contraseña coincida con la confirmación
        if (nueva != confirmar) {
            binding.etConfirmarContrasena.error = getString(com.example.trainex.R.string.error_contrasenas_no_coinciden)
            valido = false
        }
        return valido
    }

    /**
     * Configura los insets para el diseño "edge-to-edge".
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}