package com.senkurye.courier.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing an automated Dispatch Assignment in 'com.senkurye.courier.data.model'.
 * Enforces the dispatch rule: "Kurye siparişi kabul veya reddedemez".
 */
@Entity(
    tableName = "assignments",
    indices = [
        Index(value = ["orderId"], unique = true),
        Index(value = ["courierId"]),
        Index(value = ["status"]),
        Index(value = ["sequenceNumber"])
    ]
)
data class Assignment(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "orderId")
    val orderId: String,

    @ColumnInfo(name = "courierId")
    val courierId: String,

    @ColumnInfo(name = "assignedAt")
    val assignedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "status")
    val status: String, // ASSIGNED, AT_RESTAURANT, PICKED_UP, ON_ROUTE, DELIVERED, CANCELLED

    @ColumnInfo(name = "sequenceNumber")
    val sequenceNumber: Int,

    @ColumnInfo(name = "estimatedPickupTime")
    val estimatedPickupTime: Long? = null,

    @ColumnInfo(name = "estimatedDeliveryTime")
    val estimatedDeliveryTime: Long? = null,

    @ColumnInfo(name = "actualPickupTime")
    val actualPickupTime: Long? = null,

    @ColumnInfo(name = "actualDeliveryTime")
    val actualDeliveryTime: Long? = null,

    @ColumnInfo(name = "routeCostScore")
    val routeCostScore: Double = 0.0,

    @ColumnInfo(name = "isAutoAssigned")
    val isAutoAssigned: Boolean = true,

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "idempotencyKey")
    val idempotencyKey: String = ""
)

typealias AssignmentEntity = Assignment
