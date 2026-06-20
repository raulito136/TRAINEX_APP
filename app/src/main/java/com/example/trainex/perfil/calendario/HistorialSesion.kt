package com.example.trainex.perfil.calendario

import com.example.trainex.ejercicio.Ejercicio

/**
 * Modelo de datos que representa el resumen de una sesión de entrenamiento específica.
 * Se utiliza para mostrar la información en el BottomSheet de resumen diario.
 */
data class HistorialSesion(
    val nombreRutina: String,
    val tiempoDuracion: String, // Ejemplo: "45 min"
    val ejerciciosRealizados: List<Ejercicio>
)