package com.example.trainex.perfil.ajustes

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trainex.firebase.FirebaseFotosRepository
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.perfil.UserManager
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica de negocio para la edición del perfil,
 * aislando la actividad de las llamadas a repositorios y procesamiento de imágenes.
 */
class EditarPerfilViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios para interactuar con Firebase (Usuario y almacenamiento de imágenes)
    private val usuarioRepo = FirebaseUsuarioRepository()
    private val fotosRepo = FirebaseFotosRepository(application)

    // LiveData privado para actualizar el estado de la foto; público para la Activity
    private val _fotoActualizada = MutableLiveData<String>()
    val fotoActualizada: LiveData<String> get() = _fotoActualizada

    /**
     * Procesa la imagen seleccionada: reduce su tamaño, la convierte a Base64
     * y la sube a la base de datos de Firebase.
     */
    fun subirFoto(uri: Uri) {
        viewModelScope.launch {
            // 1. Obtener un Bitmap optimizado (reducido) para no saturar la memoria/DB
            val bitmap = fotosRepo.obtenerBitmapReducido(uri)

            bitmap?.let {
                // 2. Convertir el Bitmap a una cadena de texto Base64
                val base64 = fotosRepo.convertirBitmapABase64(it)

                // 3. Intentar actualizar la foto en el repositorio remoto (Firebase)
                if (usuarioRepo.actualizarFotoPerfil(base64)) {

                    // 4. Si la subida fue exitosa, actualizar el caché local en UserManager
                    val perfil = UserManager.getUserProfile(getApplication())
                    UserManager.saveUserData(getApplication(), mapOf("foto" to base64))

                    // 5. Notificar a la Activity para que refresque la imagen en pantalla
                    _fotoActualizada.value = base64
                }
            }
        }
    }
}