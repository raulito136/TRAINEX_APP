package com.example.trainex.iniciarRutina

import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.R
import com.example.trainex.databinding.ActivityEntrenamientoTablasBinding
import com.example.trainex.rutinas.Rutina
import com.example.trainex.utils.LanguageUtils
import java.util.concurrent.TimeUnit

/**
 * Activity que gestiona la pantalla de entrenamiento activo con tablas de series.
 */
class EntrenamientoTablasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntrenamientoTablasBinding
    private lateinit var adapter: EjerciciosTablasAdapter
    private lateinit var rutina: Rutina

    private val viewModel: EntrenamientoTablasViewModel by viewModels()
    private var entrenamientoIniciado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Asegurar que el idioma sea correcto antes de cargar la UI
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityEntrenamientoTablasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar el objeto Rutina pasado desde la pantalla anterior
        rutina = intent.getSerializableExtra("RUTINA") as? Rutina ?: run { finish(); return }

        setupObservers()
        // Cargar los últimos pesos realizados para estos ejercicios desde Room
        viewModel.cargarDatosPrevios(rutina)
    }

    /**
     * Suscripción a los cambios de estado en el ViewModel.
     */
    private fun setupObservers() {
        // Se ejecuta cuando el ViewModel ha terminado de buscar los pesos previos en Room
        viewModel.ejerciciosRellenos.observe(this) { listaRellena ->
            setupUI(listaRellena)
        }

        // Se ejecuta tras guardar exitosamente en Firebase y Room al terminar
        viewModel.operacionExitosa.observe(this) { exitoso ->
            if (exitoso) {
                val tiempoTranscurrido = SystemClock.elapsedRealtime() - binding.chronometerTiempo.base
                showSuccessDialog(formatTime(tiempoTranscurrido))
            }
        }

        // Manejo de errores durante el proceso de guardado
        viewModel.error.observe(this) { msg ->
            msg?.let { Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show() }
        }
    }

    /**
     * Configuración inicial de la interfaz y el RecyclerView.
     */
    private fun setupUI(ejerciciosConSeries: List<EjercicioConSeries>) {
        binding.tvTituloRutina.text = rutina.titulo

        // Inicializar el adaptador con los datos (incluyendo pesos previos de Room)
        adapter = EjerciciosTablasAdapter(ejerciciosConSeries.toMutableList())
        binding.rvEjerciciosConSeries.layoutManager = LinearLayoutManager(this)
        binding.rvEjerciciosConSeries.adapter = adapter

        // Botón único para iniciar o finalizar la sesión
        binding.btnStartFinishTraining.setOnClickListener {
            if (!entrenamientoIniciado) comenzarEntrenamiento() else confirmarFinalizacion()
        }
    }

    /**
     * Inicia el cronómetro y cambia el estado visual del botón principal.
     */
    private fun comenzarEntrenamiento() {
        entrenamientoIniciado = true
        binding.btnStartFinishTraining.text = getString(R.string.finalizar_entrenamiento)
        binding.btnStartFinishTraining.setBackgroundColor(getColor(R.color.tercerColorFondoRojoBoton))

        // El cronómetro usa el tiempo base del sistema para precisión
        binding.chronometerTiempo.base = SystemClock.elapsedRealtime()
        binding.chronometerTiempo.start()
    }

    /**
     * Calcula el tiempo total y delega el guardado de datos al ViewModel.
     */
    private fun confirmarFinalizacion() {
        val tiempoMillis = SystemClock.elapsedRealtime() - binding.chronometerTiempo.base
        viewModel.finalizarEntrenamiento(rutina, tiempoMillis, adapter.obtenerEjercicios())
    }

    /**
     * Convierte milisegundos a un formato legible: "X h XX min XX s".
     */
    private fun formatTime(millis: Long): String {
        val h = TimeUnit.MILLISECONDS.toHours(millis)
        val m = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val s = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return if (h > 0) String.format("%d h %02d min %02d s", h, m, s)
        else String.format("%02d min %02d s", m, s)
    }

    /**
     * Muestra un diálogo de éxito con el tiempo total antes de cerrar la actividad.
     */
    private fun showSuccessDialog(tiempoTotal: String) {
        binding.chronometerTiempo.stop()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enhorabuena))
            .setMessage("${getString(R.string.mensaje_finalizar)}\n\n$tiempoTotal")
            .setCancelable(false)
            .setPositiveButton(getString(R.string.aceptar)) { _, _ -> finish() }
            .show()
    }
}