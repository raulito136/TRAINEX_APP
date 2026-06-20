package com.example.trainex.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.trainex.dietas.Alimento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

/**
 * Repositorio encargado de gestionar los alimentos en Firebase Firestore.
 * Permite la creación de alimentos globales, búsqueda, gestión de historial personal y eliminación.
 */
class FirebaseAlimentosRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val alimentosRef = db.collection("alimentos_globales")

    /**
     * Sube un alimento a la colección global de Firestore.
     * Si se proporciona una imagen (Uri), se procesa, comprime y convierte a Base64 antes de subirla.
     * @param alimento El objeto con los datos nutricionales.
     * @param imageUri URI local de la imagen seleccionada por el usuario.
     * @return El objeto Alimento con el campo imagen actualizado en Base64.
     */
    suspend fun crearAlimentoGlobal(alimento: Alimento, imageUri: Uri?): Alimento {
        var alimentoFinal = alimento

        try {
            /** 1. Procesamiento de imagen: Reducción y conversión a Base64 */
            if (imageUri != null) {
                val bitmap = obtenerBitmapReducido(imageUri)
                if (bitmap != null) {
                    val imagenBase64 = convertirBitmapABase64(bitmap)
                    alimentoFinal = alimento.copy(imagen = imagenBase64)
                }
            }

            /** 2. Validación de integridad de datos */
            if (alimentoFinal.firebaseId.isEmpty()) {
                throw Exception("El alimento no tiene firebaseId")
            }

            /** 3. Persistencia en la colección global de Firestore */
            alimentosRef.document(alimentoFinal.firebaseId).set(alimentoFinal).await()
            Log.d("FirebaseAlimentos", "Alimento subido con éxito: ${alimentoFinal.nombre}")

            return alimentoFinal

        } catch (e: Exception) {
            Log.e("FirebaseAlimentos", "Error al subir alimento", e)
            throw e
        }
    }

    /**
     * Abre un flujo de entrada desde la URI, decodifica la imagen y la escala.
     * Garantiza que el lado más largo de la imagen no supere los 800 píxeles.
     */
    private fun obtenerBitmapReducido(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (original == null) return null

            val ratio = Math.min(800.0 / original.width, 800.0 / original.height)
            val width = (original.width * ratio).toInt()
            val height = (original.height * ratio).toInt()

            Bitmap.createScaledBitmap(original, width, height, true)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Comprime el Bitmap a formato JPEG con un 70% de calidad y lo codifica en Base64.
     */
    private fun convertirBitmapABase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Busca alimentos en la colección global filtrando por nombre.
     * Aplica capitalización a la primera letra para mejorar la coincidencia en Firestore.
     * @param query Texto introducido por el usuario.
     */
    suspend fun buscarAlimentosGlobales(query: String): List<Alimento> {
        return try {
            val queryCap = query.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            val snapshot = alimentosRef
                .whereGreaterThanOrEqualTo("nombre", queryCap)
                .whereLessThan("nombre", queryCap + "\uf8ff")
                .limit(20)
                .get()
                .await()

            snapshot.toObjects(Alimento::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseAlimentos", "Error buscando en firebase", e)
            emptyList()
        }
    }

    /**
     * Obtiene una lista combinada de alimentos: los creados por el usuario y los guardados en su historial.
     * @param userId ID del usuario actual.
     */
    suspend fun obtenerMisAlimentos(userId: String): List<Alimento> {
        return try {
            val tareaCreados = alimentosRef
                .whereEqualTo("userId", userId)
                .get()

            val tareaHistorial = db.collection("usuarios")
                .document(userId)
                .collection("historial_alimentos")
                .get()

            val snapshotCreados = tareaCreados.await()
            val snapshotHistorial = tareaHistorial.await()

            val listaCreados = snapshotCreados.toObjects(Alimento::class.java)
            val listaHistorial = snapshotHistorial.toObjects(Alimento::class.java)

            /** Combina ambas listas eliminando duplicados mediante el ID de Firebase */
            (listaCreados + listaHistorial).distinctBy { it.firebaseId }

        } catch (e: Exception) {
            Log.e("FirebaseAlimentos", "Error obteniendo mis alimentos", e)
            emptyList()
        }
    }

    /**
     * Almacena un alimento en la subcolección personal del usuario para acceso rápido posterior.
     */
    fun guardarEnHistorial(userId: String, alimento: Alimento) {
        try {
            db.collection("usuarios")
                .document(userId)
                .collection("historial_alimentos")
                .document(alimento.firebaseId)
                .set(alimento)
        } catch (e: Exception) {
            Log.e("FirebaseAlimentos", "Error guardando en historial", e)
        }
    }

    /**
     * Elimina un alimento del historial personal. Si el usuario es el creador original,
     * también se elimina de la base de datos global.
     */
    suspend fun eliminarAlimento(userId: String, alimento: Alimento) {
        try {
            db.collection("usuarios")
                .document(userId)
                .collection("historial_alimentos")
                .document(alimento.firebaseId)
                .delete()
                .await()

            if (alimento.userId == userId) {
                alimentosRef.document(alimento.firebaseId).delete().await()
            }

        } catch (e: Exception) {
            Log.e("FirebaseAlimentos", "Error eliminando alimento", e)
            throw e
        }
    }
}