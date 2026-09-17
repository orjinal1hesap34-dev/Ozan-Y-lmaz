package com.senkurye.courier.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing the active Courier profile and telemetry state.
 * Supports offline-first persistence for courier session and tracking.
 */
@Entity(tableName = "couriers")
data class CourierEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val surname: String,
    val phone: String,
    val status: String, // OFFLINE, ONLINE, AVAILABLE, GOING_TO_RESTAURANT, AT_RESTAURANT, GOING_TO_CUSTOMER, DELIVERED, PAUSED
    val isOnline: Boolean,
    val currentLatitude: Double,
    val currentLongitude: Double,
    val maxPackageLimit: Int = 5,
    val currentPackageCount: Int = 0,
    val vehicleType: String = "Motosiklet",
    val plateNumber: String = "",
    val physicalCashBalance: Double = 0.0,
    val lastLocationUpdate: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)
