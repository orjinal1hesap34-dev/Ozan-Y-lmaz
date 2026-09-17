package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.core.location.CourierLocationService
import com.example.presentation.CourierUiState
import com.example.presentation.CourierViewModel
import com.example.presentation.cash.CashScreen
import com.example.presentation.components.CourierBottomNavigation
import com.example.presentation.components.CourierTopBar
import com.example.presentation.components.NavTab
import com.example.presentation.delivery.DeliveryDetailDialog
import com.example.presentation.dialogs.DayEndReportDialog
import com.example.presentation.dialogs.RestaurantPaymentDialog
import com.example.presentation.dialogs.SimulationDialog
import com.example.presentation.home.HomeScreen
import com.example.presentation.profile.ProfileScreen
import com.example.presentation.route.RouteScreen
import com.example.presentation.stats.WeeklyStatsScreen
import com.example.presentation.tasks.TasksScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SenKuryeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CourierViewModel by viewModels {
        val app = application as SenKuryeApp
        CourierViewModel.Factory(
            repository = app.repository,
            notificationManager = app.notificationManager,
            syncEngine = app.syncEngine,
            connectivityObserver = app.connectivityObserver,
            roomSyncManager = app.roomSyncManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SenKuryeTheme {
                MainAppScreen(
                    viewModel = viewModel,
                    onStartLocationService = { startCourierLocationService() },
                    onStopLocationService = { stopCourierLocationService() }
                )
            }
        }
    }

    private fun startCourierLocationService() {
        val hasLocationPerm = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPerm) {
            Log.d("MainActivity", "Deferred starting location service until permission granted")
            return
        }

        try {
            val serviceIntent = Intent(this, CourierLocationService::class.java).apply {
                action = CourierLocationService.ACTION_START
            }
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Could not start CourierLocationService: ${e.message}")
        }
    }

    private fun stopCourierLocationService() {
        try {
            val serviceIntent = Intent(this, CourierLocationService::class.java).apply {
                action = CourierLocationService.ACTION_STOP
            }
            startService(serviceIntent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Could not stop CourierLocationService: ${e.message}")
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: CourierViewModel,
    onStartLocationService: () -> Unit,
    onStopLocationService: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(NavTab.HOME) }

    // Dialog controllers
    var showRestaurantPaymentDialog by remember { mutableStateOf(false) }
    var showDayEndReportDialog by remember { mutableStateOf(false) }
    var showSimulationDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Request necessary runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted && uiState.courier.isOnline) {
            onStartLocationService()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // Sync location service with online status
    LaunchedEffect(uiState.courier.isOnline) {
        if (uiState.courier.isOnline) {
            onStartLocationService()
        } else {
            onStopLocationService()
        }
    }

    // Show toast / snackbar info message
    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearInfoMessage()
        }
    }

    Scaffold(
        topBar = {
            CourierTopBar(
                courierName = "${uiState.courier.name} ${uiState.courier.surname}",
                isOnline = uiState.courier.isOnline,
                isOffline = uiState.isOffline,
                isSyncing = uiState.isSyncing,
                pendingSyncCount = uiState.pendingSyncCount,
                onToggleOnline = { viewModel.toggleOnlineStatus() },
                onOpenSimulations = { showSimulationDialog = true },
                onTriggerSync = { viewModel.triggerSyncNow() }
            )
        },
        bottomBar = {
            CourierBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                activeCount = uiState.activeOrders.size
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.HOME -> HomeScreen(
                    uiState = uiState,
                    onNavigateTab = { currentTab = it },
                    onSelectOrder = { viewModel.selectOrder(it) },
                    onOpenRestaurantPayment = { showRestaurantPaymentDialog = true },
                    onOpenDayEnd = { showDayEndReportDialog = true }
                )
                NavTab.TASKS -> TasksScreen(
                    uiState = uiState,
                    onSelectOrder = { viewModel.selectOrder(it) },
                    onRestoranaUlastim = { viewModel.markRestoranaUlastim(it) },
                    onPaketiAldim = { viewModel.markPaketiAldim(it) },
                    onTeslimataBasla = { viewModel.markTeslimataBasla(it) },
                    onTeslimatiTamamla = { viewModel.markTeslimatiTamamla(it) }
                )
                NavTab.ROUTE -> RouteScreen(
                    uiState = uiState,
                    onStopCompleted = { /* Handled */ }
                )
                NavTab.STATS -> WeeklyStatsScreen(
                    uiState = uiState,
                    onNavigateTab = { currentTab = it }
                )
                NavTab.CASH -> CashScreen(
                    uiState = uiState,
                    onOpenRestaurantPayment = { showRestaurantPaymentDialog = true },
                    onOpenDayEndReport = { showDayEndReportDialog = true }
                )
                NavTab.PROFILE -> ProfileScreen(
                    uiState = uiState,
                    onTriggerSync = { viewModel.triggerSyncNow() }
                )
            }
        }
    }

    // Modal dialogs
    uiState.selectedOrder?.let { order ->
        DeliveryDetailDialog(
            order = order,
            onDismiss = { viewModel.selectOrder(null) },
            onRestoranaUlastim = { viewModel.markRestoranaUlastim(it) },
            onPaketiAldim = { viewModel.markPaketiAldim(it) },
            onTeslimataBasla = { viewModel.markTeslimataBasla(it) },
            onTeslimatiTamamla = { viewModel.markTeslimatiTamamla(it) },
            onChangePaymentMethod = { orderId, newMethod, reason ->
                viewModel.changePaymentMethod(orderId, newMethod, reason)
            }
        )
    }

    if (showRestaurantPaymentDialog) {
        RestaurantPaymentDialog(
            restaurants = uiState.partnerRestaurants,
            currentCashBalance = uiState.dayEndSummary.expectedPhysicalCash,
            onDismiss = { showRestaurantPaymentDialog = false },
            onConfirmPayment = { restaurant, amount ->
                viewModel.payToRestaurant(restaurant, amount)
            }
        )
    }

    if (showDayEndReportDialog) {
        DayEndReportDialog(
            summary = uiState.dayEndSummary,
            onDismiss = { showDayEndReportDialog = false }
        )
    }

    if (showSimulationDialog) {
        SimulationDialog(
            onDismiss = { showSimulationDialog = false },
            onSimulateNewAssignment = { viewModel.triggerSimulatedAssignment() },
            onSimulateRouteOptimization = { viewModel.triggerSimulatedRouteOptimization() },
            onSimulateSync = { viewModel.triggerSyncNow() },
            onToggleOffline = { viewModel.toggleOfflineSimulation() },
            isOffline = uiState.isOffline
        )
    }
}

