package com.example.trainex.perfil.ejercicios

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.firebase.FirebaseHistorialRepository
import com.example.trainex.utils.UnitManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de procesar y calcular las estadísticas personales de un ejercicio específico.
 * Extrae récords como el peso máximo, la mejor serie por volumen y el volumen total por sesión.
 * Para ejercicios de cardio, calcula distancia máxima, distancia total y tiempo total.
 */
class DetalleHistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseHistorialRepository()
    private val context = application.applicationContext

    /** Flujo de estado que representa los diferentes estados de la carga de récords */
    private val _records = MutableStateFlow<RecordsState>(RecordsState.Loading)
    val records: StateFlow<RecordsState> = _records

    /** Palabras clave para identificar ejercicios de cardio/distancia */
    private val keywordsCardio = listOf(
        "bicicleta", "bike", "caminadora", "treadmill", "cinta",
        "caminata", "walk", "correr", "run", "running", "senderismo", "hike",
        "eliptico", "elliptical", "cycling", "carrera", "bici", "caminar",
        "estática", "estatica", "spinning", "cardio", "aeróbico", "aerobico"
    )

    /**
     * Carga el historial desde Firebase y dispara el cálculo de estadísticas.
     * @param ejercicio El ejercicio completo para detectar si es cardio.
     * @param userIdExterno ID opcional si se consultan datos de otro usuario.
     */
    fun cargarEstadisticas(ejercicio: Ejercicio, userIdExterno: String?) {
        viewModelScope.launch {
            try {
                val historial = if (!userIdExterno.isNullOrEmpty()) {
                    repository.obtenerHistorialPorNombreEId(ejercicio.nombre, userIdExterno)
                } else {
                    repository.obtenerHistorialPorNombre(ejercicio.nombre)
                }

                if (historial.isNotEmpty()) {
                    val esCardio = esEjercicioCardio(ejercicio.nombre)
                    if (esCardio) {
                        _records.value = calcularRecordsCardio(historial)
                    } else {
                        _records.value = calcularRecordsMusculacion(historial)
                    }
                } else {
                    val unidadPeso = UnitManager.obtener(context, "unidad_peso", "kg")
                    val unidadDistancia = UnitManager.obtener(context, "unidad_distancia", "km")
                    _records.value = RecordsState.Empty(
                        displayPeso = "- $unidadPeso",
                        displayDistancia = "- $unidadDistancia"
                    )
                }
            } catch (e: Exception) {
                _records.value = RecordsState.Error(e.message ?: "Error")
            }
        }
    }

    /**
     * Determina si un ejercicio es de cardio basándose en palabras clave en su nombre.
     */
    private fun esEjercicioCardio(nombre: String): Boolean {
        return keywordsCardio.any { nombre.contains(it, ignoreCase = true) }
    }

    /**
     * Lógica de cálculo para ejercicios de musculación (peso y repeticiones).
     */
    private fun calcularRecordsMusculacion(historial: List<Ejercicio>): RecordsState {
        val unidadPeso = UnitManager.obtener(context, "unidad_peso", "kg")
        var maxPeso = 0.0
        var maxVolumenSerieValor = 0.0
        var mejorSerieKg = 0.0
        var mejorSerieReps = 0
        var maxVolumenTotalSesion = 0.0

        for (sesion in historial) {
            val series = sesion.series ?: continue
            var volumenTotalEstaSesion = 0.0
            for (serie in series) {
                if (serie.kilogramos > maxPeso) maxPeso = serie.kilogramos

                val vol = serie.kilogramos * serie.repeticiones
                if (vol > maxVolumenSerieValor) {
                    maxVolumenSerieValor = vol
                    mejorSerieKg = serie.kilogramos
                    mejorSerieReps = serie.repeticiones
                }
                volumenTotalEstaSesion += vol
            }
            if (volumenTotalEstaSesion > maxVolumenTotalSesion) maxVolumenTotalSesion = volumenTotalEstaSesion
        }

        return RecordsState.Musculacion(
            maxPeso = "${formatear(UnitManager.aVisual(maxPeso, unidadPeso))} $unidadPeso",
            mejorSerie = "${formatear(UnitManager.aVisual(mejorSerieKg, unidadPeso))} $unidadPeso x $mejorSerieReps reps",
            mejorVolumenTotal = "${formatear(UnitManager.aVisual(maxVolumenTotalSesion, unidadPeso))} $unidadPeso"
        )
    }

    /**
     * Lógica de cálculo para ejercicios de cardio (distancia y tiempo).
     */
    private fun calcularRecordsCardio(historial: List<Ejercicio>): RecordsState {
        val unidadDistancia = UnitManager.obtener(context, "unidad_distancia", "km")

        var maxDistancia = 0.0
        var distanciaTotal = 0.0
        var tiempoTotal = 0
        var sesionesCount = 0

        for (sesion in historial) {
            val series = sesion.series ?: continue
            sesionesCount++
            for (serie in series) {
                // En cardio: kilogramos = distancia (km), repeticiones = tiempo (min)
                val distancia = serie.kilogramos
                val tiempo = serie.repeticiones

                if (distancia > maxDistancia) maxDistancia = distancia
                distanciaTotal += distancia
                tiempoTotal += tiempo
            }
        }

        return RecordsState.Cardio(
            maxDistancia = "${formatear(UnitManager.aVisual(maxDistancia, unidadDistancia))} $unidadDistancia",
            distanciaTotal = "${formatear(UnitManager.aVisual(distanciaTotal, unidadDistancia))} $unidadDistancia",
            tiempoTotal = "${tiempoTotal} min",
            sesiones = "$sesionesCount"
        )
    }

    /** Formatea el valor numérico para mostrar decimales solo si es necesario */
    private fun formatear(valor: Double) = if (valor % 1.0 == 0.0) valor.toInt().toString() else "%.1f".format(valor).replace(",", ".")

    /** Representación de los estados de los récords en la interfaz */
    sealed class RecordsState {
        object Loading : RecordsState()
        data class Empty(
            val displayPeso: String,
            val displayDistancia: String
        ) : RecordsState()
        /** Records para ejercicios de musculación (peso, volumen) */
        data class Musculacion(
            val maxPeso: String,
            val mejorSerie: String,
            val mejorVolumenTotal: String
        ) : RecordsState()
        /** Records para ejercicios de cardio (distancia, tiempo) */
        data class Cardio(
            val maxDistancia: String,
            val distanciaTotal: String,
            val tiempoTotal: String,
            val sesiones: String
        ) : RecordsState()
        data class Error(val message: String) : RecordsState()
    }
}
