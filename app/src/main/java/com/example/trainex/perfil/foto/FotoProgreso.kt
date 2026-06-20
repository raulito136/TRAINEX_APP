package com.example.trainex.perfil.foto

/**
 * Clase de datos que representa una fotografía de progreso de un usuario.
 * * @property id Identificador único de la fotografía, generalmente vinculado a la base de datos.
 * @property url Dirección de la imagen. Puede ser una URL remota o una cadena en formato Base64.
 * @property fecha Marca de tiempo (timestamp) en milisegundos que indica cuándo se tomó la foto.
 */
data class FotoProgreso(
    var id: String = "",
    val url: String = "",
    val fecha: Long = 0L
)