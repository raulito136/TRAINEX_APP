package com.example.trainex.dietas

import android.app.Application
import androidx.lifecycle.*
import com.example.trainex.database.AppDatabase
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.repository.FirebaseAlimentosRepository
import com.example.trainex.utils.NutricionCalculadora
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel que gestiona la lógica de negocio para el diario nutricional.
 * Mantiene el estado de los objetivos, consumos totales y listas de alimentos del día.
 */
class DietasViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val usuarioRepo = FirebaseUsuarioRepository()
    private val firebaseAlimentosRepo = FirebaseAlimentosRepository(application)

    // Estados expuestos a la UI mediante LiveData.
    val objetivos = MutableLiveData<NutricionObjetivos>()
    val comidas = MutableLiveData<Map<String, List<AlimentoDiario>>>()
    val totales = MutableLiveData<NutricionTotales>()

    /** DTO para representar los objetivos nutricionales diarios. */
    data class NutricionObjetivos(val cal: Int, val prot: Int, val carb: Int, val gras: Int)
    /** DTO para representar los totales consumidos acumulados. */
    data class NutricionTotales(val cal: Int, val prot: Int, val carb: Int, val gras: Int)

    /**
     * Carga los objetivos del usuario desde Firebase y actualiza las listas de consumo local.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            // 1. Obtener perfil de usuario y calcular sus objetivos personalizados.
            val usuario = usuarioRepo.obtenerUsuario()
            usuario?.let {
                val obj = NutricionCalculadora.calcularObjetivos(it)
                objetivos.value = NutricionObjetivos(obj.calorias, obj.proteinas, obj.carbohidratos, obj.grasas)
            }
            // 2. Refrescar las listas de Room.
            actualizarListas()
        }
    }

    /**
     * Recupera los registros de comida de Room para la fecha actual y calcula los totales consumidos.
     */
    fun actualizarListas() {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch(Dispatchers.IO) {
            val d = db.diarioDao().obtenerComidasPorTipo(fecha, "Desayuno")
            val c = db.diarioDao().obtenerComidasPorTipo(fecha, "Comida")
            val ce = db.diarioDao().obtenerComidasPorTipo(fecha, "Cena")
            val s = db.diarioDao().obtenerComidasPorTipo(fecha, "Snack")

            val mapa = mapOf("Desayuno" to d, "Comida" to c, "Cena" to ce, "Snack" to s)
            comidas.postValue(mapa)

            // Filtrar solo las comidas que el usuario ha marcado como "completadas" para sumar al total.
            val todas = d + c + ce + s
            val consumidas = todas.filter { it.completado }
            totales.postValue(NutricionTotales(
                consumidas.sumOf { it.kcalTotales },
                consumidas.sumOf { it.protTotales }.toInt(),
                consumidas.sumOf { it.carbTotales }.toInt(),
                consumidas.sumOf { it.grasTotales }.toInt()
            ))
        }
    }

    /**
     * Cambia el estado (marcado/no marcado) de un alimento en el diario.
     */
    fun cambiarEstadoCompletado(item: AlimentoDiario, check: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            db.diarioDao().actualizarRegistro(item.copy(completado = check))
            actualizarListas()
        }
    }

    /**
     * Elimina un registro de alimento de la base de datos local.
     */
    fun borrarAlimento(item: AlimentoDiario) {
        viewModelScope.launch(Dispatchers.IO) {
            db.diarioDao().eliminarRegistro(item.id)
            actualizarListas()
        }
    }

    /**
     * Inserta un nuevo registro en el diario local y sincroniza el alimento en el historial de Firebase.
     * @param registro El nuevo ítem para el diario local.
     * @param alimentoOriginal El objeto Alimento base para el historial remoto.
     */
    fun insertarAlimentoDiario(registro: AlimentoDiario, alimentoOriginal: Alimento) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Guardar en Room (Local).
            db.diarioDao().insertarRegistro(registro)

            // 2. Guardar en Historial (Firebase) para persistencia entre dispositivos.
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (uid.isNotEmpty()) {
                firebaseAlimentosRepo.guardarEnHistorial(uid, alimentoOriginal)
            }

            // 3. Refrescar la UI.
            actualizarListas()
        }
    }
}