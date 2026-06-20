package com.example.trainex.serie

import androidx.room.Entity

/**
 * Representa la estructura de la tabla "historial_series" en la base de datos local (Room).
 * Se utiliza para persistir el rendimiento histórico de cada ejercicio realizado.
 *
 * @property ejercicioId Identificador del ejercicio, actúa como parte de la clave primaria.
 * @property numeroSerie Número correlativo de la serie, actúa como parte de la clave primaria compuesta.
 * @property kilogramos Valor del peso registrado en la base de datos local.
 * @property repeticiones Cantidad de repeticiones registradas en la base de datos local.
 */
@Entity(tableName = "historial_series", primaryKeys = ["ejercicioId", "numeroSerie"])
data class SerieEntidad(
    val ejercicioId: String,
    val numeroSerie: Int,
    val kilogramos: Double,
    val repeticiones: Int
)