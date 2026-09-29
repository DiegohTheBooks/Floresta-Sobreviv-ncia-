package com.example.domain

import com.example.data.model.CampStructureEntity
import com.example.data.model.FireflyParticle
import com.example.data.model.ItemType
import com.example.data.model.MobType
import com.example.data.model.ResourceNodeType
import com.example.data.model.StructureType
import com.example.data.model.WorldArrow
import com.example.data.model.WorldDrop
import com.example.data.model.WorldMob
import com.example.data.model.WorldResourceNode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class OpenWorldManager {

  companion object {
    const val WORLD_WIDTH = 2400f
    const val WORLD_HEIGHT = 2400f
    const val BASE_CENTER_X = 1200f
    const val BASE_CENTER_Y = 1200f
  }

  val resourceNodes = mutableListOf<WorldResourceNode>()
  val mobs = mutableListOf<WorldMob>()
  val arrows = mutableListOf<WorldArrow>()
  val drops = mutableListOf<WorldDrop>()
  val fireflies = mutableListOf<FireflyParticle>()

  private var nextArrowId = 1L
  private var nextDropId = 1L
  private var nextFireflyId = 1L

  init {
    generateWorld()
  }

  fun generateWorld() {
    resourceNodes.clear()
    mobs.clear()
    arrows.clear()
    drops.clear()
    fireflies.clear()

    var nodeId = 1

    // 1. Safe Camp Area: a few trees and bushes nearby
    val campTrees = listOf(
      Pair(1080f, 1120f), Pair(1320f, 1140f), Pair(1150f, 1310f), Pair(1280f, 1290f)
    )
    campTrees.forEach { (x, y) ->
      resourceNodes.add(WorldResourceNode("node_${nodeId++}", ResourceNodeType.PINE_TREE, x, y))
    }
    resourceNodes.add(WorldResourceNode("node_${nodeId++}", ResourceNodeType.BERRY_BUSH, 1140f, 1080f))
    resourceNodes.add(WorldResourceNode("node_${nodeId++}", ResourceNodeType.BOULDER, 1290f, 1070f))

    // 2. West Biome: Dense Oak & Pine Forest (x: 200..950, y: 300..2100)
    for (i in 0 until 45) {
      val x = Random.nextFloat() * 750f + 200f
      val y = Random.nextFloat() * 1800f + 300f
      val type = if (Random.nextBoolean()) ResourceNodeType.OAK_TREE else ResourceNodeType.PINE_TREE
      resourceNodes.add(WorldResourceNode("node_${nodeId++}", type, x, y))
    }
    for (i in 0 until 18) {
      val x = Random.nextFloat() * 750f + 200f
      val y = Random.nextFloat() * 1800f + 300f
      resourceNodes.add(WorldResourceNode("node_${nodeId++}", ResourceNodeType.BERRY_BUSH, x, y))
    }

    // 3. North Biome: Rocky Hills & Flint Quarry (x: 500..1900, y: 200..750)
    for (i in 0 until 28) {
      val x = Random.nextFloat() * 1400f + 500f
      val y = Random.nextFloat() * 550f + 200f
      val type = if (Random.nextInt(100) < 60) ResourceNodeType.BOULDER else ResourceNodeType.FLINT_ROCK
      resourceNodes.add(WorldResourceNode("node_${nodeId++}", type, x, y))
    }
    for (i in 0 until 12) {
      val x = Random.nextFloat() * 1400f + 500f
      val y = Random.nextFloat() * 550f + 200f
      resourceNodes.add(WorldResourceNode("node_${nodeId++}", ResourceNodeType.PINE_TREE, x, y))
    }

    // 4. East Biome: Riverbank & Water Basin (x: 1600..2250, y: 700..1900)
    for (i in 0 until 18) {
      val x = Random.nextFloat() * 650f + 1600f
      val y = Random.nextFloat() * 1200f + 700f
      resourceNodes.add(WorldResourceNode("node_${nodeId++}", ResourceNodeType.WATER_SOURCE, x, y))
    }
    for (i in 0 until 15) {
      val x = Random.nextFloat() * 650f + 1600f
      val y = Random.nextFloat() * 1200f + 700f
      resourceNodes.add(WorldResourceNode("node_${nodeId++}", ResourceNodeType.HERB_PATCH, x, y))
    }

    // 5. South Biome: Ancient Obelisk & Dark Jungle (x: 400..2000, y: 1600..2250)
    for (i in 0 until 25) {
      val x = Random.nextFloat() * 1600f + 400f
      val y = Random.nextFloat() * 650f + 1600f
      val type = when (Random.nextInt(3)) {
        0 -> ResourceNodeType.OAK_TREE
        1 -> ResourceNodeType.HERB_PATCH
        else -> ResourceNodeType.FLINT_ROCK
      }
      resourceNodes.add(WorldResourceNode("node_${nodeId++}", type, x, y))
    }

    // Initial Peaceful Wildlife
    for (i in 0 until 7) {
      val x = Random.nextFloat() * 1600f + 400f
      val y = Random.nextFloat() * 1600f + 400f
      mobs.add(WorldMob("deer_$i", MobType.DEER, x, y))
    }
    for (i in 0 until 8) {
      val x = Random.nextFloat() * 1600f + 400f
      val y = Random.nextFloat() * 1600f + 400f
      mobs.add(WorldMob("rabbit_$i", MobType.RABBIT, x, y))
    }
  }

  fun updateWorldSimulation(
    timeMinutes: Int,
    isNight: Boolean,
    playerX: Float,
    playerY: Float,
    structures: List<CampStructureEntity>,
    onPlayerAttacked: (Float) -> Unit,
    onLootCollected: (ItemType, Int) -> Unit
  ) {
    val dayPhase = DayNightCycleManager.getDayPhase(timeMinutes)
    val isDark = dayPhase.isDark

    // 1. Firefly swarm simulation during dusk and night
    if (isDark) {
      if (fireflies.size < 35 && Random.nextInt(100) < 30) {
        val angle = Random.nextFloat() * 6.28f
        val dist = Random.nextFloat() * 450f + 60f
        val fx = (playerX + cos(angle) * dist).coerceIn(60f, WORLD_WIDTH - 60f)
        val fy = (playerY + sin(angle) * dist).coerceIn(60f, WORLD_HEIGHT - 60f)
        fireflies.add(
          FireflyParticle(
            id = nextFireflyId++,
            x = fx,
            y = fy,
            baseAlpha = Random.nextFloat() * 0.4f + 0.6f,
            phaseOffset = Random.nextFloat() * 6.28f,
            vx = Random.nextFloat() * 0.8f - 0.4f,
            vy = Random.nextFloat() * 0.8f - 0.4f
          )
        )
      }
      fireflies.forEach { f ->
        f.x += f.vx
        f.y += f.vy
        f.phaseOffset += 0.08f
      }
    } else {
      if (fireflies.isNotEmpty()) fireflies.clear()
    }

    // 2. Spawn Night Beasts if Dark and mob count low
    if (isDark) {
      val hostileCount = mobs.count { it.type.isHostile && it.isAlive }
      if (hostileCount < 6 && Random.nextInt(100) < 15) {
        val angle = Random.nextFloat() * 6.28f
        val distance = Random.nextFloat() * 400f + 500f
        val spawnX = (playerX + cos(angle) * distance).coerceIn(100f, WORLD_WIDTH - 100f)
        val spawnY = (playerY + sin(angle) * distance).coerceIn(100f, WORLD_HEIGHT - 100f)
        val type = if (Random.nextInt(100) < 70) MobType.NIGHT_WOLF else MobType.SHADOW_BEAST
        mobs.add(WorldMob("beast_${System.currentTimeMillis()}_${Random.nextInt(1000)}", type, spawnX, spawnY))
      }
    } else {
      // During daytime, nighttime beasts take burn damage or retreat to shadow dens
      mobs.filter { it.type.isHostile && it.isAlive }.forEach { beast ->
        beast.currentHealth -= 0.1f
        if (beast.currentHealth <= 0f) beast.isAlive = false
      }
    }

    // Active campfires for light/repel check
    val burningCampfires = structures.filter { it.structureTypeId == "campfire" && it.fuelMinutesRemaining > 0 }
    val arrowTowers = structures.filter { it.structureTypeId == "arrow_tower" && it.isBuilt && it.health > 0 }

    // 3. Mob AI & Movement influenced by day/night cycle
    mobs.removeAll { !it.isAlive && it.currentHealth <= 0f }
    mobs.forEach { mob ->
      if (!mob.isAlive) return@forEach

      if (mob.type.isHostile) {
        // Hostiles flee from campfire bright light
        var nearFire = false
        for (fire in burningCampfires) {
          val distToFire = distance(mob.x, mob.y, fire.worldX, fire.worldY)
          if (distToFire < 220f) {
            nearFire = true
            // Run away from fire
            val awayAngle = atan2(mob.y - fire.worldY, mob.x - fire.worldX)
            mob.x = (mob.x + cos(awayAngle) * mob.type.speed).coerceIn(50f, WORLD_WIDTH - 50f)
            mob.y = (mob.y + sin(awayAngle) * mob.type.speed).coerceIn(50f, WORLD_HEIGHT - 50f)
            break
          }
        }

        if (!nearFire) {
          // Hunt player
          val distToPlayer = distance(mob.x, mob.y, playerX, playerY)
          if (distToPlayer < 450f) {
            val chaseAngle = atan2(playerY - mob.y, playerX - mob.x)
            mob.x = (mob.x + cos(chaseAngle) * mob.type.speed).coerceIn(50f, WORLD_WIDTH - 50f)
            mob.y = (mob.y + sin(chaseAngle) * mob.type.speed).coerceIn(50f, WORLD_HEIGHT - 50f)

            if (distToPlayer < 38f) {
              if (mob.attackCooldown <= 0) {
                onPlayerAttacked(mob.type.attackDamage)
                mob.attackCooldown = 30 // ~1.5s
              }
            }
          }
        }
      } else {
        // Peaceful animals behavior influenced by day/night
        val distToPlayer = distance(mob.x, mob.y, playerX, playerY)
        if (isDark) {
          // At night, peaceful animals sleep unless player approaches very close
          if (distToPlayer < 55f) {
            mob.isSleeping = false
            val fleeAngle = atan2(mob.y - playerY, mob.x - playerX)
            mob.x = (mob.x + cos(fleeAngle) * mob.type.speed * 1.3f).coerceIn(50f, WORLD_WIDTH - 50f)
            mob.y = (mob.y + sin(fleeAngle) * mob.type.speed * 1.3f).coerceIn(50f, WORLD_HEIGHT - 50f)
          } else {
            mob.isSleeping = true
          }
        } else {
          mob.isSleeping = false
          if (distToPlayer < 110f) {
            val fleeAngle = atan2(mob.y - playerY, mob.x - playerX)
            mob.x = (mob.x + cos(fleeAngle) * mob.type.speed).coerceIn(50f, WORLD_WIDTH - 50f)
            mob.y = (mob.y + sin(fleeAngle) * mob.type.speed).coerceIn(50f, WORLD_HEIGHT - 50f)
          } else {
            // Random wander & grazing
            mob.wanderTimer--
            if (mob.wanderTimer <= 0) {
              mob.targetX = (mob.x + Random.nextFloat() * 120f - 60f).coerceIn(100f, WORLD_WIDTH - 100f)
              mob.targetY = (mob.y + Random.nextFloat() * 120f - 60f).coerceIn(100f, WORLD_HEIGHT - 100f)
              mob.wanderTimer = Random.nextInt(40, 100)
            }
            val moveAngle = atan2(mob.targetY - mob.y, mob.targetX - mob.x)
            mob.x += cos(moveAngle) * 0.8f
            mob.y += sin(moveAngle) * 0.8f
          }
        }
      }

      if (mob.attackCooldown > 0) mob.attackCooldown--
    }

    // 3. Arrow Defense Towers: Auto-target hostile mobs in range!
    arrowTowers.forEach { tower ->
      val target = mobs.firstOrNull { it.type.isHostile && it.isAlive && distance(tower.worldX, tower.worldY, it.x, it.y) < 240f }
      if (target != null && Random.nextInt(100) < 25) { // shoots every ~0.8s
        val angle = atan2(target.y - tower.worldY, target.x - tower.worldX)
        val speed = 9f
        arrows.add(
          WorldArrow(
            id = nextArrowId++,
            x = tower.worldX,
            y = tower.worldY,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            damage = 25f
          )
        )
      }
    }

    // 4. Update Arrows
    val arrowsToRemove = mutableListOf<WorldArrow>()
    arrows.forEach { arrow ->
      arrow.x += arrow.vx
      arrow.y += arrow.vy
      arrow.distanceTraveled += sqrt(arrow.vx * arrow.vx + arrow.vy * arrow.vy)

      if (arrow.distanceTraveled >= arrow.maxDistance) {
        arrowsToRemove.add(arrow)
      } else {
        // Check collision with mobs
        for (mob in mobs) {
          if (mob.isAlive && distance(arrow.x, arrow.y, mob.x, mob.y) < 28f) {
            mob.currentHealth -= arrow.damage
            arrowsToRemove.add(arrow)
            if (mob.currentHealth <= 0f) {
              mob.isAlive = false
              // Drop loot
              if (mob.type.isHostile) {
                drops.add(WorldDrop(nextDropId++, ItemType.HIDE, 2, mob.x, mob.y))
                drops.add(WorldDrop(nextDropId++, ItemType.RAW_MEAT, 2, mob.x, mob.y))
              }
            }
            break
          }
        }
      }
    }
    arrows.removeAll(arrowsToRemove)

    // 5. Collect Nearby Drops
    val collectedDrops = mutableListOf<WorldDrop>()
    drops.forEach { drop ->
      if (distance(playerX, playerY, drop.x, drop.y) < 45f) {
        onLootCollected(drop.itemType, drop.amount)
        collectedDrops.add(drop)
      }
    }
    drops.removeAll(collectedDrops)

    // 6. Respawn Harvested Nodes
    val now = System.currentTimeMillis()
    resourceNodes.forEach { node ->
      if (node.isHarvested && now >= node.respawnTime) {
        node.isHarvested = false
        node.currentHealth = node.type.maxHealth
      }
    }
  }

  fun harvestNearestNode(
    playerX: Float,
    playerY: Float,
    equippedTool: ItemType?,
    onLoot: (ItemType, Int) -> Unit
  ): Boolean {
    // 1. First check if any mob is in melee reach (~55px)
    val targetMob = mobs.firstOrNull { it.isAlive && distance(playerX, playerY, it.x, it.y) < 55f }
    if (targetMob != null) {
      val weaponDamage = when (equippedTool) {
        ItemType.SPEAR_FLINT -> 35f
        ItemType.SPEAR_WOOD -> 22f
        ItemType.AXE_STEEL -> 28f
        ItemType.AXE_STONE -> 16f
        ItemType.KNIFE_STONE -> 18f
        else -> 8f
      }
      targetMob.currentHealth -= weaponDamage
      if (targetMob.currentHealth <= 0f) {
        targetMob.isAlive = false
        if (targetMob.type == MobType.DEER) {
          onLoot(ItemType.RAW_MEAT, 3)
          onLoot(ItemType.HIDE, 2)
        } else if (targetMob.type == MobType.RABBIT) {
          onLoot(ItemType.RAW_MEAT, 1)
          onLoot(ItemType.HIDE, 1)
        } else {
          onLoot(ItemType.HIDE, 2)
          onLoot(ItemType.RAW_MEAT, 2)
        }
      }
      return true
    }

    // 2. Next check nearest active resource node
    val targetNode = resourceNodes
      .filter { !it.isHarvested }
      .minByOrNull { distance(playerX, playerY, it.x, it.y) }

    if (targetNode != null && distance(playerX, playerY, targetNode.x, targetNode.y) < 65f) {
      targetNode.currentHealth--
      // Bonus yields based on equipped tool
      val isAxe = equippedTool == ItemType.AXE_STONE || equippedTool == ItemType.AXE_STEEL
      val isPick = equippedTool == ItemType.PICKAXE_FLINT

      val mainYield = targetNode.type.yieldsItem
      val amount = when (targetNode.type) {
        ResourceNodeType.OAK_TREE, ResourceNodeType.PINE_TREE -> if (isAxe) 2 else 1
        ResourceNodeType.BOULDER, ResourceNodeType.FLINT_ROCK -> if (isPick) 2 else 1
        else -> 1
      }
      onLoot(mainYield, amount)

      // Secondary yield chance
      targetNode.type.secondaryYield?.let { secondary ->
        if (Random.nextInt(100) < 45) {
          onLoot(secondary, 1)
        }
      }

      if (targetNode.currentHealth <= 0) {
        targetNode.isHarvested = true
        targetNode.respawnTime = System.currentTimeMillis() + 60_000L // respawn in 60s
        onLoot(mainYield, 2) // completion bonus
      }
      return true
    }

    return false
  }

  fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    return sqrt(dx * dx + dy * dy)
  }
}
