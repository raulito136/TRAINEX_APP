package com.example.trainex.perfil.medidas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.firebase.FirebaseMedidasRepository
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.utils.UnitManager
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel encargado de la lógica de negocio para la pantalla de detalles de una medida específica.
 * Maneja la carga, conversión de unidades, persistencia en Firebase y el estado de la interfaz.
 * * @property application Contexto de la aplicación para acceder a preferencias de usuario y utilidades.
 */
class DetalleMedidaViewModel(application: Application) : AndroidViewModel(application) {

    private val repoMedidas = FirebaseMedidasRepository()
    private val repoUsuario = FirebaseUsuarioRepository()
    private val context = application.applicationContext

    private val _uiState = MutableStateFlow<DetalleMedidaUiState>(DetalleMedidaUiState.Loading)
    /** Estado de flujo que representa la situación actual de la UI (Cargando, Vacío, Éxito o Error). */
    val uiState: StateFlow<DetalleMedidaUiState> = _uiState

    private val _config = MutableStateFlow(ConfigMedidaUI())
    /** Configuración visual que define las unidades y límites según el tipo de medida seleccionada. */
    val config: StateFlow<ConfigMedidaUI> = _config

    /**
     * Recupera los datos de la medida y configura el estado de la pantalla.
     * @param nombreMedida Nombre de la métrica (ej. "Peso", "Bíceps").
     * @param userId ID opcional del usuario (para visualización de perfiles externos).
     */
    fun cargarDatos(nombreMedida: String, userId: String?) {
        viewModelScope.launch {
            _uiState.value = DetalleMedidaUiState.Loading
            try {
                val unidadPesoPref = UnitManager.obtener(context, "unidad_peso", "kg")
                // ¡CAMBIO AQUÍ! Cambiamos "unidad_medida" por "unidad_longitud" para que coincida con Ajustes
                val unidadMedidaPref = UnitManager.obtener(context, "unidad_longitud", "cm")

                val (tipoBase, limiteBase) = detectarTipoMedida(nombreMedida)

                // Determinamos qué unidad visual usar
                val unidadVisual = when (tipoBase) {
                    "kg" -> unidadPesoPref
                    "cm" -> unidadMedidaPref
                    else -> tipoBase
                }

                val limiteVisual = if (tipoBase == "kg" || tipoBase == "cm")
                    UnitManager.aVisual(limiteBase.toDouble(), unidadVisual).toFloat()
                else limiteBase

                _config.value = ConfigMedidaUI(unidadVisual, limiteVisual, tipoBase)

                // Pasamos la unidadVisual para que el historial ya venga convertido
                val historial = obtenerHistorial(nombreMedida, userId, tipoBase, unidadVisual)

                if (historial.isEmpty()) {
                    _uiState.value = DetalleMedidaUiState.Empty
                } else {
                    val chartEntries = historial.sortedBy { it.fecha }.mapIndexed { index, reg ->
                        Entry(index.toFloat(), reg.valor)
                    }
                    _uiState.value = DetalleMedidaUiState.Success(historial, chartEntries)
                }
            } catch (e: Exception) {
                // Manejo de errores omitido según código original
            }
        }
    }

    /**
     * Obtiene los registros históricos desde el repositorio correspondiente y aplica conversión de unidades.
     */
    private suspend fun obtenerHistorial(nombre: String, userId: String?, tipo: String, unidadVisual: String): List<RegistroMedida> {
        val nombreLower = nombre.lowercase()
        val esPeso = tipo == "kg" && (nombreLower.contains("peso") || nombreLower.contains("weight"))

        return if (esPeso) {
            // Obtenemos de la tabla de peso (PesoRegistro)
            val rawPeso = if (userId != null) repoUsuario.obtenerHistorialPesoPorId(userId) else repoUsuario.obtenerHistorialPeso()
            rawPeso.map { item ->
                RegistroMedida(
                    id = "",
                    userId = userId ?: "",
                    nombre = nombre,
                    fecha = item.fecha, // Ahora el compilador lo reconoce perfectamente
                    valor = UnitManager.aVisual(item.peso, unidadVisual).toFloat()
                )
            }
        } else {
            // Obtenemos de la tabla de medidas (RegistroMedida)
            val rawMedidas = if (userId != null) repoMedidas.obtenerMedidasPorNombreEId(nombre, userId) else repoMedidas.obtenerMedidasPorNombre(nombre)
            rawMedidas.map { item ->
                val valorConvertido = if (tipo == "cm" || tipo == "kg") {
                    UnitManager.aVisual(item.valor.toDouble(), unidadVisual).toFloat()
                } else {
                    item.valor
                }
                item.copy(valor = valorConvertido)
            }
        }
    }

    /**
     * Guarda una nueva medición en Firebase, convirtiendo el valor visual a la unidad base de datos.
     * * @param valorVisual Valor ingresado por el usuario en su unidad preferida.
     * @param fecha Timestamp de la medición.
     * @param nombre Nombre de la medida a guardar.
     */
    fun guardarMedida(valorVisual: Float, fecha: Long, nombre: String) {
        viewModelScope.launch {
            val conf = _config.value

            // Convertimos lo que el usuario escribió (ej: 2 in) a la base de datos (ej: 5.08 cm)
            val valorBD = if (conf.tipoBase == "cm" || conf.tipoBase == "kg") {
                UnitManager.aBaseDeDatos(valorVisual.toDouble(), conf.unidadVisual).toFloat()
            } else {
                valorVisual
            }

            val medida = RegistroMedida(
                nombre = nombre,
                valor = valorBD,
                fecha = fecha
            )

            repoMedidas.guardarMedida(medida)

            // Si es peso, también guardamos en la tabla de peso del usuario
            if (conf.tipoBase == "kg" && (nombre.lowercase().contains("peso") || nombre.lowercase().contains("weight"))) {
                repoUsuario.registrarPeso(valorBD.toDouble(), fecha)
            }

            cargarDatos(nombre, null)
        }
    }

    /**
     * Elimina un registro específico identificándolo por su tipo y fecha/ID.
     */
    fun eliminarMedida(medida: RegistroMedida) {
        viewModelScope.launch {
            val nombreLower = medida.nombre.lowercase()
            if (nombreLower.contains("peso") || nombreLower.contains("weight")) {
                repoUsuario.eliminarPesoPorFecha(medida.fecha)
            } else {
                repoMedidas.eliminarMedida(medida.id)
            }
            cargarDatos(medida.nombre, null)
        }
    }

    /**
     * Clasifica el tipo de medida y establece un límite máximo para validación de entrada.
     */
    private fun detectarTipoMedida(nombre: String): Pair<String, Float> {
        val n = nombre.lowercase(Locale.ROOT)
        return when {
            n.contains("grasa") || n.contains("fat") || n.contains("%") -> "%" to 100f
            n.contains("peso") || n.contains("weight") || n.contains("press") || n.contains("squat") ||
                    n.contains("deadlift") || n.contains("curl") || n.contains("rm") -> "kg" to 500f
            else -> "cm" to 300f // Esto marcará que bíceps, cintura, etc., usan la conversión de cm/in
        }
    }

    /**
     * Representación de los estados posibles de la interfaz de usuario.
     */
    sealed class DetalleMedidaUiState {
        object Loading : DetalleMedidaUiState()
        object Empty : DetalleMedidaUiState()
        data class Success(val historial: List<RegistroMedida>, val entries: List<Entry>) : DetalleMedidaUiState()
        data class Error(val message: String) : DetalleMedidaUiState()
    }

    /**
     * Estructura de datos para la configuración de UI de la medida actual.
     */
    data class ConfigMedidaUI(
        val unidadVisual: String = "",
        val limiteVisual: Float = 0f,
        val tipoBase: String = ""
    )
}