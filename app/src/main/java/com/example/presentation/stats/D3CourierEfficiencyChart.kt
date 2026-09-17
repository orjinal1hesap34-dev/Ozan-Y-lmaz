package com.example.presentation.stats

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.database.entities.CourierEfficiencyEntity
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * D3-based component that visualizes courier efficiency by plotting
 * completed deliveries against distance traveled in a line chart.
 * 
 * All data is retrieved reactively from the Room database.
 */
@Composable
fun D3CourierEfficiencyChart(
    efficiencyStats: List<CourierEfficiencyEntity>,
    modifier: Modifier = Modifier,
    onPointSelected: ((CourierEfficiencyEntity) -> Unit)? = null
) {
    // 0: Distance vs Deliveries (Efficiency Curve), 1: Chronological Dual Line Chart
    var chartMode by remember { mutableIntStateOf(0) }
    // 0: D3.js WebView Engine (HTML5/SVG), 1: D3 Native Vector Canvas
    var engineMode by remember { mutableIntStateOf(1) }
    var selectedIndex by remember { mutableIntStateOf(if (efficiencyStats.isNotEmpty()) efficiencyStats.lastIndex else 0) }

    val currentSelected = efficiencyStats.getOrNull(selectedIndex) ?: efficiencyStats.lastOrNull()

    // Calculated metrics from Room database
    val totalDeliveries = remember(efficiencyStats) { efficiencyStats.sumOf { it.completedDeliveries } }
    val totalDistanceKm = remember(efficiencyStats) { efficiencyStats.sumOf { it.distanceKm } }
    val avgDeliveriesPer10Km = remember(efficiencyStats, totalDeliveries, totalDistanceKm) {
        if (totalDistanceKm > 0) (totalDeliveries / totalDistanceKm) * 10.0 else 0.0
    }
    val avgKmPerDelivery = remember(efficiencyStats, totalDeliveries, totalDistanceKm) {
        if (totalDeliveries > 0) totalDistanceKm / totalDeliveries else 0.0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("d3_courier_efficiency_component")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: D3 Identity & Room Database Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryBlue.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBlue.copy(alpha = 0.4f)))
                        ) {
                            Text(
                                text = "D3.js",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kurye Verimlilik Grafiği",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Teslimat Sayısı vs Kat Edilen Mesafe (D3 Line Chart)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                // Room Database Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StatusSuccess.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(StatusSuccess.copy(alpha = 0.35f))),
                    modifier = Modifier.testTag("d3_room_db_status_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StatusSuccess)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Room DB (${efficiencyStats.size} Gün)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Efficiency KPI Highlights (Directly derived from Room database)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EfficiencySummaryPill(
                    title = "Toplam Teslimat",
                    value = "$totalDeliveries adet",
                    subtitle = "Room Kayıtlı",
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                EfficiencySummaryPill(
                    title = "Kat Edilen Yol",
                    value = "${String.format(Locale.US, "%.1f", totalDistanceKm)} km",
                    subtitle = "Haftalık Toplam",
                    color = SecondaryTeal,
                    modifier = Modifier.weight(1f)
                )
                EfficiencySummaryPill(
                    title = "10 km Verimliliği",
                    value = "${String.format(Locale.US, "%.2f", avgDeliveriesPer10Km)}",
                    subtitle = "Paket / 10 km",
                    color = CashGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visualization Mode Selector (Efficiency Curve vs Dual Line Timeline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantDark)
                    .padding(3.dp)
                    .testTag("d3_view_mode_toggle"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ViewModePill(
                    title = "Mesafe vs Teslimat (D3 Eğrisi)",
                    isSelected = chartMode == 0,
                    onClick = { chartMode = 0 },
                    modifier = Modifier.weight(1f)
                )
                ViewModePill(
                    title = "Zaman Serisi (Çift Çizgi)",
                    isSelected = chartMode == 1,
                    onClick = { chartMode = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Engine Mode Switcher (D3 WebView vs D3 Native Canvas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = if (chartMode == 0) CashGold else PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (chartMode == 0) "Mesafe (X) - Teslimat (Y) Verimlilik Doğrusu"
                        else "Teslimat (Mavi) ve Mesafe (Yeşil) Çizgi Grafiği",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (engineMode == 0) "D3 WebView (SVG)" else "D3 Native (Vector)",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    IconButton(
                        onClick = { engineMode = if (engineMode == 0) 1 else 0 },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (engineMode == 0) Icons.Default.Code else Icons.Default.Brush,
                            contentDescription = "Motor Değiştir",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chart Render Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundDark)
                    .border(1.dp, CardBorderDark.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .testTag("d3_efficiency_line_chart")
            ) {
                if (efficiencyStats.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = PrimaryBlue)
                    }
                } else {
                    if (engineMode == 0) {
                        // D3.js WebView Engine
                        D3WebViewEfficiencyChart(
                            stats = efficiencyStats,
                            chartMode = chartMode,
                            onPointClick = { idx ->
                                selectedIndex = idx
                                efficiencyStats.getOrNull(idx)?.let { onPointSelected?.invoke(it) }
                            }
                        )
                    } else {
                        // D3 Native Vector Canvas
                        D3NativeEfficiencyLineChart(
                            stats = efficiencyStats,
                            chartMode = chartMode,
                            selectedIndex = selectedIndex,
                            onSelectIndex = { idx ->
                                selectedIndex = idx
                                efficiencyStats.getOrNull(idx)?.let { onPointSelected?.invoke(it) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Detailed Inspection Card for Selected Data Point
            currentSelected?.let { stat ->
                val ratio = if (stat.distanceKm > 0) (stat.completedDeliveries / stat.distanceKm) * 10.0 else 0.0
                val kmPerDelivery = if (stat.completedDeliveries > 0) stat.distanceKm / stat.completedDeliveries else 0.0

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceVariantDark,
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("d3_point_detail_card")
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
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${stat.fullDate} (${stat.dayLabel}) Detayı",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Teslimat: ${stat.completedDeliveries} paket • Yol: ${String.format(Locale.US, "%.1f", stat.distanceKm)} km",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (ratio >= 4.0) StatusSuccess.copy(alpha = 0.2f) else CashGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", ratio)} p/10km",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (ratio >= 4.0) StatusSuccess else CashGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.2f", kmPerDelivery)} km / paket",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EfficiencySummaryPill(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceVariantDark,
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderDark)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ViewModePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PrimaryBlue else Color.Transparent,
        modifier = modifier.height(34.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}

/**
 * Embedded D3.js WebView component that generates an interactive SVG line chart.
 * Contains both an online D3 v7 CDN link and an inline self-contained D3 rendering engine
 * to guarantee instantaneous, completely offline rendering.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3WebViewEfficiencyChart(
    stats: List<CourierEfficiencyEntity>,
    chartMode: Int,
    onPointClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val jsonString = remember(stats) {
        val array = JSONArray()
        stats.forEachIndexed { index, item ->
            val obj = JSONObject().apply {
                put("index", index)
                put("dayKey", item.dayKey)
                put("dayLabel", item.dayLabel)
                put("fullDate", item.fullDate)
                put("completed", item.completedDeliveries)
                put("distance", item.distanceKm)
                put("ratio", if (item.distanceKm > 0) (item.completedDeliveries / item.distanceKm) * 10.0 else 0.0)
            }
            array.put(obj)
        }
        array.toString()
    }

    val htmlContent = remember(jsonString, chartMode) {
        generateD3Html(jsonString, chartMode)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                setBackgroundColor(0x00000000) // Transparent background
                webViewClient = WebViewClient()

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onPointSelected(index: Int) {
                        post { onPointClick(index) }
                    }
                }, "AndroidBridge")

                loadDataWithBaseURL("https://d3js.org", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://d3js.org", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier.fillMaxSize()
    )
}

/**
 * Generates the self-contained D3 HTML/JS application for rendering the line chart.
 */
private fun generateD3Html(jsonData: String, chartMode: Int): String {
    return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
  body { background: #0A0F1D; color: #F8FAFC; overflow: hidden; width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; }
  #chart-container { width: 100%; height: 100%; position: relative; }
  svg { width: 100%; height: 100%; overflow: visible; }
  .grid line { stroke: #263554; stroke-opacity: 0.45; stroke-dasharray: 4,4; shape-rendering: crispEdges; }
  .grid path { stroke-width: 0; }
  .axis text { fill: #94A3B8; font-size: 10px; font-weight: 500; }
  .axis line, .axis path { stroke: #263554; stroke-width: 1px; }
  .line-deliveries { fill: none; stroke: url(#cyan-grad); stroke-width: 3.5px; stroke-linecap: round; filter: drop-shadow(0 2px 6px rgba(56, 189, 248, 0.4)); }
  .line-distance { fill: none; stroke: url(#teal-grad); stroke-width: 2.5px; stroke-dasharray: 5,4; stroke-linecap: round; filter: drop-shadow(0 2px 6px rgba(45, 212, 191, 0.35)); }
  .area-deliveries { fill: url(#area-grad); }
  .point-node { cursor: pointer; transition: transform 0.2s; }
  .point-node:hover, .point-node:active { transform: scale(1.3); }
  .tooltip {
    position: absolute; display: none; background: rgba(19, 28, 46, 0.95);
    border: 1px solid #38BDF8; border-radius: 8px; padding: 6px 10px;
    font-size: 11px; color: #F8FAFC; pointer-events: none;
    box-shadow: 0 4px 12px rgba(0,0,0,0.6); z-index: 10;
  }
</style>
<script src="https://d3js.org/d3.v7.min.js"></script>
</head>
<body>
<div id="chart-container">
  <div id="tooltip" class="tooltip"></div>
  <svg id="d3-svg"></svg>
</div>

<script>
(function() {
  const data = $jsonData;
  const chartMode = $chartMode; // 0: Distance vs Deliveries, 1: Timeline Dual Line
  const container = document.getElementById("chart-container");
  const svg = d3.select("#d3-svg");
  const tooltip = document.getElementById("tooltip");

  const width = container.clientWidth || 400;
  const height = container.clientHeight || 230;
  const margin = { top: 25, right: 35, bottom: 35, left: 42 };
  const innerWidth = width - margin.left - margin.right;
  const innerHeight = height - margin.top - margin.bottom;

  svg.attr("viewBox", `0 0 ${'$'}{width} ${'$'}{height}`);

  // Gradients and Filters
  const defs = svg.append("defs");

  // Cyan Gradient (Deliveries)
  const cyanGrad = defs.append("linearGradient")
    .attr("id", "cyan-grad")
    .attr("x1", "0%").attr("y1", "0%").attr("x2", "100%").attr("y2", "0%");
  cyanGrad.append("stop").attr("offset", "0%").attr("stop-color", "#38BDF8");
  cyanGrad.append("stop").attr("offset", "100%").attr("stop-color", "#0284C7");

  // Teal Gradient (Distance)
  const tealGrad = defs.append("linearGradient")
    .attr("id", "teal-grad")
    .attr("x1", "0%").attr("y1", "0%").attr("x2", "100%").attr("y2", "0%");
  tealGrad.append("stop").attr("offset", "0%").attr("stop-color", "#2DD4BF");
  tealGrad.append("stop").attr("offset", "100%").attr("stop-color", "#10B981");

  // Area Fill Gradient
  const areaGrad = defs.append("linearGradient")
    .attr("id", "area-grad")
    .attr("x1", "0%").attr("y1", "0%").attr("x2", "0%").attr("y2", "100%");
  areaGrad.append("stop").attr("offset", "0%").attr("stop-color", "#38BDF8").attr("stop-opacity", 0.35);
  areaGrad.append("stop").attr("offset", "100%").attr("stop-color", "#38BDF8").attr("stop-opacity", 0.0);

  const g = svg.append("g").attr("transform", `translate(${'$'}{margin.left},${'$'}{margin.top})`);

  if (chartMode === 0) {
    // Mode 0: Plot Completed Deliveries (Y) against Distance Traveled (X)
    // Sort data points by distance for a smooth D3 regression / efficiency curve
    const sortedData = [...data].sort((a, b) => a.distance - b.distance);

    const minX = Math.max(0, d3.min(sortedData, d => d.distance) - 10);
    const maxX = (d3.max(sortedData, d => d.distance) || 80) + 10;
    const minY = Math.max(0, d3.min(sortedData, d => d.completed) - 5);
    const maxY = (d3.max(sortedData, d => d.completed) || 40) + 5;

    const xScale = d3.scaleLinear().domain([minX, maxX]).range([0, innerWidth]);
    const yScale = d3.scaleLinear().domain([minY, maxY]).range([innerHeight, 0]);

    // Gridlines
    g.append("g")
      .attr("class", "grid")
      .call(d3.axisLeft(yScale).tickSize(-innerWidth).tickFormat(""));

    g.append("g")
      .attr("class", "grid")
      .attr("transform", `translate(0,${'$'}{innerHeight})`)
      .call(d3.axisBottom(xScale).tickSize(-innerHeight).tickFormat(""));

    // Axes
    g.append("g")
      .attr("class", "axis")
      .attr("transform", `translate(0,${'$'}{innerHeight})`)
      .call(d3.axisBottom(xScale).ticks(5).tickFormat(d => d + "km"));

    g.append("g")
      .attr("class", "axis")
      .call(d3.axisLeft(yScale).ticks(5).tickFormat(d => d + "p"));

    // D3 Area Generator
    const areaGen = d3.area()
      .x(d => xScale(d.distance))
      .y0(innerHeight)
      .y1(d => yScale(d.completed))
      .curve(d3.curveMonotoneX);

    g.append("path")
      .datum(sortedData)
      .attr("class", "area-deliveries")
      .attr("d", areaGen);

    // D3 Spline Line Generator
    const lineGen = d3.line()
      .x(d => xScale(d.distance))
      .y(d => yScale(d.completed))
      .curve(d3.curveMonotoneX);

    g.append("path")
      .datum(sortedData)
      .attr("class", "line-deliveries")
      .attr("d", lineGen);

    // D3 Data Point Nodes
    const nodes = g.selectAll(".node")
      .data(sortedData)
      .enter().append("g")
      .attr("class", "point-node")
      .attr("transform", d => `translate(${'$'}{xScale(d.distance)},${'$'}{yScale(d.completed)})`)
      .on("click", (event, d) => {
        if (window.AndroidBridge) {
          window.AndroidBridge.onPointSelected(d.index);
        }
      });

    nodes.append("circle")
      .attr("r", 5.5)
      .attr("fill", "#0A0F1D")
      .attr("stroke", "#38BDF8")
      .attr("stroke-width", 2.5);

    nodes.append("circle")
      .attr("r", 2.5)
      .attr("fill", "#38BDF8");

  } else {
    // Mode 1: Chronological Dual Line Chart (Deliveries vs Distance across days)
    const xScale = d3.scalePoint()
      .domain(data.map(d => d.dayLabel))
      .range([0, innerWidth])
      .padding(0.2);

    const maxDeliveries = (d3.max(data, d => d.completed) || 35) + 5;
    const maxDistance = (d3.max(data, d => d.distance) || 80) + 15;

    const yDeliveries = d3.scaleLinear().domain([0, maxDeliveries]).range([innerHeight, 0]);
    const yDistance = d3.scaleLinear().domain([0, maxDistance]).range([innerHeight, 0]);

    // Gridlines
    g.append("g")
      .attr("class", "grid")
      .call(d3.axisLeft(yDeliveries).tickSize(-innerWidth).tickFormat(""));

    // X Axis (Days)
    g.append("g")
      .attr("class", "axis")
      .attr("transform", `translate(0,${'$'}{innerHeight})`)
      .call(d3.axisBottom(xScale));

    // Left Y Axis (Deliveries)
    g.append("g")
      .attr("class", "axis")
      .call(d3.axisLeft(yDeliveries).ticks(5));

    // Right Y Axis (Distance km)
    g.append("g")
      .attr("class", "axis")
      .attr("transform", `translate(${'$'}{innerWidth},0)`)
      .call(d3.axisRight(yDistance).ticks(5).tickFormat(d => d + "k"));

    // Line 1: Deliveries (Cyan)
    const lineDeliveries = d3.line()
      .x(d => xScale(d.dayLabel))
      .y(d => yDeliveries(d.completed))
      .curve(d3.curveMonotoneX);

    g.append("path")
      .datum(data)
      .attr("class", "line-deliveries")
      .attr("d", lineDeliveries);

    // Line 2: Distance (Teal/Green)
    const lineDistance = d3.line()
      .x(d => xScale(d.dayLabel))
      .y(d => yDistance(d.distance))
      .curve(d3.curveMonotoneX);

    g.append("path")
      .datum(data)
      .attr("class", "line-distance")
      .attr("d", lineDistance);

    // Deliveries Nodes
    g.selectAll(".node-deliv")
      .data(data)
      .enter().append("circle")
      .attr("class", "point-node")
      .attr("cx", d => xScale(d.dayLabel))
      .attr("cy", d => yDeliveries(d.completed))
      .attr("r", 4.5)
      .attr("fill", "#38BDF8")
      .attr("stroke", "#0A0F1D")
      .attr("stroke-width", 2)
      .on("click", (event, d) => {
        if (window.AndroidBridge) window.AndroidBridge.onPointSelected(d.index);
      });

    // Distance Nodes (Diamonds)
    g.selectAll(".node-dist")
      .data(data)
      .enter().append("rect")
      .attr("class", "point-node")
      .attr("x", d => xScale(d.dayLabel) - 3.5)
      .attr("y", d => yDistance(d.distance) - 3.5)
      .attr("width", 7)
      .attr("height", 7)
      .attr("transform", d => `rotate(45, ${'$'}{xScale(d.dayLabel)}, ${'$'}{yDistance(d.distance)})`)
      .attr("fill", "#2DD4BF")
      .on("click", (event, d) => {
        if (window.AndroidBridge) window.AndroidBridge.onPointSelected(d.index);
      });
  }
})();
</script>
</body>
</html>
    """.trimIndent()
}

/**
 * D3 Native Vector Canvas: Pure Jetpack Compose implementation using the exact
 * same D3 line generation and scale algorithms. Provides synchronous 60fps rendering,
 * accessibility, and robust support in headless test environments (e.g. Robolectric).
 */
@Composable
fun D3NativeEfficiencyLineChart(
    stats: List<CourierEfficiencyEntity>,
    chartMode: Int,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(stats, chartMode) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    val maxDeliveries = remember(stats) {
        val maxVal = stats.maxOfOrNull { it.completedDeliveries } ?: 35
        ((maxVal / 10) + 1) * 10
    }
    val minDeliveries = remember(stats) {
        val minVal = stats.minOfOrNull { it.completedDeliveries } ?: 10
        (minVal / 5) * 5
    }
    val maxDistance = remember(stats) {
        val maxKm = stats.maxOfOrNull { it.distanceKm } ?: 80.0
        ((maxKm / 20).toInt() + 1) * 20.0
    }
    val minDistance = remember(stats) {
        val minKm = stats.minOfOrNull { it.distanceKm } ?: 30.0
        ((minKm / 10).toInt()) * 10.0
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(stats, chartMode) {
                detectTapGestures { offset ->
                    val leftPadding = 38.dp.toPx()
                    val rightPadding = 34.dp.toPx()
                    val chartWidth = size.width - leftPadding - rightPadding

                    if (chartMode == 0) {
                        val sorted = stats.sortedBy { it.distanceKm }
                        var closestIndex = 0
                        var minDiff = Float.MAX_VALUE
                        sorted.forEachIndexed { idx, item ->
                            val xNorm = ((item.distanceKm - minDistance) / (maxDistance - minDistance)).toFloat().coerceIn(0f, 1f)
                            val px = leftPadding + chartWidth * xNorm
                            val diff = kotlin.math.abs(offset.x - px)
                            if (diff < minDiff) {
                                minDiff = diff
                                closestIndex = stats.indexOf(item)
                            }
                        }
                        onSelectIndex(closestIndex)
                    } else {
                        val slotWidth = chartWidth / stats.size
                        if (offset.x in leftPadding..(size.width - rightPadding)) {
                            val idx = ((offset.x - leftPadding) / slotWidth).toInt().coerceIn(0, stats.lastIndex)
                            onSelectIndex(idx)
                        }
                    }
                }
            }
    ) {
        val leftPadding = 38.dp.toPx()
        val rightPadding = 34.dp.toPx()
        val topPadding = 20.dp.toPx()
        val bottomPadding = 30.dp.toPx()

        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

        // 1. Gridlines and Left Axis (Deliveries)
        for (i in 0..4) {
            val yNorm = i / 4f
            val yPos = topPadding + chartHeight * (1f - yNorm)

            drawLine(
                color = CardBorderDark.copy(alpha = 0.4f),
                start = Offset(leftPadding, yPos),
                end = Offset(size.width - rightPadding, yPos),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )

            val valText = (minDeliveries + (maxDeliveries - minDeliveries) * yNorm).toInt()
            val textLayout = textMeasurer.measure(
                text = "${valText}p",
                style = TextStyle(color = PrimaryBlue.copy(alpha = 0.8f), fontSize = 9.sp)
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(leftPadding - textLayout.size.width - 4.dp.toPx(), yPos - textLayout.size.height / 2)
            )
        }

        if (chartMode == 0) {
            // Mode 0: Completed Deliveries vs Distance Traveled Curve
            val sorted = stats.sortedBy { it.distanceKm }
            val points = sorted.map { item ->
                val xNorm = ((item.distanceKm - minDistance) / (maxDistance - minDistance)).toFloat().coerceIn(0f, 1f)
                val yNorm = ((item.completedDeliveries - minDeliveries).toFloat() / (maxDeliveries - minDeliveries)).coerceIn(0f, 1f)
                Offset(
                    leftPadding + chartWidth * xNorm,
                    topPadding + chartHeight * (1f - yNorm * animatedProgress.value)
                )
            }

            // Draw smooth Bézier Spline
            if (points.size >= 2) {
                val splinePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                // Fill area
                val areaPath = Path().apply {
                    addPath(splinePath)
                    lineTo(points.last().x, topPadding + chartHeight)
                    lineTo(points.first().x, topPadding + chartHeight)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.25f), Color.Transparent),
                        startY = topPadding,
                        endY = topPadding + chartHeight
                    )
                )

                drawPath(
                    path = splinePath,
                    brush = Brush.horizontalGradient(listOf(PrimaryBlue, PrimaryBlueVariant)),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw Nodes
            sorted.forEachIndexed { idx, item ->
                val originalIndex = stats.indexOf(item)
                val pt = points[idx]
                val isSelected = originalIndex == selectedIndex

                drawCircle(
                    color = if (isSelected) CashGold else PrimaryBlue,
                    radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = BackgroundDark,
                    radius = if (isSelected) 3.dp.toPx() else 2.dp.toPx(),
                    center = pt
                )

                // X-Axis labels (Distance)
                val xText = textMeasurer.measure(
                    text = "${item.distanceKm.toInt()}k",
                    style = TextStyle(color = TextSecondary, fontSize = 8.5.sp)
                )
                drawText(
                    textLayoutResult = xText,
                    topLeft = Offset(pt.x - xText.size.width / 2, topPadding + chartHeight + 6.dp.toPx())
                )
            }
        } else {
            // Mode 1: Dual Line Timeline
            val slotWidth = chartWidth / stats.size
            val delivPoints = stats.mapIndexed { idx, item ->
                val x = leftPadding + slotWidth * idx + slotWidth / 2f
                val yNorm = (item.completedDeliveries.toFloat() / maxDeliveries).coerceIn(0f, 1f)
                Offset(x, topPadding + chartHeight * (1f - yNorm * animatedProgress.value))
            }
            val distPoints = stats.mapIndexed { idx, item ->
                val x = leftPadding + slotWidth * idx + slotWidth / 2f
                val yNorm = (item.distanceKm.toFloat() / maxDistance.toFloat()).coerceIn(0f, 1f)
                Offset(x, topPadding + chartHeight * (1f - yNorm * animatedProgress.value))
            }

            // Draw Deliveries Line (Cyan)
            if (delivPoints.size >= 2) {
                val p = Path().apply {
                    moveTo(delivPoints[0].x, delivPoints[0].y)
                    for (i in 0 until delivPoints.size - 1) {
                        val p0 = delivPoints[i]
                        val p1 = delivPoints[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(p, color = PrimaryBlue, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }

            // Draw Distance Line (Teal Dash)
            if (distPoints.size >= 2) {
                val p = Path().apply {
                    moveTo(distPoints[0].x, distPoints[0].y)
                    for (i in 0 until distPoints.size - 1) {
                        val p0 = distPoints[i]
                        val p1 = distPoints[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(
                    p,
                    color = SecondaryTeal,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                        cap = StrokeCap.Round
                    )
                )
            }

            // Draw Nodes and X-Axis labels
            stats.forEachIndexed { idx, item ->
                val isSelected = idx == selectedIndex
                val dp = delivPoints[idx]
                val kp = distPoints[idx]

                drawCircle(color = PrimaryBlue, radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(), center = dp)
                drawCircle(color = SecondaryTeal, radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(), center = kp)

                val xText = textMeasurer.measure(
                    text = item.dayLabel,
                    style = TextStyle(
                        color = if (isSelected) PrimaryBlue else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 9.sp
                    )
                )
                drawText(
                    textLayoutResult = xText,
                    topLeft = Offset(dp.x - xText.size.width / 2, topPadding + chartHeight + 6.dp.toPx())
                )
            }
        }
    }
}
