package com.example.domain.model

/**
 * Detailed daily delivery statistics for chart visualization.
 */
data class DailyDeliveryStat(
    val dayKey: String, // "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"
    val dayLabel: String, // "Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"
    val fullDayName: String, // "Pazartesi", "Salı", etc.
    val dateLabel: String, // "15 Eyl", "16 Eyl"
    val totalAssigned: Int, // e.g. 24
    val completedDeliveries: Int, // e.g. 23
    val cancelledDeliveries: Int = 1,
    val targetDeliveries: Int = 20,
    val completionRate: Float, // e.g. 95.8f
    val onTimeRate: Float, // e.g. 98.0f
    val earnings: Double, // e.g. 1450.0
    val distanceKm: Double, // e.g. 52.4
    val avgDeliveryMinutes: Int = 21,
    val isToday: Boolean = false
) {
    // Calculated efficiency metrics
    val kmPerDelivery: Float
        get() = if (completedDeliveries > 0) (distanceKm.toFloat() / completedDeliveries) else 0f

    val deliveriesPer10Km: Float
        get() = if (distanceKm > 0) (completedDeliveries.toFloat() / (distanceKm.toFloat() / 10f)) else 0f

    val efficiencyRating: String
        get() = when {
            kmPerDelivery <= 2.3f -> "Çok Yüksek"
            kmPerDelivery <= 2.6f -> "Optimal"
            else -> "Standart"
        }
}

/**
 * Time slot breakdown for intraday efficiency visualization over time.
 */
data class HourlyEfficiencyStat(
    val slotKey: String,
    val timeSlotLabel: String, // "09:00 - 12:00"
    val shortLabel: String, // "09-12"
    val completedDeliveries: Int,
    val distanceKm: Double,
    val avgMinutesPerDelivery: Int
) {
    val kmPerDelivery: Float
        get() = if (completedDeliveries > 0) (distanceKm.toFloat() / completedDeliveries) else 0f

    val deliveriesPer10Km: Float
        get() = if (distanceKm > 0) (completedDeliveries.toFloat() / (distanceKm.toFloat() / 10f)) else 0f
}

/**
 * Aggregate weekly summary statistics and comparative metrics.
 */
data class WeeklyStatsSummary(
    val weekPeriodLabel: String, // e.g. "15 - 21 Eylül 2026"
    val weekIndex: Int = 0, // 0: This Week, 1: Last Week, 2: 2 Weeks Ago
    val totalAssigned: Int = 188,
    val totalCompleted: Int = 180,
    val totalCancelled: Int = 5,
    val overallCompletionRate: Float = 95.7f,
    val targetCompletionRate: Float = 92.0f,
    val overallOnTimeRate: Float = 97.8f,
    val totalEarnings: Double = 11600.0,
    val totalDistanceKm: Double = 410.0,
    val avgDeliveryMinutes: Int = 21,
    val bonusEarned: Double = 1250.0,
    val dailyStats: List<DailyDeliveryStat> = emptyList(),
    val hourlyStats: List<HourlyEfficiencyStat> = emptyList(),
    val completionRateDeltaVsLastWeek: Float = 2.1f, // +2.1%
    val deliveriesDeltaVsLastWeek: Int = 19 // +19 deliveries
) {
    val avgKmPerDelivery: Float
        get() = if (totalCompleted > 0) (totalDistanceKm.toFloat() / totalCompleted) else 0f

    val overallDeliveriesPer10Km: Float
        get() = if (totalDistanceKm > 0) (totalCompleted.toFloat() / (totalDistanceKm.toFloat() / 10f)) else 0f

    val efficiencyScore: Int
        get() = ((1f / avgKmPerDelivery.coerceAtLeast(1.5f)) * 215).toInt().coerceIn(70, 99)
}

