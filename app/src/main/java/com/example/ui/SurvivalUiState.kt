package com.example.ui

import com.example.data.model.CampStructureEntity
import com.example.data.model.ChestItemEntity
import com.example.data.model.ForestZone
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemType
import com.example.data.model.JournalEntryEntity
import com.example.data.model.StructureType
import com.example.data.model.SurvivalProfile
import com.example.data.model.WeatherType

data class FloatingLootMessage(
  val id: Long = System.currentTimeMillis() + (0..1000).random(),
  val text: String,
  val isPositive: Boolean = true
)

enum class ActiveScreenDialog {
  NONE,
  INVENTORY,
  CRAFTING,
  BUILDING,
  CAMP_STATION,
  CHEST_STORAGE,
  JOURNAL,
  SURVIVAL_GUIDE,
  SLEEP_SELECTOR
}

data class SurvivalUiState(
  val profile: SurvivalProfile = SurvivalProfile(),
  val inventory: List<InventoryItemEntity> = emptyList(),
  val structures: List<CampStructureEntity> = emptyList(),
  val chestItems: List<ChestItemEntity> = emptyList(),
  val journalEntries: List<JournalEntryEntity> = emptyList(),
  val activeDialog: ActiveScreenDialog = ActiveScreenDialog.NONE,
  val selectedCraftingCategory: String = "TODAS",
  val isSwingingTool: Boolean = false,
  val swingProgress: Float = 0f,
  val isCrafting: Boolean = false,
  val craftingProgress: Float = 0f,
  val floatingMessages: List<FloatingLootMessage> = emptyList(),
  val activeScreenShake: Boolean = false,
  val bannerNotification: String? = null,
  val playerX: Float = 1200f,
  val playerY: Float = 1200f,
  val playerFacingAngle: Float = 0f,
  val selectedBuildingToPlace: StructureType? = null,
  val placementX: Float = 1250f,
  val placementY: Float = 1250f,
  val canPlaceHere: Boolean = true
) {
  val currentZoneEnum: ForestZone
    get() = runCatching { ForestZone.valueOf(profile.currentZone) }.getOrDefault(ForestZone.ACAMPAMENTO)

  val currentWeatherEnum: WeatherType
    get() = runCatching { WeatherType.valueOf(profile.currentWeather) }.getOrDefault(WeatherType.ENSOLARADO)

  val equippedItem: ItemType?
    get() = ItemType.values().firstOrNull { it.id == profile.equippedItemId }

  val equippedClothing: ItemType?
    get() = ItemType.values().firstOrNull { it.id == profile.equippedClothingId }

  val isNight: Boolean
    get() = profile.timeMinutes >= 1260 || profile.timeMinutes < 360 // 21:00 to 06:00

  val isDusk: Boolean
    get() = profile.timeMinutes in 1020..1259 // 17:00 to 20:59

  val isDawn: Boolean
    get() = profile.timeMinutes in 360..479 // 06:00 to 07:59

  val formattedTime: String
    get() {
      val hours = (profile.timeMinutes / 60) % 24
      val minutes = profile.timeMinutes % 60
      return "%02d:%02d".format(hours, minutes)
    }

  val hasCampfireBurning: Boolean
    get() = structures.any { it.structureTypeId == "campfire" && it.fuelMinutesRemaining > 0 }

  val hasWorkbench: Boolean
    get() = structures.any { it.structureTypeId == "workbench" && it.isBuilt }

  val hasShelter: Boolean
    get() = structures.any { (it.structureTypeId == "lean_to" || it.structureTypeId == "log_cabin") && it.isBuilt }

  val hasCabin: Boolean
    get() = structures.any { it.structureTypeId == "log_cabin" && it.isBuilt }

  fun getItemCount(itemType: ItemType): Int {
    return inventory.filter { it.itemTypeId == itemType.id }.sumOf { it.quantity }
  }
}
