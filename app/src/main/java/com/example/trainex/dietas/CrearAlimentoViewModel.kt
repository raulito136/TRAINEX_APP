package com.example.trainex.dietas

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.example.trainex.repository.FirebaseAlimentosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la lógica de creación de alimentos personalizados.
 * Maneja la interacción con el repositorio de Firebase en hilos secundarios.
 */
class CrearAlimentoViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseRepo = FirebaseAlimentosRepository(application)

    /** Estado que indica si el proceso de guardado se completó con éxito. */
    private val _estadoGuardado = MutableLiveData<Boolean>()
    val estadoGuardado: LiveData<Boolean> get() = _estadoGuardado

    /** Almacena mensajes de error ocurridos durante la operación de guardado. */
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    /**
     * Ejecuta el guardado de un alimento en Firebase Firestore y Storage.
     * @param alimento Objeto con la información nutricional y metadatos.
     * @param imageUri URI de la imagen seleccionada por el usuario (opcional).
     */
    fun guardarAlimento(alimento: Alimento, imageUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firebaseRepo.crearAlimentoGlobal(alimento, imageUri)
                _estadoGuardado.postValue(true)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error desconocido")
            }
        }
    }
}