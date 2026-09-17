package com.example.data.api

import com.example.data.database.entities.*
import java.util.UUID

/**
 * MockCourierBackend provides realistic server-side simulation of:
 * - Dispatch Engine (auto assigns tasks, courier cannot accept/reject)
 * - Route Engine (re-orders stops to minimize total route cost)
 * - Partner Restaurant Settlement & Cash wallet management
 * - Audit logs for payment changes
 */
object MockCourierBackend {

    fun createInitialCourier(): CourierProfileEntity {
        return CourierProfileEntity(
            id = "123",
            name = "Ahmet",
            surname = "Yılmaz",
            phone = "+90 532 555 0123",
            status = "ONLINE",
            latitude = 41.0082,
            longitude = 28.9784,
            packageLimit = 5,
            currentPackageCount = 3,
            isOnline = true,
            vehicleType = "Motosiklet",
            plateNumber = "34 SEN 2026"
        )
    }

    fun createInitialPartnerRestaurants(): List<PartnerRestaurantEntity> {
        return listOf(
            PartnerRestaurantEntity(
                id = "REST-001",
                name = "Köfteci Yusuf - Beşiktaş",
                address = "Sinanpaşa Mah. Şair Nedim Cad. No:14, Beşiktaş",
                phone = "+90 212 260 1010",
                pendingBalance = 1450.0,
                isActive = true
            ),
            PartnerRestaurantEntity(
                id = "REST-002",
                name = "Burger Lab - Kadıköy",
                address = "Caferağa Mah. Moda Cad. No:45, Kadıköy",
                phone = "+90 216 345 2020",
                pendingBalance = 980.0,
                isActive = true
            ),
            PartnerRestaurantEntity(
                id = "REST-003",
                name = "Pizza İl Forno - Üsküdar",
                address = "Mimar Sinan Mah. Hakimiyeti Milliye Cad. No:8, Üsküdar",
                phone = "+90 216 553 3030",
                pendingBalance = 2100.0,
                isActive = true
            ),
            PartnerRestaurantEntity(
                id = "REST-004",
                name = "Sushi Co - Levent",
                address = "Nisbetiye Cad. No:12, Beşiktaş",
                phone = "+90 212 280 4040",
                pendingBalance = 3200.0,
                isActive = true
            )
        )
    }

    fun createInitialOrders(): List<OrderEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            OrderEntity(
                id = "ORD-1001",
                externalOrderId = "SK1001",
                restaurantId = "REST-001",
                restaurantName = "Köfteci Yusuf - Beşiktaş",
                restaurantAddress = "Sinanpaşa Mah. Şair Nedim Cad. No:14, Beşiktaş",
                customerName = "Ahmet Kaya",
                customerPhone = "+90 533 111 2233",
                customerAddress = "Yıldız Mah. Çırağan Cad. No:28 D:4, Beşiktaş",
                latitude = 41.0435,
                longitude = 29.0062,
                amount = 450.0,
                paymentMethod = "CASH",
                status = "ASSIGNED",
                packageCount = 1,
                notes = "Kapıda zili çalmayın, bebek uyuyor.",
                itemsSummary = "1x 1.5 Porsiyon Köfte, 1x Piyaz, 1x Ayran",
                createdAt = now - 15 * 60 * 1000
            ),
            OrderEntity(
                id = "ORD-1002",
                externalOrderId = "SK1002",
                restaurantId = "REST-002",
                restaurantName = "Burger Lab - Kadıköy",
                restaurantAddress = "Caferağa Mah. Moda Cad. No:45, Kadıköy",
                customerName = "Mehmet Demir",
                customerPhone = "+90 535 222 3344",
                customerAddress = "Osmanağa Mah. Söğütlüçeşme Cad. No:52 D:2, Kadıköy",
                latitude = 40.9902,
                longitude = 29.0298,
                amount = 320.0,
                paymentMethod = "CARD",
                status = "ASSIGNED",
                packageCount = 1,
                notes = "Sosu bol olsun lütfen.",
                itemsSummary = "1x Truffle Burger Menü, 1x Çıtır Soğan",
                createdAt = now - 10 * 60 * 1000
            ),
            OrderEntity(
                id = "ORD-1003",
                externalOrderId = "SK1003",
                restaurantId = "REST-003",
                restaurantName = "Pizza İl Forno - Üsküdar",
                restaurantAddress = "Mimar Sinan Mah. Hakimiyeti Milliye Cad. No:8, Üsküdar",
                customerName = "Canan Öztürk",
                customerPhone = "+90 542 333 4455",
                customerAddress = "Aziz Mahmut Hüdayi Mah. Gülfem Sok. No:11, Üsküdar",
                latitude = 41.0255,
                longitude = 29.0152,
                amount = 280.0,
                paymentMethod = "CASH",
                status = "ASSIGNED",
                packageCount = 1,
                notes = "Güvenliğe bırakabilirsiniz.",
                itemsSummary = "1x Margherita Pizza (Büyük), 1x Kola Zero",
                createdAt = now - 5 * 60 * 1000
            )
        )
    }

    fun createInitialRoute(): Pair<RouteEntity, List<RouteStopEntity>> {
        val routeId = "ROUTE-" + UUID.randomUUID().toString().take(8)
        val route = RouteEntity(
            id = routeId,
            courierId = "123",
            totalDistanceKm = 8.4,
            estimatedDurationMinutes = 24,
            version = 1,
            updatedAt = System.currentTimeMillis()
        )

        // Route stops optimized by Route Engine: Pickup 1, Pickup 2, Dropoff 1, Pickup 3, Dropoff 2, Dropoff 3
        val stops = listOf(
            RouteStopEntity(
                id = "STOP-01",
                routeId = routeId,
                stopOrder = 1,
                type = "PICKUP",
                orderId = "ORD-1001",
                packageCode = "#SK1001",
                locationName = "Köfteci Yusuf (Restoran)",
                address = "Sinanpaşa Mah. Şair Nedim Cad. No:14, Beşiktaş",
                latitude = 41.0425,
                longitude = 29.0050,
                isCompleted = false,
                estimatedArrivalMinutes = 4,
                contactPhone = "+90 212 260 1010"
            ),
            RouteStopEntity(
                id = "STOP-02",
                routeId = routeId,
                stopOrder = 2,
                type = "PICKUP",
                orderId = "ORD-1002",
                packageCode = "#SK1002",
                locationName = "Burger Lab (Restoran)",
                address = "Caferağa Mah. Moda Cad. No:45, Kadıköy",
                latitude = 40.9880,
                longitude = 29.0270,
                isCompleted = false,
                estimatedArrivalMinutes = 10,
                contactPhone = "+90 216 345 2020"
            ),
            RouteStopEntity(
                id = "STOP-03",
                routeId = routeId,
                stopOrder = 3,
                type = "DROPOFF",
                orderId = "ORD-1001",
                packageCode = "#SK1001",
                locationName = "Müşteri: Ahmet Kaya",
                address = "Yıldız Mah. Çırağan Cad. No:28 D:4, Beşiktaş",
                latitude = 41.0435,
                longitude = 29.0062,
                isCompleted = false,
                estimatedArrivalMinutes = 15,
                contactPhone = "+90 533 111 2233"
            ),
            RouteStopEntity(
                id = "STOP-04",
                routeId = routeId,
                stopOrder = 4,
                type = "PICKUP",
                orderId = "ORD-1003",
                packageCode = "#SK1003",
                locationName = "Pizza İl Forno (Restoran)",
                address = "Mimar Sinan Mah. Hakimiyeti Milliye Cad. No:8, Üsküdar",
                latitude = 41.0255,
                longitude = 29.0152,
                isCompleted = false,
                estimatedArrivalMinutes = 18,
                contactPhone = "+90 216 553 3030"
            ),
            RouteStopEntity(
                id = "STOP-05",
                routeId = routeId,
                stopOrder = 5,
                type = "DROPOFF",
                orderId = "ORD-1002",
                packageCode = "#SK1002",
                locationName = "Müşteri: Mehmet Demir",
                address = "Osmanağa Mah. Söğütlüçeşme Cad. No:52 D:2, Kadıköy",
                latitude = 40.9902,
                longitude = 29.0298,
                isCompleted = false,
                estimatedArrivalMinutes = 21,
                contactPhone = "+90 535 222 3344"
            ),
            RouteStopEntity(
                id = "STOP-06",
                routeId = routeId,
                stopOrder = 6,
                type = "DROPOFF",
                orderId = "ORD-1003",
                packageCode = "#SK1003",
                locationName = "Müşteri: Canan Öztürk",
                address = "Aziz Mahmut Hüdayi Mah. Gülfem Sok. No:11, Üsküdar",
                latitude = 41.0255,
                longitude = 29.0152,
                isCompleted = false,
                estimatedArrivalMinutes = 24,
                contactPhone = "+90 542 333 4455"
            )
        )
        return Pair(route, stops)
    }

    fun createInitialCashTransactions(): List<CashTransactionEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            CashTransactionEntity(
                id = "CTX-101",
                type = "CUSTOMER_COLLECTION",
                title = "Müşteri Nakit Tahsilatı (#SK0998)",
                amount = 500.0,
                restaurantId = "REST-001",
                restaurantName = "Köfteci Yusuf",
                orderId = "ORD-0998",
                timestamp = now - 3 * 3600 * 1000,
                transactionCode = "TX-849102"
            ),
            CashTransactionEntity(
                id = "CTX-102",
                type = "CUSTOMER_COLLECTION",
                title = "Müşteri Nakit Tahsilatı (#SK0999)",
                amount = 750.0,
                restaurantId = "REST-002",
                restaurantName = "Burger Lab",
                orderId = "ORD-0999",
                timestamp = now - 2 * 3600 * 1000,
                transactionCode = "TX-849103"
            ),
            CashTransactionEntity(
                id = "CTX-103",
                type = "RESTAURANT_PAYMENT",
                title = "Anlaşmalı Restoran Ödemesi",
                amount = -1000.0,
                restaurantId = "REST-003",
                restaurantName = "Pizza İl Forno",
                orderId = null,
                timestamp = now - 1 * 3600 * 1000,
                transactionCode = "TX-849104"
            )
        )
    }

    fun generateNewSimulatedAssignment(currentCount: Int): Pair<OrderEntity, RouteStopEntity> {
        val nextNumber = 1004 + currentCount
        val orderId = "ORD-$nextNumber"
        val order = OrderEntity(
            id = orderId,
            externalOrderId = "SK$nextNumber",
            restaurantId = "REST-004",
            restaurantName = "Sushi Co - Levent",
            restaurantAddress = "Nisbetiye Cad. No:12, Beşiktaş",
            customerName = "Zeynep Aksoy",
            customerPhone = "+90 536 999 8877",
            customerAddress = "Akatlar Mah. Zeytinoğlu Cad. No:18, Beşiktaş",
            latitude = 41.0821,
            longitude = 29.0292,
            amount = 540.0,
            paymentMethod = "CASH",
            status = "ASSIGNED",
            packageCount = 1,
            notes = "Ekstra soya sosu ve zencefil lütfen.",
            itemsSummary = "1x California Roll, 1x Salmon Nigiri",
            createdAt = System.currentTimeMillis()
        )
        val dropStop = RouteStopEntity(
            id = "STOP-" + UUID.randomUUID().toString().take(6),
            routeId = "CURRENT",
            stopOrder = 7 + currentCount,
            type = "DROPOFF",
            orderId = orderId,
            packageCode = "#SK$nextNumber",
            locationName = "Müşteri: Zeynep Aksoy",
            address = "Akatlar Mah. Zeytinoğlu Cad. No:18, Beşiktaş",
            latitude = 41.0821,
            longitude = 29.0292,
            isCompleted = false,
            estimatedArrivalMinutes = 28,
            contactPhone = "+90 536 999 8877"
        )
        return Pair(order, dropStop)
    }
}
