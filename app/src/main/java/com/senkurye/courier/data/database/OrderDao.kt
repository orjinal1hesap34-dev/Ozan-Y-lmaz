package com.senkurye.courier.data.database

import androidx.room.*
import com.senkurye.courier.data.local.entities.OrderEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Order entity in 'com.senkurye.courier.data.database'.
 * Provides insert, delete, update, and reactive Flow observation methods.
 */
@Dao
interface OrderDao {

    // ==========================================
    // 1. OBSERVING DATA AS FLOW
    // ==========================================

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status != 'DELIVERED' AND status != 'CANCELLED' ORDER BY createdAt DESC")
    fun getActiveOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'DELIVERED' ORDER BY updatedAt DESC")
    fun getCompletedOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderFlow(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    fun getOrdersByRestaurantFlow(restaurantId: String): Flow<List<OrderEntity>>

    // ==========================================
    // 2. ONE-SHOT GETTERS
    // ==========================================

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders")
    suspend fun getAllOrders(): List<OrderEntity>

    // ==========================================
    // 3. INSERTION METHODS
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    // ==========================================
    // 4. UPDATE METHODS
    // ==========================================

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :newStatus, updatedAt = :timestamp WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, newStatus: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET paymentMethod = :newMethod, updatedAt = :timestamp WHERE id = :orderId")
    suspend fun updateOrderPaymentMethod(orderId: String, newMethod: String, timestamp: Long = System.currentTimeMillis())

    // ==========================================
    // 5. DELETION METHODS
    // ==========================================

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: String)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}
