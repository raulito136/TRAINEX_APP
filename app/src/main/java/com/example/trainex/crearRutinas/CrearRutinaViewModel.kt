package com.example.trainex.crearRutinas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.repository.FirebaseRutinaRepository
import com.example.trainex.rutinas.Rutina
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel que gestiona el estado y la persistencia de la rutina en edición o creación.
 */
class CrearRutinaViewModel : ViewModel() {

    private val firebaseRepo = FirebaseRutinaRepository()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** Flujo de datos para la lista de ejercicios de la rutina. */
    private val _listaEjercicios = MutableLiveData<ArrayList<Ejercicio>>(arrayListOf())
    val listaEjercicios: LiveData<ArrayList<Ejercicio>> get() = _listaEjercicios

    /** Estado para notificar si el guardado fue exitoso. */
    private val _operacionExitosa = MutableLiveData<Boolean>()
    val operacionExitosa: LiveData<Boolean> get() = _operacionExitosa

    /** Almacena mensajes de error ocurridos durante la persistencia. */
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    /**
     * Establece una nueva lista de ejercicios en el estado del ViewModel.
     */
    fun setEjercicios(nuevos: ArrayList<Ejercicio>) {
        _listaEjercicios.value = nuevos
    }

    /**
     * Procesa el guardado de la rutina en Firebase Firestore.
     * Realiza una actualización si [modoEdicion] es verdadero, de lo contrario crea un nuevo documento.
     */
    fun guardarRutina(titulo: String, modoEdicion: Boolean, rutinaAEditar: Rutina?) {
        val ejerciciosActuales = _listaEjercicios.value ?: arrayListOf()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (modoEdicion && rutinaAEditar != null) {
                    // Edición de rutina existente
                    val rutinaActualizada = rutinaAEditar.copy(
                        titulo = titulo,
                        ejercicios = ArrayList(ejerciciosActuales),
                        lastUpdate = System.currentTimeMillis()
                    )
                    firebaseRepo.subirRutinaConId(rutinaActualizada)
                } else {
                    // Creación de nueva rutina con ID generado
                    val nuevoFirebaseId = FirebaseFirestore.getInstance().collection("rutinas").document().id
                    val rutinaNueva = Rutina(
                        id = 0,
                        titulo = titulo,
                        ejercicios = ArrayList(ejerciciosActuales),
                        userId = currentUserId,
                        lastUpdate = System.currentTimeMillis(),
                        firebaseId = nuevoFirebaseId
                    )
                    firebaseRepo.subirRutinaConId(rutinaNueva)
                }

                withContext(Dispatchers.Main) {
                    _operacionExitosa.value = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message
                }
            }
        }
    }
}