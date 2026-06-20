package com.example.trainex.perfil.medidas

import androidx.lifecycle.ViewModel
import com.example.trainex.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsable de gestionar y proveer la lista de tipos de medidas disponibles.
 * Actúa como la fuente de verdad para los diferentes parámetros corporales que la app puede trackear.
 */
class MedidasViewModel : ViewModel() {

    private val _tiposMedida = MutableStateFlow<List<TipoMedida>>(emptyList())
    /**
     * Estado que expone el listado de [TipoMedida] a la vista.
     */
    val tiposMedida: StateFlow<List<TipoMedida>> = _tiposMedida

    init {
        cargarTiposMedida()
    }

    /**
     * Inicializa la lista con los recursos de texto e iconos predefinidos para cada medida corporal.
     */
    private fun cargarTiposMedida() {
        _tiposMedida.value = listOf(
            TipoMedida(R.string.medida_peso_corporal, R.drawable.ic_peso),
            TipoMedida(R.string.medida_grasa_corporal, R.drawable.ic_grasa),
            TipoMedida(R.string.medida_cuello, R.drawable.ic_cuello),
            TipoMedida(R.string.medida_hombros, R.drawable.ic_hombros),
            TipoMedida(R.string.medida_pecho, R.drawable.ic_pecho),
            TipoMedida(R.string.medida_biceps_izq, R.drawable.ic_biceps),
            TipoMedida(R.string.medida_biceps_der, R.drawable.ic_biceps),
            TipoMedida(R.string.medida_antebrazo_izq, R.drawable.ic_antebrazos),
            TipoMedida(R.string.medida_antebrazo_der, R.drawable.ic_antebrazos),
            TipoMedida(R.string.medida_cintura, R.drawable.ic_cintura),
            TipoMedida(R.string.medida_caderas, R.drawable.ic_caderas),
            TipoMedida(R.string.medida_muslo_izq, R.drawable.ic_muslo),
            TipoMedida(R.string.medida_muslo_der, R.drawable.ic_muslo),
            TipoMedida(R.string.medida_pantorrilla_izq, R.drawable.ic_gemelos),
            TipoMedida(R.string.medida_pantorrilla_der, R.drawable.ic_gemelos)
        )
    }
}