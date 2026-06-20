package com.example.trainex.login

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.trainex.databinding.ActivityRecuperarContrasenaBinding
import com.example.trainex.utils.LanguageUtils
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla para el envío de correos electrónicos de recuperación de contraseña.
 */
class RecuperarContrasenaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarContrasenaBinding

    // ViewModel asociado (Se asume una lógica similar de llamada a auth.sendPasswordResetEmail)
    private val viewModel: RecuperarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar idioma antes de inflar la vista
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    /**
     * Configuración de interacciones.
     */
    private fun setupListeners() {
        // Al pulsar enviar, se solicita la recuperación a través del ViewModel
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            viewModel.enviarCorreoRecuperacion(email)
        }

        // Botón para volver atrás o cerrar la actividad
        binding.tvSwitchMode.setOnClickListener { finish() }
    }

    /**
     * Observa el resultado del envío del correo.
     */
    private fun setupObservers() {
        // Observa un Pair o data class que contiene el mensaje y un booleano de error
        viewModel.status.observe(this) { (mensaje, esError) ->
            binding.tvErrorMessage.apply {
                text = mensaje
                // Cambia dinámicamente el color del texto (Rojo para error, Verde para éxito)
                setTextColor(if (esError) 0xFFFF4444.toInt() else 0xFF44FF44.toInt())
                visibility = View.VISIBLE
            }
        }
    }
}