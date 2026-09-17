package com.senkurye.courier.data.remote

import com.senkurye.courier.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for 'Şen Kurye' courier platform.
 * Defines the remote REST endpoints for task fetching, status updates,
 * and high-frequency telemetry location reporting.
 */
interface CourierApiService {

    // ==========================================
    // 1. TASK FETCHING ENDPOINTS
    // ==========================================

    /**
     * Fetches all assigned tasks for a courier.
     * Supports optional status filtering (e.g. ASSIGNED, ON_ROUTE, DELIVERED).
     */
    @GET("api/v1/couriers/{courierId}/tasks")
    suspend fun getTasks(
        @Path("courierId") courierId: String,
        @Query("status") status: String? = null
    ): Response<List<TaskResponse>>

    /**
     * Fetches all currently active (undelivered) tasks for the courier,
     * ordered by route optimization sequence number.
     */
    @GET("api/v1/couriers/{courierId}/tasks/active")
    suspend fun getActiveTasks(
        @Path("courierId") courierId: String
    ): Response<List<TaskResponse>>

    /**
     * Fetches single task assignment detail including order information.
     */
    @GET("api/v1/tasks/{taskId}")
    suspend fun getTaskById(
        @Path("taskId") taskId: String
    ): Response<TaskResponse>

    // ==========================================
    // 2. STATUS UPDATE ENDPOINTS
    // ==========================================

    /**
     * Updates the operational shift status of the courier (ONLINE, OFFLINE, PAUSED, AVAILABLE).
     */
    @PUT("api/v1/couriers/{courierId}/status")
    suspend fun updateCourierStatus(
        @Path("courierId") courierId: String,
        @Body request: CourierStatusUpdateRequest
    ): Response<CourierStatusUpdateResponse>

    /**
     * Updates the status of a specific task (e.g., AT_RESTAURANT, PICKED_UP, ON_ROUTE).
     * Includes idempotencyKey to ensure safe retries during network reconnects.
     */
    @PUT("api/v1/tasks/{taskId}/status")
    suspend fun updateTaskStatus(
        @Path("taskId") taskId: String,
        @Body request: TaskStatusUpdateRequest
    ): Response<TaskStatusUpdateResponse>

    /**
     * Convenient endpoint to mark that the package has been picked up from the restaurant.
     */
    @POST("api/v1/tasks/{taskId}/pickup")
    suspend fun markTaskPickedUp(
        @Path("taskId") taskId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String
    ): Response<TaskStatusUpdateResponse>

    /**
     * Completes a delivery task with payment collection details and customer acknowledgement.
     */
    @POST("api/v1/tasks/{taskId}/deliver")
    suspend fun completeDelivery(
        @Path("taskId") taskId: String,
        @Body request: DeliveryCompletionRequest
    ): Response<TaskStatusUpdateResponse>

    // ==========================================
    // 3. LOCATION REPORTING ENDPOINTS
    // ==========================================

    /**
     * Reports real-time courier GPS coordinates and telemetry.
     * Consumed by backend dispatch engine for live tracking and route re-optimization.
     */
    @POST("api/v1/couriers/{courierId}/location")
    suspend fun reportLocation(
        @Path("courierId") courierId: String,
        @Body request: LocationReportRequest
    ): Response<LocationReportResponse>

    /**
     * Flushes buffered location reports accumulated during offline periods.
     */
    @POST("api/v1/couriers/{courierId}/location/batch")
    suspend fun reportLocationBatch(
        @Path("courierId") courierId: String,
        @Body request: LocationBatchReportRequest
    ): Response<LocationReportResponse>
}
