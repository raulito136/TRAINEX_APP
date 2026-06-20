package com.example.trainex.perfil.ajustes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel responsable de la eliminación definitiva del usuario de los servicios de Firebase.
 */
class CuentaViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Estado para notificar el éxito de la eliminación irreversible
    private val _eliminacionExitosa = MutableLiveData<Boolean>()
    val eliminacionExitosa: LiveData<Boolean> get() = _eliminacionExitosa

    // Estado para capturar mensajes de error de los servicios de Firebase
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    /**
     * Ejecuta el proceso de borrado: primero los datos en Firestore y luego el usuario en Authentication.
     */
    fun eliminarCuenta() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                // 1. Eliminar el documento del usuario en la colección "usuarios" de Firestore
                // Se utiliza .await() para esperar la finalización antes de proceder al siguiente paso
                db.collection("usuarios").document(user.uid).delete().await()

                // 2. Eliminar el registro del usuario del sistema de autenticación de Firebase
                user.delete().await()

                // 3. Cerrar sesión por seguridad tras la eliminación
                auth.signOut()
                _eliminacionExitosa.postValue(true)
            } catch (e: Exception) {
                // Notifica cualquier error (ej: falta de permisos o necesidad de reautenticación reciente)
                _error.postValue(e.message)
            }
        }
    }
}