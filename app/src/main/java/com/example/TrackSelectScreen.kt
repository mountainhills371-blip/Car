package com.example

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
fun TrackSelectScreen(
    modifier: Modifier = Modifier,
    tracks: List<TrackData>,
    selectedTrack: TrackData,
    selectedCar: CarModel,
    onSelectTrack: (TrackData) -> Unit,
    onSelectGameMode: (GameMode) -> Unit,
    currentGameMode: GameMode,
    onStartRace: () -> Unit,
    onBack: () -> Unit
) {
    var mode by remember { mutableStateOf(currentGameMode) }

    Box(modifier = modifier.fillMaxSize().background(DarkCarbon)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(DarkCard, CircleShape).testTag("track_select_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CHAMPIONSHIP CIRCUIT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Selected: ${selectedCar.name} (${selectedCar.carClass})",
                            fontSize = 12.sp,
                            color = NeonCyan
                        )
                    }
                }
            }

            // Game Mode Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (gm in GameMode.entries) {
                    val isSelected = mode == gm
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NitroMagenta else DarkCard)
                            .clickable {
                                mode = gm
                                onSelectGameMode(gm)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (gm) {
                                GameMode.CLASSIC_RACE -> "CLASSIC"
                                GameMode.ELIMINATION -> "ELIMINATION"
                                GameMode.TIME_TRIAL -> "TIME ATTACK"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color.White else Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Track List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(tracks) { track ->
                    val isSelected = track.id == selectedTrack.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTrack(track) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NeonCyan else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF192233) else DarkCard
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Location",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = track.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = track.difficulty.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when (track.difficulty) {
                                        "Rookie" -> SpeedGreen
                                        "Pro" -> ElectricGold
                                        else -> NitroMagenta
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = track.location,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏁 ${track.laps} LAPS (${(track.totalLengthMeters / 1000f)} KM)",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = "🏆 PRIZE: 🪙 ${track.rewardCredits}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricGold
                                )
                            }

                            // Weather Condition Selector Pill Bar
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0F1522))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "DYNAMIC TRACK WEATHER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NeonCyan,
                                            letterSpacing = 0.8.sp
                                        )
                                        Text(
                                            text = "${track.weatherCondition.icon} ${track.weatherCondition.displayName}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = track.weatherCondition.badgeColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        WeatherCondition.entries.forEach { wCond ->
                                            val isWSelected = track.weatherCondition == wCond
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isWSelected) wCond.badgeColor.copy(alpha = 0.25f) else Color(0xFF1A2333))
                                                    .border(
                                                        width = if (isWSelected) 1.5.dp else 0.5.dp,
                                                        color = if (isWSelected) wCond.badgeColor else Color.Gray.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        track.weatherCondition = wCond
                                                        onSelectTrack(track.copy(weatherCondition = wCond))
                                                    }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = wCond.icon, fontSize = 12.sp)
                                                    Text(
                                                        text = when (wCond) {
                                                            WeatherCondition.CLEAR -> "CLEAR"
                                                            WeatherCondition.LIGHT_RAIN -> "RAIN"
                                                            WeatherCondition.HEAVY_RAIN -> "STORM"
                                                            WeatherCondition.THICK_FOG -> "FOG"
                                                        },
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (isWSelected) Color.White else Color.LightGray
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${track.weatherCondition.description} • Grip: ${(track.weatherCondition.gripMultiplier * 100).toInt()}% • Top Speed: ${(track.weatherCondition.topSpeedMultiplier * 100).toInt()}%",
                                        fontSize = 9.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Start Race Button
            Button(
                onClick = onStartRace,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpeedGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp)
                    .testTag("launch_race_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START RACE ON ${selectedTrack.name.uppercase()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
