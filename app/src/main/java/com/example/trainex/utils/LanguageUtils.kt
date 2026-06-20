package com.example.trainex.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

/**
 * Objeto de utilidad para la gestión del idioma y la internacionalización de la aplicación.
 * Permite guardar la preferencia del usuario y aplicarla de forma global en los recursos de la app.
 */
object LanguageUtils {
    // Nombre del archivo de SharedPreferences consistente con el resto de utilidades
    private const val PREF_FILE = "trainex_prefs"
    private const val IDIOMA_KEY = "idioma"

    /**
     * Guarda el código de idioma seleccionado por el usuario en el almacenamiento persistente.
     * @param context Contexto de la aplicación.
     * @param codigoIdioma Código del idioma (ej: "es", "en").
     */
    fun guardarIdioma(context: Context, codigoIdioma: String) {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        // Usamos commit() para asegurar que la escritura sea instantánea antes de un posible reinicio
        prefs.edit().putString(IDIOMA_KEY, codigoIdioma).commit()
    }

    /**
     * Recupera el código del idioma guardado actualmente.
     * @param context Contexto de la aplicación.
     * @return El código del idioma guardado o "es" (español) por defecto.
     */
    fun getIdiomaActual(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return prefs.getString(IDIOMA_KEY, "es") ?: "es"
    }

    /**
     * Aplica el idioma guardado a la configuración global de recursos del sistema.
     * Este método debe llamarse antes de 'setContentView' en las actividades.
     * @param context Contexto de la actividad o aplicación.
     */
    fun aplicarIdioma(context: Context) {
        val idioma = getIdiomaActual(context)
        val locale = java.util.Locale(idioma)
        java.util.Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}