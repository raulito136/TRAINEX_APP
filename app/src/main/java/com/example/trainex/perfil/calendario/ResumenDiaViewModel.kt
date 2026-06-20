package com.example.trainex.perfil.calendario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.R
import com.example.trainex.firebase.FirebaseHistorialCalendarioRepository
import com.example.trainex.repository.FirebaseRutinaRepository
import com.example.trainex.rutinas.Rutina
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel encargado de la lógica del resumen diario de entrenamientos.
 * Gestiona la carga de sesiones realizadas en una fecha específica y permite clonar rutinas de otros usuarios.
 */
class ResumenDiaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseHistorialCalendarioRepository()
    private val rutinaRepo = FirebaseRutinaRepository()
    private val context = application.applicationContext

    /** Flujo de estado que contiene la lista de sesiones procesadas para mostrar en la UI */
    private val _sesiones = MutableStateFlow<List<HistorialSesion>>(emptyList())
    val sesiones: StateFlow<List<HistorialSesion>> = _sesiones

    /** Flujo de eventos para notificar mensajes (éxitos o errores) a la vista */
    private val _eventos = MutableStateFlow<String?>(null)
    val eventos: StateFlow<String?> = _eventos

    /**
     * Recupera las sesiones de entrenamiento de una fecha determinada.
     * @param fecha La fecha seleccionada en el calendario.
     * @param userId ID del usuario (si es nulo, busca las del usuario actual).
     */
    fun cargarSesiones(fecha: LocalDate, userId: String?) {
        viewModelScope.launch {
            try {
                val result = if (!userId.isNullOrEmpty()) {
                    repository.obtenerSesionesPorFechaEId(fecha, userId)
                } else {
                    repository.obtenerSesionesPorFecha(fecha)
                }

                /** Mapea los datos de Firebase al modelo de vista */
                _sesiones.value = result.map { sesion ->
                    HistorialSesion(
                        nombreRutina = sesion.nombreRutina,
                        tiempoDuracion = formatMillis(sesion.tiempoMs),
                        ejerciciosRealizados = sesion.ejercicios
                    )
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    /**
     * Permite al usuario actual guardar una copia de la rutina de otra persona en su propia biblioteca.
     * @param sesion La sesión que contiene los ejercicios a copiar.
     */
    fun copiarRutina(sesion: HistorialSesion) {
        viewModelScope.launch {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val newId = FirebaseFirestore.getInstance().collection("rutinas").document().id

                /** Crea una nueva rutina basada en la sesión ajena */
                val nuevaRutina = Rutina(
                    titulo = context.getString(R.string.copiada, sesion.nombreRutina),
                    firebaseId = newId,
                    ejercicios = ArrayList(sesion.ejerciciosRealizados),
                    userId = currentUserId,
                    fechaCreacion = System.currentTimeMillis(),
                    lastUpdate = System.currentTimeMillis()
                )

                rutinaRepo.subirRutinaConId(nuevaRutina)
                _eventos.value = context.getString(R.string.rutina_a_adida_a_tus_entrenamientos)
            } catch (e: Exception) {
                _eventos.value = context.getString(R.string.error_al_copiar_rutina)
            }
        }
    }

    /** Convierte milisegundos a un formato de texto legible (ej: "1h 10min" o "45min") */
    private fun formatMillis(millis: Long): String {
        val min = millis / 60000
        val hours = min / 60
        return if (hours > 0) "${hours}h ${min % 60}min" else "${min}min"
    }
}