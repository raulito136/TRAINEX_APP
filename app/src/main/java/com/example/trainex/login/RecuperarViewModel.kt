package com.example.trainex.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel encargado de la lógica para la recuperación de contraseñas.
 */
class RecuperarViewModel : ViewModel() {
    // Instancia de Firebase Auth para gestionar las solicitudes de restablecimiento
    private val auth = FirebaseAuth.getInstance()

    // Estado que contiene un par de valores: el mensaje a mostrar y un booleano que indica si es error
    private val _status = MutableLiveData<Pair<String, Boolean>>() // Mensaje y esError
    val status: LiveData<Pair<String, Boolean>> get() = _status

    /**
     * Solicita a Firebase el envío de un correo de recuperación a la dirección proporcionada.
     */
    fun enviarCorreoRecuperacion(email: String) {
        // Validación local: el correo no puede estar vacío
        if (email.isEmpty()) {
            _status.value = "Ingresa tu correo" to true
            return
        }

        // Llamada al servicio de Firebase para enviar el email de restablecimiento
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                // Notifica éxito al usuario
                _status.value = "Correo de recuperación enviado" to false
            }
            .addOnFailureListener {
                // Captura y notifica el error ocurrido (ej: correo no registrado)
                _status.value = (it.message ?: "Error inesperado") to true
            }
    }
}