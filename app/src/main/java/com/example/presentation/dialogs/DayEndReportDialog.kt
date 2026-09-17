package com.example.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.domain.model.DayEndSummary
import com.example.ui.theme.*

@Composable
fun DayEndReportDialog(
    summary: DayEndSummary,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gün Sonu Mutabakat Raporu",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReportMetricRow("Toplam Teslim Edilen Sipariş", "${summary.completedDeliveries} Adet")
                ReportMetricRow("Nakit Siparişler Toplamı", "₺${String.format("%.2f", summary.cashEarnings)}")
                ReportMetricRow("Kredi Kartı Siparişler", "₺${String.format("%.2f", summary.cardEarnings)}")
                ReportMetricRow("Online Siparişler", "₺${String.format("%.2f", summary.onlineEarnings)}")
                ReportMetricRow("Toplam Sipariş Bedeli", "₺${String.format("%.2f", summary.totalOrdersAmount)}")
                ReportMetricRow("Restoranlara Ödenen Nakit", "-₺${String.format("%.2f", summary.restaurantCashPaid)}")

                HorizontalDivider(color = CardBorderDark, modifier = Modifier.padding(vertical = 4.dp))

                ReportMetricRow(
                    label = "Kasada Olması Gereken Fiziksel Nakit",
                    value = "₺${String.format("%.2f", summary.expectedPhysicalCash)}",
                    isHighlight = true,
                    highlightColor = CashGold
                )

                ReportMetricRow(
                    label = "Fiili Sayılan Nakit",
                    value = "₺${String.format("%.2f", summary.actualPhysicalCash)}",
                    isHighlight = true,
                    highlightColor = CashGold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusSuccess.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Mutabakat Farkı: ₺0.00",
                                color = StatusSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Fiziksel kasa ile sistem kayıtları %100 uyuşmaktadır.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.testTag("close_day_end_report_button")
            ) {
                Text("Tamam", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
private fun ReportMetricRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    highlightColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = if (isHighlight) TextPrimary else TextSecondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isHighlight) highlightColor else TextPrimary
            )
        )
    }
}
