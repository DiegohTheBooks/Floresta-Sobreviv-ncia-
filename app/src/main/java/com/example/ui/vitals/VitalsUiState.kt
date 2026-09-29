package com.example.ui.vitals

data class VitalsUiState(
  val health: Float = 100f,         // 0..100%
  val maxHealth: Float = 100f,
  val hunger: Float = 90f,          // 0..100%
  val maxHunger: Float = 100f,
  val thirst: Float = 90f,          // 0..100%
  val maxThirst: Float = 100f,
  val energy: Float = 100f,         // 0..100% (Estamina/Energia)
  val maxEnergy: Float = 100f,
  val bodyTemp: Float = 36.8f,      // °C (ideal ~36.8°C)
  val isStarving: Boolean = false,  // hunger < 20
  val isDehydrated: Boolean = false,// thirst < 20
  val isExhausted: Boolean = false, // energy < 20
  val isCriticallyInjured: Boolean = false, // health < 25
  val isShivering: Boolean = false, // bodyTemp < 35.5°C
  val statusMessage: String? = null
) {
  val healthFraction: Float get() = (health / maxHealth).coerceIn(0f, 1f)
  val hungerFraction: Float get() = (hunger / maxHunger).coerceIn(0f, 1f)
  val thirstFraction: Float get() = (thirst / maxThirst).coerceIn(0f, 1f)
  val energyFraction: Float get() = (energy / maxEnergy).coerceIn(0f, 1f)
}
