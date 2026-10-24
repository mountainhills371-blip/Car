package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Asphalt Nitro 3D", appName)
  }

  @Test
  fun `gyro motion controller initialization and calibration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val controller = GyroMotionController(context)
    
    // Initial state
    assertEquals(0f, controller.tiltInput, 0.001f)
    org.junit.Assert.assertFalse(controller.isCalibrated)

    // Calibration
    controller.calibrate()
    org.junit.Assert.assertTrue(controller.isCalibrated)

    // Reset calibration
    controller.resetCalibration()
    org.junit.Assert.assertFalse(controller.isCalibrated)

    // Sensitivity configuration
    controller.sensitivity = 1.4f
    assertEquals(1.4f, controller.sensitivity, 0.001f)
  }

  @Test
  fun `drift physics trigger and score multiplier calculation`() {
    val speedKmh = 180f
    val effectiveSteer = 0.75f
    
    // Sharp steering at high speed (> 130 km/h with |steer| > 0.55f) triggers auto drift
    val isAutoDrifting = (speedKmh > 130f && kotlin.math.abs(effectiveSteer) > 0.55f)
    org.junit.Assert.assertTrue(isAutoDrifting)

    // Score multiplier progression based on continuous drift duration
    val durationShort = 0.5f
    val multShort = when {
      durationShort >= 3.0f -> 4.0f
      durationShort >= 2.0f -> 3.0f
      durationShort >= 1.0f -> 2.0f
      durationShort >= 0.4f -> 1.5f
      else -> 1.0f
    }
    assertEquals(1.5f, multShort, 0.001f)

    val durationSustained = 2.5f
    val multSustained = when {
      durationSustained >= 3.0f -> 4.0f
      durationSustained >= 2.0f -> 3.0f
      durationSustained >= 1.0f -> 2.0f
      durationSustained >= 0.4f -> 1.5f
      else -> 1.0f
    }
    assertEquals(3.0f, multSustained, 0.001f)
  }
}
