package com.example.domain.model

enum class CourierStatus(val title: String) {
    OFFLINE("Çevrimdışı"),
    ONLINE("Çevrimiçi"),
    AVAILABLE("Boşta / Bekliyor"),
    GOING_TO_RESTAURANT("Restorana Gidiyor"),
    AT_RESTAURANT("Restoranda"),
    WAITING_FOR_ORDER("Sipariş Bekleniyor"),
    GOING_TO_CUSTOMER("Müşteriye Gidiyor"),
    DELIVERED("Teslim Edildi"),
    PAUSED("Molada")
}

enum class OrderStatus(val title: String, val stepOrder: Int) {
    ASSIGNED("Atandı", 1),
    GOING_TO_RESTAURANT("Restorana Gidiliyor", 2),
    AT_RESTAURANT("Restoranda", 3),
    PICKED_UP("Paket Alındı", 4),
    GOING_TO_CUSTOMER("Müşteriye Gidiliyor", 5),
    ARRIVED("Adrese Ulaşıldı", 6),
    DELIVERED("Teslim Edildi", 7);

    fun canTransitionTo(next: OrderStatus): Boolean {
        // Enforce strict state machine transitions to avoid invalid states like DELIVERED -> PICKED_UP
        return next.stepOrder > this.stepOrder
    }
}

enum class PaymentMethod(val title: String) {
    CASH("Nakit"),
    CARD("Kart"),
    ONLINE("Online"),
    MEAL_CARD("Yemek Kartı"),
    OTHER("Diğer"),
    FREE("Ücretsiz")
}

enum class StopType(val title: String) {
    PICKUP("Paket Alımı"),
    DROPOFF("Müşteri Teslimatı")
}

data class Courier(
    val id: String = "123",
    val name: String = "Ahmet",
    val surname: String = "Yılmaz",
    val phone: String = "+90 532 555 0123",
    val status: CourierStatus = CourierStatus.ONLINE,
    val latitude: Double = 41.0082,
    val longitude: Double = 28.9784,
    val packageLimit: Int = 5,
    val currentPackageCount: Int = 3,
    val isOnline: Boolean = true,
    val vehicleType: String = "Motosiklet",
    val plateNumber: String = "34 SEN 2026"
)

data class OrderItem(
    val name: String,
    val quantity: Int,
    val note: String? = null
)

data class Order(
    val id: String,
    val externalOrderId: String,
    val restaurantId: String,
    val restaurantName: String,
    val restaurantAddress: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val latitude: Double,
    val longitude: Double,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val status: OrderStatus,
    val packageCount: Int = 1,
    val notes: String = "",
    val itemsSummary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class RouteStop(
    val id: String,
    val routeId: String,
    val stopOrder: Int,
    val type: StopType,
    val orderId: String,
    val packageCode: String,
    val locationName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isCompleted: Boolean = false,
    val estimatedArrivalMinutes: Int = 10,
    val contactPhone: String = ""
)

data class RoutePlan(
    val id: String,
    val courierId: String,
    val stops: List<RouteStop>,
    val totalDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val version: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

data class PaymentChangeLog(
    val id: String,
    val orderId: String,
    val oldPaymentMethod: PaymentMethod,
    val newPaymentMethod: PaymentMethod,
    val courierId: String,
    val changedAt: Long,
    val changeReason: String,
    val changedBy: String = "Kurye"
)

data class CashTransaction(
    val id: String,
    val type: String, // "CUSTOMER_COLLECTION", "RESTAURANT_PAYMENT", "SETTLEMENT"
    val title: String,
    val amount: Double,
    val restaurantId: String? = null,
    val restaurantName: String? = null,
    val orderId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val transactionCode: String
)

data class PartnerRestaurant(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val pendingBalance: Double,
    val isActive: Boolean = true
)

data class DayEndSummary(
    val totalDeliveries: Int = 18,
    val completedDeliveries: Int = 14,
    val pendingDeliveries: Int = 4,
    val totalEarnings: Double = 1850.0,
    val cashEarnings: Double = 3420.0,
    val cardEarnings: Double = 1150.0,
    val onlineEarnings: Double = 2100.0,
    val totalOrdersAmount: Double = 11750.0,
    val restaurantCashPaid: Double = 2500.0,
    val expectedPhysicalCash: Double = 3750.0,
    val actualPhysicalCash: Double = 3750.0,
    val discrepancy: Double = 0.0
)

data class SyncItem(
    val id: String,
    val type: String,
    val payload: String,
    val createdAt: Long,
    val retryCount: Int = 0,
    val status: String = "PENDING", // PENDING, SYNCED, RETRY, FAILED
    val idempotencyKey: String
)
