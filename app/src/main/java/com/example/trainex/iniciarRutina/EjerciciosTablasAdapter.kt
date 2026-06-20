package com.example.trainex.iniciarRutina

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainex.R
import com.example.trainex.databinding.ItemEjercicioTablasBinding
import com.example.trainex.serie.Serie
import com.example.trainex.utils.UnitManager

/**
 * Adaptador para mostrar una lista de ejercicios, cada uno con su propia tabla de series.
 */
class EjerciciosTablasAdapter(
    private var ejerciciosConSeries: MutableList<EjercicioConSeries>
) : RecyclerView.Adapter<EjerciciosTablasAdapter.EjercicioConSeriesViewHolder>() {

    // Enumeración para definir qué tipo de datos recolectar según el ejercicio
    enum class TipoEjercicio {
        PESO,       // Ejercicios de fuerza (Kg y Repeticiones)
        DISTANCIA,  // Ejercicios de cardio con desplazamiento (Km y Tiempo)
        TIEMPO      // Ejercicios estáticos o de tiempo (Solo Tiempo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioConSeriesViewHolder {
        val binding = ItemEjercicioTablasBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EjercicioConSeriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EjercicioConSeriesViewHolder, position: Int) {
        holder.bind(ejerciciosConSeries[position])
    }

    override fun getItemCount(): Int = ejerciciosConSeries.size

    inner class EjercicioConSeriesViewHolder(private val binding: ItemEjercicioTablasBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Contexto de la vista para acceder a recursos y preferencias
        private val context: Context = binding.root.context

        // Recuperación de las unidades configuradas por el usuario (Métrico/Imperial)
        private val unidadPeso = UnitManager.obtener(context, "unidad_peso", "kg")
        private val unidadDistancia = UnitManager.obtener(context, "unidad_distancia", "km")

        // Diccionario de palabras clave para identificar ejercicios de cardio/distancia
        private val keywordsDistancia = listOf(
            "bicicleta", "bike", "caminadora", "treadmill", "cinta",
            "caminata", "walk", "correr", "run", "senderismo", "hike",
            "eliptico", "elliptical", "cycling"
        )

        // Diccionario de palabras clave para identificar ejercicios basados solo en tiempo
        private val keywordsTiempo = listOf(
            "aerobic", "boxeo", "boxing", "cuerda", "rope", "battle",
            "escalada", "climb", "hiit", "inmersion", "salto", "jump",
            "yoga", "pilates", "stretching", "estiramiento"
        )

        /**
         * Vincula los datos del ejercicio a la vista y genera la tabla de series.
         */
        fun bind(ejercicioConSeries: EjercicioConSeries) {
            val ejercicio = ejercicioConSeries.ejercicio
            val nombreEjercicio = getStringFromKey(ejercicio.nombre)

            binding.tvNombreEjercicio.text = nombreEjercicio

            // Carga de imagen con Glide manejando casos de error o imagen no encontrada
            val resourceId = getResourceIdFromName(context, ejercicio.imagen)

            if (resourceId != 0) {
                Glide.with(context)
                    .load(resourceId)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(binding.imgEjercicio)
            } else {
                Glide.with(context)
                    .load(R.drawable.ic_image_not_found)
                    .into(binding.imgEjercicio)
            }

            // Identificar el tipo de ejercicio para ajustar las columnas de la tabla
            val tipoEjercicio = determinarTipo(ejercicio.nombre, nombreEjercicio)
            configurarEncabezados(tipoEjercicio)

            // Limpiar la tabla antes de inflar las filas para evitar duplicados al reciclar vistas
            binding.tlSeriesBody.removeAllViews()

            // Asegurar que siempre exista al menos una fila si la lista está vacía
            if (ejercicioConSeries.series.isEmpty()) {
                agregarSerieAlModelo(ejercicioConSeries)
            }

            // Generar visualmente cada fila de la tabla basándose en el modelo de datos
            ejercicioConSeries.series.forEachIndexed { index, serie ->
                agregarFilaVisual(binding.tlSeriesBody, index + 1, serie, tipoEjercicio)
            }

            // Listener para añadir una nueva serie (fila) dinámicamente
            binding.btnAddSerie.setOnClickListener {
                val nuevaSerie = agregarSerieAlModelo(ejercicioConSeries)
                agregarFilaVisual(binding.tlSeriesBody, ejercicioConSeries.series.size, nuevaSerie, tipoEjercicio)
            }

            // Listener para eliminar la última serie (fila)
            binding.btnRemoveSerie.setOnClickListener {
                if (ejercicioConSeries.series.isNotEmpty()) {
                    ejercicioConSeries.series.removeAt(ejercicioConSeries.series.size - 1)
                    val childCount = binding.tlSeriesBody.childCount
                    if (childCount > 0) {
                        binding.tlSeriesBody.removeViewAt(childCount - 1)
                    }
                }
            }
        }

        /**
         * Determina si el ejercicio es de PESO, DISTANCIA o TIEMPO buscando palabras clave en su nombre.
         */
        private fun determinarTipo(key: String, nombre: String): TipoEjercicio {
            return when {
                keywordsDistancia.any { key.contains(it, true) || nombre.contains(it, true) } -> TipoEjercicio.DISTANCIA
                keywordsTiempo.any { key.contains(it, true) || nombre.contains(it, true) } -> TipoEjercicio.TIEMPO
                else -> TipoEjercicio.PESO
            }
        }

        /**
         * Ajusta el texto de los encabezados de la tabla (ej: cambiar "Kg" por "Km") según el tipo de ejercicio.
         */
        private fun configurarEncabezados(tipo: TipoEjercicio) {
            val headerRow = binding.tlSeriesHeader.getChildAt(0) as? TableRow ?: return
            val tvColumna2 = headerRow.getChildAt(1) as? TextView
            val tvColumna3 = headerRow.getChildAt(2) as? TextView

            when (tipo) {
                TipoEjercicio.DISTANCIA -> {
                    tvColumna2?.text = unidadDistancia.replaceFirstChar { it.uppercase() }
                    tvColumna3?.text = "Tiempo"
                    tvColumna2?.visibility = View.VISIBLE
                }
                TipoEjercicio.PESO -> {
                    tvColumna2?.text = unidadPeso.replaceFirstChar { it.uppercase() }
                    tvColumna3?.text = "Reps"
                    tvColumna2?.visibility = View.VISIBLE
                }
                TipoEjercicio.TIEMPO -> {
                    tvColumna2?.visibility = View.INVISIBLE // Oculta columna de peso/distancia si solo importa el tiempo
                    tvColumna3?.text = "Tiempo"
                }
            }
        }

        /**
         * Crea programáticamente una fila (TableRow) con campos de entrada (EditText) sincronizados con el modelo.
         */
        private fun agregarFilaVisual(tabla: TableLayout, numeroSerie: Int, serieObj: Serie, tipo: TipoEjercicio) {
            val row = TableRow(context)
            val params = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            // Obtiene el color de texto definido en el tema actual (Claro/Oscuro) para los campos dinámicos
            val typedValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.textColor, typedValue, true)
            val colorTextoDinamico = typedValue.data

            // Columna 1: Etiqueta del número de serie
            val tvSerie = TextView(context).apply {
                text = numeroSerie.toString()
                setTextColor(colorTextoDinamico)
                gravity = Gravity.CENTER
                layoutParams = params
            }

            // Columna 2: Entrada para Peso o Distancia
            val etCol2 = EditText(context).apply {
                setTextColor(colorTextoDinamico)
                background = null
                gravity = Gravity.CENTER
                layoutParams = params
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

                // Convertir el valor almacenado (en base métrica) a la unidad visual del usuario
                val unidadActual = if (tipo == TipoEjercicio.PESO) unidadPeso else unidadDistancia
                val valorVisual = UnitManager.aVisual(serieObj.kilogramos, unidadActual)

                if (valorVisual > 0.0) {
                    setText(String.format("%.1f", valorVisual).replace(",", "."))
                } else {
                    setText("")
                }

                hint = if (tipo == TipoEjercicio.PESO) "0" else "0.0"
                setHintTextColor(Color.GRAY)

                if (tipo == TipoEjercicio.TIEMPO) {
                    visibility = View.INVISIBLE
                    isEnabled = false
                }

                // Listener para guardar el valor en el objeto Serie tan pronto como el usuario escribe
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        if (tipo != TipoEjercicio.TIEMPO) {
                            val inputStr = s.toString().replace(",", ".")
                            val valorIntroducido = inputStr.toDoubleOrNull() ?: 0.0
                            // Se guarda siempre en la unidad base de la DB usando el UnitManager
                            serieObj.kilogramos = UnitManager.aBaseDeDatos(valorIntroducido, unidadActual)
                        }
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }

            // Columna 3: Entrada para Repeticiones o Minutos
            val etCol3 = EditText(context).apply {
                setTextColor(colorTextoDinamico)
                background = null
                gravity = Gravity.CENTER
                layoutParams = params
                inputType = InputType.TYPE_CLASS_NUMBER

                when (tipo) {
                    TipoEjercicio.PESO -> { hint = "0" }
                    else -> { hint = "min" }
                }
                setHintTextColor(Color.GRAY)

                setText(if (serieObj.repeticiones > 0) serieObj.repeticiones.toString() else "")

                // Sincronización de repeticiones/tiempo con el modelo Serie
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        serieObj.repeticiones = s.toString().toIntOrNull() ?: 0
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }

            row.addView(tvSerie)
            row.addView(etCol2)
            row.addView(etCol3)
            tabla.addView(row)
        }

        /**
         * Crea un nuevo objeto Serie y lo añade a la lista mutable del ejercicio actual.
         */
        private fun agregarSerieAlModelo(item: EjercicioConSeries): Serie {
            val nuevaSerie = Serie(
                ejercicioId = item.ejercicio.id,
                numeroSerie = item.series.size + 1,
                kilogramos = 0.0,
                repeticiones = 0
            )
            item.series.add(nuevaSerie)
            return nuevaSerie
        }

        /**
         * Intenta traducir el nombre del ejercicio usando el archivo de strings si la clave existe.
         */
        private fun getStringFromKey(key: String): String {
            val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
            return if (resourceId != 0) context.getString(resourceId) else key
        }

        /**
         * Obtiene el ID del recurso drawable basándose en su nombre guardado en la base de datos.
         */
        private fun getResourceIdFromName(context: Context, resourceName: String): Int {
            return context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        }
    }

    /**
     * Devuelve la lista actualizada de ejercicios y series para ser procesada por el ViewModel.
     */
    fun obtenerEjercicios(): List<EjercicioConSeries> {
        return ejerciciosConSeries
    }
}