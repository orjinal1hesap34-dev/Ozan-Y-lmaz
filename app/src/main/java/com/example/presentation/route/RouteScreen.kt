package com.example.presentation.route

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.location.CourierLocationService
import com.example.core.navigation.CourierNavigationManager
import com.example.core.navigation.NavDebugData
import com.example.domain.model.RoutePlan
import com.example.domain.model.RouteStop
import com.example.domain.model.StopType
import com.example.presentation.CourierUiState
import com.example.ui.theme.*

/**
 * RouteScreen: Displays courier delivery route with Google Maps SDK integration,
 * real-time location tracking, sequence of pickup/dropoff stops, and direct navigation triggers.
 */
@Composable
fun RouteScreen(
    uiState: CourierUiState,
    onStopCompleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val routePlan = uiState.routePlan
    val stops = routePlan?.stops ?: emptyList()
    val courier = uiState.courier

    var isMapExpanded by remember { mutableStateOf(false) }
    var selectedStop by remember { mutableStateOf<RouteStop?>(null) }
    var isTrackingActive by remember { mutableStateOf(false) }

    // Navigation debug & error feedback state
    var lastNavDebug by remember { mutableStateOf<NavDebugData?>(null) }
    var navErrorMessage by remember { mutableStateOf<String?>(null) }

    // The immediate next active stop in the sequence (order of stops)
    val nextStop = remember(stops) { stops.firstOrNull { !it.isCompleted } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // --- 1. GOOGLE MAPS SDK INTEGRATION VIEW ---
        val mapHeightModifier = if (isMapExpanded) {
            Modifier
                .fillMaxWidth()
                .weight(1f)
        } else {
            Modifier
                .fillMaxWidth()
                .height(260.dp)
        }

        Box(
            modifier = mapHeightModifier
                .padding(if (isMapExpanded) 0.dp else 12.dp)
        ) {
            CourierGoogleMapView(
                courier = courier,
                routePlan = routePlan,
                stops = stops,
                selectedStop = selectedStop,
                onSelectStop = { stop ->
                    selectedStop = stop
                },
                isExpanded = isMapExpanded,
                onToggleExpand = { isMapExpanded = !isMapExpanded },
                modifier = Modifier.fillMaxSize()
            )
        }

        // When map is in full screen mode, show compact bottom HUD card
        if (isMapExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                color = SurfaceDark,
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sıradaki: ${nextStop?.locationName ?: "Tüm Teslimatlar Tamamlandı"}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = nextStop?.address ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = {
                                val target = nextStop ?: selectedStop
                                CourierNavigationManager.startNavigation(
                                    context = context,
                                    courierLat = courier.latitude,
                                    courierLng = courier.longitude,
                                    targetStop = target,
                                    onError = { err ->
                                        navErrorMessage = err
                                    },
                                    onDebugLogged = { debugData ->
                                        lastNavDebug = debugData
                                        navErrorMessage = null
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Navigasyon", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // --- 2. QUICK ACTIONS & NAVIGATION LAUNCHER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Launch Turn-by-Turn Navigation in Google Maps using exact target stop LAT/LONG
                Button(
                    onClick = {
                        val target = nextStop ?: selectedStop
                        CourierNavigationManager.startNavigation(
                            context = context,
                            courierLat = courier.latitude,
                            courierLng = courier.longitude,
                            targetStop = target,
                            onError = { err ->
                                navErrorMessage = err
                            },
                            onDebugLogged = { debugData ->
                                lastNavDebug = debugData
                                navErrorMessage = null
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("google_maps_nav_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Navigasyonu Başlat",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 12.5.sp
                    )
                }

                // Live GPS Tracking Toggle Service
                OutlinedButton(
                    onClick = {
                        isTrackingActive = !isTrackingActive
                        toggleLocationTrackingService(context, isTrackingActive)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_toggle_gps_tracking"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isTrackingActive) StatusSuccess else TextPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = SolidColor(if (isTrackingActive) StatusSuccess else CardBorderDark)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isTrackingActive) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                        contentDescription = null,
                        tint = if (isTrackingActive) StatusSuccess else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTrackingActive) "GPS Aktif" else "GPS Başlat",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            // --- ERROR BANNER (Requirement 10: Invalid/missing coordinates) ---
            navErrorMessage?.let { errMsg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("nav_error_banner"),
                    shape = RoundedCornerShape(8.dp),
                    color = StatusError.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, StatusError)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusError, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errMsg,
                                color = StatusError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { navErrorMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = StatusError, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // --- DEBUG DISPLAY CARD (Requirement 9: CURRENT LOCATION, DESTINATION, LATITUDE, LONGITUDE) ---
            lastNavDebug?.let { debugData ->
                NavDebugCard(
                    debugData = debugData,
                    onDismiss = { lastNavDebug = null }
                )
            }

            // Route status & summary banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = SurfaceVariantDark
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Route, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Toplam: ${routePlan?.totalDistanceKm ?: 8.4} km • ~${routePlan?.estimatedDurationMinutes ?: 24} dk",
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${stops.count { it.isCompleted }}/${stops.size} Durak Tamam",
                        color = StatusSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- 3. DELIVERY STOPS LIST ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
            ) {
                itemsIndexed(stops) { index, stop ->
                    val isSelected = selectedStop?.id == stop.id
                    StopCard(
                        stop = stop,
                        stopIndex = index + 1,
                        isSelected = isSelected,
                        onClick = { selectedStop = stop },
                        onNavigate = {
                            CourierNavigationManager.startNavigation(
                                context = context,
                                courierLat = courier.latitude,
                                courierLng = courier.longitude,
                                targetStop = stop,
                                onError = { err ->
                                    navErrorMessage = err
                                },
                                onDebugLogged = { debugData ->
                                    lastNavDebug = debugData
                                    navErrorMessage = null
                                }
                            )
                        },
                        onComplete = {
                            onStopCompleted(stop.id)
                        }
                    )
                }
            }
        }
    }
}

/**
 * On-screen diagnostic debug card providing visual proof that exact LATITUDE & LONGITUDE
 * coordinates are communicated to Google Maps without performing geocoding or address search.
 * Meets Requirement 9 (CURRENT LOCATION, DESTINATION, LATITUDE, LONGITUDE).
 */
@Composable
private fun NavDebugCard(
    debugData: NavDebugData,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("nav_debug_info_card"),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NAVİGASYON KOORDİNAT BİLGİSİ (DEBUG)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = PrimaryBlue,
                        letterSpacing = 0.5.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4 Required Debug Fields
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                DebugFieldRow(label = "CURRENT LOCATION", value = debugData.currentLocation, tag = "debug_current_location")
                DebugFieldRow(label = "DESTINATION", value = debugData.destination, tag = "debug_destination")
                DebugFieldRow(label = "LATITUDE", value = debugData.latitude, tag = "debug_latitude")
                DebugFieldRow(label = "LONGITUDE", value = debugData.longitude, tag = "debug_longitude")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hedefe Lat/Long koordinatları iletildi • Adres metni aranmadı",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    text = if (debugData.isGoogleMapsDirect) "Google Maps Aktif" else "Chooser Açıldı",
                    fontSize = 10.sp,
                    color = CashGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DebugFieldRow(label: String, value: String, tag: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun StopCard(
    stop: RouteStop,
    stopIndex: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onNavigate: () -> Unit,
    onComplete: () -> Unit
) {
    val isPickup = stop.type == StopType.PICKUP

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceVariantDark else SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isSelected) PrimaryBlue else CardBorderDark)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("stop_card_${stop.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sequence number badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                stop.isCompleted -> StatusSuccess.copy(alpha = 0.2f)
                                isPickup -> Color(0xFFF57C00).copy(alpha = 0.2f)
                                else -> Color(0xFFD32F2F).copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (stop.isCompleted) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            text = "$stopIndex",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPickup) Color(0xFFF57C00) else Color(0xFFEF5350)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when {
                                stop.isCompleted -> "✓ TAMAMLANDI"
                                isPickup -> "📦 RESTORAN (Alım)"
                                else -> "📍 MÜŞTERİ (Teslimat)"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                stop.isCompleted -> StatusSuccess
                                isPickup -> Color(0xFFFFA726)
                                else -> Color(0xFFEF5350)
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stop.packageCode,
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stop.locationName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (stop.isCompleted) TextMuted else TextPrimary,
                            fontSize = 13.5.sp
                        )
                    )

                    Text(
                        text = stop.address,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 2
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "~${stop.estimatedArrivalMinutes} dk",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stop.isCompleted) TextMuted else TextPrimary
                    )
                    Text(
                        text = "Tahmini",
                        fontSize = 9.5.sp,
                        color = TextMuted
                    )
                }
            }

            // Expanded Action Row
            if (!stop.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = CardBorderDark.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Navigate Button
                    TextButton(
                        onClick = onNavigate,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Haritada Yol Tarifi", color = PrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Mark as completed button
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tamamla", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun toggleLocationTrackingService(context: Context, start: Boolean) {
    val intent = Intent(context, CourierLocationService::class.java).apply {
        action = if (start) CourierLocationService.ACTION_START else CourierLocationService.ACTION_STOP
    }
    try {
        if (start) {
            val hasLocationPerm = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasLocationPerm) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.startService(intent)
        }
    } catch (e: Exception) {
        // Handle background service restrictions gracefully
    }
}

private fun openGoogleMapsNavigation(context: Context, lat: Double, lng: Double) {
    val stop = RouteStop(
        id = "direct_nav",
        routeId = "route_direct",
        stopOrder = 1,
        type = StopType.DROPOFF,
        orderId = "order_direct",
        packageCode = "NAV",
        locationName = "Hedef Teslimat Noktası",
        address = "$lat, $lng",
        latitude = lat,
        longitude = lng,
        isCompleted = false,
        estimatedArrivalMinutes = 10
    )
    CourierNavigationManager.startNavigation(
        context = context,
        courierLat = 41.0082,
        courierLng = 29.0050,
        targetStop = stop
    )
}
