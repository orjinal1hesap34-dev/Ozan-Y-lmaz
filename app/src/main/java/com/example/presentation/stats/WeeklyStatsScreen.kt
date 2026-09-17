package com.example.presentation.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WeeklyStatsProvider
import com.example.domain.model.DailyDeliveryStat
import com.example.domain.model.WeeklyStatsSummary
import com.example.presentation.CourierUiState
import com.example.presentation.components.NavTab
import com.example.ui.theme.*

/**
 * Screen showing weekly delivery statistics and completion rates for the courier,
 * inspired by Recharts declarative data visualization components:
 * - Weekly Delivery Volume Bar Chart with interactive Tooltips
 * - Circular Radial Progress Gauge for overall completion rate
 * - Daily Completion Rate Spline Curve with target threshold reference line
 * - Performance KPI Summary Grid
 * - Day-by-Day Breakdown list
 */
@Composable
fun WeeklyStatsScreen(
    uiState: CourierUiState,
    onNavigateTab: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWeekIndex by remember { mutableIntStateOf(0) }
    var selectedDayIndex by remember { mutableIntStateOf(6) } // Defaults to Sunday / Today

    val completedToday = uiState.completedOrders.size
    val weeklyStats: WeeklyStatsSummary = remember(selectedWeekIndex, completedToday) {
        WeeklyStatsProvider.getWeeklyStats(selectedWeekIndex, completedToday)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // 1. Screen Title & Week Selector Segmented Pills
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Haftalık İstatistikler",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Teslimat hacmi ve tamamlanma oranları",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlue.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBlue.copy(alpha = 0.4f)))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+%${weeklyStats.completionRateDeltaVsLastWeek}",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Week Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WeekTabButton(
                        title = "Bu Hafta",
                        sub = "15-21 Eyl",
                        isSelected = selectedWeekIndex == 0,
                        onClick = {
                            selectedWeekIndex = 0
                            selectedDayIndex = 6
                        },
                        modifier = Modifier.weight(1f)
                    )
                    WeekTabButton(
                        title = "Geçen Hafta",
                        sub = "8-14 Eyl",
                        isSelected = selectedWeekIndex == 1,
                        onClick = {
                            selectedWeekIndex = 1
                            selectedDayIndex = 4
                        },
                        modifier = Modifier.weight(1f)
                    )
                    WeekTabButton(
                        title = "2 Hafta Önce",
                        sub = "1-7 Eyl",
                        isSelected = selectedWeekIndex == 2,
                        onClick = {
                            selectedWeekIndex = 2
                            selectedDayIndex = 4
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Recharts KPI Summary Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "Toplam Teslimat",
                        value = "${weeklyStats.totalCompleted}",
                        subValue = "+${weeklyStats.deliveriesDeltaVsLastWeek} vs geçen hf",
                        icon = Icons.Default.LocalShipping,
                        color = PrimaryBlue,
                        badgeColor = StatusSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Tamamlanma Oranı",
                        value = "%${String.format(java.util.Locale.US, "%.1f", weeklyStats.overallCompletionRate)}",
                        subValue = "Hedef: %${weeklyStats.targetCompletionRate.toInt()}",
                        icon = Icons.Default.CheckCircle,
                        color = StatusSuccess,
                        badgeColor = if (weeklyStats.overallCompletionRate >= weeklyStats.targetCompletionRate) StatusSuccess else StatusWarning,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "Zamanında Teslim",
                        value = "%${String.format(java.util.Locale.US, "%.1f", weeklyStats.overallOnTimeRate)}",
                        subValue = "Ort. ${weeklyStats.avgDeliveryMinutes} dk",
                        icon = Icons.Default.Timer,
                        color = SecondaryTeal,
                        badgeColor = SecondaryTeal,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Haftalık Kazanç",
                        value = "₺${weeklyStats.totalEarnings.toInt()}",
                        subValue = "₺${weeklyStats.bonusEarned.toInt()} prim dahil",
                        icon = Icons.Default.Payments,
                        color = CashGold,
                        badgeColor = CashGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Weekly Delivery Volume Bar Chart (Recharts BarChart)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_volume_chart_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GÜNLÜK TESLİMAT HACMİ",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Grafikteki sütunlara dokunarak gün detayını inceleyin",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    RechartsWeeklyBarChart(
                        dailyStats = weeklyStats.dailyStats,
                        selectedIndex = selectedDayIndex,
                        onSelectDay = { selectedDayIndex = it }
                    )
                }
            }
        }

        // 3.5. D3 COURIER EFFICIENCY: Completed Deliveries vs Distance Traveled (Room Database Line Chart)
        item {
            D3CourierEfficiencyChart(
                efficiencyStats = uiState.efficiencyStats,
                modifier = Modifier.testTag("d3_efficiency_component")
            )
        }

        // 3.6. COURIER EFFICIENCY DETAILS & QUADRANT MATRIX
        item {
            CourierEfficiencySection(
                weeklyStats = weeklyStats,
                modifier = Modifier.testTag("efficiency_analytics_section")
            )
        }

        // 4. Overall Completion Rate Radial Gauge
        item {
            RechartsRadialGauge(
                completionRate = weeklyStats.overallCompletionRate,
                targetRate = weeklyStats.targetCompletionRate,
                onTimeRate = weeklyStats.overallOnTimeRate,
                modifier = Modifier.testTag("completion_rate_radial_gauge")
            )
        }

        // 5. Daily Completion Rate Trend Line Chart (Recharts Area/LineChart)
        item {
            RechartsCompletionTrendLineChart(
                dailyStats = weeklyStats.dailyStats,
                targetRate = weeklyStats.targetCompletionRate,
                modifier = Modifier.testTag("completion_trend_line_chart")
            )
        }

        // 6. Day-by-Day Detailed Breakdown Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GÜNLÜK PERFORMANS TABLOSU",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "${weeklyStats.dailyStats.size} Gün",
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        itemsIndexed(weeklyStats.dailyStats) { index, stat ->
            val isSelected = index == selectedDayIndex
            DailyBreakdownCard(
                stat = stat,
                isSelected = isSelected,
                onClick = { selectedDayIndex = index }
            )
        }
    }
}

@Composable
private fun WeekTabButton(
    title: String,
    sub: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) SurfaceVariantDark else Color.Transparent,
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBlue.copy(alpha = 0.6f))) else null,
        modifier = modifier.height(44.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryBlue else TextSecondary
            )
            Text(
                text = sub,
                fontSize = 9.sp,
                color = if (isSelected) TextPrimary else TextMuted
            )
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    color: Color,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = badgeColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun DailyBreakdownCard(
    stat: DailyDeliveryStat,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) PrimaryBlue.copy(alpha = 0.8f) else CardBorderDark

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceVariantDark else SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(borderColor)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("daily_breakdown_card_${stat.dayKey}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else SurfaceVariantDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stat.dayLabel,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PrimaryBlue else TextPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${stat.fullDayName}, ${stat.dateLabel}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            if (stat.isToday) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SecondaryTeal.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "BUGÜN",
                                        color = SecondaryTeal,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Teslimat: ${stat.completedDeliveries} / ${stat.totalAssigned} paket • Hedef: ${stat.targetDeliveries}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (stat.completionRate >= 92f) StatusSuccess.copy(alpha = 0.15f) else StatusWarning.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "%${String.format(java.util.Locale.US, "%.1f", stat.completionRate)}",
                            color = if (stat.completionRate >= 92f) StatusSuccess else StatusWarning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₺${stat.earnings.toInt()}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CashGold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar showing completion
            LinearProgressIndicator(
                progress = { (stat.completedDeliveries.toFloat() / stat.totalAssigned.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (stat.completionRate >= 92f) PrimaryBlue else StatusWarning,
                trackColor = SurfaceVariantDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Zamanında: %${stat.onTimeRate.toInt()}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
                Text(
                    text = "Mesafe: ${stat.distanceKm} km",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
                Text(
                    text = "Verimlilik: ${String.format(java.util.Locale.US, "%.2f", stat.kmPerDelivery)} km/p",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (stat.kmPerDelivery <= 2.3f) SecondaryTeal else TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
