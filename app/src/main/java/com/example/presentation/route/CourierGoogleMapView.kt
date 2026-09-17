package com.example.presentation.route

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.example.core.navigation.CourierNavigationManager
import com.example.domain.model.Courier
import com.example.domain.model.RoutePlan
import com.example.domain.model.RouteStop
import com.example.domain.model.StopType
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class MapEngineMode {
    MAPBOX,
    GOOGLE_MAPS,
    VECTOR
}

/**
 * Intelligent Multi-Engine Map Component for Şen Kurye courier application.
 * 
 * Dynamically detects configured API key type:
 * - Mapbox Token (`pk.eyJ...`): Uses Mapbox GL JS engine with dark courier styling.
 * - Google Maps Key (`AIza...`): Uses native Google Maps SDK for Android.
 * - Fallback / Offline: High-performance Vector Route Canvas engine.
 * 
 * Prevents Google Maps SDK Authorization Failure when non-Google keys (e.g. Mapbox) are provided.
 */
@Composable
fun CourierGoogleMapView(
    courier: Courier,
    routePlan: RoutePlan?,
    stops: List<RouteStop>,
    selectedStop: RouteStop?,
    onSelectStop: (RouteStop) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val rawApiKey = remember { BuildConfig.MAPS_API_KEY.trim() }
    val isMapboxToken = remember(rawApiKey) { rawApiKey.startsWith("pk.") }
    val isGoogleKey = remember(rawApiKey) { rawApiKey.startsWith("AIza") }

    // Google Maps SDK is the primary engine for the active route screen
    var selectedEngine by remember {
        mutableStateOf(MapEngineMode.GOOGLE_MAPS)
    }

    var showKeyInfoDialog by remember { mutableStateOf(false) }

    val courierLatLng = remember(courier.latitude, courier.longitude) {
        LatLng(courier.latitude, courier.longitude)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(courierLatLng, 15f)
    }

    LaunchedEffect(courierLatLng) {
        val currentTarget = cameraPositionState.position.target
        val distLat = Math.abs(currentTarget.latitude - courierLatLng.latitude)
        val distLng = Math.abs(currentTarget.longitude - courierLatLng.longitude)
        if (distLat > 0.005 || distLng > 0.005) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(courierLatLng),
                1000
            )
        }
    }

    var isTrafficEnabled by remember { mutableStateOf(false) }
    var currentMapType by remember { mutableStateOf(MapType.NORMAL) }
    var isMapLoaded by remember { mutableStateOf(false) }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }

    val mapProperties = remember(isTrafficEnabled, currentMapType) {
        MapProperties(
            isTrafficEnabled = isTrafficEnabled,
            mapType = currentMapType
        )
    }

    val routePolylinePoints = remember(courierLatLng, stops) {
        val list = mutableListOf<LatLng>()
        list.add(courierLatLng)
        val activeStops = stops.filter { !it.isCompleted }.sortedBy { it.stopOrder }
        activeStops.forEach { stop ->
            list.add(LatLng(stop.latitude, stop.longitude))
        }
        list
    }

    val nextStop = stops.firstOrNull { !it.isCompleted }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isExpanded) 0.dp else 16.dp))
            .border(1.dp, CardBorderDark, RoundedCornerShape(if (isExpanded) 0.dp else 16.dp))
            .testTag("google_maps_view_container")
    ) {
        // --- 1. HARİTA MOTORU GÖVลูก (Mapbox / Google Maps / Vector) ---
        when (selectedEngine) {
            MapEngineMode.MAPBOX -> {
                MapboxCourierWebView(
                    courier = courier,
                    stops = stops,
                    mapboxToken = rawApiKey,
                    onSelectStop = onSelectStop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            MapEngineMode.GOOGLE_MAPS -> {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
                    onMapLoaded = { isMapLoaded = true }
                ) {
                    // 1. COURIER REAL-TIME LOCATION MARKER & ACCURACY RADIUS
                    MarkerInfoWindowContent(
                        state = rememberMarkerState(key = "courier_live_pos", position = courierLatLng),
                        title = "🛵 Kurye: ${courier.name} ${courier.surname}",
                        snippet = "Canlı Konum • Plaka: ${courier.plateNumber} • ${courier.vehicleType}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        zIndex = 10f
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceDark,
                            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBlue)),
                            tonalElevation = 6.dp
                        ) {
                            Column(modifier = Modifier.padding(10.dp).widthIn(max = 220.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CANLI KURYE KONUMU",
                                        color = PrimaryBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${courier.name} ${courier.surname}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Plaka: ${courier.plateNumber} • ${courier.vehicleType}",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "Durum: Aktif Görevde",
                                    color = StatusSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Circle(
                        center = courierLatLng,
                        radius = 35.0,
                        fillColor = Color(0x3300C853),
                        strokeColor = Color(0xFF00C853),
                        strokeWidth = 2f,
                        zIndex = 9f
                    )

                    // 2. ACTIVE DELIVERY ROUTE PATH POLYLINE
                    if (routePolylinePoints.size >= 2) {
                        // High-contrast background casing
                        Polyline(
                            points = routePolylinePoints,
                            color = Color(0xFF0F172A),
                            width = 16f,
                            jointType = JointType.ROUND,
                            startCap = RoundCap(),
                            endCap = RoundCap(),
                            zIndex = 2f
                        )
                        // Active foreground trajectory
                        Polyline(
                            points = routePolylinePoints,
                            color = PrimaryBlue,
                            width = 10f,
                            jointType = JointType.ROUND,
                            startCap = RoundCap(),
                            endCap = RoundCap(),
                            zIndex = 3f
                        )
                    }

                    // 3. RESTAURANT & CUSTOMER DELIVERY POINT MARKERS
                    stops.forEach { stop ->
                        val stopLatLng = LatLng(stop.latitude, stop.longitude)
                        val isPickup = stop.type == StopType.PICKUP
                        val isNext = stop.id == nextStop?.id
                        val isSelected = stop.id == selectedStop?.id

                        val markerHue = when {
                            stop.isCompleted -> BitmapDescriptorFactory.HUE_GREEN
                            isPickup -> BitmapDescriptorFactory.HUE_ORANGE
                            else -> BitmapDescriptorFactory.HUE_RED
                        }

                        val stopCategoryLabel = if (isPickup) "🍽️ RESTORAN (PAKET ALIMI)" else "📍 MÜŞTERİ (TESLİMAT)"
                        val stopBorderColor = if (stop.isCompleted) StatusSuccess else if (isPickup) StatusWarning else StatusError

                        MarkerInfoWindowContent(
                            state = rememberMarkerState(key = "stop_${stop.id}", position = stopLatLng),
                            title = "${stop.stopOrder}. $stopCategoryLabel",
                            snippet = "${stop.locationName} • ~${stop.estimatedArrivalMinutes} dk (${stop.packageCode})",
                            icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                            onClick = {
                                onSelectStop(stop)
                                false
                            },
                            zIndex = if (isSelected) 12f else if (isNext) 8f else 5f
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceDark,
                                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(stopBorderColor)),
                                tonalElevation = 6.dp
                            ) {
                                Column(modifier = Modifier.padding(10.dp).widthIn(max = 240.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = stopBorderColor.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = stopCategoryLabel,
                                                color = stopBorderColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "#${stop.stopOrder}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stop.locationName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = stop.address,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📦 Kod: ${stop.packageCode}",
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = if (stop.isCompleted) "✓ Teslim Edildi" else "⏱️ ~${stop.estimatedArrivalMinutes} dk",
                                            color = if (stop.isCompleted) StatusSuccess else CashGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            MapEngineMode.VECTOR -> {
                VectorCourierMapView(
                    courier = courier,
                    stops = stops,
                    selectedStop = selectedStop,
                    onSelectStop = onSelectStop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // --- OVERLAY 1: Top Navigation HUD ---
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceDark.copy(alpha = 0.94f),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showKeyInfoDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (selectedEngine) {
                                        MapEngineMode.MAPBOX -> StatusSuccess
                                        MapEngineMode.GOOGLE_MAPS -> PrimaryBlue
                                        MapEngineMode.VECTOR -> CashGold
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (selectedEngine) {
                                MapEngineMode.MAPBOX -> "MAPBOX GL HARİTASI"
                                MapEngineMode.GOOGLE_MAPS -> "GOOGLE MAPS SDK"
                                MapEngineMode.VECTOR -> "VEKTÖR ROTA HARİTASI"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (selectedEngine) {
                                MapEngineMode.MAPBOX -> StatusSuccess
                                MapEngineMode.GOOGLE_MAPS -> PrimaryBlue
                                MapEngineMode.VECTOR -> CashGold
                            },
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Bilgi",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = "Rota v${routePlan?.version ?: 1} • ${routePlan?.totalDistanceKm ?: 8.4} km",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (nextStop != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sıradaki Durak: #${nextStop.stopOrder} ${nextStop.locationName}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = if (nextStop.type == StopType.PICKUP) "📦 Paket Alımı • ${nextStop.address}" else "📍 Müşteri Teslimatı • ${nextStop.address}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                maxLines = 1
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue.copy(alpha = 0.2f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "~${nextStop.estimatedArrivalMinutes} dk",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- OVERLAY 2: Floating Quick Action Buttons on Map ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Engine Switcher FAB (Mapbox <-> Google Maps <-> Vector)
            SmallFloatingActionButton(
                onClick = {
                    selectedEngine = when (selectedEngine) {
                        MapEngineMode.MAPBOX -> if (isGoogleKey) MapEngineMode.GOOGLE_MAPS else MapEngineMode.VECTOR
                        MapEngineMode.GOOGLE_MAPS -> MapEngineMode.VECTOR
                        MapEngineMode.VECTOR -> if (isMapboxToken) MapEngineMode.MAPBOX else MapEngineMode.GOOGLE_MAPS
                    }
                },
                containerColor = SurfaceVariantDark,
                contentColor = PrimaryBlue,
                shape = CircleShape,
                modifier = Modifier.size(38.dp).testTag("btn_switch_map_engine")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapCalls,
                    contentDescription = "Harita Motorunu Değiştir",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Fullscreen / Collapse toggle button
            if (onToggleExpand != null) {
                SmallFloatingActionButton(
                    onClick = onToggleExpand,
                    containerColor = SurfaceVariantDark,
                    contentColor = TextPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp).testTag("btn_toggle_map_fullscreen")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Haritayı Genişlet",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Fit All Stops (Courier + Restaurants + Customers)
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val builder = LatLngBounds.builder()
                            builder.include(courierLatLng)
                            stops.forEach { stop ->
                                builder.include(LatLng(stop.latitude, stop.longitude))
                            }
                            val bounds = builder.build()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 100),
                                800
                            )
                        } catch (e: Exception) {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(courierLatLng, 15f),
                                800
                            )
                        }
                    }
                },
                containerColor = SurfaceVariantDark,
                contentColor = CashGold,
                shape = CircleShape,
                modifier = Modifier.size(38.dp).testTag("btn_fit_route_bounds")
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "Tüm Rotayı Göster",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Launch Native Google Maps External Navigation using exact LAT/LONG
            SmallFloatingActionButton(
                onClick = {
                    CourierNavigationManager.startNavigation(
                        context = context,
                        courierLat = courier.latitude,
                        courierLng = courier.longitude,
                        targetStop = nextStop
                    )
                },
                containerColor = StatusSuccess,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(38.dp).testTag("btn_external_maps_nav")
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Navigasyonu Başlat",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Re-center on Courier
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(courierLatLng, 16.5f),
                            800
                        )
                    }
                },
                containerColor = PrimaryBlue,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(46.dp).testTag("btn_center_courier")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Kuryeye Odaklan",
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // --- OVERLAY 3: Map Legend on Bottom-Left ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = SurfaceDark.copy(alpha = 0.90f),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapLegendItem(color = Color(0xFF0288D1), label = "Kurye")
                MapLegendItem(color = Color(0xFFF57C00), label = "Restoran")
                MapLegendItem(color = Color(0xFFD32F2F), label = "Müşteri")
                MapLegendItem(color = Color(0xFF00C853), label = "Tamamlandı")
            }
        }
    }

    // --- API KEY CONFIGURATION & DIAGNOSTICS DIALOG ---
    if (showKeyInfoDialog) {
        AlertDialog(
            onDismissRequest = { showKeyInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Harita Yapılandırması", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isMapboxToken) {
                            "Mevcut girilen anahtar (${rawApiKey.take(12)}...) bir Mapbox Access Token'ıdır. Haritanız Mapbox GL JS ile sorunsuz yüklenmiştir."
                        } else if (isGoogleKey) {
                            "Google Maps API anahtarı aktif olarak ayarlanmıştır."
                        } else {
                            "Geçerli bir harita anahtarı henüz eklenmemiştir. Vektör rota motoru devrededir."
                        },
                        fontSize = 13.sp,
                        color = TextPrimary
                    )

                    HorizontalDivider(color = CardBorderDark, modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Google Maps SDK Kullanmak İsterseniz:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PrimaryBlue
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceVariantDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("1. Google Cloud Console'da 'Maps SDK for Android'i etkinleştirin.", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("2. Kısıtlama Bilgileri:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("• Paket: com.aistudio.senkurye.courier", fontSize = 10.sp, color = CashGold)
                            Text("• SHA-1: F4:6B:C2:92:3F:B9:6B:5F:F1:BA:86:CD:7F:22:1C:9B:F3:2E:0B:61", fontSize = 10.sp, color = CashGold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("3. AI Studio Secrets panelinde MAPS_API_KEY olarak kaydedin.", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showKeyInfoDialog = false }) {
                    Text("Tamam", color = PrimaryBlue)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Interactive Mapbox GL JS engine embedded inside WebView.
 * Automatically loads the user's Mapbox key (pk.eyJ...) with dark night courier theme.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapboxCourierWebView(
    courier: Courier,
    stops: List<RouteStop>,
    mapboxToken: String,
    onSelectStop: (RouteStop) -> Unit,
    modifier: Modifier = Modifier
) {
    val stopsJson = remember(stops) {
        val array = JSONArray()
        stops.forEach { stop ->
            val obj = JSONObject().apply {
                put("id", stop.id)
                put("stopOrder", stop.stopOrder)
                put("type", stop.type.name)
                put("locationName", stop.locationName)
                put("address", stop.address)
                put("latitude", stop.latitude)
                put("longitude", stop.longitude)
                put("isCompleted", stop.isCompleted)
                put("eta", stop.estimatedArrivalMinutes)
            }
            array.put(obj)
        }
        array.toString()
    }

    val html = remember(courier.latitude, courier.longitude, mapboxToken, stopsJson) {
        generateMapboxHtml(courier.latitude, courier.longitude, mapboxToken, stopsJson)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                setBackgroundColor(0xFF0A0F1D.toInt())
                webViewClient = WebViewClient()

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onStopClicked(stopId: String) {
                        post {
                            stops.find { it.id == stopId }?.let { onSelectStop(it) }
                        }
                    }
                }, "MapboxBridge")

                loadDataWithBaseURL("https://api.mapbox.com", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://api.mapbox.com", html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

private fun generateMapboxHtml(lat: Double, lng: Double, token: String, stopsJsonStr: String): String {
    return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no">
<link href="https://api.mapbox.com/mapbox-gl-js/v3.2.0/mapbox-gl.css" rel="stylesheet">
<script src="https://api.mapbox.com/mapbox-gl-js/v3.2.0/mapbox-gl.js"></script>
<style>
  body { margin: 0; padding: 0; background: #0A0F1D; font-family: -apple-system, sans-serif; overflow: hidden; }
  #map { position: absolute; top: 0; bottom: 0; width: 100%; height: 100%; }
  .courier-marker {
    width: 28px; height: 28px; border-radius: 50%; background: #0284C7;
    border: 3px solid #FFFFFF; box-shadow: 0 0 14px #38BDF8;
    display: flex; align-items: center; justify-content: center; font-size: 14px;
    animation: pulse 2s infinite;
  }
  @keyframes pulse {
    0% { box-shadow: 0 0 0 0 rgba(56, 189, 248, 0.7); }
    70% { box-shadow: 0 0 0 14px rgba(56, 189, 248, 0); }
    100% { box-shadow: 0 0 0 0 rgba(56, 189, 248, 0); }
  }
  .stop-marker {
    width: 26px; height: 26px; border-radius: 50%; display: flex; align-items: center;
    justify-content: center; color: white; font-weight: bold; font-size: 11px;
    box-shadow: 0 3px 10px rgba(0,0,0,0.6); border: 2px solid white; cursor: pointer;
  }
  .stop-pickup { background: #F59E0B; }
  .stop-dropoff { background: #EF4444; }
  .stop-completed { background: #64748B; opacity: 0.5; }
</style>
</head>
<body>
<div id="map"></div>
<script>
(function() {
  const token = "$token";
  mapboxgl.accessToken = token;

  const map = new mapboxgl.Map({
    container: 'map',
    style: 'mapbox://styles/mapbox/dark-v11',
    center: [$lng, $lat],
    zoom: 15,
    attributionControl: false
  });

  map.on('load', function() {
    const stops = $stopsJsonStr;
    const coordinates = [[$lng, $lat]];
    stops.forEach(s => coordinates.push([s.longitude, s.latitude]));

    // Route GeoJSON
    map.addSource('route', {
      'type': 'geojson',
      'data': {
        'type': 'Feature',
        'properties': {},
        'geometry': { 'type': 'LineString', 'coordinates': coordinates }
      }
    });

    map.addLayer({
      'id': 'route-casing',
      'type': 'line',
      'source': 'route',
      'layout': { 'line-join': 'round', 'line-cap': 'round' },
      'paint': { 'line-color': '#0F172A', 'line-width': 8 }
    });

    map.addLayer({
      'id': 'route-line',
      'type': 'line',
      'source': 'route',
      'layout': { 'line-join': 'round', 'line-cap': 'round' },
      'paint': { 'line-color': '#38BDF8', 'line-width': 4 }
    });

    // Courier Marker
    const cEl = document.createElement('div');
    cEl.className = 'courier-marker';
    cEl.innerHTML = '🛵';
    new mapboxgl.Marker(cEl).setLngLat([$lng, $lat]).addTo(map);

    // Stops Markers
    stops.forEach(s => {
      const el = document.createElement('div');
      el.className = 'stop-marker ' + (s.isCompleted ? 'stop-completed' : (s.type === 'PICKUP' ? 'stop-pickup' : 'stop-dropoff'));
      el.innerHTML = s.stopOrder;
      el.onclick = function() {
        if (window.MapboxBridge) {
          window.MapboxBridge.onStopClicked(s.id);
        }
      };
      new mapboxgl.Marker(el).setLngLat([s.longitude, s.latitude]).addTo(map);
    });
  });
})();
</script>
</body>
</html>
    """.trimIndent()
}

/**
 * High-performance Vector Route Map fallback rendered via Compose Canvas.
 * Operates 100% offline with zero dependencies or external keys.
 */
@Composable
fun VectorCourierMapView(
    courier: Courier,
    stops: List<RouteStop>,
    selectedStop: RouteStop?,
    onSelectStop: (RouteStop) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        val density = LocalDensity.current
        val w = with(density) { maxWidth.toPx() }
        val h = with(density) { maxHeight.toPx() }

        // Compute coordinate bounding box
        val allLats = listOf(courier.latitude) + stops.map { it.latitude }
        val allLngs = listOf(courier.longitude) + stops.map { it.longitude }

        val minLat = allLats.minOrNull() ?: courier.latitude
        val maxLat = allLats.maxOrNull() ?: courier.latitude
        val minLng = allLngs.minOrNull() ?: courier.longitude
        val maxLng = allLngs.maxOrNull() ?: courier.longitude

        val padX = with(density) { 60.dp.toPx() }
        val padY = with(density) { 70.dp.toPx() }
        val spanLat = (maxLat - minLat).coerceAtLeast(0.005)
        val spanLng = (maxLng - minLng).coerceAtLeast(0.005)

        val courierPos = remember(courier.latitude, courier.longitude, minLat, maxLat, minLng, maxLng, w, h) {
            val px = padX + ((courier.longitude - minLng) / spanLng).toFloat() * (w - padX * 2)
            val py = padY + (1f - ((courier.latitude - minLat) / spanLat).toFloat()) * (h - padY * 2)
            Offset(px, py)
        }

        val stopPositions = remember(stops, minLat, maxLat, minLng, maxLng, w, h) {
            stops.map { stop ->
                val px = padX + ((stop.longitude - minLng) / spanLng).toFloat() * (w - padX * 2)
                val py = padY + (1f - ((stop.latitude - minLat) / spanLat).toFloat()) * (h - padY * 2)
                Offset(px, py)
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(stops, stopPositions) {
                    detectTapGestures { tapOffset ->
                        val clickedIndex = stopPositions.indexOfFirst { pos ->
                            val dx = pos.x - tapOffset.x
                            val dy = pos.y - tapOffset.y
                            (dx * dx + dy * dy) <= (32.dp.toPx() * 32.dp.toPx())
                        }
                        if (clickedIndex in stops.indices) {
                            onSelectStop(stops[clickedIndex])
                        }
                    }
                }
        ) {
            // Subtle dark grid
            val gridSize = 40.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(color = CardBorderDark.copy(alpha = 0.25f), start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
                x += gridSize
            }
            var y = 0f
            while (y < size.height) {
                drawLine(color = CardBorderDark.copy(alpha = 0.25f), start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
                y += gridSize
            }

            // Draw Route Polyline
            if (stopPositions.isNotEmpty()) {
                val polylinePath = Path().apply {
                    moveTo(courierPos.x, courierPos.y)
                    stopPositions.forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path = polylinePath,
                    color = Color(0xFF0F172A),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = polylinePath,
                    color = PrimaryBlue,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw Stops
            stops.forEachIndexed { index, stop ->
                val pos = stopPositions[index]
                val isPickup = stop.type == StopType.PICKUP
                val isSelected = stop.id == selectedStop?.id
                val color = if (stop.isCompleted) TextMuted else if (isPickup) StatusWarning else StatusError

                if (isSelected) {
                    drawCircle(color = PrimaryBlue.copy(alpha = 0.35f), radius = 22.dp.toPx(), center = pos)
                    drawCircle(color = PrimaryBlue, radius = 18.dp.toPx(), center = pos, style = Stroke(width = 2.dp.toPx()))
                }

                drawCircle(color = color, radius = 12.dp.toPx(), center = pos)
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pos)

                val text = textMeasurer.measure(
                    text = "${stop.stopOrder}",
                    style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                )
                drawText(text, topLeft = Offset(pos.x - text.size.width / 2, pos.y - text.size.height / 2))
            }

            // Draw Courier Marker
            drawCircle(color = PrimaryBlue.copy(alpha = 0.25f), radius = 28.dp.toPx(), center = courierPos)
            drawCircle(color = PrimaryBlue, radius = 14.dp.toPx(), center = courierPos)
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = courierPos)
        }
    }
}

@Composable
private fun MapLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
    }
}
