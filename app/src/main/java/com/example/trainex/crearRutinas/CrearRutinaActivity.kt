package com.example.trainex.crearRutinas

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.R
import com.example.trainex.rutinas.Rutina
import com.example.trainex.agregarEjercicios.AgregarEjercicioActivity
import com.example.trainex.databinding.ActivityCrearRutinaBinding
import com.example.trainex.ejercicio.DetalleEjercicioActivity
import com.example.trainex.utils.LanguageUtils

/**
 * Actividad principal para la creación o edición de rutinas de entrenamiento.
 * Se encarga de capturar el título, mostrar la lista de ejercicios elegidos y enviar la información
 * al ViewModel para su almacenamiento en la nube.
 */
class CrearRutinaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearRutinaBinding
    private val viewModel: CrearRutinaViewModel by viewModels()

    /** Lanzador para obtener los resultados de la actividad de selección de ejercicios. */
    private lateinit var agregarEjercicioLauncher: ActivityResultLauncher<Intent>
    private lateinit var adapter: CrearRutinaAdapter

    private var rutinaAEditar: Rutina? = null
    private var modoEdicion = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCrearRutinaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperar la rutina en caso de estar en modo edición
        rutinaAEditar = intent.getSerializableExtra("rutina_editar", Rutina::class.java)
        modoEdicion = rutinaAEditar != null

        configurarModo()
        initRecyclerView()
        initComponent()
        setupObservers()

        // Registro del contrato para recibir los ejercicios seleccionados desde AgregarEjercicioActivity
        agregarEjercicioLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val nuevosEjercicios = result.data?.getSerializableExtra(
                        "listaEjercicios",
                        ArrayList::class.java
                    ) as? ArrayList<Ejercicio>

                    nuevosEjercicios?.let { viewModel.setEjercicios(it) }
                }
            }
    }

    /**
     * Configura los observadores de LiveData para reaccionar a cambios en el ViewModel.
     */
    private fun setupObservers() {
        // Actualiza el adaptador cuando se modifica la lista de ejercicios
        viewModel.listaEjercicios.observe(this) { ejercicios ->
            adapter.actualizarLista(ejercicios)
        }

        // Gestiona la navegación y mensajes tras un guardado exitoso
        viewModel.operacionExitosa.observe(this) { exito ->
            if (exito) {
                val mensaje = if (modoEdicion) R.string.toast_update_success else R.string.toast_save_success
                Toast.makeText(this, getString(mensaje), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    /**
     * Define la apariencia y valores iniciales de la vista según el modo (Crear o Editar).
     */
    private fun configurarModo() {
        if (modoEdicion && rutinaAEditar != null) {
            binding.textView.text = getString(R.string.edit_routine_title)
            binding.etTituloRutina.setText(rutinaAEditar!!.titulo)
            binding.btnGuardarRutina.text = getString(R.string.update_button)
            viewModel.setEjercicios(ArrayList(rutinaAEditar!!.ejercicios))
        } else {
            binding.textView.text = getString(R.string.create_routine_title)
            binding.btnGuardarRutina.text = getString(R.string.save_button)
        }
    }

    /**
     * Inicializa el RecyclerView para mostrar los ejercicios seleccionados.
     */
    private fun initRecyclerView() {
        adapter = CrearRutinaAdapter(arrayListOf()) { ejercicioSeleccionado ->
            val intent = Intent(this, DetalleEjercicioActivity::class.java)
            intent.putExtra("EJERCICIO", ejercicioSeleccionado)
            startActivity(intent)
        }
        binding.rvEjerciciosSeleccionados.layoutManager = LinearLayoutManager(this)
        binding.rvEjerciciosSeleccionados.adapter = adapter
    }

    /**
     * Configura los listeners de los botones de interacción.
     */
    private fun initComponent() {
        binding.btnGuardarRutina.setOnClickListener {
            val titulo = binding.etTituloRutina.text.toString().trim()

            // Validaciones de formulario
            if (titulo.isEmpty()) {
                binding.etTituloRutina.error = getString(R.string.validation_title_error)
                return@setOnClickListener
            }

            if (viewModel.listaEjercicios.value.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.validation_exercise_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.guardarRutina(titulo, modoEdicion, rutinaAEditar)
        }

        binding.enlaceCancelar.setOnClickListener { finish() }

        binding.btnAgregarEjercicio.setOnClickListener {
            val intent = Intent(this, AgregarEjercicioActivity::class.java)
            intent.putExtra("listaEjercicios", viewModel.listaEjercicios.value)
            agregarEjercicioLauncher.launch(intent)
        }
    }
}