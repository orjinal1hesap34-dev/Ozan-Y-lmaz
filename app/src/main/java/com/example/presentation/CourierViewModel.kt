package com.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.ConnectivityObserver
import com.example.core.notification.CourierNotificationManager
import com.example.core.sync.OfflineSyncEngine
import com.example.core.sync.RoomSyncManager
import com.example.data.database.entities.CourierEfficiencyEntity
import com.example.data.repository.CourierRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CourierUiState(
    val courier: Courier = Courier(),
    val activeOrders: List<Order> = emptyList(),
    val completedOrders: List<Order> = emptyList(),
    val routePlan: RoutePlan? = null,
    val cashTransactions: List<CashTransaction> = emptyList(),
    val partnerRestaurants: List<PartnerRestaurant> = emptyList(),
    val dayEndSummary: DayEndSummary = DayEndSummary(),
    val pendingSyncCount: Int = 0,
    val selectedOrder: Order? = null,
    val infoMessage: String? = null,
    val isOffline: Boolean = false,
    val isSyncing: Boolean = false,
    val efficiencyStats: List<CourierEfficiencyEntity> = emptyList()
)

class CourierViewModel(
    private val repository: CourierRepository,
    private val notificationManager: CourierNotificationManager,
    private val syncEngine: OfflineSyncEngine,
    private val connectivityObserver: ConnectivityObserver? = null,
    private val roomSyncManager: RoomSyncManager? = null
) : ViewModel() {

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    private val _infoMessage = MutableStateFlow<String?>(null)

    // Observe real-time network connectivity
    private val _isOfflineFlow: Flow<Boolean> = connectivityObserver?.observe()?.map {
        !it.isAvailable
    } ?: flowOf(false)

    // Observe Room database background sync status
    private val _isSyncingFlow: Flow<Boolean> = roomSyncManager?.syncState?.map {
        it.isSyncing
    } ?: flowOf(false)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<CourierUiState> = combine(
        repository.getCourierFlow(),
        repository.getActiveOrdersFlow(),
        repository.getAllOrdersFlow(),
        repository.getActiveRoutePlanFlow(),
        repository.getCashTransactionsFlow(),
        repository.getPartnerRestaurantsFlow(),
        repository.getDayEndSummaryFlow(),
        repository.getPendingSyncCountFlow(),
        _selectedOrder,
        _infoMessage,
        _isOfflineFlow,
        _isSyncingFlow,
        repository.getEfficiencyStatsFlow()
    ) { params ->
        val courier = params[0] as Courier
        val activeOrders = params[1] as List<Order>
        val allOrders = params[2] as List<Order>
        val routePlan = params[3] as RoutePlan?
        val cashTransactions = params[4] as List<CashTransaction>
        val partnerRestaurants = params[5] as List<PartnerRestaurant>
        val dayEndSummary = params[6] as DayEndSummary
        val pendingSyncCount = params[7] as Int
        val selectedOrder = params[8] as Order?
        val infoMessage = params[9] as String?
        val isOffline = params[10] as? Boolean ?: false
        val isSyncing = params[11] as? Boolean ?: false
        @Suppress("UNCHECKED_CAST")
        val efficiencyStats = params[12] as? List<CourierEfficiencyEntity> ?: emptyList()

        val completedOrders = allOrders.filter { it.status == OrderStatus.DELIVERED }

        CourierUiState(
            courier = courier,
            activeOrders = activeOrders,
            completedOrders = completedOrders,
            routePlan = routePlan,
            cashTransactions = cashTransactions,
            partnerRestaurants = partnerRestaurants,
            dayEndSummary = dayEndSummary,
            pendingSyncCount = pendingSyncCount,
            selectedOrder = selectedOrder,
            infoMessage = infoMessage,
            isOffline = isOffline,
            isSyncing = isSyncing,
            efficiencyStats = efficiencyStats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CourierUiState(
            isOffline = connectivityObserver?.isConnected?.not() ?: false
        )
    )

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    fun selectOrder(order: Order?) {
        _selectedOrder.value = order
    }

    fun toggleOnlineStatus() {
        viewModelScope.launch {
            val isCurrentlyOnline = uiState.value.courier.isOnline
            val newOnline = !isCurrentlyOnline
            repository.setOnlineStatus(newOnline)
            _infoMessage.value = if (newOnline) "Mesaiye başlandı. Çevrimiçi duruma geçtiniz." else "Mesai sonlandırıldı. Çevrimdışı duruma geçtiniz."
        }
    }

    fun markRestoranaUlastim(orderId: String) {
        viewModelScope.launch {
            val success = repository.updateOrderStatus(orderId, OrderStatus.AT_RESTAURANT)
            if (success) {
                _infoMessage.value = "Restorana ulaşıldı. Sipariş hazırlanıyor."
            }
        }
    }

    fun markPaketiAldim(orderId: String) {
        viewModelScope.launch {
            val success = repository.updateOrderStatus(orderId, OrderStatus.PICKED_UP)
            if (success) {
                _infoMessage.value = "Paket teslim alındı. Müşteri teslimatına geçebilirsiniz."
            }
        }
    }

    fun markTeslimataBasla(orderId: String) {
        viewModelScope.launch {
            val success = repository.updateOrderStatus(orderId, OrderStatus.GOING_TO_CUSTOMER)
            if (success) {
                _infoMessage.value = "Teslimata başlandı. Müşteri adresine doğru yola çıkıldı."
            }
        }
    }

    fun markTeslimatiTamamla(orderId: String) {
        viewModelScope.launch {
            val order = uiState.value.activeOrders.find { it.id == orderId }
            val success = repository.updateOrderStatus(orderId, OrderStatus.DELIVERED)
            if (success) {
                _infoMessage.value = "Teslimat başarıyla tamamlandı! " +
                        if (order?.paymentMethod == PaymentMethod.CASH) "(₺${order.amount} Nakit cüzdana eklendi)" else ""
                _selectedOrder.value = null
            }
        }
    }

    fun changePaymentMethod(orderId: String, newMethod: PaymentMethod, reason: String) {
        viewModelScope.launch {
            val success = repository.changePaymentMethod(orderId, newMethod, reason)
            if (success) {
                _infoMessage.value = "Ödeme şekli güncellendi: ${newMethod.title}. Audit log kaydedildi."
                // Refresh selected order
                _selectedOrder.value = uiState.value.activeOrders.find { it.id == orderId }
            }
        }
    }

    fun payToRestaurant(restaurant: PartnerRestaurant, amount: Double) {
        viewModelScope.launch {
            val result = repository.payToPartnerRestaurant(restaurant, amount)
            result.onSuccess { txId ->
                _infoMessage.value = "${restaurant.name} için ₺$amount ödeme yapıldı. İşlem Kodu: $txId"
            }.onFailure { error ->
                _infoMessage.value = "Hata: ${error.message}"
            }
        }
    }

    // SIMULATION TRIGGERS (Required for Demo & Real-time validation)
    fun triggerSimulatedAssignment() {
        viewModelScope.launch {
            val newOrder = repository.simulateIncomingAssignment()
            notificationManager.showNewAssignmentNotification(
                orderCode = newOrder.externalOrderId,
                customerName = newOrder.customerName,
                amount = newOrder.amount
            )
            _infoMessage.value = "Backend Dispatch: Yeni Görev Atandı (${newOrder.externalOrderId})! Doğrudan rotanıza eklendi."
        }
    }

    fun triggerSimulatedRouteOptimization() {
        viewModelScope.launch {
            val newVersion = repository.simulateRouteUpdate()
            notificationManager.showRouteUpdatedNotification(distanceKm = 7.2, durationMin = 19)
            _infoMessage.value = "Backend Route Engine: Rota v$newVersion olarak maliyet optimizasyonu ile güncellendi (7.2 km, 19 dk)."
        }
    }

    fun triggerSyncNow() {
        viewModelScope.launch {
            val count1 = syncEngine.triggerManualSync()
            val count2 = roomSyncManager?.triggerManualSync() ?: 0
            val total = count1 + count2
            _infoMessage.value = "Room Senkronizasyonu tamamlandı ($total işlem iletildi)."
        }
    }

    fun toggleOfflineSimulation() {
        val observer = connectivityObserver as? com.senkurye.courier.core.network.NetworkConnectivityObserver
        if (observer != null) {
            val willBeOffline = !uiState.value.isOffline
            observer.setSimulatedOffline(willBeOffline)
            _infoMessage.value = if (willBeOffline) {
                "Simülasyon: İnternet bağlantısı kesildi. Çevrimdışı (Offline) moduna geçildi."
            } else {
                "Simülasyon: İnternet bağlantısı sağlandı. Room otomatik senkronizasyon tetiklendi."
            }
        }
    }

    class Factory(
        private val repository: CourierRepository,
        private val notificationManager: CourierNotificationManager,
        private val syncEngine: OfflineSyncEngine,
        private val connectivityObserver: ConnectivityObserver? = null,
        private val roomSyncManager: RoomSyncManager? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CourierViewModel(
                repository = repository,
                notificationManager = notificationManager,
                syncEngine = syncEngine,
                connectivityObserver = connectivityObserver,
                roomSyncManager = roomSyncManager
            ) as T
        }
    }
}
