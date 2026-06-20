package com.example.trainex.ejercicio

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.trainex.serie.Serie
import java.io.Serializable

/**
 * Representa la tabla "ejercicios" en la base de datos local.
 * Implementa Serializable para que podamos enviarlo de una pantalla a otra fácilmente.
 */
@Entity(tableName = "ejercicios")
data class Ejercicio(
    @PrimaryKey var id: Int = 0,             // ID único, esencial para Room
    var nombre: String = "",                 // Clave para buscar el nombre traducido
    var grupoMuscular: String = "",          // Clave para buscar el grupo muscular
    var imagen: String = "",                 // Nombre del archivo de imagen (sin extensión)
    var descripcion: String = "",            // Clave para la descripción larga
    var dificultad: String = "Principiante", // Nivel de esfuerzo requerido
    var urlVideo: String = "",               // Link opcional (ej. de Firebase o Local)

    @Ignore // Room no guardará este campo en la tabla, solo sirve para la lógica en memoria
    var series: List<Serie>? = null
) : Serializable