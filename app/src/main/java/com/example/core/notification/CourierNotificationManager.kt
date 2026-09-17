package com.example.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class CourierNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Yeni Görev ve Atamalar",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Backend tarafından otomatik atanan yeni paket bildirimleri"
                enableVibration(true)
            }

            val routeChannel = NotificationChannel(
                CHANNEL_ROUTE,
                "Rota Güncellemeleri",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Optimizasyon ve rota güncelleme bildirimleri"
            }

            val serviceChannel = NotificationChannel(
                CHANNEL_LOCATION,
                "Konum ve Takip Servisi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Aktif mesai sırasında arka plan konum takibi"
            }

            notificationManager.createNotificationChannel(taskChannel)
            notificationManager.createNotificationChannel(routeChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun showNewAssignmentNotification(orderCode: String, customerName: String, amount: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TASKS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Yeni Görev Atandı: $orderCode")
            .setContentText("$customerName • ₺$amount • Doğrudan rotanıza eklendi")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showRouteUpdatedNotification(distanceKm: Double, durationMin: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ROUTE)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("Rotanız Güncellendi")
            .setContentText("Yeni optimize rota: $distanceKm km • Tahmini $durationMin dk")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(202, notification)
    }

    companion object {
        const val CHANNEL_TASKS = "channel_courier_tasks"
        const val CHANNEL_ROUTE = "channel_courier_route"
        const val CHANNEL_LOCATION = "channel_courier_location"
    }
}
