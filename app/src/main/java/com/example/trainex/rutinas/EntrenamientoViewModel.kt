package com.example.trainex.rutinas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trainex.IA.DeepSeekGenerator
import com.example.trainex.IA.IAGenerator
import com.example.trainex.database.AppDatabase
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.perfil.Usuario
import com.example.trainex.repository.FirebaseRutinaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel que gestiona la lógica de rutinas y la interacción con el generador de IA.
 * Proporciona flujos de datos en tiempo real desde Firestore y gestiona la validación de usuarios.
 */
class EntrenamientoViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepo = FirebaseRutinaRepository()
    private val usuarioRepository = FirebaseUsuarioRepository()
    private val generator: IAGenerator = DeepSeekGenerator(application)
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _rutinas = MutableLiveData<List<Rutina>>()
    /** Flujo de datos con la lista de rutinas del usuario actual. */
    val rutinas: LiveData<List<Rutina>> get() = _rutinas

    private val _usuarioValido = MutableLiveData<Usuario?>()
    /** Estado de validez del usuario para controlar el acceso a la sesión. */
    val usuarioValido: LiveData<Usuario?> get() = _usuarioValido

    private val _iaStatus = MutableLiveData<String?>()
    /** Notificaciones de estado sobre el proceso de generación con IA. */
    val iaStatus: LiveData<String?> get() = _iaStatus

    init {
        escucharRutinas()
    }

    /**
     * Verifica si el usuario actual tiene un perfil completo en la base de datos.
     */
    fun validarUsuario() {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuario()
            _usuarioValido.value = usuario
        }
    }

    /**
     * Establece una escucha activa sobre las rutinas almacenadas en Firebase para el usuario.
     */
    private fun escucharRutinas() {
        if (currentUserId.isEmpty()) return
        viewModelScope.launch {
            firebaseRepo.obtenerRutinasFlow(currentUserId).collect { lista ->
                _rutinas.postValue(lista)
            }
        }
    }

    /**
     * Elimina una rutina específica de Firebase.
     * @param firebaseId El identificador único de la rutina a borrar.
     */
    fun eliminarRutina(firebaseId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepo.eliminarRutina(firebaseId)
        }
    }

    /**
     * Invoca al generador de IA para crear un plan de entrenamiento basado en los parámetros
     * del usuario y guarda el resultado automáticamente en Firebase.
     * * @param dias Cantidad de días de entrenamiento por semana.
     * @param objetivo Meta fitness (ej: "Ganar músculo", "Perder grasa").
     * @param nivel Experiencia del usuario (ej: "Principiante", "Avanzado").
     * @param infoUsuario Detalles adicionales (edad, lesiones, etc.).
     */
    fun generarRutinaIA(dias: Int, objetivo: String, nivel: String, infoUsuario: String) {
        viewModelScope.launch {
            try {
                // Obtener ejercicios de la base de datos local para alimentar la IA
                val db = AppDatabase.getDatabase(getApplication())
                val ejerciciosDisponibles = db.ejercicioDao().obtenerTodos().first().ifEmpty {
                    listOf(Ejercicio(1, "Press de Banca (Barra)", "Pecho", "", "Principiante", "", ""))
                }

                // Generación asíncrona mediante el motor de IA
                val resultado = withContext(Dispatchers.IO) {
                    generator.generarRutinas(ejerciciosDisponibles, dias, dias, objetivo, nivel, infoUsuario)
                }

                resultado.onSuccess { lista ->
                    lista.forEach { nueva ->
                        val id = FirebaseFirestore.getInstance().collection("rutinas").document().id
                        firebaseRepo.subirRutinaConId(nueva.copy(firebaseId = id, userId = currentUserId))
                    }
                    _iaStatus.postValue("¡Rutina generada con éxito!")
                }.onFailure {
                    _iaStatus.postValue("Error en la IA: ${it.message}")
                }
            } catch (e: Exception) {
                _iaStatus.postValue("Error: ${e.message}")
            }
        }
    }
}