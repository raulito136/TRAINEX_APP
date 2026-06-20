package com.example.trainex.dietas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.trainex.repository.FirebaseAlimentosRepository

/**
 * Clase Factory encargada de instanciar [AlimentosViewModel].
 * Permite pasar el repositorio de Firebase como una dependencia obligatoria al constructor del ViewModel.
 */
class AlimentosViewModelFactory(private val repository: FirebaseAlimentosRepository) : ViewModelProvider.Factory {

    /**
     * Crea una nueva instancia de la clase ViewModel solicitada.
     * @param modelClass La clase del ViewModel que se desea crear.
     * @return Una instancia del ViewModel configurado con su repositorio.
     * @throws IllegalArgumentException Si la clase proporcionada no es [AlimentosViewModel].
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlimentosViewModel::class.java)) {
            return AlimentosViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}