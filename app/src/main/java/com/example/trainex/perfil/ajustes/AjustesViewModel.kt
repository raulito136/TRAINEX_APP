package com.example.trainex.perfil.ajustes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trainex.database.AppDatabase
import com.example.trainex.utils.SharedPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica de cierre de sesión y limpieza de la base de datos local.
 */
class AjustesViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val sharedPrefs = SharedPreferencesManager(application)
    // Evento que notifica a la Activity si el cierre de sesión fue exitoso
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> get() = _logoutEvent

    /**
     * Realiza el cierre de sesión de Firebase y elimina los datos locales de Room.
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            try {
                // Obtención de la base de datos local
                val db = AppDatabase.getDatabase(getApplication())

                // Limpieza de datos locales antes de salir para evitar rastro de datos de otro usuario
                db.diarioDao().eliminarTodoElHistorial()
                db.serieDao().eliminarTodoElHistorial()

                sharedPrefs.clear()

                // Cierre de sesión en Firebase Auth
                auth.signOut()
                _logoutEvent.value = true
            } catch (e: Exception) {
                // Notifica fallo en caso de excepción
                _logoutEvent.value = false
            }
        }
    }
}