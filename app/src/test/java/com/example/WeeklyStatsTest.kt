package com.example

import com.example.data.repository.WeeklyStatsProvider
import org.junit.Assert.*
import org.junit.Test

class WeeklyStatsTest {

    @Test
    fun testThisWeekStatsCalculation() {
        val stats = WeeklyStatsProvider.getWeeklyStats(0, todayCompletedCount = 14)
        assertEquals(7, stats.dailyStats.size)
        assertTrue(stats.totalCompleted > 0)
        assertTrue(stats.overallCompletionRate > 90f)
        assertEquals(92.0f, stats.targetCompletionRate, 0.01f)
        assertTrue(stats.overallOnTimeRate > 95f)

        // Sunday is marked as today
        val sunday = stats.dailyStats.last()
        assertTrue(sunday.isToday)
        assertEquals("Paz", sunday.dayLabel)
    }

    @Test
    fun testLastWeekStatsPresets() {
        val lastWeek = WeeklyStatsProvider.getWeeklyStats(1)
        assertEquals(7, lastWeek.dailyStats.size)
        assertEquals(161, lastWeek.totalCompleted)
        assertEquals(172, lastWeek.totalAssigned)
        assertEquals(93.6f, lastWeek.overallCompletionRate, 0.1f)
    }

    @Test
    fun testTwoWeeksAgoStatsPresets() {
        val twoWeeksAgo = WeeklyStatsProvider.getWeeklyStats(2)
        assertEquals(7, twoWeeksAgo.dailyStats.size)
        assertEquals(152, twoWeeksAgo.totalCompleted)
        assertEquals(165, twoWeeksAgo.totalAssigned)
        assertEquals(92.1f, twoWeeksAgo.overallCompletionRate, 0.1f)
    }

    @Test
    fun testLiveDeliveriesIncrementReflectedInStats() {
        val baseStats = WeeklyStatsProvider.getWeeklyStats(0, todayCompletedCount = 14)
        val updatedStats = WeeklyStatsProvider.getWeeklyStats(0, todayCompletedCount = 16)

        assertTrue(updatedStats.totalCompleted >= baseStats.totalCompleted)
        assertTrue(updatedStats.totalEarnings >= baseStats.totalEarnings)
    }

    @Test
    fun testCourierEfficiencyMetrics() {
        val stats = WeeklyStatsProvider.getWeeklyStats(0, todayCompletedCount = 14)
        assertTrue("Avg km per delivery should be reasonable", stats.avgKmPerDelivery in 1.5f..3.5f)
        assertTrue("Delivery density should be positive", stats.overallDeliveriesPer10Km > 3.0f)
        assertTrue("Efficiency score should be within 70..100", stats.efficiencyScore in 70..100)

        // Check hourly time windows
        assertTrue("Hourly efficiency slots present", stats.hourlyStats.isNotEmpty())
        val lunchSlot = stats.hourlyStats.find { it.slotKey == "SLOT_LUNCH" }
        assertNotNull(lunchSlot)
        assertTrue(lunchSlot!!.completedDeliveries > 40)
        assertTrue(lunchSlot.kmPerDelivery in 1.2f..2.5f)

        // Check daily efficiency
        for (day in stats.dailyStats) {
            assertTrue(day.kmPerDelivery > 0f)
            assertTrue(day.deliveriesPer10Km > 0f)
            assertNotNull(day.efficiencyRating)
        }
    }

    @Test
    fun testRoomCourierEfficiencyEntityCalculations() {
        val entity = com.example.data.database.entities.CourierEfficiencyEntity(
            id = "eff_fri",
            dayKey = "FRI",
            dayLabel = "Cum",
            fullDate = "19 Eyl",
            completedDeliveries = 31,
            distanceKm = 69.8,
            targetDeliveries = 26,
            avgDeliveryMinutes = 23,
            earnings = 1980.0
        )

        assertEquals("eff_fri", entity.id)
        assertEquals(31, entity.completedDeliveries)
        assertEquals(69.8, entity.distanceKm, 0.001)

        val deliveriesPer10Km = (entity.completedDeliveries / entity.distanceKm) * 10.0
        assertTrue("Deliveries per 10km should be approximately 4.44", deliveriesPer10Km in 4.0..5.0)

        val kmPerDelivery = entity.distanceKm / entity.completedDeliveries
        assertTrue("Km per delivery should be approximately 2.25km", kmPerDelivery in 2.0..2.5)
    }
}
