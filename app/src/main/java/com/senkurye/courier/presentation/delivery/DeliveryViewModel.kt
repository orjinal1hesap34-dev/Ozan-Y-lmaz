package com.senkurye.courier.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.senkurye.courier.core.network.ConnectivityObserver
import com.senkurye.courier.core.sync.RoomSyncManager
import com.senkurye.courier.domain.model.*
import com.senkurye.courier.domain.repository.AssignmentRepository
import com.senkurye.courier.domain.repository.OrderRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for the Delivery Screen.
 * Encapsulates pending assignments in route sequence, currently active task,
 * connectivity status (offline badge indicator), and user interaction messages.
 */
data class DeliveryUiState(
    val isLoading: Boolean = false,
    val courierId: String = "c_101",
    val pendingAssignments: List<AssignmentDetail> = emptyList(),
    val currentActiveAssignment: AssignmentDetail? = null,
    val selectedAssignment: AssignmentDetail? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val isSyncing: Boolean = false
) {
    val pendingCount: Int get() = pendingAssignments.size
    val hasPendingDeliveries: Boolean get() = pendingAssignments.isNotEmpty()
}

/**
 * ViewModel for the Delivery Screen.
 * Uses AssignmentRepository and OrderRepository as single sources of truth to fetch
 * pending assignments, manage route progress, and execute sequential order status updates.
 * Exposes the immutable UI state via StateFlow, including real-time network connectivity
 * and Room database synchronization status.
 */
class DeliveryViewModel(
    private val assignmentRepository: AssignmentRepository,
    private val orderRepository: OrderRepository,
    initialCourierId: String = "c_101",
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val connectivityObserver: ConnectivityObserver? = null,
    private val roomSyncManager: RoomSyncManager? = null
) : ViewModel() {

    private val _courierId = MutableStateFlow(initialCourierId)
    private val _isLoading = MutableStateFlow(false)
    private val _selectedAssignment = MutableStateFlow<AssignmentDetail?>(null)
    private val _infoMessage = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Observe connectivity if observer provided
    private val _isOfflineFlow: Flow<Boolean> = connectivityObserver?.observe()?.map {
        !it.isAvailable
    } ?: flowOf(false)

    // Observe Room sync state if manager provided
    private val _isSyncingFlow: Flow<Boolean> = roomSyncManager?.syncState?.map {
        it.isSyncing
    } ?: flowOf(false)

    // Reactive flow of active assignments for the current courier
    private val _activeAssignmentsFlow = _courierId.flatMapLatest { id ->
        assignmentRepository.getActiveAssignmentsWithOrderFlow(id)
    }

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<DeliveryUiState> = combine(
        _activeAssignmentsFlow,
        _isLoading,
        _selectedAssignment,
        _infoMessage,
        _errorMessage,
        _courierId,
        _isOfflineFlow,
        _isSyncingFlow
    ) { args: Array<Any?> ->
        val assignments = args[0] as? List<AssignmentDetail> ?: emptyList()
        val loading = args[1] as? Boolean ?: false
        val selected = args[2] as? AssignmentDetail
        val info = args[3] as? String
        val error = args[4] as? String
        val cid = args[5] as? String ?: initialCourierId
        val offline = args[6] as? Boolean ?: false
        val syncing = args[7] as? Boolean ?: false

        // Sort assignments by sequence number
        val sortedAssignments = assignments.sortedBy { it.assignment.sequenceNumber }
        val activeAssignment = sortedAssignments.firstOrNull {
            it.assignment.status != AssignmentStatus.DELIVERED &&
            it.assignment.status != AssignmentStatus.CANCELLED
        }

        DeliveryUiState(
            isLoading = loading,
            courierId = cid,
            pendingAssignments = sortedAssignments,
            currentActiveAssignment = activeAssignment,
            selectedAssignment = selected ?: activeAssignment,
            infoMessage = info,
            errorMessage = error,
            isOffline = offline,
            isSyncing = syncing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DeliveryUiState(
            isLoading = true,
            courierId = initialCourierId,
            isOffline = connectivityObserver?.isConnected?.not() ?: false
        )
    )

    fun setCourierId(id: String) {
        _courierId.value = id
    }

    fun selectAssignment(detail: AssignmentDetail?) {
        _selectedAssignment.value = detail
    }

    fun clearMessages() {
        _infoMessage.value = null
        _errorMessage.value = null
    }

    /**
     * Updates the status of an order through the OrderRepository,
     * validating the state machine transition.
     */
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus, assignmentId: String? = null) {
        viewModelScope.launch(dispatcher) {
            _isLoading.value = true
            try {
                val success = orderRepository.updateOrderStatus(orderId, newStatus)
                if (success) {
                    if (assignmentId != null) {
                        // Map OrderStatus to corresponding AssignmentStatus if applicable
                        when (newStatus) {
                            OrderStatus.AT_RESTAURANT -> assignmentRepository.updateAssignmentStatus(assignmentId, AssignmentStatus.AT_RESTAURANT)
                            OrderStatus.PICKED_UP -> assignmentRepository.markPickedUp(assignmentId)
                            OrderStatus.GOING_TO_CUSTOMER -> assignmentRepository.updateAssignmentStatus(assignmentId, AssignmentStatus.ON_ROUTE)
                            OrderStatus.DELIVERED -> assignmentRepository.markDelivered(assignmentId)
                            OrderStatus.CANCELLED -> assignmentRepository.cancelAssignment(assignmentId)
                            else -> {}
                        }
                    }
                    _infoMessage.value = "Sipariş durumu güncellendi: ${newStatus.title}"
                } else {
                    _errorMessage.value = "Durum güncellemesi başarısız: Geçersiz durum geçişi."
                }
            } catch (e: Exception) {
                _errorMessage.value = "İşlem sırasında hata oluştu: ${e.localizedMessage ?: "Bilinmeyen hata"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Step 1: Courier arrived at the restaurant.
     */
    fun markArrivedAtRestaurant(orderId: String, assignmentId: String) {
        viewModelScope.launch(dispatcher) {
            _isLoading.value = true
            try {
                val success = orderRepository.updateOrderStatus(orderId, OrderStatus.AT_RESTAURANT)
                if (success) {
                    assignmentRepository.updateAssignmentStatus(assignmentId, AssignmentStatus.AT_RESTAURANT)
                    _infoMessage.value = "Restorana ulaşıldı. Sipariş bekleniyor."
                } else {
                    _errorMessage.value = "Restorana varış durumu kaydedilemedi."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Step 2: Courier picked up package from restaurant.
     */
    fun markPackagePickedUp(orderId: String, assignmentId: String) {
        viewModelScope.launch(dispatcher) {
            _isLoading.value = true
            try {
                val success = orderRepository.updateOrderStatus(orderId, OrderStatus.PICKED_UP)
                if (success) {
                    assignmentRepository.markPickedUp(assignmentId)
                    _infoMessage.value = "Paket teslim alındı. Çantaya yerleştirildi."
                } else {
                    _errorMessage.value = "Paket alma durumu güncellenemedi."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Step 3: Courier started traveling towards the customer.
     */
    fun markStartDelivery(orderId: String, assignmentId: String) {
        viewModelScope.launch(dispatcher) {
            _isLoading.value = true
            try {
                val success = orderRepository.updateOrderStatus(orderId, OrderStatus.GOING_TO_CUSTOMER)
                if (success) {
                    assignmentRepository.updateAssignmentStatus(assignmentId, AssignmentStatus.ON_ROUTE)
                    _infoMessage.value = "Müşteriye doğru yola çıkıldı."
                } else {
                    _errorMessage.value = "Teslimat rotası başlatılamadı."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Step 4: Finalize delivery at customer address.
     */
    fun completeDelivery(
        orderId: String,
        assignmentId: String,
        paymentMethod: PaymentMethod? = null,
        collectedAmount: Double? = null,
        notes: String? = null
    ) {
        viewModelScope.launch(dispatcher) {
            _isLoading.value = true
            try {
                if (paymentMethod != null) {
                    orderRepository.updateOrderPaymentMethod(orderId, paymentMethod)
                }
                val success = orderRepository.updateOrderStatus(orderId, OrderStatus.DELIVERED)
                if (success) {
                    assignmentRepository.markDelivered(assignmentId)
                    _infoMessage.value = "Sipariş başarıyla teslim edildi!"
                    _selectedAssignment.value = null
                } else {
                    _errorMessage.value = "Teslimat tamamlanamadı."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Change payment method dynamically at the door if requested by customer.
     */
    fun changePaymentMethod(orderId: String, newMethod: PaymentMethod) {
        viewModelScope.launch(dispatcher) {
            try {
                orderRepository.updateOrderPaymentMethod(orderId, newMethod)
                _infoMessage.value = "Ödeme yöntemi güncellendi: ${newMethod.title}"
            } catch (e: Exception) {
                _errorMessage.value = "Ödeme yöntemi güncellenemedi."
            }
        }
    }

    /**
     * ViewModel Factory for instantiating DeliveryViewModel.
     */
    class Factory(
        private val assignmentRepository: AssignmentRepository,
        private val orderRepository: OrderRepository,
        private val courierId: String = "c_101",
        private val connectivityObserver: ConnectivityObserver? = null,
        private val roomSyncManager: RoomSyncManager? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DeliveryViewModel::class.java)) {
                return DeliveryViewModel(
                    assignmentRepository = assignmentRepository,
                    orderRepository = orderRepository,
                    initialCourierId = courierId,
                    connectivityObserver = connectivityObserver,
                    roomSyncManager = roomSyncManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
