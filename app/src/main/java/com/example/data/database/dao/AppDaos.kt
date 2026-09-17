package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourierDao {
    @Query("SELECT * FROM courier_profile WHERE id = :id LIMIT 1")
    fun getProfileFlow(id: String = "123"): Flow<CourierProfileEntity?>

    @Query("SELECT * FROM courier_profile WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: String = "123"): CourierProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: CourierProfileEntity)

    @Query("UPDATE courier_profile SET isOnline = :isOnline, status = :status WHERE id = :id")
    suspend fun updateOnlineStatus(id: String = "123", isOnline: Boolean, status: String)

    @Query("UPDATE courier_profile SET latitude = :lat, longitude = :lng WHERE id = :id")
    suspend fun updateLocation(id: String = "123", lat: Double, lng: Double)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status != 'DELIVERED' ORDER BY createdAt DESC")
    fun getActiveOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'DELIVERED' ORDER BY createdAt DESC")
    fun getCompletedOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderFlow(orderId: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET paymentMethod = :newMethod WHERE id = :orderId")
    suspend fun updateOrderPaymentMethod(orderId: String, newMethod: String)
}

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY version DESC LIMIT 1")
    fun getLatestRouteFlow(): Flow<RouteEntity?>

    @Query("SELECT * FROM route_stops WHERE routeId = :routeId ORDER BY stopOrder ASC")
    fun getRouteStopsFlow(routeId: String): Flow<List<RouteStopEntity>>

    @Query("SELECT * FROM route_stops ORDER BY stopOrder ASC")
    fun getAllStopsFlow(): Flow<List<RouteStopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<RouteStopEntity>)

    @Query("UPDATE route_stops SET isCompleted = :completed WHERE id = :stopId")
    suspend fun updateStopCompleted(stopId: String, completed: Boolean)

    @Query("DELETE FROM route_stops WHERE routeId = :routeId")
    suspend fun clearStopsForRoute(routeId: String)
}

@Dao
interface PaymentChangeDao {
    @Query("SELECT * FROM payment_changes ORDER BY changedAt DESC")
    fun getAllChangesFlow(): Flow<List<PaymentChangeEntity>>

    @Query("SELECT * FROM payment_changes WHERE orderId = :orderId ORDER BY changedAt DESC")
    fun getChangesForOrderFlow(orderId: String): Flow<List<PaymentChangeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChange(change: PaymentChangeEntity)
}

@Dao
interface CashDao {
    @Query("SELECT * FROM cash_transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<CashTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CashTransactionEntity)

    @Query("SELECT * FROM partner_restaurants WHERE isActive = 1")
    fun getPartnerRestaurantsFlow(): Flow<List<PartnerRestaurantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartnerRestaurants(restaurants: List<PartnerRestaurantEntity>)

    @Query("UPDATE partner_restaurants SET pendingBalance = pendingBalance + :amount WHERE id = :restaurantId")
    suspend fun updateRestaurantBalance(restaurantId: String, amount: Double)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'RETRY' ORDER BY createdAt ASC")
    fun getPendingQueueFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'RETRY' ORDER BY createdAt ASC")
    suspend fun getPendingQueueList(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueueItem(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status, retryCount = :retryCount WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, retryCount: Int)

    @Query("SELECT COUNT(*) FROM sync_queue WHERE idempotencyKey = :key AND status != 'FAILED'")
    suspend fun checkDuplicate(key: String): Int
}

@Dao
interface EfficiencyDao {
    @Query("SELECT * FROM courier_efficiency_stats ORDER BY timestamp ASC")
    fun getAllEfficiencyStatsFlow(): Flow<List<CourierEfficiencyEntity>>

    @Query("SELECT * FROM courier_efficiency_stats ORDER BY timestamp ASC")
    suspend fun getAllEfficiencyStats(): List<CourierEfficiencyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEfficiencyStats(stats: List<CourierEfficiencyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEfficiencyStat(stat: CourierEfficiencyEntity)

    @Query("UPDATE courier_efficiency_stats SET completedDeliveries = :completedDeliveries, distanceKm = :distanceKm WHERE id = :id")
    suspend fun updateDeliveriesAndDistance(id: String, completedDeliveries: Int, distanceKm: Double)
}
