package com.example.trainex.perfil.ajustes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trainex.R
import com.example.trainex.firebase.FirebaseMedidasRepository
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.perfil.Usuario
import com.example.trainex.perfil.UserManager
import com.example.trainex.perfil.medidas.RegistroMedida
import com.example.trainex.utils.UnitManager
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica de negocio para la edición de datos personales.
 */
class DatosPersonalesViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioRepo = FirebaseUsuarioRepository()
    private val medidasRepo = FirebaseMedidasRepository()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> get() = _usuario

    private val _guardadoExitoso = MutableLiveData<Boolean>()
    val guardadoExitoso: LiveData<Boolean> get() = _guardadoExitoso

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _erroresValidacion = MutableLiveData<Map<String, String?>>()
    val erroresValidacion: LiveData<Map<String, String?>> get() = _erroresValidacion

    private var pesoOriginalKg: Double = 0.0

    /**
     * Carga los datos del usuario y sincroniza el peso con el último registro del historial.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            val user = usuarioRepo.obtenerUsuario()
            // Obtenemos el historial de pesajes
            val historialPeso = usuarioRepo.obtenerHistorialPeso()

            user?.let { u ->
                // Buscamos el peso más reciente por fecha
                val ultimoRegistro = historialPeso.maxByOrNull { it.fecha }

                // Si existe un registro, actualizamos el peso del usuario antes de mostrarlo
                ultimoRegistro?.let {
                    u.peso = it.peso
                }

                pesoOriginalKg = u.peso
                _usuario.value = u
            }
        }
    }
    /**
     * Ejecuta validaciones y guarda los cambios en Firebase y almacenamiento local.
     */

    fun guardarCambios(
        nombre: String, apellidos: String, genero: String, edadStr: String,
        pesoStr: String, alturaStr: String, estiloVida: String, objetivo: String
    ) {
        val contexto = getApplication<Application>()
        val errores = mutableMapOf<String, String?>()

        // Validaciones básicas
        if (nombre.isEmpty()) errores["nombre"] = contexto.getString(R.string.error_nombre_obligatorio_ajustes)
        if (apellidos.isEmpty()) errores["apellidos"] = contexto.getString(R.string.error_apellidos_obligatorio_ajustes)

        val edadInt = edadStr.toIntOrNull()
        if (edadInt == null || edadInt <= 0 || edadInt > 120) {
            errores["edad"] = contexto.getString(R.string.error_edad_invalida_ajustes)
        }

        val pesoVisual = pesoStr.toDoubleOrNull()
        if (pesoVisual == null || pesoVisual <= 0) {
            errores["peso"] = contexto.getString(R.string.error_peso_invalido_ajustes)
        }

        val alturaDouble = alturaStr.toDoubleOrNull()
        if (alturaDouble == null || alturaDouble <= 0) {
            errores["altura"] = contexto.getString(R.string.error_altura_invalida_ajustes)
        }

        if (errores.isNotEmpty()) {
            _erroresValidacion.value = errores
            return
        }

        viewModelScope.launch {
            try {
                val unidadPeso = UnitManager.obtener(contexto, "unidad_peso", "kg")
                val pesoKg = UnitManager.aBaseDeDatos(pesoVisual!!, unidadPeso)

                val usuarioActualizado = _usuario.value!!.copy(
                    nombre = nombre, apellidos = apellidos, genero = genero,
                    edad = edadInt!!, peso = pesoKg, altura = alturaDouble!!,
                    estiloVida = estiloVida, objetivo = objetivo
                )

                usuarioRepo.guardarUsuario(usuarioActualizado)

                // Si el usuario cambia el peso manualmente, se registra en el historial
                if (pesoKg != pesoOriginalKg) {
                    usuarioRepo.registrarPeso(pesoKg)
                    medidasRepo.guardarMedida(RegistroMedida(
                        id = "", nombre = contexto.getString(R.string.medida_peso_corporal),
                        valor = pesoKg.toFloat(), fecha = System.currentTimeMillis()
                    ))
                }

                UserManager.saveUserData(contexto, mapOf(
                    "nombre" to nombre,
                    "peso" to pesoKg,
                    "altura" to alturaDouble,
                    "edad" to edadInt
                ))

                _guardadoExitoso.postValue(true)
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }
}