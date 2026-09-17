package com.senkurye.courier.data.local.dao

import androidx.room.*
import com.senkurye.courier.data.local.entities.CourierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourierDao {
    @Query("SELECT * FROM couriers WHERE id = :id LIMIT 1")
    fun getCourierFlow(id: String): Flow<CourierEntity?>

    @Query("SELECT * FROM couriers WHERE id = :id LIMIT 1")
    suspend fun getCourierById(id: String): CourierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCourier(courier: CourierEntity)

    @Query("UPDATE couriers SET isOnline = :isOnline, status = :status WHERE id = :id")
    suspend fun updateOnlineStatus(id: String, isOnline: Boolean, status: String)

    @Query("UPDATE couriers SET currentLatitude = :latitude, currentLongitude = :longitude, lastLocationUpdate = :timestamp WHERE id = :id")
    suspend fun updateLocation(id: String, latitude: Double, longitude: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE couriers SET currentPackageCount = :count WHERE id = :id")
    suspend fun updatePackageCount(id: String, count: Int)

    @Query("UPDATE couriers SET physicalCashBalance = physicalCashBalance + :delta WHERE id = :id")
    suspend fun updatePhysicalCashBalance(id: String, delta: Double)
}
