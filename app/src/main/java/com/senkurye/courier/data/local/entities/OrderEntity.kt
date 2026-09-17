package com.senkurye.courier.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an Order placed by a customer and prepared by a restaurant.
 * Offline-first design allows full offline inspection and status updates.
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val externalOrderId: String, // e.g. #SK1001
    val restaurantId: String,
    val restaurantName: String,
    val restaurantAddress: String,
    val restaurantPhone: String,
    val restaurantLatitude: Double,
    val restaurantLongitude: Double,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val customerLatitude: Double,
    val customerLongitude: Double,
    val totalAmount: Double,
    val paymentMethod: String, // CASH, CARD, ONLINE, MEAL_CARD, FREE, OTHER
    val status: String, // ASSIGNED, GOING_TO_RESTAURANT, AT_RESTAURANT, PICKED_UP, GOING_TO_CUSTOMER, ARRIVED, DELIVERED, CANCELLED
    val packageCount: Int = 1,
    val notes: String = "",
    val itemsSummary: String = "",
    val preparationTimeMinutes: Int = 15,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)
