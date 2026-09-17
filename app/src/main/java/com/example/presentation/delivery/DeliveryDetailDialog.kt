package com.example.presentation.delivery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.navigation.CourierNavigationManager
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.PaymentMethod
import com.example.domain.model.RouteStop
import com.example.domain.model.StopType
import com.example.presentation.components.OrderStatusChip
import com.example.presentation.components.PaymentMethodChip
import com.example.ui.theme.*

@Composable
fun DeliveryDetailDialog(
    order: Order,
    onDismiss: () -> Unit,
    onRestoranaUlastim: (String) -> Unit,
    onPaketiAldim: (String) -> Unit,
    onTeslimataBasla: (String) -> Unit,
    onTeslimatiTamamla: (String) -> Unit,
    onChangePaymentMethod: (String, PaymentMethod, String) -> Unit
) {
    val context = LocalContext.current
    var showChangePaymentModal by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            color = BackgroundDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sipariş Detayı #${order.externalOrderId}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    OrderStatusChip(status = order.status)
                }

                // Body content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // PAYMENT & AMOUNT CARD
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Tahsil Edilecek Tutar", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                                    Text(
                                        text = "₺${String.format("%.2f", order.amount)}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                    )
                                }
                                PaymentMethodChip(method = order.paymentMethod)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Payment Modification Trigger (Section 12 & 13)
                            OutlinedButton(
                                onClick = { showChangePaymentModal = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("change_payment_method_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CashGold)
                            ) {
                                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = CashGold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ödeme Şeklini Değiştir (Nakit / Kart)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // CUSTOMER INFO & CALL BUTTON (Section 11)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "MÜŞTERİ BİLGİLERİ",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = order.customerName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )

                            Text(
                                text = order.customerAddress,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )

                            if (order.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceVariantDark)
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CashGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Not: ${order.notes}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { dialPhoneNumber(context, order.customerPhone) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("call_customer_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ARA", color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { openNavigation(context, order) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("navigate_customer_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("YOL TARİFİ", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // RESTAURANT INFO (Section 10)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "RESTORAN BİLGİSİ",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = order.restaurantName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )

                            Text(
                                text = order.restaurantAddress,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )

                            if (order.itemsSummary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "İçerik: ${order.itemsSummary}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }
                    }
                }

                // BOTTOM ACTION BAR (Controlled state machine transition)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceDark,
                    tonalElevation = 8.dp
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        when (order.status) {
                            OrderStatus.ASSIGNED, OrderStatus.GOING_TO_RESTAURANT -> {
                                Button(
                                    onClick = { onRestoranaUlastim(order.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("modal_at_restaurant_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("RESTORANA ULAŞTIM", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                            OrderStatus.AT_RESTAURANT -> {
                                Button(
                                    onClick = { onPaketiAldim(order.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("modal_pickup_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusWarning),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("PAKETİ ALDIM", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                            OrderStatus.PICKED_UP -> {
                                Button(
                                    onClick = { onTeslimataBasla(order.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("modal_start_delivery_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("TESLİMATA BAŞLA", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                            OrderStatus.GOING_TO_CUSTOMER, OrderStatus.ARRIVED -> {
                                Button(
                                    onClick = { onTeslimatiTamamla(order.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("modal_complete_delivery_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("TESLİMATI TAMAMLA", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                            OrderStatus.DELIVERED -> {
                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("TESLİMAT TAMAMLANDI", color = StatusSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showChangePaymentModal) {
        ChangePaymentMethodDialog(
            currentMethod = order.paymentMethod,
            onDismiss = { showChangePaymentModal = false },
            onConfirm = { newMethod, reason ->
                onChangePaymentMethod(order.id, newMethod, reason)
                showChangePaymentModal = false
            }
        )
    }
}

@Composable
fun ChangePaymentMethodDialog(
    currentMethod: PaymentMethod,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod, String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(if (currentMethod == PaymentMethod.CASH) PaymentMethod.CARD else PaymentMethod.CASH) }
    var reasonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Ödeme Yöntemini Değiştir", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Mevcut Yöntem: ${currentMethod.title}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )

                Text(
                    text = "Yeni Ödeme Şekli Seçin:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.values().filter { it != currentMethod && it != PaymentMethod.FREE }.take(3).forEach { method ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryBlue else SurfaceVariantDark)
                                .clickable { selectedMethod = method }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method.title,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Değişiklik Nedeni (Audit Log)") },
                    placeholder = { Text("Örn: Müşteri pos istedi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("change_payment_reason_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorderDark
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedMethod, reasonText.ifBlank { "Kurye tarafından sahada güncellendi" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.testTag("confirm_change_payment_button")
            ) {
                Text("Kaydet & Logla", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}

private fun dialPhoneNumber(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Ignored
    }
}

private fun openNavigation(context: Context, order: Order) {
    val stop = RouteStop(
        id = order.id,
        routeId = "route_direct",
        stopOrder = 1,
        type = StopType.DROPOFF,
        orderId = order.id,
        packageCode = order.externalOrderId,
        locationName = order.customerName,
        address = order.customerAddress,
        latitude = order.latitude,
        longitude = order.longitude,
        isCompleted = order.status == OrderStatus.DELIVERED,
        estimatedArrivalMinutes = 15,
        contactPhone = order.customerPhone
    )
    CourierNavigationManager.startNavigation(
        context = context,
        courierLat = 41.0082,
        courierLng = 29.0050,
        targetStop = stop
    )
}
