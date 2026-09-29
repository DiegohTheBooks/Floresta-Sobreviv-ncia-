package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val itemTypeId: String,
  val name: String,
  val category: String, // "COLETAVEL", "FERRAMENTA", "ALIMENTO", "SOBREVIVENCIA", "VESTUARIO", "MEDICINA"
  val quantity: Int,
  val durability: Int = 0,
  val maxDurability: Int = 0,
  val isCrafted: Boolean = false, // true for tools and gear crafted by the player
  val isEquipped: Boolean = false,
  val slotIndex: Int = 0,
  val timestamp: Long = System.currentTimeMillis()
)
