package com.senkurye.courier.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Interface to observe network connectivity status in real-time.
 * Essential for Room offline-first sync and UI offline indicator badge.
 */
interface ConnectivityObserver {
    fun observe(): Flow<Status>
    val isConnected: Boolean

    enum class Status(val isAvailable: Boolean) {
        AVAILABLE(true),
        UNAVAILABLE(false),
        LOSING(false),
        LOST(false)
    }
}
