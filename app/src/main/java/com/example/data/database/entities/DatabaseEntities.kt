package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courier_profile")
data class CourierProfileEntity(
    @PrimaryKey val id: String = "123",
    val name: String,
    val surname: String,
    val phone: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val packageLimit: Int,
    val currentPackageCount: Int,
    val isOnline: Boolean,
    val vehicleType: String,
    val plateNumber: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val externalOrderId: String,
    val restaurantId: String,
    val restaurantName: String,
    val restaurantAddress: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val latitude: Double,
    val longitude: Double,
    val amount: Double,
    val paymentMethod: String,
    val status: String,
    val packageCount: Int,
    val notes: String,
    val itemsSummary: String,
    val createdAt: Long
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: String,
    val courierId: String,
    val totalDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val version: Int,
    val updatedAt: Long
)

@Entity(tableName = "route_stops")
data class RouteStopEntity(
    @PrimaryKey val id: String,
    val routeId: String,
    val stopOrder: Int,
    val type: String, // PICKUP or DROPOFF
    val orderId: String,
    val packageCode: String,
    val locationName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isCompleted: Boolean,
    val estimatedArrivalMinutes: Int,
    val contactPhone: String
)

@Entity(tableName = "payment_changes")
data class PaymentChangeEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val oldPaymentMethod: String,
    val newPaymentMethod: String,
    val courierId: String,
    val changedAt: Long,
    val changeReason: String,
    val changedBy: String
)

@Entity(tableName = "cash_transactions")
data class CashTransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val amount: Double,
    val restaurantId: String?,
    val restaurantName: String?,
    val orderId: String?,
    val timestamp: Long,
    val transactionCode: String
)

@Entity(tableName = "partner_restaurants")
data class PartnerRestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val pendingBalance: Double,
    val isActive: Boolean
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val type: String,
    val payload: String,
    val createdAt: Long,
    val retryCount: Int,
    val status: String,
    val idempotencyKey: String
)

@Entity(tableName = "courier_efficiency_stats")
data class CourierEfficiencyEntity(
    @PrimaryKey val id: String,
    val dayKey: String,
    val dayLabel: String,
    val fullDate: String,
    val completedDeliveries: Int,
    val distanceKm: Double,
    val targetDeliveries: Int = 20,
    val avgDeliveryMinutes: Int = 21,
    val earnings: Double = 1420.0,
    val timestamp: Long = System.currentTimeMillis()
)
