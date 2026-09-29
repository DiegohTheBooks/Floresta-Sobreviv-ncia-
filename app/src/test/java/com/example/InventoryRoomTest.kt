package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SurvivalDao
import com.example.data.local.SurvivalDatabase
import com.example.data.model.ItemType
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InventoryRoomTest {

  private lateinit var database: SurvivalDatabase
  private lateinit var dao: SurvivalDao
  private lateinit var inventoryRepository: InventoryRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, SurvivalDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = database.survivalDao()
    inventoryRepository = InventoryRepository(dao)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testAddCollectedResourcePersistsInRoom() = runBlocking {
    val id = inventoryRepository.addCollectedResource(ItemType.WOOD, 5)
    assertTrue(id > 0)

    val items = inventoryRepository.allItems.first()
    assertEquals(1, items.size)
    val item = items.first()
    assertEquals("wood", item.itemTypeId)
    assertEquals(5, item.quantity)
    assertFalse("Resource should not be marked as crafted", item.isCrafted)

    // Stacking test
    inventoryRepository.addCollectedResource(ItemType.WOOD, 3)
    val updatedItems = inventoryRepository.allItems.first()
    assertEquals(1, updatedItems.size)
    assertEquals(8, updatedItems.first().quantity)
  }

  @Test
  fun testAddCraftedToolPersistsWithDurability() = runBlocking {
    val id = inventoryRepository.addCraftedTool(ItemType.AXE_STONE)
    assertTrue(id > 0)

    val tools = inventoryRepository.craftedTools.first()
    assertEquals(1, tools.size)
    val tool = tools.first()
    assertEquals("axe_stone", tool.itemTypeId)
    assertTrue("Crafted tool must have isCrafted = true", tool.isCrafted)
    assertEquals(ItemType.AXE_STONE.maxDurability, tool.durability)
    assertEquals(ItemType.AXE_STONE.maxDurability, tool.maxDurability)

    // Equipping test
    inventoryRepository.setEquippedState(tool.id, true)
    val equippedTool = inventoryRepository.craftedTools.first().first()
    assertTrue(equippedTool.isEquipped)

    // Degrading test
    val broke = inventoryRepository.degradeTool(tool.id)
    assertFalse("Tool should not break on first use", broke)
    val degraded = inventoryRepository.craftedTools.first().first()
    assertEquals(ItemType.AXE_STONE.maxDurability - 1, degraded.durability)
  }

  @Test
  fun testConsumeOrDropItem() = runBlocking {
    inventoryRepository.addCollectedResource(ItemType.BERRIES, 10)
    val initial = inventoryRepository.allItems.first().first()
    assertEquals(10, initial.quantity)

    // Consume 3 berries
    inventoryRepository.consumeOrDrop(initial, 3)
    val afterConsume = inventoryRepository.allItems.first().first()
    assertEquals(7, afterConsume.quantity)

    // Drop remaining 7 berries
    inventoryRepository.consumeOrDrop(afterConsume, 7)
    val afterDrop = inventoryRepository.allItems.first()
    assertTrue(afterDrop.isEmpty())
  }
}
