package com.example.trainex.perfil

import android.content.Context

/**
 * Objeto singleton encargado de gestionar la persistencia local de los datos del usuario.
 * Utiliza SharedPreferences para almacenar información de perfil y configuraciones de entrenamiento.
 *
 */
object UserManager {
    private const val PREF_NAME = "user_prefs"

    // Claves existentes
    private const val KEY_USERNAME = "username"
    private const val KEY_PESO = "peso"
    private const val KEY_ALTURA = "altura"
    private const val KEY_EDAD = "edad"
    private const val KEY_FOTO = "foto_perfil"

    private const val KEY_DIAS_ENTRENO = "dias_entreno"
    private const val KEY_HORA_ENTRENO = "hora_entreno"
    private const val KEY_NOTIF_ENTRENO_ACTIVA = "notif_entreno_active"

    /**
     * Guarda de forma masiva los datos básicos del perfil en las preferencias locales.
     *
     * * @param context Contexto de la aplicación.
     * @param data Mapa que contiene los pares clave-valor de la información del usuario.
     */
    fun saveUserData(context: Context, data: Map<String, Any>) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        (data["username"] as? String)?.let { editor.putString(KEY_USERNAME, it) }
        (data["peso"] as? Double)?.let { editor.putFloat(KEY_PESO, it.toFloat()) }
        (data["altura"] as? Double)?.let { editor.putFloat(KEY_ALTURA, it.toFloat()) }
        (data["edad"] as? Int)?.let { editor.putInt(KEY_EDAD, it) }
        (data["foto"] as? String)?.let { editor.putString(KEY_FOTO, it) }

        editor.apply()
    }

    /**
     * Almacena la configuración de días y horas programadas para el entrenamiento.
     *
     * * @param dias Cadena con los días seleccionados (ej: "Lunes,Miércoles").
     * @param hora Cadena con la hora del recordatorio (ej: "18:30").
     * @param activo Estado de las notificaciones de entrenamiento.
     */
    fun guardarHorarioEntrenamiento(context: Context, dias: String, hora: String, activo: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_DIAS_ENTRENO, dias)
            putString(KEY_HORA_ENTRENO, hora)
            putBoolean(KEY_NOTIF_ENTRENO_ACTIVA, activo)
            apply()
        }
    }

    /**
     * Recupera la configuración actual del horario de entrenamiento.
     *
     * * @return Un Triple que contiene (días, hora, estado activo).
     */
    fun obtenerHorarioEntrenamiento(context: Context): Triple<String, String, Boolean> {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val dias = sharedPreferences.getString(KEY_DIAS_ENTRENO, "") ?: ""
        val hora = sharedPreferences.getString(KEY_HORA_ENTRENO, "10:00") ?: "10:00"
        val activo = sharedPreferences.getBoolean(KEY_NOTIF_ENTRENO_ACTIVA, false)
        return Triple(dias, hora, activo)
    }

    /**
     * Obtiene un objeto consolidado del perfil de usuario desde las preferencias.
     *
     */
    fun getUserProfile(context: Context): UserProfile {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return UserProfile(
            username = sharedPreferences.getString(KEY_USERNAME, "Usuario") ?: "Usuario",
            peso = sharedPreferences.getFloat(KEY_PESO, 70.0f).toDouble(),
            altura = sharedPreferences.getFloat(KEY_ALTURA, 1.75f).toDouble(),
            edad = sharedPreferences.getInt(KEY_EDAD, 25),
            foto = sharedPreferences.getString(KEY_FOTO, "") ?: "",
            objetivo = null
        )
    }

    /**
     * Clase de datos interna para representar el perfil simplificado del usuario.
     *
     */
    data class UserProfile(
        val username: String,
        val peso: Double,
        val altura: Double,
        val edad: Int,
        val foto: String,
        val objetivo: String?
    )
}