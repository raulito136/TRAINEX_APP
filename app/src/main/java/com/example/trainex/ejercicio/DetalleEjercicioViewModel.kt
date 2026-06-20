package com.example.trainex.ejercicio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Gestiona el estado de la pantalla de detalles.
 * Al usar LiveData, la Activity se entera automáticamente cuando el ejercicio cambia.
 */
class DetalleEjercicioViewModel : ViewModel() {

    // _ejercicio es privado para que nadie fuera de aquí pueda manipular los datos (encapsulamiento)
    private val _ejercicio = MutableLiveData<Ejercicio>()

    // ejercicio es público e inmutable, solo para lectura desde la Activity
    val ejercicio: LiveData<Ejercicio> get() = _ejercicio

    /**
     * Recibe el ejercicio y lo "emite" a través del LiveData.
     */
    fun setEjercicio(ejercicio: Ejercicio) {
        _ejercicio.value = ejercicio
    }
}