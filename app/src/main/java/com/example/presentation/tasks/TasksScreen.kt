package com.example.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.PaymentMethod
import com.example.presentation.CourierUiState
import com.example.presentation.components.OrderStatusChip
import com.example.presentation.components.PaymentMethodChip
import com.example.ui.theme.*

@Composable
fun TasksScreen(
    uiState: CourierUiState,
    onSelectOrder: (Order) -> Unit,
    onRestoranaUlastim: (String) -> Unit,
    onPaketiAldim: (String) -> Unit,
    onTeslimataBasla: (String) -> Unit,
    onTeslimatiTamamla: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(0) } // 0: Aktif, 1: Tamamlanan

    val activeOrders = uiState.activeOrders
    val completedOrders = uiState.completedOrders
    val currentOrders = if (selectedFilter == 0) activeOrders else completedOrders

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Tab header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceDark)
                .padding(4.dp)
        ) {
            TabButton(
                title = "Aktif Görevler (${activeOrders.size})",
                isSelected = selectedFilter == 0,
                modifier = Modifier.weight(1f),
                onClick = { selectedFilter = 0 }
            )
            TabButton(
                title = "Tamamlanan (${completedOrders.size + 14})",
                isSelected = selectedFilter == 1,
                modifier = Modifier.weight(1f),
                onClick = { selectedFilter = 1 }
            )
        }

        if (currentOrders.isEmpty() && selectedFilter == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aktif Görev Bulunmuyor",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Çevrimiçi olduğunuzda Backend Dispatch Engine otomatik olarak yeni görev atayacaktır.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(currentOrders.size) { index ->
                    val order = currentOrders[index]
                    TaskCard(
                        order = order,
                        onCardClick = { onSelectOrder(order) },
                        onRestoranaUlastim = { onRestoranaUlastim(order.id) },
                        onPaketiAldim = { onPaketiAldim(order.id) },
                        onTeslimataBasla = { onTeslimataBasla(order.id) },
                        onTeslimatiTamamla = { onTeslimatiTamamla(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PrimaryBlue else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else TextSecondary
            )
        )
    }
}

@Composable
fun TaskCard(
    order: Order,
    onCardClick: () -> Unit,
    onRestoranaUlastim: () -> Unit,
    onPaketiAldim: () -> Unit,
    onTeslimataBasla: () -> Unit,
    onTeslimatiTamamla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: External Order ID, Chips and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.externalOrderId}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PaymentMethodChip(method = order.paymentMethod)
                }

                Text(
                    text = "₺${order.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OrderStatusChip(status = order.status)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = CardBorderDark
            )

            // Restaurant info
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = order.restaurantName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = order.restaurantAddress,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer info
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.PersonPinCircle,
                    contentDescription = null,
                    tint = CashGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Müşteri: ${order.customerName}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = order.customerAddress,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                }
            }

            // Controlled Progressive Action Button (Section 10, 11, 24)
            if (order.status != OrderStatus.DELIVERED) {
                Spacer(modifier = Modifier.height(14.dp))
                when (order.status) {
                    OrderStatus.ASSIGNED, OrderStatus.GOING_TO_RESTAURANT -> {
                        Button(
                            onClick = onRestoranaUlastim,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("at_restaurant_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.NearMe, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restorana Ulaştım", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.AT_RESTAURANT -> {
                        Button(
                            onClick = onPaketiAldim,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("pickup_order_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusWarning),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paketi Aldım", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.PICKED_UP -> {
                        Button(
                            onClick = onTeslimataBasla,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("start_delivery_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TwoWheeler, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TESLİMATA BAŞLA", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.GOING_TO_CUSTOMER, OrderStatus.ARRIVED -> {
                        Button(
                            onClick = onTeslimatiTamamla,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("complete_delivery_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TESLİMATI TAMAMLA", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
