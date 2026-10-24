package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroMagenta
import com.example.ui.theme.NitroPurple
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.ScreenRotation
import com.example.ui.theme.SpeedGreen
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RacingHud(
    modifier: Modifier = Modifier,
    playerSpeedKmh: Float,
    racePosition: Int,
    totalRivals: Int,
    currentLap: Int,
    totalLaps: Int,
    raceTimeSeconds: Float,
    nitroLevel: Float, // 0.0f to 100.0f
    nitroActive: Boolean,
    nitroStage: NitroStage,
    countdownValue: Int, // 3, 2, 1, 0 (0 = GO, -1 = In Race)
    notification: RaceNotification?,
    isMuted: Boolean,
    isGyroEnabled: Boolean = true,
    gyroTiltInput: Float = 0f,
    hasGyroscope: Boolean = true,
    isCalibrated: Boolean = false,
    cameraViewMode: CameraViewMode = CameraViewMode.THIRD_PERSON,
    trackData: TrackData = TrackData("desert_dash", "Desert Dash", "Nevada", 4200f, 2, "MEDIUM", 1200, "MOJAVE_SUNSET", 1.0f),
    weatherCondition: WeatherCondition = trackData.weatherCondition,
    trackDistanceMeters: Float = 0f,
    totalTrackMeters: Float = 1000f,
    playerLane: Float = 0f,
    rivals: List<RivalCar> = emptyList(),
    currentDriftScore: Int = 0,
    currentDriftMultiplier: Float = 1.0f,
    isDrifting: Boolean = false,
    onSteerLeftPress: (Boolean) -> Unit,
    onSteerRightPress: (Boolean) -> Unit,
    onNitroClick: () -> Unit,
    onDriftPress: (Boolean) -> Unit,
    onBrakePress: (Boolean) -> Unit,
    onPauseClick: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleGyro: () -> Unit = {},
    onCalibrateGyro: () -> Unit = {},
    onToggleCameraViewMode: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val shockwavePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shockwavePulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // -------------------------------------------------------------
        // NITRO SPEED BOOST FULLSCREEN OVERLAY
        // -------------------------------------------------------------
        if (nitroActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 8.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                NeonCyan.copy(alpha = 0.4f),
                                NitroMagenta.copy(alpha = 0.75f)
                            )
                        ),
                        shape = androidx.compose.ui.graphics.RectangleShape
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val numWarpLines = 20
                    for (i in 0 until numWarpLines) {
                        val startX = (i * 179f) % size.width
                        val startY = (i * 113f) % size.height
                        val lineLength = 50f + (i * 9) % 70
                        drawLine(
                            color = if (i % 2 == 0) NeonCyan.copy(alpha = 0.5f) else NitroMagenta.copy(alpha = 0.5f),
                            start = Offset(startX, startY),
                            end = Offset(startX, startY + lineLength),
                            strokeWidth = 3f
                        )
                    }
                }
            }
        }
        // -------------------------------------------------------------
        // TOP HUD: POSITION, LAP, TIME, PAUSE, NITRO BAR
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Position Badge (e.g., 1ST / 6)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DarkCarbon.copy(alpha = 0.85f), Color(0xFF1B2338))
                            )
                        )
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (racePosition) {
                            1 -> "1ST"
                            2 -> "2ND"
                            3 -> "3RD"
                            else -> "${racePosition}TH"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (racePosition == 1) ElectricGold else NeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "/ $totalRivals",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Lap & Time Center Counter
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LAP $currentLap / $totalLaps",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Color.White
                    )
                    val minutes = (raceTimeSeconds / 60).toInt()
                    val seconds = (raceTimeSeconds % 60).toInt()
                    val millis = ((raceTimeSeconds * 100) % 100).toInt()
                    Text(
                        text = String.format("%02d:%02d.%02d", minutes, seconds, millis),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                }

                // Camera View, Gyro, Pause & Audio Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Camera FPS Toggle Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT) Color(0xFF3B1D47)
                                else DarkCarbon.copy(alpha = 0.7f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT) NitroMagenta else Color.Gray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleCameraViewMode() }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                            .testTag("camera_view_toggle_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Camera View Mode",
                                tint = if (cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT) NitroMagenta else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT) "FPS" else "3RD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT) NitroMagenta else Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Gyro Toggle Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isGyroEnabled) Color(0xFF0F324D)
                                else DarkCarbon.copy(alpha = 0.7f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isGyroEnabled) NeonCyan else Color.Gray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleGyro() }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                            .testTag("gyro_toggle_hud_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Gyro Motion Steering",
                                tint = if (isGyroEnabled) NeonCyan else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGyroEnabled) "GYRO" else "TOUCH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isGyroEnabled) NeonCyan else Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkCarbon.copy(alpha = 0.7f), CircleShape)
                            .testTag("mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Toggle Mute",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onPauseClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkCarbon.copy(alpha = 0.7f), CircleShape)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause Race",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // VISUAL NITRO ENERGY PROGRESS BAR
            val isFullNitro = nitroLevel >= 95f
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = if (isFullNitro) NitroMagenta else NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFullNitro) "⚡ SHOCKWAVE READY!" else "NITRO BOOST GAUGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFullNitro) NitroMagenta else Color.White,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = "${nitroLevel.toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isFullNitro) NitroMagenta else NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF0F141F))
                        .border(
                            width = if (isFullNitro) 2.dp else 1.dp,
                            brush = if (isFullNitro) {
                                Brush.horizontalGradient(listOf(NeonCyan, NitroMagenta, NitroPurple))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                            },
                            shape = RoundedCornerShape(7.dp)
                        )
                ) {
                    val fillRatio = (nitroLevel / 100f).coerceIn(0f, 1f)
                    val nitroBrush = when {
                        nitroStage == NitroStage.SHOCKWAVE || isFullNitro -> Brush.horizontalGradient(
                            listOf(NeonCyan, NitroMagenta, NitroPurple)
                        )
                        nitroStage == NitroStage.PERFECT -> Brush.horizontalGradient(
                            listOf(Color(0xFF00B0FF), NeonCyan)
                        )
                        else -> Brush.horizontalGradient(
                            listOf(Color(0xFFFF9100), ElectricGold)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillRatio)
                            .height(14.dp)
                            .background(nitroBrush)
                    )

                    // Segment dividers at 33% and 66%
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color(0xFF070A10)))
                        Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color(0xFF070A10)))
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // WEATHER TELEMETRY HUD BADGE
        // -------------------------------------------------------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 95.dp, start = 14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkCarbon.copy(alpha = 0.88f))
                .border(1.dp, weatherCondition.badgeColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("weather_telemetry_badge")
        ) {
            Text(
                text = weatherCondition.icon,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = weatherCondition.displayName.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = weatherCondition.badgeColor
                )
                Text(
                    text = "GRIP: ${(weatherCondition.gripMultiplier * 100).toInt()}% | VIS: ${weatherCondition.visibilityMeters.toInt()}m",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // -------------------------------------------------------------
        // PERSISTENT 3D MINI-MAP UI COMPONENT IN CORNER
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 95.dp, end = 14.dp)
        ) {
            MiniMap3D(
                trackData = trackData,
                playerDistanceMeters = trackDistanceMeters,
                totalTrackMeters = totalTrackMeters,
                playerLane = playerLane,
                rivals = rivals
            )
        }

        // -------------------------------------------------------------
        // ACTIVE DRIFT SCORE & MULTIPLIER BADGE
        // -------------------------------------------------------------
        if (isDrifting || currentDriftScore > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 115.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCarbon.copy(alpha = 0.92f))
                    .border(2.dp, ElectricGold, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("drift_score_badge")
            ) {
                Text(
                    text = "🔥 DRIFT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+$currentDriftScore PTS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NitroMagenta)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "x${String.format("%.1f", currentDriftMultiplier)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // NOTIFICATIONS & STUNT POPUPS (e.g. "PERFECT NITRO!", "KNOCKDOWN!")
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = notification != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 85.dp)
        ) {
            notification?.let { notif ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCarbon.copy(alpha = 0.9f))
                        .border(1.5.dp, notif.color, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = notif.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = notif.color,
                        letterSpacing = 1.sp
                    )
                    if (notif.subText != null) {
                        Text(
                            text = notif.subText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // COUNTDOWN OVERLAY AT START (3, 2, 1, GO!)
        // -------------------------------------------------------------
        if (countdownValue >= 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (countdownValue == 0) "GO!!" else countdownValue.toString(),
                    fontSize = if (countdownValue == 0) 72.sp else 92.sp,
                    fontWeight = FontWeight.Black,
                    color = if (countdownValue == 0) SpeedGreen else ElectricGold,
                    letterSpacing = 2.sp
                )
            }
        }

        // -------------------------------------------------------------
        // GYROSCOPE REAL-TIME TILT HORIZON METER
        // -------------------------------------------------------------
        if (isGyroEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCarbon.copy(alpha = 0.88f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { onCalibrateGyro() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("gyro_horizon_meter"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "◀ TILT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (gyroTiltInput < -0.1f) NeonCyan else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Horizon Bar Track
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF0D121B))
                            .border(0.5.dp, Color(0xFF263345), RoundedCornerShape(5.dp))
                    ) {
                        // Center Zero Notch
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(10.dp)
                                .background(Color.White.copy(alpha = 0.5f))
                                .align(Alignment.Center)
                        )

                        // Dynamic Moving Bubble / Indicator
                        val bubbleOffset = (gyroTiltInput * 46f).coerceIn(-46f, 46f)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.Center)
                                .offset(x = bubbleOffset.dp)
                                .clip(CircleShape)
                                .background(
                                    if (abs(gyroTiltInput) > 0.05f) NeonCyan else SpeedGreen
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TILT ▶",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (gyroTiltInput > 0.1f) NeonCyan else Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = "Calibrate Center",
                        tint = if (isCalibrated) SpeedGreen else Color.LightGray,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // BOTTOM HUD: STEERING CONTROLS, COMPACT SPEEDOMETER & ACTIONS
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // LEFT & RIGHT STEERING BUTTONS
            Row(verticalAlignment = Alignment.Bottom) {
                // Steer Left
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkCarbon.copy(alpha = 0.85f))
                        .border(2.dp, NeonCyan.copy(alpha = 0.8f), CircleShape)
                        .testTag("steer_left_button")
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onSteerLeftPress(true)
                                    tryAwaitRelease()
                                    onSteerLeftPress(false)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀",
                        fontSize = 26.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Steer Right
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkCarbon.copy(alpha = 0.85f))
                        .border(2.dp, NeonCyan.copy(alpha = 0.8f), CircleShape)
                        .testTag("steer_right_button")
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onSteerRightPress(true)
                                    tryAwaitRelease()
                                    onSteerRightPress(false)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        fontSize = 26.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // CENTER COMPACT SPEEDOMETER (Out of the car's way)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCarbon.copy(alpha = 0.85f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${playerSpeedKmh.toInt()}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (nitroActive) NitroMagenta else Color.White
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "KM/H",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                val gear = when {
                    playerSpeedKmh < 5 -> "N"
                    playerSpeedKmh < 60 -> "1"
                    playerSpeedKmh < 120 -> "2"
                    playerSpeedKmh < 180 -> "3"
                    playerSpeedKmh < 240 -> "4"
                    playerSpeedKmh < 310 -> "5"
                    playerSpeedKmh < 380 -> "6"
                    else -> "7"
                }
                Text(
                    text = if (nitroActive) "STAGE: ${nitroStage.name}" else "GEAR $gear",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (nitroActive) NitroMagenta else Color.LightGray
                )
            }

            // RIGHT: DRIFT & NITRO BUTTONS
            Row(verticalAlignment = Alignment.Bottom) {
                // Drift Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF261D33).copy(alpha = 0.9f))
                        .border(2.dp, ElectricGold.copy(alpha = 0.85f), CircleShape)
                        .testTag("drift_button")
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onDriftPress(true)
                                    tryAwaitRelease()
                                    onDriftPress(false)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DRIFT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricGold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Large Nitro Boost Button
                val isShockwaveReady = nitroLevel >= 95f
                val nitroScale = if (isShockwaveReady) shockwavePulse else 1.0f

                Box(
                    modifier = Modifier
                        .scale(nitroScale)
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                if (isShockwaveReady) listOf(NitroMagenta, NitroPurple)
                                else listOf(NeonCyan, Color(0xFF0077FF))
                            )
                        )
                        .border(
                            width = 2.5.dp,
                            color = if (isShockwaveReady) ElectricGold else Color.White,
                            shape = CircleShape
                        )
                        .clickable { onNitroClick() }
                        .testTag("nitro_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Nitro Boost",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isShockwaveReady) "SHOCKWAVE" else "NITRO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Persistent 3D Mini-Map Component displayed in the HUD corner.
 * Tracks player position relative to track finish line and opponent rivals.
 */
@Composable
fun MiniMap3D(
    modifier: Modifier = Modifier,
    trackData: TrackData,
    playerDistanceMeters: Float,
    totalTrackMeters: Float,
    playerLane: Float,
    rivals: List<RivalCar>
) {
    val trackLength = (trackData.totalLengthMeters * trackData.laps).coerceAtLeast(1f)
    val progressPercent = ((playerDistanceMeters / trackLength) * 100f).coerceIn(0f, 100f).toInt()

    Box(
        modifier = modifier
            .size(118.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCarbon.copy(alpha = 0.88f))
            .border(1.5.dp, NeonCyan.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(6.dp)
            .testTag("mini_map_3d")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3D MAP",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$progressPercent%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricGold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Canvas(modifier = Modifier.fillMaxSize()) {
                val mapW = size.width
                val mapH = size.height
                val cx = mapW * 0.5f
                val cy = mapH * 0.5f
                val rx = mapW * 0.38f
                val ry = mapH * 0.32f

                // 1. Draw 3D Track Loop Circuit Base
                val trackPath = Path()
                val numPoints = 36
                for (i in 0..numPoints) {
                    val angle = (i.toFloat() / numPoints) * 2f * PI.toFloat() - (PI.toFloat() / 2f)
                    val rMod = 1.0f + (sin(angle * 2f) * trackData.roadCurveIntensity * 0.15f)
                    val x = cx + (rx * rMod) * cos(angle)
                    val y = cy + (ry * rMod) * sin(angle)
                    if (i == 0) trackPath.moveTo(x, y) else trackPath.lineTo(x, y)
                }
                trackPath.close()

                // Dark Track Road Bed
                drawPath(
                    path = trackPath,
                    color = Color(0xFF141C2B),
                    style = Stroke(width = 12f)
                )
                // Neon Track Center Line
                drawPath(
                    path = trackPath,
                    color = NeonCyan.copy(alpha = 0.6f),
                    style = Stroke(width = 3.5f)
                )

                // 2. Finish Line Marker at t = 0 (top of loop)
                val finishX = cx
                val finishY = cy - ry
                drawRect(
                    color = ElectricGold,
                    topLeft = Offset(finishX - 4f, finishY - 6f),
                    size = Size(8f, 12f)
                )

                // 3. Draw Rivals (Red/Gold Opponent Dots)
                for (rival in rivals) {
                    val rivalNorm = ((rival.distanceMeters % trackLength) / trackLength).coerceIn(0f, 1f)
                    val rivalAngle = (rivalNorm * 2f * PI.toFloat()) - (PI.toFloat() / 2f)
                    val rMod = 1.0f + (sin(rivalAngle * 2f) * trackData.roadCurveIntensity * 0.15f)
                    val rivalX = cx + (rx * rMod) * cos(rivalAngle)
                    val rivalY = cy + (ry * rMod) * sin(rivalAngle)

                    drawCircle(
                        color = rival.color,
                        radius = 4.5f,
                        center = Offset(rivalX, rivalY)
                    )
                }

                // 4. Draw Player Position (Cyan Glowing Arrow)
                val playerNorm = ((playerDistanceMeters % trackLength) / trackLength).coerceIn(0f, 1f)
                val playerAngle = (playerNorm * 2f * PI.toFloat()) - (PI.toFloat() / 2f)
                val rMod = 1.0f + (sin(playerAngle * 2f) * trackData.roadCurveIntensity * 0.15f)
                val pX = cx + (rx * rMod) * cos(playerAngle)
                val pY = cy + (ry * rMod) * sin(playerAngle)

                // Outer Glow Pulse
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.45f),
                    radius = 9f,
                    center = Offset(pX, pY)
                )
                // Player Dot
                drawCircle(
                    color = Color.White,
                    radius = 5.5f,
                    center = Offset(pX, pY)
                )
                drawCircle(
                    color = NeonCyan,
                    radius = 3.5f,
                    center = Offset(pX, pY)
                )
            }
        }
    }
}
