package com.example.trainex.formulario.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.trainex.R
import com.example.trainex.databinding.FragmentoInformacionPersonalBinding

/**
 * Segundo fragmento del flujo: Captura nombre, apellidos, edad y género.
 */
class InformacionPersonalFragmento : Fragment() {

    private var _binding: FragmentoInformacionPersonalBinding? = null
    private val binding get() = _binding!!

    // Referencia al ViewModel compartido con el ciclo de vida de la Activity
    private val viewModel: RegistroViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflado del layout usando ViewBinding
        _binding = FragmentoInformacionPersonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración inicial del selector de género
        configurarDropdownGenero()

        // Asignación del listener al botón para procesar la información
        binding.btnGuardarInfo.setOnClickListener {
            validarYGuardar()
        }
    }

    /**
     * Llena el AutoCompleteTextView (Dropdown) con las opciones de sexo definidas en recursos.
     */
    private fun configurarDropdownGenero() {
        val opciones = resources.getStringArray(R.array.array_sexo)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opciones)
        binding.etGenero.setAdapter(adapter)
    }

    /**
     * Realiza la validación de los campos de texto y, si son correctos, actualiza el ViewModel.
     */
    private fun validarYGuardar() {
        // Obtención de valores y eliminación de espacios en blanco
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val edadStr = binding.etEdad.text.toString().trim()
        val genero = binding.etGenero.text.toString().trim()

        val opciones = resources.getStringArray(R.array.array_sexo)
        val opcionInvalida = opciones[0] // Usualmente la opción "Seleccione..."

        var isValid = true

        // Validación del nombre
        if (nombre.isEmpty()) {
            binding.etNombre.error = getString(R.string.error_nombre_req)
            isValid = false
        }

        // Validación de apellidos
        if (apellidos.isEmpty()) {
            binding.etApellidos.error = getString(R.string.error_apellidos_req)
            isValid = false
        }

        // Validación de la edad (debe ser un número entre 1 y 120)
        val edad = edadStr.toIntOrNull()
        if (edadStr.isEmpty() || edad == null || edad <= 0 || edad > 120) {
            binding.etEdad.error = getString(R.string.error_edad_valida)
            isValid = false
        }

        // Validación de la selección de género
        if (genero.isEmpty() || genero == opcionInvalida) {
            binding.tilGenero.error = getString(R.string.error_seleccionar_genero)
            isValid = false
        } else {
            binding.tilGenero.error = null
        }

        // Si todas las validaciones pasan, se guardan los datos y se avanza
        if (isValid) {
            val data = mapOf(
                "nombre" to nombre,
                "apellidos" to apellidos,
                "edad" to (edad ?: 0),
                "genero" to genero
            )
            viewModel.updateData(data)

            // Ordena al ViewModel pasar a la siguiente pantalla
            viewModel.nextStep()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberación de memoria del binding
        _binding = null
    }
}