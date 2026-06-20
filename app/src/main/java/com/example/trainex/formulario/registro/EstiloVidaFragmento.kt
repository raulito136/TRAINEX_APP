package com.example.trainex.formulario.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.trainex.R
import com.example.trainex.databinding.FragmentoEstiloDeVidaBinding

/**
 * Fragmento encargado de capturar el estilo de vida y el objetivo del usuario.
 */
class EstiloVidaFragmento : Fragment() {

    private var _binding: FragmentoEstiloDeVidaBinding? = null
    private val binding get() = _binding!!

    // Acceso al ViewModel compartido de la Activity para persistir datos temporalmente
    private val viewModel: RegistroViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el layout para este fragmento
        _binding = FragmentoEstiloDeVidaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura los adaptadores para los menús desplegables (ComboBox/Spinner)
        setupComboboxes()

        // Listener del botón para validar y avanzar
        binding.btnGuardarObjetivo.setOnClickListener {
            validarYGuardar()
        }
    }

    /**
     * Carga las opciones desde los recursos (arrays.xml) hacia los AutoCompleteTextView.
     */
    private fun setupComboboxes() {
        // Configuración del dropdown "Estilo de Vida"
        val opcionesVida = resources.getStringArray(R.array.array_estilo_vida)
        val adapterVida = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opcionesVida)
        binding.etEstiloVida.setAdapter(adapterVida)

        // Configuración del dropdown "Objetivo"
        val opcionesObj = resources.getStringArray(R.array.array_objetivos)
        val adapterObj = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opcionesObj)
        binding.etObjetivo.setAdapter(adapterObj)
    }

    /**
     * Verifica que el usuario haya seleccionado opciones válidas antes de continuar.
     */
    private fun validarYGuardar() {
        val estiloVida = binding.etEstiloVida.text.toString().trim()
        val objetivo = binding.etObjetivo.text.toString().trim()

        // Se asume que el índice 0 de los arrays es un texto de instrucción (ej: "Seleccione...")
        val opcionVidaInvalida = resources.getStringArray(R.array.array_estilo_vida)[0]
        val opcionObjetivoInvalida = resources.getStringArray(R.array.array_objetivos)[0]

        var isValid = true

        // Validación de Estilo de Vida
        if (estiloVida.isEmpty() || estiloVida == opcionVidaInvalida) {
            binding.tilEstiloVida.error = getString(R.string.error_seleccionar_estilo)
            isValid = false
        } else {
            binding.tilEstiloVida.error = null
        }

        // Validación de Objetivo
        if (objetivo.isEmpty() || objetivo == opcionObjetivoInvalida) {
            binding.tilObjetivo.error = getString(R.string.error_seleccionar_objetivo)
            isValid = false
        } else {
            binding.tilObjetivo.error = null
        }

        if (isValid) {
            // Actualiza el mapa de datos en el ViewModel
            viewModel.updateData(mapOf(
                "estilo_vida" to estiloVida,
                "objetivo" to objetivo
            ))

            // Llama a nextStep(), lo que provocará que la Activity ejecute finalizarRegistro()
            viewModel.nextStep()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpieza de la referencia del binding para evitar fugas de memoria
        _binding = null
    }
}