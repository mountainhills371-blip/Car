package com.example

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural Audio Synthesizer for Asphalt Nitro 3D.
 * Generates dynamic engine revs, turbo roar, tire drift screech,
 * crash impacts, and UI sound effects using Android's native AudioTrack.
 */
class SoundEffects {
    var isMuted: Boolean = false
    private val sampleRate = 22050
    private var engineJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private var currentEngineRpmRatio = 0.2f
    private var isNitroRoarActive = false
    private var isDriftScreechActive = false

    fun updateEngineState(speedRatio: Float, nitro: Boolean, drifting: Boolean) {
        currentEngineRpmRatio = speedRatio.coerceIn(0.1f, 1.2f)
        isNitroRoarActive = nitro
        isDriftScreechActive = drifting
    }

    fun startEngine() {
        if (engineJob != null) return
        engineJob = scope.launch {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            try {
                track.play()
                val buffer = ShortArray(1024)
                var phase = 0.0
                var subPhase = 0.0

                while (isActive) {
                    if (isMuted) {
                        buffer.fill(0)
                        track.write(buffer, 0, buffer.size)
                        delay(20)
                        continue
                    }

                    // Engine base frequency: 55Hz (idle) to 260Hz (redline)
                    val baseFreq = 55.0 + currentEngineRpmRatio * 205.0
                    val nitroMult = if (isNitroRoarActive) 1.25 else 1.0
                    val freq = baseFreq * nitroMult

                    for (i in buffer.indices) {
                        phase += (2.0 * Math.PI * freq) / sampleRate
                        subPhase += (2.0 * Math.PI * (freq * 0.5)) / sampleRate
                        if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                        if (subPhase > 2.0 * Math.PI) subPhase -= 2.0 * Math.PI

                        // Harmonically rich engine tone with distortion
                        val osc1 = sin(phase)
                        val osc2 = sin(subPhase) * 0.4
                        val harmonic = sin(phase * 3.0) * 0.2
                        var sample = (osc1 + osc2 + harmonic) * 0.35

                        // Nitro turbo whoosh (high frequency turbulent noise)
                        if (isNitroRoarActive) {
                            val noise = (Math.random() * 2.0 - 1.0) * 0.25
                            val turboWhine = sin(phase * 6.0) * 0.2
                            sample = (sample * 0.7) + noise + turboWhine
                        }

                        // Drift screech (white noise + resonance)
                        if (isDriftScreechActive) {
                            val screechNoise = (Math.random() * 2.0 - 1.0) * 0.35
                            sample += screechNoise
                        }

                        // Soft clipping
                        sample = sample.coerceIn(-1.0, 1.0)
                        buffer[i] = (sample * 16000).toInt().toShort()
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Audio track exception safe-guard
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (e: Exception) {}
            }
        }
    }

    fun stopEngine() {
        engineJob?.cancel()
        engineJob = null
    }

    fun playKnockdownCrash() {
        if (isMuted) return
        scope.launch {
            playTone(frequency = 90.0, durationMs = 250, isNoise = true, volume = 0.9f)
        }
    }

    fun playNitroIgnite() {
        if (isMuted) return
        scope.launch {
            playTone(frequency = 420.0, durationMs = 180, isNoise = false, volume = 0.6f)
        }
    }

    fun playCountdownBeep(isFinal: Boolean) {
        if (isMuted) return
        scope.launch {
            val freq = if (isFinal) 880.0 else 440.0
            playTone(frequency = freq, durationMs = if (isFinal) 300 else 150, isNoise = false, volume = 0.7f)
        }
    }

    private fun playTone(frequency: Double, durationMs: Int, isNoise: Boolean, volume: Float) {
        try {
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0

            for (i in 0 until numSamples) {
                val envelope = 1.0 - (i.toDouble() / numSamples)
                val sampleVal = if (isNoise) {
                    (Math.random() * 2.0 - 1.0) * envelope * volume
                } else {
                    phase += (2.0 * Math.PI * frequency) / sampleRate
                    sin(phase) * envelope * volume
                }
                buffer[i] = (sampleVal.coerceIn(-1.0, 1.0) * 24000).toInt().toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            scope.launch {
                delay(durationMs.toLong() + 50)
                try {
                    track.release()
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {}
    }
}
