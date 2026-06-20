package com.example.trainex.rutinas

import com.example.trainex.ejercicio.Ejercicio
import java.io.Serializable
import java.util.ArrayList

/**
 * Clase de datos que representa una rutina de entrenamiento completa.
 * Implementa [Serializable] para permitir el paso de objetos entre componentes de Android.
 * * @property firebaseId Identificador único generado por Firebase Firestore.
 * @property userId Identificador del usuario propietario de la rutina.
 * @property titulo Nombre descriptivo de la rutina.
 * @property ejercicios Lista de objetos [Ejercicio] que componen la rutina.
 * @property fechaCreacion Marca de tiempo (timestamp) de la creación inicial.
 * @property lastUpdate Marca de tiempo de la última modificación realizada.
 * @property id Identificador numérico opcional para uso local.
 */
data class Rutina(
    val firebaseId: String = "",
    val userId: String = "",
    val titulo: String = "",
    val ejercicios: ArrayList<Ejercicio> = ArrayList(),
    val fechaCreacion: Long = System.currentTimeMillis(),
    var lastUpdate: Long = System.currentTimeMillis(),
    val id: Long = 0
) : Serializable {

    /**
     * Constructor secundario vacío requerido por Firebase para la deserialización de objetos.
     */
    constructor() : this("", "", "", ArrayList(), System.currentTimeMillis(), System.currentTimeMillis(), 0)

    /**
     * Retorna la cantidad total de ejercicios contenidos en la rutina.
     * @return Número de elementos en la lista de ejercicios.
     */
    fun getCantidadEjercicios(): Int {
        return ejercicios.size
    }
}