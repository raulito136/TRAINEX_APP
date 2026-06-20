package com.example.trainex.serie

import java.io.Serializable

/**
 * Clase de datos que representa una serie individual dentro de un ejercicio de entrenamiento.
 * Se utiliza para el transporte de datos y la lógica de negocio en la aplicación.
 * Implementa [Serializable] para facilitar su paso entre actividades y fragmentos.
 *
 * @property id Identificador único de la serie.
 * @property ejercicioId Identificador del ejercicio al que pertenece esta serie.
 * @property numeroSerie Índice o posición de la serie dentro del ejercicio (ej: Serie 1, 2, etc.).
 * @property kilogramos Cantidad de peso levantado en esta serie.
 * @property repeticiones Número de ejecuciones completadas en esta serie.
 */
data class Serie(
    val id: Long = 0,
    val ejercicioId: Int = 0,
    var numeroSerie: Int = 0,
    var kilogramos: Double = 0.0,
    var repeticiones: Int = 0
) : Serializable