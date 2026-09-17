package com.senkurye.courier.data.repository

import com.senkurye.courier.data.local.dao.CourierDao
import com.senkurye.courier.data.remote.CourierApiService

/**
 * Implementation subclass of CourierRepository for backward compatibility.
 */
class CourierRepositoryImpl(
    courierDao: CourierDao,
    apiService: CourierApiService? = null
) : CourierRepository(courierDao, apiService)

