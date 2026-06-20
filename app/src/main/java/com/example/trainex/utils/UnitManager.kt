package com.example.trainex.utils

import android.content.Context
import kotlin.math.roundToInt

/**
 * Objeto de utilidad para la gestión y conversión de unidades físicas (peso, distancia, longitud).
 * Permite transformar valores entre el sistema métrico (base de datos) y sistemas visuales (imperiales).
 */
object UnitManager {
    /** Nombre del archivo de preferencias para unidades. */
    private const val PREFS = "UnidadesPrefs"

    /** Factores de conversión constantes. */
    private const val KG_TO_LBS = 2.20462
    private const val KM_TO_MILES = 0.621371
    private const val CM_TO_IN = 0.393701 // 1 cm son 0.39 pulgadas

    /**
     * Convierte un valor de la unidad base (métrica) a la unidad visual seleccionada.
     * @param valor El valor numérico original (ej: en kg o cm).
     * @param unidad La unidad destino (ej: "lbs", "in", "miles").
     * @return El valor convertido y redondeado a un decimal.
     */
    fun aVisual(valor: Double, unidad: String): Double {
        return when (unidad) {
            "in" -> redondear(valor * CM_TO_IN)
            "lbs" -> redondear(valor * KG_TO_LBS)
            "miles" -> redondear(valor * KM_TO_MILES)
            else -> redondear(valor)
        }
    }

    /**
     * Convierte un valor visual (introducido por el usuario) a la unidad base de la base de datos.
     * @param valor El valor introducido por el usuario.
     * @param unidad La unidad en la que se introdujo (ej: "lbs", "in").
     * @return El valor convertido al sistema métrico base.
     */
    fun aBaseDeDatos(valor: Double, unidad: String): Double {
        return when (unidad) {
            "in" -> valor / CM_TO_IN
            "lbs" -> valor / KG_TO_LBS
            "miles" -> valor / KM_TO_MILES
            else -> valor
        }
    }

    /**
     * Guarda la preferencia de unidad de una métrica específica en SharedPreferences.
     * @param context Contexto de la aplicación.
     * @param clave Identificador de la métrica (ej: "unidad_peso").
     * @param valor El código de la unidad a guardar (ej: "kg").
     */
    fun guardar(context: Context, clave: String, valor: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(clave, valor).apply()
    }

    /**
     * Recupera la preferencia de unidad guardada para una métrica.
     * @param context Contexto de la aplicación.
     * @param clave Identificador de la métrica.
     * @param defecto Valor a retornar si no existe una preferencia guardada.
     * @return El código de la unidad almacenada.
     */
    fun obtener(context: Context, clave: String, defecto: String): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(clave, defecto) ?: defecto
    }

    /**
     * Función interna de redondeo para asegurar que los valores visuales tengan un solo decimal.
     * @param valor El número decimal.
     * @return El número redondeado a una cifra decimal.
     */
    private fun redondear(valor: Double): Double = (Math.round(valor * 10.0) / 10.0)
}