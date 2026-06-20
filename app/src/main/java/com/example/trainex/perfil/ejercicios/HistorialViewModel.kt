package com.example.trainex.historial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.firebase.FirebaseHistorialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona el estado de la UI para la pantalla de historial de ejercicios.
 * Se encarga de la obtención de datos desde Firebase, el filtrado por nombre y la eliminación de duplicados.
 */
class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseHistorialRepository()
    private val context = application.applicationContext

    /** Referencia local a la lista cargada para facilitar el filtrado sin nuevas consultas a red. */
    private var listaCompleta: List<Ejercicio> = emptyList()

    /** Flujo de estado reactivo que emite los diferentes estados de la interfaz (Cargando, Éxito, Error, etc.). */
    private val _uiState = MutableStateFlow<HistorialUiState>(HistorialUiState.Loading)
    val uiState: StateFlow<HistorialUiState> = _uiState

    /**
     * Carga el historial de ejercicios desde Firebase.
     * @param userIdExterno ID del usuario si se consulta un perfil ajeno; de lo contrario, carga el propio.
     */
    fun cargarHistorial(userIdExterno: String?) {
        viewModelScope.launch {
            _uiState.value = HistorialUiState.Loading
            try {
                val listaBruta = if (!userIdExterno.isNullOrEmpty()) {
                    repository.obtenerHistorialPorUsuarioId(userIdExterno)
                } else {
                    repository.obtenerHistorialUsuario()
                }

                /** Elimina duplicados para mostrar solo una entrada por tipo de ejercicio. */
                listaCompleta = listaBruta.distinctBy { it.nombre }

                if (listaCompleta.isEmpty()) {
                    _uiState.value = HistorialUiState.Empty
                } else {
                    _uiState.value = HistorialUiState.Success(listaCompleta)
                }
            } catch (e: Exception) {
                _uiState.value = HistorialUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Filtra la lista actual basándose en el texto introducido por el usuario.
     * Realiza la comparación sobre el nombre traducido del ejercicio.
     */
    fun filtrarLista(texto: String?) {
        if (listaCompleta.isEmpty()) return

        val listaFiltrada = if (texto.isNullOrEmpty()) {
            listaCompleta
        } else {
            listaCompleta.filter { ejercicio ->
                val nombreReal = getStringFromKey(ejercicio.nombre)
                nombreReal.contains(texto, ignoreCase = true)
            }
        }

        if (listaFiltrada.isEmpty()) {
            _uiState.value = HistorialUiState.NoResults
        } else {
            _uiState.value = HistorialUiState.Success(listaFiltrada)
        }
    }

    /**
     * Obtiene el valor traducido de una clave de recurso string.
     */
    private fun getStringFromKey(key: String): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else key
    }

    /**
     * Define los posibles estados de la interfaz de usuario para el historial.
     */
    sealed class HistorialUiState {
        object Loading : HistorialUiState()
        object Empty : HistorialUiState()
        object NoResults : HistorialUiState()
        data class Success(val ejercicios: List<Ejercicio>) : HistorialUiState()
        data class Error(val message: String) : HistorialUiState()
    }
}