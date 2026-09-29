package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CampStructureEntity
import com.example.data.model.ChestItemEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.SurvivalProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface SurvivalDao {
  // Survival Profile
  @Query("SELECT * FROM survival_profile WHERE id = 1 LIMIT 1")
  fun getProfile(): Flow<SurvivalProfile?>

  @Query("SELECT * FROM survival_profile WHERE id = 1 LIMIT 1")
  suspend fun getProfileSync(): SurvivalProfile?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfile(profile: SurvivalProfile)

  @Update
  suspend fun updateProfile(profile: SurvivalProfile)

  // Room Inventory Operations (Coletáveis & Ferramentas Fabricadas)
  @Query("SELECT * FROM inventory_items ORDER BY isEquipped DESC, isCrafted DESC, id DESC")
  fun getAllInventoryItems(): Flow<List<InventoryItemEntity>>

  @Query("SELECT * FROM inventory_items WHERE isCrafted = 1 ORDER BY isEquipped DESC, id DESC")
  fun getCraftedTools(): Flow<List<InventoryItemEntity>>

  @Query("SELECT * FROM inventory_items WHERE isCrafted = 0 ORDER BY id DESC")
  fun getCollectibleItems(): Flow<List<InventoryItemEntity>>

  @Query("SELECT * FROM inventory_items WHERE itemTypeId = :itemTypeId LIMIT 1")
  suspend fun findFirstByTypeId(itemTypeId: String): InventoryItemEntity?

  @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
  fun getInventoryItemById(id: Long): Flow<InventoryItemEntity?>

  @Query("SELECT * FROM inventory_items ORDER BY slotIndex ASC")
  suspend fun getAllInventoryItemsSync(): List<InventoryItemEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInventoryItem(item: InventoryItemEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllInventoryItems(items: List<InventoryItemEntity>)

  @Update
  suspend fun updateInventoryItem(item: InventoryItemEntity)

  @Query("DELETE FROM inventory_items WHERE id = :id")
  suspend fun deleteInventoryItem(id: Long)

  @Query("DELETE FROM inventory_items")
  suspend fun clearInventory()

  // Camp Structures
  @Query("SELECT * FROM camp_structures")
  fun getAllStructures(): Flow<List<CampStructureEntity>>

  @Query("SELECT * FROM camp_structures")
  suspend fun getAllStructuresSync(): List<CampStructureEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStructure(structure: CampStructureEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllStructures(structures: List<CampStructureEntity>)

  @Update
  suspend fun updateStructure(structure: CampStructureEntity)

  @Query("DELETE FROM camp_structures WHERE structureTypeId = :typeId")
  suspend fun deleteStructure(typeId: String)

  // Storage Chest
  @Query("SELECT * FROM chest_items")
  fun getAllChestItems(): Flow<List<ChestItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChestItem(item: ChestItemEntity): Long

  @Update
  suspend fun updateChestItem(item: ChestItemEntity)

  @Query("DELETE FROM chest_items WHERE id = :id")
  suspend fun deleteChestItem(id: Long)

  @Query("DELETE FROM chest_items")
  suspend fun clearChest()

  // Journal Logs
  @Query("SELECT * FROM survival_journal ORDER BY id DESC LIMIT 50")
  fun getRecentJournal(): Flow<List<JournalEntryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJournal(entry: JournalEntryEntity)

  @Query("DELETE FROM survival_journal")
  suspend fun clearJournal()
}
