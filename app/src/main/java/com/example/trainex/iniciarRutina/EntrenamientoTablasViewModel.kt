package com.example.trainex.iniciarRutina

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trainex.database.AppDatabase
import com.example.trainex.firebase.FirebaseHistorialCalendarioRepository
import com.example.trainex.firebase.FirebaseHistorialRepository
import com.example.trainex.perfil.calendario.SesionEntrenamiento
import com.example.trainex.rutinas.Rutina
import com.example.trainex.serie.Serie
import com.example.trainex.serie.SerieEntidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel encargado de la lógica de persistencia durante y después del entrenamiento.
 */
class EntrenamientoTablasViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia de base de datos local Room
    private val db = AppDatabase.getDatabase(application)
    private val serieDao = db.serieDao()

    // Repositorios para sincronizar con Firebase
    private val historialRepo = FirebaseHistorialRepository()
    private val calendarioRepo = FirebaseHistorialCalendarioRepository()

    private val _ejerciciosRellenos = MutableLiveData<List<EjercicioConSeries>>()
    val ejerciciosRellenos: LiveData<List<EjercicioConSeries>> get() = _ejerciciosRellenos

    private val _operacionExitosa = MutableLiveData<Boolean>()
    val operacionExitosa: LiveData<Boolean> get() = _operacionExitosa

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    /**
     * Busca en la base de datos Room si existen series guardadas anteriormente para los ejercicios de la rutina.
     * Esto permite que el usuario vea automáticamente el peso que levantó la última vez.
     */
    fun cargarDatosPrevios(rutina: Rutina) {
        viewModelScope.launch(Dispatchers.IO) {
            val listaRellena = rutina.ejercicios.map { ejercicio ->
                // Consultar Room por ID de ejercicio
                val seriesGuardadas = serieDao.obtenerSeriesPorEjercicio(ejercicio.id)

                // Mapear los datos de la entidad de DB al modelo de la App
                val seriesMapeadas = seriesGuardadas.map { entidad ->
                    Serie(
                        ejercicioId = entidad.ejercicioId.toInt(),
                        numeroSerie = entidad.numeroSerie,
                        kilogramos = entidad.kilogramos,
                        repeticiones = entidad.repeticiones
                    )
                }.toMutableList()

                EjercicioConSeries(ejercicio, seriesMapeadas)
            }
            // Retornar al hilo principal para actualizar la UI
            withContext(Dispatchers.Main) {
                _ejerciciosRellenos.value = listaRellena
            }
        }
    }

    /**
     * Realiza el proceso de guardado doble: localmente en Room y remotamente en Firebase.
     */
    fun finalizarEntrenamiento(rutina: Rutina, tiempoMillis: Long, ejerciciosConSeries: List<EjercicioConSeries>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Guardar en Room (Sobrescribe los pesos anteriores para la próxima sesión)
                ejerciciosConSeries.forEach { item ->
                    item.series.forEach { s ->
                        val entidad = SerieEntidad(
                            ejercicioId = item.ejercicio.id.toString(),
                            numeroSerie = s.numeroSerie,
                            kilogramos = s.kilogramos,
                            repeticiones = s.repeticiones
                        )
                        serieDao.insertarSerie(entidad) // Room se encarga de reemplazar si existe conflicto
                    }
                }

                // 2. Guardar en Firebase (Historial y Calendario)
                val ejerciciosRealizados = ejerciciosConSeries.map { it.ejercicio.copy(series = it.series) }

                // Guardar en el repositorio de historial de ejercicios
                historialRepo.guardarEjerciciosRealizados(ejerciciosRealizados)

                // Crear objeto de sesión y guardar en el repositorio del calendario
                val sesion = SesionEntrenamiento(
                    id = "", userId = "", nombreRutina = rutina.titulo,
                    tiempoMs = tiempoMillis, fecha = System.currentTimeMillis(),
                    ejercicios = ejerciciosRealizados
                )
                calendarioRepo.guardarSesion(sesion)

                withContext(Dispatchers.Main) {
                    _operacionExitosa.value = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message
                }
            }
        }
    }
}