package com.senkurye.courier.domain.repository

import com.senkurye.courier.domain.model.Assignment
import com.senkurye.courier.domain.model.AssignmentDetail
import com.senkurye.courier.domain.model.AssignmentStatus
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for Courier Assignments.
 * Enforces the dispatch rule: "Kurye siparişi kabul veya reddedemez".
 * Automatically synchronizes assignments and route sequence into the active queue.
 */
interface AssignmentRepository {
    fun getAssignmentsForCourierFlow(courierId: String): Flow<List<Assignment>>
    fun getActiveAssignmentsWithOrderFlow(courierId: String): Flow<List<AssignmentDetail>>
    suspend fun getAssignmentById(id: String): Assignment?
    suspend fun getAssignmentByOrderId(orderId: String): Assignment?
    suspend fun autoAssignOrder(assignment: Assignment)
    suspend fun autoAssignOrders(assignments: List<Assignment>)
    suspend fun updateAssignmentStatus(id: String, status: AssignmentStatus)
    suspend fun updateSequence(id: String, newSequence: Int)
    suspend fun markPickedUp(id: String)
    suspend fun markDelivered(id: String)
    suspend fun cancelAssignment(id: String)
}
