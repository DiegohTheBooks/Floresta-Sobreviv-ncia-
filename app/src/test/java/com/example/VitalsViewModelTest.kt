package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.vitals.VitalsUiState
import com.example.ui.vitals.VitalsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VitalsViewModelTest {

  private lateinit var viewModel: VitalsViewModel

  @Before
  fun setUp() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    viewModel = VitalsViewModel(application)
  }

  @Test
  fun testVitalsUiStateFractionCalculations() {
    val state = VitalsUiState(
      health = 50f,
      maxHealth = 100f,
      hunger = 75f,
      maxHunger = 100f,
      thirst = 30f,
      maxThirst = 100f,
      energy = 80f,
      maxEnergy = 100f
    )
    assertEquals(0.5f, state.healthFraction, 0.01f)
    assertEquals(0.75f, state.hungerFraction, 0.01f)
    assertEquals(0.3f, state.thirstFraction, 0.01f)
    assertEquals(0.8f, state.energyFraction, 0.01f)
    assertFalse(state.isStarving)
    assertFalse(state.isDehydrated)
  }

  @Test
  fun testFeedRestoresHungerAndHealth() {
    viewModel.feed(25f, 5f)
    val state = viewModel.vitalsState.value
    assertTrue(state.hunger > 0f)
  }

  @Test
  fun testHydrateRestoresThirst() {
    viewModel.hydrate(30f)
    val state = viewModel.vitalsState.value
    assertTrue(state.thirst > 0f)
  }

  @Test
  fun testExpendEnergyAndRecovery() {
    val initialEnergy = viewModel.vitalsState.value.energy
    val success = viewModel.expendEnergy(15f)
    assertTrue(success)
    val afterExpend = viewModel.vitalsState.value.energy
    assertEquals(initialEnergy - 15f, afterExpend, 0.1f)

    viewModel.recoverEnergy(10f)
    val afterRecover = viewModel.vitalsState.value.energy
    assertEquals(initialEnergy - 5f, afterRecover, 0.1f)
  }

  @Test
  fun testTakeDamageAndHeal() {
    val initialHealth = viewModel.vitalsState.value.health
    viewModel.takeDamage(20f)
    assertEquals(initialHealth - 20f, viewModel.vitalsState.value.health, 0.1f)

    viewModel.heal(15f)
    assertEquals(initialHealth - 5f, viewModel.vitalsState.value.health, 0.1f)
  }
}
