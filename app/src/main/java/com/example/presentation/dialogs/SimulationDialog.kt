package com.example.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sync
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
import com.example.ui.theme.*

@Composable
fun SimulationDialog(
    onDismiss: () -> Unit,
    onSimulateNewAssignment: () -> Unit,
    onSimulateRouteOptimization: () -> Unit,
    onSimulateSync: () -> Unit,
    onToggleOffline: (() -> Unit)? = null,
    isOffline: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = CashGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Backend & Event Simülasyonu",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Sistem mimarisindeki backend ve ağ olaylarını canlı olarak tetikleyebilirsiniz:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                // Trigger 1: NEW_ASSIGNMENT event
                Button(
                    onClick = {
                        onSimulateNewAssignment()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("simulate_assignment_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("YENİ SİPARİŞ ATAMASI GÖNDER", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Kurye onaylamaz, doğrudan görev listesine eklenir", color = Color.Black.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                }

                // Trigger 2: ROUTE_UPDATED event
                Button(
                    onClick = {
                        onSimulateRouteOptimization()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("simulate_route_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Route, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("ROTA OPTİMİZASYON GÜNCELLEMESİ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Route engine maliyet analizi ile sıralamayı yeniler", color = Color.Black.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                }

                // Trigger 3: Toggle Offline Mode
                if (onToggleOffline != null) {
                    Button(
                        onClick = {
                            onToggleOffline()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("simulate_offline_toggle_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOffline) StatusSuccess.copy(alpha = 0.25f) else StatusWarning.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isOffline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isOffline) StatusSuccess else StatusWarning
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = if (isOffline) "İNTERNETİ BAĞLA (ÇEVRİMİÇİ YAP)" else "İNTERNETİ KES (ÇEVRİMDIŞI YAP)",
                                color = if (isOffline) StatusSuccess else StatusWarning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isOffline) "Room bekleyen verileri otomatik senkronize eder" else "Offline rozetini ve yerel Room kaydını test et",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Trigger 4: Force offline sync
                Button(
                    onClick = {
                        onSimulateSync()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("simulate_sync_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Room Çevrimdışı Kuyruğu Eşitle", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}
