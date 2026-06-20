package com.example.trainex.perfil.seguidos_seguidores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.perfil.Usuario
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica de obtención de datos para las listas sociales.
 * Se comunica con el repositorio de Firebase para traer los perfiles de usuario.
 */
class ListaUsuariosViewModel : ViewModel() {

    private val repositorio = FirebaseUsuarioRepository()

    // Estado interno y público de la lista de usuarios
    private val _usuarios = MutableLiveData<List<Usuario>>()
    /** LiveData que expone la lista de usuarios cargada para ser observada por la UI. */
    val usuarios: LiveData<List<Usuario>> get() = _usuarios

    // Estado de carga para gestionar indicadores visuales en la UI
    private val _estaCargando = MutableLiveData<Boolean>()
    /** LiveData que indica si hay una operación de red en curso. */
    val estaCargando: LiveData<Boolean> get() = _estaCargando

    /**
     * Carga la lista de usuarios según el rol social especificado.
     * * @param userId ID del usuario objetivo.
     * @param modo Determina si se cargan seguidores (0) o seguidos (1).
     */
    fun cargarUsuarios(userId: String, modo: Int) {
        if (_usuarios.value != null) return // Evita recargar si ya hay datos en memoria para ahorrar ancho de banda

        _estaCargando.value = true
        viewModelScope.launch {
            try {
                val resultado = if (modo == 0) {
                    repositorio.obtenerListaSeguidores(userId)
                } else {
                    repositorio.obtenerListaSeguidos(userId)
                }
                _usuarios.postValue(resultado)
            } catch (e: Exception) {
                // En caso de fallo, se retorna una lista vacía para evitar bloqueos en la UI
                _usuarios.postValue(emptyList())
            } finally {
                _estaCargando.value = false
            }
        }
    }
}