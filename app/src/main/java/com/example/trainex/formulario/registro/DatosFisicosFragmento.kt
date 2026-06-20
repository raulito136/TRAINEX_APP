package com.example.trainex.formulario.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.trainex.databinding.FragmentoDatosFisicosBinding

/**
 * Fragmento encargado de capturar el peso y la altura del usuario.
 */
class DatosFisicosFragmento : Fragment() {

    private var _binding: FragmentoDatosFisicosBinding? = null
    private val binding get() = _binding!!

    // Acceso al mismo ViewModel de la Activity para mantener la integridad de los datos
    private val viewModel: RegistroViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflación del layout con ViewBinding
        _binding = FragmentoDatosFisicosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Listener para procesar los datos físicos al hacer clic en el botón
        binding.btnGuardarFisico.setOnClickListener {
            validateAndSaveData()
        }
    }

    /**
     * Valida que las entradas de texto sean números válidos antes de actualizar el ViewModel.
     */
    private fun validateAndSaveData() {
        // Intenta convertir el texto a Double, devuelve null si no es un número válido
        val peso = binding.etPeso.text.toString().toDoubleOrNull()
        val altura = binding.etAltura.text.toString().toDoubleOrNull()

        if (peso != null && altura != null) {
            // Si ambos son correctos, guarda en el ViewModel y avanza al siguiente paso
            viewModel.updateData(mapOf("peso" to peso, "altura" to altura))
            viewModel.nextStep()
        } else {
            // Muestra errores visuales si alguno de los campos es nulo o inválido
            if (peso == null) binding.etPeso.error = "Ingresa un peso válido"
            if (altura == null) binding.etAltura.error = "Ingresa una altura válida"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberación del binding
        _binding = null
    }
}