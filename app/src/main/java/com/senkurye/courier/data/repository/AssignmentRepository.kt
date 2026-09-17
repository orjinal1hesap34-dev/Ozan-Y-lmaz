package com.senkurye.courier.data.repository

import com.senkurye.courier.data.local.dao.AssignmentDao
import com.senkurye.courier.data.local.dao.OrderDao
import com.senkurye.courier.data.mapper.toAssignmentEntity
import com.senkurye.courier.data.mapper.toDomain
import com.senkurye.courier.data.mapper.toEntity
import com.senkurye.courier.data.remote.CourierApiService
import com.senkurye.courier.data.remote.dto.TaskStatusUpdateRequest
import com.senkurye.courier.domain.model.Assignment
import com.senkurye.courier.domain.model.AssignmentDetail
import com.senkurye.courier.domain.model.AssignmentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository class acting as the single source of truth for Courier Assignments.
 * Abstracts Room database and Retrofit network calls for the rest of the application.
 * Enforces the core dispatch rule: "Kurye siparişi kabul veya reddedemez".
 */
open class AssignmentRepository(
    private val assignmentDao: AssignmentDao,
    private val orderDao: OrderDao? = null,
    private val apiService: CourierApiService? = null
) : com.senkurye.courier.domain.repository.AssignmentRepository {

    override fun getAssignmentsForCourierFlow(courierId: String): Flow<List<Assignment>> {
        return assignmentDao.getAssignmentsForCourierFlow(courierId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getActiveAssignmentsWithOrderFlow(courierId: String): Flow<List<AssignmentDetail>> {
        return assignmentDao.getActiveAssignmentsWithOrderFlow(courierId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getAssignmentById(id: String): Assignment? {
        return assignmentDao.getAssignmentById(id)?.toDomain()
    }

    override suspend fun getAssignmentByOrderId(orderId: String): Assignment? {
        return assignmentDao.getAssignmentByOrderId(orderId)?.toDomain()
    }

    override suspend fun autoAssignOrder(assignment: Assignment) {
        val autoAssigned = assignment.copy(isAutoAssigned = true)
        assignmentDao.insertAssignment(autoAssigned.toEntity())
    }

    override suspend fun autoAssignOrders(assignments: List<Assignment>) {
        val list = assignments.map { it.copy(isAutoAssigned = true).toEntity() }
        assignmentDao.insertAssignments(list)
    }

    override suspend fun updateAssignmentStatus(id: String, status: AssignmentStatus) {
        assignmentDao.updateAssignmentStatus(id, status.name)
        if (apiService != null) {
            try {
                val now = System.currentTimeMillis()
                val request = TaskStatusUpdateRequest(
                    status = status.name,
                    idempotencyKey = "asg-status-$id-${status.name}-$now",
                    timestamp = now
                )
                apiService.updateTaskStatus(id, request)
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }

    override suspend fun updateSequence(id: String, newSequence: Int) {
        assignmentDao.updateSequence(id, newSequence)
    }

    override suspend fun markPickedUp(id: String) {
        assignmentDao.markPickedUp(id)
        if (apiService != null) {
            try {
                apiService.markTaskPickedUp(id, "pickup-$id-${System.currentTimeMillis()}")
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }

    override suspend fun markDelivered(id: String) {
        assignmentDao.markDelivered(id)
    }

    override suspend fun cancelAssignment(id: String) {
        assignmentDao.updateAssignmentStatus(id, AssignmentStatus.CANCELLED.name)
    }

    /**
     * Fetches active tasks from the remote backend API and synchronizes them
     * into local Room database tables (both orders and assignments).
     * Returns true if remote sync succeeded, false if offline/failed.
     */
    suspend fun refreshActiveTasksFromRemote(courierId: String): Boolean {
        if (apiService == null) return false
        return try {
            val response = apiService.getActiveTasks(courierId)
            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!
                val orderEntities = tasks.map { it.order.toEntity(isSynced = true) }
                val assignmentEntities = tasks.map { it.toAssignmentEntity(isSynced = true) }

                orderDao?.insertOrders(orderEntities)
                assignmentDao.insertAssignments(assignmentEntities)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
