package com.example

import com.example.domain.DayNightCycleManager
import com.example.domain.DayPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DayNightCycleTest {

  @Test
  fun testDayPhaseTransitions() {
    assertEquals(DayPhase.MADRUGADA, DayNightCycleManager.getDayPhase(120))  // 02:00
    assertEquals(DayPhase.ALVORADA, DayNightCycleManager.getDayPhase(360))   // 06:00
    assertEquals(DayPhase.MANHA, DayNightCycleManager.getDayPhase(600))      // 10:00
    assertEquals(DayPhase.MEIO_DIA, DayNightCycleManager.getDayPhase(780))   // 13:00
    assertEquals(DayPhase.ENTARDECER, DayNightCycleManager.getDayPhase(1050))// 17:30
    assertEquals(DayPhase.CREPUSCULO, DayNightCycleManager.getDayPhase(1200))// 20:00
    assertEquals(DayPhase.NOITE, DayNightCycleManager.getDayPhase(1320))     // 22:00
  }

  @Test
  fun testAmbientDarknessCurve() {
    // Broad daylight at noon must be 0.0 darkness
    assertEquals(0.0f, DayNightCycleManager.getAmbientDarkness(720), 0.01f) // 12:00
    assertEquals(0.0f, DayNightCycleManager.getAmbientDarkness(600), 0.01f) // 10:00

    // Midnight / Madrugada should reach peak nocturnal darkness (~0.88)
    assertEquals(0.88f, DayNightCycleManager.getAmbientDarkness(120), 0.01f) // 02:00
    assertEquals(0.88f, DayNightCycleManager.getAmbientDarkness(1350), 0.01f) // 22:30

    // Sunset / Dusk should be a smooth intermediate gradient
    val sunsetDarkness = DayNightCycleManager.getAmbientDarkness(1050) // 17:30
    assertTrue("Sunset darkness must be between 0.0 and 0.5", sunsetDarkness in 0.01f..0.50f)
  }

  @Test
  fun testPlayerVisionRadiusWithTorch() {
    // Broad daylight: view is unrestricted
    val dayVision = DayNightCycleManager.getPlayerVisionRadius(720, hasTorch = false)
    assertEquals(1000f, dayVision, 1f)

    // Deep night: without torch vision contracts
    val unlitNightVision = DayNightCycleManager.getPlayerVisionRadius(120, hasTorch = false)
    assertTrue("Unlit night vision should contract below 150px", unlitNightVision < 150f)

    // Deep night: with torch equipped, vision radius extends
    val torchNightVision = DayNightCycleManager.getPlayerVisionRadius(120, hasTorch = true)
    assertTrue("Torch night vision must exceed unlit vision", torchNightVision > unlitNightVision)
  }

  @Test
  fun testCycleTemperatureInfluences() {
    // Afternoon should provide warming offset
    val afternoonTemp = DayNightCycleManager.getCycleTemperatureOffset(840) // 14:00
    assertTrue("Afternoon temperature should be positive", afternoonTemp > 0f)

    // Early morning / pre-dawn should be freezing cold
    val preDawnTemp = DayNightCycleManager.getCycleTemperatureOffset(120) // 02:00
    assertTrue("Pre-dawn temperature should be negative", preDawnTemp < 0f)
  }
}
