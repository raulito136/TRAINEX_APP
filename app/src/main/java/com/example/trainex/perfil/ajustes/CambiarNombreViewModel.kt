package com.example.trainex.perfil.ajustes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.firebase.FirebaseUsuarioRepository
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica de verificación y actualización del nombre de usuario.
 */
class CambiarNombreViewModel : ViewModel() {
    // Repositorio para interactuar con los datos del usuario en Firebase
    private val usuarioRepo = FirebaseUsuarioRepository()

    // Almacena el nombre de usuario actual para mostrarlo en la UI
    private val _nombreActual = MutableLiveData<String>()
    val nombreActual: LiveData<String> get() = _nombreActual

    // Notifica el resultado de la operación (éxito con el nuevo nombre o fallo con excepción)
    private val _resultado = MutableLiveData<Result<String>>()
    val resultado: LiveData<Result<String>> get() = _resultado

    /**
     * Recupera de forma asíncrona el nombre de usuario actual desde el repositorio.
     */
    fun cargarNombre() {
        viewModelScope.launch {
            // Si se encuentra el usuario, se actualiza el LiveData del nombre actual
            usuarioRepo.obtenerUsuario()?.let { _nombreActual.value = it.username }
        }
    }

    /**
     * Verifica la disponibilidad del nuevo nombre y lo guarda si no existe duplicado.
     */
    fun intentarCambioNombre(nuevoNombre: String) {
        viewModelScope.launch {
            // 1. Verificar si el nombre ya está registrado por otro usuario
            if (usuarioRepo.existeNombreUsuario(nuevoNombre)) {
                _resultado.value = Result.failure(Exception("El nombre ya existe"))
                return@launch
            }

            // 2. Si está disponible, se obtiene el perfil actual y se actualiza el campo username
            usuarioRepo.obtenerUsuario()?.let {
                usuarioRepo.guardarUsuario(it.copy(username = nuevoNombre))
                _resultado.value = Result.success(nuevoNombre)
            }
        }
    }
}