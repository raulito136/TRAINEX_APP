package com.example.trainex.perfil

/**
 * Clase de datos que representa una entrada única en el historial de peso del usuario.
 * Se utiliza para realizar el seguimiento del peso corporal a lo largo del tiempo.
 *
 * * @property peso El valor numérico del peso corporal.
 * @property fecha Marca de tiempo (timestamp) en milisegundos de cuándo se registró el peso.
 */
data class RegistroPeso(
    val peso: Double = 0.0,
    val fecha: Long = System.currentTimeMillis()
)