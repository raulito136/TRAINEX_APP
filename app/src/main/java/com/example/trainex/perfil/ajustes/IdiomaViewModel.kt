package com.example.trainex.perfil.ajustes

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trainex.utils.LanguageUtils

/**
 * ViewModel encargado de la lógica de cambio de idioma de la aplicación.
 * Utiliza AndroidViewModel para tener acceso al contexto de la aplicación.
 */
class IdiomaViewModel(application: Application) : AndroidViewModel(application) {

    // Canal de comunicación para avisar a la Activity que debe reiniciar la app
    private val _reiniciarApp = MutableLiveData<String>()
    val reiniciarApp: LiveData<String> get() = _reiniciarApp

    /**
     * Procesa la selección de un nuevo idioma.
     * @param codigo El código del idioma (ej. "es" para español, "en" para inglés).
     */
    fun seleccionarIdioma(codigo: String) {
        // 1. GUARDAR el idioma en SharedPreferences para que sea persistente tras cerrar la app
        LanguageUtils.guardarIdioma(getApplication(), codigo)

        // 2. APLICAR el idioma mediante el delegado de AppCompat (compatible con Android 13+)
        // Esto cambia la configuración de recursos de la aplicación dinámicamente
        val appLocales: LocaleListCompat = LocaleListCompat.forLanguageTags(codigo)
        AppCompatDelegate.setApplicationLocales(appLocales)

        // 3. NOTIFICAR a la Activity mediante el LiveData para que realice el reinicio manual del flujo
        _reiniciarApp.value = codigo
    }
}