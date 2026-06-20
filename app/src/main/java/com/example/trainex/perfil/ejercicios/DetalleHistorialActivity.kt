package com.example.trainex.perfil.ejercicios

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.trainex.R
import com.example.trainex.databinding.ActivityDetalleHistorialBinding
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.utils.LanguageUtils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DetalleHistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleHistorialBinding
    private val viewModel: DetalleHistorialViewModel by viewModels()
    private var exoPlayer: ExoPlayer? = null

    /** Palabras clave para identificar ejercicios de cardio/distancia */
    private val keywordsCardio = listOf(
        "bicicleta", "bike", "caminadora", "treadmill", "cinta",
        "caminata", "walk", "correr", "run", "running", "senderismo", "hike",
        "eliptico", "elliptical", "cycling", "carrera", "bici", "caminar",
        "estática", "estatica", "spinning", "cardio", "aeróbico", "aerobico"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetalleHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ejercicio = intent.getSerializableExtra("EJERCICIO") as? Ejercicio
        val userId = intent.getStringExtra("USER_ID")

        if (ejercicio != null) {
            val esCardio = esEjercicioCardio(ejercicio.nombre)
            setupUI(ejercicio, esCardio)
            setupVideoPlayer()
            cargarContenidoMultimedia(ejercicio)

            // Observar los records del ViewModel
            observarViewModel(esCardio)
            // Cargar estadísticas pasando el ejercicio completo
            viewModel.cargarEstadisticas(ejercicio, userId)
        } else {
            finish()
            return
        }

        binding.tvBack.setOnClickListener { finish() }
    }

    /**
     * Determina si un ejercicio es de cardio basándose en palabras clave en su nombre.
     */
    private fun esEjercicioCardio(nombre: String): Boolean {
        return keywordsCardio.any { nombre.contains(it, ignoreCase = true) }
    }

    /**
     * Escucha los cambios en el estado de los récords del ViewModel y actualiza los TextViews.
     * Muestra diferentes estadísticas según si es cardio (distancia/tiempo) o musculación (peso/volumen).
     */
    private fun observarViewModel(esCardio: Boolean) {
        lifecycleScope.launch {
            viewModel.records.collect { state ->
                when (state) {
                    is DetalleHistorialViewModel.RecordsState.Musculacion -> {
                        binding.maryorPeso.text = state.maxPeso
                        binding.mejorVolumenSerie.text = state.mejorSerie
                        binding.mejorVolumenTotal.text = state.mejorVolumenTotal
                    }
                    is DetalleHistorialViewModel.RecordsState.Cardio -> {
                        // Para cardio: maxDistancia, distanciaTotal, tiempoTotal
                        binding.maryorPeso.text = state.maxDistancia
                        binding.mejorVolumenSerie.text = state.distanciaTotal
                        binding.mejorVolumenTotal.text = state.tiempoTotal
                    }
                    is DetalleHistorialViewModel.RecordsState.Empty -> {
                        binding.maryorPeso.text = "-"
                        binding.mejorVolumenSerie.text = "-"
                        binding.mejorVolumenTotal.text = "-"
                    }
                    is DetalleHistorialViewModel.RecordsState.Error -> {
                        binding.maryorPeso.text = "Error"
                        binding.mejorVolumenSerie.text = "Error"
                        binding.mejorVolumenTotal.text = "Error"
                    }
                    is DetalleHistorialViewModel.RecordsState.Loading -> {
                        binding.maryorPeso.text = "..."
                        binding.mejorVolumenSerie.text = "..."
                        binding.mejorVolumenTotal.text = "..."
                    }
                }
            }
        }
    }

    /**
     * Configura la UI incluyendo los títulos de los ejercicios y los labels según el tipo.
     */
    private fun setupUI(ejercicio: Ejercicio, esCardio: Boolean) {
        binding.tvTituloEjercicio.text = getStringFromKey(ejercicio.nombre)
        binding.tituloEjercicio.text = getStringFromKey(ejercicio.nombre)
        binding.grupoMuscular.text = getStringFromKey(ejercicio.grupoMuscular)

        // Actualizar labels según el tipo de ejercicio
        val label1 = findViewById<TextView>(R.id.textView9)   // Mayor peso / Mayor distancia
        val label2 = findViewById<TextView>(R.id.textView10)  // Mejor volumen serie / Distancia total
        val label3 = findViewById<TextView>(R.id.textView11)  // Mejor volumen total / Tiempo total

        if (esCardio) {
            label1?.text = getString(R.string.mayor_distancia)
            label2?.text = getString(R.string.distancia_total)
            label3?.text = getString(R.string.tiempo_total)
        } else {
            label1?.text = getString(R.string.mayor_peso)
            label2?.text = getString(R.string.mejor_volumen_serie)
            label3?.text = getString(R.string.mejor_volumen_total)
        }
    }

    private fun cargarContenidoMultimedia(ejercicio: Ejercicio) {
        if (ejercicio.urlVideo.isEmpty()) {
            binding.playerView.visibility = View.GONE
            binding.ivImagenEjercicio.visibility = View.VISIBLE
            val resId = resources.getIdentifier(ejercicio.imagen, "drawable", packageName)
            if (resId != 0) binding.ivImagenEjercicio.setImageResource(resId)
        } else {
            binding.ivImagenEjercicio.visibility = View.GONE
            binding.playerView.visibility = View.VISIBLE
            loadVideo(ejercicio.urlVideo)
        }
    }

    private fun getStringFromKey(key: String): String {
        val resId = resources.getIdentifier(key, "string", packageName)
        return if (resId != 0) getString(resId) else key
    }

    private fun setupVideoPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.player = exoPlayer
        exoPlayer?.repeatMode = Player.REPEAT_MODE_ALL
    }

    private fun loadVideo(url: String) {
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    override fun onDestroy() { super.onDestroy(); exoPlayer?.release() }
    override fun onPause() { super.onPause(); exoPlayer?.pause() }
    override fun onResume() { super.onResume(); exoPlayer?.play() }
}
