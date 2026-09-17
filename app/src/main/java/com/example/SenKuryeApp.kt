package com.example

import android.app.Application
import com.example.core.network.ConnectivityObserver
import com.example.core.network.NetworkConnectivityObserver
import com.example.core.notification.CourierNotificationManager
import com.example.core.sync.OfflineSyncEngine
import com.example.core.sync.RoomSyncManager
import com.example.data.database.SenKuryeDatabase
import com.example.data.repository.CourierRepository

class SenKuryeApp : Application() {

    lateinit var database: SenKuryeDatabase
        private set

    lateinit var repository: CourierRepository
        private set

    lateinit var notificationManager: CourierNotificationManager
        private set

    lateinit var syncEngine: OfflineSyncEngine
        private set

    lateinit var connectivityObserver: ConnectivityObserver
        private set

    lateinit var roomSyncManager: RoomSyncManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = SenKuryeDatabase.getInstance(this)
        repository = CourierRepository(database)
        notificationManager = CourierNotificationManager(this)
        connectivityObserver = NetworkConnectivityObserver(this)
        roomSyncManager = RoomSyncManager(database, connectivityObserver)
        roomSyncManager.startAutoSync()
        syncEngine = OfflineSyncEngine(database)
        syncEngine.startSyncDaemon()
    }
}
