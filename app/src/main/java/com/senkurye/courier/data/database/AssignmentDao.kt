package com.senkurye.courier.data.database

import androidx.room.*
import com.senkurye.courier.data.local.entities.AssignmentEntity
import com.senkurye.courier.data.local.entities.OrderEntity
import kotlinx.coroutines.flow.Flow

data class AssignmentWithOrder(
    @Embedded val assignment: AssignmentEntity,
    @Relation(
        parentColumn = "orderId",
        entityColumn = "id"
    )
    val order: OrderEntity?
)

/**
 * Data Access Object for Assignment entity in 'com.senkurye.courier.data.database'.
 * Provides insert, delete, update, and reactive Flow observation methods.
 */
@Dao
interface AssignmentDao {

    // ==========================================
    // 1. OBSERVING DATA AS FLOW
    // ==========================================

    @Query("SELECT * FROM assignments WHERE courierId = :courierId ORDER BY sequenceNumber ASC")
    fun getAssignmentsForCourierFlow(courierId: String): Flow<List<AssignmentEntity>>

    @Transaction
    @Query("SELECT * FROM assignments WHERE courierId = :courierId AND status != 'DELIVERED' AND status != 'CANCELLED' ORDER BY sequenceNumber ASC")
    fun getActiveAssignmentsWithOrderFlow(courierId: String): Flow<List<AssignmentWithOrder>>

    @Query("SELECT * FROM assignments WHERE id = :id LIMIT 1")
    fun getAssignmentFlow(id: String): Flow<AssignmentEntity?>

    @Query("SELECT * FROM assignments WHERE orderId = :orderId LIMIT 1")
    fun getAssignmentByOrderIdFlow(orderId: String): Flow<AssignmentEntity?>

    @Query("SELECT * FROM assignments ORDER BY assignedAt DESC")
    fun getAllAssignmentsFlow(): Flow<List<AssignmentEntity>>

    // ==========================================
    // 2. ONE-SHOT GETTERS
    // ==========================================

    @Query("SELECT * FROM assignments WHERE id = :id LIMIT 1")
    suspend fun getAssignmentById(id: String): AssignmentEntity?

    @Query("SELECT * FROM assignments WHERE orderId = :orderId LIMIT 1")
    suspend fun getAssignmentByOrderId(orderId: String): AssignmentEntity?

    @Query("SELECT * FROM assignments")
    suspend fun getAllAssignments(): List<AssignmentEntity>

    // ==========================================
    // 3. INSERTION METHODS
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    // ==========================================
    // 4. UPDATE METHODS
    // ==========================================

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Query("UPDATE assignments SET status = :status WHERE id = :id")
    suspend fun updateAssignmentStatus(id: String, status: String)

    @Query("UPDATE assignments SET sequenceNumber = :newSequence WHERE id = :id")
    suspend fun updateSequence(id: String, newSequence: Int)

    @Query("UPDATE assignments SET actualPickupTime = :time, status = 'PICKED_UP' WHERE id = :id")
    suspend fun markPickedUp(id: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE assignments SET actualDeliveryTime = :time, status = 'DELIVERED' WHERE id = :id")
    suspend fun markDelivered(id: String, time: Long = System.currentTimeMillis())

    // ==========================================
    // 5. DELETION METHODS
    // ==========================================

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: String)

    @Query("DELETE FROM assignments")
    suspend fun deleteAllAssignments()
}
