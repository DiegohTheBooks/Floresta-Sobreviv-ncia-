package com.example.data.repository

import com.example.data.local.SurvivalDao
import com.example.data.model.CampStructureEntity
import com.example.data.model.ChestItemEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemType
import com.example.data.model.JournalEntryEntity
import com.example.data.model.StructureType
import com.example.data.model.SurvivalProfile
import com.example.data.model.WeatherType
import kotlinx.coroutines.flow.Flow

class SurvivalRepository(private val dao: SurvivalDao) {

  val profile: Flow<SurvivalProfile?> = dao.getProfile()
  val inventory: Flow<List<InventoryItemEntity>> = dao.getAllInventoryItems()
  val structures: Flow<List<CampStructureEntity>> = dao.getAllStructures()
  val chestItems: Flow<List<ChestItemEntity>> = dao.getAllChestItems()
  val journalEntries: Flow<List<JournalEntryEntity>> = dao.getRecentJournal()

  suspend fun initializeGameIfNeeded() {
    val existingProfile = dao.getProfileSync()
    if (existingProfile == null) {
      resetToNewGame()
    }
  }

  suspend fun resetToNewGame() {
    dao.clearInventory()
    dao.clearChest()
    dao.clearJournal()

    val initialProfile = SurvivalProfile(
      id = 1,
      health = 100f,
      hunger = 90f,
      thirst = 90f,
      bodyTemp = 36.8f,
      stamina = 100f,
      wetness = 0f,
      hasInfection = false,
      isShivering = false,
      daysSurvived = 1,
      timeMinutes = 480, // 08:00 AM
      currentWeather = WeatherType.ENSOLARADO.name,
      equippedItemId = ItemType.AXE_STONE.id,
      playerX = 1200f,
      playerY = 1230f
    )
    dao.insertProfile(initialProfile)

    val starterItems = listOf(
      InventoryItemEntity(
        itemTypeId = ItemType.AXE_STONE.id,
        name = ItemType.AXE_STONE.displayName,
        category = "FERRAMENTA",
        quantity = 1,
        durability = ItemType.AXE_STONE.maxDurability,
        maxDurability = ItemType.AXE_STONE.maxDurability,
        isCrafted = true,
        isEquipped = true,
        slotIndex = 0
      ),
      InventoryItemEntity(
        itemTypeId = ItemType.FIRE_DRILL.id,
        name = ItemType.FIRE_DRILL.displayName,
        category = "SOBREVIVENCIA",
        quantity = 1,
        durability = ItemType.FIRE_DRILL.maxDurability,
        maxDurability = ItemType.FIRE_DRILL.maxDurability,
        isCrafted = true,
        isEquipped = false,
        slotIndex = 1
      ),
      InventoryItemEntity(
        itemTypeId = ItemType.BERRIES.id,
        name = ItemType.BERRIES.displayName,
        category = "ALIMENTO",
        quantity = 6,
        durability = 0,
        maxDurability = 0,
        isCrafted = false,
        isEquipped = false,
        slotIndex = 2
      ),
      InventoryItemEntity(
        itemTypeId = ItemType.STICK.id,
        name = ItemType.STICK.displayName,
        category = "COLETAVEL",
        quantity = 10,
        durability = 0,
        maxDurability = 0,
        isCrafted = false,
        isEquipped = false,
        slotIndex = 3
      ),
      InventoryItemEntity(
        itemTypeId = ItemType.WOOD.id,
        name = ItemType.WOOD.displayName,
        category = "COLETAVEL",
        quantity = 4,
        durability = 0,
        maxDurability = 0,
        isCrafted = false,
        isEquipped = false,
        slotIndex = 4
      ),
      InventoryItemEntity(
        itemTypeId = ItemType.FIBER.id,
        name = ItemType.FIBER.displayName,
        category = "COLETAVEL",
        quantity = 6,
        durability = 0,
        maxDurability = 0,
        isCrafted = false,
        isEquipped = false,
        slotIndex = 5
      )
    )
    dao.insertAllInventoryItems(starterItems)

    val starterStructures = listOf(
      CampStructureEntity(
        structureTypeId = StructureType.FOGUEIRA.id,
        worldX = 1200f,
        worldY = 1200f,
        health = 100f,
        fuelMinutesRemaining = 60, // burning campfire to welcome player
        isBuilt = true,
        builtDay = 1
      )
    )
    dao.insertAllStructures(starterStructures)

    val firstJournal = JournalEntryEntity(
      day = 1,
      timeString = "08:00",
      title = "Despertar na Floresta Densa",
      message = "Abri os olhos sob as copas das árvores gigantes. O ar é úmido e fresco. Restam algumas brasas de uma fogueira improvisada. Devo colher madeira, achar água potável e preparar um abrigo resistente antes do anoitecer.",
      type = "MILESTONE"
    )
    dao.insertJournal(firstJournal)
  }

  suspend fun updateProfile(profile: SurvivalProfile) = dao.updateProfile(profile)

  suspend fun saveInventoryItems(items: List<InventoryItemEntity>) {
    dao.clearInventory()
    dao.insertAllInventoryItems(items)
  }

  suspend fun insertInventoryItem(item: InventoryItemEntity) = dao.insertInventoryItem(item)

  suspend fun deleteInventoryItem(id: Long) = dao.deleteInventoryItem(id)

  suspend fun updateInventoryItem(item: InventoryItemEntity) = dao.updateInventoryItem(item)

  suspend fun insertOrUpdateStructure(structure: CampStructureEntity) = dao.insertStructure(structure)

  suspend fun getStructuresSync(): List<CampStructureEntity> = dao.getAllStructuresSync()

  suspend fun addChestItem(item: ChestItemEntity) = dao.insertChestItem(item)

  suspend fun deleteChestItem(id: Long) = dao.deleteChestItem(id)

  suspend fun addJournalEntry(entry: JournalEntryEntity) = dao.insertJournal(entry)
}
