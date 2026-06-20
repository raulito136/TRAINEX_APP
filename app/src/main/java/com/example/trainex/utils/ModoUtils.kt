package com.example.trainex.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Objeto de utilidad para gestionar el tema visual de la aplicación (Modo Oscuro o Modo Claro).
 */
object ModoUtils {
        const val PREF_NAME = "trainex_prefs"
        const val KEY_MODO = "modo"
        const val MODO_OSCURO = 0
        const val MODO_CLARO = 1

    /**
     * Recupera la preferencia de tema visual del almacenamiento y la aplica a la aplicación
     * utilizando AppCompatDelegate.
     * @param context Contexto de la aplicación.
     */
    fun aplicarModoGuardado(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val modo = sharedPref.getInt(KEY_MODO, MODO_OSCURO)
        when (modo) {
            MODO_OSCURO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            MODO_CLARO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}