package com.example.presentation.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
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
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Recharts-style Bar & Area Chart for weekly delivery volume.
 * Supports:
 * - CartesianGrid (dashed horizontal grid lines)
 * - X-Axis with day labels and date sub-labels
 * - Y-Axis ticks (0, 10, 20, 30, 40)
 * - Dual layered bars: Target (semi-transparent) and Delivered (gradient)
 * - Interactive Tap / Hover with vertical cursor guide line and Recharts Tooltip Card
 */
@Composable
fun RechartsWeeklyBarChart(
    dailyStats: List<DailyDeliveryStat>,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxDeliveries = remember(dailyStats) {
        val maxVal = dailyStats.maxOfOrNull { maxOf(it.totalAssigned, it.completedDeliveries, it.targetDeliveries) } ?: 35
        ((maxVal / 10) + 1) * 10
    }

    // Animation progress for smooth entrance
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyStats) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend row (Recharts Legend component)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
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
                    text = "Tamamlanan",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, CardBorderDark, RoundedCornerShape(2.dp))
                        .background(SurfaceVariantDark.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hedef / Atanan",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .testTag("weekly_bar_chart_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dailyStats) {
                        detectTapGestures { offset ->
                            val leftPadding = 32.dp.toPx()
                            val rightPadding = 12.dp.toPx()
                            val chartWidth = size.width - leftPadding - rightPadding
                            val barSlotWidth = chartWidth / dailyStats.size
                            if (offset.x >= leftPadding && offset.x <= size.width - rightPadding) {
                                val clickedIndex = ((offset.x - leftPadding) / barSlotWidth).toInt()
                                    .coerceIn(0, dailyStats.lastIndex)
                                onSelectDay(clickedIndex)
                            }
                        }
                    }
            ) {
                val leftPadding = 32.dp.toPx()
                val rightPadding = 12.dp.toPx()
                val topPadding = 16.dp.toPx()
                val bottomPadding = 32.dp.toPx()

                val chartWidth = size.width - leftPadding - rightPadding
                val chartHeight = size.height - topPadding - bottomPadding
                val barSlotWidth = chartWidth / dailyStats.size
                val barWidth = barSlotWidth * 0.48f

                // 1. Draw CartesianGrid & Y-Axis Labels
                val gridSteps = 4
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                val gridColor = CardBorderDark.copy(alpha = 0.5f)

                for (i in 0..gridSteps) {
                    val yVal = maxDeliveries * (gridSteps - i) / gridSteps
                    val yPos = topPadding + (chartHeight * i / gridSteps)

                    // Grid line
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPadding, yPos),
                        end = Offset(size.width - rightPadding, yPos),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )

                    // Y-Axis tick text
                    val textLayout = textMeasurer.measure(
                        text = "$yVal",
                        style = TextStyle(color = TextMuted, fontSize = 9.sp)
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(leftPadding - textLayout.size.width - 6.dp.toPx(), yPos - textLayout.size.height / 2)
                    )
                }

                // 2. Draw Bars and X-Axis Labels
                dailyStats.forEachIndexed { index, stat ->
                    val isSelected = index == selectedIndex
                    val slotCenterX = leftPadding + (index * barSlotWidth) + (barSlotWidth / 2)
                    val barLeft = slotCenterX - (barWidth / 2)

                    // Active guide line on selected bar
                    if (isSelected) {
                        drawLine(
                            color = PrimaryBlue.copy(alpha = 0.25f),
                            start = Offset(slotCenterX, topPadding),
                            end = Offset(slotCenterX, topPadding + chartHeight),
                            strokeWidth = barWidth * 1.5f
                        )
                    }

                    // Background bar (Target/Assigned)
                    val targetHeight = (stat.totalAssigned.toFloat() / maxDeliveries) * chartHeight * animationProgress.value
                    val targetTop = topPadding + chartHeight - targetHeight
                    drawRoundRect(
                        color = SurfaceVariantDark.copy(alpha = 0.8f),
                        topLeft = Offset(barLeft - 2.dp.toPx(), targetTop),
                        size = Size(barWidth + 4.dp.toPx(), targetHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Foreground bar (Completed deliveries)
                    val deliveredHeight = (stat.completedDeliveries.toFloat() / maxDeliveries) * chartHeight * animationProgress.value
                    val deliveredTop = topPadding + chartHeight - deliveredHeight

                    val barBrush = Brush.verticalGradient(
                        colors = if (isSelected) {
                            listOf(Color(0xFF67E8F9), PrimaryBlue)
                        } else if (stat.isToday) {
                            listOf(SecondaryTeal, SecondaryTeal.copy(alpha = 0.7f))
                        } else {
                            listOf(PrimaryBlue, PrimaryBlueVariant)
                        },
                        startY = deliveredTop,
                        endY = topPadding + chartHeight
                    )

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(barLeft, deliveredTop),
                        size = Size(barWidth, deliveredHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Value label on top of bar if selected
                    if (isSelected) {
                        val valText = textMeasurer.measure(
                            text = "${stat.completedDeliveries}",
                            style = TextStyle(color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textLayoutResult = valText,
                            topLeft = Offset(slotCenterX - (valText.size.width / 2), (deliveredTop - valText.size.height - 3.dp.toPx()).coerceAtLeast(0f))
                        )
                    }

                    // X-Axis Day Label (Pzt, Sal, etc.)
                    val labelColor = if (isSelected) PrimaryBlue else if (stat.isToday) SecondaryTeal else TextSecondary
                    val labelWeight = if (isSelected || stat.isToday) FontWeight.Bold else FontWeight.Normal

                    val dayLayout = textMeasurer.measure(
                        text = stat.dayLabel,
                        style = TextStyle(color = labelColor, fontSize = 10.sp, fontWeight = labelWeight)
                    )
                    drawText(
                        textLayoutResult = dayLayout,
                        topLeft = Offset(slotCenterX - (dayLayout.size.width / 2), topPadding + chartHeight + 6.dp.toPx())
                    )

                    // Sub date label (15 Eyl)
                    val dateLayout = textMeasurer.measure(
                        text = stat.dateLabel.split(" ").firstOrNull() ?: "",
                        style = TextStyle(color = TextMuted, fontSize = 8.5.sp)
                    )
                    drawText(
                        textLayoutResult = dateLayout,
                        topLeft = Offset(slotCenterX - (dateLayout.size.width / 2), topPadding + chartHeight + 18.dp.toPx())
                    )
                }
            }
        }

        // 3. Recharts-style Tooltip Card (Synchronized with selected bar)
        val selectedDay = dailyStats.getOrNull(selectedIndex)
        if (selectedDay != null) {
            Spacer(modifier = Modifier.height(10.dp))
            RechartsTooltipCard(selectedDay)
        }
    }
}

/**
 * Recharts Tooltip Component:
 * Clean, floating popover showing precise delivery statistics for the selected data point.
 */
@Composable
fun RechartsTooltipCard(stat: DailyDeliveryStat, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBlue.copy(alpha = 0.5f))),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_tooltip_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${stat.fullDayName} (${stat.dateLabel})",
                        style = MaterialTheme.typography.titleSmall.copy(
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
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Teslimat: ${stat.completedDeliveries} / ${stat.totalAssigned}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = "Zamanında: %${stat.onTimeRate.toInt()}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (stat.completionRate >= 92f) StatusSuccess.copy(alpha = 0.15f) else StatusWarning.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "%${String.format(java.util.Locale.US, "%.1f", stat.completionRate)} Başarı",
                        color = if (stat.completionRate >= 92f) StatusSuccess else StatusWarning,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "₺${stat.earnings.toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CashGold
                    )
                )
            }
        }
    }
}

/**
 * Recharts-style Radial Gauge & Completion Ring Component:
 * Displays weekly overall completion rate with arc progress, target benchmark notch,
 * and high-contrast central performance metric.
 */
@Composable
fun RechartsRadialGauge(
    completionRate: Float,
    targetRate: Float = 92.0f,
    onTimeRate: Float = 97.8f,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(completionRate) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = (completionRate / 100f).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HAFTALIK TAMAMLANMA ORANI",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (completionRate >= targetRate) StatusSuccess.copy(alpha = 0.18f) else StatusWarning.copy(alpha = 0.18f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (completionRate >= targetRate) Icons.Default.CheckCircle else Icons.Default.Flag,
                            contentDescription = null,
                            tint = if (completionRate >= targetRate) StatusSuccess else StatusWarning,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (completionRate >= targetRate) "HEDEF AŞILDI" else "HEDEF ALTI",
                            color = if (completionRate >= targetRate) StatusSuccess else StatusWarning,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gauge Center Visualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(170.dp)
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // 240-degree open gauge starting from 150 degrees
                        val startAngle = 150f
                        val sweepAngle = 240f

                        // Background track arc
                        drawArc(
                            color = SurfaceVariantDark,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Target line benchmark tick (at 92%)
                        val targetAngle = startAngle + (sweepAngle * (targetRate / 100f))
                        val rad = Math.toRadians(targetAngle.toDouble())
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = diameter / 2
                        val tickStart = Offset(
                            (center.x + (radius - 12.dp.toPx()) * cos(rad)).toFloat(),
                            (center.y + (radius - 12.dp.toPx()) * sin(rad)).toFloat()
                        )
                        val tickEnd = Offset(
                            (center.x + (radius + 8.dp.toPx()) * cos(rad)).toFloat(),
                            (center.y + (radius + 8.dp.toPx()) * sin(rad)).toFloat()
                        )
                        drawLine(
                            color = CashGold,
                            start = tickStart,
                            end = tickEnd,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Foreground completed progress arc
                        val progressSweep = sweepAngle * animatedProgress.value
                        val arcGradient = Brush.sweepGradient(
                            listOf(PrimaryBlue, SecondaryTeal, StatusSuccess)
                        )
                        drawArc(
                            brush = arcGradient,
                            startAngle = startAngle,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Centered Text KPI
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "%${String.format(java.util.Locale.US, "%.1f", completionRate)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 32.sp
                            )
                        )
                        Text(
                            text = "Haftalık Başarı",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hedef: %${targetRate.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CashGold,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Breakdown Sub-indicators
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GaugeMetricRow(
                        title = "Zamanında Teslim",
                        value = "%${String.format(java.util.Locale.US, "%.1f", onTimeRate)}",
                        color = StatusSuccess,
                        subtext = "KPI hedefi %95"
                    )
                    GaugeMetricRow(
                        title = "Hedef Sapması",
                        value = if (completionRate >= targetRate) "+%${String.format(java.util.Locale.US, "%.1f", completionRate - targetRate)}" else "-%${String.format(java.util.Locale.US, "%.1f", targetRate - completionRate)}",
                        color = if (completionRate >= targetRate) StatusSuccess else StatusWarning,
                        subtext = "Şirket normuna göre"
                    )
                    GaugeMetricRow(
                        title = "Haftalık Prim",
                        value = "₺1,450",
                        color = CashGold,
                        subtext = "%95+ prim baremi"
                    )
                }
            }
        }
    }
}

@Composable
private fun GaugeMetricRow(
    title: String,
    value: String,
    color: Color,
    subtext: String
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "• $subtext", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
        }
    }
}

/**
 * Recharts-style Area & Line Chart for Daily Completion Rate Trends:
 * Shows the % curve across the week, target benchmark reference line,
 * and filled gradient area underneath.
 */
@Composable
fun RechartsCompletionTrendLineChart(
    dailyStats: List<DailyDeliveryStat>,
    targetRate: Float = 92f,
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
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GÜNLÜK TAMAMLANMA EĞRİSİ",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CashGold)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hedef Eşik (%${targetRate.toInt()})",
                        fontSize = 10.sp,
                        color = CashGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val leftPadding = 34.dp.toPx()
                    val rightPadding = 16.dp.toPx()
                    val topPadding = 16.dp.toPx()
                    val bottomPadding = 28.dp.toPx()

                    val chartWidth = size.width - leftPadding - rightPadding
                    val chartHeight = size.height - topPadding - bottomPadding

                    val minRate = 80f
                    val maxRate = 100f
                    val rateRange = maxRate - minRate

                    // 1. Grid Lines and Y-Ticks (80%, 90%, 100%)
                    val ticks = listOf(100f, 95f, 90f, 85f, 80f)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

                    ticks.forEach { tick ->
                        val yPos = topPadding + (chartHeight * (1f - ((tick - minRate) / rateRange)))
                        drawLine(
                            color = CardBorderDark.copy(alpha = 0.4f),
                            start = Offset(leftPadding, yPos),
                            end = Offset(size.width - rightPadding, yPos),
                            strokeWidth = 1f,
                            pathEffect = dashEffect
                        )
                        val tLayout = textMeasurer.measure(
                            text = "%${tick.toInt()}",
                            style = TextStyle(color = TextMuted, fontSize = 8.5.sp)
                        )
                        drawText(
                            textLayoutResult = tLayout,
                            topLeft = Offset(leftPadding - tLayout.size.width - 4.dp.toPx(), yPos - tLayout.size.height / 2)
                        )
                    }

                    // 2. Reference Line (Recharts <ReferenceLine>) for Target
                    val targetY = topPadding + (chartHeight * (1f - ((targetRate - minRate) / rateRange)))
                    drawLine(
                        color = CashGold.copy(alpha = 0.8f),
                        start = Offset(leftPadding, targetY),
                        end = Offset(size.width - rightPadding, targetY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    // 3. Construct Spline Curve & Area Path
                    if (dailyStats.isNotEmpty()) {
                        val stepX = chartWidth / (dailyStats.size - 1).coerceAtLeast(1)
                        val points = dailyStats.mapIndexed { index, stat ->
                            val x = leftPadding + (index * stepX)
                            val normalizedY = ((stat.completionRate - minRate) / rateRange).coerceIn(0f, 1f)
                            val y = topPadding + chartHeight * (1f - (normalizedY * animatedProgress.value))
                            Offset(x, y)
                        }

                        // Path for line
                        val linePath = Path()
                        val areaPath = Path()

                        points.forEachIndexed { i, pt ->
                            if (i == 0) {
                                linePath.moveTo(pt.x, pt.y)
                                areaPath.moveTo(pt.x, topPadding + chartHeight)
                                areaPath.lineTo(pt.x, pt.y)
                            } else {
                                val prev = points[i - 1]
                                val cx1 = prev.x + (pt.x - prev.x) / 2
                                val cy1 = prev.y
                                val cx2 = prev.x + (pt.x - prev.x) / 2
                                val cy2 = pt.y
                                linePath.cubicTo(cx1, cy1, cx2, cy2, pt.x, pt.y)
                                areaPath.cubicTo(cx1, cy1, cx2, cy2, pt.x, pt.y)
                            }
                        }

                        areaPath.lineTo(points.last().x, topPadding + chartHeight)
                        areaPath.close()

                        // Draw shaded area underneath (Recharts Area fill)
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(SecondaryTeal.copy(alpha = 0.35f), Color.Transparent),
                                startY = topPadding,
                                endY = topPadding + chartHeight
                            )
                        )

                        // Draw smooth line
                        drawPath(
                            path = linePath,
                            color = SecondaryTeal,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw circular nodes
                        points.forEachIndexed { index, pt ->
                            val stat = dailyStats[index]
                            val isAboveTarget = stat.completionRate >= targetRate

                            // Outer glow ring
                            drawCircle(
                                color = if (isAboveTarget) StatusSuccess.copy(alpha = 0.3f) else StatusWarning.copy(alpha = 0.3f),
                                radius = 7.dp.toPx(),
                                center = pt
                            )
                            // Inner filled node
                            drawCircle(
                                color = if (isAboveTarget) StatusSuccess else StatusWarning,
                                radius = 4.dp.toPx(),
                                center = pt
                            )

                            // X-Axis Day label
                            val dayLayout = textMeasurer.measure(
                                text = stat.dayLabel,
                                style = TextStyle(color = TextSecondary, fontSize = 9.sp)
                            )
                            drawText(
                                textLayoutResult = dayLayout,
                                topLeft = Offset(pt.x - (dayLayout.size.width / 2), topPadding + chartHeight + 6.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}
