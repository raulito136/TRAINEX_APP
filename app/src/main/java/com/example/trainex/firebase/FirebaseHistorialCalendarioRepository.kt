package com.example.trainex.firebase

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.perfil.calendario.SesionEntrenamiento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

/**
 * Repositorio para la gestión de sesiones de entrenamiento completadas en Firestore.
 * Almacena los registros históricos que alimentan el calendario de la aplicación.
 */
class FirebaseHistorialCalendarioRepository {

    private val db = FirebaseFirestore.getInstance()
    /** Colección centralizada que agrupa las sesiones de todos los usuarios */
    private val sesionesRef = db.collection("sesiones_completadas")

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     * Registra una sesión de entrenamiento (rutina, tiempo y ejercicios) en Firestore.
     * Genera un ID automático y estampa la fecha actual del sistema.
     */
    suspend fun guardarSesion(sesion: SesionEntrenamiento) {
        if (currentUserId.isEmpty()) return

        try {
            val docRef = sesionesRef.document()
            val sesionConId = sesion.copy(
                id = docRef.id,
                userId = currentUserId,
                fecha = System.currentTimeMillis()
            )
            docRef.set(sesionConId).await()
            Log.d("FirebaseHistorial", "Sesión guardada correctamente")
        } catch (e: Exception) {
            Log.e("FirebaseHistorial", "Error al guardar sesión", e)
            throw e
        }
    }

    /**
     * Recupera las sesiones de un día específico para el usuario actual.
     * @param fecha Fecha de la cual se desean obtener los registros.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerSesionesPorFecha(fecha: LocalDate): List<SesionEntrenamiento> {
        if (currentUserId.isEmpty()) return emptyList()

        /** Define el rango temporal en milisegundos desde las 00:00:00 hasta las 23:59:59 del día dado */
        val startOfDay = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        return try {
            val snapshot = sesionesRef
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("fecha", startOfDay)
                .whereLessThanOrEqualTo("fecha", endOfDay)
                .get()
                .await()

            snapshot.toObjects(SesionEntrenamiento::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseHistorial", "Error al obtener sesiones por fecha", e)
            emptyList()
        }
    }

    /**
     * Obtiene todos los entrenamientos realizados por el usuario logueado en orden cronológico.
     */
    suspend fun obtenerTodasLasSesiones(): List<SesionEntrenamiento> {
        if (currentUserId.isEmpty()) return emptyList()

        return try {
            val snapshot = sesionesRef
                .whereEqualTo("userId", currentUserId)
                .orderBy("fecha", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.toObjects(SesionEntrenamiento::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseHistorial", "Error al obtener historial completo", e)
            emptyList()
        }
    }

    /**
     * Consulta el historial de sesiones de un usuario externo.
     * @param uid El identificador del usuario objetivo.
     */
    suspend fun obtenerSesionesPorId(uid: String): List<SesionEntrenamiento> {
        return try {
            val snapshot = sesionesRef
                .whereEqualTo("userId", uid)
                .orderBy("fecha", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.toObjects(SesionEntrenamiento::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseHistorial", "Error al obtener historial público", e)
            emptyList()
        }
    }

    /**
     * Obtiene el listado completo de sesiones de otro usuario, útil para marcar hitos en el calendario.
     */
    suspend fun obtenerTodasLasSesionesPorId(uid: String): List<SesionEntrenamiento> {
        return try {
            val snapshot = sesionesRef
                .whereEqualTo("userId", uid)
                .orderBy("fecha", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.toObjects(SesionEntrenamiento::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Recupera el detalle de los entrenamientos de un usuario externo para un día concreto.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerSesionesPorFechaEId(fecha: LocalDate, uid: String): List<SesionEntrenamiento> {
        val startOfDay = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        return try {
            val snapshot = sesionesRef
                .whereEqualTo("userId", uid)
                .whereGreaterThanOrEqualTo("fecha", startOfDay)
                .whereLessThanOrEqualTo("fecha", endOfDay)
                .get()
                .await()
            snapshot.toObjects(SesionEntrenamiento::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}