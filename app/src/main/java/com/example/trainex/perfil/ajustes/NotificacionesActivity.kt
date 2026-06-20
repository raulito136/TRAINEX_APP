package com.example.trainex.perfil.ajustes

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.trainex.R
import com.example.trainex.databinding.ActivityNotificacionesBinding
import com.example.trainex.utils.LanguageUtils

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificacionesBinding
    private val viewModel: NotificacionesViewModel by viewModels()

    // Ya no están hardcodeados, se inicializan en onCreate
    private lateinit var diasSemana: Array<String>
    private lateinit var inicialesDias: Array<String>
    private val diasSeleccionados = BooleanArray(7)

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) { /* Lógica opcional */ }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar idioma antes de inflar la vista
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityNotificacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializamos los arrays desde los recursos XML
        diasSemana = resources.getStringArray(R.array.dias_semana)
        inicialesDias = resources.getStringArray(R.array.dias_semana_short)

        setupObservers()
        setupSwitchListeners()

        binding.tvBack.setOnClickListener { finish() }
        pedirPermisoNotificaciones()

        binding.layoutConfigEntreno.setOnClickListener {
            mostrarDialogoDias()
        }
    }

    private fun setupObservers() {
        viewModel.configEntreno.observe(this) { (resumen, hora, minuto) ->
            val textoHora = String.format("%02d:%02d", hora, minuto)

            binding.tvResumenEntreno.text = if (resumen.isEmpty()) {
                getString(R.string.toca_para_configurar)
            } else {
                // Usamos una string con formato: "%1$s a las %2$s"
                getString(R.string.formato_resumen_entreno, resumen, textoHora)
            }
        }

        viewModel.switchesEstado.observe(this) { estados ->
            val generalActivo = estados["general"] ?: true
            binding.switchNotificacionesGenerales.isChecked = generalActivo
            binding.switchSeguidores.isChecked = estados["seguidores"] ?: true
            binding.switchEntrenamiento.isChecked = estados["entreno"] ?: false
            binding.switchFotos.isChecked = estados["fotos"] ?: false

            binding.switchSeguidores.isEnabled = generalActivo
            binding.switchEntrenamiento.isEnabled = generalActivo
            binding.switchFotos.isEnabled = generalActivo
            binding.layoutConfigEntreno.isEnabled = generalActivo
        }
    }

    private fun setupSwitchListeners() {
        binding.switchNotificacionesGenerales.setOnCheckedChangeListener { _, isChecked ->
            viewModel.guardarEstadoSwitch("general", isChecked)
        }
        binding.switchSeguidores.setOnCheckedChangeListener { _, isChecked ->
            viewModel.guardarEstadoSwitch("seguidores", isChecked)
        }
        binding.switchEntrenamiento.setOnCheckedChangeListener { _, isChecked ->
            viewModel.guardarEstadoSwitch("entreno", isChecked)
        }
        binding.switchFotos.setOnCheckedChangeListener { _, isChecked ->
            viewModel.guardarEstadoSwitch("fotos", isChecked)
        }
    }

    private fun mostrarDialogoDias() {
        val current = viewModel.configEntreno.value ?: Triple("", 10, 0)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.seleccionar_dias))
            // Usamos el array cargado dinámicamente
            .setMultiChoiceItems(diasSemana, diasSeleccionados) { _, which, isChecked ->
                diasSeleccionados[which] = isChecked
            }
            .setPositiveButton(getString(R.string.aceptar)) { _, _ ->
                val nuevoResumen = generarResumenDias()
                mostrarTimePicker(nuevoResumen, current.second, current.third)
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun mostrarTimePicker(resumen: String, h: Int, m: Int) {
        TimePickerDialog(this, { _, hora, min ->
            viewModel.guardarConfiguracionEntreno(resumen, hora, min)
        }, h, m, true).show()
    }

    private fun generarResumenDias(): String {
        val sb = StringBuilder()
        for (i in diasSeleccionados.indices) {
            if (diasSeleccionados[i]) {
                if (sb.isNotEmpty()) sb.append(", ")
                // Usamos las iniciales del array multiidioma
                sb.append(inicialesDias[i])
            }
        }
        return sb.toString()
    }

    private fun pedirPermisoNotificaciones() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}