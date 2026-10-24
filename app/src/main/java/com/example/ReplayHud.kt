package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DarkCard
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroMagenta
import com.example.ui.theme.SpeedGreen

@Composable
fun ReplayHud(
    modifier: Modifier = Modifier,
    totalFrames: Int,
    currentFrameIndex: Int,
    isPlaying: Boolean,
    playbackSpeed: Float,
    selectedCameraAngle: CinematicCameraAngle,
    currentFrameData: ReplayFrame?,
    onSeekToFrame: (Int) -> Unit,
    onTogglePlayPause: () -> Unit,
    onChangeSpeed: (Float) -> Unit,
    onSelectCameraAngle: (CinematicCameraAngle) -> Unit,
    onExitReplay: () -> Unit
) {
    val totalSeconds = (totalFrames * 0.033f).coerceAtLeast(0.1f)
    val currentSeconds = (currentFrameIndex * 0.033f).coerceAtMost(totalSeconds)

    Box(modifier = modifier.fillMaxSize()) {
        // -------------------------------------------------------------
        // TOP REPLAY HEADER BADGE
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(DarkCarbon.copy(alpha = 0.9f), Color(0xFF1E2838))
                        )
                    )
                    .border(1.5.dp, NitroMagenta, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = NitroMagenta,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "🎬 INSTANT REPLAY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = selectedCameraAngle.description.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            // Speed & Telemetry Snapshot Badge
            if (currentFrameData != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCarbon.copy(alpha = 0.85f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${currentFrameData.playerSpeedKmh.toInt()} KM/H",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (currentFrameData.nitroActive) NitroMagenta else Color.White
                    )
                    if (currentFrameData.isDrifting) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DRIFTING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricGold
                        )
                    }
                }
            }

            // Exit Replay Button
            IconButton(
                onClick = onExitReplay,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkCarbon.copy(alpha = 0.9f))
                    .border(1.5.dp, Color.LightGray, CircleShape)
                    .testTag("replay_exit_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Replay",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // -------------------------------------------------------------
        // BOTTOM REPLAY CONTROL PANEL & SCRUBBER
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DarkCarbon.copy(alpha = 0.95f), DarkCarbon)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CINEMATIC CAMERA ANGLE SELECTOR PILLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CAMERA:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.LightGray,
                    modifier = Modifier.padding(end = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CinematicCameraAngle.values()) { angle ->
                        val isSelected = angle == selectedCameraAngle
                        val tag = when (angle) {
                            CinematicCameraAngle.CHASE -> "replay_cam_chase"
                            CinematicCameraAngle.DRONE_HOVER -> "replay_cam_drone"
                            CinematicCameraAngle.WHEEL_SIDE -> "replay_cam_wheel"
                            CinematicCameraAngle.BUMPER_FRONT -> "replay_cam_bumper"
                            CinematicCameraAngle.BROADCAST_TV -> "replay_cam_broadcast"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NitroMagenta else DarkCard)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) ElectricGold else Color(0xFF334155),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectCameraAngle(angle) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag(tag)
                        ) {
                            Text(
                                text = angle.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.LightGray
                            )
                        }
                    }
                }
            }

            // TIMELINE SCRUBBER SLIDER & TIME INDICATOR
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("00:%04.1fs", currentSeconds),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                    Text(
                        text = String.format("00:%04.1fs", totalSeconds),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                }

                Slider(
                    value = currentFrameIndex.toFloat(),
                    onValueChange = { onSeekToFrame(it.toInt()) },
                    valueRange = 0f..(totalFrames.coerceAtLeast(1) - 1).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NitroMagenta,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("replay_timeline_slider")
                )
            }

            // CONTROLS ROW: PLAY/PAUSE + PLAYBACK SPEED TOGGLES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SpeedGreen)
                        .clickable { onTogglePlayPause() }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("replay_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "PAUSE" else "PLAY",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                // Playback Speed Toggle Pills (0.5x, 1.0x, 2.0x)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPEED:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    listOf(0.5f to "0.5x", 1.0f to "1.0x", 2.0f to "2.0x").forEach { (speed, label) ->
                        val isSelected = playbackSpeed == speed
                        val tag = when (speed) {
                            0.5f -> "replay_speed_05x"
                            1.0f -> "replay_speed_10x"
                            else -> "replay_speed_20x"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan else Color(0xFF1E293B))
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onChangeSpeed(speed) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag(tag)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.Black else Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}
