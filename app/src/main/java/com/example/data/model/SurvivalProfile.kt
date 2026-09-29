package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survival_profile")
data class SurvivalProfile(
  @PrimaryKey val id: Int = 1,
  val health: Float = 100f,          // 0..100
  val hunger: Float = 85f,          // 0..100
  val thirst: Float = 85f,          // 0..100
  val bodyTemp: Float = 36.8f,       // 34.0..40.0 °C (normal ~36.8°C)
  val stamina: Float = 100f,         // 0..100
  val wetness: Float = 0f,           // 0..100% (elevates hypothermia risk)
  val hasInfection: Boolean = false, // from drinking dirty water or untreated wounds
  val isShivering: Boolean = false,  // hypothermia warning state
  val daysSurvived: Int = 1,
  val timeMinutes: Int = 480,        // 480 min = 08:00 AM morning
  val currentWeather: String = WeatherType.ENSOLARADO.name,
  val currentZone: String = ForestZone.ACAMPAMENTO.name,
  val equippedItemId: String? = "axe_stone",
  val equippedClothingId: String? = null,
  val playerX: Float = 600f,
  val playerY: Float = 600f,
  val totalWoodGathered: Int = 0,
  val totalStoneMined: Int = 0,
  val totalAnimalsHunted: Int = 0,
  val totalItemsCrafted: Int = 0,
  val isGameOver: Boolean = false,
  val deathCause: String? = null
)
