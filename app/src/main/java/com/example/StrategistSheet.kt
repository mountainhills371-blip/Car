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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.NitroPurple

@Composable
fun StrategistSheet(
    modifier: Modifier = Modifier,
    currentCar: CarModel,
    currentTrack: TrackData,
    lastTelemetry: RaceTelemetry?,
    adviceText: String,
    isLoading: Boolean,
    onAskStrategist: (String) -> Unit,
    onClose: () -> Unit
) {
    var queryInput by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Optimal Shockwave Nitro timing for ${currentTrack.name}",
        "How to apex curves without dropping speed below 250 KM/H?",
        "Takedown tactics: how to knockdown aggressive rivals?",
        "What part should I upgrade next for ${currentCar.name}?",
        "Drafting slipstream physics breakdown"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCarbon)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .background(DarkCard, CircleShape)
                            .testTag("close_strategist_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "APEX-9 AI RACE STRATEGIST",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = ElectricGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Model: gemini-4-argon • High Thinking Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NitroMagenta
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Quick Chips
                Text(
                    text = "SELECT TACTICAL QUERY:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (q in quickQuestions) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkCard)
                                .border(1.dp, Color(0xFF26334D), RoundedCornerShape(10.dp))
                                .clickable {
                                    queryInput = q
                                    onAskStrategist(q)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = q,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Strategy Output Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF10141F))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "🧠 Apex-9 Deep Thinking Analysis in Progress...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NitroMagenta
                            )
                            Text(
                                text = "Simulating aerodynamics, telemetry and corner entry vectors",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Text(
                            text = adviceText.ifEmpty {
                                "Tap any tactical directive above or enter a telemetry question below to engage Apex-9 AI."
                            },
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Query Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = queryInput,
                    onValueChange = { queryInput = it },
                    placeholder = { Text("Ask telemetry or race tactic...", fontSize = 13.sp, color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_query_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF2B374E),
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (queryInput.isNotBlank()) {
                            onAskStrategist(queryInput)
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(NeonCyan, NitroMagenta))
                        )
                        .testTag("submit_ai_query_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}
