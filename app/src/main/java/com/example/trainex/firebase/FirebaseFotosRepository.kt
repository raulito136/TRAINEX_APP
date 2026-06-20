package com.example.trainex.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.trainex.perfil.foto.FotoProgreso
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

/**
 * Gestiona el almacenamiento y recuperación de fotos de progreso de los usuarios.
 * Utiliza un enfoque de almacenamiento de imágenes como cadenas de texto Base64 dentro de Firestore.
 */
class FirebaseFotosRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Convierte una imagen (Uri) a una cadena Base64 y la guarda en la subcolección del usuario.
     * @param uri Localización de la foto en el dispositivo.
     */
    suspend fun subirFotoComoTexto(uri: Uri) {
        val uid = currentUserId ?: throw Exception("Usuario no logueado")

        val bitmap = obtenerBitmapReducido(uri) ?: throw Exception("No se pudo procesar la imagen")
        val imagenBase64 = convertirBitmapABase64(bitmap)

        val fotoData = mapOf(
            "url" to imagenBase64,
            "fecha" to System.currentTimeMillis(),
            "esBase64" to true
        )

        try {
            db.collection("usuarios").document(uid)
                .collection("historial_fotos")
                .add(fotoData)
                .await()
        } catch (e: Exception) {
            Log.e("FotosRepo", "Error al guardar en Firestore", e)
            throw e
        }
    }

    /**
     * Elimina un documento de foto específico del historial del usuario actual.
     * @return true si la operación fue exitosa, false en caso contrario.
     */
    suspend fun eliminarFoto(idFoto: String): Boolean {
        val uid = currentUserId ?: return false
        return try {
            db.collection("usuarios").document(uid)
                .collection("historial_fotos")
                .document(idFoto)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("FotosRepo", "Error al eliminar foto", e)
            false
        }
    }

    /**
     * Recupera todas las fotos de progreso del usuario logueado, ordenadas de más reciente a más antigua.
     */
    suspend fun obtenerFotosProgreso(): List<FotoProgreso> {
        val uid = currentUserId ?: return emptyList()

        return try {
            val snapshot = db.collection("usuarios").document(uid)
                .collection("historial_fotos")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { doc ->
                FotoProgreso(
                    id = doc.id,
                    url = doc.getString("url") ?: "",
                    fecha = doc.getLong("fecha") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Reduce las dimensiones de la imagen para que su lado mayor sea de 800px.
     * Esto es crítico para evitar exceder el límite de tamaño de documento de Firestore (1MB).
     */
    fun obtenerBitmapReducido(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

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
     * Codifica el Bitmap comprimido a una cadena de caracteres Base64.
     */
    fun convertirBitmapABase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Permite consultar el historial de fotos de un usuario distinto al actual (para perfiles públicos).
     */
    suspend fun obtenerFotosPorId(uid: String): List<FotoProgreso> {
        return try {
            val snapshot = db.collection("usuarios").document(uid)
                .collection("historial_fotos")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { doc ->
                FotoProgreso(
                    id = doc.id,
                    url = doc.getString("url") ?: "",
                    fecha = doc.getLong("fecha") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Función alternativa para subir fotos de progreso retornando un booleano de éxito.
     */
    suspend fun subirFotoProgreso(uri: Uri): Boolean {
        val uid = currentUserId ?: return false

        return try {
            val bitmap = obtenerBitmapReducido(uri) ?: return false
            val imagenBase64 = convertirBitmapABase64(bitmap)

            val fotoData = mapOf(
                "url" to imagenBase64,
                "fecha" to System.currentTimeMillis(),
                "esBase64" to true
            )

            db.collection("usuarios").document(uid)
                .collection("historial_fotos")
                .add(fotoData)
                .await()

            true
        } catch (e: Exception) {
            Log.e("FotosRepo", "Error al guardar la foto en Base64", e)
            false
        }
    }
}