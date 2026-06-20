package com.example.trainex.perfil.calendario

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.trainex.databinding.ActivityHistorialCalendarioBinding
import com.example.trainex.utils.LanguageUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Actividad encargada de mostrar el historial de entrenamientos en un formato de calendario.
 * Soporta tanto el perfil del usuario actual como perfiles externos.
 */
@RequiresApi(Build.VERSION_CODES.O)
class HistorialCalendarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialCalendarioBinding
    private val viewModel: HistorialCalendarioViewModel by viewModels()
    private var userIdExterno: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialCalendarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** Recupera el ID del usuario si se está viendo el calendario de otra persona */
        userIdExterno = intent.getStringExtra("USER_ID")

        setupRecyclerView(emptyList())
        initListeners()
        observarViewModel()

        /** Arranca la lógica de carga del calendario en el ViewModel */
        viewModel.inicializar(userIdExterno)
    }

    /** Suscribe la actividad a los cambios en la lista de ítems del calendario generados por el ViewModel */
    private fun observarViewModel() {
        lifecycleScope.launch {
            viewModel.calendarItems.collect { items ->
                setupRecyclerView(items)
            }
        }
    }

    /** * Configura el RecyclerView con un GridLayout de 7 columnas (una por día de la semana).
     * Incluye una lógica de span para que los nombres de los meses ocupen todo el ancho.
     */
    private fun setupRecyclerView(calendarData: List<CalendarioItem>) {
        val adapter = CalendarioAdapter(calendarData) { fecha ->
            /** Al pulsar un día, se abre un BottomSheet con el resumen de la jornada */
            val bottomSheet = ResumenDiaBottomSheet.newInstance(fecha, userIdExterno)
            bottomSheet.show(supportFragmentManager, "ResumenDia")
        }

        val layoutManager = GridLayoutManager(this, 7)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                /** Si es una cabecera de mes, ocupa las 7 columnas; si es un día, solo 1 */
                return if (adapter.getItemViewType(position) == CalendarioAdapter.TYPE_HEADER) 7 else 1
            }
        }

        binding.rvCalendar.layoutManager = layoutManager
        binding.rvCalendar.adapter = adapter
    }

    /** Configura los eventos de interacción básica de la actividad */
    private fun initListeners() {
        binding.tvBack.setOnClickListener { finish() }
    }
}