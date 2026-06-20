package com.example.trainex.perfil.ajustes

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.trainex.utils.ModoUtils

/**
 * ViewModel que gestiona la persistencia y la aplicación del tema visual (Oscuro/Claro).
 */
class ModoViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPref = application.getSharedPreferences(ModoUtils.PREF_NAME, Context.MODE_PRIVATE)

    private val _modoActual = MutableLiveData<Int>()
    val modoActual: LiveData<Int> get() = _modoActual

    init {
        _modoActual.value = sharedPref.getInt(ModoUtils.KEY_MODO, ModoUtils.MODO_OSCURO)
    }

    /**
     * Cambia el modo de la aplicación, guarda la preferencia y aplica el cambio al sistema.
     * @param nuevoModo 0 para modo noche (YES), 1 para modo día (NO).
     */
    fun cambiarModo(nuevoModo: Int) {
        sharedPref.edit().putInt(ModoUtils.KEY_MODO, nuevoModo).apply()

        _modoActual.value = nuevoModo

        if (nuevoModo == ModoUtils.MODO_OSCURO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}