package com.example.trainex.formulario.registro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.trainex.R
import com.example.trainex.databinding.FragmentoUsuarioBinding
import kotlinx.coroutines.launch

/**
 * Primer fragmento del flujo: Captura y verificación de disponibilidad del nombre de usuario.
 */
class UsuarioFragmento : Fragment(R.layout.fragmento_usuario) {

    private var _binding: FragmentoUsuarioBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartido para coordinar los datos globales del registro
    private val viewModel: RegistroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inicialización del binding para el fragmento
        _binding = FragmentoUsuarioBinding.bind(view)

        // Configuración del botón para iniciar la verificación
        binding.btnSave.setOnClickListener { verificarYGuardar() }
    }

    /**
     * Valida el formato del nombre de usuario y consulta su disponibilidad de forma asíncrona.
     */
    private fun verificarYGuardar() {
        // Normalización del nombre de usuario a minúsculas
        val username = binding.etUsername.text.toString().trim().lowercase()

        // Validación simple de campo vacío
        if (username.isEmpty()) {
            binding.etUsername.error = getString(R.string.error_username_empty)
            return
        }

        // Bloqueo del botón y cambio de texto para indicar que se está procesando
        binding.btnSave.isEnabled = false
        binding.btnSave.setText(R.string.status_checking)

        // Lanzamiento de una corrutina en el scope del ciclo de vida del fragmento
        lifecycleScope.launch {
            // Consulta al ViewModel si el username ya existe en la base de datos
            val existe = viewModel.verificarDisponibilidadUsername(username)

            if (existe) {
                // Si el usuario existe, se muestra error y se reactiva el botón
                binding.etUsername.error = getString(R.string.error_username_taken)
                binding.btnSave.isEnabled = true
                binding.btnSave.setText(R.string.guardar)
            } else {
                // Si está disponible, se guardan los datos en el mapa global y se avanza al siguiente paso
                viewModel.updateData(mapOf("username" to username))
                viewModel.nextStep()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevención de memory leaks
        _binding = null
    }
}