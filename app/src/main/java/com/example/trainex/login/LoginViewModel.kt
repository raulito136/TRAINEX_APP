package com.example.trainex.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * ViewModel encargado de gestionar los estados y procesos de inicio de sesión.
 */
class LoginViewModel : ViewModel() {

    // Instancia de FirebaseAuth para interactuar con el servicio de autenticación de Google
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- ESTADOS DE LA UI (Encapsulamiento de LiveData) ---

    // Indica si hay una operación en segundo plano (petición a Firebase)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Almacena el resultado de la autenticación (éxito o error con su excepción)
    private val _loginResult = MutableLiveData<Result<Boolean>>()
    val loginResult: LiveData<Result<Boolean>> get() = _loginResult

    // Identifica si el usuario acaba de registrarse para redirigirlo al formulario de datos
    private val _isNewUser = MutableLiveData<Boolean>()
    val isNewUser: LiveData<Boolean> get() = _isNewUser

    // Almacena mensajes descriptivos de error para mostrar al usuario
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    /**
     * Verifica de forma síncrona si existe una sesión activa en el dispositivo.
     */
    fun usuarioYaAutenticado(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Realiza la autenticación tradicional mediante correo electrónico y contraseña.
     */
    fun loginConEmail(email: String, pass: String) {
        _isLoading.value = true // Activa estado de carga
        _errorMessage.value = null

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                // Notifica éxito en el login
                _loginResult.value = Result.success(true)
                _isLoading.value = false
            }
            .addOnFailureListener {
                // Notifica error y desactiva carga
                _errorMessage.value = "Correo o contraseña incorrectos"
                _loginResult.value = Result.failure(it)
                _isLoading.value = false
            }
    }

    /**
     * Completa el flujo de Google vinculando el ID Token obtenido por la Activity con Firebase.
     */
    fun autenticarEnFirebaseConGoogle(idToken: String) {
        _isLoading.value = true
        // Genera la credencial de Firebase a partir del token de Google
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                // Verifica si es la primera vez que el usuario entra con esta cuenta de Google
                val nuevoUsuario = authResult.additionalUserInfo?.isNewUser == true
                _isNewUser.value = nuevoUsuario
                _loginResult.value = Result.success(true)
                _isLoading.value = false
            }
            .addOnFailureListener {
                _errorMessage.value = "Error al vincular con Google"
                _loginResult.value = Result.failure(it)
                _isLoading.value = false
            }
    }
}