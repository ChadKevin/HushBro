package com.invictus.hushbro.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListenerBridge @Inject constructor() {

    private val _isListenerConnected = MutableStateFlow(false)
    val isListenerConnected: StateFlow<Boolean> = _isListenerConnected.asStateFlow()

    private val _activeNotificationCount = MutableStateFlow(0)
    val activeNotificationCount: StateFlow<Int> = _activeNotificationCount.asStateFlow()

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

    fun updateActiveCount() {
        val service = activeService
        if (service != null) {
            val count = runCatching {
                service.activeNotifications?.count {
                    it.packageName != service.packageName && !it.isOngoing
                } ?: 0
            }.getOrDefault(0)
            _activeNotificationCount.value = count
        }
    }

    fun cancelNotificationByKey(key: String) {
        activeService?.cancelNotification(key)
    }

    fun getActiveNotifications() = activeService?.activeNotifications ?: emptyArray()
}
