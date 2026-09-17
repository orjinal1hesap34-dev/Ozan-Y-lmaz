package com.senkurye.courier.data.remote.dto

/**
 * Data Transfer Objects for Courier API communication.
 */
data class CourierStatusUpdateRequest(
    val status: String,
    val isOnline: Boolean,
    val reason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class CourierStatusUpdateResponse(
    val success: Boolean,
    val courierId: String,
    val status: String,
    val isOnline: Boolean,
    val message: String? = null
)

data class LocationReportRequest(
    val latitude: Double,
    val longitude: Double,
    val speed: Float = 0f,
    val heading: Float = 0f,
    val accuracy: Float = 0f,
    val batteryLevel: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class LocationBatchReportRequest(
    val courierId: String,
    val reports: List<LocationReportRequest>
)

data class LocationReportResponse(
    val success: Boolean,
    val recordedCount: Int = 1,
    val serverTime: Long = System.currentTimeMillis()
)

data class TaskResponse(
    val assignmentId: String,
    val orderId: String,
    val courierId: String,
    val sequenceNumber: Int,
    val status: String, // ASSIGNED, AT_RESTAURANT, PICKED_UP, ON_ROUTE, DELIVERED, CANCELLED
    val assignedAt: Long,
    val estimatedPickupTime: Long?,
    val estimatedDeliveryTime: Long?,
    val order: TaskOrderDto
)

data class TaskOrderDto(
    val id: String,
    val externalOrderId: String,
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
    val paymentMethod: String,
    val status: String,
    val notes: String = "",
    val itemsSummary: String = "",
    val preparationTimeMinutes: Int = 15
)

data class TaskStatusUpdateRequest(
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String? = null,
    val idempotencyKey: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TaskStatusUpdateResponse(
    val success: Boolean,
    val assignmentId: String,
    val orderId: String,
    val newStatus: String,
    val nextSequenceNumber: Int? = null,
    val message: String? = null
)

data class DeliveryCompletionRequest(
    val paymentMethod: String,
    val collectedAmount: Double,
    val customerSignatureUrl: String? = null,
    val deliveryNote: String? = null,
    val idempotencyKey: String,
    val timestamp: Long = System.currentTimeMillis()
)
