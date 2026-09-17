package com.senkurye.courier.data.repository

import com.senkurye.courier.data.local.dao.AssignmentDao
import com.senkurye.courier.data.local.dao.OrderDao
import com.senkurye.courier.data.remote.CourierApiService

/**
 * Implementation subclass of AssignmentRepository for backward compatibility.
 */
class AssignmentRepositoryImpl(
    assignmentDao: AssignmentDao,
    orderDao: OrderDao? = null,
    apiService: CourierApiService? = null
) : AssignmentRepository(assignmentDao, orderDao, apiService)

