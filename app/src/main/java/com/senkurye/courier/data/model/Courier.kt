package com.senkurye.courier.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a Courier in 'com.senkurye.courier.data.model'.
 * Encapsulates the courier's real-time operational status, location, capacity, and balance.
 */
@Entity(tableName = "couriers")
data class Courier(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "surname")
    val surname: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "status")
    val status: String, // OFFLINE, ONLINE, AVAILABLE, GOING_TO_RESTAURANT, AT_RESTAURANT, etc.

    @ColumnInfo(name = "isOnline")
    val isOnline: Boolean = false,

    @ColumnInfo(name = "currentLatitude")
    val currentLatitude: Double = 0.0,

    @ColumnInfo(name = "currentLongitude")
    val currentLongitude: Double = 0.0,

    @ColumnInfo(name = "maxPackageLimit")
    val maxPackageLimit: Int = 5,

    @ColumnInfo(name = "currentPackageCount")
    val currentPackageCount: Int = 0,

    @ColumnInfo(name = "vehicleType")
    val vehicleType: String = "Motosiklet",

    @ColumnInfo(name = "plateNumber")
    val plateNumber: String = "",

    @ColumnInfo(name = "physicalCashBalance")
    val physicalCashBalance: Double = 0.0,

    @ColumnInfo(name = "lastLocationUpdate")
    val lastLocationUpdate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true
)

typealias CourierEntity = Courier
