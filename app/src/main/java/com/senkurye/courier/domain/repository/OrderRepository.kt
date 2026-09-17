package com.senkurye.courier.domain.repository

import com.senkurye.courier.domain.model.Order
import com.senkurye.courier.domain.model.OrderStatus
import com.senkurye.courier.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for Order domain data.
 * Abstracts local database and remote synchronization from presentation/domain layers.
 */
interface OrderRepository {
    fun getAllOrdersFlow(): Flow<List<Order>>
    fun getActiveOrdersFlow(): Flow<List<Order>>
    fun getCompletedOrdersFlow(): Flow<List<Order>>
    fun getOrderFlow(orderId: String): Flow<Order?>
    suspend fun getOrderById(orderId: String): Order?
    suspend fun saveOrders(orders: List<Order>)
    suspend fun saveOrder(order: Order)
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean
    suspend fun updateOrderPaymentMethod(orderId: String, newMethod: PaymentMethod)
}
