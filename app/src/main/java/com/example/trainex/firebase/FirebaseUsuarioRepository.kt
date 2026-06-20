package com.example.trainex.firebase

import android.util.Log
import com.example.trainex.perfil.RegistroPeso
import com.example.trainex.perfil.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Repositorio encargado de la gestión de datos de usuario en Firebase Firestore.
 * Incluye la gestión del perfil básico, historial de peso, funciones sociales (seguidores/seguidos)
 * y actualización de credenciales visuales.
 */
class FirebaseUsuarioRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("usuarios")

    // --- SECCIÓN: USUARIO BÁSICO ---

    /**
     * Guarda o actualiza la información del perfil general del usuario.
     * Utiliza el UID de autenticación como identificador único del documento.
     */
    suspend fun guardarUsuario(usuario: Usuario) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                collection.document(uid).set(usuario.copy(id = uid)).await()
                Log.d("UsuarioRepo", "Perfil guardado correctamente en la nube")
            } catch (e: Exception) {
                Log.e("UsuarioRepo", "Error al guardar perfil", e)
                throw e
            }
        }
    }

    /**
     * Recupera los datos del perfil del usuario actualmente autenticado.
     * @return Objeto Usuario o null si no se encuentra o no hay sesión iniciada.
     */
    suspend fun obtenerUsuario(): Usuario? {
        val uid = auth.currentUser?.uid
        return if (uid != null) {
            try {
                val snapshot = collection.document(uid).get().await()
                snapshot.toObject(Usuario::class.java)
            } catch (e: Exception) {
                Log.e("UsuarioRepo", "Error al obtener perfil", e)
                null
            }
        } else {
            null
        }
    }

    /**
     * Verifica si un nombre de usuario ya está en uso por otro atleta.
     * @param username Nombre a comprobar.
     * @return true si el nombre ya existe, false si está disponible.
     */
    suspend fun existeNombreUsuario(username: String): Boolean {
        return try {
            val snapshot = collection
                .whereEqualTo("username", username)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e("CHECK_USER", "ERROR CRÍTICO AL BUSCAR USUARIO", e)
            true
        }
    }

    // --- SECCIÓN: GESTIÓN DE PESO (HISTORIAL Y GRÁFICA) ---

    /**
     * Registra un nuevo peso en el historial.
     * Implementa una lógica de limpieza para asegurar que solo exista un registro de peso por día.
     * @param nuevoPeso Valor numérico del peso.
     * @param fechaMs Fecha del registro en milisegundos.
     */
    suspend fun registrarPeso(nuevoPeso: Double, fechaMs: Long = System.currentTimeMillis()) {
        val uid = auth.currentUser?.uid ?: return

        try {
            val subCollection = collection.document(uid).collection("historial_peso")

            /** Calculamos el rango del día para detectar registros existentes */
            val (inicioDia, finDia) = obtenerRangoDia(fechaMs)

            val snapshot = subCollection
                .whereGreaterThanOrEqualTo("fecha", inicioDia)
                .whereLessThanOrEqualTo("fecha", finDia)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                /** Si ya existen registros hoy, actualizamos el primero encontrado */
                val documentos = snapshot.documents
                val idDocPrincipal = documentos[0].id
                subCollection.document(idDocPrincipal).update(
                    mapOf(
                        "peso" to nuevoPeso,
                        "fecha" to fechaMs
                    )
                ).await()
                Log.d("UsuarioRepo", "Peso actualizado.")

                /** Limpieza de duplicados: Elimina cualquier otro registro del mismo día */
                if (documentos.size > 1) {
                    for (i in 1 until documentos.size) {
                        subCollection.document(documentos[i].id).delete()
                        Log.d("UsuarioRepo", "Duplicado eliminado: ${documentos[i].id}")
                    }
                }

            } else {
                /** Si no hay registros previos hoy, creamos una nueva entrada */
                val registro = mapOf(
                    "peso" to nuevoPeso,
                    "fecha" to fechaMs
                )
                subCollection.add(registro).await()
                Log.d("UsuarioRepo", "Nuevo registro de peso creado.")
            }

            /** Sincroniza el peso actual en el documento principal del perfil */
            collection.document(uid).update("peso", nuevoPeso).await()

        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al registrar nuevo peso", e)
        }
    }

    /**
     * Recupera el historial de peso completo del usuario logueado para generar gráficas.
     */
    suspend fun obtenerHistorialPeso(): List<RegistroPeso> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = collection.document(uid)
                .collection("historial_peso")
                .orderBy("fecha")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val peso = doc.getDouble("peso") ?: 0.0
                val fecha = doc.getLong("fecha") ?: 0L
                RegistroPeso(peso, fecha)
            }
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al obtener historial", e)
            emptyList()
        }
    }

    /**
     * Elimina cualquier registro de peso asociado a una fecha específica (rango de 24h).
     */
    suspend fun eliminarPesoPorFecha(fechaMs: Long) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val (inicio, fin) = obtenerRangoDia(fechaMs)
            val subCollection = collection.document(uid).collection("historial_peso")

            val snapshot = subCollection
                .whereGreaterThanOrEqualTo("fecha", inicio)
                .whereLessThanOrEqualTo("fecha", fin)
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al eliminar peso", e)
        }
    }

    // --- SECCIÓN: UTILIDADES Y CONSULTAS EXTERNAS ---

    /**
     * Calcula los milisegundos de inicio (00:00) y fin (23:59) de un día concreto.
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
     * Obtiene el perfil de cualquier usuario mediante su identificador único.
     */
    suspend fun obtenerUsuarioPorId(uid: String): Usuario? {
        return try {
            val snapshot = collection.document(uid).get().await()
            snapshot.toObject(Usuario::class.java)
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al obtener perfil público", e)
            null
        }
    }

    /**
     * Recupera el historial de peso de otro usuario para visualizar su progreso.
     */
    suspend fun obtenerHistorialPesoPorId(uid: String): List<RegistroPeso> {
        return try {
            val snapshot = collection.document(uid)
                .collection("historial_peso")
                .orderBy("fecha")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val peso = doc.getDouble("peso") ?: 0.0
                val fecha = doc.getLong("fecha") ?: 0L
                RegistroPeso(peso, fecha)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Actualiza el nombre de usuario del perfil actual.
     */
    suspend fun actualizarUsername(usuario: Usuario, nuevoUsername: String): Boolean {
        return try {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val usuarioActualizado = usuario.copy(username = nuevoUsername)
                collection.document(uid).set(usuarioActualizado).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al actualizar username", e)
            false
        }
    }

    // --- SECCIÓN: FUNCIONES SOCIALES (FOLLOW SYSTEM) ---

    /**
     * Establece una relación de seguimiento entre el usuario actual y un objetivo.
     * Utiliza una transacción Batch para asegurar que ambas listas (Siguiendo y Seguidores) se actualicen.
     */
    suspend fun seguirUsuario(idUsuarioObjetivo: String, datosUsuarioObjetivo: Usuario) {
        val miId = auth.currentUser?.uid ?: return
        val miUsuario = obtenerUsuario() ?: return

        val batch = db.batch()

        /** 1. Añade el perfil objetivo a la colección 'siguiendo' del usuario actual */
        val dataSiguiendo = mapOf(
            "id" to idUsuarioObjetivo,
            "username" to datosUsuarioObjetivo.username,
            "timestamp" to System.currentTimeMillis()
        )
        val refSiguiendo = db.collection("usuarios").document(miId)
            .collection("siguiendo").document(idUsuarioObjetivo)
        batch.set(refSiguiendo, dataSiguiendo)

        /** 2. Añade el perfil del usuario actual a la colección 'seguidores' del objetivo */
        val dataSeguidor = mapOf(
            "id" to miId,
            "username" to miUsuario.username,
            "timestamp" to System.currentTimeMillis()
        )
        val refSeguidores = db.collection("usuarios").document(idUsuarioObjetivo)
            .collection("seguidores").document(miId)
        batch.set(refSeguidores, dataSeguidor)

        batch.commit().await()
    }

    /**
     * Elimina la relación de seguimiento entre ambos usuarios mediante una transacción Batch.
     */
    suspend fun dejarDeSeguirUsuario(idUsuarioObjetivo: String) {
        val miId = auth.currentUser?.uid ?: return

        val batch = db.batch()

        val refSiguiendo = db.collection("usuarios").document(miId)
            .collection("siguiendo").document(idUsuarioObjetivo)
        batch.delete(refSiguiendo)

        val refSeguidores = db.collection("usuarios").document(idUsuarioObjetivo)
            .collection("seguidores").document(miId)
        batch.delete(refSeguidores)

        batch.commit().await()
    }

    /**
     * Comprueba si el usuario logueado sigue actualmente al usuario especificado.
     */
    suspend fun estoySiguiendoA(idUsuarioObjetivo: String): Boolean {
        val miId = auth.currentUser?.uid ?: return false
        val doc = db.collection("usuarios").document(miId)
            .collection("siguiendo").document(idUsuarioObjetivo)
            .get().await()
        return doc.exists()
    }

    /**
     * Obtiene la lista de usuarios que el atleta actual está siguiendo.
     */
    suspend fun obtenerUsuariosSeguidos(): List<Usuario> {
        val miId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("usuarios").document(miId)
            .collection("siguiendo")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()

        return snapshot.documents.map { doc ->
            Usuario(
                id = doc.getString("id") ?: "",
                username = doc.getString("username") ?: "",
                peso = 0.0, altura = 0.0, edad = 0
            )
        }
    }

    /**
     * Retorna el número total de personas que siguen a un usuario concreto.
     */
    suspend fun contarSeguidores(userId: String): Int {
        val snapshot = db.collection("usuarios").document(userId)
            .collection("seguidores").get().await()
        return snapshot.size()
    }

    /**
     * Retorna el número total de personas a las que sigue un usuario concreto.
     */
    suspend fun contarSeguidos(userId: String): Int {
        val snapshot = db.collection("usuarios").document(userId)
            .collection("siguiendo").get().await()
        return snapshot.size()
    }

    /**
     * Recupera la lista detallada de seguidores (usuarios que siguen al ID proporcionado).
     */
    suspend fun obtenerListaSeguidores(userId: String): List<Usuario> {
        return try {
            val snapshot = db.collection("usuarios").document(userId)
                .collection("seguidores")
                .get().await()

            snapshot.documents.map { doc ->
                Usuario(
                    id = doc.getString("id") ?: "",
                    username = doc.getString("username") ?: "",
                    peso = 0.0, altura = 0.0, edad = 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Recupera la lista detallada de seguidos (usuarios a los que sigue el ID proporcionado).
     */
    suspend fun obtenerListaSeguidos(userId: String): List<Usuario> {
        return try {
            val snapshot = db.collection("usuarios").document(userId)
                .collection("siguiendo")
                .get().await()

            snapshot.documents.map { doc ->
                Usuario(
                    id = doc.getString("id") ?: "",
                    username = doc.getString("username") ?: "",
                    peso = 0.0, altura = 0.0, edad = 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Actualiza la foto de perfil del usuario mediante una cadena de texto en formato Base64.
     */
    suspend fun actualizarFotoPerfil(base64: String): Boolean {
        return try {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                collection.document(uid).update("foto", base64).await()
                true
            } else false
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al actualizar foto", e)
            false
        }
    }

    /**
     * Persiste un registro de peso inicial directamente en la subcolección del usuario.
     */
    suspend fun guardarPesoEnHistorial(registro: RegistroPeso) {
        val uid = auth.currentUser?.uid ?: return
        try {
            collection.document(uid)
                .collection("historial_peso")
                .add(registro)
                .await()
            Log.d("UsuarioRepo", "Peso inicial guardado en el historial para la gráfica")
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al guardar peso inicial", e)
        }
    }
}