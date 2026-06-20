package com.example.trainex.dietas

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Entidad que representa un alimento en la base de datos local y Firebase.
 * @property id Identificador autogenerado para la base de datos local Room.
 * @property firebaseId Identificador único del documento en Firestore.
 * @property userId ID del usuario que creó el alimento (si es personal).
 * @property imagen Cadena que puede ser una URL o una imagen codificada en Base64.
 * @property nombre Nombre descriptivo del producto.
 * @property marca Empresa o fabricante del alimento.
 * @property categoria Clasificación del producto (frutas, lácteos, etc.).
 * @property calorias Cantidad de energía por cada 100g/ml.
 * @property proteinas Gramos de proteína por cada 100g/ml.
 * @property carbohidratos Gramos de hidratos de carbono por cada 100g/ml.
 * @property grasas Gramos de lípidos por cada 100g/ml.
 * @property esLiquido Indica si la unidad de medida es mililitros o gramos.
 */
@Entity(tableName = "alimentos", indices = [Index(value = ["firebaseId"], unique = true)])
data class Alimento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firebaseId: String = "",
    val userId: String = "",
    val imagen: String,
    val nombre: String,
    val marca: String,
    val categoria: String,
    val calorias: Int,
    val proteinas: Double,
    val carbohidratos: Double,
    val grasas: Double,
    val esLiquido: Boolean = false
) : Serializable {
    /** Constructor vacío necesario para la deserialización automática de Firebase. */
    constructor() : this(0, "", "", "", "", "", "", 0, 0.0, 0.0, 0.0, false)
}