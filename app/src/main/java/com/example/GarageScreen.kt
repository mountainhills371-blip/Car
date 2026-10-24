package com.example

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DarkCard
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroMagenta
import com.example.ui.theme.NitroPurple
import com.example.ui.theme.SpeedGreen

@Composable
fun GarageScreen(
    modifier: Modifier = Modifier,
    carsList: List<CarModel>,
    selectedCarIndex: Int,
    playerCredits: Int,
    onSelectCarIndex: (Int) -> Unit,
    onBuyCar: (CarModel) -> Unit,
    onUpgradeEngine: (CarModel) -> Unit,
    onUpgradeNitro: (CarModel) -> Unit,
    onUpgradeHandling: (CarModel) -> Unit,
    onChangeColor: (CarModel, Color) -> Unit,
    onChangeUnderglow: (CarModel, Color) -> Unit,
    onStartRace: () -> Unit,
    onOpenAiStrategist: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val currentCar = carsList.getOrNull(selectedCarIndex) ?: carsList.first()
    var activeTab by remember { mutableIntStateOf(0) } // 0: Upgrades, 1: Paint Booth

    val availableColors = listOf(
        Color(0xFFFF1744) to "Crimson Red",
        Color(0xFF00F0FF) to "Cyber Cyan",
        Color(0xFFFFD700) to "Solar Gold",
        Color(0xFF9D00FF) to "Neon Violet",
        Color(0xFF1E2430) to "Carbon Shadow",
        Color(0xFF00FF66) to "Acid Green",
        Color(0xFFFFFFFF) to "Alpine White"
    )

    val availableUnderglows = listOf(
        Color(0xFF00F0FF) to "Cyan Glow",
        Color(0xFFFF007F) to "Magenta Glow",
        Color(0xFF9D00FF) to "Purple Glow",
        Color(0xFFFFD700) to "Gold Glow",
        Color(0xFF00FF66) to "Green Glow"
    )

    Box(modifier = modifier.fillMaxSize().background(DarkCarbon)) {
        // Showroom Backdrop
        Image(
            painter = painterResource(id = R.drawable.bg_garage),
            contentDescription = "Garage Showroom",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.35f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToMenu,
                        modifier = Modifier
                            .size(40.dp)
                            .background(DarkCard, CircleShape)
                            .testTag("garage_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "HYPER GARAGE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                // Credits Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1F1A07))
                        .border(1.dp, ElectricGold, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = "🪙", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$playerCredits",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricGold
                    )
                }
            }

            // CAR SELECTOR CAROUSEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newIdx = if (selectedCarIndex > 0) selectedCarIndex - 1 else carsList.size - 1
                        onSelectCarIndex(newIdx)
                    },
                    modifier = Modifier.background(DarkCard, CircleShape).testTag("prev_car_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Car",
                        tint = NeonCyan
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentCar.carClass.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = NitroMagenta,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = currentCar.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = currentCar.manufacturer,
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                }

                IconButton(
                    onClick = {
                        val newIdx = if (selectedCarIndex < carsList.size - 1) selectedCarIndex + 1 else 0
                        onSelectCarIndex(newIdx)
                    },
                    modifier = Modifier.background(DarkCard, CircleShape).testTag("next_car_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Car",
                        tint = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D CAR VISUAL DISPLAY PODIUM
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF1E283C), Color(0xFF0C1019))
                        )
                    )
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Underglow glow
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(14.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, currentCar.underglowColor, Color.Transparent)
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Car silhouette icon preview with current body color
                    Box(
                        modifier = Modifier
                            .size(130.dp, 70.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(currentCar.bodyColor)
                            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentCar.name.take(12),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lock or Unlocked Badge
                    if (!currentCar.isUnlocked) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = ElectricGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LOCKED - ${currentCar.priceCredits} CREDITS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricGold
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Unlocked", tint = SpeedGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OWNED & TUNED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpeedGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CAR SPECIFICATIONS CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PERFORMANCE SPECS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Top Speed
                    SpecProgressBar(
                        label = "TOP SPEED",
                        valueStr = "${currentCar.currentTopSpeed.toInt()} KM/H",
                        progress = (currentCar.currentTopSpeed / 450f).coerceIn(0f, 1f),
                        color = NeonCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Acceleration
                    SpecProgressBar(
                        label = "ACCELERATION",
                        valueStr = "${String.format("%.1f", currentCar.currentAcceleration)}s (0-100)",
                        progress = (1f - (currentCar.currentAcceleration / 4.5f)).coerceIn(0f, 1f),
                        color = NitroMagenta
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Handling
                    SpecProgressBar(
                        label = "HANDLING",
                        valueStr = String.format("%.2f", currentCar.currentHandling),
                        progress = (currentCar.currentHandling / 1.8f).coerceIn(0f, 1f),
                        color = ElectricGold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Nitro Tank Bonus
                    SpecProgressBar(
                        label = "NITRO CAPACITY",
                        valueStr = "+${currentCar.currentNitroBonus.toInt()}%",
                        progress = (currentCar.currentNitroBonus / 60f).coerceIn(0f, 1f),
                        color = NitroPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TABS: [UPGRADES & TUNING] | [PAINT & CUSTOMIZE]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { activeTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == 0) NeonCyan else DarkCard,
                        contentColor = if (activeTab == 0) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("tab_upgrades")
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = "Tuning", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "TUNING", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { activeTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == 1) NitroMagenta else DarkCard,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("tab_paint")
                ) {
                    Icon(imageVector = Icons.Default.ColorLens, contentDescription = "Paint", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "PAINT BOOTH", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TAB CONTENT
            if (activeTab == 0) {
                // TUNING UPGRADES
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val upgradeCost = 800 * currentCar.engineUpgradeLevel

                    UpgradeRow(
                        title = "ENGINE BLOCK (TOP SPEED)",
                        level = currentCar.engineUpgradeLevel,
                        cost = upgradeCost,
                        canAfford = playerCredits >= upgradeCost,
                        onUpgrade = { onUpgradeEngine(currentCar) }
                    )

                    UpgradeRow(
                        title = "NITRO INJECTION TANK",
                        level = currentCar.nitroUpgradeLevel,
                        cost = upgradeCost,
                        canAfford = playerCredits >= upgradeCost,
                        onUpgrade = { onUpgradeNitro(currentCar) }
                    )

                    UpgradeRow(
                        title = "SPORT SUSPENSION & TIRES",
                        level = currentCar.handlingUpgradeLevel,
                        cost = upgradeCost,
                        canAfford = playerCredits >= upgradeCost,
                        onUpgrade = { onUpgradeHandling(currentCar) }
                    )
                }
            } else {
                // PAINT BOOTH & UNDERGLOW
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "BODY COLOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for ((c, _) in availableColors) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (currentCar.bodyColor == c) 3.dp else 1.dp,
                                        color = if (currentCar.bodyColor == c) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onChangeColor(currentCar, c) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "NEON UNDERGLOW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for ((u, _) in availableUnderglows) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(u)
                                    .border(
                                        width = if (currentCar.underglowColor == u) 3.dp else 1.dp,
                                        color = if (currentCar.underglowColor == u) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onChangeUnderglow(currentCar, u) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ACTION BUTTONS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!currentCar.isUnlocked) {
                    Button(
                        onClick = { onBuyCar(currentCar) },
                        enabled = playerCredits >= currentCar.priceCredits,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("buy_car_button")
                    ) {
                        Text(
                            text = "UNLOCK CAR (🪙 ${currentCar.priceCredits})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Button(
                        onClick = onStartRace,
                        colors = ButtonDefaults.buttonColors(containerColor = SpeedGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("race_now_button")
                    ) {
                        Icon(imageVector = Icons.Default.SportsScore, contentDescription = "Race", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RACE WITH THIS CAR",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // AI RACE STRATEGIST BUTTON
                Button(
                    onClick = onOpenAiStrategist,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("ai_strategist_garage_button")
                ) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI Strategist", tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APEX-9 AI TUNING ADVISOR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecProgressBar(
    label: String,
    valueStr: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(text = valueStr, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF131722)
        )
    }
}

@Composable
private fun UpgradeRow(
    title: String,
    level: Int,
    cost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "LEVEL $level / 5", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = NeonCyan)
        }

        Button(
            onClick = onUpgrade,
            enabled = canAfford && level < 5,
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricGold,
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF232A38),
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = if (level >= 5) "MAX" else "🪙 $cost",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
