package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Photorealistic 3D Real-Time Arcade Racing Engine.
 * Features:
 * 1. Realistic Road with dark tarmac, tire skid rubber marks, concrete curbs, and metallic guardrails.
 * 2. Realistic 3D Buildings & Cyber Towers textured with authentic glass facades, office interiors, and neon signs.
 * 3. Overhead Highway Bridges, suspension arches, and digital gantry signs passing over the car.
 * 4. High-definition 3D player supercar with volumetric shading, quad titanium exhausts, and underglow.
 * 5. 100% full screen top-to-bottom coverage without any gaps.
 */
@Composable
fun RacingCanvas3D(
    modifier: Modifier = Modifier,
    playerCar: CarModel,
    playerSpeedKmh: Float,
    playerLane: Float, // -1.0f (left curb) to +1.0f (right curb)
    trackDistanceMeters: Float,
    trackData: TrackData,
    nitroActive: Boolean,
    nitroStage: NitroStage,
    isDrifting: Boolean,
    driftAngle: Float,
    jumpAirborneHeight: Float,
    spinRotationAngle: Float,
    rivals: List<RivalCar>,
    pickups: List<TrackPickup>,
    screenShakeOffset: Offset,
    cameraViewMode: CameraViewMode = CameraViewMode.THIRD_PERSON,
    cinematicCameraAngle: CinematicCameraAngle? = null,
    weatherCondition: WeatherCondition = trackData.weatherCondition
) {
    val context = LocalContext.current
    val skyscraperBitmap = remember {
        try {
            BitmapFactory.decodeResource(context.resources, R.drawable.tex_skyscraper)
        } catch (e: Exception) {
            null
        }
    }
    val cyberTowerBitmap = remember {
        try {
            BitmapFactory.decodeResource(context.resources, R.drawable.tex_cyber_tower)
        } catch (e: Exception) {
            null
        }
    }
    val asphaltTextureBitmap = remember {
        try {
            BitmapFactory.decodeResource(context.resources, R.drawable.tex_asphalt)
        } catch (e: Exception) {
            null
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        // Horizon elevation: adjusted based on camera mode or cinematic camera angle
        val horizonY = when {
            cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT -> height * 0.25f
            cinematicCameraAngle == CinematicCameraAngle.DRONE_HOVER -> height * 0.38f
            cinematicCameraAngle == CinematicCameraAngle.WHEEL_SIDE -> height * 0.10f
            cinematicCameraAngle == CinematicCameraAngle.BUMPER_FRONT -> height * 0.14f
            cinematicCameraAngle == CinematicCameraAngle.BROADCAST_TV -> height * 0.22f
            else -> height * 0.18f
        }

        val effectiveLane = if (cinematicCameraAngle == CinematicCameraAngle.BROADCAST_TV) {
            val tvPan = sin((trackDistanceMeters * 0.05f).toDouble()).toFloat() * 0.75f
            playerLane + tvPan
        } else playerLane

        translate(screenShakeOffset.x, screenShakeOffset.y) {
            // 1. Atmospheric Sky & Real 3D Distant Skyline (No flat static 2D image)
            drawSkyAndDistantSkyline(
                width = width,
                height = height,
                horizonY = horizonY,
                playerLane = effectiveLane,
                trackDistance = trackDistanceMeters,
                skyTheme = trackData.skyTheme,
                weatherCondition = weatherCondition
            )

            // 2. Photorealistic 3D Road with Asphalt Texture, Wet Gloss Sheen & Skid Marks
            drawRealistic3DRoad(
                width = width,
                height = height,
                horizonY = horizonY,
                playerLane = effectiveLane,
                trackDistance = trackDistanceMeters,
                curveIntensity = trackData.roadCurveIntensity,
                asphaltTex = asphaltTextureBitmap,
                weatherCondition = weatherCondition
            )

            // 3. Real 3D Buildings & Towers with Architectural Facades Rushing Backwards
            drawRealistic3DBuildings(
                width = width,
                height = height,
                horizonY = horizonY,
                playerLane = effectiveLane,
                trackDistance = trackDistanceMeters,
                curveIntensity = trackData.roadCurveIntensity,
                skyscraperTex = skyscraperBitmap,
                cyberTowerTex = cyberTowerBitmap
            )

            // 4. Overhead 3D Highway Bridges, Arches & Gantry Signs
            drawOverhead3DBridges(
                width = width,
                height = height,
                horizonY = horizonY,
                playerLane = effectiveLane,
                trackDistance = trackDistanceMeters,
                curveIntensity = trackData.roadCurveIntensity
            )

            // 5. Overhead 3D Highway Light Poles
            drawHighwayLightPoles(
                width = width,
                height = height,
                horizonY = horizonY,
                playerLane = effectiveLane,
                trackDistance = trackDistanceMeters,
                curveIntensity = trackData.roadCurveIntensity
            )

            // 6. 3D Track Pickups (Nitro Bottles & Coins in Depth)
            drawTrackPickups(
                width = width,
                height = height,
                horizonY = horizonY,
                playerLane = effectiveLane,
                trackDistance = trackDistanceMeters,
                pickups = pickups
            )

            // 7. 3D Rival Cars
            draw3DRivalCars(
                width = width,
                height = height,
                horizonY = horizonY,
                playerLane = effectiveLane,
                playerDistance = trackDistanceMeters,
                rivals = rivals
            )

            // 8. Motion Warp Speed Lines at High Velocity
            if (playerSpeedKmh > 220f || nitroActive) {
                drawSpeedWarpLines(
                    width = width,
                    height = height,
                    horizonY = horizonY,
                    speedKmh = playerSpeedKmh,
                    nitroStage = nitroStage
                )
            }

            // 9. Volumetric Fog Layer (dense horizon & track fog)
            if (weatherCondition.fogAlpha > 0.05f) {
                drawVolumetricFogOverlay(
                    width = width,
                    height = height,
                    horizonY = horizonY,
                    weatherCondition = weatherCondition
                )
            }

            // 10. Animated Rain Drop Particles & Tire Water Spray
            if (weatherCondition.rainIntensity > 0.05f) {
                drawRainEffect(
                    width = width,
                    height = height,
                    weatherCondition = weatherCondition,
                    trackDistance = trackDistanceMeters,
                    playerSpeedKmh = playerSpeedKmh,
                    playerLane = effectiveLane
                )
            }

            // 11. Player Rendering: Third Person, Cockpit, Bumper, or Cinematic
            if (cameraViewMode == CameraViewMode.FIRST_PERSON_COCKPIT) {
                drawDashboardCockpitView(
                    width = width,
                    height = height,
                    playerCar = playerCar,
                    playerSpeedKmh = playerSpeedKmh,
                    steerInput = driftAngle,
                    nitroActive = nitroActive,
                    nitroStage = nitroStage,
                    weatherCondition = weatherCondition,
                    trackDistance = trackDistanceMeters
                )
            } else if (cinematicCameraAngle == CinematicCameraAngle.BUMPER_FRONT) {
                // Front bumper camera: no vehicle body rendered to provide an aggressive road-hugging viewpoint
            } else {
                drawPlayerSupercar(
                    width = width,
                    height = height,
                    playerCar = playerCar,
                    playerSpeedKmh = playerSpeedKmh,
                    playerLane = effectiveLane,
                    isDrifting = isDrifting,
                    driftAngle = driftAngle,
                    jumpHeight = jumpAirborneHeight,
                    spinAngle = spinRotationAngle,
                    nitroActive = nitroActive,
                    nitroStage = nitroStage
                )
            }
        }
    }
}

// -------------------------------------------------------------
// ATMOSPHERIC SKY & DYNAMIC 3D DISTANT SKYLINE (NO 2D FLAT IMAGE)
// -------------------------------------------------------------
private fun DrawScope.drawSkyAndDistantSkyline(
    width: Float,
    height: Float,
    horizonY: Float,
    playerLane: Float,
    trackDistance: Float,
    skyTheme: String,
    weatherCondition: WeatherCondition = WeatherCondition.CLEAR
) {
    // 1. Celestial Sky Gradient adjusted for weather conditions (Overcast storm vs clear sky)
    val skyColors = when (weatherCondition) {
        WeatherCondition.HEAVY_RAIN -> listOf(
            Color(0xFF04070D), // Stormy dark charcoal
            Color(0xFF0C1322), // Storm gray indigo
            Color(0xFF141F33), // Wet slate blue
            Color(0xFF1F2B3E), // Cold overcast horizon
            Color(0xFF2B384A)
        )
        WeatherCondition.THICK_FOG -> listOf(
            Color(0xFF0B1019), // Mist shrouded sky
            Color(0xFF131A28), // Soft slate fog
            Color(0xFF1E2838), // Dense overcast haze
            Color(0xFF2C394E),
            Color(0xFF38475E)
        )
        WeatherCondition.LIGHT_RAIN -> listOf(
            Color(0xFF040710),
            Color(0xFF0D1528),
            Color(0xFF16223A),
            Color(0xFF241A38),
            Color(0xFF342244)
        )
        WeatherCondition.CLEAR -> listOf(
            Color(0xFF03050C), // Cosmic black at top
            Color(0xFF0B1020), // Deep indigo
            Color(0xFF141933), // Cyber blue
            Color(0xFF261842), // Atmospheric twilight
            Color(0xFF381F4B)  // Horizon warm city glow
        )
    }

    val skyGradient = Brush.verticalGradient(
        colors = skyColors,
        startY = 0f,
        endY = horizonY
    )
    drawRect(brush = skyGradient, topLeft = Offset.Zero, size = Size(width, horizonY))

    // 2. Stars in the Upper Atmosphere
    val numStars = 32
    for (s in 0 until numStars) {
        val starX = (s * 137.5f) % width
        val starY = (s * 41.7f) % (horizonY * 0.85f)
        val starAlpha = 0.45f + (((s * 19) % 10) / 20f)
        drawCircle(
            color = Color.White.copy(alpha = starAlpha),
            radius = 1.2f,
            center = Offset(starX, starY)
        )
    }

    // 3. Cyberpunk Moon Orb with Atmospheric Radial Glow
    val moonCenterX = width * 0.78f - (playerLane * 20f)
    val moonCenterY = horizonY * 0.38f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x3300F0FF), Color(0x00000000)),
            center = Offset(moonCenterX, moonCenterY),
            radius = 52f
        ),
        radius = 52f,
        center = Offset(moonCenterX, moonCenterY)
    )
    drawCircle(
        color = Color(0xFFD8EEFF),
        radius = 16f,
        center = Offset(moonCenterX, moonCenterY)
    )

    // 4. Distant 3D Architectural City Skyline across the entire horizon
    // Pans dynamically with track curve and player steering for genuine 3D perspective
    val scrollOffset = ((trackDistance * 0.035f) - (playerLane * 75f))
    val numDistantTowers = 36
    val slotWidth = width / 18f

    for (t in -4..numDistantTowers + 4) {
        val towerId = ((t % 15) + 15) % 15
        val towerSeed = towerId * 37

        val tWidth = slotWidth * (0.85f + ((towerSeed % 5) * 0.18f))
        val tHeight = 35f + ((towerSeed % 9) * 14f)
        val tX = (t * slotWidth * 1.05f) - (scrollOffset % (slotWidth * 15f))
        val tY = horizonY

        if (tX + tWidth < -40f || tX > width + 40f) continue

        // Tower Silhouette Body
        val towerColor = when (towerId % 4) {
            0 -> Color(0xFF101726)
            1 -> Color(0xFF161F33)
            2 -> Color(0xFF0C121D)
            else -> Color(0xFF1D283F)
        }
        drawRect(
            color = towerColor,
            topLeft = Offset(tX, tY - tHeight),
            size = Size(tWidth, tHeight)
        )

        // Glass Office Window Grids on Distant Towers
        val numCols = (tWidth / 7f).toInt().coerceAtLeast(1)
        val numRows = (tHeight / 10f).toInt().coerceAtLeast(1)
        for (r in 0 until numRows) {
            val winY = tY - tHeight + 6f + (r * 10f)
            if (winY >= tY - 4f) continue
            for (c in 0 until numCols) {
                val winX = tX + 3f + (c * 7f)
                val isLit = ((towerSeed + r * 7 + c * 13) % 5) < 3
                if (isLit) {
                    val winColor = when ((towerSeed + r + c) % 6) {
                        0 -> Color(0xBB00F0FF) // Cyan
                        1 -> Color(0xBBDDF4FF) // Warm white
                        2 -> Color(0xBBFFD700) // Amber
                        3 -> Color(0xBBFF007F) // Magenta
                        else -> Color(0x8898A8C0) // Soft blue
                    }
                    drawRect(
                        color = winColor,
                        topLeft = Offset(winX, winY),
                        size = Size(3.5f, 5f)
                    )
                }
            }
        }

        // Rooftop Communication Spire & Flashing Hazard Beacon
        if (tHeight > 60f) {
            val spireX = tX + tWidth * 0.5f
            val spireH = tHeight * 0.35f
            drawLine(
                color = Color(0xFF4C5D7A),
                start = Offset(spireX, tY - tHeight),
                end = Offset(spireX, tY - tHeight - spireH),
                strokeWidth = 1.5f
            )
            val blink = ((trackDistance * 0.1f + towerSeed).toInt() % 2) == 0
            if (blink) {
                drawCircle(
                    color = Color(0xFFFF1744),
                    radius = 2.2f,
                    center = Offset(spireX, tY - tHeight - spireH)
                )
            }
        }
    }

    // 5. Volumetric City Light Fog along the horizon line
    val fogBrush = Brush.verticalGradient(
        colors = listOf(Color(0x000E131E), Color(0x99231838), Color(0xDD0E131E)),
        startY = horizonY - 24f,
        endY = horizonY + 8f
    )
    drawRect(
        brush = fogBrush,
        topLeft = Offset(0f, horizonY - 24f),
        size = Size(width, 32f)
    )
}

private class RoadPoint(
    var screenX: Float = 0f,
    var screenY: Float = 0f,
    var roadWidth: Float = 0f,
    var curbWidth: Float = 0f,
    var scale: Float = 0f,
    var isEven: Boolean = false,
    var curveOffset: Float = 0f
)

// -------------------------------------------------------------
// REALISTIC 3D ROAD PROJECTION WITH ASPHALT TEXTURE & SKID MARKS
// -------------------------------------------------------------
private fun DrawScope.drawRealistic3DRoad(
    width: Float,
    height: Float,
    horizonY: Float,
    playerLane: Float,
    trackDistance: Float,
    curveIntensity: Float,
    asphaltTex: Bitmap?,
    weatherCondition: WeatherCondition = WeatherCondition.CLEAR
) {
    val numSegments = 90
    val roadScreenHeight = height - horizonY
    val baseRoadWidth = width * 1.85f

    // 1. Off-Road Ground/Shoulder Terrain
    val terrainBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0E131E), Color(0xFF05070C)),
        startY = horizonY,
        endY = height + 10f
    )
    drawRect(brush = terrainBrush, topLeft = Offset(0f, horizonY), size = Size(width, height - horizonY + 10f))

    val points = Array(numSegments + 1) { RoadPoint() }

    for (i in 0..numSegments) {
        val t = i.toFloat() / numSegments
        val depth = 1f - t
        val scale = Math.pow(depth.toDouble(), 1.75).toFloat()

        val worldDistance = trackDistance + (i * 38f)
        val curveOffset = sin((worldDistance * 0.0028).toDouble()).toFloat() * (220f * curveIntensity)
        val hillOffset = cos((worldDistance * 0.0016).toDouble()).toFloat() * 50f

        val screenY = horizonY + (roadScreenHeight * scale) + (hillOffset * scale * 1.4f)
        val screenX = (width * 0.5f) + (curveOffset - (playerLane * width * 0.65f)) * scale
        val roadW = baseRoadWidth * scale

        points[i].screenX = screenX
        points[i].screenY = screenY
        points[i].roadWidth = roadW
        points[i].curbWidth = roadW * 0.10f
        points[i].scale = scale
        points[i].isEven = ((worldDistance * 0.05f).toInt() % 2) == 0
        points[i].curveOffset = curveOffset
    }

    // 2. Render Road Segments Far to Near
    for (i in (numSegments - 1) downTo 0) {
        val pFar = points[i + 1]
        val pNear = points[i]
        val isEven = pNear.isEven

        // Realistic Dark Asphalt Road Surface (slight blue/gray tint when wet)
        val roadColor = if (weatherCondition.roadReflectivity > 0.2f) {
            if (isEven) Color(0xFF101926) else Color(0xFF172233)
        } else {
            if (isEven) Color(0xFF141721) else Color(0xFF1B1F2C)
        }
        val roadPath = Path().apply {
            moveTo(pFar.screenX - pFar.roadWidth * 0.5f, pFar.screenY)
            lineTo(pFar.screenX + pFar.roadWidth * 0.5f, pFar.screenY)
            lineTo(pNear.screenX + pNear.roadWidth * 0.5f, pNear.screenY)
            lineTo(pNear.screenX - pNear.roadWidth * 0.5f, pNear.screenY)
            close()
        }
        drawPath(roadPath, color = roadColor)

        // Glossy Wet Asphalt Water Reflection Sheen on road surface
        if (weatherCondition.roadReflectivity > 0.2f && i % 3 == 0) {
            val reflectionAlpha = (weatherCondition.roadReflectivity * 0.35f * pNear.scale).coerceIn(0.05f, 0.40f)
            val sheenGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF00F0FF).copy(alpha = reflectionAlpha),
                    Color(0x00FFFFFF)
                ),
                startY = pFar.screenY,
                endY = pNear.screenY
            )
            drawPath(roadPath, brush = sheenGradient)
        }

        // Asphalt Texture overlay in foreground (closest 40 segments)
        if (asphaltTex != null && i < 40 && i % 2 == 0) {
            val imgBmp = asphaltTex.asImageBitmap()
            val texW = (pNear.roadWidth * 0.96f).toInt().coerceAtLeast(10)
            val texH = ((pNear.screenY - pFar.screenY) * 1.15f).toInt().coerceAtLeast(4)
            val texX = (pNear.screenX - texW * 0.5f).toInt()
            val texY = pFar.screenY.toInt()

            drawImage(
                image = imgBmp,
                dstOffset = IntOffset(texX, texY),
                dstSize = IntSize(texW, texH),
                alpha = 0.28f
            )
        }

        // Concrete Sidewalk Shoulders (Dark Gray Road Border)
        val shoulderWFar = pFar.curbWidth * 0.45f
        val shoulderWNear = pNear.curbWidth * 0.45f

        // Left Concrete Shoulder
        val leftShoulder = Path().apply {
            moveTo(pFar.screenX - pFar.roadWidth * 0.5f, pFar.screenY)
            lineTo(pFar.screenX - pFar.roadWidth * 0.5f - shoulderWFar, pFar.screenY)
            lineTo(pNear.screenX - pNear.roadWidth * 0.5f - shoulderWNear, pNear.screenY)
            lineTo(pNear.screenX - pNear.roadWidth * 0.5f, pNear.screenY)
            close()
        }
        drawPath(leftShoulder, color = Color(0xFF2C3242))

        // Right Concrete Shoulder
        val rightShoulder = Path().apply {
            moveTo(pFar.screenX + pFar.roadWidth * 0.5f, pFar.screenY)
            lineTo(pFar.screenX + pFar.roadWidth * 0.5f + shoulderWFar, pFar.screenY)
            lineTo(pNear.screenX + pNear.roadWidth * 0.5f + shoulderWNear, pNear.screenY)
            lineTo(pNear.screenX + pNear.roadWidth * 0.5f, pNear.screenY)
            close()
        }
        drawPath(rightShoulder, color = Color(0xFF2C3242))

        // Alternating Racing Curbs (Vibrant Red & Pure White with Bevels)
        val curbColor = if (isEven) Color(0xFFFF2238) else Color(0xFFF0F0F0)
        val leftCurbPath = Path().apply {
            moveTo(pFar.screenX - pFar.roadWidth * 0.5f - shoulderWFar, pFar.screenY)
            lineTo(pFar.screenX - pFar.roadWidth * 0.5f - pFar.curbWidth, pFar.screenY)
            lineTo(pNear.screenX - pNear.roadWidth * 0.5f - pNear.curbWidth, pNear.screenY)
            lineTo(pNear.screenX - pNear.roadWidth * 0.5f - shoulderWNear, pNear.screenY)
            close()
        }
        drawPath(leftCurbPath, color = curbColor)

        val rightCurbPath = Path().apply {
            moveTo(pFar.screenX + pFar.roadWidth * 0.5f + shoulderWFar, pFar.screenY)
            lineTo(pFar.screenX + pFar.roadWidth * 0.5f + pFar.curbWidth, pFar.screenY)
            lineTo(pNear.screenX + pNear.roadWidth * 0.5f + pNear.curbWidth, pNear.screenY)
            lineTo(pNear.screenX + pNear.roadWidth * 0.5f + shoulderWNear, pNear.screenY)
            close()
        }
        drawPath(rightCurbPath, color = curbColor)

        // REALISTIC TIRE SKID BURNOUT MARKS (Drift Rubber Marks on Road)
        if (i in 5..50 && i % 4 in 0..2) {
            val skidOffsetLeft = -pNear.roadWidth * 0.16f + sin(i * 0.4) * 12f
            val skidOffsetRight = pNear.roadWidth * 0.16f + sin(i * 0.4) * 12f
            val skidW = pNear.roadWidth * 0.032f

            // Left Skid Mark
            drawRoundRect(
                color = Color(0x7706080E),
                topLeft = Offset(pNear.screenX + skidOffsetLeft.toFloat(), pNear.screenY - 14f),
                size = Size(skidW, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
            )
            // Right Skid Mark
            drawRoundRect(
                color = Color(0x7706080E),
                topLeft = Offset(pNear.screenX + skidOffsetRight.toFloat(), pNear.screenY - 14f),
                size = Size(skidW, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
            )
        }

        // REALISTIC ROAD LANE MARKINGS
        if (isEven) {
            val centerWFar = pFar.roadWidth * 0.024f
            val centerWNear = pNear.roadWidth * 0.024f

            // Center Dashed Double Yellow Line
            val centerPath = Path().apply {
                moveTo(pFar.screenX - centerWFar * 0.5f, pFar.screenY)
                lineTo(pFar.screenX + centerWFar * 0.5f, pFar.screenY)
                lineTo(pNear.screenX + centerWNear * 0.5f, pNear.screenY)
                lineTo(pNear.screenX - centerWNear * 0.5f, pNear.screenY)
                close()
            }
            drawPath(centerPath, color = Color(0xFFFFD600))

            // Left Highway Lane White Line
            val leftLaneOffsetFar = -pFar.roadWidth * 0.22f
            val leftLaneOffsetNear = -pNear.roadWidth * 0.22f
            val leftLanePath = Path().apply {
                moveTo(pFar.screenX + leftLaneOffsetFar - centerWFar * 0.35f, pFar.screenY)
                lineTo(pFar.screenX + leftLaneOffsetFar + centerWFar * 0.35f, pFar.screenY)
                lineTo(pNear.screenX + leftLaneOffsetNear + centerWNear * 0.35f, pNear.screenY)
                lineTo(pNear.screenX + leftLaneOffsetNear - centerWNear * 0.35f, pNear.screenY)
                close()
            }
            drawPath(leftLanePath, color = Color(0xEEFFFFFF))

            // Right Highway Lane White Line
            val rightLaneOffsetFar = pFar.roadWidth * 0.22f
            val rightLaneOffsetNear = pNear.roadWidth * 0.22f
            val rightLanePath = Path().apply {
                moveTo(pFar.screenX + rightLaneOffsetFar - centerWFar * 0.35f, pFar.screenY)
                lineTo(pFar.screenX + rightLaneOffsetFar + centerWFar * 0.35f, pFar.screenY)
                lineTo(pNear.screenX + rightLaneOffsetNear + centerWNear * 0.35f, pNear.screenY)
                lineTo(pNear.screenX + rightLaneOffsetNear - centerWNear * 0.35f, pNear.screenY)
                close()
            }
            drawPath(rightLanePath, color = Color(0xEEFFFFFF))
        }

        // METALLIC HIGHWAY GUARDRAIL BARRIERS (W-Beam Posts with Reflectors)
        if (i % 3 == 0) {
            val railHeightNear = 22f * pNear.scale * 2.2f
            val leftRailXNear = pNear.screenX - pNear.roadWidth * 0.5f - pNear.curbWidth * 1.15f
            val rightRailXNear = pNear.screenX + pNear.roadWidth * 0.5f + pNear.curbWidth * 1.15f

            // Left Post
            drawRect(
                color = Color(0xFF6B7A99),
                topLeft = Offset(leftRailXNear - 2.5f, pNear.screenY - railHeightNear),
                size = Size(5f, railHeightNear)
            )
            drawCircle(
                color = Color(0xFF00F0FF),
                radius = (5f * pNear.scale * 2.2f).coerceAtLeast(2f),
                center = Offset(leftRailXNear, pNear.screenY - railHeightNear * 0.85f)
            )

            // Right Post
            drawRect(
                color = Color(0xFF6B7A99),
                topLeft = Offset(rightRailXNear - 2.5f, pNear.screenY - railHeightNear),
                size = Size(5f, railHeightNear)
            )
            drawCircle(
                color = Color(0xFFFF007F),
                radius = (5f * pNear.scale * 2.2f).coerceAtLeast(2f),
                center = Offset(rightRailXNear, pNear.screenY - railHeightNear * 0.85f)
            )
        }
    }
}

// -------------------------------------------------------------
// REALISTIC 3D BUILDINGS & TOWERS WITH TEXTURED FAÇADES
// -------------------------------------------------------------
private fun DrawScope.drawRealistic3DBuildings(
    width: Float,
    height: Float,
    horizonY: Float,
    playerLane: Float,
    trackDistance: Float,
    curveIntensity: Float,
    skyscraperTex: Bitmap?,
    cyberTowerTex: Bitmap?
) {
    val roadScreenHeight = height - horizonY
    val baseRoadWidth = width * 1.85f

    val buildingInterval = 90f
    val maxDrawDistance = 2800f

    val startIdx = (trackDistance / buildingInterval).toInt()
    val endIdx = startIdx + 28

    val skyImg = skyscraperTex?.asImageBitmap()
    val towerImg = cyberTowerTex?.asImageBitmap()

    for (idx in endIdx downTo startIdx) {
        val bDist = idx * buildingInterval
        val relDist = bDist - trackDistance
        if (relDist < 6f || relDist > maxDrawDistance) continue

        val normDist = (relDist / maxDrawDistance).coerceIn(0f, 1f)
        val scale = Math.pow((1f - normDist).toDouble(), 2.0).toFloat()

        val curveOffset = sin((bDist * 0.0028).toDouble()).toFloat() * (220f * curveIntensity)
        val roadCenterX = (width * 0.5f) + (curveOffset - (playerLane * width * 0.65f)) * scale
        val roadCenterY = horizonY + (roadScreenHeight * scale)
        val roadW = baseRoadWidth * scale

        val isLeft = (idx % 2 == 0)
        val bWidthBase = 85f + ((idx * 31) % 55)
        val bHeightBase = 260f + ((idx * 47) % 320)
        val bDepthBase = 65f + ((idx * 17) % 40)

        val bW = bWidthBase * scale * 2.8f
        val bH = bHeightBase * scale * 3.6f
        val bD = bDepthBase * scale * 2.4f

        if (bH < 4f) continue

        val sideOffset = if (isLeft) -roadW * 0.58f - bW * 0.95f else roadW * 0.58f + bW * 0.05f
        val bX = roadCenterX + sideOffset
        val bY = roadCenterY

        // 1. CONCRETE SIDEWALK FOUNDATION BASE
        drawRect(
            color = Color(0xFF232A38),
            topLeft = Offset(bX - 4f, bY - 6f),
            size = Size(bW + 8f, 8f)
        )

        // 2. 3D PERSPECTIVE SIDE WALL (Architectural concrete columns & floor slabs)
        val sideWall = Path().apply {
            if (isLeft) {
                moveTo(bX, bY)
                lineTo(bX - bD * 0.6f, bY - bD * 0.3f)
                lineTo(bX - bD * 0.6f, bY - bH - bD * 0.3f)
                lineTo(bX, bY - bH)
            } else {
                moveTo(bX + bW, bY)
                lineTo(bX + bW + bD * 0.6f, bY - bD * 0.3f)
                lineTo(bX + bW + bD * 0.6f, bY - bH - bD * 0.3f)
                lineTo(bX + bW, bY - bH)
            }
            close()
        }
        drawPath(sideWall, color = Color(0xFF0F1420))

        // Floor slab lines on side wall
        if (bH > 30f) {
            val numFloors = 6
            for (fl in 1 until numFloors) {
                val flY = bY - (fl * (bH / numFloors))
                if (isLeft) {
                    drawLine(
                        color = Color(0xFF2A364F),
                        start = Offset(bX, flY),
                        end = Offset(bX - bD * 0.6f, flY - bD * 0.3f),
                        strokeWidth = 2f
                    )
                } else {
                    drawLine(
                        color = Color(0xFF2A364F),
                        start = Offset(bX + bW, flY),
                        end = Offset(bX + bW + bD * 0.6f, flY - bD * 0.3f),
                        strokeWidth = 2f
                    )
                }
            }
        }

        // 3. FRONT FAÇADE: REAL PHOTOREALISTIC BUILDING TEXTURE
        val texToUse = if (idx % 2 == 0) skyImg else towerImg
        if (texToUse != null && bW > 12f && bH > 16f) {
            drawImage(
                image = texToUse,
                dstOffset = IntOffset(bX.toInt(), (bY - bH).toInt()),
                dstSize = IntSize(bW.toInt(), bH.toInt())
            )
            // Architectural edge outline frame
            drawRect(
                color = Color(0xFF384A6B),
                topLeft = Offset(bX, bY - bH),
                size = Size(bW, bH),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        } else {
            // Procedural fallback glass curtain wall
            val frontWall = Path().apply {
                moveTo(bX, bY)
                lineTo(bX + bW, bY)
                lineTo(bX + bW, bY - bH)
                lineTo(bX, bY - bH)
                close()
            }
            val wallBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1E273C), Color(0xFF0E131D)),
                startY = bY - bH,
                endY = bY
            )
            drawPath(frontWall, brush = wallBrush)
        }

        // 4. ROOFTOP PARAPET & REALISTIC HVAC CHILLERS / ANTENNAS
        val roof = Path().apply {
            if (isLeft) {
                moveTo(bX, bY - bH)
                lineTo(bX + bW, bY - bH)
                lineTo(bX + bW - bD * 0.6f, bY - bH - bD * 0.3f)
                lineTo(bX - bD * 0.6f, bY - bH - bD * 0.3f)
            } else {
                moveTo(bX, bY - bH)
                lineTo(bX + bW, bY - bH)
                lineTo(bX + bW + bD * 0.6f, bY - bH - bD * 0.3f)
                lineTo(bX + bD * 0.6f, bY - bH - bD * 0.3f)
            }
            close()
        }
        drawPath(roof, color = Color(0xFF28364D))

        // Rooftop Air Conditioning Units (AC boxes)
        if (bW > 24f && bH > 35f) {
            val acW = bW * 0.28f
            val acH = 8f * scale * 2f
            drawRect(
                color = Color(0xFF4A5A78),
                topLeft = Offset(bX + bW * 0.15f, bY - bH - acH),
                size = Size(acW, acH)
            )
        }

        // Communications Mast & Aviation Strobe
        if (bH > 30f) {
            val antennaX = bX + bW * 0.5f
            val antennaH = bH * 0.28f
            drawLine(
                color = Color(0xFF8B9BB4),
                start = Offset(antennaX, bY - bH),
                end = Offset(antennaX, bY - bH - antennaH),
                strokeWidth = (2.5f * scale * 2.2f).coerceAtLeast(1.5f)
            )
            // Flashing Red Aviation Beacon
            drawCircle(
                color = Color(0xFFFF1744),
                radius = (3.5f * scale * 2.5f).coerceAtLeast(1.8f),
                center = Offset(antennaX, bY - bH - antennaH)
            )
        }

        // 5. Overhead Connecting Skybridge between skyscrapers across the highway
        if (idx % 4 == 0 && scale > 0.08f) {
            val skybridgeSpan = roadW * 1.16f
            val skybridgeLeftX = roadCenterX - roadW * 0.58f
            val skybridgeW = skybridgeSpan
            val skybridgeH = (26f * scale * 2.5f).coerceAtLeast(6f)
            val skybridgeY = bY - (bH * 0.65f).coerceAtLeast(100f * scale * 2.5f)

            // Skybridge body (glass-enclosed high-level walkway)
            val skybridgeRect = Path().apply {
                moveTo(skybridgeLeftX, skybridgeY)
                lineTo(skybridgeLeftX + skybridgeW, skybridgeY)
                lineTo(skybridgeLeftX + skybridgeW, skybridgeY + skybridgeH)
                lineTo(skybridgeLeftX, skybridgeY + skybridgeH)
                close()
            }
            drawPath(
                skybridgeRect,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2E3E5C), Color(0xFF141C2B)),
                    startY = skybridgeY,
                    endY = skybridgeY + skybridgeH
                )
            )
            // Glowing neon handrail / window strip
            drawRect(
                color = Color(0xFF00F0FF),
                topLeft = Offset(skybridgeLeftX, skybridgeY + skybridgeH * 0.45f),
                size = Size(skybridgeW, (3.5f * scale * 2.0f).coerceAtLeast(1.5f))
            )
            // Steel truss diagonal supports
            val numTrusses = 6
            val trussSpacing = skybridgeW / numTrusses
            for (t in 0..numTrusses) {
                val tx = skybridgeLeftX + t * trussSpacing
                drawLine(
                    color = Color(0x664B628A),
                    start = Offset(tx, skybridgeY),
                    end = Offset(tx, skybridgeY + skybridgeH),
                    strokeWidth = 1.5f
                )
            }
        }
    }
}

// -------------------------------------------------------------
// OVERHEAD 3D HIGHWAY BRIDGES, ARCHES & GANTRY SIGNS
// -------------------------------------------------------------
private fun DrawScope.drawOverhead3DBridges(
    width: Float,
    height: Float,
    horizonY: Float,
    playerLane: Float,
    trackDistance: Float,
    curveIntensity: Float
) {
    val roadScreenHeight = height - horizonY
    val baseRoadWidth = width * 1.85f

    // Frequent bridges every 320m for a continuous real-time highway experience
    val bridgeInterval = 320f
    val startIdx = ((trackDistance - 30f) / bridgeInterval).toInt().coerceAtLeast(0)
    val endIdx = startIdx + 5

    for (idx in endIdx downTo startIdx) {
        val bridgeTrackDist = idx * bridgeInterval + 180f
        val relDist = bridgeTrackDist - trackDistance

        if (relDist < -25f || relDist > 2800f) continue

        val normDist = (relDist.coerceAtLeast(0f) / 2800f).coerceIn(0f, 1f)
        val scale = Math.pow((1f - normDist).toDouble(), 2.0).toFloat()

        val curveOffset = sin((bridgeTrackDist * 0.0028).toDouble()).toFloat() * (220f * curveIntensity)
        val roadCenterX = (width * 0.5f) + (curveOffset - (playerLane * width * 0.65f)) * scale
        val roadCenterY = horizonY + (roadScreenHeight * scale)
        val roadW = baseRoadWidth * scale

        // Soaring monumental bridge height reaching into the upper screen
        val bridgeHeight = 250f * scale * 3.4f
        val beamThickness = 48f * scale * 2.8f
        val pillarWidth = (roadW * 0.14f).coerceAtLeast(8f)

        val leftPillarX = roadCenterX - roadW * 0.56f - pillarWidth
        val rightPillarX = roadCenterX + roadW * 0.56f
        val beamTopY = roadCenterY - bridgeHeight
        val beamBottomY = beamTopY + beamThickness

        // 1. Road Surface Shadow Beneath Bridge
        val shadowW = roadW * 1.05f
        val shadowH = 28f * scale * 2.0f
        drawOval(
            color = Color(0x99000000),
            topLeft = Offset(roadCenterX - shadowW * 0.5f, roadCenterY - shadowH * 0.5f),
            size = Size(shadowW, shadowH)
        )

        // 2. Concrete Left Pillar
        val leftPillar = Path().apply {
            moveTo(leftPillarX, roadCenterY)
            lineTo(leftPillarX + pillarWidth, roadCenterY)
            lineTo(leftPillarX + pillarWidth, beamTopY)
            lineTo(leftPillarX, beamTopY)
            close()
        }
        drawPath(
            leftPillar,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF222B3D), Color(0xFF3B4861)),
                startX = leftPillarX,
                endX = leftPillarX + pillarWidth
            )
        )

        val stripeH = (22f * scale * 2f).coerceAtLeast(6f)
        drawRect(
            color = Color(0xFFFFD700),
            topLeft = Offset(leftPillarX, roadCenterY - stripeH),
            size = Size(pillarWidth, stripeH)
        )

        // 3. Concrete Right Pillar
        val rightPillar = Path().apply {
            moveTo(rightPillarX, roadCenterY)
            lineTo(rightPillarX + pillarWidth, roadCenterY)
            lineTo(rightPillarX + pillarWidth, beamTopY)
            lineTo(rightPillarX, beamTopY)
            close()
        }
        drawPath(
            rightPillar,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF3B4861), Color(0xFF222B3D)),
                startX = rightPillarX,
                endX = rightPillarX + pillarWidth
            )
        )

        drawRect(
            color = Color(0xFFFFD700),
            topLeft = Offset(rightPillarX, roadCenterY - stripeH),
            size = Size(pillarWidth, stripeH)
        )

        // 4. Overhead Highway Span Deck
        val spanWidth = (rightPillarX + pillarWidth) - leftPillarX
        val bridgeDeck = Path().apply {
            moveTo(leftPillarX, beamTopY)
            lineTo(leftPillarX + spanWidth, beamTopY)
            lineTo(leftPillarX + spanWidth, beamBottomY)
            lineTo(leftPillarX, beamBottomY)
            close()
        }
        val deckBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF485672), Color(0xFF1B2333)),
            startY = beamTopY,
            endY = beamBottomY
        )
        drawPath(bridgeDeck, brush = deckBrush)

        // 5. Suspension Arch & Cable Stays
        val archPeakHeight = bridgeHeight * 1.15f
        val archTopY = beamTopY - archPeakHeight
        val archPath = Path().apply {
            moveTo(leftPillarX, beamTopY)
            quadraticTo(roadCenterX, archTopY, rightPillarX + pillarWidth, beamTopY)
            lineTo(rightPillarX + pillarWidth, beamTopY + 4f)
            quadraticTo(roadCenterX, archTopY + 6f, leftPillarX, beamTopY + 4f)
            close()
        }
        drawPath(archPath, color = Color(0xFF00F0FF))

        val numCables = 7
        for (c in 1 until numCables) {
            val t = c.toFloat() / numCables
            val cableX = leftPillarX + spanWidth * t
            val archPointY = beamTopY - (4f * t * (1f - t) * archPeakHeight)
            drawLine(
                color = Color(0xAA00F0FF),
                start = Offset(cableX, archPointY),
                end = Offset(cableX, beamTopY),
                strokeWidth = (2f * scale * 2.2f).coerceAtLeast(1.2f)
            )
        }

        // 6. Overhead Digital Signboard
        val signW = roadW * 0.55f
        val signH = beamThickness * 0.85f
        val signX = roadCenterX - signW * 0.5f
        val signY = beamTopY + (beamThickness - signH) * 0.5f

        drawRoundRect(
            color = Color(0xFF0A0F1A),
            topLeft = Offset(signX, signY),
            size = Size(signW, signH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
        val signBorderColor = if (idx % 2 == 0) Color(0xFF00F0FF) else Color(0xFFFF007F)
        drawRoundRect(
            color = signBorderColor,
            topLeft = Offset(signX, signY),
            size = Size(signW, signH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
        )

        val stripW = signW * 0.75f
        val stripH = signH * 0.35f
        drawRect(
            color = if (idx % 2 == 0) Color(0xFF00FF66) else Color(0xFFFFD700),
            topLeft = Offset(roadCenterX - stripW * 0.5f, signY + signH * 0.32f),
            size = Size(stripW, stripH)
        )

        // 7. Ceiling Lights on Bridge Underside
        val numCeilingLights = 6
        val lightW = (roadW / numCeilingLights) * 0.65f
        for (l in 0 until numCeilingLights) {
            val lightX = roadCenterX - roadW * 0.45f + l * (roadW * 0.9f / (numCeilingLights - 1))
            drawRect(
                color = Color(0xFFFFF275),
                topLeft = Offset(lightX - lightW * 0.5f, beamBottomY - 3f),
                size = Size(lightW, 4f)
            )
        }
    }
}

// -------------------------------------------------------------
// OVERHEAD 3D HIGHWAY LIGHT POLES
// -------------------------------------------------------------
private fun DrawScope.drawHighwayLightPoles(
    width: Float,
    height: Float,
    horizonY: Float,
    playerLane: Float,
    trackDistance: Float,
    curveIntensity: Float
) {
    val roadScreenHeight = height - horizonY
    val baseRoadWidth = width * 1.85f

    val poleInterval = 65f
    val startIdx = (trackDistance / poleInterval).toInt()
    val endIdx = startIdx + 20

    for (idx in endIdx downTo startIdx) {
        val pDist = idx * poleInterval
        val relDist = pDist - trackDistance
        if (relDist < 4f || relDist > 2000f) continue

        val normDist = (relDist / 2000f).coerceIn(0f, 1f)
        val scale = Math.pow((1f - normDist).toDouble(), 2.0).toFloat()

        val curveOffset = sin((pDist * 0.0028).toDouble()).toFloat() * (220f * curveIntensity)
        val roadCenterX = (width * 0.5f) + (curveOffset - (playerLane * width * 0.65f)) * scale
        val roadCenterY = horizonY + (roadScreenHeight * scale)
        val roadW = baseRoadWidth * scale

        val isLeft = (idx % 2 == 0)
        val poleX = if (isLeft) roadCenterX - roadW * 0.55f else roadCenterX + roadW * 0.55f
        val poleH = 75f * scale * 2.8f
        val poleTopY = roadCenterY - poleH

        drawLine(
            color = Color(0xFF5A6B88),
            start = Offset(poleX, roadCenterY),
            end = Offset(poleX, poleTopY),
            strokeWidth = (3.5f * scale * 2.2f).coerceAtLeast(1.5f)
        )

        val armLength = roadW * 0.18f
        val armEndX = if (isLeft) poleX + armLength else poleX - armLength
        drawLine(
            color = Color(0xFF5A6B88),
            start = Offset(poleX, poleTopY),
            end = Offset(armEndX, poleTopY - 8f * scale),
            strokeWidth = (2.5f * scale * 2.2f).coerceAtLeast(1.2f)
        )

        drawCircle(
            color = Color(0xFF00F0FF),
            radius = (4.5f * scale * 2.5f).coerceAtLeast(2f),
            center = Offset(armEndX, poleTopY - 6f * scale)
        )
    }
}

// -------------------------------------------------------------
// TRACK PICKUPS IN 3D DEPTH
// -------------------------------------------------------------
private fun DrawScope.drawTrackPickups(
    width: Float,
    height: Float,
    horizonY: Float,
    playerLane: Float,
    trackDistance: Float,
    pickups: List<TrackPickup>
) {
    val roadScreenHeight = height - horizonY
    val baseRoadWidth = width * 1.85f

    for (p in pickups) {
        if (p.isCollected) continue
        val deltaDist = p.distanceMeters - trackDistance
        if (deltaDist < 2f || deltaDist > 2500f) continue

        val normDist = (deltaDist / 2500f).coerceIn(0f, 1f)
        val scale = Math.pow((1f - normDist).toDouble(), 2.0).toFloat()

        val curveOffset = sin((p.distanceMeters * 0.0028).toDouble()).toFloat() * 220f
        val screenX = (width * 0.5f) + (curveOffset + (p.laneOffset - playerLane) * width * 0.65f) * scale
        val screenY = horizonY + (roadScreenHeight * scale) - (38f * scale * 2.4f)

        val sizePx = (46f * scale * 2.4f).coerceIn(10f, 95f)

        when (p.type) {
            PickupType.NITRO_CANISTER -> {
                drawCircle(
                    color = Color(0x6600F0FF),
                    radius = sizePx * 0.95f,
                    center = Offset(screenX, screenY)
                )
                drawRoundRect(
                    color = Color(0xFF00F0FF),
                    topLeft = Offset(screenX - sizePx * 0.32f, screenY - sizePx * 0.5f),
                    size = Size(sizePx * 0.64f, sizePx),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(sizePx * 0.2f)
                )
                drawRect(
                    color = Color(0xFFFF007F),
                    topLeft = Offset(screenX - sizePx * 0.2f, screenY - sizePx * 0.68f),
                    size = Size(sizePx * 0.4f, sizePx * 0.2f)
                )
            }
            PickupType.CREDIT_TOKEN -> {
                drawCircle(
                    color = Color(0x55FFD700),
                    radius = sizePx * 0.85f,
                    center = Offset(screenX, screenY)
                )
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = sizePx * 0.6f,
                    center = Offset(screenX, screenY)
                )
                drawCircle(
                    color = Color(0xFFFF9900),
                    radius = sizePx * 0.42f,
                    center = Offset(screenX, screenY)
                )
            }
            PickupType.SPEED_PAD -> {
                val arrowW = sizePx * 1.6f
                val arrowPath = Path().apply {
                    moveTo(screenX, screenY - sizePx * 0.7f)
                    lineTo(screenX + arrowW * 0.5f, screenY + sizePx * 0.4f)
                    lineTo(screenX, screenY)
                    lineTo(screenX - arrowW * 0.5f, screenY + sizePx * 0.4f)
                    close()
                }
                drawPath(arrowPath, color = Color(0xFF00FF66))
            }
        }
    }
}

// -------------------------------------------------------------
// 3D RIVAL CARS
// -------------------------------------------------------------
private fun DrawScope.draw3DRivalCars(
    width: Float,
    height: Float,
    horizonY: Float,
    playerLane: Float,
    playerDistance: Float,
    rivals: List<RivalCar>
) {
    val roadScreenHeight = height - horizonY
    val baseRoadWidth = width * 1.85f

    val sortedRivals = rivals.sortedByDescending { it.distanceMeters }

    for (rival in sortedRivals) {
        val relDist = rival.distanceMeters - playerDistance
        if (relDist < 2f || relDist > 2500f) continue

        val normDist = (relDist / 2500f).coerceIn(0f, 1f)
        val scale = Math.pow((1f - normDist).toDouble(), 2.0).toFloat()

        val curveOffset = sin((rival.distanceMeters * 0.0028).toDouble()).toFloat() * 220f
        val screenX = (width * 0.5f) + (curveOffset + (rival.laneOffset - playerLane) * width * 0.65f) * scale
        val screenY = horizonY + (roadScreenHeight * scale)

        val carW = (110f * scale * 2.4f).coerceIn(16f, 220f)
        val carH = carW * 0.48f

        rotate(degrees = rival.spinAngle, pivot = Offset(screenX, screenY - carH * 0.5f)) {
            drawOval(
                color = Color(0x99000000),
                topLeft = Offset(screenX - carW * 0.55f, screenY - carH * 0.1f),
                size = Size(carW * 1.1f, carH * 0.45f)
            )

            val tireW = carW * 0.17f
            val tireH = carH * 0.6f
            drawRoundRect(
                color = Color(0xFF10141D),
                topLeft = Offset(screenX - carW * 0.5f, screenY - tireH),
                size = Size(tireW, tireH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            drawRoundRect(
                color = Color(0xFF10141D),
                topLeft = Offset(screenX + carW * 0.5f - tireW, screenY - tireH),
                size = Size(tireW, tireH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )

            val bodyGradient = Brush.verticalGradient(
                colors = listOf(
                    rival.color,
                    rival.color.copy(red = rival.color.red * 0.65f, green = rival.color.green * 0.65f, blue = rival.color.blue * 0.65f)
                ),
                startY = screenY - carH,
                endY = screenY
            )

            val bodyPath = Path().apply {
                moveTo(screenX - carW * 0.45f, screenY - carH * 0.15f)
                lineTo(screenX + carW * 0.45f, screenY - carH * 0.15f)
                lineTo(screenX + carW * 0.42f, screenY - carH * 0.65f)
                lineTo(screenX + carW * 0.28f, screenY - carH)
                lineTo(screenX - carW * 0.28f, screenY - carH)
                lineTo(screenX - carW * 0.42f, screenY - carH * 0.65f)
                close()
            }
            drawPath(bodyPath, brush = bodyGradient)

            val cabinPath = Path().apply {
                moveTo(screenX - carW * 0.25f, screenY - carH * 0.95f)
                lineTo(screenX + carW * 0.25f, screenY - carH * 0.95f)
                lineTo(screenX + carW * 0.32f, screenY - carH * 0.68f)
                lineTo(screenX - carW * 0.32f, screenY - carH * 0.68f)
                close()
            }
            drawPath(cabinPath, color = Color(0xDD0D111A))

            val lightW = carW * 0.24f
            val lightH = carH * 0.13f
            drawRoundRect(
                color = Color(0xFFFF1744),
                topLeft = Offset(screenX - carW * 0.42f, screenY - carH * 0.52f),
                size = Size(lightW, lightH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
            )
            drawRoundRect(
                color = Color(0xFFFF1744),
                topLeft = Offset(screenX + carW * 0.18f, screenY - carH * 0.52f),
                size = Size(lightW, lightH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
            )

            drawRoundRect(
                color = Color(0xFF1E2430),
                topLeft = Offset(screenX - carW * 0.46f, screenY - carH * 1.15f),
                size = Size(carW * 0.92f, carH * 0.14f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
            )
        }

        if (rival.isKnockedDown) {
            val sparkCount = 8
            for (s in 0 until sparkCount) {
                val sparkAngle = (s * (360f / sparkCount) + rival.spinAngle) * (PI / 180.0)
                val sparkDist = carW * 0.8f
                val spX = screenX + (cos(sparkAngle) * sparkDist).toFloat()
                val spY = screenY - carH * 0.5f + (sin(sparkAngle) * sparkDist).toFloat()
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = (5f * scale * 2.5f).coerceAtLeast(2f),
                    center = Offset(spX, spY)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// MOTION WARP SPEED LINES
// -------------------------------------------------------------
private fun DrawScope.drawSpeedWarpLines(
    width: Float,
    height: Float,
    horizonY: Float,
    speedKmh: Float,
    nitroStage: NitroStage
) {
    val centerX = width * 0.5f
    val centerY = horizonY + 20f
    val lineCount = if (nitroStage == NitroStage.SHOCKWAVE) 34 else 18
    val strokeColor = when (nitroStage) {
        NitroStage.SHOCKWAVE -> Color(0x99FF007F)
        NitroStage.PERFECT -> Color(0x9900F0FF)
        NitroStage.STANDARD -> Color(0x77FFE500)
        NitroStage.NONE -> Color(0x33FFFFFF)
    }

    val timeSeed = (System.currentTimeMillis() % 1000) / 1000f

    for (i in 0 until lineCount) {
        val angle = (i * (360f / lineCount) + timeSeed * 45f) * (PI / 180.0)
        val innerR = width * 0.2f
        val outerR = width * 0.7f

        val startX = centerX + (cos(angle) * innerR).toFloat()
        val startY = centerY + (sin(angle) * innerR * 0.65f).toFloat()
        val endX = centerX + (cos(angle) * outerR).toFloat()
        val endY = centerY + (sin(angle) * outerR * 0.65f).toFloat()

        drawLine(
            color = strokeColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (nitroStage != NitroStage.NONE) 3.5f else 2f
        )
    }
}

// -------------------------------------------------------------
// HIGH-DEFINITION 3D PLAYER SUPERCAR
// -------------------------------------------------------------
private fun DrawScope.drawPlayerSupercar(
    width: Float,
    height: Float,
    playerCar: CarModel,
    playerSpeedKmh: Float,
    playerLane: Float,
    isDrifting: Boolean,
    driftAngle: Float,
    jumpHeight: Float,
    spinAngle: Float,
    nitroActive: Boolean,
    nitroStage: NitroStage
) {
    val carW = (width * 0.46f).coerceIn(210f, 380f)
    val carH = carW * 0.55f
    val carCenterX = width * 0.5f + (driftAngle * 1.8f)
    val carCenterY = height * 0.76f - jumpHeight

    val steerRoll = (driftAngle * 0.65f).coerceIn(-30f, 30f) + spinAngle

    rotate(degrees = steerRoll, pivot = Offset(carCenterX, carCenterY)) {
        // 1. NEON UNDERGLOW
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    playerCar.underglowColor.copy(alpha = if (nitroActive) 0.95f else 0.55f),
                    playerCar.underglowColor.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(carCenterX, carCenterY + carH * 0.28f),
                radius = carW * 0.85f
            ),
            topLeft = Offset(carCenterX - carW * 0.75f, carCenterY - carH * 0.12f),
            size = Size(carW * 1.5f, carH * 0.85f)
        )

        // 2. CONTACT PATCH GROUND SHADOW
        drawOval(
            color = Color(0xEE000000),
            topLeft = Offset(carCenterX - carW * 0.56f, carCenterY + carH * 0.12f),
            size = Size(carW * 1.12f, carH * 0.38f)
        )

        // 3. WIDE RACING WHEELS (With Visible Alloy Rims & Brembo Calipers)
        val tireW = carW * 0.20f
        val tireH = carH * 0.70f

        val leftTireX = carCenterX - carW * 0.54f
        val tireY = carCenterY - tireH * 0.48f
        drawRoundRect(
            color = Color(0xFF0C0F17),
            topLeft = Offset(leftTireX, tireY),
            size = Size(tireW, tireH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
        )
        drawCircle(
            color = Color(0xFF242C3D),
            radius = tireW * 0.42f,
            center = Offset(leftTireX + tireW * 0.5f, carCenterY + carH * 0.05f)
        )
        drawCircle(
            color = Color(0xFF7E8EA6),
            radius = tireW * 0.28f,
            center = Offset(leftTireX + tireW * 0.5f, carCenterY + carH * 0.05f)
        )
        drawRect(
            color = Color(0xFFFF1744),
            topLeft = Offset(leftTireX + tireW * 0.25f, carCenterY - tireH * 0.12f),
            size = Size(tireW * 0.35f, tireH * 0.18f)
        )

        val rightTireX = carCenterX + carW * 0.54f - tireW
        drawRoundRect(
            color = Color(0xFF0C0F17),
            topLeft = Offset(rightTireX, tireY),
            size = Size(tireW, tireH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
        )
        drawCircle(
            color = Color(0xFF242C3D),
            radius = tireW * 0.42f,
            center = Offset(rightTireX + tireW * 0.5f, carCenterY + carH * 0.05f)
        )
        drawCircle(
            color = Color(0xFF7E8EA6),
            radius = tireW * 0.28f,
            center = Offset(rightTireX + tireW * 0.5f, carCenterY + carH * 0.05f)
        )
        drawRect(
            color = Color(0xFFFF1744),
            topLeft = Offset(rightTireX + tireW * 0.4f, carCenterY - tireH * 0.12f),
            size = Size(tireW * 0.35f, tireH * 0.18f)
        )

        // 4. CARBON FIBER REAR DIFFUSER & VORTEX STRAKES
        val diffuserPath = Path().apply {
            moveTo(carCenterX - carW * 0.44f, carCenterY + carH * 0.18f)
            lineTo(carCenterX + carW * 0.44f, carCenterY + carH * 0.18f)
            lineTo(carCenterX + carW * 0.40f, carCenterY + carH * 0.32f)
            lineTo(carCenterX - carW * 0.40f, carCenterY + carH * 0.32f)
            close()
        }
        drawPath(diffuserPath, color = Color(0xFF11141D))

        for (f in -2..2) {
            val finX = carCenterX + f * (carW * 0.14f)
            drawRect(
                color = Color(0xFF00F0FF).copy(alpha = 0.5f),
                topLeft = Offset(finX - 1.5f, carCenterY + carH * 0.20f),
                size = Size(3f, carH * 0.12f)
            )
        }

        // 5. 3D SCULPTED MAIN CHASSIS
        val fenderGradient = Brush.verticalGradient(
            colors = listOf(
                playerCar.bodyColor.copy(alpha = 0.85f),
                playerCar.bodyColor.copy(red = playerCar.bodyColor.red * 0.7f, green = playerCar.bodyColor.green * 0.7f, blue = playerCar.bodyColor.blue * 0.7f)
            ),
            startY = carCenterY - carH * 0.4f,
            endY = carCenterY + carH * 0.18f
        )
        val fenderPath = Path().apply {
            moveTo(carCenterX - carW * 0.48f, carCenterY + carH * 0.18f)
            lineTo(carCenterX + carW * 0.48f, carCenterY + carH * 0.18f)
            lineTo(carCenterX + carW * 0.46f, carCenterY - carH * 0.38f)
            lineTo(carCenterX + carW * 0.36f, carCenterY - carH * 0.72f)
            lineTo(carCenterX - carW * 0.36f, carCenterY - carH * 0.72f)
            lineTo(carCenterX - carW * 0.46f, carCenterY - carH * 0.38f)
            close()
        }
        drawPath(fenderPath, brush = fenderGradient)

        val upperDeckGradient = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.4f),
                playerCar.bodyColor,
                playerCar.bodyColor.copy(red = playerCar.bodyColor.red * 0.5f, green = playerCar.bodyColor.green * 0.5f, blue = playerCar.bodyColor.blue * 0.5f)
            ),
            startY = carCenterY - carH * 0.85f,
            endY = carCenterY - carH * 0.1f
        )
        val upperDeckPath = Path().apply {
            moveTo(carCenterX - carW * 0.38f, carCenterY - carH * 0.20f)
            lineTo(carCenterX + carW * 0.38f, carCenterY - carH * 0.20f)
            lineTo(carCenterX + carW * 0.32f, carCenterY - carH * 0.78f)
            lineTo(carCenterX - carW * 0.32f, carCenterY - carH * 0.78f)
            close()
        }
        drawPath(upperDeckPath, brush = upperDeckGradient)

        // 6. 3D AERODYNAMIC SIDE MIRRORS
        val leftMirrorPath = Path().apply {
            moveTo(carCenterX - carW * 0.38f, carCenterY - carH * 0.60f)
            lineTo(carCenterX - carW * 0.52f, carCenterY - carH * 0.65f)
            lineTo(carCenterX - carW * 0.50f, carCenterY - carH * 0.55f)
            lineTo(carCenterX - carW * 0.38f, carCenterY - carH * 0.54f)
            close()
        }
        drawPath(leftMirrorPath, color = playerCar.bodyColor)
        drawCircle(
            color = Color(0xFF00F0FF),
            radius = 3.5f,
            center = Offset(carCenterX - carW * 0.50f, carCenterY - carH * 0.60f)
        )

        val rightMirrorPath = Path().apply {
            moveTo(carCenterX + carW * 0.38f, carCenterY - carH * 0.60f)
            lineTo(carCenterX + carW * 0.52f, carCenterY - carH * 0.65f)
            lineTo(carCenterX + carW * 0.50f, carCenterY - carH * 0.55f)
            lineTo(carCenterX + carW * 0.38f, carCenterY - carH * 0.54f)
            close()
        }
        drawPath(rightMirrorPath, color = playerCar.bodyColor)
        drawCircle(
            color = Color(0xFF00F0FF),
            radius = 3.5f,
            center = Offset(carCenterX + carW * 0.50f, carCenterY - carH * 0.60f)
        )

        // 7. TINTED FASTBACK REAR WINDOW & ROOF SCOOP
        val rearWindowPath = Path().apply {
            moveTo(carCenterX - carW * 0.25f, carCenterY - carH * 0.74f)
            lineTo(carCenterX + carW * 0.25f, carCenterY - carH * 0.74f)
            lineTo(carCenterX + carW * 0.32f, carCenterY - carH * 0.42f)
            lineTo(carCenterX - carW * 0.32f, carCenterY - carH * 0.42f)
            close()
        }
        drawPath(
            rearWindowPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0D121F), Color(0xFF1E283D)),
                startY = carCenterY - carH * 0.74f,
                endY = carCenterY - carH * 0.42f
            )
        )
        drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(carCenterX - carW * 0.18f, carCenterY - carH * 0.70f),
            end = Offset(carCenterX + carW * 0.15f, carCenterY - carH * 0.46f),
            strokeWidth = 3f
        )
        drawRoundRect(
            color = Color(0xFF111520),
            topLeft = Offset(carCenterX - carW * 0.08f, carCenterY - carH * 0.82f),
            size = Size(carW * 0.16f, carH * 0.08f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )

        // 8. 3D LED LIGHTBAR TAILLIGHTS
        val taillightColor = if (nitroActive) Color(0xFF00F0FF) else Color(0xFFFF113A)
        drawRoundRect(
            color = taillightColor.copy(alpha = 0.5f),
            topLeft = Offset(carCenterX - carW * 0.44f, carCenterY - carH * 0.28f),
            size = Size(carW * 0.88f, carH * 0.14f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(taillightColor, Color(0xFFFFF77A), taillightColor),
                startX = carCenterX - carW * 0.42f,
                endX = carCenterX + carW * 0.42f
            ),
            topLeft = Offset(carCenterX - carW * 0.42f, carCenterY - carH * 0.25f),
            size = Size(carW * 0.84f, carH * 0.085f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )

        // 9. MASSIVE CARBON FIBER GT SPOILER
        drawRect(
            color = Color(0xFF161C26),
            topLeft = Offset(carCenterX - carW * 0.22f, carCenterY - carH * 1.05f),
            size = Size(carW * 0.045f, carH * 0.32f)
        )
        drawRect(
            color = Color(0xFF161C26),
            topLeft = Offset(carCenterX + carW * 0.175f, carCenterY - carH * 1.05f),
            size = Size(carW * 0.045f, carH * 0.32f)
        )
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF384357), Color(0xFF121620)),
                startY = carCenterY - carH * 1.15f,
                endY = carCenterY - carH * 0.95f
            ),
            topLeft = Offset(carCenterX - carW * 0.52f, carCenterY - carH * 1.12f),
            size = Size(carW * 1.04f, carH * 0.13f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
        drawRoundRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(carCenterX - carW * 0.53f, carCenterY - carH * 1.20f),
            size = Size(carW * 0.035f, carH * 0.24f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
        )
        drawRoundRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(carCenterX + carW * 0.495f, carCenterY - carH * 1.20f),
            size = Size(carW * 0.035f, carH * 0.24f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
        )

        // 10. QUAD TITANIUM EXHAUST PIPES
        val exhaustY = carCenterY + carH * 0.16f
        val pipeRadius = carW * 0.052f

        val leftPipe1 = carCenterX - carW * 0.25f
        val leftPipe2 = carCenterX - carW * 0.15f
        drawCircle(color = Color(0xFF00F0FF), radius = pipeRadius * 1.15f, center = Offset(leftPipe1, exhaustY))
        drawCircle(color = Color(0xFF11141B), radius = pipeRadius, center = Offset(leftPipe1, exhaustY))
        drawCircle(color = Color(0xFF00F0FF), radius = pipeRadius * 1.15f, center = Offset(leftPipe2, exhaustY))
        drawCircle(color = Color(0xFF11141B), radius = pipeRadius, center = Offset(leftPipe2, exhaustY))

        val rightPipe1 = carCenterX + carW * 0.15f
        val rightPipe2 = carCenterX + carW * 0.25f
        drawCircle(color = Color(0xFF00F0FF), radius = pipeRadius * 1.15f, center = Offset(rightPipe1, exhaustY))
        drawCircle(color = Color(0xFF11141B), radius = pipeRadius, center = Offset(rightPipe1, exhaustY))
        drawCircle(color = Color(0xFF00F0FF), radius = pipeRadius * 1.15f, center = Offset(rightPipe2, exhaustY))
        drawCircle(color = Color(0xFF11141B), radius = pipeRadius, center = Offset(rightPipe2, exhaustY))

        // 11. NITRO JET FLAMES
        if (nitroActive) {
            val flameLength = when (nitroStage) {
                NitroStage.SHOCKWAVE -> carH * 1.8f
                NitroStage.PERFECT -> carH * 1.4f
                NitroStage.STANDARD -> carH * 1.0f
                NitroStage.NONE -> carH * 0.7f
            }
            val flameColors = when (nitroStage) {
                NitroStage.SHOCKWAVE -> listOf(Color.White, Color(0xFFFF007F), Color(0xFF9D00FF), Color.Transparent)
                NitroStage.PERFECT -> listOf(Color.White, Color(0xFF00F0FF), Color(0xFF0066FF), Color.Transparent)
                NitroStage.STANDARD -> listOf(Color.White, Color(0xFFFFD600), Color(0xFFFF4500), Color.Transparent)
                NitroStage.NONE -> listOf(Color.White, Color(0xFFFF6600), Color.Transparent)
            }

            val leftFlame = Path().apply {
                moveTo(carCenterX - carW * 0.26f, exhaustY)
                lineTo(carCenterX - carW * 0.14f, exhaustY)
                lineTo(carCenterX - carW * 0.20f, exhaustY + flameLength)
                close()
            }
            drawPath(
                leftFlame,
                brush = Brush.verticalGradient(flameColors, startY = exhaustY, endY = exhaustY + flameLength)
            )

            val rightFlame = Path().apply {
                moveTo(carCenterX + carW * 0.14f, exhaustY)
                lineTo(carCenterX + carW * 0.26f, exhaustY)
                lineTo(carCenterX + carW * 0.20f, exhaustY + flameLength)
                close()
            }
            drawPath(
                rightFlame,
                brush = Brush.verticalGradient(flameColors, startY = exhaustY, endY = exhaustY + flameLength)
            )
        }

        // 12. DRIFT TIRE SMOKE & SPARK PARTICLES
        if (isDrifting && playerSpeedKmh > 75f) {
            val smokePuffs = 7
            for (s in 0 until smokePuffs) {
                val puffX = carCenterX + (if (driftAngle > 0) -carW * 0.48f else carW * 0.48f) + (s * 9f - 27f)
                val puffY = carCenterY + carH * 0.18f + (s * 7f)
                val puffRadius = 16f + s * 5f

                drawCircle(
                    color = Color(0x44CCCCCC),
                    radius = puffRadius,
                    center = Offset(puffX, puffY)
                )

                if (s % 2 == 0) {
                    drawCircle(
                        color = Color(0xFFFF9900),
                        radius = 4.5f,
                        center = Offset(puffX + (Math.random() * 24 - 12).toFloat(), puffY + (Math.random() * 12).toFloat())
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// FIRST-PERSON COCKPIT DASHBOARD VIEW (FPS CAMERA)
// -------------------------------------------------------------
private fun DrawScope.drawDashboardCockpitView(
    width: Float,
    height: Float,
    playerCar: CarModel,
    playerSpeedKmh: Float,
    steerInput: Float,
    nitroActive: Boolean,
    nitroStage: NitroStage,
    weatherCondition: WeatherCondition = WeatherCondition.CLEAR,
    trackDistance: Float = 0f
) {
    val dashY = height * 0.68f
    val dashHeight = height - dashY

    // 1. Aerodynamic Hood Nose Visible Through Windshield
    val hoodPath = Path().apply {
        moveTo(width * 0.12f, height)
        lineTo(width * 0.32f, dashY + 12f)
        lineTo(width * 0.50f, dashY + 4f)
        lineTo(width * 0.68f, dashY + 12f)
        lineTo(width * 0.88f, height)
        close()
    }
    val hoodBrush = Brush.verticalGradient(
        colors = listOf(
            playerCar.bodyColor.copy(alpha = 0.9f),
            playerCar.bodyColor.copy(
                red = playerCar.bodyColor.red * 0.6f,
                green = playerCar.bodyColor.green * 0.6f,
                blue = playerCar.bodyColor.blue * 0.6f
            )
        ),
        startY = dashY,
        endY = height
    )
    drawPath(hoodPath, brush = hoodBrush)

    // Center hood aerodynamic crease line
    drawLine(
        color = Color.White.copy(alpha = 0.35f),
        start = Offset(width * 0.5f, dashY + 4f),
        end = Offset(width * 0.5f, dashY + 65f),
        strokeWidth = 2.5f
    )

    // 2. Leather & Carbon Fiber Dashboard Body
    val dashPath = Path().apply {
        moveTo(0f, dashY)
        lineTo(width * 0.22f, dashY - 18f)
        lineTo(width * 0.50f, dashY - 10f)
        lineTo(width * 0.78f, dashY - 18f)
        lineTo(width, dashY)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    val dashBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1B2230),
            Color(0xFF101522),
            Color(0xFF080B12)
        ),
        startY = dashY - 20f,
        endY = height
    )
    drawPath(dashPath, brush = dashBrush)

    // Top Dash Leather Stitching Line & Ambient LED Lightstrip
    val stitchPath = Path().apply {
        moveTo(width * 0.04f, dashY - 2f)
        lineTo(width * 0.22f, dashY - 16f)
        lineTo(width * 0.50f, dashY - 8f)
        lineTo(width * 0.78f, dashY - 16f)
        lineTo(width * 0.96f, dashY - 2f)
    }
    val ledGlowColor = if (nitroActive) Color(0xFFFF007F) else playerCar.underglowColor
    drawPath(
        stitchPath,
        color = ledGlowColor.copy(alpha = 0.6f),
        style = Stroke(width = 6f)
    )
    drawPath(
        stitchPath,
        color = ledGlowColor,
        style = Stroke(width = 2.5f)
    )

    // Air Condition Vents (Left, Center, Right)
    val ventY = dashY + 16f
    for (vX in listOf(width * 0.15f, width * 0.50f, width * 0.85f)) {
        drawRoundRect(
            color = Color(0xFF090D14),
            topLeft = Offset(vX - 28f, ventY),
            size = Size(56f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(vX - 28f, ventY),
            size = Size(56f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f),
            style = Stroke(width = 1.5f)
        )
        // Louvers
        for (l in 1..3) {
            drawLine(
                color = Color(0xFF475569),
                start = Offset(vX - 24f, ventY + l * 5.5f),
                end = Offset(vX + 24f, ventY + l * 5.5f),
                strokeWidth = 1.2f
            )
        }
    }

    // 3. Digital Instrument Display Screen (Behind Steering Wheel)
    val clusterW = width * 0.32f
    val clusterH = dashHeight * 0.58f
    val clusterX = width * 0.34f
    val clusterY = dashY + 22f

    drawRoundRect(
        color = Color(0xFF06090F),
        topLeft = Offset(clusterX, clusterY),
        size = Size(clusterW, clusterH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
    )
    drawRoundRect(
        color = if (nitroActive) Color(0xFFFF007F) else Color(0xFF00F0FF),
        topLeft = Offset(clusterX, clusterY),
        size = Size(clusterW, clusterH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f),
        style = Stroke(width = 2f)
    )

    // Curved RPM Tachometer Arc
    val arcCx = clusterX + clusterW * 0.5f
    val arcCy = clusterY + clusterH * 0.65f
    val arcR = clusterW * 0.38f
    val revPercent = (playerSpeedKmh / playerCar.currentTopSpeed).coerceIn(0f, 1f)

    drawArc(
        color = Color(0xFF1E293B),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(arcCx - arcR, arcCy - arcR),
        size = Size(arcR * 2f, arcR * 2f),
        style = Stroke(width = 8f)
    )
    drawArc(
        brush = Brush.horizontalGradient(
            listOf(Color(0xFF00FF66), Color(0xFFFFD700), Color(0xFFFF1744))
        ),
        startAngle = 180f,
        sweepAngle = 180f * revPercent,
        useCenter = false,
        topLeft = Offset(arcCx - arcR, arcCy - arcR),
        size = Size(arcR * 2f, arcR * 2f),
        style = Stroke(width = 8f)
    )

    // Shift LED Lights across cluster top
    val shiftLights = 8
    val lightSpacing = clusterW * 0.8f / shiftLights
    for (s in 0 until shiftLights) {
        val lx = clusterX + clusterW * 0.1f + s * lightSpacing
        val isLit = (s.toFloat() / shiftLights) <= revPercent
        val lColor = when {
            !isLit -> Color(0xFF1E293B)
            s < 4 -> Color(0xFF00FF66)
            s < 6 -> Color(0xFFFFD700)
            else -> Color(0xFFFF1744)
        }
        drawCircle(color = lColor, radius = 3.5f, center = Offset(lx, clusterY + 12f))
    }

    // 4. 3D Performance Steering Wheel (Rotates with Steer Input)
    val wheelCx = width * 0.38f
    val wheelCy = dashY + dashHeight * 0.65f
    val wheelRadius = width * 0.16f
    val wheelRoll = (steerInput * 1.5f).coerceIn(-45f, 45f)

    rotate(degrees = wheelRoll, pivot = Offset(wheelCx, wheelCy)) {
        // Paddle Shifters behind wheel
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(wheelCx - wheelRadius * 1.15f, wheelCy - wheelRadius * 0.4f),
            size = Size(wheelRadius * 0.25f, wheelRadius * 0.8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(wheelCx + wheelRadius * 0.90f, wheelCy - wheelRadius * 0.4f),
            size = Size(wheelRadius * 0.25f, wheelRadius * 0.8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )

        // Steering Wheel Outer Rim (Leather & Alcantara)
        drawCircle(
            color = Color(0xFF151B26),
            radius = wheelRadius,
            center = Offset(wheelCx, wheelCy),
            style = Stroke(width = 24f)
        )
        // Red Center Marker Top Stripe
        drawArc(
            color = Color(0xFFFF1744),
            startAngle = 262f,
            sweepAngle = 16f,
            useCenter = false,
            topLeft = Offset(wheelCx - wheelRadius, wheelCy - wheelRadius),
            size = Size(wheelRadius * 2f, wheelRadius * 2f),
            style = Stroke(width = 26f)
        )

        // Center Hub
        drawCircle(
            color = Color(0xFF0A0E17),
            radius = wheelRadius * 0.38f,
            center = Offset(wheelCx, wheelCy)
        )
        drawCircle(
            color = if (nitroActive) Color(0xFFFF007F) else playerCar.bodyColor,
            radius = wheelRadius * 0.22f,
            center = Offset(wheelCx, wheelCy)
        )
        // Supercar Emblem
        drawCircle(
            color = Color.White,
            radius = wheelRadius * 0.08f,
            center = Offset(wheelCx, wheelCy)
        )

        // Steering Spokes (Left, Right, Bottom)
        drawLine(
            color = Color(0xFF273248),
            start = Offset(wheelCx, wheelCy),
            end = Offset(wheelCx - wheelRadius * 0.88f, wheelCy - wheelRadius * 0.1f),
            strokeWidth = 14f
        )
        drawLine(
            color = Color(0xFF273248),
            start = Offset(wheelCx, wheelCy),
            end = Offset(wheelCx + wheelRadius * 0.88f, wheelCy - wheelRadius * 0.1f),
            strokeWidth = 14f
        )
        drawLine(
            color = Color(0xFF273248),
            start = Offset(wheelCx, wheelCy),
            end = Offset(wheelCx, wheelCy + wheelRadius * 0.88f),
            strokeWidth = 14f
        )

        // Multifunction Buttons on Spokes
        drawCircle(color = Color(0xFF00F0FF), radius = 4f, center = Offset(wheelCx - wheelRadius * 0.5f, wheelCy - wheelRadius * 0.08f))
        drawCircle(color = Color(0xFFFF007F), radius = 4f, center = Offset(wheelCx + wheelRadius * 0.5f, wheelCy - wheelRadius * 0.08f))
    }

    // 5. Windshield Frame (A-Pillars & Top Tint Bar)
    val pillarW = width * 0.08f
    val leftPillar = Path().apply {
        moveTo(0f, 0f)
        lineTo(pillarW, 0f)
        lineTo(width * 0.16f, dashY + 10f)
        lineTo(0f, dashY + 10f)
        close()
    }
    drawPath(leftPillar, color = Color(0xFF0B0E14))

    val rightPillar = Path().apply {
        moveTo(width, 0f)
        lineTo(width - pillarW, 0f)
        lineTo(width * 0.84f, dashY + 10f)
        lineTo(width, dashY + 10f)
        close()
    }
    drawPath(rightPillar, color = Color(0xFF0B0E14))

    // Windshield Top Sunshade Tint Bar
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xEE080C14), Color(0x33080C14), Color.Transparent),
            startY = 0f,
            endY = height * 0.15f
        ),
        topLeft = Offset.Zero,
        size = Size(width, height * 0.15f)
    )

    // Center Interior Rearview Mirror hanging from ceiling
    val mirrorW = width * 0.22f
    val mirrorH = height * 0.065f
    val mirrorX = width * 0.5f - mirrorW * 0.5f
    val mirrorY = height * 0.04f

    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(width * 0.5f, 0f),
        end = Offset(width * 0.5f, mirrorY),
        strokeWidth = 6f
    )
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(mirrorX, mirrorY),
        size = Size(mirrorW, mirrorH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(
            listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF1E293B))
        ),
        topLeft = Offset(mirrorX + 3f, mirrorY + 3f),
        size = Size(mirrorW - 6f, mirrorH - 6f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
    )

    // 6. Left & Right Exterior Side Mirrors
    val sideMirrorW = width * 0.12f
    val sideMirrorH = height * 0.08f

    // Left Side Mirror
    drawRoundRect(
        color = Color(0xFF0B0E14),
        topLeft = Offset(width * 0.02f, dashY - sideMirrorH * 1.1f),
        size = Size(sideMirrorW, sideMirrorH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF475569))),
        topLeft = Offset(width * 0.02f + 2f, dashY - sideMirrorH * 1.1f + 2f),
        size = Size(sideMirrorW - 4f, sideMirrorH - 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )

    // Right Side Mirror
    drawRoundRect(
        color = Color(0xFF0B0E14),
        topLeft = Offset(width * 0.86f, dashY - sideMirrorH * 1.1f),
        size = Size(sideMirrorW, sideMirrorH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFF475569), Color(0xFF1E293B))),
        topLeft = Offset(width * 0.86f + 2f, dashY - sideMirrorH * 1.1f + 2f),
        size = Size(sideMirrorW - 4f, sideMirrorH - 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )

    // 7. Windshield Rain Droplets & Wiper Sweep in Raining Cockpit View
    if (weatherCondition.rainIntensity > 0.05f) {
        val numDrops = (35 * weatherCondition.rainIntensity).toInt()
        val wiperAngle = sin((trackDistance * 0.15f).toDouble()).toFloat() * 35f
        for (d in 0 until numDrops) {
            val dropX = ((d * 97 + trackDistance * 0.05f) % (width * 0.8f)) + width * 0.1f
            val dropY = ((d * 53 + trackDistance * 0.8f) % (dashY * 0.85f)) + height * 0.1f
            drawCircle(
                color = Color(0xAA88E5FF),
                radius = 2.2f + (d % 3),
                center = Offset(dropX.toFloat(), dropY.toFloat())
            )
            drawLine(
                color = Color(0x4488E5FF),
                start = Offset(dropX.toFloat(), dropY.toFloat()),
                end = Offset(dropX.toFloat() - 3f, dropY.toFloat() + 12f),
                strokeWidth = 1.2f
            )
        }

        // Windshield Wiper Arm
        val wiperPivotX = width * 0.5f
        val wiperPivotY = dashY + 5f
        val wiperLength = width * 0.38f
        val rad = (wiperAngle - 90f) * (PI.toFloat() / 180f)
        val wiperTipX = wiperPivotX + wiperLength * cos(rad)
        val wiperTipY = wiperPivotY + wiperLength * sin(rad)
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(wiperPivotX, wiperPivotY),
            end = Offset(wiperTipX, wiperTipY),
            strokeWidth = 6f
        )
    }
}

// -------------------------------------------------------------
// VOLUMETRIC FOG OVERLAY
// -------------------------------------------------------------
private fun DrawScope.drawVolumetricFogOverlay(
    width: Float,
    height: Float,
    horizonY: Float,
    weatherCondition: WeatherCondition
) {
    val fogDensity = weatherCondition.fogAlpha
    val fogBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xEEBAC8DA).copy(alpha = fogDensity * 0.85f),
            Color(0xCC8FA3BB).copy(alpha = fogDensity * 0.95f),
            Color(0x88687D96).copy(alpha = fogDensity * 0.65f),
            Color.Transparent
        ),
        startY = 0f,
        endY = horizonY + (height - horizonY) * 0.65f
    )
    drawRect(
        brush = fogBrush,
        topLeft = Offset.Zero,
        size = Size(width, horizonY + (height - horizonY) * 0.65f)
    )

    // Dynamic drifting mist bands across lower track
    val numMistBands = 4
    for (m in 0 until numMistBands) {
        val mistY = horizonY + (m * 45f)
        val mistBrush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x66CFD8DC).copy(alpha = fogDensity * 0.5f),
                Color(0xAAECEFF1).copy(alpha = fogDensity * 0.7f),
                Color.Transparent
            )
        )
        drawRect(
            brush = mistBrush,
            topLeft = Offset(0f, mistY),
            size = Size(width, 30f)
        )
    }
}

// -------------------------------------------------------------
// ANIMATED RAIN PARTICLES & TIRE WATER SPRAY
// -------------------------------------------------------------
private fun DrawScope.drawRainEffect(
    width: Float,
    height: Float,
    weatherCondition: WeatherCondition,
    trackDistance: Float,
    playerSpeedKmh: Float,
    playerLane: Float
) {
    val intensity = weatherCondition.rainIntensity
    val numRainDrops = (80 * intensity).toInt()

    // 1. Diagonal falling rain streaks
    for (r in 0 until numRainDrops) {
        val seedX = (r * 137.3f + trackDistance * 1.8f) % (width + 100f) - 50f
        val seedY = (r * 73.1f + trackDistance * 4.2f) % (height + 60f) - 30f
        val streakLen = 22f + (r % 5) * 8f + (playerSpeedKmh * 0.08f)

        val rainAlpha = (0.35f + (r % 4) * 0.12f * intensity).coerceIn(0.2f, 0.85f)
        val rainColor = if (r % 3 == 0) Color(0xAA00F0FF) else Color(0xAACCEEFF)

        drawLine(
            color = rainColor.copy(alpha = rainAlpha),
            start = Offset(seedX, seedY),
            end = Offset(seedX - 8f, seedY + streakLen),
            strokeWidth = 1.6f + (r % 2) * 0.8f
        )
    }

    // 2. Rear tire water spray / mist cloud when driving on wet asphalt
    if (playerSpeedKmh > 50f && intensity > 0.2f) {
        val sprayCenterX = width * 0.5f - (playerLane * width * 0.35f)
        val sprayBaseY = height * 0.82f
        val numSprayParticles = (18 * intensity).toInt()

        for (p in 0 until numSprayParticles) {
            val pOffset = (p * 23.5f + trackDistance * 2f) % 60f
            val pX = sprayCenterX + ((p % 5 - 2) * 22f) + (Math.random() * 12 - 6).toFloat()
            val pY = sprayBaseY - pOffset
            val pRadius = 10f + (pOffset * 0.45f)
            val pAlpha = ((1.0f - pOffset / 60f) * 0.40f * intensity).coerceIn(0f, 0.40f)

            drawCircle(
                color = Color(0x88B0E2FF).copy(alpha = pAlpha),
                radius = pRadius,
                center = Offset(pX, pY)
            )
        }
    }
}
