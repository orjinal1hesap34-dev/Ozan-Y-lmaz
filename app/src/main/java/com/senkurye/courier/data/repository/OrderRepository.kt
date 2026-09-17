package com.senkurye.courier.data.repository

import com.senkurye.courier.data.local.dao.OrderDao
import com.senkurye.courier.data.mapper.toDomain
import com.senkurye.courier.data.mapper.toEntity
import com.senkurye.courier.data.remote.CourierApiService
import com.senkurye.courier.data.remote.dto.DeliveryCompletionRequest
import com.senkurye.courier.data.remote.dto.TaskStatusUpdateRequest
import com.senkurye.courier.domain.model.Order
import com.senkurye.courier.domain.model.OrderStatus
import com.senkurye.courier.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Repository class acting as the single source of truth for Order data.
 * Abstracts Room database and Retrofit network calls for the rest of the application.
 * Enforces state machine transitions and offline-first persistence.
 */
open class OrderRepository(
    private val orderDao: OrderDao,
    private val apiService: CourierApiService? = null
) : com.senkurye.courier.domain.repository.OrderRepository {

    override fun getAllOrdersFlow(): Flow<List<Order>> {
        return orderDao.getAllOrdersFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveOrdersFlow(): Flow<List<Order>> {
        return orderDao.getActiveOrdersFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getCompletedOrdersFlow(): Flow<List<Order>> {
        return orderDao.getCompletedOrdersFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getOrderFlow(orderId: String): Flow<Order?> {
        return orderDao.getOrderFlow(orderId).map { it?.toDomain() }
    }

    override suspend fun getOrderById(orderId: String): Order? {
        return orderDao.getOrderById(orderId)?.toDomain()
    }

    override suspend fun saveOrders(orders: List<Order>) {
        orderDao.insertOrders(orders.map { it.toEntity() })
    }

    override suspend fun saveOrder(order: Order) {
        orderDao.insertOrder(order.toEntity())
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean {
        val existing = orderDao.getOrderById(orderId) ?: return false
        val currentStatus = OrderStatus.fromString(existing.status)

        // Strict state machine validation: cannot regress or transition illegally
        if (!currentStatus.canTransitionTo(newStatus)) {
            return false
        }

        val now = System.currentTimeMillis()
        // 1. Single source of truth update in Room
        orderDao.updateOrderStatus(orderId, newStatus.name, now)

        // 2. Synchronize with backend API service
        if (apiService != null) {
            try {
                val idempotencyKey = "order-status-$orderId-${newStatus.name}-$now"
                val request = TaskStatusUpdateRequest(
                    status = newStatus.name,
                    idempotencyKey = idempotencyKey,
                    timestamp = now
                )
                apiService.updateTaskStatus(orderId, request)
            } catch (e: Exception) {
                // Offline fallback: order status updated in Room local DB
            }
        }

        return true
    }

    override suspend fun updateOrderPaymentMethod(orderId: String, newMethod: PaymentMethod) {
        orderDao.updateOrderPaymentMethod(orderId, newMethod.name)
    }

    suspend fun completeDelivery(
        orderId: String,
        paymentMethod: PaymentMethod,
        collectedAmount: Double,
        deliveryNote: String? = null
    ): Boolean {
        val success = updateOrderStatus(orderId, OrderStatus.DELIVERED)
        if (success && apiService != null) {
            try {
                val idempotencyKey = "delivery-complete-$orderId-${System.currentTimeMillis()}"
                val request = DeliveryCompletionRequest(
                    paymentMethod = paymentMethod.name,
                    collectedAmount = collectedAmount,
                    deliveryNote = deliveryNote,
                    idempotencyKey = idempotencyKey
                )
                apiService.completeDelivery(orderId, request)
            } catch (e: Exception) {
                // Offline fallback
            }
        }
        return success
    }
}
