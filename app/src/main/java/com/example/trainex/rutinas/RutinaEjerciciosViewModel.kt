package com.example.trainex.rutinas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel que gestiona la información de la rutina seleccionada para visualizar sus ejercicios.
 * Proporciona el estado de la rutina a la vista de manera reactiva.
 */
class RutinaEjerciciosViewModel : ViewModel() {
    private val _rutina = MutableLiveData<Rutina>()
    /**
     * LiveData que expone la información de la [Rutina] actual a la actividad.
     */
    val rutina: LiveData<Rutina> get() = _rutina

    /**
     * Establece la rutina que se desea mostrar en la pantalla de detalles.
     * @param rutina El objeto de tipo [Rutina] con sus ejercicios asociados.
     */
    fun setRutina(rutina: Rutina) {
        _rutina.value = rutina
    }
}