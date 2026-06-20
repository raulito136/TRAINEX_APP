package com.example.trainex.firebase

import android.util.Log
import com.example.trainex.ejercicio.Ejercicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la gestión del historial de ejercicios individuales realizados.
 * Permite guardar ejecuciones con sus series y consultar el progreso histórico por ejercicio.
 */
class FirebaseHistorialRepository {

    private val db = FirebaseFirestore.getInstance()
    private val historialRef = db.collection("historial_ejercicios")

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     * Guarda una lista de ejercicios realizados utilizando una operación por lotes (Batch).
     * @param ejercicios Lista de objetos Ejercicio que contienen sus respectivas series.
     */
    suspend fun guardarEjerciciosRealizados(ejercicios: List<Ejercicio>) {
        if (currentUserId.isEmpty() || ejercicios.isEmpty()) return

        try {
            val batch = db.batch()

            ejercicios.forEach { ejercicio ->
                val docRef = historialRef.document()
                /** Estructura el documento vinculando el usuario y un timestamp del servidor */
                val data = hashMapOf(
                    "userId" to currentUserId,
                    "fecha" to FieldValue.serverTimestamp(),
                    "ejercicio" to ejercicio
                )
                batch.set(docRef, data)
            }

            batch.commit().await()
            Log.d("FirebaseHistorial", "Guardados ${ejercicios.size} ejercicios con series.")

        } catch (e: Exception) {
            Log.e("FirebaseHistorial", "Error al guardar historial", e)
            throw e
        }
    }

    /**
     * Obtiene el historial completo de ejercicios del usuario actual ordenado por fecha descendente.
     */
    suspend fun obtenerHistorialUsuario(): List<Ejercicio> {
        if (currentUserId.isEmpty()) return emptyList()

        return try {
            val snapshot = historialRef
                .whereEqualTo("userId", currentUserId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.get("ejercicio", Ejercicio::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseHistorial", "Error al obtener historial", e)
            emptyList()
        }
    }

    /**
     * Busca registros históricos filtrando por el nombre exacto del ejercicio.
     * Accede al campo anidado 'ejercicio.nombre' dentro de los documentos de Firestore.
     */
    suspend fun obtenerHistorialPorNombre(nombreEjercicio: String): List<Ejercicio> {
        if (currentUserId.isEmpty()) return emptyList()

        return try {
            val snapshot = historialRef
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("ejercicio.nombre", nombreEjercicio)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("FirebaseHistorial", "Encontrados ${snapshot.size()} registros para $nombreEjercicio")

            snapshot.documents.mapNotNull { doc ->
                doc.get("ejercicio", Ejercicio::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseHistorial", "Error buscando historial de $nombreEjercicio", e)
            emptyList()
        }
    }

    /**
     * Recupera el historial de ejercicios de un usuario externo mediante su ID.
     */
    suspend fun obtenerHistorialPorUsuarioId(uid: String): List<Ejercicio> {
        return try {
            val snapshot = historialRef
                .whereEqualTo("userId", uid)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc -> doc.get("ejercicio", Ejercicio::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Busca registros históricos de un ejercicio específico para un usuario ajeno.
     */
    suspend fun obtenerHistorialPorNombreEId(nombreEjercicio: String, uid: String): List<Ejercicio> {
        return try {
            val snapshot = historialRef
                .whereEqualTo("userId", uid)
                .whereEqualTo("ejercicio.nombre", nombreEjercicio)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("FirebaseHistorial", "Encontrados ${snapshot.size()} registros ajenos para $nombreEjercicio")

            snapshot.documents.mapNotNull { doc ->
                doc.get("ejercicio", Ejercicio::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseHistorial", "Error buscando historial ajeno de $nombreEjercicio", e)
            emptyList()
        }
    }
}