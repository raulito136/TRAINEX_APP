package com.example.trainex.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.trainex.R
import com.example.trainex.databinding.ActivityLoginBinding
import com.example.trainex.formulario.registro.FormulariosRegistroActivity
import com.example.trainex.rutinas.Entrenamiento
import com.example.trainex.utils.LanguageUtils
import com.example.trainex.utils.ModoUtils
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Pantalla principal de inicio de sesión que soporta Email/Password y Google Sign-In.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicialización de configuraciones de apariencia e idioma guardadas en SharedPreferences
        ModoUtils.aplicarModoGuardado(this)
        LanguageUtils.aplicarIdioma(this)

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificación automática de sesión al arrancar la actividad
        if (viewModel.usuarioYaAutenticado()) {
            irAEntrenamiento()
        }

        setupListeners()
        setupObservers()
    }

    /**
     * Configuración de eventos de clic en los elementos de la interfaz.
     */
    private fun setupListeners() {
        // Intento de login manual
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validarCampos(email, password)) {
                viewModel.loginConEmail(email, password)
            }
        }

        // Lanzamiento del selector de cuentas de Google
        binding.btnGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }

        // Navegación a recuperación de cuenta
        binding.tvOlvidadoContrasena.setOnClickListener {
            startActivity(Intent(this, RecuperarContrasenaActivity::class.java))
        }

        // Navegación al registro manual
        binding.tvRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Suscripción a los datos expuestos por el LoginViewModel.
     */
    private fun setupObservers() {
        // Bloquea o desbloquea el botón según si hay una petición en curso
        viewModel.isLoading.observe(this) { loading ->
            binding.btnLogin.isEnabled = !loading
        }

        // Muestra mensajes de error provenientes de Firebase (ej: contraseña incorrecta)
        viewModel.errorMessage.observe(this) { msg ->
            msg?.let {
                binding.tvError.text = it
                binding.tvError.visibility = View.VISIBLE
            }
        }

        // Escucha el resultado exitoso para navegar a la pantalla principal
        viewModel.loginResult.observe(this) { result ->
            if (result.isSuccess && viewModel.isNewUser.value != true) {
                irAEntrenamiento()
            }
        }

        // Caso especial: Si el usuario es nuevo tras usar Google, debe completar su perfil primero
        viewModel.isNewUser.observe(this) { isNew ->
            if (isNew) {
                Toast.makeText(this, "Cuenta creada. Completa tus datos.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, FormulariosRegistroActivity::class.java))
                finish()
            }
        }
    }

    /**
     * Validación sintáctica de los campos de entrada.
     */
    private fun validarCampos(email: String, pass: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            binding.etEmail.error = "Ingresa tu correo"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Correo inválido"
            isValid = false
        }
        if (pass.isEmpty()) {
            binding.etPassword.error = "Ingresa tu contraseña"
            isValid = false
        }
        return isValid
    }

    /**
     * Implementación de Google Identity mediante Credential Manager.
     * Se ejecuta en un coroutine scope por ser una operación de red suspendida.
     */
    private fun iniciarSesionGoogle() {
        lifecycleScope.launch {
            try {
                // Configuración de la petición de Google ID
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Permite ver todas las cuentas, no solo las ya usadas
                    .setServerClientId(getString(R.string.default_web_client_id)) // ID de cliente Web de Firebase
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // Llamada al gestor de credenciales de Android
                val credentialManager = CredentialManager.create(this@LoginActivity)
                val result = credentialManager.getCredential(this@LoginActivity, request)

                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("GoogleSign", "Error: ${e.message}")
            }
        }
    }

    /**
     * Procesa la respuesta del Credential Manager y extrae el token necesario para Firebase.
     */
    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            // Envía el token al ViewModel para la fase de Firebase Auth
            viewModel.autenticarEnFirebaseConGoogle(googleIdTokenCredential.idToken)
        }
    }

    /**
     * Limpia la pila de navegación y lleva al usuario al flujo de entrenamiento.
     */
    private fun irAEntrenamiento() {
        val intent = Intent(this, Entrenamiento::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}