package com.example.trainex.perfil.medidas

/**
 * Clase de datos que representa una entrada específica en el historial de medidas del usuario.
 * Se utiliza para almacenar valores numéricos asociados a una métrica y una fecha concreta.
 * * @property id Identificador único del registro en la base de datos (Firebase).
 * @property userId Identificador del usuario al que pertenece la medición.
 * @property nombre Nombre o etiqueta de la medida (ej. "Bíceps Izquierdo", "Peso").
 * @property fecha Marca de tiempo (timestamp) en milisegundos que indica cuándo se realizó la toma.
 * @property valor Valor numérico de la medición (puede estar en la unidad base o visual según el contexto).
 */
data class RegistroMedida(
    var id: String = "",
    var userId: String = "",
    val nombre: String = "",
    val fecha: Long = 0L,
    val valor: Float = 0f
)