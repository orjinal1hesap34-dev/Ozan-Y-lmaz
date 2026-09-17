package com.senkurye.courier.domain.model

enum class CourierStatus(val title: String) {
    OFFLINE("Çevrimdışı"),
    ONLINE("Çevrimiçi"),
    AVAILABLE("Boşta / Bekliyor"),
    GOING_TO_RESTAURANT("Restorana Gidiyor"),
    AT_RESTAURANT("Restoranda"),
    WAITING_FOR_ORDER("Sipariş Bekleniyor"),
    GOING_TO_CUSTOMER("Müşteriye Gidiyor"),
    DELIVERED("Teslim Edildi"),
    PAUSED("Molada");

    companion object {
        fun fromString(value: String): CourierStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: ONLINE
        }
    }
}

enum class OrderStatus(val title: String, val stepOrder: Int) {
    ASSIGNED("Atandı", 1),
    GOING_TO_RESTAURANT("Restorana Gidiliyor", 2),
    AT_RESTAURANT("Restoranda", 3),
    PICKED_UP("Paket Alındı", 4),
    GOING_TO_CUSTOMER("Müşteriye Gidiliyor", 5),
    ARRIVED("Adrese Ulaşıldı", 6),
    DELIVERED("Teslim Edildi", 7),
    CANCELLED("İptal Edildi", 0);

    fun canTransitionTo(next: OrderStatus): Boolean {
        if (this == DELIVERED || this == CANCELLED) return false
        if (next == CANCELLED) return true
        return next.stepOrder > this.stepOrder
    }

    companion object {
        fun fromString(value: String): OrderStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: ASSIGNED
        }
    }
}

enum class PaymentMethod(val title: String) {
    CASH("Nakit"),
    CARD("Kart"),
    ONLINE("Online"),
    MEAL_CARD("Yemek Kartı"),
    FREE("Ücretsiz"),
    OTHER("Diğer");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CASH
        }
    }
}

enum class AssignmentStatus(val title: String) {
    ASSIGNED("Atandı"),
    AT_RESTAURANT("Restoranda"),
    PICKED_UP("Paket Alındı"),
    ON_ROUTE("Dağıtımda"),
    DELIVERED("Teslim Edildi"),
    CANCELLED("İptal");

    companion object {
        fun fromString(value: String): AssignmentStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: ASSIGNED
        }
    }
}

data class Courier(
    val id: String,
    val name: String,
    val surname: String,
    val phone: String,
    val status: CourierStatus = CourierStatus.ONLINE,
    val isOnline: Boolean = true,
    val currentLatitude: Double = 41.0082,
    val currentLongitude: Double = 28.9784,
    val maxPackageLimit: Int = 5,
    val currentPackageCount: Int = 0,
    val vehicleType: String = "Motosiklet",
    val plateNumber: String = "",
    val physicalCashBalance: Double = 0.0,
    val lastLocationUpdate: Long = System.currentTimeMillis()
)

data class Order(
    val id: String,
    val externalOrderId: String,
    val restaurantId: String,
    val restaurantName: String,
    val restaurantAddress: String,
    val restaurantPhone: String,
    val restaurantLatitude: Double,
    val restaurantLongitude: Double,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val customerLatitude: Double,
    val customerLongitude: Double,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val status: OrderStatus,
    val packageCount: Int = 1,
    val notes: String = "",
    val itemsSummary: String = "",
    val preparationTimeMinutes: Int = 15,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Assignment(
    val id: String,
    val orderId: String,
    val courierId: String,
    val assignedAt: Long = System.currentTimeMillis(),
    val status: AssignmentStatus = AssignmentStatus.ASSIGNED,
    val sequenceNumber: Int = 1,
    val estimatedPickupTime: Long? = null,
    val estimatedDeliveryTime: Long? = null,
    val actualPickupTime: Long? = null,
    val actualDeliveryTime: Long? = null,
    val routeCostScore: Double = 0.0,
    val isAutoAssigned: Boolean = true,
    val idempotencyKey: String = ""
)

data class AssignmentDetail(
    val assignment: Assignment,
    val order: Order?
)
