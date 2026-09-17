package com.senkurye.courier.data.repository

import com.senkurye.courier.data.local.dao.OrderDao
import com.senkurye.courier.data.remote.CourierApiService

/**
 * Implementation subclass of OrderRepository for backward compatibility.
 */
class OrderRepositoryImpl(
    orderDao: OrderDao,
    apiService: CourierApiService? = null
) : OrderRepository(orderDao, apiService)

