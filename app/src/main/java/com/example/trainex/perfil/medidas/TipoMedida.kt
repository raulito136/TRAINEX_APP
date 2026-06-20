package com.example.trainex.perfil.medidas

/**
 * Clase de datos que define una categoría de medición disponible en la aplicación.
 * Vincula las referencias de recursos necesarias para mostrar la medida en la interfaz de usuario.
 * * @property nombreResId Identificador del recurso de cadena (String Resource) para el nombre de la medida.
 * @property iconoResId Identificador del recurso gráfico (Drawable Resource) para el icono representativo.
 */
data class TipoMedida(
    val nombreResId: Int,
    val iconoResId: Int
)