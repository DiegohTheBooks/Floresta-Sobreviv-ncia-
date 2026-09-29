package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chest_items")
data class ChestItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val itemTypeId: String,
  val quantity: Int,
  val durability: Int = 0
)

@Entity(tableName = "survival_journal")
data class JournalEntryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val day: Int,
  val timeString: String,
  val title: String,
  val message: String,
  val type: String = "INFO" // INFO, MILESTONE, DANGER, CRAFT
)
