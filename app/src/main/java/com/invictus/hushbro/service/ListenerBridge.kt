package com.invictus.hushbro.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class ListenerBridge @Inject constructor() {

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
                    it.packageName != service.packageName
                } ?: 0
            }.getOrDefault(0)
            _activeNotificationCount.value = count
        }
    }

    // `open` (with the class above) so unit tests can subclass and record
    // cancellation requests; production behavior is unchanged.
    open fun cancelNotificationByKey(key: String) {
        activeService?.cancelNotification(key)
    }

    fun getActiveNotifications() = activeService?.activeNotifications ?: emptyArray()
}
