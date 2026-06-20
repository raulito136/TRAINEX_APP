package com.example.trainex.formulario.registro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.firebase.FirebaseMedidasRepository
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.perfil.RegistroPeso
import com.example.trainex.perfil.Usuario
import com.example.trainex.perfil.medidas.RegistroMedida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroViewModel : ViewModel() {

    private val usuarioRepo = FirebaseUsuarioRepository()
    private val medidasRepo = FirebaseMedidasRepository()

    // Mapa temporal para recolectar datos (username, peso, etc.) de los 4 fragmentos
    private val _userData = mutableMapOf<String, Any>()

    // Controla en qué pantalla estamos (0 a 3)
    private val _currentStep = MutableLiveData<Int>(0)
    val currentStep: LiveData<Int> get() = _currentStep

    // Indica a la Activity si el proceso terminó con éxito
    private val _registroExitoso = MutableLiveData<Boolean>()
    val registroExitoso: LiveData<Boolean> get() = _registroExitoso

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    /**
     * Agrega nuevos campos al mapa de datos sin borrar los anteriores.
     */
    fun updateData(newData: Map<String, Any>) {
        _userData.putAll(newData)
    }

    /**
     * Incrementa el índice del paso para mostrar el siguiente fragmento.
     */
    fun nextStep() {
        val next = (_currentStep.value ?: 0) + 1
        _currentStep.value = next
    }

    /**
     * Decrementa el índice del paso (útil si añades un botón de "Atrás").
     */
    fun previousStep() {
        val current = _currentStep.value ?: 0
        if (current > 0) {
            _currentStep.value = current - 1
        }
    }

    /**
     * Punto final del registro: consolida los datos y los envía a Firebase en un hilo secundario.
     */
    fun finalizarRegistro(nombrePesoCorporal: String) {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Mapeo de datos recolectados al objeto Usuario
                val nuevoUsuario = Usuario(
                    id = "",
                    username = _userData["username"] as? String ?: "",
                    nombre = _userData["nombre"] as? String ?: "",
                    apellidos = _userData["apellidos"] as? String ?: "",
                    genero = _userData["genero"] as? String ?: "",
                    edad = _userData["edad"] as? Int ?: 0,
                    peso = _userData["peso"] as? Double ?: 0.0,
                    altura = _userData["altura"] as? Double ?: 0.0,
                    estiloVida = _userData["estilo_vida"] as? String ?: "",
                    objetivo = _userData["objetivo"] as? String ?: ""
                )

                // 2. Guardar el perfil en la colección "usuarios"
                usuarioRepo.guardarUsuario(nuevoUsuario)

                // 3. Crear el primer registro histórico de peso para las gráficas
                val registroInicialPeso = RegistroPeso(
                    peso = nuevoUsuario.peso,
                    fecha = System.currentTimeMillis()
                )
                usuarioRepo.guardarPesoEnHistorial(registroInicialPeso)

                // 4. Registrar la medida inicial de peso para la sección de medidas
                val nuevaMedida = RegistroMedida(
                    id = "",
                    nombre = nombrePesoCorporal,
                    valor = nuevoUsuario.peso.toFloat(),
                    fecha = System.currentTimeMillis()
                )
                medidasRepo.guardarMedida(nuevaMedida)

                // Notificar éxito a la Activity en el hilo principal
                withContext(Dispatchers.Main) {
                    _registroExitoso.value = true
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Verifica si el nombre de usuario ya está en uso antes de permitir avanzar.
     */
    suspend fun verificarDisponibilidadUsername(username: String): Boolean {
        return try {
            usuarioRepo.existeNombreUsuario(username)
        } catch (e: Exception) {
            true
        }
    }
}