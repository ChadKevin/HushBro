package com.invictus.hushbro.service

import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ListenerTestState {
    IDLE,
    WAITING,
    PASSED,
    FAILED
}

@Singleton
open class ListenerBridge @Inject constructor() {

    private val _isListenerConnected = MutableStateFlow(false)
    val isListenerConnected: StateFlow<Boolean> = _isListenerConnected.asStateFlow()

    private val _activeNotificationCount = MutableStateFlow(0)
    val activeNotificationCount: StateFlow<Int> = _activeNotificationCount.asStateFlow()

    private val _listenerTestState = MutableStateFlow(ListenerTestState.IDLE)
    val listenerTestState: StateFlow<ListenerTestState> = _listenerTestState.asStateFlow()

    private var activeService: HushBroListenerService? = null

    fun attachService(service: HushBroListenerService) {
        activeService = service
        _isListenerConnected.value = true
        updateActiveCount()
    }

    fun detachService() {
        activeService = null
        _isListenerConnected.value = false
        _activeNotificationCount.value = 0
    }

    fun beginListenerTest() {
        _listenerTestState.value = ListenerTestState.WAITING
    }

    fun markListenerTestPassed() {
        _listenerTestState.value = ListenerTestState.PASSED
    }

    fun markListenerTestFailed() {
        if (_listenerTestState.value == ListenerTestState.WAITING) {
            _listenerTestState.value = ListenerTestState.FAILED
        }
    }

    fun updateActiveCount() {
        val service = activeService
        if (service != null) {
            val count = runCatching {
                service.activeNotifications?.count {
                    it.packageName != service.packageName
                } ?: 0
            }.getOrDefault(0)
            _activeNotificationCount.value = count
        }
    }

    // Kept for non-listener pipeline callers and unit tests.
    open fun cancelNotificationByKey(key: String) {
        activeService?.cancelNotification(key)
    }

    fun getActiveNotifications(): Array<StatusBarNotification> = runCatching {
        activeService?.activeNotifications ?: emptyArray()
    }.getOrDefault(emptyArray())
}
