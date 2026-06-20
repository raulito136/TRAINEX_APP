package com.example.trainex.perfil.ajustes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

/**
 * ViewModel que gestiona operaciones sensibles de la cuenta como cambio de contraseña y email.
 */
class CambiarCuentaViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // LiveData que reporta el mensaje de estado y si este representa un error
    private val _status = MutableLiveData<Pair<String?, Boolean>>() // Mensaje y esError
    val status: LiveData<Pair<String?, Boolean>> get() = _status

    /**
     * Actualiza la contraseña del usuario tras verificar sus credenciales actuales.
     */
    fun actualizarContrasena(actual: String, nueva: String) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(user.email!!, actual)

        user.reauthenticate(credential).addOnCompleteListener { reauth ->
            if (reauth.isSuccessful) {
                user.updatePassword(nueva).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _status.value = "Contraseña actualizada" to false
                    } else {
                        // Capturamos el error real de Firebase
                        val errorMsg = task.exception?.localizedMessage ?: "Error desconocido"
                        _status.value = "Error al actualizar: $errorMsg" to true
                    }
                }
            } else {
                _status.value = "Contraseña actual incorrecta" to true
            }
        }
    }

    /**
     * Actualiza el correo electrónico del usuario tras validar su identidad.
     */
    fun actualizarEmail(nuevoEmail: String, pass: String) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(user.email!!, pass)

        user.reauthenticate(credential).addOnCompleteListener { reauth ->
            if (reauth.isSuccessful) {
                user.updateEmail(nuevoEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _status.value = "Email actualizado" to false
                    } else {
                        // Traducimos los errores más comunes de Firebase o mostramos el real
                        val exception = task.exception
                        val mensajeError = when (exception) {
                            is FirebaseAuthUserCollisionException -> "El email ya está en uso por otra cuenta."
                            is FirebaseAuthInvalidCredentialsException -> "El formato del correo es inválido."
                            else -> exception?.localizedMessage ?: "Error al actualizar el email."
                        }
                        _status.value = "Error: $mensajeError" to true
                    }
                }
            } else {
                _status.value = "Contraseña incorrecta" to true
            }
        }
    }
}