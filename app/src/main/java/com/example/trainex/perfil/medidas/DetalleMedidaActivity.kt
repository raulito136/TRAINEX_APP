package com.example.trainex.perfil.medidas

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.R
import com.example.trainex.databinding.ActivityDetalleMedidaBinding
import com.example.trainex.utils.LanguageUtils
import com.example.trainex.utils.UnitManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de detalle para una medida específica.
 * Proporciona una visualización gráfica (LineChart) del progreso y un historial detallado en lista.
 * Permite añadir nuevos registros y eliminar existentes.
 */
class DetalleMedidaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleMedidaBinding
    private val viewModel: DetalleMedidaViewModel by viewModels()

    private var nombreMedida: String = ""
    private var userIdExterno: String? = null

    /**
     * Inicializa la actividad, configura el View Binding y procesa los extras del Intent.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleMedidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nombreMedida = intent.getStringExtra("NOMBRE_MEDIDA") ?: ""
        userIdExterno = intent.getStringExtra("USER_ID")
        binding.tvTituloMedida.text = nombreMedida

        if (userIdExterno != null) {
            binding.btnAgregar.visibility = View.GONE
            binding.btnAgregarCentro.visibility = View.GONE
        }

        setupListeners()
        observarViewModel()
        viewModel.cargarDatos(nombreMedida, userIdExterno)
    }

    /**
     * Suscribe la UI a los flujos de datos del ViewModel para reaccionar a cambios de estado.
     */
    private fun observarViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is DetalleMedidaViewModel.DetalleMedidaUiState.Success -> {
                        binding.layoutVacio.visibility = View.GONE
                        binding.groupDatos.visibility = View.VISIBLE
                        actualizarRecyclerView(state.historial)
                        actualizarGrafico(state.entries, state.historial)
                    }
                    is DetalleMedidaViewModel.DetalleMedidaUiState.Empty -> {
                        binding.layoutVacio.visibility = View.VISIBLE
                        binding.groupDatos.visibility = View.GONE
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Configura y actualiza el RecyclerView con el historial de mediciones.
     */
    private fun actualizarRecyclerView(lista: List<RegistroMedida>) {
        val unidad = viewModel.config.value.unidadVisual
        binding.rvHistorial.layoutManager = LinearLayoutManager(this)
        binding.rvHistorial.adapter = HistorialMedidasAdapter(
            lista.sortedByDescending { it.fecha },
            unidad
        ) { medida ->
            if (userIdExterno == null) mostrarDialogoEliminar(medida)
        }
    }

    /**
     * Define los eventos de clic para los botones de navegación y acción.
     */
    private fun setupListeners() {
        binding.tvBack.setOnClickListener { finish() }
        binding.btnAgregar.setOnClickListener { mostrarDialogoInput() }
        binding.btnAgregarCentro.setOnClickListener { mostrarDialogoInput() }
    }

    /**
     * Despliega un diálogo personalizado para registrar un nuevo valor y fecha de medición.
     */
    private fun mostrarDialogoInput() {
        val viewDialog = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_medida, null)
        val dialog = AlertDialog.Builder(this).setView(viewDialog).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val config = viewModel.config.value
        val etValor = viewDialog.findViewById<TextInputEditText>(R.id.etValor)
        val tilValor = viewDialog.findViewById<TextInputLayout>(R.id.tilValor)
        val etFecha = viewDialog.findViewById<TextInputEditText>(R.id.etFecha)
        val btnGuardar = viewDialog.findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = viewDialog.findViewById<Button>(R.id.btnCancelar)

        tilValor.hint = getString(R.string.hint_valor_unidad, config.unidadVisual)

        var fechaMs = System.currentTimeMillis()
        etFecha.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))

        etFecha.setOnClickListener {
            val cal = Calendar.getInstance()

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                fechaMs = cal.timeInMillis
                etFecha.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val valorStr = etValor.text.toString()
            val valorVisualIngresado = valorStr.toDoubleOrNull()

            if (valorVisualIngresado != null && valorVisualIngresado > 0 && valorVisualIngresado <= config.limiteVisual) {
                viewModel.guardarMedida(valorVisualIngresado.toFloat(), fechaMs, nombreMedida)
                dialog.dismiss()
            } else {
                tilValor.error = getString(R.string.valor_invalido)
            }
        }
        dialog.show()
    }

    /**
     * Muestra una confirmación de alerta antes de proceder con la eliminación de un registro.
     */
    private fun mostrarDialogoEliminar(medida: RegistroMedida) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialogo_eliminar_medida_titulo)
            .setPositiveButton(R.string.accion_eliminar) { _, _ ->
                viewModel.eliminarMedida(medida)
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    /**
     * Gestiona la configuración estética y los datos del gráfico de líneas.
     * * @param entries Lista de puntos (índice, valor) para dibujar la línea.
     * @param historial Lista de registros originales para extraer las etiquetas de fecha del eje X.
     */
    private fun actualizarGrafico(entries: List<Entry>, historial: List<RegistroMedida>) {
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val color = if (isDark) Color.CYAN else Color.BLUE

        val listaCronologica = historial.sortedBy { it.fecha }

        val dataSet = LineDataSet(entries, nombreMedida).apply {
            this.color = color
            setCircleColor(color)
            lineWidth = 2f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = color
            fillAlpha = 50
        }

        binding.chartProgreso.apply {
            data = LineData(dataSet)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = if(isDark) Color.WHITE else Color.BLACK
                setDrawGridLines(false)
                granularity = 1f
                isGranularityEnabled = true

                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < listaCronologica.size) {
                            val fecha = Date(listaCronologica[index].fecha)
                            SimpleDateFormat("dd/MM", Locale.getDefault()).format(fecha)
                        } else {
                            ""
                        }
                    }
                }
            }

            axisLeft.textColor = if(isDark) Color.WHITE else Color.BLACK
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false

            notifyDataSetChanged()
            invalidate()
        }
    }
}