package com.senkurye.courier.data.repository

import com.senkurye.courier.data.local.dao.CourierDao
import com.senkurye.courier.data.mapper.toDomain
import com.senkurye.courier.data.mapper.toEntity
import com.senkurye.courier.data.remote.CourierApiService
import com.senkurye.courier.data.remote.dto.CourierStatusUpdateRequest
import com.senkurye.courier.data.remote.dto.LocationReportRequest
import com.senkurye.courier.domain.model.Courier
import com.senkurye.courier.domain.model.CourierStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository class acting as the single source of truth for Courier data.
 * Abstracts Room database and Retrofit network calls for the rest of the application.
 * Employs offline-first persistence: state is always saved to Room DB first,
 * followed by asynchronous Retrofit synchronization with graceful fallback.
 */
open class CourierRepository(
    private val courierDao: CourierDao,
    private val apiService: CourierApiService? = null
) : com.senkurye.courier.domain.repository.CourierRepository {

    override fun getCourierFlow(id: String): Flow<Courier?> {
        return courierDao.getCourierFlow(id).map { it?.toDomain() }
    }

    override suspend fun getCourierById(id: String): Courier? {
        return courierDao.getCourierById(id)?.toDomain()
    }

    override suspend fun saveCourier(courier: Courier) {
        courierDao.insertOrUpdateCourier(courier.toEntity())
    }

    override suspend fun updateOnlineStatus(id: String, isOnline: Boolean, status: CourierStatus) {
        // 1. Local single source of truth update
        courierDao.updateOnlineStatus(id, isOnline, status.name)

        // 2. Remote network sync via Retrofit (graceful offline handling)
        if (apiService != null) {
            try {
                val request = CourierStatusUpdateRequest(
                    status = status.name,
                    isOnline = isOnline,
                    reason = "Kurye durum değişikliği"
                )
                apiService.updateCourierStatus(id, request)
            } catch (e: Exception) {
                // Offline fallback: status remains in Room local DB
            }
        }
    }

    override suspend fun updateLocation(id: String, latitude: Double, longitude: Double) {
        val now = System.currentTimeMillis()
        // 1. Persist telemetry to local DB
        courierDao.updateLocation(id, latitude, longitude, now)

        // 2. Report telemetry to backend dispatch engine
        if (apiService != null) {
            try {
                val request = LocationReportRequest(
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = now
                )
                apiService.reportLocation(id, request)
            } catch (e: Exception) {
                // Offline fallback: location cached locally
            }
        }
    }

    override suspend fun updatePackageCount(id: String, count: Int) {
        courierDao.updatePackageCount(id, count)
    }

    override suspend fun updatePhysicalCashBalance(id: String, delta: Double) {
        courierDao.updatePhysicalCashBalance(id, delta)
    }
}
