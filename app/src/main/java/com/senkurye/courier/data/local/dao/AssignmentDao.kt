package com.senkurye.courier.data.local.dao

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

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments WHERE courierId = :courierId ORDER BY sequenceNumber ASC")
    fun getAssignmentsForCourierFlow(courierId: String): Flow<List<AssignmentEntity>>

    @Transaction
    @Query("SELECT * FROM assignments WHERE courierId = :courierId AND status != 'DELIVERED' AND status != 'CANCELLED' ORDER BY sequenceNumber ASC")
    fun getActiveAssignmentsWithOrderFlow(courierId: String): Flow<List<AssignmentWithOrder>>

    @Query("SELECT * FROM assignments WHERE id = :id LIMIT 1")
    suspend fun getAssignmentById(id: String): AssignmentEntity?

    @Query("SELECT * FROM assignments WHERE orderId = :orderId LIMIT 1")
    suspend fun getAssignmentByOrderId(orderId: String): AssignmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Query("UPDATE assignments SET status = :status WHERE id = :id")
    suspend fun updateAssignmentStatus(id: String, status: String)

    @Query("UPDATE assignments SET sequenceNumber = :newSequence WHERE id = :id")
    suspend fun updateSequence(id: String, newSequence: Int)

    @Query("UPDATE assignments SET actualPickupTime = :time, status = 'PICKED_UP' WHERE id = :id")
    suspend fun markPickedUp(id: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE assignments SET actualDeliveryTime = :time, status = 'DELIVERED' WHERE id = :id")
    suspend fun markDelivered(id: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignment(id: String)
}
