package com.example.core.location

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.SenKuryeApp
import com.example.core.notification.CourierNotificationManager
import kotlinx.coroutines.*

class CourierLocationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isTracking = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopTracking()
            try {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping foreground: ${e.message}")
            }
            stopSelf()
            return START_NOT_STICKY
        }

        val started = startForegroundServiceWithNotification()
        if (started) {
            startTracking()
        } else {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startForegroundServiceWithNotification(): Boolean {
        val notification: Notification = NotificationCompat.Builder(this, CourierNotificationManager.CHANNEL_LOCATION)
            .setContentTitle("Şen Kurye Takip Servisi")
            .setContentText("Çevrimiçi • Konum dispatch merkezine iletiliyor")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (hasLocationPermission()) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                } else {
                    Log.w(TAG, "Location permission not granted yet, cannot start FGS location type")
                    return false
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting foreground service: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error starting foreground service: ${e.message}")
            false
        }
    }

    private fun startTracking() {
        if (isTracking) return
        isTracking = true

        serviceScope.launch {
            val app = application as? SenKuryeApp
            val repository = app?.repository

            var currentLat = 41.0410
            var currentLng = 29.0030

            // Realistic continuous location stream towards delivery route
            while (isActive && isTracking) {
                // Micro-shift to simulate real motorcycle GPS movement along route
                currentLat += 0.00015 + (Math.random() - 0.5) * 0.0001
                currentLng += 0.00015 + (Math.random() - 0.5) * 0.0001

                repository?.updateLocation(currentLat, currentLng)

                // Update every 4 seconds for responsive real-time map tracking
                delay(4000)
            }
        }
    }

    private fun stopTracking() {
        isTracking = false
        serviceScope.cancel()
    }

    override fun onDestroy() {
        stopTracking()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "CourierLocationService"
        const val ACTION_START = "ACTION_START_LOCATION_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_LOCATION_TRACKING"
        const val NOTIFICATION_ID = 1001
    }
}

