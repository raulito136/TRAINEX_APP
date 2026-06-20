package com.example.trainex.perfil.calendario

import java.time.LocalDate

/**
 * Representa los diferentes tipos de elementos que pueden aparecer en la lista del calendario.
 * Utiliza una clase sellada (sealed class) para garantizar la seguridad de tipos.
 */
sealed class CalendarioItem {

    /** Representa el título de un mes (ej: "Enero 2024") */
    data class MonthHeader(val name: String) : CalendarioItem()

    /** * Representa un día específico del mes.
     * @param date Objeto LocalDate con la fecha.
     * @param tieneEntreno Booleano que indica si hubo actividad ese día.
     */
    data class Day(val date: LocalDate, val tieneEntreno: Boolean = false) : CalendarioItem()

    /** Objeto para rellenar los huecos iniciales cuando el mes no empieza en lunes/domingo */
    object Empty : CalendarioItem()
}