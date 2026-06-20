package com.example.trainex.rutinas

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.PerfilActivity
import com.example.trainex.R
import com.example.trainex.crearRutinas.CrearRutinaActivity
import com.example.trainex.databinding.ActivityEntrenamientoBinding
import com.example.trainex.dietas.DietasActivity
import com.example.trainex.formulario.registro.FormulariosRegistroActivity
import com.example.trainex.perfil.UserManager
import com.example.trainex.utils.LanguageUtils
import com.example.trainex.utils.TraductorUniversal
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlin.math.abs

/**
 * Actividad principal del módulo de rutinas de entrenamiento.
 * Gestiona la visualización de las rutinas del usuario, la creación manual y la
 * generación inteligente mediante IA, integrando además anuncios recompensados.
 */
class Entrenamiento : AppCompatActivity() {

    private lateinit var binding: ActivityEntrenamientoBinding
    private lateinit var adapter: RutinaAdapter
    private lateinit var gestureDetector: GestureDetector
    private val viewModel: EntrenamientoViewModel by viewModels()

    private var mRewardedAd: RewardedAd? = null
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private var iaGenerandoEnProceso = false

    /**
     * Inicializa la actividad, configura los servicios de traducción, publicidad,
     * diseño de borde a borde y los observadores de datos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        val idioma = LanguageUtils.getIdiomaActual(this)
        TraductorUniversal.configurarIdioma(idioma)
        TraductorUniversal.preparar()
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        enableEdgeToEdge()
        binding = ActivityEntrenamientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupListeners()
        setupObservers()
        iniciarDetectorGestos()

        viewModel.validarUsuario()
        procesarIntentoGeneracion(intent)
    }

    /**
     * Suscribe la interfaz a los cambios en el estado del usuario, la lista de rutinas
     * y las notificaciones del generador de IA.
     */
    private fun setupObservers() {
        viewModel.usuarioValido.observe(this) { usuario ->
            if (usuario == null) {
                startActivity(Intent(this, FormulariosRegistroActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            } else {
                UserManager.saveUserData(this, mapOf("username" to usuario.username, "nombre" to usuario.nombre))
            }
        }

        viewModel.rutinas.observe(this) { lista ->
            adapter.actualizarLista(lista)
        }

        viewModel.iaStatus.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    /**
     * Configura los eventos de clic para la navegación y la creación de rutinas.
     */
    private fun setupListeners() {
        binding.cvNuevaRutina.setOnClickListener { startActivity(Intent(this, CrearRutinaActivity::class.java)) }
        binding.cvGenerarRutina.setOnClickListener { startActivity(Intent(this, GenerarRutinaActivity::class.java)) }
        binding.ivPerfil.setOnClickListener { irAPerfilConAnimacion() }
        binding.ivCalorias.setOnClickListener { irADietasConAnimacion() }
    }

    /**
     * Prepara el RecyclerView para mostrar el listado de rutinas, definiendo las acciones
     * de clic, edición y eliminación.
     */
    private fun setupRecyclerView() {
        binding.rvMisRutinas.layoutManager = LinearLayoutManager(this)
        adapter = RutinaAdapter(arrayListOf(),
            onRutinaClick = { r -> startActivity(Intent(this, RutinaEjerciciosActivity::class.java).apply { putExtra("RUTINA", r) }) },
            onEditarClick = { r -> startActivity(Intent(this, CrearRutinaActivity::class.java).apply { putExtra("rutina_editar", r) }) },
            onEliminarClick = { confirmarEliminacion(it) }
        )
        binding.rvMisRutinas.adapter = adapter
    }

    /**
     * Muestra un diálogo de advertencia para confirmar la eliminación definitiva de una rutina.
     */
    private fun confirmarEliminacion(rutina: Rutina) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.eliminar))
            .setMessage(getString(R.string.mensaje_borrar_rutina, rutina.titulo))
            .setPositiveButton(getString(R.string.eliminar)) { _, _ -> viewModel.eliminarRutina(rutina.firebaseId) }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    // --- ANUNCIOS Y GESTOS ---

    /**
     * Evalúa si la actividad fue iniciada con una orden de generación automática de rutina.
     */
    private fun procesarIntentoGeneracion(intent: Intent?) {
        val generar = intent?.getBooleanExtra("generar_rutina", false) ?: false
        if (generar && !iaGenerandoEnProceso) {
            val edad = intent?.getStringExtra("edad") ?: ""
            val dias = intent?.getStringExtra("dias")?.filter { it.isDigit() }?.toIntOrNull() ?: 1
            val obj = intent?.getStringExtra("objetivo") ?: ""
            val exp = intent?.getStringExtra("experiencia") ?: ""

            iaGenerandoEnProceso = true
            showAdAndGenerate(dias, obj, exp, "Edad: $edad, Exp: $exp")
        }
    }

    /**
     * Muestra un anuncio de video recompensado antes de proceder con la generación de IA.
     */
    private fun showAdAndGenerate(dias: Int, obj: String, exp: String, info: String) {
        loadRewardedAd(onAdReady = { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    viewModel.generarRutinaIA(dias, obj, exp, info)
                    iaGenerandoEnProceso = false
                }
            }
            ad.show(this) {}
        }, onAdFailed = {
            viewModel.generarRutinaIA(dias, obj, exp, info)
            iaGenerandoEnProceso = false
        })
    }

    /**
     * Carga un anuncio recompensado de AdMob.
     */
    private fun loadRewardedAd(onAdReady: (RewardedAd) -> Unit, onAdFailed: () -> Unit) {
        RewardedAd.load(this, AD_UNIT_ID, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(e: LoadAdError) { onAdFailed() }
            override fun onAdLoaded(ad: RewardedAd) { onAdReady(ad) }
        })
    }

    /**
     * Inicializa la detección de gestos de deslizamiento (swipe) para navegar entre pantallas.
     */
    private fun iniciarDetectorGestos() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                if (abs(diffX) > 100 && abs(vX) > 100) {
                    if (diffX > 0) irAPerfilConAnimacion() else irADietasConAnimacion()
                    return true
                }
                return false
            }
        })
    }

    /**
     * Intercepta los toques en pantalla para procesar primero los gestos de navegación.
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean = gestureDetector.onTouchEvent(ev!!) || super.dispatchTouchEvent(ev)

    /**
     * Navega al Perfil con una animación de entrada desde la izquierda.
     */
    private fun irAPerfilConAnimacion() {
        startActivity(Intent(this, PerfilActivity::class.java),
            ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right).toBundle())
        finish()
    }

    /**
     * Navega a Dietas con una animación de entrada desde la derecha.
     */
    private fun irADietasConAnimacion() {
        startActivity(Intent(this, DietasActivity::class.java),
            ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left).toBundle())
        finish()
    }
}