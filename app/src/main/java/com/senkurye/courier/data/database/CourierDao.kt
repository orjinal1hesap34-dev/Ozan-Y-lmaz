package com.senkurye.courier.data.database

import androidx.room.*
import com.senkurye.courier.data.local.entities.CourierEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Courier entity in 'com.senkurye.courier.data.database'.
 * Provides insert, delete, update, and reactive Flow observation methods.
 */
@Dao
interface CourierDao {

    // ==========================================
    // 1. OBSERVING DATA AS FLOW
    // ==========================================

    @Query("SELECT * FROM couriers WHERE id = :id LIMIT 1")
    fun getCourierFlow(id: String): Flow<CourierEntity?>

    @Query("SELECT * FROM couriers ORDER BY name ASC")
    fun getAllCouriersFlow(): Flow<List<CourierEntity>>

    @Query("SELECT * FROM couriers WHERE isOnline = 1")
    fun getOnlineCouriersFlow(): Flow<List<CourierEntity>>

    // ==========================================
    // 2. ONE-SHOT GETTERS
    // ==========================================

    @Query("SELECT * FROM couriers WHERE id = :id LIMIT 1")
    suspend fun getCourierById(id: String): CourierEntity?

    @Query("SELECT * FROM couriers")
    suspend fun getAllCouriers(): List<CourierEntity>

    // ==========================================
    // 3. INSERTION METHODS
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourier(courier: CourierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCourier(courier: CourierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCouriers(couriers: List<CourierEntity>)

    // ==========================================
    // 4. UPDATE METHODS
    // ==========================================

    @Update
    suspend fun updateCourier(courier: CourierEntity)

    @Query("UPDATE couriers SET isOnline = :isOnline, status = :status WHERE id = :id")
    suspend fun updateOnlineStatus(id: String, isOnline: Boolean, status: String)

    @Query("UPDATE couriers SET currentLatitude = :latitude, currentLongitude = :longitude, lastLocationUpdate = :timestamp WHERE id = :id")
    suspend fun updateLocation(id: String, latitude: Double, longitude: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE couriers SET currentPackageCount = :count WHERE id = :id")
    suspend fun updatePackageCount(id: String, count: Int)

    @Query("UPDATE couriers SET physicalCashBalance = physicalCashBalance + :delta WHERE id = :id")
    suspend fun updatePhysicalCashBalance(id: String, delta: Double)

    // ==========================================
    // 5. DELETION METHODS
    // ==========================================

    @Delete
    suspend fun deleteCourier(courier: CourierEntity)

    @Query("DELETE FROM couriers WHERE id = :id")
    suspend fun deleteCourierById(id: String)

    @Query("DELETE FROM couriers")
    suspend fun deleteAllCouriers()
}
