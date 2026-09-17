package com.example.core.sync

import com.example.data.database.SenKuryeDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class OfflineSyncEngine(
    private val database: SenKuryeDatabase
) {
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val syncQueueDao = database.syncQueueDao()

    fun startSyncDaemon() {
        syncScope.launch {
            // Continuously observe pending queue items and dispatch them
            syncQueueDao.getPendingQueueFlow().collectLatest { pendingList ->
                if (pendingList.isNotEmpty()) {
                    processBatch(pendingList)
                }
            }
        }
    }

    private suspend fun processBatch(items: List<com.example.data.database.entities.SyncQueueEntity>) {
        items.forEach { item ->
            try {
                // Simulate network post to backend with idempotency key
                delay(300)
                // Successfully synced
                syncQueueDao.updateStatus(item.id, "SYNCED", item.retryCount)
            } catch (e: Exception) {
                val nextRetry = item.retryCount + 1
                val newStatus = if (nextRetry >= 5) "FAILED" else "RETRY"
                syncQueueDao.updateStatus(item.id, newStatus, nextRetry)
            }
        }
    }

    suspend fun triggerManualSync(): Int {
        val list = syncQueueDao.getPendingQueueList()
        processBatch(list)
        return list.size
    }
}
