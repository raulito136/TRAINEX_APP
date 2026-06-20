package com.example.trainex.iniciarRutina

import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.serie.Serie

/**
 * Clase de datos que vincula un objeto [Ejercicio] con una lista mutable de sus [Serie]s.
 * Se utiliza para representar cada bloque de ejercicio en la interfaz de tablas.
 */
data class EjercicioConSeries(
    val ejercicio: Ejercicio,
    var series: MutableList<Serie>
)