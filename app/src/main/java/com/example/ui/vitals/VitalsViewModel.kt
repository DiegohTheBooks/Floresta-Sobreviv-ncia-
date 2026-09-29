package com.example.ui.vitals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SurvivalDatabase
import com.example.data.model.SurvivalProfile
import com.example.data.repository.SurvivalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VitalsViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: SurvivalRepository
  private val _localVitals = MutableStateFlow(VitalsUiState())
  private var metabolismJob: Job? = null

  init {
    val db = SurvivalDatabase.getDatabase(application)
    repository = SurvivalRepository(db.survivalDao())

    // Observe Room profile to keep local vitals state in sync with persistence
    viewModelScope.launch {
      repository.profile.collect { profile ->
        if (profile != null) {
          updateFromProfile(profile)
        }
      }
    }

    startMetabolismLoop()
  }

  val vitalsState: StateFlow<VitalsUiState> = _localVitals.asStateFlow()

  private fun updateFromProfile(p: SurvivalProfile) {
    _localVitals.value = _localVitals.value.copy(
      health = p.health,
      hunger = p.hunger,
      thirst = p.thirst,
      energy = p.stamina,
      bodyTemp = p.bodyTemp,
      isStarving = p.hunger < 20f,
      isDehydrated = p.thirst < 20f,
      isExhausted = p.stamina < 20f,
      isCriticallyInjured = p.health < 25f,
      isShivering = p.isShivering
    )
  }

  private fun startMetabolismLoop() {
    metabolismJob?.cancel()
    metabolismJob = viewModelScope.launch {
      while (true) {
        delay(1000L) // tick every second
        tickMetabolism()
      }
    }
  }

  private suspend fun tickMetabolism() {
    val current = _localVitals.value

    // 1. Natural Decay
    val newHunger = (current.hunger - 0.05f).coerceAtLeast(0f)
    val newThirst = (current.thirst - 0.08f).coerceAtLeast(0f)

    // 2. Energy Regeneration when not performing heavy work
    val energyRegen = if (newHunger < 20f || newThirst < 20f) 0.5f else 1.8f
    val newEnergy = (current.energy + energyRegen).coerceAtMost(100f)

    // 3. Health Consequences (Starvation & Dehydration)
    var newHealth = current.health
    var statusMsg: String? = null

    if (newHunger <= 0f) {
      newHealth = (newHealth - 0.3f).coerceAtLeast(0f)
      statusMsg = "Inanição: Você está morrendo de fome!"
    } else if (newThirst <= 0f) {
      newHealth = (newHealth - 0.5f).coerceAtLeast(0f)
      statusMsg = "Desidratação: Você precisa de água urgente!"
    } else if (newHunger > 60f && newThirst > 60f && current.health < 100f) {
      // Natural healing when well nourished and hydrated
      newHealth = (newHealth + 0.2f).coerceAtMost(100f)
    }

    val updatedState = current.copy(
      health = newHealth,
      hunger = newHunger,
      thirst = newThirst,
      energy = newEnergy,
      isStarving = newHunger < 20f,
      isDehydrated = newThirst < 20f,
      isExhausted = newEnergy < 20f,
      isCriticallyInjured = newHealth < 25f,
      statusMessage = statusMsg
    )
    _localVitals.value = updatedState

    // Sync back to Room profile
    val profile = repository.profile
    viewModelScope.launch {
      val existing = repository.getStructuresSync() // ensure DB active
      // update Room
      val currentProf = SurvivalProfile(
        id = 1,
        health = updatedState.health,
        hunger = updatedState.hunger,
        thirst = updatedState.thirst,
        stamina = updatedState.energy,
        bodyTemp = updatedState.bodyTemp
      )
      repository.updateProfile(currentProf)
    }
  }

  fun feed(amount: Float, healthBonus: Float = 0f) {
    val cur = _localVitals.value
    val newHunger = (cur.hunger + amount).coerceAtMost(100f)
    val newHealth = (cur.health + healthBonus).coerceAtMost(100f)
    val updated = cur.copy(hunger = newHunger, health = newHealth, isStarving = newHunger < 20f)
    _localVitals.value = updated
    persistVitals(updated)
  }

  fun hydrate(amount: Float) {
    val cur = _localVitals.value
    val newThirst = (cur.thirst + amount).coerceAtMost(100f)
    val updated = cur.copy(thirst = newThirst, isDehydrated = newThirst < 20f)
    _localVitals.value = updated
    persistVitals(updated)
  }

  fun expendEnergy(amount: Float): Boolean {
    val cur = _localVitals.value
    if (cur.energy < amount) return false

    val newEnergy = (cur.energy - amount).coerceAtLeast(0f)
    val updated = cur.copy(energy = newEnergy, isExhausted = newEnergy < 20f)
    _localVitals.value = updated
    persistVitals(updated)
    return true
  }

  fun recoverEnergy(amount: Float) {
    val cur = _localVitals.value
    val newEnergy = (cur.energy + amount).coerceAtMost(100f)
    val updated = cur.copy(energy = newEnergy, isExhausted = newEnergy < 20f)
    _localVitals.value = updated
    persistVitals(updated)
  }

  fun takeDamage(damage: Float) {
    val cur = _localVitals.value
    val newHealth = (cur.health - damage).coerceAtLeast(0f)
    val updated = cur.copy(health = newHealth, isCriticallyInjured = newHealth < 25f)
    _localVitals.value = updated
    persistVitals(updated)
  }

  fun heal(amount: Float) {
    val cur = _localVitals.value
    val newHealth = (cur.health + amount).coerceAtMost(100f)
    val updated = cur.copy(health = newHealth, isCriticallyInjured = newHealth < 25f)
    _localVitals.value = updated
    persistVitals(updated)
  }

  fun updateBodyTemp(temp: Float) {
    val cur = _localVitals.value
    val updated = cur.copy(bodyTemp = temp, isShivering = temp < 35.5f)
    _localVitals.value = updated
    persistVitals(updated)
  }

  private fun persistVitals(state: VitalsUiState) {
    viewModelScope.launch {
      val prof = SurvivalProfile(
        id = 1,
        health = state.health,
        hunger = state.hunger,
        thirst = state.thirst,
        stamina = state.energy,
        bodyTemp = state.bodyTemp
      )
      repository.updateProfile(prof)
    }
  }

  override fun onCleared() {
    super.onCleared()
    metabolismJob?.cancel()
  }
}
