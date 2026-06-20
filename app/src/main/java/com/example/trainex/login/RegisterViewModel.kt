package com.example.trainex.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel que procesa la creación de nuevas cuentas de usuario.
 */
class RegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // Controla el estado de carga para la UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Notifica el éxito o fallo de la operación de registro
    private val _registerResult = MutableLiveData<Result<Boolean>>()
    val registerResult: LiveData<Result<Boolean>> get() = _registerResult

    // Almacena el mensaje de error para ser mostrado en la Activity
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    /**
     * Ejecuta las validaciones de negocio y procede con la creación del usuario en Firebase.
     */
    fun registrarUsuario(email: String, pass: String, confirmPass: String) {
        // 1. Validaciones de lógica de negocio: Asegura integridad antes de llamar a red
        if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            _errorMessage.value = "Por favor completa todos los campos"
            return
        }
        if (pass != confirmPass) {
            _errorMessage.value = "Las contraseñas no coinciden"
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        // 2. Llamada asíncrona a Firebase para crear la cuenta
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _registerResult.value = Result.success(true)
                _isLoading.value = false
            }
            .addOnFailureListener {
                // Devuelve el error de Firebase (ej: el usuario ya existe)
                _errorMessage.value = it.message ?: "Error al crear la cuenta"
                _registerResult.value = Result.failure(it)
                _isLoading.value = false
            }
    }
}