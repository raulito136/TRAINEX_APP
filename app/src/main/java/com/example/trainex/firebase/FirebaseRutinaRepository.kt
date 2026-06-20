package com.example.trainex.repository

import android.util.Log
import com.example.trainex.rutinas.Rutina
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la gestión de rutinas de entrenamiento.
 * Implementa tanto consultas puntuales como flujos de datos en tiempo real (Flow).
 */
class FirebaseRutinaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val rutinasRef = db.collection("rutinas")

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "anon"

    /**
     * Crea un Flow que escucha cambios en tiempo real en las rutinas de un usuario.
     * Utiliza un SnapshotListener de Firestore que se cierra automáticamente al cancelar el flujo.
     */
    fun obtenerRutinasFlow(userId: String): Flow<List<Rutina>> = callbackFlow {
        val query = rutinasRef
            .whereEqualTo("userId", userId)
            .orderBy("lastUpdate", Query.Direction.DESCENDING)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseRepo", "Error escuchando rutinas", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val rutinas = snapshot.toObjects(Rutina::class.java)
                trySend(rutinas)
            }
        }

        /** Se asegura de limpiar el listener cuando el flujo ya no es necesario */
        awaitClose {
            Log.d("FirebaseRepo", "Cerrando listener de Firestore")
            listenerRegistration.remove()
        }
    }

    /**
     * Sube o actualiza una rutina utilizando su ID de Firebase asignado.
     */
    suspend fun subirRutinaConId(rutina: Rutina) {
        try {
            if (rutina.firebaseId.isEmpty()) {
                Log.e("FirebaseRepo", "Intento de subir rutina sin firebaseId.")
                return
            }
            db.collection("rutinas").document(rutina.firebaseId).set(rutina).await()
            Log.d("FirebaseRepo", "Rutina subida/actualizada con éxito: ${rutina.firebaseId}")
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error al subir la rutina con ID", e)
        }
    }

    /**
     * Elimina una rutina permanentemente de la colección de Firestore.
     */
    suspend fun eliminarRutina(firebaseId: String) {
        if (firebaseId.isEmpty()) return
        try {
            db.collection("rutinas").document(firebaseId).delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error al eliminar rutina", e)
        }
    }

    /**
     * Obtiene de forma puntual (una sola vez) todas las rutinas creadas por el usuario logueado.
     */
    suspend fun obtenerRutinasPropias(): List<Rutina> {
        return rutinasRef
            .whereEqualTo("userId", currentUserId)
            .get()
            .await()
            .toObjects(Rutina::class.java)
    }

    /**
     * Recupera todas las rutinas existentes en la base de datos (Uso administrativo o global).
     */
    suspend fun obtenerTodas(): List<Rutina> {
        return rutinasRef.get().await().toObjects(Rutina::class.java)
    }
}