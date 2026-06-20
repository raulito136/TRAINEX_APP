package com.example.trainex.dietas

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.PerfilActivity
import com.example.trainex.R
import com.example.trainex.databinding.ActivityDietasBinding
import com.example.trainex.rutinas.Entrenamiento
import com.example.trainex.utils.LanguageUtils
import kotlin.math.abs

/**
 * Activity que representa el diario nutricional del usuario siguiendo el patrón MVVM.
 * Se encarga de la visualización de macronutrientes, calorías y la gestión de las distintas comidas del día.
 */
class DietasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDietasBinding
    private lateinit var gestureDetector: GestureDetector

    /** Inyección del ViewModel para gestionar la lógica de negocio de la dieta. */
    private val viewModel: DietasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDietasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupObservers()
        setupListeners()
        iniciarDetectorGestos()
    }

    override fun onResume() {
        super.onResume()
        // Dispara la carga de datos cada vez que la actividad vuelve al primer plano.
        viewModel.cargarDatos()
    }

    /**
     * Configura los observadores de LiveData para reaccionar a los cambios de estado en el ViewModel.
     */
    private fun setupObservers() {
        // Observa los objetivos nutricionales calculados (límites diarios).
        viewModel.objetivos.observe(this) { objetivos ->
            val totales = viewModel.totales.value
            if (totales != null) {
                actualizarBarras(totales.cal, totales.prot, totales.carb, totales.gras, objetivos)
            }
        }

        // Observa los totales consumidos acumulados en el día.
        viewModel.totales.observe(this) { totales ->
            val obj = viewModel.objetivos.value
            if (obj != null) {
                actualizarBarras(totales.cal, totales.prot, totales.carb, totales.gras, obj)
            }
        }

        // Distribuye las listas de alimentos en sus respectivos RecyclerViews por tipo de comida.
        viewModel.comidas.observe(this) { mapaComidas ->
            mapaComidas["Desayuno"]?.let {
                setupAdapter(binding.rvBreakfast, it)
                actualizarResumenSeccion(binding.tvBreakfastSummary, it)
            }
            mapaComidas["Comida"]?.let {
                setupAdapter(binding.rvLunch, it)
                actualizarResumenSeccion(binding.tvLunchSummary, it)
            }
            mapaComidas["Cena"]?.let {
                setupAdapter(binding.rvDinner, it)
                actualizarResumenSeccion(binding.tvDinnerSummary, it)
            }
            mapaComidas["Snack"]?.let {
                setupAdapter(binding.rvSnack, it)
                actualizarResumenSeccion(binding.tvSnackSummary, it)
            }
        }
    }

    /**
     * Inicializa el adaptador para un RecyclerView de alimentos diario.
     * @param recyclerView El componente visual a configurar.
     * @param lista La lista de alimentos a mostrar en dicha sección.
     */
    private fun setupAdapter(recyclerView: RecyclerView, lista: List<AlimentoDiario>) {
        val adapter = DiarioAdapter(
            listaComidas = lista,
            onCheckChanged = { item, isChecked ->
                viewModel.cambiarEstadoCompletado(item, isChecked)
            },
            onLongClick = { item ->
                viewModel.borrarAlimento(item)
                Toast.makeText(this, getString(R.string.msg_eliminado_ok), Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /**
     * Calcula y muestra el resumen numérico de macronutrientes para una sección específica.
     */
    private fun actualizarResumenSeccion(textView: TextView, lista: List<AlimentoDiario>) {
        val k = lista.sumOf { it.kcalTotales }
        val p = lista.sumOf { it.protTotales }.toInt()
        val c = lista.sumOf { it.carbTotales }.toInt()
        val g = lista.sumOf { it.grasTotales }.toInt()

        textView.text = getString(R.string.formato_resumen_comida, k, p, c, g)
    }

    /**
     * Actualiza los valores de texto y las barras de progreso de la interfaz.
     */
    private fun actualizarBarras(
        calActual: Int, protActual: Int, carbActual: Int, grasActual: Int,
        obj: DietasViewModel.NutricionObjetivos
    ) {
        binding.tvCalorieSummary.text = getString(R.string.formato_calorias_total, calActual, obj.cal)
        binding.tvProteinValue.text = getString(R.string.formato_macro_gramos, protActual, obj.prot)
        binding.tvCarbsValue.text = getString(R.string.formato_macro_gramos, carbActual, obj.carb)
        binding.tvFatValue.text = getString(R.string.formato_macro_gramos, grasActual, obj.gras)

        configurarBarraConColor(binding.progressCalories, calActual, obj.cal)
        configurarBarraConColor(binding.progressProtein, protActual, obj.prot)
        configurarBarraConColor(binding.progressCarbs, carbActual, obj.carb)
        configurarBarraConColor(binding.progressFat, grasActual, obj.gras)
    }

    /**
     * Aplica animaciones y cambios de color a las barras de progreso según el porcentaje alcanzado.
     */
    private fun configurarBarraConColor(progressBar: android.widget.ProgressBar, actual: Int, objetivo: Int) {
        progressBar.max = objetivo

        val animacion = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, actual)
        animacion.duration = 1000
        animacion.interpolator = DecelerateInterpolator()
        animacion.start()

        val porcentaje = if (objetivo > 0) (actual.toDouble() / objetivo) * 100 else 0.0

        // Cambio cromático: Verde (ok), Naranja (límite), Rojo (exceso).
        val colorHex = when {
            porcentaje > 115 -> "#F44336"
            porcentaje > 100 -> "#FF9800"
            else -> "#4CAF50"
        }

        val layerDrawable = progressBar.progressDrawable.mutate() as? LayerDrawable
        val progressLayer = layerDrawable?.findDrawableByLayerId(android.R.id.progress)
        progressLayer?.setTint(Color.parseColor(colorHex))
    }

    /**
     * Define los eventos de clic para la navegación y la adición de alimentos.
     */
    private fun setupListeners() {
        binding.ivEntrenamiento.setOnClickListener {
            startActivity(Intent(this, Entrenamiento::class.java))
            finish()
        }
        binding.ivPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
            finish()
        }
        binding.btnAddBreakfast.setOnClickListener { abrirBuscador("Desayuno") }
        binding.btnAddLunch.setOnClickListener { abrirBuscador("Comida") }
        binding.btnAddDinner.setOnClickListener { abrirBuscador("Cena") }
        binding.btnAddSnack.setOnClickListener { abrirBuscador("Snack") }
    }

    /**
     * Lanza la actividad de búsqueda de alimentos pasando el tipo de comida como parámetro.
     */
    private fun abrirBuscador(tipo: String) {
        val intent = Intent(this, AlimentosActivity::class.java)
        intent.putExtra("TIPO_COMIDA", tipo)
        startActivity(intent)
    }

    /**
     * Configura el detector de gestos para permitir la navegación mediante "swipes" laterales.
     */
    private fun iniciarDetectorGestos() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                if (abs(diffX) > 100 && abs(vX) > 100) {
                    if (diffX > 0) {
                        // Deslizar a la derecha para volver a Entrenamiento.
                        val intent = Intent(this@DietasActivity, Entrenamiento::class.java)
                        val options = ActivityOptionsCompat.makeCustomAnimation(
                            this@DietasActivity, android.R.anim.slide_in_left, android.R.anim.slide_out_right
                        )
                        startActivity(intent, options.toBundle())
                        finish()
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(ev!!) || super.dispatchTouchEvent(ev)
    }
}