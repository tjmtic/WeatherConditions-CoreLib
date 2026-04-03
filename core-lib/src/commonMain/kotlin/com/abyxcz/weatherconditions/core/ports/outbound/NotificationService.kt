package com.abyxcz.weatherconditions.core.ports.outbound

interface NotificationService {
    fun showNotification(title: String, message: String)
}
