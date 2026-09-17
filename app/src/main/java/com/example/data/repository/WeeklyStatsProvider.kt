package com.example.data.repository

import com.example.domain.model.DailyDeliveryStat
import com.example.domain.model.WeeklyStatsSummary

object WeeklyStatsProvider {

    fun getWeeklyStats(weekIndex: Int, todayCompletedCount: Int = 14): WeeklyStatsSummary {
        return when (weekIndex) {
            0 -> getThisWeekStats(todayCompletedCount)
            1 -> getLastWeekStats()
            else -> getTwoWeeksAgoStats()
        }
    }

    private fun getThisWeekStats(todayCompletedCount: Int): WeeklyStatsSummary {
        val sundayCompleted = 14 + todayCompletedCount.coerceAtLeast(0)
        val sundayAssigned = (sundayCompleted + 2).coerceAtLeast(18)
        val sundayRate = (sundayCompleted.toFloat() / sundayAssigned * 100f).coerceAtMost(100f)

        val days = listOf(
            DailyDeliveryStat(
                dayKey = "MON",
                dayLabel = "Pzt",
                fullDayName = "Pazartesi",
                dateLabel = "15 Eyl",
                totalAssigned = 24,
                completedDeliveries = 23,
                cancelledDeliveries = 1,
                targetDeliveries = 20,
                completionRate = 95.8f,
                onTimeRate = 98.2f,
                earnings = 1420.0,
                distanceKm = 52.4,
                avgDeliveryMinutes = 21,
                isToday = false
            ),
            DailyDeliveryStat(
                dayKey = "TUE",
                dayLabel = "Sal",
                fullDayName = "Salı",
                dateLabel = "16 Eyl",
                totalAssigned = 26,
                completedDeliveries = 25,
                cancelledDeliveries = 1,
                targetDeliveries = 22,
                completionRate = 96.2f,
                onTimeRate = 97.5f,
                earnings = 1580.0,
                distanceKm = 58.0,
                avgDeliveryMinutes = 22,
                isToday = false
            ),
            DailyDeliveryStat(
                dayKey = "WED",
                dayLabel = "Çar",
                fullDayName = "Çarşamba",
                dateLabel = "17 Eyl",
                totalAssigned = 28,
                completedDeliveries = 27,
                cancelledDeliveries = 1,
                targetDeliveries = 24,
                completionRate = 96.4f,
                onTimeRate = 99.1f,
                earnings = 1710.0,
                distanceKm = 61.2,
                avgDeliveryMinutes = 20,
                isToday = false
            ),
            DailyDeliveryStat(
                dayKey = "THU",
                dayLabel = "Per",
                fullDayName = "Perşembe",
                dateLabel = "18 Eyl",
                totalAssigned = 25,
                completedDeliveries = 24,
                cancelledDeliveries = 1,
                targetDeliveries = 22,
                completionRate = 96.0f,
                onTimeRate = 98.0f,
                earnings = 1520.0,
                distanceKm = 54.5,
                avgDeliveryMinutes = 21,
                isToday = false
            ),
            DailyDeliveryStat(
                dayKey = "FRI",
                dayLabel = "Cum",
                fullDayName = "Cuma",
                dateLabel = "19 Eyl",
                totalAssigned = 32,
                completedDeliveries = 31,
                cancelledDeliveries = 1,
                targetDeliveries = 26,
                completionRate = 96.9f,
                onTimeRate = 97.8f,
                earnings = 1980.0,
                distanceKm = 69.8,
                avgDeliveryMinutes = 23,
                isToday = false
            ),
            DailyDeliveryStat(
                dayKey = "SAT",
                dayLabel = "Cmt",
                fullDayName = "Cumartesi",
                dateLabel = "20 Eyl",
                totalAssigned = 35,
                completedDeliveries = 34,
                cancelledDeliveries = 1,
                targetDeliveries = 30,
                completionRate = 97.1f,
                onTimeRate = 98.4f,
                earnings = 2240.0,
                distanceKm = 78.2,
                avgDeliveryMinutes = 22,
                isToday = false
            ),
            DailyDeliveryStat(
                dayKey = "SUN",
                dayLabel = "Paz",
                fullDayName = "Pazar",
                dateLabel = "21 Eyl",
                totalAssigned = sundayAssigned,
                completedDeliveries = sundayCompleted,
                cancelledDeliveries = 0,
                targetDeliveries = 18,
                completionRate = sundayRate,
                onTimeRate = 98.5f,
                earnings = 1150.0 + (todayCompletedCount * 85.0),
                distanceKm = 38.0 + (todayCompletedCount * 2.5),
                avgDeliveryMinutes = 19,
                isToday = true
            )
        )

        val totalAssigned = days.sumOf { it.totalAssigned }
        val totalCompleted = days.sumOf { it.completedDeliveries }
        val totalCancelled = days.sumOf { it.cancelledDeliveries }
        val overallRate = (totalCompleted.toFloat() / totalAssigned * 100f)
        val totalEarnings = days.sumOf { it.earnings }
        val totalDist = days.sumOf { it.distanceKm }
        val avgDuration = (days.sumOf { it.avgDeliveryMinutes } / days.size)

        val hourlySlots = listOf(
            com.example.domain.model.HourlyEfficiencyStat(
                slotKey = "SLOT_MORNING",
                timeSlotLabel = "09:00 - 12:00",
                shortLabel = "09-12",
                completedDeliveries = 38,
                distanceKm = 82.5,
                avgMinutesPerDelivery = 18
            ),
            com.example.domain.model.HourlyEfficiencyStat(
                slotKey = "SLOT_LUNCH",
                timeSlotLabel = "12:00 - 15:00",
                shortLabel = "12-15",
                completedDeliveries = 56,
                distanceKm = 101.4,
                avgMinutesPerDelivery = 16
            ),
            com.example.domain.model.HourlyEfficiencyStat(
                slotKey = "SLOT_AFTERNOON",
                timeSlotLabel = "15:00 - 18:00",
                shortLabel = "15-18",
                completedDeliveries = 41,
                distanceKm = 96.0,
                avgMinutesPerDelivery = 21
            ),
            com.example.domain.model.HourlyEfficiencyStat(
                slotKey = "SLOT_EVENING",
                timeSlotLabel = "18:00 - 21:00",
                shortLabel = "18-21",
                completedDeliveries = 45,
                distanceKm = 110.2,
                avgMinutesPerDelivery = 19
            )
        )

        return WeeklyStatsSummary(
            weekPeriodLabel = "15 - 21 Eylül 2026 (Bu Hafta)",
            weekIndex = 0,
            totalAssigned = totalAssigned,
            totalCompleted = totalCompleted,
            totalCancelled = totalCancelled,
            overallCompletionRate = String.format(java.util.Locale.US, "%.1f", overallRate).toFloat(),
            targetCompletionRate = 92.0f,
            overallOnTimeRate = 98.1f,
            totalEarnings = totalEarnings,
            totalDistanceKm = String.format(java.util.Locale.US, "%.1f", totalDist).toDouble(),
            avgDeliveryMinutes = avgDuration,
            bonusEarned = 1450.0,
            dailyStats = days,
            hourlyStats = hourlySlots,
            completionRateDeltaVsLastWeek = 2.4f,
            deliveriesDeltaVsLastWeek = 19
        )
    }

    private fun getLastWeekStats(): WeeklyStatsSummary {
        val days = listOf(
            DailyDeliveryStat("MON", "Pzt", "Pazartesi", "8 Eyl", 22, 20, 2, 20, 90.9f, 95.0f, 1260.0, 48.0, 23),
            DailyDeliveryStat("TUE", "Sal", "Salı", "9 Eyl", 24, 22, 2, 22, 91.7f, 96.0f, 1380.0, 52.0, 22),
            DailyDeliveryStat("WED", "Çar", "Çarşamba", "10 Eyl", 25, 24, 1, 22, 96.0f, 97.0f, 1510.0, 56.0, 21),
            DailyDeliveryStat("THU", "Per", "Perşembe", "11 Eyl", 24, 22, 2, 22, 91.7f, 95.5f, 1390.0, 51.0, 23),
            DailyDeliveryStat("FRI", "Cum", "Cuma", "12 Eyl", 30, 28, 2, 25, 93.3f, 96.8f, 1790.0, 64.0, 24),
            DailyDeliveryStat("SAT", "Cmt", "Cumartesi", "13 Eyl", 32, 30, 2, 28, 93.8f, 97.0f, 1950.0, 71.0, 22),
            DailyDeliveryStat("SUN", "Paz", "Pazar", "14 Eyl", 15, 15, 0, 16, 100.0f, 98.0f, 970.0, 35.0, 20)
        )

        return WeeklyStatsSummary(
            weekPeriodLabel = "8 - 14 Eylül 2026 (Geçen Hafta)",
            weekIndex = 1,
            totalAssigned = 172,
            totalCompleted = 161,
            totalCancelled = 11,
            overallCompletionRate = 93.6f,
            targetCompletionRate = 92.0f,
            overallOnTimeRate = 96.5f,
            totalEarnings = 10250.0,
            totalDistanceKm = 377.0,
            avgDeliveryMinutes = 22,
            bonusEarned = 1000.0,
            dailyStats = days,
            completionRateDeltaVsLastWeek = 1.5f,
            deliveriesDeltaVsLastWeek = 9
        )
    }

    private fun getTwoWeeksAgoStats(): WeeklyStatsSummary {
        val days = listOf(
            DailyDeliveryStat("MON", "Pzt", "Pazartesi", "1 Eyl", 20, 18, 2, 20, 90.0f, 94.0f, 1150.0, 44.0, 24),
            DailyDeliveryStat("TUE", "Sal", "Salı", "2 Eyl", 22, 20, 2, 20, 90.9f, 95.0f, 1280.0, 49.0, 23),
            DailyDeliveryStat("WED", "Çar", "Çarşamba", "3 Eyl", 24, 22, 2, 22, 91.7f, 96.0f, 1400.0, 53.0, 22),
            DailyDeliveryStat("THU", "Per", "Perşembe", "4 Eyl", 23, 21, 2, 20, 91.3f, 95.0f, 1340.0, 50.0, 23),
            DailyDeliveryStat("FRI", "Cum", "Cuma", "5 Eyl", 28, 26, 2, 25, 92.9f, 96.0f, 1650.0, 60.0, 24),
            DailyDeliveryStat("SAT", "Cmt", "Cumartesi", "6 Eyl", 30, 28, 2, 26, 93.3f, 96.5f, 1820.0, 68.0, 23),
            DailyDeliveryStat("SUN", "Paz", "Pazar", "7 Eyl", 18, 17, 1, 16, 94.4f, 97.0f, 1080.0, 39.0, 21)
        )

        return WeeklyStatsSummary(
            weekPeriodLabel = "1 - 7 Eylül 2026 (2 Hafta Önce)",
            weekIndex = 2,
            totalAssigned = 165,
            totalCompleted = 152,
            totalCancelled = 13,
            overallCompletionRate = 92.1f,
            targetCompletionRate = 92.0f,
            overallOnTimeRate = 95.6f,
            totalEarnings = 9720.0,
            totalDistanceKm = 363.0,
            avgDeliveryMinutes = 23,
            bonusEarned = 800.0,
            dailyStats = days,
            completionRateDeltaVsLastWeek = 0.8f,
            deliveriesDeltaVsLastWeek = 4
        )
    }
}
