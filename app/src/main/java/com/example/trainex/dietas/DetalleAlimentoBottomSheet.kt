package com.example.trainex.dietas

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.trainex.R
import com.example.trainex.databinding.FragmentDetalleAlimentoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

/**
 * Fragmento de tipo BottomSheet para mostrar y registrar detalles de un alimento.
 * Permite al usuario ajustar la cantidad y añadir el alimento al diario nutricional.
 */
class DetalleAlimentoBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentDetalleAlimentoBinding? = null
    private val binding get() = _binding!!

    private var alimento: Alimento? = null
    /** Representa la categoría de la comida (ej. Desayuno, Almuerzo). */
    private var tipoComida: String = ""

    /** * ViewModel compartido con la actividad para gestionar la inserción en el diario.
     */
    private val viewModel: DietasViewModel by activityViewModels()

    companion object {
        const val TAG = "DetalleAlimentoBS"

        /**
         * Crea una nueva instancia del BottomSheet con los datos del alimento y el tipo de comida.
         * @param alimento Objeto con la información nutricional.
         * @param tipo Categoría de tiempo de comida asignada.
         * @return Instancia configurada de [DetalleAlimentoBottomSheet].
         */
        fun newInstance(alimento: Alimento, tipo: String): DetalleAlimentoBottomSheet {
            val fragment = DetalleAlimentoBottomSheet()
            val args = Bundle()
            args.putSerializable("alimento", alimento)
            args.putString("tipo", tipo)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            alimento = it.getSerializable("alimento") as? Alimento
            tipoComida = it.getString("tipo") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleAlimentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alimento?.let { item ->
            configurarInterfaz(item)

            binding.btnIngresarComida.setOnClickListener {
                guardarEnDiario()
            }
        }
    }

    /**
     * Vincula los datos del objeto Alimento con los elementos visuales de la interfaz.
     * @param item El alimento cuyos detalles se van a mostrar.
     */
    private fun configurarInterfaz(item: Alimento) {
        binding.tvDetalleNombre.text = item.nombre
        binding.tvDetalleMarca.text = item.marca

        // Traducimos el nombre de la sección para el botón
        val nombreComidaDisplay = obtenerNombreComidaTraducido(tipoComida)
        binding.btnIngresarComida.text = getString(R.string.btn_ingresar_a, nombreComidaDisplay)

        // Determinar unidades según si es líquido o sólido
        val unidad = if (item.esLiquido) "ml" else "g"
        val unidad100 = if (item.esLiquido) "100ml" else "100g"

        // Formateo de valores nutricionales a texto
        binding.tvDetalleCalorias.text = String.format("%.0f", item.calorias.toDouble())
        binding.tvDetalleProteinas.text = String.format("%.1f g", item.proteinas)
        binding.tvDetalleCarbos.text = String.format("%.1f g", item.carbohidratos)
        binding.tvDetalleGrasas.text = String.format("%.1f g", item.grasas)

        binding.tvLabel100g.text = getString(R.string.datos_por_100, unidad100)
        binding.tvUnidadInput.text = unidad

        cargarImagen(item.imagen)
    }

    /**
     * Gestiona la carga de la imagen del alimento, soportando URLs externas y Base64.
     * @param imagenSource Cadena con la URL o el código Base64 de la imagen.
     */
    private fun cargarImagen(imagenSource: String) {
        try {
            if (imagenSource.isNotEmpty()) {
                if (imagenSource.startsWith("http")) {
                    // Carga desde URL con cabecera User-Agent personalizada
                    val urlConHeaders = GlideUrl(
                        imagenSource,
                        LazyHeaders.Builder()
                            .addHeader("User-Agent", "TrainexApp-Android")
                            .build()
                    )
                    Glide.with(this)
                        .load(urlConHeaders)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.ivDetalleImagen)
                } else {
                    // Decodificación de imagen en formato Base64
                    val imageBytes = Base64.decode(imagenSource, Base64.DEFAULT)
                    Glide.with(this)
                        .load(imageBytes)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.ivDetalleImagen)
                }
            } else {
                binding.ivDetalleImagen.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } catch (e: Exception) {
            binding.ivDetalleImagen.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    /**
     * Calcula los nutrientes proporcionales a la cantidad ingresada y crea un registro en el diario.
     * Valida que la cantidad sea mayor a cero antes de proceder.
     */
    private fun guardarEnDiario() {
        val cantidad = binding.etCantidadGram.text.toString().toDoubleOrNull() ?: 0.0

        if (cantidad <= 0) {
            Toast.makeText(context, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
            return
        }

        alimento?.let { item ->
            // Factor de cálculo basado en la base de 100g/ml
            val factor = cantidad / 100.0
            val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val registro = AlimentoDiario(
                nombre = item.nombre,
                marca = item.marca,
                imagen = item.imagen,
                kcalTotales = (item.calorias * factor).toInt(),
                protTotales = item.proteinas * factor,
                carbTotales = item.carbohidratos * factor,
                grasTotales = item.grasas * factor,
                cantidadGramos = cantidad,
                tipoComida = tipoComida,
                fecha = fechaHoy,
                completado = true,
                esLiquido = item.esLiquido
            )

            // Delegamos la persistencia al ViewModel compartido
            viewModel.insertarAlimentoDiario(registro, item)

            val nombreComidaDisplay = obtenerNombreComidaTraducido(tipoComida)
            Toast.makeText(context, getString(R.string.msg_anadido_a, nombreComidaDisplay), Toast.LENGTH_SHORT).show()

            dismiss()
            activity?.finish() // Cierra el flujo de búsqueda
        }
    }

    /**
     * Traduce las claves internas de las secciones de comida a sus equivalentes localizados.
     * @param claveInterna El nombre técnico de la sección (ej. "Desayuno").
     * @return El nombre traducido listo para mostrar en la interfaz.
     */
    private fun obtenerNombreComidaTraducido(claveInterna: String): String {
        return when (claveInterna) {
            "Desayuno" -> getString(R.string.seccion_desayuno)
            "Comida" -> getString(R.string.seccion_comida)
            "Cena" -> getString(R.string.seccion_cena)
            "Snack", "Snacks" -> getString(R.string.seccion_snacks)
            else -> claveInterna
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}