package com.example

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun carModel_upgradeCalculation_isAccurate() {
        val car = CarModel(
            id = "test_car",
            name = "Test Beast",
            manufacturer = "APEX",
            carClass = "Class S",
            baseTopSpeed = 350f,
            baseAcceleration = 2.8f,
            baseHandling = 1.0f,
            baseNitro = 20f,
            priceCredits = 5000,
            bodyColor = Color.Red
        )

        assertEquals(350f, car.currentTopSpeed, 0.01f)
        car.engineUpgradeLevel = 3
        // base + 2 * 12 = 374
        assertEquals(374f, car.currentTopSpeed, 0.01f)
    }

    @Test
    fun trackData_lapDistance_isPositive() {
        val track = TrackData(
            id = "tokyo",
            name = "Tokyo Neon",
            location = "Tokyo",
            totalLengthMeters = 3800f,
            laps = 2,
            difficulty = "Pro",
            rewardCredits = 3000,
            skyTheme = "TOKYO_NEON"
        )
        assertTrue(track.totalLengthMeters > 0)
        assertEquals(2, track.laps)
    }
}
