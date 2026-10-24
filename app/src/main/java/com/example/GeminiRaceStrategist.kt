package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRaceStrategist {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun getTacticalStrategy(
        query: String,
        telemetry: RaceTelemetry?,
        currentCar: CarModel,
        currentTrack: TrackData
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineTacticalAdvice(query, telemetry, currentCar, currentTrack)
        }

        try {
            val telemetryContext = if (telemetry != null) {
                """
                LATEST RACE TELEMETRY:
                - Track: ${telemetry.trackName}
                - Vehicle: ${telemetry.carName}
                - Final Position: ${telemetry.position} of 6
                - Time: ${String.format("%.2f", telemetry.finishTimeSeconds)}s
                - Top Speed: ${telemetry.topSpeedAchieved.toInt()} km/h
                - Avg Speed: ${telemetry.averageSpeed.toInt()} km/h
                - Total Drift Duration: ${String.format("%.1f", telemetry.totalDriftSeconds)}s
                - Takedowns/Knockdowns: ${telemetry.knockdownsTriggered}
                - Shockwaves Fired: ${telemetry.shockwavesUsed}
                - Nitro Efficiency: ${(telemetry.nitroEfficiencyRatio * 100).toInt()}%
                """.trimIndent()
            } else {
                """
                CURRENT RACE SETUP:
                - Selected Track: ${currentTrack.name} (${currentTrack.location}, ${currentTrack.difficulty})
                - Vehicle: ${currentCar.name} (${currentCar.carClass})
                - Top Speed: ${currentCar.currentTopSpeed.toInt()} km/h
                - Acceleration: ${currentCar.currentAcceleration}s (0-100)
                - Handling: ${currentCar.currentHandling}
                - Engine Upgrade: Lv.${currentCar.engineUpgradeLevel} | Nitro: Lv.${currentCar.nitroUpgradeLevel} | Handling: Lv.${currentCar.handlingUpgradeLevel}
                """.trimIndent()
            }

            val systemPrompt = """
                You are APEX-9, the elite Chief AI Race Strategist & Engineering Director for Asphalt Nitro 3D.
                You provide deep, hyper-analytical race engineering guidance, telemetry diagnostics, corner apex calculations, shockwave nitro timing, aerodynamic downforce balance, and rival knockdown tactics.
                Structure your output with clear, punchy race radio callouts, bullet points, and high-IQ telemetry insights.
            """.trimIndent()

            val combinedPrompt = """
                $telemetryContext

                PILOT QUERY:
                $query

                Please give a thorough, high-level tactical and telemetry analysis with precise recommendations.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", combinedPrompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
                // Mandated gemini-4-argon with thinkingLevel: HIGH, without maxOutputTokens
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-4-argon:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // If model endpoint returned error, fallback gracefully to contextual calculation
                return@withContext getOfflineTacticalAdvice(query, telemetry, currentCar, currentTrack)
            }

            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val responseTextBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("text")) {
                        responseTextBuilder.append(part.getString("text"))
                    }
                }
            }

            val result = responseTextBuilder.toString().trim()
            if (result.isNotEmpty()) result else getOfflineTacticalAdvice(query, telemetry, currentCar, currentTrack)
        } catch (e: Exception) {
            getOfflineTacticalAdvice(query, telemetry, currentCar, currentTrack)
        }
    }

    private fun getOfflineTacticalAdvice(
        query: String,
        telemetry: RaceTelemetry?,
        car: CarModel,
        track: TrackData
    ): String {
        return buildString {
            appendLine("🏎️ **APEX-9 TACTICAL RACE ENGINEER REPORT**")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📍 **Sector Target:** ${track.name} [${track.difficulty}]")
            appendLine("🚀 **Machine:** ${car.name} (${car.carClass}) | Top Speed: ${car.currentTopSpeed.toInt()} km/h")
            appendLine()

            if (telemetry != null) {
                appendLine("📊 **Telemetry Evaluation:**")
                if (telemetry.position == 1) {
                    appendLine("• **P1 Podium Secured!** Peak velocity clocked at ${telemetry.topSpeedAchieved.toInt()} km/h.")
                    appendLine("• Nitro recovery through drifts accounted for ${String.format("%.1f", telemetry.totalDriftSeconds)}s of continuous boost.")
                } else {
                    appendLine("• **Finish Position: P${telemetry.position}**. Potential gain of ~${String.format("%.2f", (telemetry.position - 1) * 1.8)}s identified in Sector 2.")
                    if (telemetry.shockwavesUsed < 2) {
                        appendLine("• **Shockwave Deficit:** Only ${telemetry.shockwavesUsed} shockwaves fired. Save full 3-bar gauge for tight hairpin exits!")
                    }
                    if (telemetry.knockdownsTriggered == 0) {
                        appendLine("• **Aggression Opportunity:** No knockdowns recorded. Ram rivals with full Nitro active to trigger instant 100% Nitro refill!")
                    }
                }
                appendLine()
            }

            appendLine("🧠 **Engineering & Driving Directives:**")
            when {
                query.contains("nitro", ignoreCase = true) -> {
                    appendLine("1. **Shockwave Timing:** Double-tap Nitro when the gauge enters the magenta threshold for 140% thrust and complete collision immunity.")
                    appendLine("2. **Drift-to-Boost Loop:** Feather handbrake on turns to generate 25% nitro per second, then unleash instantly down straightaways.")
                    appendLine("3. **Stunt Airtime:** Hit launch ramps while boosting to initiate 360 flat spins that top up your nitro tank mid-air.")
                }
                query.contains("corner", ignoreCase = true) || query.contains("drift", ignoreCase = true) -> {
                    appendLine("1. **Late Apex Entry:** Turn in slightly late, kick the rear wheels out, and hold counter-steer to hug the inner curb.")
                    appendLine("2. **Angle Stabilization:** Maintain 25°-35° slip angle; exceeding 45° sheds excess velocity below 220 km/h.")
                    appendLine("3. **Snap Straight:** Release drift right as the road straightens and punch Nitro immediately for zero-lag corner exit.")
                }
                query.contains("upgrade", ignoreCase = true) || query.contains("tune", ignoreCase = true) -> {
                    appendLine("1. **Recommended Priority:** Upgrade Engine to Lv.${car.engineUpgradeLevel + 1} (+12 km/h top speed ceiling).")
                    appendLine("2. **Secondary Priority:** Level up Nitro Injectors for extended burn duration.")
                    appendLine("3. **Underglow Aerodynamics:** Equip Neon Cyan underglow for enhanced track visual contrast in high-speed night sectors.")
                }
                else -> {
                    appendLine("1. **Drafting Strategy:** Slingshot behind lead rivals at 15m to reduce aerodynamic drag by 18%, then slingshot with Nitro.")
                    appendLine("2. **Takedown Rules:** When Shockwave Nitro is engaged, side-swipe any opponent to cause an instant explosive knockdown.")
                    appendLine("3. **Curvature Optimization:** In ${track.name}, bank hard into the curved segments to maximize centrifugal momentum.")
                }
            }
            appendLine()
            appendLine("🏁 *Ready to dominate the Asphalt Cup!*")
        }
    }
}
