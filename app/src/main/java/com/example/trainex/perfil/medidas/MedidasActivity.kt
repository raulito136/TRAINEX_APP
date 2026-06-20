package com.example.trainex.perfil.medidas

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.databinding.ActivityMedidasBinding
import com.example.trainex.utils.LanguageUtils
import kotlinx.coroutines.launch

/**
 * Actividad principal del módulo de medidas.
 * Muestra un listado de diferentes tipos de medidas corporales (peso, grasa, perímetros)
 * que el usuario puede seleccionar para ver su progreso detallado.
 */
class MedidasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedidasBinding
    private val viewModel: MedidasViewModel by viewModels()
    private var userIdExterno: String? = null

    /**
     * Configura la vista, el idioma y procesa posibles parámetros de entrada.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMedidasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera el ID de usuario si se está visualizando el perfil de otra persona
        userIdExterno = intent.getStringExtra("USER_ID")

        setupListeners()
        observarViewModel()
    }

    /**
     * Observa el flujo de datos de los tipos de medida y actualiza el RecyclerView.
     * Al hacer clic en un elemento, navega hacia [DetalleMedidaActivity].
     */
    private fun observarViewModel() {
        lifecycleScope.launch {
            viewModel.tiposMedida.collect { lista ->
                binding.rvMedidas.layoutManager = LinearLayoutManager(this@MedidasActivity)
                binding.rvMedidas.adapter = MedidasAdapter(lista) { medida ->
                    val intent = Intent(this@MedidasActivity, DetalleMedidaActivity::class.java)
                    intent.putExtra("NOMBRE_MEDIDA", getString(medida.nombreResId))
                    userIdExterno?.let { intent.putExtra("USER_ID", it) }
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Configura los escuchadores de eventos para los elementos de la interfaz.
     */
    private fun setupListeners() {
        binding.tvBack.setOnClickListener { finish() }
    }
}