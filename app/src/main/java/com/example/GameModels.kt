package com.example

import androidx.compose.ui.graphics.Color

enum class GameScreen {
    MAIN_MENU,
    GARAGE,
    TRACK_SELECT,
    RACE_PLAYING,
    RACE_PAUSED,
    RACE_SUMMARY,
    AI_STRATEGIST,
    REPLAY_VIEW
}

enum class CameraViewMode {
    THIRD_PERSON,
    FIRST_PERSON_COCKPIT
}

enum class CinematicCameraAngle(val displayName: String, val description: String) {
    CHASE("Chase Cam", "Dynamic low exterior chase view with nitro zoom"),
    DRONE_HOVER("Drone Orbit", "Elevated orbital track aerial view"),
    WHEEL_SIDE("Wheel Cam", "Ultra-low side wheel & asphalt skid level"),
    BUMPER_FRONT("Bumper View", "Aggressive road-hugging front bumper camera"),
    BROADCAST_TV("Broadcast TV", "Roadside tracking camera panning pass-bys")
}

enum class WeatherCondition(
    val displayName: String,
    val description: String,
    val icon: String,
    val gripMultiplier: Float,       // Steering agility & friction scaling
    val topSpeedMultiplier: Float,   // Max speed modulation
    val visibilityMeters: Float,    // Fog / rain horizon visibility range
    val fogAlpha: Float,            // Volumetric fog overlay density
    val rainIntensity: Float,       // Animated rain drop streaks density
    val roadReflectivity: Float,    // Wet glossy asphalt reflections
    val badgeColor: Color
) {
    CLEAR("Clear Asphalt", "Optimal grip & maximum track visibility", "☀️", 1.0f, 1.0f, 1200f, 0.0f, 0.0f, 0.08f, Color(0xFF00F0FF)),
    LIGHT_RAIN("Light Rain", "Wet road, -15% grip & active water droplets", "🌧️", 0.85f, 0.96f, 850f, 0.18f, 0.45f, 0.60f, Color(0xFF29B6F6)),
    HEAVY_RAIN("Heavy Downpour", "Puddles, -32% grip, intense hydroplaning drift", "⛈️", 0.68f, 0.90f, 600f, 0.35f, 0.88f, 0.92f, Color(0xFF00E5FF)),
    THICK_FOG("Thick Fog", "Dense mist overlay, low horizon visibility & -12% grip", "🌫️", 0.88f, 0.94f, 320f, 0.70f, 0.12f, 0.25f, Color(0xFFCFD8DC))
}

data class ReplayFrame(
    val timestampMs: Long,
    val playerLane: Float,
    val playerSpeedKmh: Float,
    val trackDistanceMeters: Float,
    val nitroActive: Boolean,
    val nitroStage: NitroStage,
    val isDrifting: Boolean,
    val driftAngle: Float,
    val jumpAirborneHeight: Float,
    val spinRotationAngle: Float,
    val rivals: List<RivalCar>,
    val pickups: List<TrackPickup>,
    val weatherCondition: WeatherCondition = WeatherCondition.CLEAR
)

enum class GameMode(val title: String, val description: String) {
    CLASSIC_RACE("Classic Championship", "6 Rivals. 2 Laps. Full Asphalt Nitro rules & takedowns."),
    ELIMINATION("Elimination Sprint", "Last place car gets knocked out every 20 seconds!"),
    TIME_TRIAL("Time Attack", "Race against the clock to unlock legendary performance badges.")
}

enum class NitroStage {
    NONE,
    STANDARD,   // Yellow flames, +40 km/h
    PERFECT,    // Cyan flames, +80 km/h, longer burn
    SHOCKWAVE   // Magenta/Purple warp, +120 km/h, invincible knockdowns!
}

data class CarModel(
    val id: String,
    val name: String,
    val manufacturer: String,
    val carClass: String, // "Class D", "Class C", "Class B", "Class A", "Class S"
    val baseTopSpeed: Float,     // e.g. 320 km/h
    val baseAcceleration: Float, // 0-100 km/h in seconds (lower is faster)
    val baseHandling: Float,     // steer agility (0.5f to 1.5f)
    val baseNitro: Float,        // nitro capacity & burn rate
    val priceCredits: Int,
    var isUnlocked: Boolean = false,
    var bodyColor: Color,
    var underglowColor: Color = Color(0xFF00F0FF),
    var engineUpgradeLevel: Int = 1,
    var nitroUpgradeLevel: Int = 1,
    var handlingUpgradeLevel: Int = 1
) {
    val currentTopSpeed: Float
        get() = baseTopSpeed + (engineUpgradeLevel - 1) * 12f

    val currentAcceleration: Float
        get() = (baseAcceleration - (engineUpgradeLevel - 1) * 0.15f).coerceAtLeast(1.8f)

    val currentHandling: Float
        get() = baseHandling + (handlingUpgradeLevel - 1) * 0.1f

    val currentNitroBonus: Float
        get() = baseNitro + (nitroUpgradeLevel - 1) * 15f
}

data class TrackData(
    val id: String,
    val name: String,
    val location: String,
    val totalLengthMeters: Float = 4200f,
    val laps: Int = 2,
    val difficulty: String,
    val rewardCredits: Int,
    val skyTheme: String, // "TOKYO_NEON", "MOJAVE_SUNSET", "ALPS_CYBER"
    val roadCurveIntensity: Float = 1.0f,
    var weatherCondition: WeatherCondition = WeatherCondition.CLEAR
)

data class RivalCar(
    val id: Int,
    val name: String,
    var distanceMeters: Float,
    var laneOffset: Float, // -0.8f (left) to 0.8f (right)
    var currentSpeed: Float, // km/h
    val maxSpeed: Float,
    val color: Color,
    var isKnockedDown: Boolean = false,
    var knockdownTimer: Float = 0f,
    var spinAngle: Float = 0f
)

enum class PickupType {
    NITRO_CANISTER,
    CREDIT_TOKEN,
    SPEED_PAD
}

data class TrackPickup(
    val id: Int,
    val distanceMeters: Float,
    val laneOffset: Float,
    val type: PickupType,
    var isCollected: Boolean = false
)

data class RaceTelemetry(
    val trackName: String,
    val carName: String,
    val position: Int,
    val finishTimeSeconds: Float,
    val topSpeedAchieved: Float,
    val totalDriftSeconds: Float,
    val knockdownsTriggered: Int,
    val shockwavesUsed: Int,
    val nitroEfficiencyRatio: Float,
    val averageSpeed: Float
)

data class RaceNotification(
    val text: String,
    val subText: String? = null,
    val color: Color = Color(0xFF00F0FF),
    val timestamp: Long = System.currentTimeMillis()
)
