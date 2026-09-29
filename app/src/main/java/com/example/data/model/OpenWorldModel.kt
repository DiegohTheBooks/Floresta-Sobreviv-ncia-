package com.example.data.model

enum class ResourceNodeType(
  val displayName: String,
  val maxHealth: Int,
  val yieldsItem: ItemType,
  val secondaryYield: ItemType? = null
) {
  OAK_TREE("Carvalho Frondoso", 4, ItemType.WOOD, ItemType.STICK),
  PINE_TREE("Pinheiro Selvagem", 3, ItemType.WOOD, ItemType.RESIN),
  BOULDER("Pedregulho Mineral", 5, ItemType.STONE, ItemType.CLAY),
  FLINT_ROCK("Veio de Sílex", 4, ItemType.FLINT, ItemType.STONE),
  BERRY_BUSH("Arbusto de Amoras", 2, ItemType.BERRIES, ItemType.FIBER),
  HERB_PATCH("Canteiro de Ervas", 2, ItemType.HERB, ItemType.FIBER),
  WATER_SOURCE("Margem da Nascente", 1, ItemType.DIRTY_WATER, ItemType.CLAY)
}

enum class MobType(
  val displayName: String,
  val maxHealth: Float,
  val speed: Float,
  val isHostile: Boolean,
  val attackDamage: Float = 0f
) {
  DEER("Cervo da Floresta", 40f, 3.2f, false),
  RABBIT("Lebre Veloz", 15f, 4.0f, false),
  NIGHT_WOLF("Lobo da Noite", 50f, 2.6f, true, attackDamage = 12f),
  SHADOW_BEAST("Fera Sombria", 80f, 2.2f, true, attackDamage = 18f)
}

data class WorldResourceNode(
  val id: String,
  val type: ResourceNodeType,
  val x: Float,
  val y: Float,
  var currentHealth: Int = type.maxHealth,
  var isHarvested: Boolean = false,
  var respawnTime: Long = 0L
)

data class WorldMob(
  val id: String,
  val type: MobType,
  var x: Float,
  var y: Float,
  var targetX: Float = x,
  var targetY: Float = y,
  var currentHealth: Float = type.maxHealth,
  var isAlive: Boolean = true,
  var wanderTimer: Int = 0,
  var attackCooldown: Int = 0,
  var isSleeping: Boolean = false // Peaceful mobs sleep under trees at night
)

data class WorldArrow(
  val id: Long,
  var x: Float,
  var y: Float,
  val vx: Float,
  val vy: Float,
  val damage: Float,
  var distanceTraveled: Float = 0f,
  val maxDistance: Float = 280f
)

data class WorldDrop(
  val id: Long,
  val itemType: ItemType,
  val amount: Int,
  val x: Float,
  val y: Float
)

data class FireflyParticle(
  val id: Long,
  var x: Float,
  var y: Float,
  var baseAlpha: Float = 0.8f,
  var phaseOffset: Float = 0f,
  var vx: Float = 0f,
  var vy: Float = 0f
)
