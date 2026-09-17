package com.senkurye.courier.data.local.dao

import androidx.room.*
import com.senkurye.courier.data.local.entities.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status != 'DELIVERED' AND status != 'CANCELLED' ORDER BY createdAt DESC")
    fun getActiveOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'DELIVERED' ORDER BY updatedAt DESC")
    fun getCompletedOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderFlow(orderId: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :newStatus, updatedAt = :timestamp WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, newStatus: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET paymentMethod = :newMethod, updatedAt = :timestamp WHERE id = :orderId")
    suspend fun updateOrderPaymentMethod(orderId: String, newMethod: String, timestamp: Long = System.currentTimeMillis())
}
