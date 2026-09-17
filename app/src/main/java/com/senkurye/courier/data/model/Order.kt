package com.senkurye.courier.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an Order in 'com.senkurye.courier.data.model'.
 * Contains customer and restaurant locations, financials, and lifecycle status.
 */
@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "externalOrderId")
    val externalOrderId: String,

    @ColumnInfo(name = "restaurantId")
    val restaurantId: String,

    @ColumnInfo(name = "restaurantName")
    val restaurantName: String,

    @ColumnInfo(name = "restaurantAddress")
    val restaurantAddress: String,

    @ColumnInfo(name = "restaurantPhone")
    val restaurantPhone: String,

    @ColumnInfo(name = "restaurantLatitude")
    val restaurantLatitude: Double,

    @ColumnInfo(name = "restaurantLongitude")
    val restaurantLongitude: Double,

    @ColumnInfo(name = "customerName")
    val customerName: String,

    @ColumnInfo(name = "customerPhone")
    val customerPhone: String,

    @ColumnInfo(name = "customerAddress")
    val customerAddress: String,

    @ColumnInfo(name = "customerLatitude")
    val customerLatitude: Double,

    @ColumnInfo(name = "customerLongitude")
    val customerLongitude: Double,

    @ColumnInfo(name = "totalAmount")
    val totalAmount: Double,

    @ColumnInfo(name = "paymentMethod")
    val paymentMethod: String, // CASH, CARD, ONLINE, MEAL_CARD, FREE, OTHER

    @ColumnInfo(name = "status")
    val status: String, // ASSIGNED, GOING_TO_RESTAURANT, AT_RESTAURANT, PICKED_UP, DELIVERED, CANCELLED

    @ColumnInfo(name = "packageCount")
    val packageCount: Int = 1,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "itemsSummary")
    val itemsSummary: String = "",

    @ColumnInfo(name = "preparationTimeMinutes")
    val preparationTimeMinutes: Int = 15,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true
)

typealias OrderEntity = Order
