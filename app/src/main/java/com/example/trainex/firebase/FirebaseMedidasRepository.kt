package com.example.trainex.firebase

import android.util.Log
import com.example.trainex.perfil.medidas.RegistroMedida
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Repositorio para la gestión de medidas corporales (bíceps, cintura, etc.) en Firestore.
 * Incluye lógica para evitar duplicados diarios, permitiendo solo un registro por medida al día.
 */
class FirebaseMedidasRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Guarda una medida corporal. Si ya existe un registro para el mismo nombre en el mismo día,
     * se sobrescribe el anterior para evitar duplicidad de datos en las gráficas.
     */
    suspend fun guardarMedida(medida: RegistroMedida) {
        val uid = currentUserId ?: return

        try {
            val collectionRef = db.collection("usuarios").document(uid)
                .collection("historial_medidas")

            /** 1. Calcula el rango temporal del día (00:00 a 23:59) */
            val (inicioDia, finDia) = obtenerRangoDia(medida.fecha)

            var idFinal = medida.id

            /** 2. Si es un registro nuevo, busca si ya existe uno previo en el mismo día */
            if (idFinal.isEmpty()) {
                val querySnapshot = collectionRef
                    .whereEqualTo("nombre", medida.nombre)
                    .whereGreaterThanOrEqualTo("fecha", inicioDia)
                    .whereLessThanOrEqualTo("fecha", finDia)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    /** Si existe, recupera su ID para realizar una actualización en lugar de una creación */
                    idFinal = querySnapshot.documents[0].id
                    Log.d("MedidasRepo", "Medida existente encontrada ($idFinal), se sobrescribirá.")
                }
            }

            /** 3. Realiza el guardado: Actualiza si existe ID, crea uno nuevo si no */
            if (idFinal.isNotEmpty()) {
                val medidaActualizada = medida.copy(id = idFinal, userId = uid)
                collectionRef.document(idFinal).set(medidaActualizada).await()
            } else {
                val docRef = collectionRef.document()
                val medidaNueva = medida.copy(id = docRef.id, userId = uid)
                docRef.set(medidaNueva).await()
            }

            Log.d("MedidasRepo", "Operación de guardado exitosa para: ${medida.nombre}")

        } catch (e: Exception) {
            Log.e("MedidasRepo", "Error al guardar medida", e)
            throw e
        }
    }

    /**
     * Obtiene todos los registros históricos de una medida específica para el usuario actual.
     */
    suspend fun obtenerMedidasPorNombre(nombreMedida: String): List<RegistroMedida> {
        val uid = currentUserId ?: return emptyList()

        return try {
            val snapshot = db.collection("usuarios").document(uid)
                .collection("historial_medidas")
                .whereEqualTo("nombre", nombreMedida)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RegistroMedida::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("MedidasRepo", "Error al obtener medidas", e)
            emptyList()
        }
    }

    /**
     * Elimina un registro de medida permanente mediante su ID.
     */
    suspend fun eliminarMedida(idMedida: String) {
        val uid = currentUserId ?: return
        try {
            db.collection("usuarios").document(uid)
                .collection("historial_medidas")
                .document(idMedida)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("MedidasRepo", "Error al eliminar", e)
            throw e
        }
    }

    /**
     * Función auxiliar que calcula los milisegundos de inicio y fin de un día dado.
     */
    private fun obtenerRangoDia(fechaMs: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fechaMs

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val inicio = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val fin = calendar.timeInMillis

        return Pair(inicio, fin)
    }

    /**
     * Consulta registros de medidas de otro usuario, permitiendo visualizar su progreso.
     */
    suspend fun obtenerMedidasPorNombreEId(nombreMedida: String, uid: String): List<RegistroMedida> {
        return try {
            val snapshot = db.collection("usuarios").document(uid)
                .collection("historial_medidas")
                .whereEqualTo("nombre", nombreMedida)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RegistroMedida::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("MedidasRepo", "Error al obtener medidas ajenas", e)
            emptyList()
        }
    }
}