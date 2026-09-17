package com.example.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.presentation.CourierUiState
import com.example.presentation.components.NavTab
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    uiState: CourierUiState,
    onNavigateTab: (NavTab) -> Unit,
    onSelectOrder: (Order) -> Unit,
    onOpenRestaurantPayment: () -> Unit,
    onOpenDayEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val courier = uiState.courier
    val summary = uiState.dayEndSummary
    val activeOrders = uiState.activeOrders
    val routePlan = uiState.routePlan

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // 1. TODAY'S PERFORMANCE SUMMARY (Section 5)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BUGÜNÜN ÖZETİ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${summary.totalDeliveries} Teslimat",
                                color = PrimaryBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Deliveries row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(title = "Tamamlanan", value = "${summary.completedDeliveries}", color = StatusSuccess)
                        StatItem(title = "Bekleyen", value = "${summary.pendingDeliveries}", color = StatusWarning)
                        StatItem(title = "Kazanç", value = "₺${summary.totalEarnings.toInt()}", color = PrimaryBlue)
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = CardBorderDark
                    )

                    // Financial row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Fiziksel Nakit", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text(
                                text = "₺${summary.expectedPhysicalCash.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CashGold
                                )
                            )
                        }
                        Column {
                            Text(text = "Kart Tahsilatı", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text(
                                text = "₺${summary.cardEarnings.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            )
                        }
                        Column {
                            Text(text = "Online Sipariş", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text(
                                text = "₺${summary.onlineEarnings.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryTeal
                                )
                            )
                        }
                    }
                }
            }
        }

        // 1.5. WEEKLY STATS & COMPLETION RATE BANNER (Recharts Data Visualization)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SecondaryTeal.copy(alpha = 0.4f))),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTab(NavTab.STATS) }
                    .testTag("home_weekly_stats_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SecondaryTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Haftalık İstatistikler",
                            tint = SecondaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Haftalık Başarı: %95.7",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = StatusSuccess.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "HEDEF AŞILDI",
                                    color = StatusSuccess,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "180/188 Teslimat • Günlük hacim ve tamamlama grafiği",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Detay",
                        tint = SecondaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. ACTIVE ROUTE / ACTIVE DISPATCH (Section 5 & 40)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue.copy(alpha = 0.5f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AKTİF ROTA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        // Backend Package Limit Badge: e.g. "3 / 5 Paket"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceVariantDark
                        ) {
                            Text(
                                text = "${activeOrders.size} / ${courier.packageLimit} Paket",
                                color = if (activeOrders.size >= courier.packageLimit) StatusWarning else PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Route metrics (Packages, Stops, Distance, Time)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariantDark)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RouteMetricBadge(icon = "📦", label = "${activeOrders.size} Paket")
                        RouteMetricBadge(icon = "📍", label = "${routePlan?.stops?.size ?: 4} Durak")
                        RouteMetricBadge(icon = "🚗", label = "${routePlan?.totalDistanceKm ?: 8.4} km")
                        RouteMetricBadge(icon = "⏱", label = "${routePlan?.estimatedDurationMinutes ?: 24} dk")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stops Preview (Optimized sequence by Route Engine)
                    val displayStops = routePlan?.stops?.take(4) ?: emptyList()
                    if (displayStops.isNotEmpty()) {
                        displayStops.forEachIndexed { index, stop ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (stop.type == StopType.PICKUP) PrimaryBlue.copy(alpha = 0.2f) else CashGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (stop.type == StopType.PICKUP) PrimaryBlue else CashGold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stop.locationName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "→ Paket ${stop.packageCode}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "1. Köfteci Yusuf → Paket #SK1001\n2. Burger Lab → Paket #SK1002\n3. Müşteri: Ahmet Kaya → Paket #SK1001\n4. Müşteri: Mehmet Demir → Paket #SK1002",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 22.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onNavigateTab(NavTab.ROUTE) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("see_route_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(imageVector = Icons.Default.AltRoute, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ROTAYI GÖR",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }
                }
            }
        }

        // 3. QUICK OPERATIONAL ACTIONS
        item {
            Text(
                text = "HIZLI İŞLEMLER",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Partner Restaurant Cash Payment button
                QuickActionButton(
                    title = "Restorana\nÖdeme Yap",
                    icon = Icons.Default.Payments,
                    color = CashGold,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenRestaurantPayment
                )

                // Day End Reconciliation button
                QuickActionButton(
                    title = "Gün Sonu\nMutabakatı",
                    icon = Icons.Default.Summarize,
                    color = SecondaryTeal,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenDayEnd
                )

                // View All Tasks button
                QuickActionButton(
                    title = "Aktif\nGörevler",
                    icon = Icons.Default.Inventory,
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(NavTab.TASKS) }
                )
            }
        }

        // 4. ACTIVE ORDER CARDS (List of packages on the bike/car)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TAŞINAN PAKETLER (${activeOrders.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Tümünü Gör",
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateTab(NavTab.TASKS) }
                )
            }
        }

        items(activeOrders.size) { index ->
            val order = activeOrders[index]
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectOrder(order) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${order.externalOrderId}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (order.paymentMethod == PaymentMethod.CASH) CashGold.copy(alpha = 0.2f) else PrimaryBlue.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = order.paymentMethod.title,
                                    color = if (order.paymentMethod == PaymentMethod.CASH) CashGold else PrimaryBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Restoran: ${order.restaurantName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 1
                        )
                        Text(
                            text = "Müşteri: ${order.customerName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                            maxLines = 1
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₺${order.amount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(title: String, value: String, color: Color) {
    Column {
        Text(text = title, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
private fun RouteMetricBadge(icon: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
