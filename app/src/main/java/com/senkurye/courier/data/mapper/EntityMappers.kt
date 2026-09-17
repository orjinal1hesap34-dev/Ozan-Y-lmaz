package com.senkurye.courier.data.mapper

import com.senkurye.courier.data.local.dao.AssignmentWithOrder
import com.senkurye.courier.data.local.entities.AssignmentEntity
import com.senkurye.courier.data.local.entities.CourierEntity
import com.senkurye.courier.data.local.entities.OrderEntity
import com.senkurye.courier.domain.model.*

fun CourierEntity.toDomain(): Courier {
    return Courier(
        id = id,
        name = name,
        surname = surname,
        phone = phone,
        status = CourierStatus.fromString(status),
        isOnline = isOnline,
        currentLatitude = currentLatitude,
        currentLongitude = currentLongitude,
        maxPackageLimit = maxPackageLimit,
        currentPackageCount = currentPackageCount,
        vehicleType = vehicleType,
        plateNumber = plateNumber,
        physicalCashBalance = physicalCashBalance,
        lastLocationUpdate = lastLocationUpdate
    )
}

fun Courier.toEntity(isSynced: Boolean = true): CourierEntity {
    return CourierEntity(
        id = id,
        name = name,
        surname = surname,
        phone = phone,
        status = status.name,
        isOnline = isOnline,
        currentLatitude = currentLatitude,
        currentLongitude = currentLongitude,
        maxPackageLimit = maxPackageLimit,
        currentPackageCount = currentPackageCount,
        vehicleType = vehicleType,
        plateNumber = plateNumber,
        physicalCashBalance = physicalCashBalance,
        lastLocationUpdate = lastLocationUpdate,
        isSynced = isSynced
    )
}

fun OrderEntity.toDomain(): Order {
    return Order(
        id = id,
        externalOrderId = externalOrderId,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        restaurantAddress = restaurantAddress,
        restaurantPhone = restaurantPhone,
        restaurantLatitude = restaurantLatitude,
        restaurantLongitude = restaurantLongitude,
        customerName = customerName,
        customerPhone = customerPhone,
        customerAddress = customerAddress,
        customerLatitude = customerLatitude,
        customerLongitude = customerLongitude,
        totalAmount = totalAmount,
        paymentMethod = PaymentMethod.fromString(paymentMethod),
        status = OrderStatus.fromString(status),
        packageCount = packageCount,
        notes = notes,
        itemsSummary = itemsSummary,
        preparationTimeMinutes = preparationTimeMinutes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Order.toEntity(isSynced: Boolean = true): OrderEntity {
    return OrderEntity(
        id = id,
        externalOrderId = externalOrderId,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        restaurantAddress = restaurantAddress,
        restaurantPhone = restaurantPhone,
        restaurantLatitude = restaurantLatitude,
        restaurantLongitude = restaurantLongitude,
        customerName = customerName,
        customerPhone = customerPhone,
        customerAddress = customerAddress,
        customerLatitude = customerLatitude,
        customerLongitude = customerLongitude,
        totalAmount = totalAmount,
        paymentMethod = paymentMethod.name,
        status = status.name,
        packageCount = packageCount,
        notes = notes,
        itemsSummary = itemsSummary,
        preparationTimeMinutes = preparationTimeMinutes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}

fun AssignmentEntity.toDomain(): Assignment {
    return Assignment(
        id = id,
        orderId = orderId,
        courierId = courierId,
        assignedAt = assignedAt,
        status = AssignmentStatus.fromString(status),
        sequenceNumber = sequenceNumber,
        estimatedPickupTime = estimatedPickupTime,
        estimatedDeliveryTime = estimatedDeliveryTime,
        actualPickupTime = actualPickupTime,
        actualDeliveryTime = actualDeliveryTime,
        routeCostScore = routeCostScore,
        isAutoAssigned = isAutoAssigned,
        idempotencyKey = idempotencyKey
    )
}

fun Assignment.toEntity(isSynced: Boolean = true): AssignmentEntity {
    return AssignmentEntity(
        id = id,
        orderId = orderId,
        courierId = courierId,
        assignedAt = assignedAt,
        status = status.name,
        sequenceNumber = sequenceNumber,
        estimatedPickupTime = estimatedPickupTime,
        estimatedDeliveryTime = estimatedDeliveryTime,
        actualPickupTime = actualPickupTime,
        actualDeliveryTime = actualDeliveryTime,
        routeCostScore = routeCostScore,
        isAutoAssigned = isAutoAssigned,
        isSynced = isSynced,
        idempotencyKey = idempotencyKey
    )
}

fun AssignmentWithOrder.toDomain(): AssignmentDetail {
    return AssignmentDetail(
        assignment = assignment.toDomain(),
        order = order?.toDomain()
    )
}

fun com.senkurye.courier.data.remote.dto.TaskOrderDto.toEntity(isSynced: Boolean = true): OrderEntity {
    return OrderEntity(
        id = id,
        externalOrderId = externalOrderId,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        restaurantAddress = restaurantAddress,
        restaurantPhone = restaurantPhone,
        restaurantLatitude = restaurantLatitude,
        restaurantLongitude = restaurantLongitude,
        customerName = customerName,
        customerPhone = customerPhone,
        customerAddress = customerAddress,
        customerLatitude = customerLatitude,
        customerLongitude = customerLongitude,
        totalAmount = totalAmount,
        paymentMethod = paymentMethod,
        status = status,
        notes = notes,
        itemsSummary = itemsSummary,
        preparationTimeMinutes = preparationTimeMinutes,
        isSynced = isSynced
    )
}

fun com.senkurye.courier.data.remote.dto.TaskResponse.toAssignmentEntity(isSynced: Boolean = true): AssignmentEntity {
    return AssignmentEntity(
        id = assignmentId,
        orderId = orderId,
        courierId = courierId,
        assignedAt = assignedAt,
        status = status,
        sequenceNumber = sequenceNumber,
        estimatedPickupTime = estimatedPickupTime,
        estimatedDeliveryTime = estimatedDeliveryTime,
        isAutoAssigned = true,
        isSynced = isSynced
    )
}

