package com.example

import com.example.data.model.ItemCategory
import com.example.data.model.ItemType
import com.example.data.model.StructureType
import com.example.domain.CraftingCatalog
import com.example.domain.OpenWorldManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SurvivalGameTest {

  @Test
  fun testCraftingCatalogRecipesAreValid() {
    assertTrue(CraftingCatalog.RECIPES.isNotEmpty())
    CraftingCatalog.RECIPES.forEach { recipe ->
      assertNotNull("Recipe must have an id", recipe.id)
      assertTrue("Recipe must require at least 1 ingredient", recipe.ingredients.isNotEmpty())
      assertTrue("Crafting time must be positive", recipe.craftingTimeSeconds > 0)
      assertTrue("Result amount must be positive", recipe.resultAmount > 0)
    }
  }

  @Test
  fun testStructureRecipesContainCoreBuildingsAndDefenses() {
    val types = CraftingCatalog.STRUCTURE_RECIPES.map { it.structureType }
    assertTrue(types.contains(StructureType.FOGUEIRA))
    assertTrue(types.contains(StructureType.MURALHA_TRONCOS))
    assertTrue(types.contains(StructureType.TORRE_FLECHAS))
    assertTrue(types.contains(StructureType.ABRIGO_FOLHAS))
    assertTrue(types.contains(StructureType.CABANA_MADEIRA))
    assertTrue(types.contains(StructureType.COLETOR_CHUVA))
    assertTrue(types.contains(StructureType.BANCADA_TRABALHO))
  }

  @Test
  fun testOpenWorldManagerGeneration() {
    val worldManager = OpenWorldManager()
    assertTrue(worldManager.resourceNodes.size > 50)
    assertTrue(worldManager.mobs.isNotEmpty())
    assertEquals(2400f, OpenWorldManager.WORLD_WIDTH, 0.1f)
    assertEquals(2400f, OpenWorldManager.WORLD_HEIGHT, 0.1f)
  }

  @Test
  fun testItemCatalogNutritionalAndThermalValues() {
    // Cooked meat should be significantly more nutritious than raw meat
    assertTrue(ItemType.COOKED_MEAT.hungerRestored > ItemType.RAW_MEAT.hungerRestored)
    // Clean water should be safe
    assertTrue(ItemType.CLEAN_WATER.thirstRestored > 0f)
    assertTrue(ItemType.CLEAN_WATER.healthRestored >= 0f)
    // Fur coat must have positive thermal effect
    assertTrue(ItemType.FUR_COAT.tempEffect > 2.0f)
  }

  @Test
  fun testToolDurabilityConfiguration() {
    assertTrue(ItemType.AXE_STONE.maxDurability > 0)
    assertTrue(ItemType.AXE_STEEL.maxDurability > ItemType.AXE_STONE.maxDurability)
    assertTrue(ItemType.PICKAXE_FLINT.maxDurability > 0)
  }
}
