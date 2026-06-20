package com.example.trainex.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.trainex.databinding.ActivityRegisterBinding
import com.example.trainex.formulario.registro.FormulariosRegistroActivity
import com.example.trainex.utils.LanguageUtils

/**
 * Actividad que gestiona la vista de registro de nuevos usuarios.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Configuración del idioma según las preferencias del usuario
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    /**
     * Define las acciones al interactuar con los botones de la interfaz.
     */
    private fun setupListeners() {
        // Al intentar registrarse, se envían los datos al ViewModel
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()

            viewModel.registrarUsuario(email, pass, confirm)
        }

        // Permite volver a la pantalla de login cerrando la actual
        binding.tvBackToLogin.setOnClickListener { finish() }
    }

    /**
     * Observa los cambios de estado en el RegisterViewModel.
     */
    private fun setupObservers() {
        // Escucha el resultado del proceso de creación de cuenta
        viewModel.registerResult.observe(this) { result ->
            if (result.isSuccess) {
                Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                // Si el registro es exitoso, redirige al flujo de formularios iniciales
                startActivity(Intent(this, FormulariosRegistroActivity::class.java))
                finish()
            }
        }

        // Muestra mensajes de error (validaciones o fallos de Firebase) mediante Toasts
        viewModel.errorMessage.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }
    }
}