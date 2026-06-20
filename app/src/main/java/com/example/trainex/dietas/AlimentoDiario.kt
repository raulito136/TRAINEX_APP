package com.example.trainex.dietas

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un registro de consumo en el diario nutricional.
 * Almacena la cantidad específica consumida y los macronutrientes totales calculados.
 */
@Entity(tableName = "diario_comidas")
data class AlimentoDiario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val marca: String,
    val imagen: String,
    val kcalTotales: Int,
    val protTotales: Double,
    val carbTotales: Double,
    val grasTotales: Double,
    val cantidadGramos: Double,
    /** Define el momento del día (Desayuno, Almuerzo, Cena, Snack). */
    val tipoComida: String,
    /** Fecha del registro en formato String (YYYY-MM-DD). */
    val fecha: String,
    val completado: Boolean = true,
    val esLiquido: Boolean = false
)