package com.example.trainex.perfil.ajustes

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainex.R
import com.example.trainex.databinding.ActivityUnidadesBinding
import com.example.trainex.utils.LanguageUtils

/**
 * Pantalla para configurar las unidades de medida preferidas por el usuario
 * (Peso, Distancia y Longitud).
 */
class UnidadesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnidadesBinding
    private val viewModel: UnidadesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityUnidadesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()      // Ajuste de insets para barras de sistema
        setupClickListeners()  // Gestión de eventos táctiles
        setupObservers()       // Observación de cambios en las preferencias
    }

    /**
     * Se suscribe a los cambios del ViewModel para actualizar el aspecto de los botones.
     */
    private fun setupObservers() {
        viewModel.peso.observe(this) { actualizarBotonesPeso(it) }
        viewModel.distancia.observe(this) { actualizarBotonesDistancia(it) }
        viewModel.medida.observe(this) { actualizarBotonesMedidas(it) }
    }

    /**
     * Configura los clics para cambiar entre unidades.
     */
    private fun setupClickListeners() {
        binding.tvBack.setOnClickListener { finish() }

        // Configuración de Peso (kg vs lbs)
        binding.btnKg.setOnClickListener { viewModel.guardarUnidad("unidad_peso", "kg") }
        binding.btnLbs.setOnClickListener { viewModel.guardarUnidad("unidad_peso", "lbs") }

        // Configuración de Distancia (km vs miles)
        binding.btnKm.setOnClickListener { viewModel.guardarUnidad("unidad_distancia", "km") }
        binding.btnMillas.setOnClickListener { viewModel.guardarUnidad("unidad_distancia", "miles") }

        // Configuración de Medidas/Altura (cm vs in)
        binding.btnCm.setOnClickListener { viewModel.guardarUnidad("unidad_longitud", "cm") }
        binding.btnIn.setOnClickListener { viewModel.guardarUnidad("unidad_longitud", "in") }
    }

    /**
     * Actualiza el estilo visual de los botones de peso (Kg/Lbs).
     */
    private fun actualizarBotonesPeso(seleccion: String) {
        val isKg = seleccion == "kg"
        binding.btnKg.setBackgroundResource(if (isKg) R.drawable.bg_button_selected else R.drawable.bg_button_unselected)
        binding.btnKg.setTextColor(if (isKg) Color.WHITE else resources.getColor(R.color.text, theme))

        binding.btnLbs.setBackgroundResource(if (!isKg) R.drawable.bg_button_selected else R.drawable.bg_button_unselected)
        binding.btnLbs.setTextColor(if (!isKg) Color.WHITE else resources.getColor(R.color.text, theme))
    }

    /**
     * Actualiza el estilo visual de los botones de distancia (Km/Millas).
     */
    private fun actualizarBotonesDistancia(seleccion: String) {
        val isKm = seleccion == "km"
        binding.btnKm.setBackgroundResource(if (isKm) R.drawable.bg_button_selected else R.drawable.bg_button_unselected)
        binding.btnKm.setTextColor(if (isKm) Color.WHITE else resources.getColor(R.color.text, theme))

        binding.btnMillas.setBackgroundResource(if (!isKm) R.drawable.bg_button_selected else R.drawable.bg_button_unselected)
        binding.btnMillas.setTextColor(if (!isKm) Color.WHITE else resources.getColor(R.color.text, theme))
    }

    /**
     * Actualiza el estilo visual de los botones de medida corporal (Cm/In).
     */
    private fun actualizarBotonesMedidas(seleccion: String) {
        val isCm = seleccion == "cm"
        binding.btnCm.setBackgroundResource(if (isCm) R.drawable.bg_button_selected else R.drawable.bg_button_unselected)
        binding.btnCm.setTextColor(if (isCm) Color.WHITE else resources.getColor(R.color.text, theme))

        binding.btnIn.setBackgroundResource(if (!isCm) R.drawable.bg_button_selected else R.drawable.bg_button_unselected)
        binding.btnIn.setTextColor(if (!isCm) Color.WHITE else resources.getColor(R.color.text, theme))
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}