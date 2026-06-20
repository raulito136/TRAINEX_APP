package com.example.trainex.perfil.ajustes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.example.trainex.utils.SharedPreferencesManager
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ViewModel que gestiona la lógica de persistencia de preferencias y
 * la programación de tareas en segundo plano (Workers) mediante WorkManager.
 */
class NotificacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = SharedPreferencesManager(application)
    private val workManager = WorkManager.getInstance(application)

    // LiveData para la configuración de texto del entrenamiento
    private val _configEntreno = MutableLiveData<Triple<String, Int, Int>>()
    val configEntreno: LiveData<Triple<String, Int, Int>> get() = _configEntreno

    // LiveData para el estado de los interruptores (Clave -> Booleano)
    private val _switchesEstado = MutableLiveData<Map<String, Boolean>>()
    val switchesEstado: LiveData<Map<String, Boolean>> get() = _switchesEstado

    init {
        // 1. Cargar estados persistentes al iniciar el ViewModel
        val estadosCargados = mapOf(
            "general" to prefs.getBoolean("switch_general", true),
            "seguidores" to prefs.getBoolean("switch_seguidores", true),
            "entreno" to prefs.getBoolean("switch_entreno", false),
            "fotos" to prefs.getBoolean("switch_fotos", false)
        )
        _switchesEstado.value = estadosCargados

        // 2. Cargar datos de la hora de entrenamiento
        _configEntreno.value = Triple(
            prefs.getString("entreno_resumen", "") ?: "",
            prefs.getInt("entreno_hora", 10),
            prefs.getInt("entreno_minuto", 0)
        )

        // 3. REACTIVACIÓN: Asegurar que los Workers estén corriendo si su switch está ON tras un reinicio
        if (estadosCargados["seguidores"] == true) {
            programarWorkerSeguidores()
        }

        if (estadosCargados["entreno"] == true) {
            val config = _configEntreno.value!!
            programarWorkerEntrenamiento(config.second, config.third)
        }

        if (estadosCargados["fotos"] == true) {
            programarWorkerFoto()
        }
    }

    /**
     * Guarda el estado de un switch y gestiona su impacto en los Workers.
     */
    fun guardarEstadoSwitch(key: String, value: Boolean) {
        val nuevoMapa = _switchesEstado.value?.toMutableMap() ?: mutableMapOf()

        if (key == "general") {
            // Lógica Maestra: Si el general se apaga/enciende, afecta a todos los demás
            nuevoMapa["general"] = value
            nuevoMapa["seguidores"] = value
            nuevoMapa["entreno"] = value
            nuevoMapa["fotos"] = value

            // Guardar todos los estados en persistencia
            prefs.saveBoolean("switch_general", value)
            prefs.saveBoolean("switch_seguidores", value)
            prefs.saveBoolean("switch_entreno", value)
            prefs.saveBoolean("switch_fotos", value)

            // Actualizar la ejecución de los Workers en masa
            actualizarEstadoWorkers("seguidores", value)
            actualizarEstadoWorkers("entreno", value)
            actualizarEstadoWorkers("fotos", value)
        } else {
            // Lógica Individual: Solo afecta a una funcionalidad concreta
            nuevoMapa[key] = value
            prefs.saveBoolean("switch_$key", value)
            actualizarEstadoWorkers(key, value)

            // Auto-activar general si se activa cualquier opción individual
            if (value) {
                nuevoMapa["general"] = true
                prefs.saveBoolean("switch_general", true)
            }
        }
        _switchesEstado.value = nuevoMapa
    }

    /**
     * Encola o cancela trabajos en WorkManager según el estado de los switches.
     */
    private fun actualizarEstadoWorkers(key: String, activo: Boolean) {
        when (key) {
            "entreno" -> {
                if (activo) {
                    val config = _configEntreno.value ?: Triple("", 10, 0)
                    programarWorkerEntrenamiento(config.second, config.third)
                } else {
                    workManager.cancelUniqueWork("EntrenoWork")
                }
            }
            "seguidores" -> {
                if (activo) programarWorkerSeguidores() else workManager.cancelUniqueWork("SeguidoresWork")
            }
            "fotos" -> {
                if (activo) programarWorkerFoto() else workManager.cancelUniqueWork("TRABAJO_FOTO_SEMANAL")
            }
        }
    }

    /**
     * Programa un trabajo periódico para revisar nuevos seguidores cada hora.
     */
    private fun programarWorkerSeguidores() {
        val request = PeriodicWorkRequestBuilder<NotificacionesSeguidoresWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniquePeriodicWork(
            "SeguidoresWork",
            ExistingPeriodicWorkPolicy.KEEP, // No reinicia el contador si ya existe
            request
        )
    }

    /**
     * Guarda la configuración horaria y reprograma el worker de recordatorio.
     */
    fun guardarConfiguracionEntreno(resumen: String, hora: Int, minuto: Int) {
        prefs.saveString("entreno_resumen", resumen)
        prefs.saveInt("entreno_hora", hora)
        prefs.saveInt("entreno_minuto", minuto)
        _configEntreno.value = Triple(resumen, hora, minuto)

        programarWorkerEntrenamiento(hora, minuto)
    }

    /**
     * Calcula el delay necesario para que la notificación salte a la hora exacta.
     */
    private fun programarWorkerEntrenamiento(hora: Int, minuto: Int) {
        workManager.cancelUniqueWork("EntrenoWork")

        val calendarActual = Calendar.getInstance()
        val calendarDestino = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
        }

        // Si la hora ya pasó hoy, programamos para mañana
        if (calendarDestino.before(calendarActual)) {
            calendarDestino.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = calendarDestino.timeInMillis - calendarActual.timeInMillis

        val workRequest = OneTimeWorkRequestBuilder<RecordatorioEntrenoWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("WORK_ENTRENO")
            .build()

        workManager.enqueueUniqueWork("EntrenoWork", ExistingWorkPolicy.REPLACE, workRequest)
    }

    /**
     * Programa el recordatorio semanal de fotos.
     */
    private fun programarWorkerFoto() {
        val request = OneTimeWorkRequestBuilder<RecordatorioFotoWorker>()
            .addTag("WORK_FOTO_SEMANAL")
            .build()

        workManager.enqueueUniqueWork(
            "TRABAJO_FOTO_SEMANAL",
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}