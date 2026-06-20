package com.example.trainex.perfil.ajustes

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainex.R
import com.example.trainex.databinding.ActivityDatosPersonalesBinding
import com.example.trainex.utils.LanguageUtils
import com.example.trainex.utils.UnitManager

/**
 * Actividad refactorizada para utilizar DatosPersonalesViewModel siguiendo el patrón MVVM.
 */
class DatosPersonalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatosPersonalesBinding

    // Inyección del ViewModel
    private val viewModel: DatosPersonalesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityDatosPersonalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
        setupDropdowns()
        setupObservers() // Configura la observación de LiveData

        // Iniciamos la carga de datos desde el ViewModel
        viewModel.cargarDatos()
    }

    private fun setupClickListeners() {
        binding.tvBack.setOnClickListener { finish() }

        binding.btnGuardar.setOnClickListener {
            // Delegamos el guardado al ViewModel enviando los strings de la UI
            viewModel.guardarCambios(
                nombre = binding.etNombre.text.toString().trim(),
                apellidos = binding.etApellidos.text.toString().trim(),
                genero = binding.actvGenero.text.toString().trim(),
                edadStr = binding.etEdad.text.toString().trim(),
                pesoStr = binding.etPeso.text.toString().trim(),
                alturaStr = binding.etAltura.text.toString().trim(),
                estiloVida = binding.actvEstiloVida.text.toString().trim(),
                objetivo = binding.actvObjetivo.text.toString().trim()
            )
        }
    }

    /**
     * Configura los observadores para reaccionar a los cambios de estado en el ViewModel.
     */
    private fun setupObservers() {
        // 1. Observa los datos del usuario para rellenar la interfaz
        viewModel.usuario.observe(this) { u ->
            u?.let {
                binding.etNombre.setText(it.nombre)
                binding.etApellidos.setText(it.apellidos)
                binding.actvGenero.setText(it.genero, false)
                binding.etEdad.setText(it.edad.toString())

                // Gestión de unidades visuales
                val unidadPeso = UnitManager.obtener(this, "unidad_peso", "kg")
                val pesoVisual = UnitManager.aVisual(it.peso, unidadPeso)
                binding.etPeso.setText(pesoVisual.toString())
                binding.etPeso.hint = getString(R.string.hint_peso_con_unidad, unidadPeso)

                binding.etAltura.setText(it.altura.toString())
                binding.actvEstiloVida.setText(it.estiloVida, false)
                binding.actvObjetivo.setText(it.objetivo, false)
            }
        }

        // 2. Observa errores de validación para mostrarlos en los campos específicos
        viewModel.erroresValidacion.observe(this) { errores ->
            binding.etNombre.error = errores["nombre"]
            binding.etApellidos.error = errores["apellidos"]
            binding.etEdad.error = errores["edad"]
            binding.etPeso.error = errores["peso"]
            binding.etAltura.error = errores["altura"]

            // Gestión de errores en dropdowns
            errores["genero"]?.let { binding.actvGenero.error = it }
            errores["estiloVida"]?.let { binding.actvEstiloVida.error = it }
            errores["objetivo"]?.let { binding.actvObjetivo.error = it }
        }

        // 3. Observa si el guardado fue exitoso para cerrar la pantalla
        viewModel.guardadoExitoso.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, getString(R.string.mensaje_datos_actualizados_ajustes), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }

        // 4. Observa errores generales de Firebase o red
        viewModel.error.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, getString(R.string.mensaje_error_guardar_ajustes, it), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDropdowns() {
        val generos = resources.getStringArray(R.array.array_sexo)
        binding.actvGenero.setAdapter(createNoFilterAdapter(generos))
        binding.actvGenero.setOnClickListener { binding.actvGenero.showDropDown() }

        val estilosVida = resources.getStringArray(R.array.array_estilo_vida)
        binding.actvEstiloVida.setAdapter(createNoFilterAdapter(estilosVida))
        binding.actvEstiloVida.setOnClickListener { binding.actvEstiloVida.showDropDown() }

        val objetivos = resources.getStringArray(R.array.array_objetivos)
        binding.actvObjetivo.setAdapter(createNoFilterAdapter(objetivos))
        binding.actvObjetivo.setOnClickListener { binding.actvObjetivo.showDropDown() }
    }

    private fun createNoFilterAdapter(items: Array<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, items) {
            override fun getFilter(): android.widget.Filter {
                return object : android.widget.Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        return FilterResults().apply {
                            values = items
                            count = items.size
                        }
                    }
                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }
}