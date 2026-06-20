package com.example.trainex.perfil

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trainex.R
import com.example.trainex.firebase.FirebaseFotosRepository
import com.example.trainex.firebase.FirebaseHistorialCalendarioRepository
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.perfil.foto.FotoProgreso
import com.example.trainex.utils.UnitManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioRepo = FirebaseUsuarioRepository()
    private val fotosRepo = FirebaseFotosRepository(application)
    private val historialCalendarioRepo = FirebaseHistorialCalendarioRepository()

    // --- ESTADOS DE LA UI ---
    private val _perfilData = MutableLiveData<Usuario?>()
    val perfilData: LiveData<Usuario?> get() = _perfilData

    private val _counts = MutableLiveData<Pair<Int, Int>>() // Seguidores, Seguidos
    val counts: LiveData<Pair<Int, Int>> get() = _counts

    private val _fotos = MutableLiveData<List<FotoProgreso>>()
    val fotos: LiveData<List<FotoProgreso>> get() = _fotos

    private val _chartData = MutableLiveData<Pair<List<Pair<String, LineDataSet>>, List<List<String>>>>()
    val chartData: LiveData<Pair<List<Pair<String, LineDataSet>>, List<List<String>>>> get() = _chartData

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> get() = _statusMessage

    // Método auxiliar para obtener strings según el idioma
    private fun getString(resId: Int): String {
        return getApplication<Application>().getString(resId)
    }

    private fun getString(resId: Int, vararg formatArgs: Any): String {
        return getApplication<Application>().getString(resId, *formatArgs)
    }

    /**
     * Carga todos los datos del perfil de forma coordinada.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            try {
                // 1. Cargar Usuario y el Historial de Peso simultáneamente
                val usuarioRemoto = usuarioRepo.obtenerUsuario()
                val historialPeso = usuarioRepo.obtenerHistorialPeso()

                usuarioRemoto?.let { user ->
                    // --- LÓGICA DE ACTUALIZACIÓN DE PESO ---
                    val ultimoRegistro = historialPeso.maxByOrNull { it.fecha }

                    ultimoRegistro?.let {
                        user.peso = it.peso
                    }
                    // ---------------------------------------

                    _perfilData.value = user

                    val dataMap = mapOf(
                        "username" to user.username,
                        "nombre" to user.nombre,
                        "edad" to user.edad,
                        "peso" to user.peso,
                        "altura" to user.altura,
                        "foto" to user.foto
                    )
                    UserManager.saveUserData(getApplication(), dataMap)

                    // 2. Cargar Estadísticas de Comunidad
                    val seguidores = usuarioRepo.contarSeguidores(user.id)
                    val seguidos = usuarioRepo.contarSeguidos(user.id)
                    _counts.value = seguidores to seguidos
                }

                // 3. Cargar Galería de Fotos
                _fotos.value = fotosRepo.obtenerFotosProgreso()

                // 4. Procesar Gráficas
                procesarGraficas()

            } catch (e: Exception) {
                _statusMessage.postValue(getString(
                    R.string.error_cargar_datos_formato,
                    e.message ?: getString(R.string.error_desconocido)
                ))
            }
        }
    }

    /**
     * Procesa la lógica de volumen de entrenamiento y peso corporal para las gráficas.
     */
    private suspend fun procesarGraficas() = withContext(Dispatchers.Default) {
        val context = getApplication<Application>()
        val unidadPeso = UnitManager.obtener(context, "unidad_peso", "kg")

        // --- GRÁFICA DE PESO ---
        val historial = usuarioRepo.obtenerHistorialPeso()
        val entriesPeso = historial.mapIndexed { i, r ->
            Entry(i.toFloat(), UnitManager.aVisual(r.peso, unidadPeso).toFloat())
        }
        val labelsPeso = historial.map {
            SimpleDateFormat(getString(R.string.formato_fecha_grafico), Locale.getDefault()).format(Date(it.fecha))
        }
        val dsPeso = LineDataSet(entriesPeso, getString(R.string.grafico_etiqueta_peso_con_unidad, unidadPeso))

        // --- GRÁFICA DE VOLUMEN SEMANAL ---
        val sesiones = historialCalendarioRepo.obtenerTodasLasSesiones()
        val volumenPorSemana = mutableMapOf<Int, Double>()
        val calendar = Calendar.getInstance()

        sesiones.forEach { sesion ->
            calendar.timeInMillis = sesion.fecha
            val semana = calendar.get(Calendar.WEEK_OF_YEAR)
            var volSesion = 0.0
            sesion.ejercicios.forEach { ej ->
                ej.series?.forEach { s ->
                    volSesion += (UnitManager.aVisual(s.kilogramos, unidadPeso) * s.repeticiones)
                }
            }
            volumenPorSemana[semana] = volumenPorSemana.getOrDefault(semana, 0.0) + volSesion
        }

        val semanasOrdenadas = volumenPorSemana.keys.sorted()
        val entriesVol = semanasOrdenadas.mapIndexed { i, sem ->
            Entry(i.toFloat(), volumenPorSemana[sem]!!.toFloat())
        }
        val labelsVol = semanasOrdenadas.map {
            getString(R.string.etiqueta_semana_con_numero, it)
        }
        val dsVol = LineDataSet(entriesVol, getString(R.string.grafico_etiqueta_volumen_con_unidad, unidadPeso))

        // Publicar resultados unificados para el ChartsAdapter
        _chartData.postValue(
            listOf(
                getString(R.string.grafico_titulo_volumen) to dsVol,
                getString(R.string.grafico_titulo_peso) to dsPeso
            ) to listOf(labelsVol, labelsPeso)
        )
    }

    /**
     * Gestiona la subida de una nueva foto de progreso.
     */
    fun procesarSubidaFoto(uri: Uri) {
        viewModelScope.launch {
            _statusMessage.value = getString(R.string.toast_subiendo_imagen)
            val exito = fotosRepo.subirFotoProgreso(uri)
            if (exito) {
                _statusMessage.value = getString(R.string.toast_foto_guardada)
                cargarDatos()
            } else {
                _statusMessage.value = getString(R.string.toast_error_subida_foto)
            }
        }
    }

    /**
     * Elimina una foto de progreso.
     */
    fun eliminarFoto(fotoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _statusMessage.postValue(getString(R.string.toast_eliminando))
            val exito = fotosRepo.eliminarFoto(fotoId)
            if (exito) {
                _statusMessage.postValue(getString(R.string.toast_foto_eliminada))
                cargarDatos()
            } else {
                _statusMessage.postValue(getString(R.string.error_al_eliminar))
            }
        }
    }
}