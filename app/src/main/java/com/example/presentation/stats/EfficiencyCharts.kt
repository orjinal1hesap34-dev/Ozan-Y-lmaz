package com.example.presentation.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DailyDeliveryStat
import com.example.domain.model.HourlyEfficiencyStat
import com.example.domain.model.WeeklyStatsSummary
import com.example.ui.theme.*
import kotlin.math.max

/**
 * Recharts & D3-inspired Efficiency Visualization Component:
 * Displays dual-axis correlation between Completed Deliveries (Bar Series, Left Axis)
 * and Distance Traveled in km (Spline Line Series, Right Axis) over time,
 * complete with dynamic tooltips, quadrant classification, and efficiency density metrics.
 */
@Composable
fun CourierEfficiencySection(
    weeklyStats: WeeklyStatsSummary,
    modifier: Modifier = Modifier
) {
    var timeViewMode by remember { mutableIntStateOf(0) } // 0: Daily (Haftalık Günler), 1: Hourly (Vardiya Saatleri)
    var selectedDayIndex by remember { mutableIntStateOf(6) } // Default: Today / Sunday
    var selectedHourIndex by remember { mutableIntStateOf(1) } // Default: Lunch Peak

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("courier_efficiency_section")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title & Time Series Segmented Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KOURYER VERİMLİLİK ANALİZİ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Teslimat adedi vs Kat edilen mesafe (D3 / Recharts Çift Eksen)",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Granularity Switcher: Günlük Zaman Serisi vs Saatlik Dilimler
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantDark)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TimeGranularityPill(
                    title = "7 Günlük Zaman Serisi",
                    sub = "Günlük Değişim",
                    isSelected = timeViewMode == 0,
                    onClick = { timeViewMode = 0 },
                    modifier = Modifier.weight(1f)
                )
                TimeGranularityPill(
                    title = "Vardiya Saatleri",
                    sub = "Zaman Pencereleri",
                    isSelected = timeViewMode == 1,
                    onClick = { timeViewMode = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Efficiency KPI Quick Highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EfficiencyMiniKpi(
                    title = "Mesafe/Teslimat",
                    value = "${String.format(java.util.Locale.US, "%.2f", weeklyStats.avgKmPerDelivery)} km",
                    badge = "Çok Verimli",
                    color = SecondaryTeal,
                    modifier = Modifier.weight(1f)
                )
                EfficiencyMiniKpi(
                    title = "10 km Başına Paket",
                    value = "${String.format(java.util.Locale.US, "%.1f", weeklyStats.overallDeliveriesPer10Km)} adet",
                    badge = "+%18 Rota Yoğunluğu",
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                EfficiencyMiniKpi(
                    title = "Verimlilik Skoru",
                    value = "${weeklyStats.efficiencyScore}/100",
                    badge = "A+ Seviye",
                    color = CashGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Dual-Axis Composed Chart (Recharts ComposedChart: Bar + Spline Line)
            if (timeViewMode == 0) {
                RechartsDualAxisEfficiencyChart(
                    dailyStats = weeklyStats.dailyStats,
                    selectedIndex = selectedDayIndex,
                    onSelectIndex = { selectedDayIndex = it },
                    modifier = Modifier.testTag("recharts_daily_efficiency_chart")
                )
            } else {
                RechartsHourlyEfficiencyChart(
                    hourlyStats = weeklyStats.hourlyStats,
                    selectedIndex = selectedHourIndex,
                    onSelectIndex = { selectedHourIndex = it },
                    modifier = Modifier.testTag("recharts_hourly_efficiency_chart")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // D3 Quadrant Scatter Matrix (Deliveries vs Distance)
            D3EfficiencyQuadrantScatterPlot(
                dailyStats = weeklyStats.dailyStats,
                selectedDayIndex = selectedDayIndex,
                onSelectDay = { selectedDayIndex = it },
                modifier = Modifier.testTag("d3_efficiency_quadrant_plot")
            )
        }
    }
}

@Composable
private fun TimeGranularityPill(
    title: String,
    sub: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PrimaryBlue else Color.Transparent,
        modifier = modifier.height(42.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
            Text(
                text = sub,
                fontSize = 9.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted
            )
        }
    }
}

@Composable
private fun EfficiencyMiniKpi(
    title: String,
    value: String,
    badge: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceVariantDark,
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.5.sp),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

/**
 * Recharts Dual Axis Composed Chart:
 * - Left Y-Axis: Completed Deliveries (Bars with cyan gradient)
 * - Right Y-Axis: Distance Traveled in km (Teal/Gold Bézier spline curve)
 * - Interactive vertical cursor guide and detailed popup Tooltip
 */
@Composable
fun RechartsDualAxisEfficiencyChart(
    dailyStats: List<DailyDeliveryStat>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(dailyStats) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val maxDeliveries = remember(dailyStats) {
        val maxVal = dailyStats.maxOfOrNull { it.completedDeliveries } ?: 35
        ((maxVal / 10) + 1) * 10
    }

    val maxDistance = remember(dailyStats) {
        val maxKm = dailyStats.maxOfOrNull { it.distanceKm } ?: 80.0
        ((maxKm / 20).toInt() + 1) * 20
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Dual Axis Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PrimaryBlue)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Teslimat (Sol Eksen)",
                    fontSize = 10.5.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SecondaryTeal)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Mesafe - km (Sağ Eksen)",
                    fontSize = 10.5.sp,
                    color = SecondaryTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chart Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dailyStats) {
                        detectTapGestures { offset ->
                            val leftPadding = 32.dp.toPx()
                            val rightPadding = 34.dp.toPx()
                            val chartWidth = size.width - leftPadding - rightPadding
                            val slotWidth = chartWidth / dailyStats.size

                            if (offset.x >= leftPadding && offset.x <= size.width - rightPadding) {
                                val idx = ((offset.x - leftPadding) / slotWidth).toInt().coerceIn(0, dailyStats.lastIndex)
                                onSelectIndex(idx)
                            }
                        }
                    }
            ) {
                val leftPadding = 32.dp.toPx()
                val rightPadding = 34.dp.toPx()
                val topPadding = 18.dp.toPx()
                val bottomPadding = 30.dp.toPx()

                val chartWidth = size.width - leftPadding - rightPadding
                val chartHeight = size.height - topPadding - bottomPadding

                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

                // 1. Cartesian Grid & Left Y-Axis Ticks (Deliveries: 0, 10, 20, 30, 40)
                val deliveryStep = maxDeliveries / 4
                for (i in 0..4) {
                    val value = i * deliveryStep
                    val yPos = topPadding + chartHeight * (1f - (value.toFloat() / maxDeliveries))

                    // Grid line
                    drawLine(
                        color = CardBorderDark.copy(alpha = 0.35f),
                        start = Offset(leftPadding, yPos),
                        end = Offset(size.width - rightPadding, yPos),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )

                    // Left Y-Axis text (Deliveries count)
                    val leftText = textMeasurer.measure(
                        text = "$value",
                        style = TextStyle(color = PrimaryBlue.copy(alpha = 0.8f), fontSize = 8.5.sp)
                    )
                    drawText(
                        textLayoutResult = leftText,
                        topLeft = Offset(leftPadding - leftText.size.width - 4.dp.toPx(), yPos - leftText.size.height / 2)
                    )

                    // Right Y-Axis text (Distance km: e.g. 0, 25, 50, 75, 100 km)
                    val distVal = (i * (maxDistance / 4))
                    val rightText = textMeasurer.measure(
                        text = "${distVal}k",
                        style = TextStyle(color = SecondaryTeal.copy(alpha = 0.8f), fontSize = 8.5.sp)
                    )
                    drawText(
                        textLayoutResult = rightText,
                        topLeft = Offset(size.width - rightPadding + 4.dp.toPx(), yPos - rightText.size.height / 2)
                    )
                }

                if (dailyStats.isNotEmpty()) {
                    val slotWidth = chartWidth / dailyStats.size
                    val barWidth = slotWidth * 0.44f

                    // 2. Draw Bars for Completed Deliveries (Left Axis)
                    dailyStats.forEachIndexed { index, stat ->
                        val centerX = leftPadding + (index * slotWidth) + (slotWidth / 2)
                        val isSelected = index == selectedIndex

                        val barHeight = (chartHeight * (stat.completedDeliveries.toFloat() / maxDeliveries) * animatedProgress.value).coerceAtLeast(4f)
                        val barTop = topPadding + chartHeight - barHeight

                        // Column Highlight background if selected
                        if (isSelected) {
                            drawRoundRect(
                                color = SurfaceVariantDark.copy(alpha = 0.5f),
                                topLeft = Offset(centerX - slotWidth / 2 + 2.dp.toPx(), topPadding),
                                size = Size(slotWidth - 4.dp.toPx(), chartHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            // Vertical Recharts cursor line
                            drawLine(
                                color = PrimaryBlue.copy(alpha = 0.5f),
                                start = Offset(centerX, topPadding),
                                end = Offset(centerX, topPadding + chartHeight),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                            )
                        }

                        // Bar Gradient
                        val barBrush = Brush.verticalGradient(
                            colors = if (isSelected) {
                                listOf(Color(0xFF64B5F6), PrimaryBlue)
                            } else {
                                listOf(PrimaryBlue.copy(alpha = 0.85f), PrimaryBlue.copy(alpha = 0.55f))
                            },
                            startY = barTop,
                            endY = topPadding + chartHeight
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(centerX - barWidth / 2, barTop),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // X-Axis Day label
                        val labelLayout = textMeasurer.measure(
                            text = stat.dayLabel,
                            style = TextStyle(
                                color = if (isSelected) PrimaryBlue else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        drawText(
                            textLayoutResult = labelLayout,
                            topLeft = Offset(centerX - labelLayout.size.width / 2, topPadding + chartHeight + 6.dp.toPx())
                        )
                    }

                    // 3. Draw Distance Traveled Spline Curve (Right Axis)
                    val distancePoints = dailyStats.mapIndexed { index, stat ->
                        val centerX = leftPadding + (index * slotWidth) + (slotWidth / 2)
                        val normDist = (stat.distanceKm.toFloat() / maxDistance).coerceIn(0f, 1f)
                        val y = topPadding + chartHeight * (1f - (normDist * animatedProgress.value))
                        Offset(centerX, y)
                    }

                    val linePath = Path()
                    distancePoints.forEachIndexed { i, pt ->
                        if (i == 0) {
                            linePath.moveTo(pt.x, pt.y)
                        } else {
                            val prev = distancePoints[i - 1]
                            val cx1 = prev.x + (pt.x - prev.x) / 2
                            val cy1 = prev.y
                            val cx2 = prev.x + (pt.x - prev.x) / 2
                            val cy2 = pt.y
                            linePath.cubicTo(cx1, cy1, cx2, cy2, pt.x, pt.y)
                        }
                    }

                    // Draw Smooth Line for Distance Traveled
                    drawPath(
                        path = linePath,
                        color = SecondaryTeal,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw circular nodes on Distance Line
                    distancePoints.forEachIndexed { index, pt ->
                        val isSelected = index == selectedIndex

                        // Outer ring
                        drawCircle(
                            color = if (isSelected) SecondaryTeal.copy(alpha = 0.4f) else BackgroundDark,
                            radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                            center = pt
                        )
                        // Inner node
                        drawCircle(
                            color = SecondaryTeal,
                            radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recharts Interactive Dynamic Tooltip Card
        val currentStat = dailyStats.getOrNull(selectedIndex)
        if (currentStat != null) {
            EfficiencyRechartsTooltipCard(
                periodTitle = "${currentStat.fullDayName}, ${currentStat.dateLabel}",
                deliveries = currentStat.completedDeliveries,
                distanceKm = currentStat.distanceKm,
                kmPerDelivery = currentStat.kmPerDelivery,
                deliveriesPer10Km = currentStat.deliveriesPer10Km,
                rating = currentStat.efficiencyRating
            )
        }
    }
}

/**
 * Hourly Intraday Efficiency Chart for different times of day (09-12, 12-15, 15-18, 18-21).
 */
@Composable
fun RechartsHourlyEfficiencyChart(
    hourlyStats: List<HourlyEfficiencyStat>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(hourlyStats) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val maxDeliveries = remember(hourlyStats) {
        val maxVal = hourlyStats.maxOfOrNull { it.completedDeliveries } ?: 60
        ((maxVal / 10) + 1) * 10
    }

    val maxDistance = remember(hourlyStats) {
        val maxDist = hourlyStats.maxOfOrNull { it.distanceKm } ?: 120.0
        ((maxDist / 20).toInt() + 1) * 20
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zaman Dilimi Analizi",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Text(
                text = "Öğle 12-15 En Yüksek Yoğunluk",
                fontSize = 10.sp,
                color = StatusSuccess,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(hourlyStats) {
                        detectTapGestures { offset ->
                            val leftPadding = 32.dp.toPx()
                            val rightPadding = 34.dp.toPx()
                            val chartWidth = size.width - leftPadding - rightPadding
                            val slotWidth = chartWidth / hourlyStats.size

                            if (offset.x >= leftPadding && offset.x <= size.width - rightPadding) {
                                val idx = ((offset.x - leftPadding) / slotWidth).toInt().coerceIn(0, hourlyStats.lastIndex)
                                onSelectIndex(idx)
                            }
                        }
                    }
            ) {
                val leftPadding = 32.dp.toPx()
                val rightPadding = 34.dp.toPx()
                val topPadding = 18.dp.toPx()
                val bottomPadding = 30.dp.toPx()

                val chartWidth = size.width - leftPadding - rightPadding
                val chartHeight = size.height - topPadding - bottomPadding

                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

                // Grid Lines & Ticks
                for (i in 0..4) {
                    val yPos = topPadding + chartHeight * (1f - (i / 4f))
                    drawLine(
                        color = CardBorderDark.copy(alpha = 0.35f),
                        start = Offset(leftPadding, yPos),
                        end = Offset(size.width - rightPadding, yPos),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )

                    val leftVal = (i * (maxDeliveries / 4))
                    val leftText = textMeasurer.measure(
                        text = "$leftVal",
                        style = TextStyle(color = PrimaryBlue.copy(alpha = 0.8f), fontSize = 8.5.sp)
                    )
                    drawText(
                        textLayoutResult = leftText,
                        topLeft = Offset(leftPadding - leftText.size.width - 4.dp.toPx(), yPos - leftText.size.height / 2)
                    )

                    val rightVal = (i * (maxDistance / 4))
                    val rightText = textMeasurer.measure(
                        text = "${rightVal}k",
                        style = TextStyle(color = SecondaryTeal.copy(alpha = 0.8f), fontSize = 8.5.sp)
                    )
                    drawText(
                        textLayoutResult = rightText,
                        topLeft = Offset(size.width - rightPadding + 4.dp.toPx(), yPos - rightText.size.height / 2)
                    )
                }

                if (hourlyStats.isNotEmpty()) {
                    val slotWidth = chartWidth / hourlyStats.size
                    val barWidth = slotWidth * 0.48f

                    // Bars
                    hourlyStats.forEachIndexed { index, stat ->
                        val centerX = leftPadding + (index * slotWidth) + (slotWidth / 2)
                        val isSelected = index == selectedIndex

                        val barHeight = (chartHeight * (stat.completedDeliveries.toFloat() / maxDeliveries) * animatedProgress.value).coerceAtLeast(4f)
                        val barTop = topPadding + chartHeight - barHeight

                        if (isSelected) {
                            drawRoundRect(
                                color = SurfaceVariantDark.copy(alpha = 0.5f),
                                topLeft = Offset(centerX - slotWidth / 2 + 2.dp.toPx(), topPadding),
                                size = Size(slotWidth - 4.dp.toPx(), chartHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }

                        val barBrush = Brush.verticalGradient(
                            colors = if (isSelected) listOf(Color(0xFF81D4FA), PrimaryBlue) else listOf(PrimaryBlue.copy(alpha = 0.85f), PrimaryBlue.copy(alpha = 0.5f)),
                            startY = barTop,
                            endY = topPadding + chartHeight
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(centerX - barWidth / 2, barTop),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        val timeLayout = textMeasurer.measure(
                            text = stat.shortLabel,
                            style = TextStyle(
                                color = if (isSelected) PrimaryBlue else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        drawText(
                            textLayoutResult = timeLayout,
                            topLeft = Offset(centerX - timeLayout.size.width / 2, topPadding + chartHeight + 6.dp.toPx())
                        )
                    }

                    // Distance Line
                    val points = hourlyStats.mapIndexed { index, stat ->
                        val centerX = leftPadding + (index * slotWidth) + (slotWidth / 2)
                        val norm = (stat.distanceKm.toFloat() / maxDistance).coerceIn(0f, 1f)
                        val y = topPadding + chartHeight * (1f - (norm * animatedProgress.value))
                        Offset(centerX, y)
                    }

                    val linePath = Path()
                    points.forEachIndexed { i, pt ->
                        if (i == 0) linePath.moveTo(pt.x, pt.y)
                        else {
                            val prev = points[i - 1]
                            val cx = prev.x + (pt.x - prev.x) / 2
                            linePath.cubicTo(cx, prev.y, cx, pt.y, pt.x, pt.y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = SecondaryTeal,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    points.forEachIndexed { idx, pt ->
                        drawCircle(color = SecondaryTeal, radius = 4.dp.toPx(), center = pt)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val currentHour = hourlyStats.getOrNull(selectedIndex)
        if (currentHour != null) {
            EfficiencyRechartsTooltipCard(
                periodTitle = "Saat Dilimi: ${currentHour.timeSlotLabel}",
                deliveries = currentHour.completedDeliveries,
                distanceKm = currentHour.distanceKm,
                kmPerDelivery = currentHour.kmPerDelivery,
                deliveriesPer10Km = currentHour.deliveriesPer10Km,
                rating = if (currentHour.kmPerDelivery < 2.0f) "Maksimum Yoğunluk" else "Optimum"
            )
        }
    }
}

/**
 * Recharts Tooltip styled popup card displaying dual metric synergy.
 */
@Composable
fun EfficiencyRechartsTooltipCard(
    periodTitle: String,
    deliveries: Int,
    distanceKm: Double,
    kmPerDelivery: Float,
    deliveriesPer10Km: Float,
    rating: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceVariantDark,
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBlue.copy(alpha = 0.4f))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = periodTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StatusSuccess.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = rating,
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Tamamlanan Paket", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                    Text(text = "$deliveries Teslimat", style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryBlue, fontWeight = FontWeight.Bold))
                }
                Column {
                    Text(text = "Kat Edilen Yol", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                    Text(text = "${String.format(java.util.Locale.US, "%.1f", distanceKm)} km", style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryTeal, fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Birim Verimlilik", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                    Text(text = "${String.format(java.util.Locale.US, "%.2f", kmPerDelivery)} km / paket", style = MaterialTheme.typography.bodyMedium.copy(color = CashGold, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/**
 * D3-inspired Efficiency Quadrant Scatter Plot:
 * Correlates Distance (X-axis) vs Completed Deliveries (Y-axis).
 * Identifies the courier's sweet spot: High Delivery Volume with Low Distance.
 */
@Composable
fun D3EfficiencyQuadrantScatterPlot(
    dailyStats: List<DailyDeliveryStat>,
    selectedDayIndex: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(dailyStats) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "D3 VERİMLİLİK MATRİSİ (DAĞILIM)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Text(
                        text = "Y-Ekseni: Paket Sayısı • X-Ekseni: Kat Edilen Mesafe (km)",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SecondaryTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "En Verimli: Çarşamba",
                        color = SecondaryTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dailyStats) {
                            detectTapGestures { offset ->
                                val leftPadding = 32.dp.toPx()
                                val rightPadding = 20.dp.toPx()
                                val topPadding = 16.dp.toPx()
                                val bottomPadding = 24.dp.toPx()

                                val chartWidth = size.width - leftPadding - rightPadding
                                val chartHeight = size.height - topPadding - bottomPadding

                                val minKm = 30f
                                val maxKm = 85f
                                val minDel = 12f
                                val maxDel = 38f

                                dailyStats.forEachIndexed { index, stat ->
                                    val xNorm = ((stat.distanceKm.toFloat() - minKm) / (maxKm - minKm)).coerceIn(0f, 1f)
                                    val yNorm = ((stat.completedDeliveries - minDel) / (maxDel - minDel)).coerceIn(0f, 1f)
                                    val ptX = leftPadding + (chartWidth * xNorm)
                                    val ptY = topPadding + chartHeight * (1f - yNorm)

                                    val dist = (Offset(ptX, ptY) - offset).getDistance()
                                    if (dist <= 24.dp.toPx()) {
                                        onSelectDay(index)
                                    }
                                }
                            }
                        }
                ) {
                    val leftPadding = 32.dp.toPx()
                    val rightPadding = 20.dp.toPx()
                    val topPadding = 16.dp.toPx()
                    val bottomPadding = 24.dp.toPx()

                    val chartWidth = size.width - leftPadding - rightPadding
                    val chartHeight = size.height - topPadding - bottomPadding

                    val minKm = 30f
                    val maxKm = 85f
                    val minDel = 12f
                    val maxDel = 38f

                    // 1. Draw Quadrant dividers
                    val midX = leftPadding + chartWidth * 0.5f
                    val midY = topPadding + chartHeight * 0.5f

                    // D3 Quadrant shaded background (Top-Left: Golden Zone / High Volume, Low Distance)
                    drawRect(
                        color = StatusSuccess.copy(alpha = 0.05f),
                        topLeft = Offset(leftPadding, topPadding),
                        size = Size(chartWidth * 0.5f, chartHeight * 0.5f)
                    )

                    // Quadrant axis lines
                    drawLine(
                        color = CardBorderDark.copy(alpha = 0.4f),
                        start = Offset(midX, topPadding),
                        end = Offset(midX, topPadding + chartHeight),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                    drawLine(
                        color = CardBorderDark.copy(alpha = 0.4f),
                        start = Offset(leftPadding, midY),
                        end = Offset(size.width - rightPadding, midY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Quadrant Label (Golden Zone)
                    val goldenLabel = textMeasurer.measure(
                        text = "★ ALTIN BÖLGE (Yüksek Paket, Az Km)",
                        style = TextStyle(color = StatusSuccess.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    )
                    drawText(goldenLabel, topLeft = Offset(leftPadding + 6.dp.toPx(), topPadding + 4.dp.toPx()))

                    // 2. Trend Line (Average efficiency slope line)
                    val startTrend = Offset(leftPadding, topPadding + chartHeight * 0.85f)
                    val endTrend = Offset(leftPadding + chartWidth, topPadding + chartHeight * 0.15f)
                    drawLine(
                        color = PrimaryBlue.copy(alpha = 0.3f),
                        start = startTrend,
                        end = endTrend,
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // 3. Scatter Bubble Nodes for each day
                    dailyStats.forEachIndexed { index, stat ->
                        val isSelected = index == selectedDayIndex
                        val xNorm = ((stat.distanceKm.toFloat() - minKm) / (maxKm - minKm)).coerceIn(0f, 1f)
                        val yNorm = ((stat.completedDeliveries - minDel) / (maxDel - minDel)).coerceIn(0f, 1f)

                        val ptX = leftPadding + (chartWidth * xNorm)
                        val ptY = topPadding + chartHeight * (1f - (yNorm * animatedProgress.value))

                        // Node Color based on efficiency ratio
                        val nodeColor = when {
                            stat.kmPerDelivery <= 2.3f -> StatusSuccess
                            stat.kmPerDelivery <= 2.6f -> PrimaryBlue
                            else -> CashGold
                        }

                        // Outer pulsing glow if selected
                        if (isSelected) {
                            drawCircle(
                                color = nodeColor.copy(alpha = 0.3f),
                                radius = 14.dp.toPx(),
                                center = Offset(ptX, ptY)
                            )
                        }

                        // Solid circle node
                        drawCircle(
                            color = nodeColor,
                            radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                            center = Offset(ptX, ptY)
                        )

                        // Day label beside node
                        val nodeLabel = textMeasurer.measure(
                            text = "${stat.dayLabel} (${stat.completedDeliveries}p)",
                            style = TextStyle(
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 8.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        drawText(
                            textLayoutResult = nodeLabel,
                            topLeft = Offset(ptX + 8.dp.toPx(), ptY - nodeLabel.size.height / 2)
                        )
                    }
                }
            }
        }
    }
}
