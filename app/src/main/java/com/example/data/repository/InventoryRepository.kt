package com.example.data.repository

import com.example.data.local.SurvivalDao
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.ItemType
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val dao: SurvivalDao) {

  val allItems: Flow<List<InventoryItemEntity>> = dao.getAllInventoryItems()
  val craftedTools: Flow<List<InventoryItemEntity>> = dao.getCraftedTools()
  val collectibleItems: Flow<List<InventoryItemEntity>> = dao.getCollectibleItems()

  suspend fun addCollectedResource(itemType: ItemType, quantity: Int): Long {
    val items = dao.getAllInventoryItemsSync()
    val existing = items.firstOrNull { it.itemTypeId == itemType.id && !it.isCrafted && it.quantity < itemType.maxStack }

    return if (existing != null) {
      val newQty = (existing.quantity + quantity).coerceAtMost(itemType.maxStack)
      val updated = existing.copy(quantity = newQty)
      dao.updateInventoryItem(updated)

      val overflow = (existing.quantity + quantity) - itemType.maxStack
      if (overflow > 0) {
        val newEntity = InventoryItemEntity(
          itemTypeId = itemType.id,
          name = itemType.displayName,
          category = "COLETAVEL",
          quantity = overflow,
          durability = 0,
          maxDurability = 0,
          isCrafted = false,
          isEquipped = false,
          slotIndex = items.size
        )
        dao.insertInventoryItem(newEntity)
      } else {
        existing.id
      }
    } else {
      val newEntity = InventoryItemEntity(
        itemTypeId = itemType.id,
        name = itemType.displayName,
        category = if (itemType.category == ItemCategory.ALIMENTO) "ALIMENTO" else "COLETAVEL",
        quantity = quantity,
        durability = itemType.maxDurability,
        maxDurability = itemType.maxDurability,
        isCrafted = false,
        isEquipped = false,
        slotIndex = items.size
      )
      dao.insertInventoryItem(newEntity)
    }
  }

  suspend fun addCraftedTool(itemType: ItemType, quantity: Int = 1): Long {
    val items = dao.getAllInventoryItemsSync()
    val newEntity = InventoryItemEntity(
      itemTypeId = itemType.id,
      name = itemType.displayName,
      category = itemType.category.name,
      quantity = quantity,
      durability = itemType.maxDurability,
      maxDurability = itemType.maxDurability,
      isCrafted = true,
      isEquipped = false,
      slotIndex = items.size
    )
    return dao.insertInventoryItem(newEntity)
  }

  suspend fun setEquippedState(itemId: Long, equipped: Boolean) {
    val items = dao.getAllInventoryItemsSync()
    val target = items.firstOrNull { it.id == itemId } ?: return
    dao.updateInventoryItem(target.copy(isEquipped = equipped))
  }

  suspend fun consumeOrDrop(entity: InventoryItemEntity, count: Int = 1) {
    if (entity.quantity > count) {
      dao.updateInventoryItem(entity.copy(quantity = entity.quantity - count))
    } else {
      dao.deleteInventoryItem(entity.id)
    }
  }

  suspend fun degradeTool(itemId: Long): Boolean {
    val items = dao.getAllInventoryItemsSync()
    val target = items.firstOrNull { it.id == itemId } ?: return false

    return if (target.durability > 1) {
      dao.updateInventoryItem(target.copy(durability = target.durability - 1))
      false // not broken yet
    } else {
      dao.deleteInventoryItem(target.id)
      true // broke!
    }
  }

  suspend fun getItemCount(itemTypeId: String): Int {
    return dao.getAllInventoryItemsSync()
      .filter { it.itemTypeId == itemTypeId }
      .sumOf { it.quantity }
  }

  suspend fun deductItemQuantity(itemTypeId: String, amount: Int) {
    var remaining = amount
    val items = dao.getAllInventoryItemsSync()
    for (item in items) {
      if (item.itemTypeId == itemTypeId) {
        if (item.quantity <= remaining) {
          remaining -= item.quantity
          dao.deleteInventoryItem(item.id)
        } else {
          dao.updateInventoryItem(item.copy(quantity = item.quantity - remaining))
          remaining = 0
        }
        if (remaining <= 0) break
      }
    }
  }
}
