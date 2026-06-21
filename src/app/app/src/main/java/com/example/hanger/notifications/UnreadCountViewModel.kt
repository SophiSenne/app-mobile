package com.example.hanger.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnreadCountViewModel : ViewModel() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var receiver: BroadcastReceiver? = null

    fun register(context: Context) {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val count = intent?.getIntExtra(NotificationPollingService.EXTRA_UNREAD_COUNT, 0) ?: 0
                _unreadCount.value = count
            }
        }
        val filter = IntentFilter(NotificationPollingService.ACTION_UNREAD_COUNT)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun unregister(context: Context) {
        receiver?.let { context.unregisterReceiver(it) }
        receiver = null
    }

    fun markAllRead() {
        _unreadCount.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        receiver = null
    }
}
