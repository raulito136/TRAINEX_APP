package com.example.trainex.perfil.buscador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.perfil.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** ViewModel que gestiona la lógica de búsqueda y filtrado de usuarios mediante Firebase */
class BuscadorUsuariosViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val usuarioRepo = FirebaseUsuarioRepository()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    /** Flujo de estado para la lista de usuarios mostrada en la UI */
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    /** Almacén temporal para los seguidos para restaurar la lista cuando el buscador esté vacío */
    private var listaSeguidos: List<Usuario> = emptyList()

    /** Obtiene y filtra la lista de usuarios seguidos por el usuario actual */
    fun cargarSeguidos() {
        viewModelScope.launch {
            try {
                val seguidos = usuarioRepo.obtenerUsuariosSeguidos()
                /** Filtra para evitar que el propio usuario aparezca en su lista de seguidos */
                listaSeguidos = seguidos.filter { it.id != currentUserId }
                _usuarios.value = listaSeguidos
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Realiza una consulta a Firestore basada en el nombre de usuario escrito */
    fun buscarUsuarios(texto: String) {
        if (texto.isEmpty()) {
            _usuarios.value = listaSeguidos
            return
        }

        db.collection("usuarios")
            .orderBy("username")
            .startAt(texto)
            .endAt(texto + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                val listaCompleta = documents.toObjects(Usuario::class.java)

                /** Asigna IDs de documento y filtra al usuario actual para que no se encuentre a sí mismo */
                val listaFiltrada = listaCompleta.mapIndexedNotNull { index, user ->
                    val docId = documents.documents[index].id
                    if (user.id.isEmpty()) user.id = docId

                    if (user.id == currentUserId) null else user
                }

                _usuarios.value = listaFiltrada
            }
            .addOnFailureListener { it.printStackTrace() }
    }
}