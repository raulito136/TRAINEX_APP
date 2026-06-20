package com.example.trainex.rutinas

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.iniciarRutina.EntrenamientoTablasActivity
import com.example.trainex.databinding.ActivityRutinaEjerciciosBinding
import com.example.trainex.ejercicio.EjercicioAdapter

/**
 * Actividad que muestra la lista de ejercicios contenidos en una rutina específica.
 * Permite al usuario previsualizar los ejercicios antes de comenzar la sesión de entrenamiento.
 */
class RutinaEjerciciosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRutinaEjerciciosBinding
    private val viewModel: RutinaEjerciciosViewModel by viewModels()

    /**
     * Inicializa la actividad, configura el View Binding, gestiona los insets del sistema
     * y recupera la rutina pasada a través del Intent.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRutinaEjerciciosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aplicar padding para barras del sistema para un diseño edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperar el objeto Rutina. Si es nulo, se cierra la actividad por seguridad.
        val rutinaIntent = intent.getSerializableExtra("RUTINA") as? Rutina ?: run { finish(); return }
        viewModel.setRutina(rutinaIntent)

        setupUI()
        setupObservers()
    }

    /**
     * Configura los componentes visuales de la interfaz, incluyendo el RecyclerView y los listeners de botones.
     */
    private fun setupUI() {
        binding.rvEjercicios.layoutManager = LinearLayoutManager(this)

        // Configurar botón de retroceso para finalizar la actividad
        binding.tvBack?.setOnClickListener { finish() }

        /**
         * Al pulsar el botón de inicio, se navega hacia la actividad de ejecución
         * del entrenamiento ([EntrenamientoTablasActivity]) pasando la rutina actual.
         */
        binding.btnStartRoutine.setOnClickListener {
            val intent = Intent(this, EntrenamientoTablasActivity::class.java)
            intent.putExtra("RUTINA", viewModel.rutina.value)
            startActivity(intent)
        }
    }

    /**
     * Observa los cambios en el LiveData del ViewModel para actualizar el título
     * de la rutina y cargar la lista de ejercicios en el adaptador.
     */
    private fun setupObservers() {
        viewModel.rutina.observe(this) { rutina ->
            binding.tvTituloRutina.text = rutina.titulo
            // Pasamos los ejercicios al adaptador y definimos la acción al hacer clic (vacía en este caso)
            binding.rvEjercicios.adapter = EjercicioAdapter(rutina.ejercicios) { _ ->
            }
        }
    }
}