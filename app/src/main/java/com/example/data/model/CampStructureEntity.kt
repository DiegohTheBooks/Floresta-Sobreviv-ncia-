package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camp_structures")
data class CampStructureEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val structureTypeId: String, // e.g. "campfire", "tower", "wall", "workbench", etc.
  val worldX: Float = 550f,
  val worldY: Float = 550f,
  val health: Float = 100f,
  val level: Int = 1,
  val fuelMinutesRemaining: Int = 0, // for campfire: burns wood/sticks
  val waterStoredLiters: Float = 0f, // for rain catcher: up to 10.0L
  val isBuilt: Boolean = true,
  val builtDay: Int = 1
)
