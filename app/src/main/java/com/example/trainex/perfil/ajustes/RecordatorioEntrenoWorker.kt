package com.example.trainex.perfil.ajustes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.trainex.R
import java.util.*

/**
 * Worker encargado de gestionar los recordatorios diarios de entrenamiento.
 * Se ejecuta en segundo plano para comprobar si el usuario debe entrenar hoy.
 */
class RecordatorioEntrenoWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // 1. Obtener la configuración guardada del entrenamiento desde SharedPreferences
        val prefs = applicationContext.getSharedPreferences("trainex_prefs", Context.MODE_PRIVATE)
        val resumen = prefs.getString("entreno_resumen", "") ?: ""
        val hora = prefs.getInt("entreno_hora", 10)
        val minuto = prefs.getInt("entreno_minuto", 0)

        // 2. Determinar el día de la semana actual en formato de iniciales
        val hoy = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "L"
            Calendar.TUESDAY -> "M"
            Calendar.WEDNESDAY -> "X"
            Calendar.THURSDAY -> "J"
            Calendar.FRIDAY -> "V"
            Calendar.SATURDAY -> "S"
            Calendar.SUNDAY -> "D"
            else -> ""
        }

        // 3. Lógica de disparo: se envía la notificación si el día de hoy está en el resumen (ej: "L, X")
        // o si el resumen está vacío (asumiendo recordatorio diario por defecto)
        if (resumen.contains(hoy) || resumen.isEmpty()) {
            enviarNotificacionFisica()
        }

        // 4. RECURSIVIDAD: Programar la ejecución automática para el día siguiente a la misma hora
        programarSiguienteEjecucion(hora, minuto)

        return Result.success()
    }

    /**
     * Calcula el tiempo restante hasta mañana a la hora configurada y encola un nuevo trabajo.
     */
    private fun programarSiguienteEjecucion(hora: Int, minuto: Int) {
        val calendarManana = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1) // Avanzar 24 horas
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
        }

        val delay = calendarManana.timeInMillis - System.currentTimeMillis()

        // Crear una petición de trabajo único con el retraso calculado
        val nextRequest = androidx.work.OneTimeWorkRequestBuilder<RecordatorioEntrenoWorker>()
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag("WORK_ENTRENO")
            .build()

        // Reemplazar cualquier trabajo existente para evitar duplicados
        androidx.work.WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "EntrenoWork",
            androidx.work.ExistingWorkPolicy.REPLACE,
            nextRequest
        )
    }

    /**
     * Crea y muestra la notificación visual en el sistema.
     */
    private fun enviarNotificacionFisica() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "entrenamiento_channel"

        // Configuración del canal para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Entreno",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val channelName = applicationContext.getString(R.string.canal_entreno_nombre) // Nuevo string para el nombre del canal

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.notif_entreno_titulo))
            .setContentText(applicationContext.getString(R.string.notif_entreno_mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification)
    }
}