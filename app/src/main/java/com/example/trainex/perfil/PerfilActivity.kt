package com.example.trainex

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.trainex.databinding.ActivityPerfilBinding
import com.example.trainex.dietas.DietasActivity
import com.example.trainex.historial.HistorialActivity
import com.example.trainex.perfil.ChartsAdapter
import com.example.trainex.perfil.PerfilViewModel
import com.example.trainex.perfil.ajustes.AjustesActivity
import com.example.trainex.perfil.buscador.BuscadorUsuariosActivity
import com.example.trainex.perfil.calendario.HistorialCalendarioActivity
import com.example.trainex.perfil.foto.FotoProgreso
import com.example.trainex.perfil.foto.FotosAdapter
import com.example.trainex.perfil.medidas.MedidasActivity
import com.example.trainex.perfil.seguidos_seguidores.SeguidosSeguidoresActivity
import com.example.trainex.rutinas.Entrenamiento
import com.example.trainex.utils.LanguageUtils
import com.example.trainex.utils.UnitManager
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import kotlin.math.abs

/**
 * Actividad principal del perfil de usuario.
 * Gestiona la visualización de datos personales, estadísticas (IMC, Peso), gráficas de progreso,
 * galería de fotos y navegación hacia otras secciones (Ajustes, Entrenamiento, Dietas).
 */
class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private val viewModel: PerfilViewModel by viewModels()
    private lateinit var adapterFotos: FotosAdapter
    private lateinit var gestureDetector: GestureDetector
    private var uriFotoCamara: Uri? = null

    /** Manejador para seleccionar imágenes de la galería del dispositivo. */
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.procesarSubidaFoto(it) }
    }

    /** Manejador para capturar fotografías directamente desde la cámara. */
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && uriFotoCamara != null) {
            viewModel.procesarSubidaFoto(uriFotoCamara!!)
        }
    }

    /**
     * Inicializa la actividad, configura los componentes visuales y carga los datos del ViewModel.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupRecyclerViewFotos()
        setupListeners()
        setupObservers()
        iniciarDetectorGestos()

        viewModel.cargarDatos()
    }

    /**
     * Refresca los datos del perfil cada vez que el usuario regresa a la actividad.
     */
    override fun onResume() {
        super.onResume()
        viewModel.cargarDatos()
    }

    /**
     * Configura los observadores de LiveData para actualizar la UI en tiempo real.
     */
    private fun setupObservers() {
        // Observar datos de perfil e IMC
        viewModel.perfilData.observe(this) { usuario ->
            usuario?.let {
                val alturaM = if (it.altura > 3) it.altura / 100 else it.altura
                val imc = if (alturaM > 0) it.peso / (alturaM * alturaM) else 0.0

                val unidadPeso = UnitManager.obtener(this, "unidad_peso", "kg")
                val pesoVisual = UnitManager.aVisual(it.peso, unidadPeso)

                // --- INICIO CAMBIO MÍNIMO ---
                val unidadLongitud = UnitManager.obtener(this, "unidad_longitud", "cm")
                // Pasamos la altura a cm (alturaM * 100) porque UnitManager espera centímetros
                val alturaVisual = UnitManager.aVisual(alturaM * 100, unidadLongitud)

                binding.tvUserName.text = it.username

                val etiquetaAltura = getString(R.string.altura)
                binding.tvHeight.text = "$etiquetaAltura: $alturaVisual $unidadLongitud"

                binding.tvAge.text = getString(R.string.age_format, it.edad)
                binding.tvBMI.text = getString(R.string.formato_imc, imc)
                val etiqueta = getString(R.string.grafico_etiqueta_peso)
                binding.tvWeight.text = "$etiqueta: $pesoVisual $unidadPeso"

                actualizarFotoCabecera(it.foto)
            }
        }

        // Observar conteos de comunidad
        viewModel.counts.observe(this) { (seguidores, seguidos) ->
            binding.tvSeguidores.text = getString(R.string.seguidores, seguidores)
            binding.tvSeguidos.text = getString(R.string.seguidos, seguidos)
        }

        // Observar gráficas (Peso y Volumen)
        viewModel.chartData.observe(this) { (dataSets, labels) ->
            binding.vpCharts.adapter = ChartsAdapter(dataSets, labels)
            TabLayoutMediator(binding.tabLayoutCharts, binding.vpCharts) { tab, position ->
                tab.text = dataSets[position].first
            }.attach()
        }

        // Observar lista de fotos de progreso
        viewModel.fotos.observe(this) { fotos ->
            adapterFotos.actualizarLista(fotos)
            binding.rvFotosProgreso.visibility = if (fotos.isEmpty()) View.GONE else View.VISIBLE
        }

        // Observar mensajes de estado/toast
        viewModel.statusMessage.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    /**
     * Define los eventos de clic para los botones y elementos interactivos.
     */
    private fun setupListeners() {
        binding.ivEntrenamiento.setOnClickListener { irAEntrenamientoConAnimacion() }
        binding.ivCalorias.setOnClickListener {
            startActivity(Intent(this, DietasActivity::class.java))
            finish()
        }
        binding.ivSettings.setOnClickListener { startActivity(Intent(this, AjustesActivity::class.java)) }
        binding.btnEjercicios.setOnClickListener { startActivity(Intent(this, HistorialActivity::class.java)) }
        binding.btnCalendario.setOnClickListener { startActivity(Intent(this, HistorialCalendarioActivity::class.java)) }
        binding.btnMedidas.setOnClickListener { startActivity(Intent(this, MedidasActivity::class.java)) }
        binding.btnAddFoto.setOnClickListener { mostrarDialogoSeleccion() }
        binding.ivSearch.setOnClickListener { startActivity(Intent(this, BuscadorUsuariosActivity::class.java)) }

        binding.tvSeguidores.setOnClickListener { abrirSeguidosSeguidores(0) }
        binding.tvSeguidos.setOnClickListener { abrirSeguidosSeguidores(1) }
    }

    /**
     * Configura el RecyclerView horizontal para la galería de fotos de progreso.
     */
    private fun setupRecyclerViewFotos() {
        adapterFotos = FotosAdapter(
            lista = mutableListOf(),
            onFotoClick = { foto -> mostrarFotoConZoom(foto) },
            onFotoLongClick = { foto -> mostrarDialogoEliminarFoto(foto) }
        )
        binding.rvFotosProgreso.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFotosProgreso.adapter = adapterFotos
    }

    /**
     * Muestra un diálogo para elegir entre capturar una foto o seleccionarla de la galería.
     */
    private fun mostrarDialogoSeleccion() {
        val opciones = arrayOf(getString(R.string.opcion_hacer_foto), getString(R.string.opcion_galeria))
        AlertDialog.Builder(this)
            .setTitle(R.string.dialogo_titulo_foto)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> abrirCamara()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }.show()
    }

    /**
     * Prepara el archivo temporal e inicia la aplicación de cámara del sistema.
     */
    private fun abrirCamara() {
        try {
            val archivoFoto = File.createTempFile("img_${System.currentTimeMillis()}_", ".jpg", externalCacheDir)
            uriFotoCamara = FileProvider.getUriForFile(this, "${packageName}.fileprovider", archivoFoto)
            uriFotoCamara?.let { takePictureLauncher.launch(it) }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_iniciar_camara, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Abre un diálogo a pantalla completa para previsualizar una foto con capacidades de zoom.
     */
    private fun mostrarFotoConZoom(foto: FotoProgreso) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val photoView = PhotoView(this)
        dialog.setContentView(photoView)

        if (foto.url.length > 200 && !foto.url.startsWith("http")) {
            val imageBytes = Base64.decode(foto.url, Base64.DEFAULT)
            Glide.with(this).load(imageBytes).into(photoView)
        } else {
            Glide.with(this).load(foto.url).into(photoView)
        }
        photoView.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Muestra una advertencia antes de proceder con la eliminación de una foto de progreso.
     */
    private fun mostrarDialogoEliminarFoto(foto: FotoProgreso) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialogo_titulo_eliminar)
            .setMessage(R.string.dialogo_mensaje_eliminar)
            .setPositiveButton(R.string.accion_eliminar) { _, _ -> viewModel.eliminarFoto(foto.id) }
            .setNegativeButton(R.string.accion_cancelar, null)
            .show()
    }

    /**
     * Actualiza el avatar circular en la cabecera del perfil.
     */
    private fun actualizarFotoCabecera(fotoStr: String) {
        if (fotoStr.isNotEmpty()) {
            val imageBytes = Base64.decode(fotoStr, Base64.DEFAULT)
            Glide.with(this).load(imageBytes).circleCrop().into(binding.ivUserAvatar)
        }
    }

    /**
     * Configura el detector de gestos para soportar navegación mediante deslizamiento (Swipe).
     */
    private fun iniciarDetectorGestos() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                if (abs(diffX) > 100 && abs(vX) > 100 && diffX < 0) { // Swipe Izquierda
                    irAEntrenamientoConAnimacion()
                    return true
                }
                return false
            }
        })
    }

    /**
     * Intercepta los eventos de toque para diferenciar entre gestos de navegación y scroll en las gráficas.
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            val viewPagerRect = Rect()
            binding.vpCharts.getGlobalVisibleRect(viewPagerRect)
            if (viewPagerRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) return super.dispatchTouchEvent(ev)
            if (gestureDetector.onTouchEvent(ev)) return true
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * Navega a la pantalla de Entrenamiento aplicando una transición animada personalizada.
     */
    private fun irAEntrenamientoConAnimacion() {
        val options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
        startActivity(Intent(this, Entrenamiento::class.java), options.toBundle())
        finish()
    }

    /**
     * Navega a la pantalla social (seguidores/seguidos) seleccionando la pestaña correspondiente.
     */
    private fun abrirSeguidosSeguidores(tabIndex: Int) {
        viewModel.perfilData.value?.let {
            val intent = Intent(this, SeguidosSeguidoresActivity::class.java).apply {
                putExtra("USER_ID", it.id)
                putExtra("USERNAME", it.username)
                putExtra("START_TAB", tabIndex)
            }
            startActivity(intent)
        }
    }

    /**
     * Ajusta los márgenes de la vista para evitar solapamientos con las barras del sistema.
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}