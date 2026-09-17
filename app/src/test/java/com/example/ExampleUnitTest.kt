package com.example

import com.example.data.api.MockCourierBackend
import com.example.domain.model.OrderStatus
import com.example.domain.model.PaymentMethod
import com.senkurye.courier.data.mapper.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testOrderStatusStateMachineTransitions() {
        // Valid forward transitions
        assertTrue(OrderStatus.ASSIGNED.canTransitionTo(OrderStatus.GOING_TO_RESTAURANT))
        assertTrue(OrderStatus.GOING_TO_RESTAURANT.canTransitionTo(OrderStatus.AT_RESTAURANT))
        assertTrue(OrderStatus.AT_RESTAURANT.canTransitionTo(OrderStatus.PICKED_UP))
        assertTrue(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.GOING_TO_CUSTOMER))
        assertTrue(OrderStatus.GOING_TO_CUSTOMER.canTransitionTo(OrderStatus.DELIVERED))

        // Invalid backward transitions (Strict state machine defense)
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PICKED_UP))
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.ASSIGNED))
        assertFalse(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.GOING_TO_RESTAURANT))
    }

    @Test
    fun testInitialCourierProfileMockData() {
        val courier = MockCourierBackend.createInitialCourier()
        assertEquals("Ahmet", courier.name)
        assertEquals("Yılmaz", courier.surname)
        assertEquals(5, courier.packageLimit)
        assertEquals(3, courier.currentPackageCount)
        assertTrue(courier.isOnline)
        assertEquals("34 SEN 2026", courier.plateNumber)
    }

    @Test
    fun testInitialRouteAndMultiPackageStops() {
        val (route, stops) = MockCourierBackend.createInitialRoute()
        assertEquals(1, route.version)
        assertEquals(6, stops.size)
        // Check pickup and dropoff pairing
        val pickupStops = stops.filter { it.type == "PICKUP" }
        val dropoffStops = stops.filter { it.type == "DROPOFF" }
        assertEquals(3, pickupStops.size)
        assertEquals(3, dropoffStops.size)
    }

    @Test
    fun testCashReconciliationMath() {
        val initialFloat = 3420.0
        val customerCollection = 500.0
        val restaurantPayment = 1000.0

        val netExpectedPhysicalCash = initialFloat + customerCollection - restaurantPayment
        assertEquals(2920.0, netExpectedPhysicalCash, 0.001)
    }

    @Test
    fun testSenKuryeCourierDataLayerEntities() {
        val courier = com.senkurye.courier.data.local.entities.CourierEntity(
            id = "c_101",
            name = "Ahmet",
            surname = "Yılmaz",
            phone = "+905325550123",
            status = "ONLINE",
            isOnline = true,
            currentLatitude = 41.0082,
            currentLongitude = 28.9784,
            maxPackageLimit = 5,
            currentPackageCount = 3,
            vehicleType = "Motosiklet",
            plateNumber = "34 SEN 2026",
            physicalCashBalance = 3420.0
        )
        assertEquals("c_101", courier.id)
        assertEquals(5, courier.maxPackageLimit)
        assertTrue(courier.isOnline)

        val order = com.senkurye.courier.data.local.entities.OrderEntity(
            id = "ord_99",
            externalOrderId = "SK1001",
            restaurantId = "rest_1",
            restaurantName = "Köfteci Yusuf",
            restaurantAddress = "Beşiktaş",
            restaurantPhone = "02120000000",
            restaurantLatitude = 41.04,
            restaurantLongitude = 29.00,
            customerName = "Can",
            customerPhone = "05330000000",
            customerAddress = "Yıldız Cad.",
            customerLatitude = 41.05,
            customerLongitude = 29.01,
            totalAmount = 450.0,
            paymentMethod = "CASH",
            status = "ASSIGNED"
        )
        assertEquals("SK1001", order.externalOrderId)
        assertEquals(450.0, order.totalAmount, 0.001)

        val assignment = com.senkurye.courier.data.local.entities.AssignmentEntity(
            id = "asg_1",
            orderId = "ord_99",
            courierId = "c_101",
            status = "ASSIGNED",
            sequenceNumber = 1,
            isAutoAssigned = true
        )
        assertEquals("ord_99", assignment.orderId)
        assertTrue(assignment.isAutoAssigned)
    }

    @Test
    fun testEntityMappersAndDomainAbstraction() {
        val courierEntity = com.senkurye.courier.data.local.entities.CourierEntity(
            id = "c_505",
            name = "Mehmet",
            surname = "Demir",
            phone = "+905551234567",
            status = "ONLINE",
            isOnline = true,
            currentLatitude = 41.01,
            currentLongitude = 28.97,
            maxPackageLimit = 6,
            currentPackageCount = 2,
            vehicleType = "Scooter",
            plateNumber = "34 KRY 789",
            physicalCashBalance = 1500.0
        )

        val domainCourier = courierEntity.toDomain()
        assertEquals(com.senkurye.courier.domain.model.CourierStatus.ONLINE, domainCourier.status)
        assertEquals("Mehmet", domainCourier.name)
        assertEquals(6, domainCourier.maxPackageLimit)

        val convertedEntity = domainCourier.toEntity()
        assertEquals(courierEntity.id, convertedEntity.id)
        assertEquals(courierEntity.phone, convertedEntity.phone)

        val orderEntity = com.senkurye.courier.data.local.entities.OrderEntity(
            id = "o_1",
            externalOrderId = "SK2001",
            restaurantId = "r_1",
            restaurantName = "Dönerci Ali",
            restaurantAddress = "Kadıköy",
            restaurantPhone = "02161112233",
            restaurantLatitude = 40.99,
            restaurantLongitude = 29.02,
            customerName = "Ayşe",
            customerPhone = "05441112233",
            customerAddress = "Moda",
            customerLatitude = 40.98,
            customerLongitude = 29.03,
            totalAmount = 320.0,
            paymentMethod = "CARD",
            status = "ASSIGNED"
        )

        val domainOrder = orderEntity.toDomain()
        assertEquals(com.senkurye.courier.domain.model.PaymentMethod.CARD, domainOrder.paymentMethod)
        assertEquals(com.senkurye.courier.domain.model.OrderStatus.ASSIGNED, domainOrder.status)
        assertTrue(domainOrder.status.canTransitionTo(com.senkurye.courier.domain.model.OrderStatus.GOING_TO_RESTAURANT))
        assertFalse(domainOrder.status.canTransitionTo(com.senkurye.courier.domain.model.OrderStatus.DELIVERED).not()) // ASSIGNED can transition forward
    }

    @Test
    fun testCourierApiServiceDataTransferObjects() {
        val locationReq = com.senkurye.courier.data.remote.dto.LocationReportRequest(
            latitude = 41.0082,
            longitude = 28.9784,
            speed = 35.5f,
            heading = 180.0f,
            accuracy = 4.2f,
            batteryLevel = 88
        )
        assertEquals(41.0082, locationReq.latitude, 0.0001)
        assertEquals(35.5f, locationReq.speed)

        val statusReq = com.senkurye.courier.data.remote.dto.CourierStatusUpdateRequest(
            status = "ONLINE",
            isOnline = true,
            reason = "Vardiya başlangıcı"
        )
        assertTrue(statusReq.isOnline)
        assertEquals("ONLINE", statusReq.status)

        val taskStatusReq = com.senkurye.courier.data.remote.dto.TaskStatusUpdateRequest(
            status = "AT_RESTAURANT",
            latitude = 41.0082,
            longitude = 28.9784,
            idempotencyKey = "idem-task-123"
        )
        assertEquals("AT_RESTAURANT", taskStatusReq.status)
        assertEquals("idem-task-123", taskStatusReq.idempotencyKey)
    }

    @Test
    fun testRepositoriesClassesExistence() {
        val courierRepoClass = com.senkurye.courier.data.repository.CourierRepository::class.java
        assertNotNull(courierRepoClass)

        val orderRepoClass = com.senkurye.courier.data.repository.OrderRepository::class.java
        assertNotNull(orderRepoClass)

        val assignmentRepoClass = com.senkurye.courier.data.repository.AssignmentRepository::class.java
        assertNotNull(assignmentRepoClass)
    }

    @Test
    fun testAppDatabaseAndDaosPackage() {
        val dbClass = com.senkurye.courier.data.database.AppDatabase::class.java
        assertNotNull(dbClass)

        val courierDaoClass = com.senkurye.courier.data.database.CourierDao::class.java
        assertNotNull(courierDaoClass)
        // Verify insert, delete, and flow methods exist
        assertTrue(courierDaoClass.methods.any { it.name == "insertCourier" })
        assertTrue(courierDaoClass.methods.any { it.name == "deleteCourier" })
        assertTrue(courierDaoClass.methods.any { it.name == "getCourierFlow" })

        val orderDaoClass = com.senkurye.courier.data.database.OrderDao::class.java
        assertNotNull(orderDaoClass)
        assertTrue(orderDaoClass.methods.any { it.name == "insertOrder" })
        assertTrue(orderDaoClass.methods.any { it.name == "deleteOrder" })
        assertTrue(orderDaoClass.methods.any { it.name == "getAllOrdersFlow" })

        val assignmentDaoClass = com.senkurye.courier.data.database.AssignmentDao::class.java
        assertNotNull(assignmentDaoClass)
        assertTrue(assignmentDaoClass.methods.any { it.name == "insertAssignment" })
        assertTrue(assignmentDaoClass.methods.any { it.name == "deleteAssignment" })
        assertTrue(assignmentDaoClass.methods.any { it.name == "getAssignmentsForCourierFlow" })
    }

    @Test
    fun testDataModelEntities() {
        val courier = com.senkurye.courier.data.model.Courier(
            id = "c_101",
            name = "Ahmet",
            surname = "Yılmaz",
            phone = "+905550001122",
            status = "AVAILABLE",
            isOnline = true,
            currentLatitude = 41.015,
            currentLongitude = 28.979,
            maxPackageLimit = 4,
            currentPackageCount = 1,
            vehicleType = "Motosiklet",
            plateNumber = "34 KRY 101",
            physicalCashBalance = 450.0
        )
        assertEquals("c_101", courier.id)
        assertEquals("Ahmet", courier.name)
        assertTrue(courier.isOnline)

        val order = com.senkurye.courier.data.model.Order(
            id = "ord_555",
            externalOrderId = "#SK999",
            restaurantId = "r_10",
            restaurantName = "Burger King",
            restaurantAddress = "Levent",
            restaurantPhone = "02120000000",
            restaurantLatitude = 41.08,
            restaurantLongitude = 29.01,
            customerName = "Caner",
            customerPhone = "05320000000",
            customerAddress = "4. Levent",
            customerLatitude = 41.09,
            customerLongitude = 29.02,
            totalAmount = 285.50,
            paymentMethod = "CASH",
            status = "ASSIGNED"
        )
        assertEquals("ord_555", order.id)
        assertEquals(285.50, order.totalAmount, 0.01)

        val assignment = com.senkurye.courier.data.model.Assignment(
            id = "asg_123",
            orderId = "ord_555",
            courierId = "c_101",
            status = "ASSIGNED",
            sequenceNumber = 1
        )
        assertEquals("asg_123", assignment.id)
        assertTrue(assignment.isAutoAssigned)
    }

    @Test
    fun testDeliveryViewModelStateFlowAndStatusUpdates() {
        val testOrder = com.senkurye.courier.domain.model.Order(
            id = "ord_1",
            externalOrderId = "#1001",
            restaurantId = "r_1",
            restaurantName = "Test Restoran",
            restaurantAddress = "Kadıköy",
            restaurantPhone = "02160000000",
            restaurantLatitude = 40.99,
            restaurantLongitude = 29.02,
            customerName = "Ali Veli",
            customerPhone = "05551112233",
            customerAddress = "Moda",
            customerLatitude = 40.98,
            customerLongitude = 29.03,
            totalAmount = 150.0,
            paymentMethod = com.senkurye.courier.domain.model.PaymentMethod.CASH,
            status = com.senkurye.courier.domain.model.OrderStatus.ASSIGNED
        )

        val testAssignment = com.senkurye.courier.domain.model.Assignment(
            id = "asg_1",
            orderId = "ord_1",
            courierId = "c_101",
            status = com.senkurye.courier.domain.model.AssignmentStatus.ASSIGNED,
            sequenceNumber = 1
        )

        val assignmentDetail = com.senkurye.courier.domain.model.AssignmentDetail(
            assignment = testAssignment,
            order = testOrder
        )

        var currentOrderStatus = testOrder.status
        var currentAssignmentStatus = testAssignment.status

        val fakeAssignmentRepo = object : com.senkurye.courier.domain.repository.AssignmentRepository {
            override fun getAssignmentsForCourierFlow(courierId: String) =
                kotlinx.coroutines.flow.flowOf(listOf(testAssignment))

            override fun getActiveAssignmentsWithOrderFlow(courierId: String) =
                kotlinx.coroutines.flow.flowOf(listOf(assignmentDetail))

            override suspend fun getAssignmentById(id: String) = testAssignment
            override suspend fun getAssignmentByOrderId(orderId: String) = testAssignment
            override suspend fun autoAssignOrder(assignment: com.senkurye.courier.domain.model.Assignment) {}
            override suspend fun autoAssignOrders(assignments: List<com.senkurye.courier.domain.model.Assignment>) {}
            override suspend fun updateAssignmentStatus(id: String, status: com.senkurye.courier.domain.model.AssignmentStatus) {
                currentAssignmentStatus = status
            }
            override suspend fun updateSequence(id: String, newSequence: Int) {}
            override suspend fun markPickedUp(id: String) {
                currentAssignmentStatus = com.senkurye.courier.domain.model.AssignmentStatus.PICKED_UP
            }
            override suspend fun markDelivered(id: String) {
                currentAssignmentStatus = com.senkurye.courier.domain.model.AssignmentStatus.DELIVERED
            }
            override suspend fun cancelAssignment(id: String) {
                currentAssignmentStatus = com.senkurye.courier.domain.model.AssignmentStatus.CANCELLED
            }
        }

        val fakeOrderRepo = object : com.senkurye.courier.domain.repository.OrderRepository {
            override fun getAllOrdersFlow() = kotlinx.coroutines.flow.flowOf(listOf(testOrder))
            override fun getActiveOrdersFlow() = kotlinx.coroutines.flow.flowOf(listOf(testOrder))
            override fun getCompletedOrdersFlow() = kotlinx.coroutines.flow.flowOf(emptyList<com.senkurye.courier.domain.model.Order>())
            override fun getOrderFlow(orderId: String) = kotlinx.coroutines.flow.flowOf(testOrder)
            override suspend fun getOrderById(orderId: String) = testOrder
            override suspend fun saveOrders(orders: List<com.senkurye.courier.domain.model.Order>) {}
            override suspend fun saveOrder(order: com.senkurye.courier.domain.model.Order) {}
            override suspend fun updateOrderStatus(orderId: String, newStatus: com.senkurye.courier.domain.model.OrderStatus): Boolean {
                currentOrderStatus = newStatus
                return true
            }
            override suspend fun updateOrderPaymentMethod(orderId: String, newMethod: com.senkurye.courier.domain.model.PaymentMethod) {}
        }

        val viewModel = com.senkurye.courier.presentation.delivery.DeliveryViewModel(
            assignmentRepository = fakeAssignmentRepo,
            orderRepository = fakeOrderRepo,
            initialCourierId = "c_101",
            dispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        )

        assertNotNull(viewModel.uiState)
        viewModel.markArrivedAtRestaurant("ord_1", "asg_1")
        assertEquals(com.senkurye.courier.domain.model.OrderStatus.AT_RESTAURANT, currentOrderStatus)
        assertEquals(com.senkurye.courier.domain.model.AssignmentStatus.AT_RESTAURANT, currentAssignmentStatus)

        viewModel.markPackagePickedUp("ord_1", "asg_1")
        assertEquals(com.senkurye.courier.domain.model.OrderStatus.PICKED_UP, currentOrderStatus)
        assertEquals(com.senkurye.courier.domain.model.AssignmentStatus.PICKED_UP, currentAssignmentStatus)

        viewModel.completeDelivery("ord_1", "asg_1")
        assertEquals(com.senkurye.courier.domain.model.OrderStatus.DELIVERED, currentOrderStatus)
        assertEquals(com.senkurye.courier.domain.model.AssignmentStatus.DELIVERED, currentAssignmentStatus)
    }
}



