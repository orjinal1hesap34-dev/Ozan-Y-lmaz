package com.example.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PartnerRestaurant
import com.example.ui.theme.*

@Composable
fun RestaurantPaymentDialog(
    restaurants: List<PartnerRestaurant>,
    currentCashBalance: Double,
    onDismiss: () -> Unit,
    onConfirmPayment: (PartnerRestaurant, Double) -> Unit
) {
    var selectedRestaurant by remember { mutableStateOf<PartnerRestaurant?>(restaurants.firstOrNull()) }
    var amountText by remember { mutableStateOf("500") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = CashGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Anlaşmalı Restorana Ödeme Yap",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Mevcut Fiziksel Kasa: ₺${String.format("%.2f", currentCashBalance)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CashGold
                    )
                )

                Text(
                    text = "Ödeme Yapılacak Restoran:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(restaurants.size) { idx ->
                        val r = restaurants[idx]
                        val isSelected = selectedRestaurant?.id == r.id
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else SurfaceVariantDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRestaurant = r }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = r.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Cari Alacak: ₺${r.pendingBalance.toInt()}", color = TextSecondary, fontSize = 11.sp)
                                }
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Ödeme Tutarı (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restaurant_payment_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CashGold,
                        unfocusedBorderColor = CardBorderDark
                    )
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = StatusError, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Lütfen geçerli bir tutar girin."
                    } else if (amount > currentCashBalance) {
                        errorMessage = "Yetersiz nakit kasa! Mevcut kasa: ₺$currentCashBalance"
                    } else if (selectedRestaurant == null) {
                        errorMessage = "Lütfen bir restoran seçin."
                    } else {
                        onConfirmPayment(selectedRestaurant!!, amount)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CashGold),
                modifier = Modifier.testTag("confirm_restaurant_payment_button")
            ) {
                Text("Ödemeyi Yap & Düş", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}
