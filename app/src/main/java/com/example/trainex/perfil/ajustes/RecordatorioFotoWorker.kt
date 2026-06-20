package com.example.trainex.perfil.ajustes // Ajusta al paquete correcto

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.trainex.R
import com.example.trainex.firebase.FirebaseFotosRepository
import com.example.trainex.perfil.ajustes.NotificacionesActivity
import java.util.concurrent.TimeUnit

/**
 * Worker que utiliza Corrutinas para verificar en Firebase si han pasado
 * 7 días desde la última foto de progreso subida por el usuario.
 */
class RecordatorioFotoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // Instancia del repositorio
    private val repository = FirebaseFotosRepository(context)

    override suspend fun doWork(): Result {
        return try {
            // 1. Obtener la lista de fotos desde Firebase
            val fotos = repository.obtenerFotosProgreso()

            // 2. Calcular la fecha de la última foto
            val ultimaFecha = if (fotos.isNotEmpty()) fotos[0].fecha else 0L

            // Si nunca ha subido foto, asumimos que "hace mucho" (0L) para que notifique ya,
            // o podrías poner System.currentTimeMillis() si quieres dar 7 días de margen desde hoy.

            val sieteDiasMillis = 7 * 24 * 60 * 60 * 1000L
            val fechaObjetivo = ultimaFecha + sieteDiasMillis
            val ahora = System.currentTimeMillis()
            val tiempoRestante = fechaObjetivo - ahora

            if (tiempoRestante <= 0) {
                // A) YA PASARON 7 DÍAS: Lanzar notificación
                lanzarNotificacion()

                // REPROGRAMAR PARA MAÑANA:
                // Si el usuario no sube la foto hoy, le volvemos a avisar en 24 horas
                programarSiguienteChequeo(24 * 60 * 60 * 1000L)

                Result.success()
            } else {
                // B) AÚN NO ES HORA: Reprogramar para el momento exacto en que se cumplan los 7 días
                programarSiguienteChequeo(tiempoRestante)
                Result.success()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry() // Si falla Firebase (sin red), reintenta luego
        }
    }

    /**
     * Reprograma el Worker con un delay específico.
     */
    private fun programarSiguienteChequeo(delayMillis: Long) {
        val workRequest = OneTimeWorkRequestBuilder<RecordatorioFotoWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag("WORK_FOTO_SEMANAL")
            .build()

        // Usamos REPLACE para asegurar que solo haya UN temporizador activo
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "TRABAJO_FOTO_SEMANAL",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Lanza la notificación invitando al usuario a registrar su progreso.
     */
    private fun lanzarNotificacion() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "canal_recordatorio_foto"

        // Crear canal (Necesario para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Progreso",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la app al tocar la notificación
        val intent = Intent(applicationContext, NotificacionesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

// Dentro de lanzarNotificacion()
        val channelName = applicationContext.getString(R.string.canal_foto_nombre) // Nuevo string para el nombre del canal

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.notif_foto_titulo))
            .setContentText(applicationContext.getString(R.string.notif_foto_mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
