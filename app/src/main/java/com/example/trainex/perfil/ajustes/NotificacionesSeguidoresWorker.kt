package com.example.trainex.perfil.ajustes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.trainex.R
import com.example.trainex.firebase.FirebaseUsuarioRepository
import com.example.trainex.perfil.seguidos_seguidores.SeguidosSeguidoresActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Worker encargado de comparar la lista de seguidores remota (Firebase)
 * con una caché local para detectar nuevos seguidores o unfollows.
 */
class NotificacionesSeguidoresWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repositorio = FirebaseUsuarioRepository()
    private val auth = FirebaseAuth.getInstance()
    private val prefs = context.getSharedPreferences("seguidores_cache", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()

        return try {
            // 1. Obtener la lista de seguidores actualizada desde la nube
            val listaRemota = repositorio.obtenerListaSeguidores(userId)
            val mapRemoto = listaRemota.associateBy { it.id } // Mapear por ID para comparación rápida

            // 2. Obtener los IDs que teníamos guardados de la última ejecución
            val idsGuardados = prefs.getStringSet("ids_seguidores", emptySet()) ?: emptySet()

            // 3. Comparación de conjuntos para encontrar diferencias
            val nuevosIds = mapRemoto.keys - idsGuardados // Están en remoto pero no en local
            val perdidosIds = idsGuardados - mapRemoto.keys // Estaban en local pero ya no en remoto

            // 4. Lanzar notificaciones para los nuevos seguidores
            if (nuevosIds.isNotEmpty()) {
                nuevosIds.forEach { id ->
                    val usuario = mapRemoto[id]
                    usuario?.let { lanzarNotificacion(it.username, true) }
                }
            }

            // 5. Lanzar notificación genérica por unfollows
            if (perdidosIds.isNotEmpty()) {
                lanzarNotificacion("Alguien", false)
            }

            // 6. Actualizar la caché local con la nueva realidad de la nube
            prefs.edit().putStringSet("ids_seguidores", mapRemoto.keys).apply()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry() // Reintentar en caso de fallo de red
        }
    }

    /**
     * Crea y muestra la notificación en el sistema.
     */
    private fun lanzarNotificacion(nombreUsuario: String, esNuevo: Boolean) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "canal_seguidores"

        // Crear canal de notificación para Android Oreo o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = applicationContext.getString(R.string.canal_seguidores_nombre_notificacion)
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Configurar el destino al pulsar la notificación: Pestaña de seguidores
        val intent = Intent(applicationContext, SeguidosSeguidoresActivity::class.java).apply {
            putExtra("USER_ID", auth.currentUser?.uid)
            putExtra("START_TAB", 0)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val titulo = applicationContext.getString(R.string.titulo_seguidores_notificacion)
        val mensaje = if (esNuevo) {
            applicationContext.getString(R.string.seguidor_nuevo_notificacion, nombreUsuario)
        } else {
            applicationContext.getString(R.string.seguidor_perdido_notificacion, nombreUsuario)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // ID único basado en tiempo para permitir múltiples notificaciones simultáneas
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}