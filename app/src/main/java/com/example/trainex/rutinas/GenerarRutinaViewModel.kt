package com.example.trainex.rutinas

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel responsable de la lógica de validación del formulario de generación de rutinas.
 * Centraliza las reglas de negocio para asegurar que los datos enviados a la IA sean correctos.
 */
class GenerarRutinaViewModel : ViewModel() {

    private val _errorEdad = MutableLiveData<String?>()
    /** LiveData que expone mensajes de error relacionados con la edad. */
    val errorEdad: LiveData<String?> get() = _errorEdad

    private val _navegacionEvent = MutableLiveData<Bundle?>()
    /** LiveData que encapsula los datos validados para disparar la navegación en la vista. */
    val navegacionEvent: LiveData<Bundle?> get() = _navegacionEvent

    /**
     * Valida los datos del formulario. Si son correctos, prepara un Bundle y notifica la navegación.
     * * @param edadStr Cadena de texto que representa la edad.
     * @param dias Frecuencia semanal seleccionada.
     * @param objetivo Meta fitness seleccionada.
     * @param experiencia Nivel de experiencia del usuario.
     */
    fun validarYEnviar(edadStr: String, dias: String, objetivo: String, experiencia: String) {
        val edadInt = edadStr.toIntOrNull()

        // Regla: La edad debe ser un número válido
        if (edadInt == null) {
            _errorEdad.value = "Debes ingresar un número"
            return
        }

        // Regla: Rango de edad permitido entre 12 y 100 años
        if (edadInt < 12 || edadInt > 100) {
            _errorEdad.value = "Introduce una edad válida (12-100)"
            return
        }

        // Si la validación es exitosa, se limpian errores y se genera el evento de éxito
        _errorEdad.value = null
        val bundle = Bundle().apply {
            putString("edad", edadStr)
            putString("dias", dias)
            putString("objetivo", objetivo)
            putString("experiencia", experiencia)
        }
        _navegacionEvent.value = bundle
    }
}