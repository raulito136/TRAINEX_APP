package com.example.trainex.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Clase encargada de centralizar el acceso a SharedPreferences.
 * Proporciona métodos genéricos de lectura y escritura para diferentes tipos de datos.
 * @property context Contexto necesario para acceder a las preferencias.
 */
class SharedPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("trainex_prefs", Context.MODE_PRIVATE)

    /**
     * Guarda un valor booleano en las preferencias.
     */
    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    /**
     * Obtiene un valor booleano o el valor por defecto si la clave no existe.
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    /**
     * Guarda una cadena de texto en las preferencias.
     */
    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    /**
     * Obtiene una cadena de texto o el valor por defecto si la clave no existe.
     */
    fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Guarda un valor entero en las preferencias.
     */
    fun saveInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    /**
     * Obtiene un valor entero o el valor por defecto si la clave no existe.
     */
    fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    /**
     * Elimina todos los datos almacenados en el archivo de preferencias.
     */
    fun clear() {
        prefs.edit().clear().apply()
    }

    /**
     * Obtiene el estado de las notificaciones generales.
     * @return true si están habilitadas (por defecto), false en caso contrario.
     */
    fun getNotificacionesGenerales(): Boolean {
        return prefs.getBoolean("generales_enabled", true)
    }

    /**
     * Actualiza el estado de las notificaciones generales.
     * @param value Estado booleano a guardar.
     */
    fun setNotificacionesGenerales(value: Boolean) {
        prefs.edit().putBoolean("generales_enabled", value).apply()
    }
}