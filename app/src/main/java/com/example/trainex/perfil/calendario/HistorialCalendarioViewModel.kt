package com.example.trainex.perfil.calendario

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.R
import com.example.trainex.firebase.FirebaseHistorialCalendarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.*

/**
 * ViewModel encargado de procesar la lógica del calendario de entrenamientos.
 * Gestiona la carga de sesiones, el cálculo de fechas de inicio y la generación de la lista de ítems para la UI.
 */
@RequiresApi(Build.VERSION_CODES.O)
class HistorialCalendarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseHistorialCalendarioRepository()
    private val context = application.applicationContext

    /** Flujo de estado que contiene la lista de elementos (cabeceras, días, vacíos) del calendario */
    private val _calendarItems = MutableStateFlow<List<CalendarioItem>>(emptyList())
    val calendarItems: StateFlow<List<CalendarioItem>> = _calendarItems

    /** Flujo de estado para controlar el indicador de carga en la interfaz */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Inicializa el calendario cargando los días entrenados y generando la lista de ítems desde la fecha de inicio.
     * @param userIdExterno ID del usuario si se consulta un perfil ajeno; null si es el propio.
     */
    fun inicializar(userIdExterno: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val diasEntrenados = cargarDiasEntrenados(userIdExterno)
                val fechaInicio = calcularFechaInicio(userIdExterno, diasEntrenados)
                _calendarItems.value = generarListaCalendario(fechaInicio, diasEntrenados)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Obtiene las fechas (LocalDate) en las que existen sesiones registradas en Firebase. */
    private suspend fun cargarDiasEntrenados(userId: String?): Set<LocalDate> {
        val sesiones = if (!userId.isNullOrEmpty()) {
            repository.obtenerTodasLasSesionesPorId(userId)
        } else {
            repository.obtenerTodasLasSesiones()
        }
        return sesiones.map {
            Instant.ofEpochMilli(it.fecha).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()
    }

    /**
     * Determina la fecha más antigua que debe mostrar el calendario.
     * Si es el usuario actual, utiliza la fecha de creación de la cuenta o un año atrás por defecto.
     */
    private fun calcularFechaInicio(userId: String?, dias: Set<LocalDate>): LocalDate {
        if (!userId.isNullOrEmpty()) {
            return dias.minOrNull() ?: LocalDate.now()
        }
        val user = FirebaseAuth.getInstance().currentUser
        val creation = user?.metadata?.creationTimestamp
        return if (creation != null && creation > 0) {
            Instant.ofEpochMilli(creation).atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            LocalDate.now().minusYears(1)
        }
    }

    /**
     * Construye la lista de elementos para el RecyclerView, incluyendo cabeceras de mes y espacios vacíos de relleno.
     */
    private fun generarListaCalendario(fechaLimite: LocalDate, diasEntrenados: Set<LocalDate>): List<CalendarioItem> {
        val items = mutableListOf<CalendarioItem>()
        var currentMonth = YearMonth.from(LocalDate.now())
        val endMonth = YearMonth.from(fechaLimite)

        while (!currentMonth.isBefore(endMonth)) {
            val monthName = context.getString(getMonthStringRes(currentMonth.month))
            items.add(CalendarioItem.MonthHeader("$monthName ${currentMonth.year}"))

            /** Espacios vacíos para que el primer día del mes coincida con su día de la semana */
            val firstDay = currentMonth.atDay(1)
            for (i in 1 until firstDay.dayOfWeek.value) {
                items.add(CalendarioItem.Empty)
            }

            for (day in 1..currentMonth.lengthOfMonth()) {
                val date = currentMonth.atDay(day)
                items.add(CalendarioItem.Day(date, diasEntrenados.contains(date)))
            }
            currentMonth = currentMonth.minusMonths(1)
        }
        return items
    }

    /** Mapea el mes de java.time al recurso de string correspondiente en la app */
    private fun getMonthStringRes(month: Month): Int = when (month) {
        Month.JANUARY -> R.string.month_january; Month.FEBRUARY -> R.string.month_february
        Month.MARCH -> R.string.month_march; Month.APRIL -> R.string.month_april
        Month.MAY -> R.string.month_may; Month.JUNE -> R.string.month_june
        Month.JULY -> R.string.month_july; Month.AUGUST -> R.string.month_august
        Month.SEPTEMBER -> R.string.month_september; Month.OCTOBER -> R.string.month_october
        Month.NOVEMBER -> R.string.month_november; Month.DECEMBER -> R.string.month_december
    }
}