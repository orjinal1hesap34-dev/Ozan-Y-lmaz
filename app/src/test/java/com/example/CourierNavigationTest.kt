package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.navigation.CourierNavigationManager
import com.example.core.navigation.NavDebugData
import com.example.domain.model.RouteStop
import com.example.domain.model.StopType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CourierNavigationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `navigation rejects null target stop and returns error message`() {
        var capturedError: String? = null
        val result = CourierNavigationManager.startNavigation(
            context = context,
            courierLat = 41.0082,
            courierLng = 28.9784,
            targetStop = null,
            onError = { capturedError = it }
        )

        assertFalse(result)
        assertEquals("Teslimat adresinin konum bilgisi bulunamadı.", capturedError)
    }

    @Test
    fun `navigation rejects invalid or zero coordinates and returns error message`() {
        val zeroStop = RouteStop(
            id = "stop_zero",
            routeId = "route_1",
            stopOrder = 1,
            type = StopType.DROPOFF,
            orderId = "order_1",
            packageCode = "SK1001",
            locationName = "Eksik Konumlu Adres",
            address = "Bilinmeyen Sokak",
            latitude = 0.0,
            longitude = 0.0,
            isCompleted = false
        )

        var capturedError: String? = null
        val result = CourierNavigationManager.startNavigation(
            context = context,
            courierLat = 41.0082,
            courierLng = 28.9784,
            targetStop = zeroStop,
            onError = { capturedError = it }
        )

        assertFalse(result)
        assertEquals("Teslimat adresinin konum bilgisi bulunamadı.", capturedError)
    }

    @Test
    fun `navigation emits accurate debug data containing required location and coordinates`() {
        val targetStop = RouteStop(
            id = "stop_valid",
            routeId = "route_1",
            stopOrder = 1,
            type = StopType.DROPOFF,
            orderId = "order_1",
            packageCode = "SK1001",
            locationName = "Köfteci Yusuf (Paket #SK1001)",
            address = "Beşiktaş Çarşı No:12",
            latitude = 41.042500,
            longitude = 29.005000,
            isCompleted = false
        )

        var capturedDebug: NavDebugData? = null
        val courierLat = 41.008200
        val courierLng = 28.978400

        CourierNavigationManager.startNavigation(
            context = context,
            courierLat = courierLat,
            courierLng = courierLng,
            targetStop = targetStop,
            onDebugLogged = { capturedDebug = it }
        )

        assertNotNull(capturedDebug)
        assertEquals("41.008200, 28.978400", capturedDebug?.currentLocation)
        assertEquals("Köfteci Yusuf (Paket #SK1001)", capturedDebug?.destination)
        assertEquals("41.042500", capturedDebug?.latitude)
        assertEquals("29.005000", capturedDebug?.longitude)
    }

    @Test
    fun `three package scenario always navigates to immediate next pending stop in order`() {
        val stop1 = RouteStop(
            id = "stop_1",
            routeId = "route_1",
            stopOrder = 1,
            type = StopType.PICKUP,
            orderId = "ord_1",
            packageCode = "SK1001",
            locationName = "1. Restoran (Alım)",
            address = "Restoran Sokak No:1",
            latitude = 41.041000,
            longitude = 29.001000,
            isCompleted = true // Completed
        )

        val stop2 = RouteStop(
            id = "stop_2",
            routeId = "route_1",
            stopOrder = 2,
            type = StopType.DROPOFF,
            orderId = "ord_1",
            packageCode = "SK1001",
            locationName = "1. Müşteri (Teslimat)",
            address = "Müşteri Sokak No:2",
            latitude = 41.043500,
            longitude = 29.004500,
            isCompleted = false // Active next!
        )

        val stop3 = RouteStop(
            id = "stop_3",
            routeId = "route_1",
            stopOrder = 3,
            type = StopType.DROPOFF,
            orderId = "ord_2",
            packageCode = "SK1002",
            locationName = "2. Müşteri (Teslimat)",
            address = "Müşteri Sokak No:3",
            latitude = 41.048000,
            longitude = 29.009000,
            isCompleted = false
        )

        val stops = listOf(stop1, stop2, stop3)
        val nextStop = stops.firstOrNull { !it.isCompleted }

        // Stop 2 must be picked
        assertNotNull(nextStop)
        assertEquals("stop_2", nextStop?.id)

        var capturedDebug: NavDebugData? = null
        CourierNavigationManager.startNavigation(
            context = context,
            courierLat = 41.041000,
            courierLng = 29.001000,
            targetStop = nextStop,
            onDebugLogged = { capturedDebug = it }
        )

        assertNotNull(capturedDebug)
        assertEquals("41.043500", capturedDebug?.latitude)
        assertEquals("29.004500", capturedDebug?.longitude)
        assertEquals("1. Müşteri (Teslimat)", capturedDebug?.destination)
    }
}
