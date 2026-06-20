package com.example.trainex.perfil.calendario

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.databinding.FragmentResumenDiaBinding
import com.example.trainex.databinding.ItemSesionResumenBinding
import com.example.trainex.perfil.ejercicios.DetalleHistorialActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Fragmento de tipo BottomSheet que muestra las rutinas y ejercicios realizados en una fecha seleccionada.
 */
class ResumenDiaBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentResumenDiaBinding
    private val viewModel: ResumenDiaViewModel by viewModels()

    private var fecha: LocalDate? = null
    private var userIdExterno: String? = null

    companion object {
        /** Crea una nueva instancia pasando la fecha y el ID de usuario opcional como argumentos */
        fun newInstance(fecha: LocalDate, userId: String?) = ResumenDiaBottomSheet().apply {
            arguments = Bundle().apply {
                putSerializable("FECHA", fecha)
                putString("USER_ID", userId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fecha = arguments?.getSerializable("FECHA") as? LocalDate
        userIdExterno = arguments?.getString("USER_ID")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        binding = FragmentResumenDiaBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** Configura el título con la fecha formateada e inicia la carga de datos en el ViewModel */
        fecha?.let {
            val formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", Locale("es", "ES"))
            binding.tvFechaSeleccionada.text = it.format(formatter)
            viewModel.cargarSesiones(it, userIdExterno)
        }

        observarViewModel()
        binding.btnClose.setOnClickListener { dismiss() }
    }

    /** Suscribe la UI a los flujos de datos de sesiones y eventos del ViewModel */
    private fun observarViewModel() {
        lifecycleScope.launch {
            viewModel.sesiones.collect { lista -> pintarSesiones(lista) }
        }
        lifecycleScope.launch {
            viewModel.eventos.collect { msg -> msg?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
        }
    }

    /**
     * Genera dinámicamente las vistas para cada sesión de entrenamiento encontrada en el día.
     * Incluye la configuración del botón para copiar rutinas si el perfil es externo.
     */
    private fun pintarSesiones(sesiones: List<HistorialSesion>) {
        binding.llContainerRutinas.removeAllViews()
        val inflater = LayoutInflater.from(context)
        val esAjeno = !userIdExterno.isNullOrEmpty()

        sesiones.forEach { sesion ->
            val item = ItemSesionResumenBinding.inflate(inflater, binding.llContainerRutinas, false)
            item.tvNombreRutina.text = sesion.nombreRutina
            item.tvDuracion.text = sesion.tiempoDuracion

            /** Configura la lista interna de ejercicios para la sesión */
            item.rvEjerciciosSesion.layoutManager = LinearLayoutManager(context)
            item.rvEjerciciosSesion.adapter = ResumenEjercicioAdapter(sesion.ejerciciosRealizados) { ej ->
                /** Navega al detalle histórico al pulsar un ejercicio */
                startActivity(Intent(context, DetalleHistorialActivity::class.java).putExtra("EJERCICIO", ej))
            }

            /** El botón de copiar solo es visible en perfiles de otros usuarios */
            item.btnCopiarRutina.visibility = if (esAjeno) View.VISIBLE else View.GONE
            item.btnCopiarRutina.setOnClickListener { viewModel.copiarRutina(sesion) }

            binding.llContainerRutinas.addView(item.root)
        }
    }
}