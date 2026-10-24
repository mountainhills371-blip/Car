package com.example

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DarkCard
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroMagenta
import com.example.ui.theme.NitroPurple
import com.example.ui.theme.SpeedGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            MyApplicationTheme {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkCarbon)
                ) {
                    AsphaltGameApp(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun AsphaltGameApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val soundEffects = remember { SoundEffects() }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val strategist = remember { GeminiRaceStrategist() }

    // Audio & Haptics helper
    fun triggerHaptic(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {}
    }

    var isMuted by remember { mutableStateOf(false) }

    // -------------------------------------------------------------
    // GAME CARS & PROGRESSION STATE
    // -------------------------------------------------------------
    val carsList = remember {
        mutableStateListOf(
            CarModel(
                id = "venom_neo",
                name = "Venom GT Neo",
                manufacturer = "APEX MOTORS",
                carClass = "Class D",
                baseTopSpeed = 320f,
                baseAcceleration = 3.2f,
                baseHandling = 1.0f,
                baseNitro = 20f,
                priceCredits = 0,
                isUnlocked = true,
                bodyColor = Color(0xFFFF1744)
            ),
            CarModel(
                id = "vulcan_hyperion",
                name = "Vulcan Hyperion",
                manufacturer = "TITAN WORKS",
                carClass = "Class C",
                baseTopSpeed = 345f,
                baseAcceleration = 2.8f,
                baseHandling = 1.15f,
                baseNitro = 25f,
                priceCredits = 2500,
                isUnlocked = false,
                bodyColor = Color(0xFF00F0FF)
            ),
            CarModel(
                id = "centenario_apex",
                name = "Centenario Apex",
                manufacturer = "SYNDICATE CORSE",
                carClass = "Class B",
                baseTopSpeed = 372f,
                baseAcceleration = 2.5f,
                baseHandling = 1.25f,
                baseNitro = 30f,
                priceCredits = 6000,
                isUnlocked = false,
                bodyColor = Color(0xFFFFD700)
            ),
            CarModel(
                id = "chiron_storm",
                name = "Chiron Storm",
                manufacturer = "AERO CYBERNETICS",
                carClass = "Class A",
                baseTopSpeed = 412f,
                baseAcceleration = 2.1f,
                baseHandling = 1.35f,
                baseNitro = 35f,
                priceCredits = 14000,
                isUnlocked = false,
                bodyColor = Color(0xFF9D00FF)
            ),
            CarModel(
                id = "cyber_spectre",
                name = "Cyber Spectre X",
                manufacturer = "QUANTUM DYNAMICS",
                carClass = "Class S",
                baseTopSpeed = 445f,
                baseAcceleration = 1.8f,
                baseHandling = 1.5f,
                baseNitro = 45f,
                priceCredits = 25000,
                isUnlocked = false,
                bodyColor = Color(0xFF00FF66)
            )
        )
    }

    var selectedCarIndex by remember { mutableIntStateOf(0) }
    var playerCredits by remember { mutableIntStateOf(3500) }

    // -------------------------------------------------------------
    // TRACKS & GAME MODES
    // -------------------------------------------------------------
    val tracksList = remember {
        listOf(
            TrackData(
                id = "tokyo_night",
                name = "Tokyo Neon Nightway",
                location = "Tokyo Express Highway, Japan",
                totalLengthMeters = 3800f,
                laps = 2,
                difficulty = "Rookie",
                rewardCredits = 2200,
                skyTheme = "TOKYO_NEON",
                roadCurveIntensity = 1.0f,
                weatherCondition = WeatherCondition.LIGHT_RAIN
            ),
            TrackData(
                id = "mojave_desert",
                name = "Mojave Nitro Desert",
                location = "Route 66 Interstate, USA",
                totalLengthMeters = 4800f,
                laps = 2,
                difficulty = "Pro",
                rewardCredits = 3600,
                skyTheme = "MOJAVE_SUNSET",
                roadCurveIntensity = 0.85f,
                weatherCondition = WeatherCondition.CLEAR
            ),
            TrackData(
                id = "alpine_ridge",
                name = "Cyber Alpine Pass",
                location = "St. Gotthard Pass, Switzerland",
                totalLengthMeters = 5400f,
                laps = 2,
                difficulty = "Master",
                rewardCredits = 5200,
                skyTheme = "ALPS_CYBER",
                roadCurveIntensity = 1.4f,
                weatherCondition = WeatherCondition.THICK_FOG
            )
        )
    }

    var selectedTrack by remember { mutableStateOf(tracksList[0]) }
    var currentGameMode by remember { mutableStateOf(GameMode.CLASSIC_RACE) }
    var currentScreen by remember { mutableStateOf(GameScreen.MAIN_MENU) }

    // -------------------------------------------------------------
    // IN-RACE SIMULATION STATE
    // -------------------------------------------------------------
    var playerSpeedKmh by remember { mutableFloatStateOf(0f) }
    var playerLane by remember { mutableFloatStateOf(0f) } // -1.0 to 1.0
    var trackDistanceMeters by remember { mutableFloatStateOf(0f) }
    var currentLap by remember { mutableIntStateOf(1) }
    var nitroLevel by remember { mutableFloatStateOf(60f) }
    var nitroActive by remember { mutableStateOf(false) }
    var nitroStage by remember { mutableStateOf(NitroStage.NONE) }
    var isDrifting by remember { mutableStateOf(false) }
    var isManualDriftPress by remember { mutableStateOf(false) }
    var currentDriftScore by remember { mutableIntStateOf(0) }
    var currentDriftMultiplier by remember { mutableFloatStateOf(1.0f) }
    var continuousDriftDuration by remember { mutableFloatStateOf(0f) }
    var driftAngle by remember { mutableFloatStateOf(0f) }
    var jumpHeight by remember { mutableFloatStateOf(0f) }
    var spinAngle by remember { mutableFloatStateOf(0f) }
    var racePosition by remember { mutableIntStateOf(6) }
    var raceTimeSeconds by remember { mutableFloatStateOf(0f) }
    var countdownValue by remember { mutableIntStateOf(3) }
    var notification by remember { mutableStateOf<RaceNotification?>(null) }
    var screenShakeOffset by remember { mutableStateOf(Offset.Zero) }

    // Telemetry & stats tracking for race summary and Gemini
    var lastTelemetry by remember { mutableStateOf<RaceTelemetry?>(null) }
    var peakSpeedAchieved by remember { mutableFloatStateOf(0f) }
    var totalDriftSeconds by remember { mutableFloatStateOf(0f) }
    var knockdownsCount by remember { mutableIntStateOf(0) }
    var shockwavesUsedCount by remember { mutableIntStateOf(0) }
    var earnedCreditsThisRace by remember { mutableIntStateOf(0) }

    // Gyroscope & Motion Sensor Controller
    val gyroController = rememberGyroMotionController()
    var isGyroEnabled by remember { mutableStateOf(true) }
    var gyroSensitivity by remember { mutableFloatStateOf(1.0f) }
    var cameraViewMode by remember { mutableStateOf(CameraViewMode.THIRD_PERSON) }

    // Input States
    var isSteeringLeft by remember { mutableStateOf(false) }
    var isSteeringRight by remember { mutableStateOf(false) }
    var isBraking by remember { mutableStateOf(false) }

    // AI Strategist State
    var aiAdviceText by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }

    // 3D Rivals
    val rivals = remember { mutableStateListOf<RivalCar>() }
    // 3D Pickups
    val pickups = remember { mutableStateListOf<TrackPickup>() }

    // 30-Second Rolling Replay Buffer & Playback State
    val replayBuffer = remember { mutableStateListOf<ReplayFrame>() }
    var replayFrameIndex by remember { mutableIntStateOf(0) }
    var isReplayPlaying by remember { mutableStateOf(true) }
    var replayPlaybackSpeed by remember { mutableFloatStateOf(1.0f) }
    var replayCameraAngle by remember { mutableStateOf(CinematicCameraAngle.CHASE) }
    var replayReturnScreen by remember { mutableStateOf(GameScreen.RACE_PAUSED) }

    fun startReplay(fromScreen: GameScreen) {
        if (replayBuffer.isEmpty()) return
        replayReturnScreen = fromScreen
        replayFrameIndex = 0
        isReplayPlaying = true
        currentScreen = GameScreen.REPLAY_VIEW
    }

    // Setup fresh race session
    fun setupRace() {
        replayBuffer.clear()
        playerSpeedKmh = 0f
        playerLane = 0f
        trackDistanceMeters = 0f
        currentLap = 1
        nitroLevel = 75f
        nitroActive = false
        nitroStage = NitroStage.NONE
        isDrifting = false
        isManualDriftPress = false
        currentDriftScore = 0
        currentDriftMultiplier = 1.0f
        continuousDriftDuration = 0f
        driftAngle = 0f
        jumpHeight = 0f
        spinAngle = 0f
        racePosition = 6
        raceTimeSeconds = 0f
        countdownValue = 3
        notification = null
        peakSpeedAchieved = 0f
        totalDriftSeconds = 0f
        knockdownsCount = 0
        shockwavesUsedCount = 0

        // Populate Rivals
        rivals.clear()
        val rivalColors = listOf(
            Color(0xFF2979FF), Color(0xFFFF5252), Color(0xFFFFAB00),
            Color(0xFF00E676), Color(0xFFE040FB)
        )
        val rivalNames = listOf("Phantom", "Blaze", "Vortex", "Apex Titan", "Shadow")
        for (i in 0 until 5) {
            val startDist = 60f + (i * 35f)
            val lane = if (i % 2 == 0) -0.5f + (i * 0.15f) else 0.5f - (i * 0.15f)
            val baseMaxSpeed = selectedTrack.roadCurveIntensity * 280f + (i * 14f)
            rivals.add(
                RivalCar(
                    id = i,
                    name = rivalNames[i],
                    distanceMeters = startDist,
                    laneOffset = lane,
                    currentSpeed = 0f,
                    maxSpeed = baseMaxSpeed,
                    color = rivalColors[i]
                )
            )
        }

        // Populate Track Pickups & Jump Ramps
        pickups.clear()
        var pDist = 180f
        var pId = 0
        while (pDist < selectedTrack.totalLengthMeters * selectedTrack.laps) {
            val lane = ((pId % 3) - 1) * 0.55f
            val type = when (pId % 4) {
                0 -> PickupType.NITRO_CANISTER
                1 -> PickupType.CREDIT_TOKEN
                2 -> PickupType.SPEED_PAD
                else -> PickupType.NITRO_CANISTER
            }
            pickups.add(TrackPickup(id = pId, distanceMeters = pDist, laneOffset = lane, type = type))
            pDist += 220f
            pId++
        }

        soundEffects.startEngine()
        currentScreen = GameScreen.RACE_PLAYING
    }

    // Nitro Trigger Action
    fun triggerNitro() {
        if (nitroLevel < 15f) return

        if (!nitroActive) {
            nitroActive = true
            nitroStage = when {
                nitroLevel >= 95f -> {
                    shockwavesUsedCount++
                    notification = RaceNotification("⚡ SHOCKWAVE NITRO!", "MAXIMUM THRUST & KNOCKDOWN IMMUNITY", NitroMagenta)
                    triggerHaptic(120)
                    NitroStage.SHOCKWAVE
                }
                nitroLevel >= 55f -> {
                    notification = RaceNotification("🔥 PERFECT NITRO!", "+80 KM/H VELOCITY BOOST", NeonCyan)
                    triggerHaptic(70)
                    NitroStage.PERFECT
                }
                else -> {
                    notification = RaceNotification("NITRO BOOST", "+40 KM/H", ElectricGold)
                    triggerHaptic(40)
                    NitroStage.STANDARD
                }
            }
            soundEffects.playNitroIgnite()
        } else {
            // Re-tapping during active nitro upgrades to Shockwave if gauge is full
            if (nitroLevel >= 85f && nitroStage != NitroStage.SHOCKWAVE) {
                nitroStage = NitroStage.SHOCKWAVE
                shockwavesUsedCount++
                notification = RaceNotification("⚡ SHOCKWAVE ENGAGED!", "RAM RIVALS FOR TAKEDOWNS", NitroPurple)
                soundEffects.playNitroIgnite()
                triggerHaptic(150)
            }
        }
    }

    // -------------------------------------------------------------
    // MAIN 60FPS RACING GAME LOOP
    // -------------------------------------------------------------
    LaunchedEffect(currentScreen) {
        if (currentScreen != GameScreen.RACE_PLAYING) {
            soundEffects.stopEngine()
            return@LaunchedEffect
        }

        // Countdown sequence (3, 2, 1, GO!)
        while (countdownValue > 0) {
            soundEffects.playCountdownBeep(isFinal = false)
            delay(1000)
            countdownValue--
        }
        soundEffects.playCountdownBeep(isFinal = true)
        delay(600)
        countdownValue = -1 // Race starts!

        val car = carsList[selectedCarIndex]
        val totalTrackMeters = selectedTrack.totalLengthMeters * selectedTrack.laps

        while (isActive && currentScreen == GameScreen.RACE_PLAYING) {
            val dt = 0.033f // ~30-60 physics step
            raceTimeSeconds += dt

            // 1. SPEED CALCULATION WITH WEATHER DRAG & NITRO CLEARANCE
            val activeWeather = selectedTrack.weatherCondition
            val weatherTopSpeed = car.currentTopSpeed * (if (nitroActive) 1.0f else activeWeather.topSpeedMultiplier)
            val targetMaxSpeed = weatherTopSpeed + when (nitroStage) {
                NitroStage.SHOCKWAVE -> 120f
                NitroStage.PERFECT -> 75f
                NitroStage.STANDARD -> 40f
                NitroStage.NONE -> 0f
            }

            if (isBraking) {
                playerSpeedKmh = (playerSpeedKmh - 160f * dt).coerceAtLeast(0f)
            } else {
                val accelRate = (targetMaxSpeed / car.currentAcceleration) * 0.45f
                playerSpeedKmh = (playerSpeedKmh + accelRate * dt).coerceAtMost(targetMaxSpeed)
            }

            if (playerSpeedKmh > peakSpeedAchieved) {
                peakSpeedAchieved = playerSpeedKmh
            }

            // 2. NITRO DEPLETION & DRIFT CHARGE
            if (nitroActive) {
                val burnRate = when (nitroStage) {
                    NitroStage.SHOCKWAVE -> 22f
                    NitroStage.PERFECT -> 16f
                    NitroStage.STANDARD -> 12f
                    NitroStage.NONE -> 10f
                }
                nitroLevel = (nitroLevel - burnRate * dt).coerceAtLeast(0f)
                if (nitroLevel <= 0f) {
                    nitroActive = false
                    nitroStage = NitroStage.NONE
                }
            }

            // 3. STEERING & DYNAMIC WEATHER DRIFT PHYSICS (Lower grip = wider slide)
            val steerSpeed = car.currentHandling * 1.75f * activeWeather.gripMultiplier * dt
            val gyroTilt = if (isGyroEnabled) gyroController.tiltInput else 0f
            val buttonSteer = when {
                isSteeringLeft && !isSteeringRight -> -1.0f
                isSteeringRight && !isSteeringLeft -> 1.0f
                else -> 0f
            }

            // Button input overrides if pressed; otherwise smooth gyro tilt is used
            val effectiveSteer = if (buttonSteer != 0f) buttonSteer else (gyroTilt * gyroSensitivity)

            // Dynamic sharp steering auto-drift trigger (wet roads slide easier)
            val autoDriftThreshold = if (activeWeather.gripMultiplier < 0.9f) 0.42f else 0.55f
            val isAutoDrifting = (playerSpeedKmh > 115f && kotlin.math.abs(effectiveSteer) > autoDriftThreshold)
            val activeDrifting = isManualDriftPress || isAutoDrifting
            isDrifting = activeDrifting

            if (effectiveSteer != 0f) {
                playerLane = (playerLane + effectiveSteer * steerSpeed).coerceIn(-1.0f, 1.0f)
                val targetDriftAngle = if (isDrifting) {
                    effectiveSteer * (36f + (1.0f - activeWeather.gripMultiplier) * 20f)
                } else {
                    effectiveSteer * 18f
                }
                driftAngle += (targetDriftAngle - driftAngle) * 0.38f
            } else {
                driftAngle *= 0.80f // return smoothly to center
            }

            // DRIFT SCORE & MULTIPLIER ACCUMULATION (Wet asphalt drift score bonus)
            if (isDrifting && playerSpeedKmh > 60f) {
                totalDriftSeconds += dt
                continuousDriftDuration += dt

                val wetMultBonus = if (activeWeather.gripMultiplier < 0.9f) 1.25f else 1.0f
                currentDriftMultiplier = (when {
                    continuousDriftDuration >= 3.0f -> 4.0f
                    continuousDriftDuration >= 2.0f -> 3.0f
                    continuousDriftDuration >= 1.0f -> 2.0f
                    continuousDriftDuration >= 0.4f -> 1.5f
                    else -> 1.0f
                }) * wetMultBonus

                val driftPointsEarned = (playerSpeedKmh * dt * 2.8f * currentDriftMultiplier).toInt()
                currentDriftScore += driftPointsEarned

                // Charge nitro faster when drifting on wet asphalt!
                val nitroChargeRate = 22f * (1.2f - activeWeather.gripMultiplier * 0.2f)
                nitroLevel = (nitroLevel + nitroChargeRate * dt).coerceAtMost(100f)
            } else {
                if (currentDriftScore > 0) {
                    val bankedPoints = currentDriftScore
                    val lastMult = currentDriftMultiplier
                    earnedCreditsThisRace += (bankedPoints / 8).coerceAtLeast(50)
                    notification = RaceNotification(
                        "DRIFT +$bankedPoints PTS",
                        "MULTIPLIER x${String.format("%.1f", lastMult)}",
                        ElectricGold
                    )
                    triggerHaptic(75)
                    currentDriftScore = 0
                    currentDriftMultiplier = 1.0f
                    continuousDriftDuration = 0f
                }
            }

            // 4. ADVANCE DISTANCE ALONG TRACK
            val speedMps = (playerSpeedKmh * 1000f) / 3600f
            trackDistanceMeters += speedMps * dt

            // Lap update
            val currentLapCalc = (trackDistanceMeters / selectedTrack.totalLengthMeters).toInt() + 1
            if (currentLapCalc > currentLap && currentLap < selectedTrack.laps) {
                currentLap = currentLapCalc
                notification = RaceNotification("🏁 FINAL LAP!", "PUSH FOR THE PODIUM!", ElectricGold)
                triggerHaptic(80)
            }

            // Check Race Finished
            if (trackDistanceMeters >= totalTrackMeters) {
                soundEffects.stopEngine()
                earnedCreditsThisRace = selectedTrack.rewardCredits + (if (racePosition == 1) 1500 else 400) + (knockdownsCount * 300)
                playerCredits += earnedCreditsThisRace

                lastTelemetry = RaceTelemetry(
                    trackName = selectedTrack.name,
                    carName = car.name,
                    position = racePosition,
                    finishTimeSeconds = raceTimeSeconds,
                    topSpeedAchieved = peakSpeedAchieved,
                    totalDriftSeconds = totalDriftSeconds,
                    knockdownsTriggered = knockdownsCount,
                    shockwavesUsed = shockwavesUsedCount,
                    nitroEfficiencyRatio = (totalDriftSeconds / 20f).coerceIn(0.4f, 0.98f),
                    averageSpeed = (playerSpeedKmh * 0.85f).coerceAtLeast(180f)
                )

                currentScreen = GameScreen.RACE_SUMMARY
                break
            }

            // 5. UPDATE RIVALS AI
            var playerRank = 1
            for (rival in rivals) {
                if (rival.isKnockedDown) {
                    rival.knockdownTimer -= dt
                    rival.spinAngle += 420f * dt
                    rival.currentSpeed = (rival.currentSpeed - 80f * dt).coerceAtLeast(20f)
                    if (rival.knockdownTimer <= 0f) {
                        rival.isKnockedDown = false
                    }
                } else {
                    // AI acceleration
                    rival.currentSpeed = (rival.currentSpeed + 45f * dt).coerceAtMost(rival.maxSpeed)
                    // AI gentle weaving
                    rival.laneOffset += sin((raceTimeSeconds + rival.id).toDouble()).toFloat() * 0.12f * dt
                    rival.laneOffset = rival.laneOffset.coerceIn(-0.85f, 0.85f)
                }

                val rSpeedMps = (rival.currentSpeed * 1000f) / 3600f
                rival.distanceMeters += rSpeedMps * dt

                if (rival.distanceMeters > trackDistanceMeters) {
                    playerRank++
                }

                // Collision detection with player
                val distDelta = abs(rival.distanceMeters - trackDistanceMeters)
                val laneDelta = abs(rival.laneOffset - playerLane)
                if (distDelta < 8f && laneDelta < 0.28f && !rival.isKnockedDown) {
                    if (nitroStage == NitroStage.SHOCKWAVE || (nitroActive && playerSpeedKmh > rival.currentSpeed + 25f)) {
                        // TAKEDOWN / KNOCKDOWN!
                        rival.isKnockedDown = true
                        rival.knockdownTimer = 2.5f
                        knockdownsCount++
                        nitroLevel = 100f // Instant 100% Nitro refill reward!
                        notification = RaceNotification("💥 KNOCKDOWN!", "RIVAL WRECKED! +100% NITRO REFILL", SpeedGreen)
                        soundEffects.playKnockdownCrash()
                        screenShakeOffset = Offset((Math.random() * 24 - 12).toFloat(), (Math.random() * 16 - 8).toFloat())
                        triggerHaptic(200)
                    } else {
                        // Regular car collision bumper bump
                        playerSpeedKmh = (playerSpeedKmh - 25f).coerceAtLeast(40f)
                        screenShakeOffset = Offset(8f, 6f)
                        triggerHaptic(50)
                    }
                }
            }
            racePosition = playerRank

            // 6. UPDATE PICKUPS COLLISION
            for (p in pickups) {
                if (!p.isCollected) {
                    val pDelta = abs(p.distanceMeters - trackDistanceMeters)
                    val pLaneDelta = abs(p.laneOffset - playerLane)
                    if (pDelta < 9f && pLaneDelta < 0.32f) {
                        p.isCollected = true
                        when (p.type) {
                            PickupType.NITRO_CANISTER -> {
                                nitroLevel = (nitroLevel + 35f).coerceAtMost(100f)
                                notification = RaceNotification("+35% NITRO REFILL", null, NeonCyan)
                                soundEffects.playNitroIgnite()
                                triggerHaptic(40)
                            }
                            PickupType.CREDIT_TOKEN -> {
                                playerCredits += 150
                                notification = RaceNotification("+🪙 150 CREDITS", null, ElectricGold)
                                triggerHaptic(30)
                            }
                            PickupType.SPEED_PAD -> {
                                playerSpeedKmh = (playerSpeedKmh + 65f).coerceAtMost(targetMaxSpeed + 40f)
                                notification = RaceNotification("🚀 SPEED PAD BOOST!", null, SpeedGreen)
                                triggerHaptic(60)
                            }
                        }
                    }
                }
            }

            // 7. JUMP RAMPS / AIRTIME (Every 800 meters)
            val rampPhase = (trackDistanceMeters % 900f)
            if (rampPhase < 80f && playerSpeedKmh > 200f) {
                jumpHeight = sin((rampPhase / 80f) * Math.PI).toFloat() * 95f
                spinAngle += 360f * dt * 2.5f
                if (rampPhase > 70f && spinAngle > 200f) {
                    notification = RaceNotification("🌀 360° FLAT SPIN!", "+250 BONUS SCORE", ElectricGold)
                    nitroLevel = (nitroLevel + 25f).coerceAtMost(100f)
                }
            } else {
                jumpHeight = 0f
                spinAngle = 0f
            }

            // Decay screen shake
            screenShakeOffset = Offset(screenShakeOffset.x * 0.75f, screenShakeOffset.y * 0.75f)

            // Update Audio Synthesizer
            val speedRatio = (playerSpeedKmh / car.currentTopSpeed).coerceIn(0.1f, 1.2f)
            soundEffects.updateEngineState(
                speedRatio = speedRatio,
                nitro = nitroActive,
                drifting = isDrifting
            )

            // Record telemetry frame snapshot for 30-second rolling replay buffer
            if (countdownValue == -1) {
                val snapshotRivals = rivals.map { it.copy() }
                val snapshotPickups = pickups.map { it.copy() }
                replayBuffer.add(
                    ReplayFrame(
                        timestampMs = System.currentTimeMillis(),
                        playerLane = playerLane,
                        playerSpeedKmh = playerSpeedKmh,
                        trackDistanceMeters = trackDistanceMeters,
                        nitroActive = nitroActive,
                        nitroStage = nitroStage,
                        isDrifting = isDrifting,
                        driftAngle = driftAngle,
                        jumpAirborneHeight = jumpHeight,
                        spinRotationAngle = spinAngle,
                        rivals = snapshotRivals,
                        pickups = snapshotPickups,
                        weatherCondition = selectedTrack.weatherCondition
                    )
                )
                if (replayBuffer.size > 900) {
                    replayBuffer.removeAt(0)
                }
            }

            delay(33) // ~30 fps tick for stability
        }
    }

    // -------------------------------------------------------------
    // REPLAY PLAYBACK TICKER LOOP
    // -------------------------------------------------------------
    LaunchedEffect(currentScreen, isReplayPlaying, replayPlaybackSpeed) {
        if (currentScreen != GameScreen.REPLAY_VIEW) return@LaunchedEffect

        while (isActive && currentScreen == GameScreen.REPLAY_VIEW) {
            if (isReplayPlaying && replayBuffer.isNotEmpty()) {
                val stepDelay = (33f / replayPlaybackSpeed).toLong().coerceAtLeast(10L)
                delay(stepDelay)
                replayFrameIndex = (replayFrameIndex + 1) % replayBuffer.size
            } else {
                delay(100)
            }
        }
    }

    // Ensure audio cleanup
    DisposableEffect(Unit) {
        onDispose {
            soundEffects.stopEngine()
        }
    }

    // -------------------------------------------------------------
    // BACK HANDLER
    // -------------------------------------------------------------
    BackHandler(enabled = currentScreen != GameScreen.MAIN_MENU) {
        when (currentScreen) {
            GameScreen.RACE_PLAYING -> {
                currentScreen = GameScreen.RACE_PAUSED
            }
            GameScreen.RACE_PAUSED -> {
                currentScreen = GameScreen.RACE_PLAYING
            }
            GameScreen.REPLAY_VIEW -> {
                currentScreen = replayReturnScreen
            }
            GameScreen.RACE_SUMMARY, GameScreen.AI_STRATEGIST, GameScreen.TRACK_SELECT -> {
                currentScreen = GameScreen.GARAGE
            }
            GameScreen.GARAGE -> {
                currentScreen = GameScreen.MAIN_MENU
            }
            else -> {}
        }
    }

    // -------------------------------------------------------------
    // SCREEN RENDERING
    // -------------------------------------------------------------
    Box(modifier = modifier.fillMaxSize().background(DarkCarbon)) {
        when (currentScreen) {
            // ---------------------------------------------------------
            // MAIN MENU
            // ---------------------------------------------------------
            GameScreen.MAIN_MENU -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.bg_track_tokyo),
                        contentDescription = "Menu Backdrop",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().alpha(0.45f)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Title Branding
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ASPHALT",
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NITRO",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = NitroMagenta
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "3D",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = NeonCyan
                                )
                            }
                            Text(
                                text = "HIGH-SPEED ARCADE RACING SIMULATOR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.LightGray,
                                letterSpacing = 1.sp
                            )
                        }

                        // Car Showcase Icon
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(2.dp, NeonCyan, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_asphalt_car),
                                contentDescription = "Supercar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Navigation Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    setupRace()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SpeedGreen,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("menu_quick_race_button")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "RACE NOW", fontSize = 17.sp, fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = { currentScreen = GameScreen.GARAGE },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("menu_garage_button")
                            ) {
                                Icon(imageVector = Icons.Default.Build, contentDescription = "Garage", tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "HYPER GARAGE & TUNING", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = { currentScreen = GameScreen.TRACK_SELECT },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NitroMagenta),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("menu_tracks_button")
                            ) {
                                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Circuits", tint = NitroMagenta, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "CHAMPIONSHIP CIRCUITS", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    aiAdviceText = ""
                                    currentScreen = GameScreen.AI_STRATEGIST
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E172A)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricGold),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("menu_ai_button")
                            ) {
                                Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI", tint = ElectricGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "APEX-9 AI RACE STRATEGIST", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricGold)
                            }
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // GARAGE & TUNING SCREEN
            // ---------------------------------------------------------
            GameScreen.GARAGE -> {
                GarageScreen(
                    carsList = carsList,
                    selectedCarIndex = selectedCarIndex,
                    playerCredits = playerCredits,
                    onSelectCarIndex = { selectedCarIndex = it },
                    onBuyCar = { car ->
                        if (playerCredits >= car.priceCredits) {
                            playerCredits -= car.priceCredits
                            car.isUnlocked = true
                            triggerHaptic(60)
                        }
                    },
                    onUpgradeEngine = { car ->
                        val cost = 800 * car.engineUpgradeLevel
                        if (playerCredits >= cost && car.engineUpgradeLevel < 5) {
                            playerCredits -= cost
                            car.engineUpgradeLevel++
                            triggerHaptic(40)
                        }
                    },
                    onUpgradeNitro = { car ->
                        val cost = 800 * car.nitroUpgradeLevel
                        if (playerCredits >= cost && car.nitroUpgradeLevel < 5) {
                            playerCredits -= cost
                            car.nitroUpgradeLevel++
                            triggerHaptic(40)
                        }
                    },
                    onUpgradeHandling = { car ->
                        val cost = 800 * car.handlingUpgradeLevel
                        if (playerCredits >= cost && car.handlingUpgradeLevel < 5) {
                            playerCredits -= cost
                            car.handlingUpgradeLevel++
                            triggerHaptic(40)
                        }
                    },
                    onChangeColor = { car, color ->
                        car.bodyColor = color
                    },
                    onChangeUnderglow = { car, underglow ->
                        car.underglowColor = underglow
                    },
                    onStartRace = {
                        setupRace()
                    },
                    onOpenAiStrategist = {
                        aiAdviceText = ""
                        currentScreen = GameScreen.AI_STRATEGIST
                    },
                    onBackToMenu = {
                        currentScreen = GameScreen.MAIN_MENU
                    }
                )
            }

            // ---------------------------------------------------------
            // TRACK SELECTION SCREEN
            // ---------------------------------------------------------
            GameScreen.TRACK_SELECT -> {
                TrackSelectScreen(
                    tracks = tracksList,
                    selectedTrack = selectedTrack,
                    selectedCar = carsList[selectedCarIndex],
                    onSelectTrack = { selectedTrack = it },
                    onSelectGameMode = { currentGameMode = it },
                    currentGameMode = currentGameMode,
                    onStartRace = { setupRace() },
                    onBack = { currentScreen = GameScreen.MAIN_MENU }
                )
            }

            // ---------------------------------------------------------
            // 3D RACE PLAYING CANVAS & HUD
            // ---------------------------------------------------------
            GameScreen.RACE_PLAYING -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 3D Canvas
                    RacingCanvas3D(
                        playerCar = carsList[selectedCarIndex],
                        playerSpeedKmh = playerSpeedKmh,
                        playerLane = playerLane,
                        trackDistanceMeters = trackDistanceMeters,
                        trackData = selectedTrack,
                        nitroActive = nitroActive,
                        nitroStage = nitroStage,
                        isDrifting = isDrifting,
                        driftAngle = driftAngle,
                        jumpAirborneHeight = jumpHeight,
                        spinRotationAngle = spinAngle,
                        rivals = rivals,
                        pickups = pickups,
                        screenShakeOffset = screenShakeOffset,
                        cameraViewMode = cameraViewMode
                    )

                    // Overlay HUD
                    RacingHud(
                        playerSpeedKmh = playerSpeedKmh,
                        racePosition = racePosition,
                        totalRivals = 6,
                        currentLap = currentLap,
                        totalLaps = selectedTrack.laps,
                        raceTimeSeconds = raceTimeSeconds,
                        nitroLevel = nitroLevel,
                        nitroActive = nitroActive,
                        nitroStage = nitroStage,
                        countdownValue = countdownValue,
                        notification = notification,
                        isMuted = isMuted,
                        isGyroEnabled = isGyroEnabled,
                        gyroTiltInput = gyroController.tiltInput,
                        hasGyroscope = gyroController.hasGyroscope,
                        isCalibrated = gyroController.isCalibrated,
                        cameraViewMode = cameraViewMode,
                        trackData = selectedTrack,
                        trackDistanceMeters = trackDistanceMeters,
                        totalTrackMeters = selectedTrack.totalLengthMeters * selectedTrack.laps,
                        playerLane = playerLane,
                        rivals = rivals,
                        currentDriftScore = currentDriftScore,
                        currentDriftMultiplier = currentDriftMultiplier,
                        isDrifting = isDrifting,
                        onSteerLeftPress = { isSteeringLeft = it },
                        onSteerRightPress = { isSteeringRight = it },
                        onNitroClick = { triggerNitro() },
                        onDriftPress = { isManualDriftPress = it },
                        onBrakePress = { isBraking = it },
                        onPauseClick = { currentScreen = GameScreen.RACE_PAUSED },
                        onToggleMute = {
                            isMuted = !isMuted
                            soundEffects.isMuted = isMuted
                        },
                        onToggleGyro = {
                            isGyroEnabled = !isGyroEnabled
                            triggerHaptic(40)
                            notification = RaceNotification(
                                if (isGyroEnabled) "🌀 GYRO STEERING ON" else "🕹️ TOUCH STEERING ON",
                                if (isGyroEnabled) "TILT PHONE TO STEER" else "USE SCREEN BUTTONS",
                                if (isGyroEnabled) NeonCyan else Color.White
                            )
                        },
                        onCalibrateGyro = {
                            gyroController.calibrate()
                            triggerHaptic(60)
                            notification = RaceNotification(
                                "🎯 CENTER CALIBRATED",
                                "CURRENT POSITION SET AS NEUTRAL",
                                SpeedGreen
                            )
                        },
                        onToggleCameraViewMode = {
                            cameraViewMode = if (cameraViewMode == CameraViewMode.THIRD_PERSON) {
                                CameraViewMode.FIRST_PERSON_COCKPIT
                            } else {
                                CameraViewMode.THIRD_PERSON
                            }
                            triggerHaptic(40)
                            notification = RaceNotification(
                                if (cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT) "🏎️ FIRST-PERSON FPS VIEW" else "🏎️ THIRD-PERSON CHASE CAM",
                                if (cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT) "DASHBOARD COCKPIT PERSPECTIVE ENGAGED" else "EXTERIOR CHASE CAMERA",
                                if (cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT) NitroMagenta else NeonCyan
                            )
                        }
                    )
                }
            }

            // ---------------------------------------------------------
            // RACE PAUSED DIALOG
            // ---------------------------------------------------------
            GameScreen.RACE_PAUSED -> {
                Box(modifier = Modifier.fillMaxSize().background(DarkCarbon.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .border(1.dp, NeonCyan, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "RACE PAUSED",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            // Gyro Motion Steering Settings Panel
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkCarbon.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26334D))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "MOTION STEERING",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (isGyroEnabled) "Tilt phone physically to turn" else "Touch buttons active",
                                                fontSize = 10.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                isGyroEnabled = !isGyroEnabled
                                                triggerHaptic(40)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isGyroEnabled) NeonCyan else Color(0xFF334155),
                                                contentColor = if (isGyroEnabled) Color.Black else Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(34.dp).testTag("pause_toggle_gyro_btn")
                                        ) {
                                            Text(
                                                text = if (isGyroEnabled) "GYRO ON" else "OFF",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (isGyroEnabled) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "SENSITIVITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                listOf(
                                                    0.7f to "LOW",
                                                    1.0f to "MID",
                                                    1.4f to "HIGH"
                                                ).forEach { (sens, label) ->
                                                    val isSelected = abs(gyroSensitivity - sens) < 0.1f
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isSelected) SpeedGreen else Color(0xFF1E293B))
                                                            .clickable {
                                                                gyroSensitivity = sens
                                                                triggerHaptic(30)
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = if (isSelected) Color.Black else Color.LightGray
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                gyroController.calibrate()
                                                triggerHaptic(50)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF192A3D)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(36.dp).testTag("pause_calibrate_gyro_btn")
                                        ) {
                                            Text(
                                                text = if (gyroController.isCalibrated) "✓ CENTER CALIBRATED" else "🎯 CALIBRATE ZERO ANGLE",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (gyroController.isCalibrated) SpeedGreen else NeonCyan
                                            )
                                        }
                                    }
                                }
                            }

                            if (replayBuffer.isNotEmpty()) {
                                Button(
                                    onClick = { startReplay(GameScreen.RACE_PAUSED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1A47)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NitroMagenta),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("pause_watch_replay_button")
                                ) {
                                    Text(text = "🎬 WATCH INSTANT REPLAY (30S)", fontWeight = FontWeight.Black, color = NitroMagenta)
                                }
                            }

                            Button(
                                onClick = { currentScreen = GameScreen.RACE_PLAYING },
                                colors = ButtonDefaults.buttonColors(containerColor = SpeedGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("resume_race_button")
                            ) {
                                Text(text = "RESUME RACE", fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = { setupRace() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26334D)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("restart_race_button")
                            ) {
                                Text(text = "RESTART RACE", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    soundEffects.stopEngine()
                                    currentScreen = GameScreen.GARAGE
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCarbon),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("quit_to_garage_button")
                            ) {
                                Text(text = "QUIT TO GARAGE", fontWeight = FontWeight.Bold, color = Color.LightGray)
                            }
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // RACE SUMMARY / PODIUM SCREEN
            // ---------------------------------------------------------
            GameScreen.RACE_SUMMARY -> {
                lastTelemetry?.let { telemetry ->
                    RaceSummaryScreen(
                        telemetry = telemetry,
                        earnedCredits = earnedCreditsThisRace,
                        onRaceAgain = { setupRace() },
                        onOpenGarage = { currentScreen = GameScreen.GARAGE },
                        onConsultAiDebrief = {
                            currentScreen = GameScreen.AI_STRATEGIST
                            isAiLoading = true
                            coroutineScope.launch {
                                aiAdviceText = strategist.getTacticalStrategy(
                                    query = "Provide a deep telemetry debrief of my recent race finish at position P${telemetry.position}.",
                                    telemetry = telemetry,
                                    currentCar = carsList[selectedCarIndex],
                                    currentTrack = selectedTrack
                                )
                                isAiLoading = false
                            }
                        },
                        onWatchReplay = if (replayBuffer.isNotEmpty()) { { startReplay(GameScreen.RACE_SUMMARY) } } else null
                    )
                }
            }

            // ---------------------------------------------------------
            // 3D INSTANT REPLAY CINEMATIC VIEW
            // ---------------------------------------------------------
            GameScreen.REPLAY_VIEW -> {
                Box(modifier = Modifier.fillMaxSize().background(DarkCarbon)) {
                    val frameData = replayBuffer.getOrNull(replayFrameIndex)
                    if (frameData != null) {
                        RacingCanvas3D(
                            playerCar = carsList[selectedCarIndex],
                            playerSpeedKmh = frameData.playerSpeedKmh,
                            playerLane = frameData.playerLane,
                            trackDistanceMeters = frameData.trackDistanceMeters,
                            trackData = selectedTrack,
                            nitroActive = frameData.nitroActive,
                            nitroStage = frameData.nitroStage,
                            isDrifting = frameData.isDrifting,
                            driftAngle = frameData.driftAngle,
                            jumpAirborneHeight = frameData.jumpAirborneHeight,
                            spinRotationAngle = frameData.spinRotationAngle,
                            rivals = frameData.rivals,
                            pickups = frameData.pickups,
                            screenShakeOffset = Offset.Zero,
                            cameraViewMode = CameraViewMode.THIRD_PERSON,
                            cinematicCameraAngle = replayCameraAngle,
                            weatherCondition = frameData.weatherCondition
                        )

                        ReplayHud(
                            totalFrames = replayBuffer.size,
                            currentFrameIndex = replayFrameIndex,
                            isPlaying = isReplayPlaying,
                            playbackSpeed = replayPlaybackSpeed,
                            selectedCameraAngle = replayCameraAngle,
                            currentFrameData = frameData,
                            onSeekToFrame = { newIndex ->
                                replayFrameIndex = newIndex.coerceIn(0, (replayBuffer.size - 1).coerceAtLeast(0))
                            },
                            onTogglePlayPause = {
                                isReplayPlaying = !isReplayPlaying
                            },
                            onChangeSpeed = { newSpeed ->
                                replayPlaybackSpeed = newSpeed
                            },
                            onSelectCameraAngle = { newAngle ->
                                replayCameraAngle = newAngle
                                triggerHaptic(40)
                            },
                            onExitReplay = {
                                currentScreen = replayReturnScreen
                            }
                        )
                    }
                }
            }

            // ---------------------------------------------------------
            // AI RACE STRATEGIST SHEET (Powered by Gemini 4 Argon)
            // ---------------------------------------------------------
            GameScreen.AI_STRATEGIST -> {
                StrategistSheet(
                    currentCar = carsList[selectedCarIndex],
                    currentTrack = selectedTrack,
                    lastTelemetry = lastTelemetry,
                    adviceText = aiAdviceText,
                    isLoading = isAiLoading,
                    onAskStrategist = { query ->
                        isAiLoading = true
                        coroutineScope.launch {
                            aiAdviceText = strategist.getTacticalStrategy(
                                query = query,
                                telemetry = lastTelemetry,
                                currentCar = carsList[selectedCarIndex],
                                currentTrack = selectedTrack
                            )
                            isAiLoading = false
                        }
                    },
                    onClose = {
                        currentScreen = if (lastTelemetry != null) GameScreen.RACE_SUMMARY else GameScreen.GARAGE
                    }
                )
            }
        }
    }
}
