package com.senkurye.courier.domain.repository

import com.senkurye.courier.domain.model.Courier
import com.senkurye.courier.domain.model.CourierStatus
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for Courier domain data.
 * Abstracts local database and remote synchronization from presentation/domain layers.
 */
interface CourierRepository {
    fun getCourierFlow(id: String): Flow<Courier?>
    suspend fun getCourierById(id: String): Courier?
    suspend fun saveCourier(courier: Courier)
    suspend fun updateOnlineStatus(id: String, isOnline: Boolean, status: CourierStatus)
    suspend fun updateLocation(id: String, latitude: Double, longitude: Double)
    suspend fun updatePackageCount(id: String, count: Int)
    suspend fun updatePhysicalCashBalance(id: String, delta: Double)
}
