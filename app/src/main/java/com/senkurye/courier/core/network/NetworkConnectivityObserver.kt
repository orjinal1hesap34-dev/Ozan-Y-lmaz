package com.senkurye.courier.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Concrete Android implementation of ConnectivityObserver utilizing ConnectivityManager.
 * Monitors network capability changes and emits Status via Kotlin callbackFlow.
 * Also supports simulated offline toggle for safe testing and UI demonstration.
 */
class NetworkConnectivityObserver(
    private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _simulatedStatus = MutableStateFlow<ConnectivityObserver.Status?>(null)

    fun setSimulatedOffline(isOffline: Boolean) {
        _simulatedStatus.value = if (isOffline) ConnectivityObserver.Status.UNAVAILABLE else ConnectivityObserver.Status.AVAILABLE
    }

    fun clearSimulation() {
        _simulatedStatus.value = null
    }

    override val isConnected: Boolean
        get() {
            val simulated = _simulatedStatus.value
            if (simulated != null) return simulated.isAvailable
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    override fun observe(): Flow<ConnectivityObserver.Status> {
        val realFlow = callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(ConnectivityObserver.Status.AVAILABLE) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch { send(ConnectivityObserver.Status.LOSING) }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(ConnectivityObserver.Status.LOST) }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch { send(ConnectivityObserver.Status.UNAVAILABLE) }
                }
            }

            val initialStatus = if (isConnected) {
                ConnectivityObserver.Status.AVAILABLE
            } else {
                ConnectivityObserver.Status.UNAVAILABLE
            }
            send(initialStatus)

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            try {
                connectivityManager.registerNetworkCallback(request, callback)
            } catch (e: Exception) {
                try {
                    connectivityManager.registerDefaultNetworkCallback(callback)
                } catch (ignored: Exception) {}
            }

            awaitClose {
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                } catch (e: Exception) {
                    // Ignore if already unregistered
                }
            }
        }

        return combine(realFlow, _simulatedStatus) { real, simulated ->
            simulated ?: real
        }.distinctUntilChanged()
    }
}
