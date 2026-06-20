package com.example.trainex.perfil.buscador

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.R
import com.example.trainex.firebase.FirebaseFotosRepository
import com.example.trainex.firebase.FirebaseHistorialCalendarioRepository
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.perfil.Usuario
import com.example.trainex.perfil.foto.FotoProgreso
import com.example.trainex.utils.UnitManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/** ViewModel encargado de recopilar toda la información pública de un usuario específico */
class PerfilPublicoViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioRepo = FirebaseUsuarioRepository()
    private val fotosRepo = FirebaseFotosRepository(application)
    private val historialCalendarioRepo = FirebaseHistorialCalendarioRepository()

    /** Flujo de estado reactivo para la UI del perfil */
    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    /** Carga todos los datos necesarios (usuario, fotos, seguidores, gráficas) de forma asíncrona */
    fun cargarDatos(userId: String) {
        viewModelScope.launch {
            try {
                val usuario = usuarioRepo.obtenerUsuarioPorId(userId)
                val isFollowing = usuarioRepo.estoySiguiendoA(userId)
                val fotos = fotosRepo.obtenerFotosPorId(userId)
                val seguidores = usuarioRepo.contarSeguidores(userId)
                val seguidos = usuarioRepo.contarSeguidos(userId)

                val datosPeso = obtenerDatosGraficaPeso(userId, usuario?.peso ?: 0.0)
                val datosVolumen = obtenerDatosGraficaVolumen(userId)

                _uiState.value = PerfilUiState(
                    usuario = usuario,
                    isFollowing = isFollowing,
                    fotos = fotos,
                    seguidoresCount = seguidores,
                    seguidosCount = seguidos,
                    datosPeso = datosPeso,
                    datosVolumen = datosVolumen
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Ejecuta la acción de seguir o dejar de seguir y actualiza el estado local */
    fun toggleSeguimiento(userId: String) {
        val currentState = _uiState.value
        val userObjetivo = currentState.usuario ?: return

        viewModelScope.launch {
            try {
                if (currentState.isFollowing) {
                    usuarioRepo.dejarDeSeguirUsuario(userId)
                } else {
                    usuarioRepo.seguirUsuario(userId, userObjetivo)
                }

                val newIsFollowing = !currentState.isFollowing
                val seguidores = usuarioRepo.contarSeguidores(userId)
                _uiState.value = currentState.copy(
                    isFollowing = newIsFollowing,
                    seguidoresCount = seguidores
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Obtiene el historial de peso y lo convierte a un formato compatible con gráficas de líneas */
    private suspend fun obtenerDatosGraficaPeso(userId: String, pesoActual: Double): Pair<LineDataSet, List<String>>? {
        val context = getApplication<Application>()
        val unidadPeso = UnitManager.obtener(context, "unidad_peso", "kg")
        val etiquetaPeso = context.getString(R.string.grafico_etiqueta_peso_con_unidad, unidadPeso)

        val historial = usuarioRepo.obtenerHistorialPesoPorId(userId)
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())

        return if (historial.isNotEmpty()) {
            val entries = historial.mapIndexed { i, r ->
                Entry(i.toFloat(), UnitManager.aVisual(r.peso, unidadPeso).toFloat())
            }
            val labels = historial.map { sdf.format(Date(it.fecha)) }
            Pair(LineDataSet(entries, etiquetaPeso), labels)
        } else if (pesoActual > 0) {
            val pesoConvertido = UnitManager.aVisual(pesoActual, unidadPeso).toFloat()
            Pair(LineDataSet(listOf(Entry(0f, pesoConvertido)), etiquetaPeso), listOf(sdf.format(Date())))
        } else null
    }

    /** Calcula el volumen semanal de entrenamiento a partir de las sesiones del usuario */
    private suspend fun obtenerDatosGraficaVolumen(userId: String): Pair<LineDataSet, List<String>>? {
        val sesiones = historialCalendarioRepo.obtenerSesionesPorId(userId)
        if (sesiones.isEmpty()) return null

        val context = getApplication<Application>()
        val unidadPeso = UnitManager.obtener(context, "unidad_peso", "kg")
        val etiquetaVolumen = context.getString(R.string.grafico_etiqueta_volumen_con_unidad, unidadPeso)

        val volumenPorSemana = mutableMapOf<Int, Double>()
        val cal = Calendar.getInstance()

        sesiones.forEach { sesion ->
            cal.timeInMillis = sesion.fecha
            val sem = cal.get(Calendar.WEEK_OF_YEAR)
            val vol = sesion.ejercicios.sumOf { ej ->
                ej.series?.sumOf { s -> UnitManager.aVisual(s.kilogramos, unidadPeso) * s.repeticiones } ?: 0.0
            }
            volumenPorSemana[sem] = volumenPorSemana.getOrDefault(sem, 0.0) + vol
        }

        val sortedWeeks = volumenPorSemana.keys.sorted()
        val entries = sortedWeeks.mapIndexed { i, sem ->
            Entry(i.toFloat(), volumenPorSemana[sem]!!.toFloat())
        }
        val labels = sortedWeeks.map { context.getString(R.string.etiqueta_semana_con_numero, it) }

        return Pair(LineDataSet(entries, etiquetaVolumen), labels)
    }

    /** Representación del estado completo de la pantalla de perfil */
    data class PerfilUiState(
        val usuario: Usuario? = null,
        val isFollowing: Boolean = false,
        val fotos: List<FotoProgreso> = emptyList(),
        val seguidoresCount: Int = 0,
        val seguidosCount: Int = 0,
        val datosPeso: Pair<LineDataSet, List<String>>? = null,
        val datosVolumen: Pair<LineDataSet, List<String>>? = null
    )
}