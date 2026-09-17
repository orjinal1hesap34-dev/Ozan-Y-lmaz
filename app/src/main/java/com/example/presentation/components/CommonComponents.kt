package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CourierStatus
import com.example.domain.model.OrderStatus
import com.example.domain.model.PaymentMethod
import com.example.ui.theme.*

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Ana Sayfa", Icons.Default.Home),
    TASKS("Görevler", Icons.Default.Inventory2),
    ROUTE("Rota", Icons.Default.AltRoute),
    STATS("İstatistik", Icons.Default.BarChart),
    CASH("Kasa", Icons.Default.AccountBalanceWallet),
    PROFILE("Profil", Icons.Default.Person)
}

@Composable
fun CourierTopBar(
    courierName: String,
    isOnline: Boolean,
    onToggleOnline: () -> Unit,
    onOpenSimulations: () -> Unit,
    isOffline: Boolean = false,
    isSyncing: Boolean = false,
    pendingSyncCount: Int = 0,
    onTriggerSync: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDark,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Şen Kurye",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isOnline) StatusSuccess.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) StatusSuccess else StatusError)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isOnline) "ÇEVRİMİÇİ" else "ÇEVRİMDIŞI",
                                    color = if (isOnline) StatusSuccess else StatusError,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Offline status badge
                        if (isOffline) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .testTag("offline_badge")
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusWarning.copy(alpha = 0.2f))
                                    .border(1.dp, StatusWarning.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = "İnternet Yok (Çevrimdışı)",
                                        tint = StatusWarning,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "OFFLINE",
                                        color = StatusWarning,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else if (isSyncing) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .testTag("syncing_badge")
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SecondaryTeal.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        color = SecondaryTeal,
                                        strokeWidth = 1.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SYNC",
                                        color = SecondaryTeal,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Kurye: $courierName",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSimulations,
                        modifier = Modifier
                            .testTag("simulation_button")
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Simülasyon Paneli",
                            tint = CashGold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onToggleOnline,
                        modifier = Modifier
                            .testTag("online_toggle_button")
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOnline) StatusError.copy(alpha = 0.25f) else StatusSuccess,
                            contentColor = if (isOnline) StatusError else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (isOnline) "Mesaiyi Bitir" else "Mesaiye Başla",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Offline notice notification banner
            if (isOffline) {
                Surface(
                    color = Color(0xFF2A1B00),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("offline_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "İnternet bağlantısı yok",
                            tint = StatusWarning,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "İnternet bağlantısı yok. İşlemler yerel Room veritabanında güvenle saklanıyor.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = StatusWarning,
                                fontSize = 11.5.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (pendingSyncCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$pendingSyncCount bekleyen",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusWarning.copy(alpha = 0.35f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            } else if (isSyncing) {
                Surface(
                    color = SecondaryTeal.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("syncing_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = SecondaryTeal,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Room veritabanı sunucu ile eşitleniyor...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecondaryTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourierBottomNavigation(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    activeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab == NavTab.TASKS && activeCount > 0) {
                                Badge(
                                    containerColor = PrimaryBlue,
                                    contentColor = Color.Black
                                ) {
                                    Text(text = activeCount.toString(), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        maxLines = 1,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = SurfaceVariantDark
                )
            )
        }
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val (color, text) = when (status) {
        OrderStatus.ASSIGNED -> Pair(StatusInfo, "Atandı")
        OrderStatus.GOING_TO_RESTAURANT -> Pair(StatusWarning, "Restorana Gidiliyor")
        OrderStatus.AT_RESTAURANT -> Pair(StatusWarning, "Restoranda")
        OrderStatus.PICKED_UP -> Pair(PrimaryBlue, "Paket Alındı")
        OrderStatus.GOING_TO_CUSTOMER -> Pair(PrimaryBlue, "Müşteriye Gidiliyor")
        OrderStatus.ARRIVED -> Pair(StatusWarning, "Adrese Ulaşıldı")
        OrderStatus.DELIVERED -> Pair(StatusSuccess, "Teslim Edildi")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PaymentMethodChip(method: PaymentMethod, modifier: Modifier = Modifier) {
    val (color, text) = when (method) {
        PaymentMethod.CASH -> Pair(CashGold, "NAKİT")
        PaymentMethod.CARD -> Pair(PrimaryBlue, "KART")
        PaymentMethod.ONLINE -> Pair(SecondaryTeal, "ONLINE")
        PaymentMethod.MEAL_CARD -> Pair(CardPurple, "YEMEK KARTI")
        PaymentMethod.FREE -> Pair(StatusSuccess, "ÜCRETSİZ")
        PaymentMethod.OTHER -> Pair(TextSecondary, "DİĞER")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
