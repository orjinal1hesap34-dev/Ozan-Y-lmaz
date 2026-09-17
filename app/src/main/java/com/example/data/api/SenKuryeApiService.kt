package com.example.data.api

import retrofit2.Response
import retrofit2.http.*

// API Request / Response DTOs
data class LoginRequest(val phone: String, val pin: String)
data class AuthResponse(val token: String, val refreshToken: String, val courierId: String)

data class StatusUpdateRequest(val status: String, val isOnline: Boolean)
data class LocationUpdateRequest(val latitude: Double, val longitude: Double, val speed: Float = 0f, val accuracy: Float = 0f, val timestamp: Long = System.currentTimeMillis())

data class PaymentChangeRequest(
    val orderId: String,
    val oldPaymentMethod: String,
    val newPaymentMethod: String,
    val changeReason: String,
    val idempotencyKey: String
)

data class RestaurantPaymentRequest(
    val restaurantId: String,
    val amount: Double,
    val courierId: String,
    val idempotencyKey: String
)

data class RestaurantPaymentResponse(
    val transactionId: String,
    val status: String,
    val newCashBalance: Double,
    val timestamp: Long
)

data class SyncBatchRequest(
    val items: List<SyncItemDto>
)

data class SyncItemDto(
    val id: String,
    val type: String,
    val payload: String,
    val idempotencyKey: String
)

data class SyncResponse(
    val processedKeys: List<String>,
    val failedKeys: List<String>
)

interface SenKuryeApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Header("Authorization") token: String): Response<AuthResponse>

    @POST("courier/status")
    suspend fun updateStatus(
        @Header("Authorization") token: String,
        @Body request: StatusUpdateRequest
    ): Response<Unit>

    @POST("courier/location")
    suspend fun sendLocation(
        @Header("Authorization") token: String,
        @Body request: LocationUpdateRequest
    ): Response<Unit>

    @POST("orders/{id}/pickup")
    suspend fun markOrderPickedUp(
        @Path("id") orderId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("orders/{id}/arrived")
    suspend fun markOrderArrived(
        @Path("id") orderId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("orders/{id}/deliver")
    suspend fun markOrderDelivered(
        @Path("id") orderId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("orders/{id}/payment/change")
    suspend fun changePaymentMethod(
        @Path("id") orderId: String,
        @Header("Authorization") token: String,
        @Body request: PaymentChangeRequest
    ): Response<Unit>

    @POST("courier/cash/restaurant-payment")
    suspend fun payToRestaurant(
        @Header("Authorization") token: String,
        @Body request: RestaurantPaymentRequest
    ): Response<RestaurantPaymentResponse>

    @POST("sync")
    suspend fun syncOfflineData(
        @Header("Authorization") token: String,
        @Body request: SyncBatchRequest
    ): Response<SyncResponse>
}
