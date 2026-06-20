package com.example.trainex.perfil

/**
 * Clase de datos que representa el modelo de usuario principal en el sistema.
 * Contiene toda la información biométrica y de perfil almacenada en Firebase.
 *
 * * @property id Identificador único del usuario (UID) vinculado a Firebase Auth.
 * @property username Nombre de usuario público.
 * @property nombre Nombre real del usuario.
 * @property apellidos Apellidos del usuario.
 * @property edad Edad del usuario.
 * @property peso Peso actual del usuario en kilogramos (unidad base).
 * @property altura Altura del usuario.
 * @property genero Género biológico del usuario.
 * @property estiloVida Descripción de la actividad física diaria (ej: sedentario, activo).
 * @property objetivo Meta principal del usuario (ej: pérdida de peso, ganar músculo).
 * @property foto Cadena en formato Base64 o URL que representa la imagen de perfil.
 */
data class Usuario(
    var id: String = "",
    val username: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val edad: Int = 0,
    var peso: Double = 0.0,
    val altura: Double = 0.0,
    val genero: String = "",
    val estiloVida: String = "",
    val objetivo: String = "",
    val foto: String = ""
)