package com.example.trainex.perfil

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat // IMPORTANTE: Añadir esto
import androidx.recyclerview.widget.RecyclerView
import com.example.trainex.R // Asegúrate de importar tu R
import com.example.trainex.databinding.ItemChartPaginaBinding
import com.example.trainex.utils.LanguageUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * Adaptador para ViewPager2 que muestra diferentes tipos de gráficas lineales.
 * Gestiona el estilo visual (colores, fuentes) y los datos de rendimiento/peso.
 * * @property dataSets Lista de pares que contienen el título de la gráfica y su conjunto de datos.
 * @property xLabels Etiquetas para el eje X correspondientes a cada gráfica.
 */
class ChartsAdapter(
    private val dataSets: List<Pair<String, LineDataSet>>,
    private val xLabels: List<List<String>>
) : RecyclerView.Adapter<ChartsAdapter.ChartViewHolder>() {

    class ChartViewHolder(val binding: ItemChartPaginaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        val binding = ItemChartPaginaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChartViewHolder(binding)
    }

    /**
     * Vincula y configura la gráfica específica para la posición actual.
     * Ajusta colores dinámicos basados en el tema (claro/oscuro) y aplica estilos de línea.
     */
    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        val actualPosition = holder.bindingAdapterPosition
        if (actualPosition == RecyclerView.NO_POSITION) return

        val context = holder.itemView.context // Necesitamos el contexto para sacar los colores

        // Usamos R.color.text que será blanco en modo oscuro y negro en modo claro
        val colorTexto = ContextCompat.getColor(context, R.color.text)
        // Usamos el color de fondo de la tarjeta para el agujero del círculo
        val colorFondo = ContextCompat.getColor(context, R.color.backgroundCardView)

        val (title, dataSet) = dataSets[actualPosition]
        val labels = xLabels[actualPosition]

        holder.binding.tvChartTitle.text = title

        // Aplicar color al título también por si acaso (aunque suele heredar del layout)
        holder.binding.tvChartTitle.setTextColor(colorTexto)

        // Configuración Base de la Gráfica
        holder.binding.chartPage.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            legend.isEnabled = false

            setBackgroundColor(Color.TRANSPARENT)

            axisRight.isEnabled = false

            xAxis.apply {
                this.position = XAxis.XAxisPosition.BOTTOM

                textColor = colorTexto

                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < labels.size) labels[index] else ""
                    }
                }
            }

            axisLeft.apply {
                textColor = colorTexto
                setDrawGridLines(true)
            }
        }

        // Estilos específicos (Amarillo o Verde)
        if (actualPosition == 0) {
            dataSet.color = Color.parseColor("#FFCA28")
            dataSet.fillColor = Color.parseColor("#FFCA28")
        } else {
            dataSet.color = Color.parseColor("#00E676")
            dataSet.fillColor = Color.parseColor("#00E676")
        }

        // Configuración del DataSet
        dataSet.apply {
            valueTextColor = colorTexto

            lineWidth = 2.5f
            setDrawCircles(true)
            circleRadius = 5f

            circleHoleColor = colorFondo

            setDrawFilled(true)
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        holder.binding.chartPage.fitScreen()
        holder.binding.chartPage.data = LineData(dataSet)
        holder.binding.chartPage.invalidate()
    }

    override fun getItemCount(): Int = dataSets.size
}