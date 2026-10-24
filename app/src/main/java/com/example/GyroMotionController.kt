package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.min

/**
 * Real-time Gyroscope and Motion Sensor Controller for 3D racing game.
 * Reads device tilt (roll angle) and angular velocity to enable physical steering.
 */
class GyroMotionController(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val rotationVectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val gravitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val isSensorAvailable: Boolean =
        rotationVectorSensor != null || gravitySensor != null || accelerometerSensor != null
    val hasGyroscope: Boolean = gyroSensor != null || rotationVectorSensor != null

    // Compose state properties
    var tiltInput by mutableFloatStateOf(0f)
        private set

    var angularVelocityZ by mutableFloatStateOf(0f)
        private set

    var rawRollAngleDeg by mutableFloatStateOf(0f)
        private set

    var isCalibrated by mutableStateOf(false)
        private set

    // Internal physics & filter variables
    @Volatile
    private var smoothedTilt: Float = 0f

    @Volatile
    private var zeroCalibrationOffset: Float = 0f

    private val rotationMatrix = FloatArray(9)
    private val adjustedMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Sensitivity configuration (1.0 = normal, 1.4 = fast, 0.7 = low)
    var sensitivity: Float = 1.0f

    private var isListening = false

    fun start() {
        if (isListening || sensorManager == null) return

        // 1. Prioritize Rotation Vector for fused tilt without drift
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        } else if (gravitySensor != null) {
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME)
        } else if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        // 2. Register Gyroscope for instantaneous angular velocity
        if (gyroSensor != null) {
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        isListening = true
    }

    fun stop() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    /**
     * Calibrate neutral resting position of the phone.
     */
    fun calibrate() {
        zeroCalibrationOffset = smoothedTilt
        isCalibrated = true
    }

    /**
     * Reset calibration back to flat center.
     */
    fun resetCalibration() {
        zeroCalibrationOffset = 0f
        isCalibrated = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val displayRotation = getDisplayRotation()

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                // Remap coordinates based on device screen orientation
                when (displayRotation) {
                    Surface.ROTATION_90 -> {
                        SensorManager.remapCoordinateSystem(
                            rotationMatrix,
                            SensorManager.AXIS_Y,
                            SensorManager.AXIS_MINUS_X,
                            adjustedMatrix
                        )
                    }
                    Surface.ROTATION_270 -> {
                        SensorManager.remapCoordinateSystem(
                            rotationMatrix,
                            SensorManager.AXIS_MINUS_Y,
                            SensorManager.AXIS_X,
                            adjustedMatrix
                        )
                    }
                    Surface.ROTATION_180 -> {
                        SensorManager.remapCoordinateSystem(
                            rotationMatrix,
                            SensorManager.AXIS_MINUS_X,
                            SensorManager.AXIS_MINUS_Y,
                            adjustedMatrix
                        )
                    }
                    else -> {
                        SensorManager.remapCoordinateSystem(
                            rotationMatrix,
                            SensorManager.AXIS_X,
                            SensorManager.AXIS_Y,
                            adjustedMatrix
                        )
                    }
                }

                SensorManager.getOrientation(adjustedMatrix, orientationAngles)
                // orientationAngles[2] is roll in radians
                val rollRad = orientationAngles[2]
                val rollDeg = Math.toDegrees(rollRad.toDouble()).toFloat()
                rawRollAngleDeg = rollDeg

                // Max steering lock reached at ~28 degrees of physical tilt
                val maxSteerAngleDeg = 28f
                val normalizedTilt = (rollDeg / maxSteerAngleDeg).coerceIn(-1.0f, 1.0f)
                processTilt(normalizedTilt)
            }

            Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER -> {
                // Fallback when rotation vector is absent
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                val rawTilt = when (displayRotation) {
                    Surface.ROTATION_90 -> (-ay / 6.0f).coerceIn(-1.0f, 1.0f)
                    Surface.ROTATION_270 -> (ay / 6.0f).coerceIn(-1.0f, 1.0f)
                    Surface.ROTATION_180 -> (-ax / 6.0f).coerceIn(-1.0f, 1.0f)
                    else -> (ax / 6.0f).coerceIn(-1.0f, 1.0f)
                }
                rawRollAngleDeg = rawTilt * 28f
                processTilt(rawTilt)
            }

            Sensor.TYPE_GYROSCOPE -> {
                // Angular velocity around Z axis (screen face) in rad/s
                val gz = when (displayRotation) {
                    Surface.ROTATION_90 -> -event.values[0]
                    Surface.ROTATION_270 -> event.values[0]
                    else -> event.values[2]
                }
                angularVelocityZ = gz
            }
        }
    }

    private fun processTilt(targetNormalizedTilt: Float) {
        // Low-pass filter for smooth motion without jitter
        val filterAlpha = 0.28f
        smoothedTilt += (targetNormalizedTilt - smoothedTilt) * filterAlpha

        val calibratedTilt = (smoothedTilt - zeroCalibrationOffset).coerceIn(-1.0f, 1.0f)

        // Deadzone of 0.04 (~2.5 degrees) so the car drives straight naturally
        val deadzone = 0.04f
        val finalTilt = if (abs(calibratedTilt) < deadzone) {
            0f
        } else {
            val sign = if (calibratedTilt > 0f) 1f else -1f
            val magnitude = (abs(calibratedTilt) - deadzone) / (1f - deadzone)
            // Non-linear response curve for delicate fine-tuning around center and sharp turning at edges
            sign * (magnitude * 0.75f + magnitude * magnitude * 0.25f)
        }

        tiltInput = (finalTilt * sensitivity).coerceIn(-1.0f, 1.0f)
    }

    private fun getDisplayRotation(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.rotation ?: Surface.ROTATION_0
            } else {
                @Suppress("DEPRECATION")
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }
        } catch (e: Exception) {
            Surface.ROTATION_0
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

/**
 * Remember and lifecycle-manage the GyroMotionController
 */
@Composable
fun rememberGyroMotionController(): GyroMotionController {
    val context = LocalContext.current
    val controller = remember(context) { GyroMotionController(context) }

    DisposableEffect(controller) {
        controller.start()
        onDispose {
            controller.stop()
        }
    }

    return controller
}
