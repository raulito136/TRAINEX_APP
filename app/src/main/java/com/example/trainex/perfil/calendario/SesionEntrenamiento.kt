package com.example.trainex.perfil.calendario

import com.example.trainex.ejercicio.Ejercicio

/**
 * Clase de datos que representa una sesión de entrenamiento completa tal cual se guarda en Firebase.
 * Almacena metadatos del usuario, tiempos, fechas y la lista completa de ejercicios con sus resultados.
 */
data class SesionEntrenamiento(
    /** Identificador único del documento en Firestore */
    val id: String = "",
    /** ID del usuario que realizó el entrenamiento */
    val userId: String = "",
    /** Nombre de la rutina que se utilizó como base */
    val nombreRutina: String = "",
    /** Duración total del entrenamiento en milisegundos */
    val tiempoMs: Long = 0,
    /** Marca de tiempo (timestamp) del momento en que se guardó la sesión */
    val fecha: Long = 0,
    /** Lista de ejercicios realizados con sus series y repeticiones registradas */
    val ejercicios: List<Ejercicio> = emptyList()
)