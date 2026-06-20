package com.example.trainex.perfil.ajustes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainex.databinding.ActivityCambiarEmailBinding
import com.example.trainex.utils.LanguageUtils
import com.google.firebase.auth.FirebaseAuth

/**
 * Actividad para cambiar el correo electrónico del usuario.
 */
class CambiarEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCambiarEmailBinding
    // Comparte el ViewModel con la actividad de cambiar contraseña por tener lógica similar
    private val viewModel: CambiarCuentaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar localización de idioma
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupClickListeners()
        setupObservers()

        // Muestra el correo electrónico actual del usuario autenticado en Firebase
        binding.tvEmailActual.text = FirebaseAuth.getInstance().currentUser?.email
    }

    /**
     * Observa el estado del flujo de actualización (éxito o error).
     */
    private fun setupObservers() {
        viewModel.status.observe(this) { (mensaje, esError) ->
            mensaje?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                // Si la operación fue exitosa, cierra la actividad
                if (!esError) finish()
            }
        }
    }

    /**
     * Configura los listeners de los botones.
     */
    private fun setupClickListeners() {
        binding.tvBack.setOnClickListener { finish() }

        binding.btnGuardarEmail.setOnClickListener {
            val nuevoEmail = binding.etNuevoEmail.text.toString().trim()
            val pass = binding.etContrasenaActual.text.toString().trim()

            // Validación simple antes de enviar al ViewModel
            if (nuevoEmail.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.actualizarEmail(nuevoEmail, pass)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Configura los márgenes del sistema para diseño edge-to-edge.
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}