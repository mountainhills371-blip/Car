package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
fun RaceSummaryScreen(
    modifier: Modifier = Modifier,
    telemetry: RaceTelemetry,
    earnedCredits: Int,
    onRaceAgain: () -> Unit,
    onOpenGarage: () -> Unit,
    onConsultAiDebrief: () -> Unit,
    onWatchReplay: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCarbon.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkCard)
                .border(2.dp, if (telemetry.position == 1) ElectricGold else NeonCyan, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trophy or Placement Title
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Podium",
                tint = when (telemetry.position) {
                    1 -> ElectricGold
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> NeonCyan
                },
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (telemetry.position) {
                    1 -> "VICTORY! 1ST PLACE"
                    2 -> "2ND PLACE PODIUM"
                    3 -> "3RD PLACE PODIUM"
                    else -> "${telemetry.position}TH PLACE FINISH"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "${telemetry.trackName} • ${telemetry.carName}",
                fontSize = 12.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // REWARDS BADGE
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF261D07))
                    .border(1.dp, ElectricGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "REWARD: +🪙 $earnedCredits CREDITS", fontSize = 15.sp, fontWeight = FontWeight.Black, color = ElectricGold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // STATS GRID
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10141F)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val minutes = (telemetry.finishTimeSeconds / 60).toInt()
                    val seconds = (telemetry.finishTimeSeconds % 60).toInt()
                    val millis = ((telemetry.finishTimeSeconds * 100) % 100).toInt()

                    StatLine("FINISH TIME", String.format("%02d:%02d.%02d", minutes, seconds, millis), NeonCyan)
                    StatLine("TOP VELOCITY", "${telemetry.topSpeedAchieved.toInt()} KM/H", NitroMagenta)
                    StatLine("AVG SPEED", "${telemetry.averageSpeed.toInt()} KM/H", Color.White)
                    StatLine("DRIFT DURATION", "${String.format("%.1f", telemetry.totalDriftSeconds)}s", ElectricGold)
                    StatLine("TAKEDOWNS", "${telemetry.knockdownsTriggered} RIVALS", SpeedGreen)
                    StatLine("SHOCKWAVES", "${telemetry.shockwavesUsed} FIRED", Color(0xFF9D00FF))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACTION BUTTONS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRaceAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = SpeedGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("summary_race_again_button")
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = "Retry", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "RACE AGAIN", fontWeight = FontWeight.Black)
                }

                if (onWatchReplay != null) {
                    Button(
                        onClick = onWatchReplay,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26183B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NitroMagenta),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("summary_watch_replay_button")
                    ) {
                        Text(text = "🎬 WATCH INSTANT REPLAY (30S)", fontWeight = FontWeight.Black, color = NitroMagenta)
                    }
                }

                Button(
                    onClick = onConsultAiDebrief,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCarbon),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("summary_ai_debrief_button")
                ) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI Debrief", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "APEX-9 AI TELEMETRY DEBRIEF", fontWeight = FontWeight.Bold, color = NeonCyan)
                }

                Button(
                    onClick = onOpenGarage,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("summary_garage_button")
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = "Garage", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "RETURN TO GARAGE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 13.sp, color = color, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
    }
}
