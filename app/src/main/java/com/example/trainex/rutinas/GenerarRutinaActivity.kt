package com.example.trainex.rutinas

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainex.databinding.ActivityFormularioIaBinding
import com.example.trainex.perfil.UserManager
import com.example.trainex.utils.LanguageUtils

/**
 * Actividad que gestiona la interfaz del formulario para la generación de rutinas por IA.
 * Implementa un patrón MVVM donde la actividad actúa como una vista pasiva que observa
 * los eventos de validación y navegación del [GenerarRutinaViewModel].
 */
class GenerarRutinaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioIaBinding

    // Vinculamos el ViewModel utilizando el delegado viewModels()
    private val viewModel: GenerarRutinaViewModel by viewModels()

    /**
     * Inicializa la actividad, configura el diseño de borde a borde y establece los observadores.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityFormularioIaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()

        /**
         * Al hacer clic en el botón de envío, se recolectan los datos de los inputs
         * y se delega la validación al ViewModel.
         */
        binding.btnAgregarEjercicio.setOnClickListener {
            val edad = binding.editTextEdad.text.toString().trim()
            val dias = binding.spinnerDias.selectedItem.toString()
            val objetivo = binding.spinnerObjetivo.selectedItem.toString()
            val experiencia = binding.spinnerExperiencia.selectedItem.toString()

            viewModel.validarYEnviar(edad, dias, objetivo, experiencia)
        }
    }

    /**
     * Configura la interfaz de usuario inicial, precargando datos desde el perfil del usuario.
     */
    private fun setupUI() {
        // Recuperar datos iniciales del perfil del usuario (edad y objetivo predeterminado)
        val perfil = UserManager.getUserProfile(this)
        binding.editTextEdad.setText(perfil.edad.toString())
        setSpinnerDefaultValue(binding.spinnerObjetivo, perfil.objetivo)

        // Ajuste de padding dinámico para evitar solapamientos con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Establece la observación de los LiveData del ViewModel para reaccionar a errores o navegación.
     */
    private fun setupObservers() {
        // Observar y mostrar errores de validación en el campo de edad
        viewModel.errorEdad.observe(this) { errorMsg ->
            binding.textInputEdad.error = errorMsg
            if (errorMsg != null) {
                binding.textInputEdad.requestFocus()
            }
        }

        /**
         * Observar el evento de navegación. Si los datos son válidos, se redirige a la
         * actividad de Entrenamiento para procesar la generación de la rutina.
         */
        viewModel.navegacionEvent.observe(this) { bundle ->
            bundle?.let {
                val intent = Intent(this, Entrenamiento::class.java).apply {
                    putExtra("generar_rutina", true) // Señal para iniciar el flujo de IA
                    putExtras(it) // Adjunta edad, días, objetivo y experiencia
                }
                startActivity(intent)
                finish() // Limpiar la actividad actual del stack
            }
        }
    }

    /**
     * Utilidad para establecer el valor seleccionado de un Spinner basado en un texto de referencia.
     * @param spinner El componente visual de selección.
     * @param value El valor de texto que se desea preseleccionar.
     */
    private fun setSpinnerDefaultValue(spinner: android.widget.Spinner, value: String?) {
        if (value == null) return
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString().equals(value, ignoreCase = true)) {
                spinner.setSelection(i)
                break
            }
        }
    }
}