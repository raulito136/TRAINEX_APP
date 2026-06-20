package com.example.trainex.ejercicio

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.trainex.R
import com.example.trainex.databinding.ActivityDetalleEjercicioBinding
import com.example.trainex.utils.LanguageUtils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout

class DetalleEjercicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEjercicioBinding
    private val viewModel: DetalleEjercicioViewModel by viewModels()
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aseguramos que el idioma sea el correcto antes de mostrar nada
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEjercicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()   // Ajuste para pantallas modernas (Edge-to-Edge)
        setupVideoPlayer()  // Preparación del motor de video

        // Intentamos recuperar el ejercicio enviado desde la lista
        val ejercicioIntent = intent.getSerializableExtra("EJERCICIO") as? Ejercicio
        ejercicioIntent?.let {
            viewModel.setEjercicio(it)
        } ?: finish() // Si no hay datos, cerramos la pantalla para evitar errores

        setupObservers() // Empezamos a escuchar al ViewModel

        binding.tvBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupObservers() {
        // Cuando el ejercicio esté listo en el ViewModel, actualizamos la pantalla
        viewModel.ejercicio.observe(this) { ejercicio ->
            actualizarUI(ejercicio)
        }
    }

    private fun actualizarUI(ejercicio: Ejercicio) {
        // Traducción dinámica: buscamos el texto real usando la clave guardada
        binding.tvTituloEjercicio.text = getStringFromKey(ejercicio.nombre)

        // Usamos strings con formato (ej: "Dificultad: %s") para que el prefijo también se traduzca
        binding.tvGrupoMuscular.text = getString(R.string.formato_grupo_muscular, getStringFromKey(ejercicio.grupoMuscular))
        binding.tvDificultad.text = getString(R.string.formato_dificultad, getStringFromKey(ejercicio.dificultad))
        binding.tvDescripcion.text = getStringFromKey(ejercicio.descripcion)

        // Decisión: ¿Mostramos video o imagen estática?
        if (ejercicio.urlVideo.isNullOrEmpty()) {
            binding.playerView.visibility = View.GONE
            binding.ivImagenEjercicio.visibility = View.VISIBLE

            // Buscamos la imagen en la carpeta drawable por su nombre
            val resId = resources.getIdentifier(ejercicio.imagen, "drawable", packageName)

            Glide.with(this)
                .load(if (resId != 0) resId else R.drawable.ic_image_not_found)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_image_not_found)
                .into(binding.ivImagenEjercicio)
        } else {
            binding.playerView.visibility = View.VISIBLE
            binding.ivImagenEjercicio.visibility = View.GONE
            loadVideo(ejercicio.urlVideo)
        }
    }

    private fun setupVideoPlayer() {
        binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        binding.playerView.useController = false // Estilo "loop" limpio sin botones
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.player = exoPlayer
    }

    private fun loadVideo(videoUrl: String) {
        if (videoUrl.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ONE // Bucle infinito
                prepare()
                playWhenReady = true
            }
        }
    }

    /**
     * Convierte una clave (ej: "press_pecho") en el texto traducido del strings.xml
     */
    private fun getStringFromKey(key: String): String {
        val resourceId = resources.getIdentifier(key, "string", packageName)
        return if (resourceId != 0) getString(resourceId) else key
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // --- Gestión de memoria y recursos del reproductor ---
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release() // ¡Vital para evitar fugas de memoria!
        exoPlayer = null
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.play()
    }
}