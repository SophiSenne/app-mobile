package com.example.hanger.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.hanger.R
import com.hanger.app.data.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NotificationPollingService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var pollingJob: Job? = null

    companion object {
        private const val FOREGROUND_CHANNEL_ID = "hanger_polling"
        private const val FOREGROUND_NOTIF_ID = 1
        private const val POLL_INTERVAL_MS = 30_000L

        const val EXTRA_USER_ID = "user_id"
        const val ACTION_UNREAD_COUNT = "com.example.hanger.UNREAD_COUNT"
        const val EXTRA_UNREAD_COUNT = "unread_count"

        fun buildIntent(context: Context, userId: String) =
            Intent(context, NotificationPollingService::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        createForegroundChannel()
        startForeground(FOREGROUND_NOTIF_ID, buildForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = intent?.getStringExtra(EXTRA_USER_ID) ?: return START_NOT_STICKY
        restartPolling(userId)
        return START_STICKY
    }

    private fun restartPolling(userId: String) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            val seenIds = mutableSetOf<String>()
            var firstRun = true

            while (isActive) {
                runCatching {
                    val response = RetrofitClient.apiService.getNotifications(userId, limit = 50)
                    if (response.isSuccessful) {
                        val notifications = response.body().orEmpty()

                        if (firstRun) {
                            // Primeira execução: registra IDs existentes sem disparar push
                            seenIds.addAll(notifications.map { it.id })
                            firstRun = false
                            sendUnreadBroadcast(notifications.count { !it.read })
                        } else {
                            val newOnes = notifications.filter { it.id !in seenIds }
                            newOnes.forEach { n ->
                                NotificationHelper.showNotification(this@NotificationPollingService, n)
                                seenIds.add(n.id)
                            }
                            sendUnreadBroadcast(notifications.count { !it.read })
                        }
                    }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun sendUnreadBroadcast(count: Int) {
        val intent = Intent(ACTION_UNREAD_COUNT).apply {
            putExtra(EXTRA_UNREAD_COUNT, count)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        pollingJob?.cancel()
        super.onDestroy()
    }

    private fun createForegroundChannel() {
        val channel = NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            "Sincronização",
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(): Notification =
        NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Hanger")
            .setContentText("Verificando atividade…")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
}
