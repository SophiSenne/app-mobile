package com.example.hanger.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hanger.MainActivity
import com.example.hanger.R
import com.hanger.app.data.model.NotificationDto
import com.hanger.app.data.model.NotificationType

object NotificationHelper {

    private const val CHANNEL_ID = "hanger_notifications"
    private const val CHANNEL_NAME = "Atividade"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Curtidas, comentários e novos seguidores"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showNotification(context: Context, notification: NotificationDto) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_notifications", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val type = NotificationType.from(notification.type)
        val title = buildTitle(type)
        val body = buildBody(notification, type)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context)
                .notify(notification.id.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // permissão POST_NOTIFICATIONS negada pelo usuário
        }
    }

    private fun buildTitle(type: NotificationType) = when (type) {
        NotificationType.LIKE -> "Nova curtida"
        NotificationType.COMMENT -> "Novo comentário"
        NotificationType.FOLLOW -> "Novo seguidor"
        NotificationType.UNKNOWN -> "Nova atividade"
    }

    private fun buildBody(n: NotificationDto, type: NotificationType) = when (type) {
        NotificationType.LIKE -> "@${n.actorUsername} curtiu sua publicação"
        NotificationType.COMMENT -> {
            val preview = n.commentContent?.take(60)?.let { " \"$it\"" } ?: ""
            "@${n.actorUsername} comentou na sua publicação$preview"
        }
        NotificationType.FOLLOW -> "@${n.actorUsername} começou a seguir você"
        NotificationType.UNKNOWN -> "@${n.actorUsername} interagiu com você"
    }
}
