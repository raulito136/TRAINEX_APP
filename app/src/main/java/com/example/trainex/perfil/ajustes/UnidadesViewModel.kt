package com.example.trainex.perfil.ajustes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.trainex.utils.UnitManager

/**
 * ViewModel que conecta la interfaz de Unidades con el gestor de preferencias UnitManager.
 */
class UnidadesViewModel(application: Application) : AndroidViewModel(application) {

    // LiveDatas para cada tipo de unidad, permitiendo una UI reactiva
    private val _peso = MutableLiveData<String>()
    val peso: LiveData<String> get() = _peso

    private val _distancia = MutableLiveData<String>()
    val distancia: LiveData<String> get() = _distancia

    private val _medida = MutableLiveData<String>()
    val medida: LiveData<String> get() = _medida

    init {
        // Cargar los valores guardados inmediatamente al instanciar el ViewModel
        cargarPreferencias()
    }

    /**
     * Recupera las unidades guardadas en SharedPreferences a través del UnitManager.
     */
    private fun cargarPreferencias() {
        val ctx = getApplication<Application>()
        _peso.value = UnitManager.obtener(ctx, "unidad_peso", "kg")
        _distancia.value = UnitManager.obtener(ctx, "unidad_distancia", "km")
        _medida.value = UnitManager.obtener(ctx, "unidad_longitud", "cm")
    }

    /**
     * Guarda la nueva unidad seleccionada y actualiza el estado local (LiveData).
     * @param tipo El identificador de la unidad (peso, distancia o medida).
     * @param valor El valor de la unidad (ej: "kg", "lbs", "miles").
     */
    fun guardarUnidad(tipo: String, valor: String) {
        UnitManager.guardar(getApplication(), tipo, valor)
        when (tipo) {
            "unidad_peso" -> _peso.value = valor
            "unidad_distancia" -> _distancia.value = valor
            "unidad_longitud" -> _medida.value = valor
        }
    }
}