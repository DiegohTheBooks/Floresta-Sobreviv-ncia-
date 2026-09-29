package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CampStructureEntity
import com.example.data.model.FireflyParticle
import com.example.data.model.ItemType
import com.example.data.model.MobType
import com.example.data.model.ResourceNodeType
import com.example.data.model.StructureType
import com.example.data.model.WorldMob
import com.example.domain.DayNightCycleManager
import com.example.domain.OpenWorldManager
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OpenWorldGameView(
  worldManager: OpenWorldManager,
  playerX: Float,
  playerY: Float,
  playerFacingAngle: Float,
  equippedTool: ItemType?,
  structures: List<CampStructureEntity>,
  timeMinutes: Int = 480,
  isNight: Boolean = false,
  isDusk: Boolean = false,
  isRaining: Boolean = false,
  isSwinging: Boolean = false,
  swingProgress: Float = 0f,
  selectedBuildingToPlace: StructureType? = null,
  placementX: Float = 0f,
  placementY: Float = 0f,
  canPlaceHere: Boolean = true,
  onTapWorld: (Float, Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "worldAnim")
  val fireFlicker by infiniteTransition.animateFloat(
    initialValue = 0.82f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
    label = "fireFlicker"
  )

  val ambientDarkness = DayNightCycleManager.getAmbientDarkness(timeMinutes)
  val atmosphereColor = DayNightCycleManager.getAtmosphereColor(timeMinutes)

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF1B2B1E))
      .pointerInput(Unit) {
        detectTapGestures { offset ->
          onTapWorld(offset.x, offset.y)
        }
      }
      .testTag("open_world_canvas_view")
  ) {
    val screenWidth = constraints.maxWidth.toFloat()
    val screenHeight = constraints.maxHeight.toFloat()

    // Camera translates world coordinates to screen coordinates
    val cameraX = screenWidth / 2f - playerX
    val cameraY = screenHeight / 2f - playerY

    Canvas(modifier = Modifier.fillMaxSize()) {
      // 1. Draw Biome Terrains with subtle daytime/dusk tone
      drawTerrain(cameraX, cameraY, screenWidth, screenHeight, timeMinutes)

      // 2. Draw Harvestable Resource Nodes
      worldManager.resourceNodes.forEach { node ->
        if (!node.isHarvested) {
          val sx = node.x + cameraX
          val sy = node.y + cameraY
          if (sx >= -100 && sx <= screenWidth + 100 && sy >= -100 && sy <= screenHeight + 100) {
            drawResourceNode(node.type, sx, sy, node.currentHealth, node.type.maxHealth)
          }
        }
      }

      // 3. Draw Placed Base Structures (Walls, Towers, Campfires, Workbenches)
      structures.forEach { structure ->
        val sx = structure.worldX + cameraX
        val sy = structure.worldY + cameraY
        if (sx >= -120 && sx <= screenWidth + 120 && sy >= -120 && sy <= screenHeight + 120) {
          drawBaseStructure(structure, sx, sy, fireFlicker)
        }
      }

      // 4. Draw Ground Drops
      worldManager.drops.forEach { drop ->
        val sx = drop.x + cameraX
        val sy = drop.y + cameraY
        drawCircle(color = Color(0xFFFFD54F), radius = 6f, center = Offset(sx, sy))
        drawCircle(color = Color(0xFF5D4037), radius = 6f, center = Offset(sx, sy), style = Stroke(1.5f))
      }

      // 5. Draw Mobs (Peaceful Wildlife & Night Hostiles influenced by Day/Night)
      worldManager.mobs.forEach { mob ->
        if (mob.isAlive) {
          val sx = mob.x + cameraX
          val sy = mob.y + cameraY
          if (sx >= -80 && sx <= screenWidth + 80 && sy >= -80 && sy <= screenHeight + 80) {
            drawMob(mob, sx, sy)
          }
        }
      }

      // 6. Draw Arrow Projectiles
      worldManager.arrows.forEach { arrow ->
        val sx = arrow.x + cameraX
        val sy = arrow.y + cameraY
        drawLine(
          color = Color(0xFFFFF9C4),
          start = Offset(sx, sy),
          end = Offset(sx - arrow.vx * 1.5f, sy - arrow.vy * 1.5f),
          strokeWidth = 3f
        )
      }

      // 7. Draw Player Character
      val playerScreenX = screenWidth / 2f
      val playerScreenY = screenHeight / 2f
      drawPlayerCharacter(
        cx = playerScreenX,
        cy = playerScreenY,
        facingAngle = playerFacingAngle,
        equippedTool = equippedTool,
        isSwinging = isSwinging,
        swingProgress = swingProgress
      )

      // 8. Draw Ghost Structure if in Build Mode
      if (selectedBuildingToPlace != null) {
        val ghostSx = placementX + cameraX
        val ghostSy = placementY + cameraY
        val ghostColor = if (canPlaceHere) Color(0x994CAF50) else Color(0x99F44336)
        drawCircle(
          color = ghostColor,
          radius = 32f,
          center = Offset(ghostSx, ghostSy)
        )
        drawCircle(
          color = Color.White,
          radius = 32f,
          center = Offset(ghostSx, ghostSy),
          style = Stroke(width = 2.5f)
        )
      }

      // 9. Fireflies floating in the dark
      if (ambientDarkness > 0.15f) {
        worldManager.fireflies.forEach { firefly ->
          val fx = firefly.x + cameraX
          val fy = firefly.y + cameraY
          val sinVal = kotlin.math.sin(firefly.phaseOffset.toDouble()).toFloat()
          val glowAlpha = ((sinVal * 0.35f + 0.65f) * ambientDarkness).coerceIn(0f, 1f)
          drawCircle(
            color = Color(0x66CCFF90).copy(alpha = (glowAlpha * 0.5f).coerceIn(0f, 1f)),
            radius = 7f,
            center = Offset(fx, fy)
          )
          drawCircle(
            color = Color(0xFFEEFF41).copy(alpha = glowAlpha),
            radius = 2.6f,
            center = Offset(fx, fy)
          )
        }
      }

      // 10. Dynamic Day/Night Lighting Layer & Smooth Gradual Illumination
      if (ambientDarkness > 0.02f) {
        // Base smooth darkness layer
        drawRect(color = Color(0xFF030610).copy(alpha = ambientDarkness), size = size)

        // Chromatic mood overlay (sunset rose/coral, dusk purple, midnight indigo)
        drawRect(color = atmosphereColor.copy(alpha = (ambientDarkness * 0.25f)), size = size)

        // Twinkling stars if late night
        if (ambientDarkness > 0.65f) {
          val starAlpha = ((ambientDarkness - 0.65f) / 0.23f) * (fireFlicker * 0.85f)
          val starOffsets = listOf(
            Offset(screenWidth * 0.12f, screenHeight * 0.18f),
            Offset(screenWidth * 0.28f, screenHeight * 0.12f),
            Offset(screenWidth * 0.45f, screenHeight * 0.22f),
            Offset(screenWidth * 0.72f, screenHeight * 0.14f),
            Offset(screenWidth * 0.85f, screenHeight * 0.25f),
            Offset(screenWidth * 0.60f, screenHeight * 0.30f),
            Offset(screenWidth * 0.20f, screenHeight * 0.38f),
            Offset(screenWidth * 0.90f, screenHeight * 0.45f)
          )
          starOffsets.forEach { pos ->
            drawCircle(color = Color.White.copy(alpha = starAlpha.coerceIn(0f, 1f)), radius = 2f, center = pos)
          }
        }

        // A. Campfire light pools (warm radial auras with flickering flames)
        structures.filter { it.structureTypeId == "campfire" && it.fuelMinutesRemaining > 0 }.forEach { fire ->
          val fx = fire.worldX + cameraX
          val fy = fire.worldY + cameraY
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0xB3FFA726),
                Color(0x66FF7043),
                Color(0x22E65100),
                Color.Transparent
              ),
              center = Offset(fx, fy),
              radius = 250f * fireFlicker
            ),
            radius = 250f * fireFlicker,
            center = Offset(fx, fy)
          )
        }

        // B. Arrow Tower Watchlight (elevated defensive lantern illumination)
        structures.filter { it.structureTypeId == "arrow_tower" && it.isBuilt && it.health > 0 }.forEach { tower ->
          val tx = tower.worldX + cameraX
          val ty = tower.worldY + cameraY
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0x99FFD54F),
                Color(0x44FFA726),
                Color.Transparent
              ),
              center = Offset(tx, ty),
              radius = 160f
            ),
            radius = 160f,
            center = Offset(tx, ty)
          )
        }

        // C. Player Vision / Torch Light Pool
        val hasTorch = equippedTool == ItemType.TORCH
        val lightRadius = DayNightCycleManager.getPlayerVisionRadius(timeMinutes, hasTorch)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(if (hasTorch) 0xCCFFB74D else 0x99FFFFFF),
              Color(if (hasTorch) 0x66FF8A65 else 0x33B0BEC5),
              Color.Transparent
            ),
            center = Offset(playerScreenX, playerScreenY),
            radius = lightRadius
          ),
          radius = lightRadius,
          center = Offset(playerScreenX, playerScreenY)
        )
      }
    }

    // 11. Minimap Radar Widget in Corner
    MinimapRadar(
      playerX = playerX,
      playerY = playerY,
      worldManager = worldManager,
      structures = structures,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 10.dp, end = 12.dp)
    )
  }
}

fun DrawScope.drawTerrain(cameraX: Float, cameraY: Float, screenW: Float, screenH: Float, timeMinutes: Int) {
  // Base meadow grass
  drawRect(color = Color(0xFF243B25), size = size)

  // East river stream
  val riverStartX = 1650f + cameraX
  val riverW = 340f
  drawRect(
    brush = Brush.horizontalGradient(
      colors = listOf(Color(0xFF1E88E5), Color(0xFF039BE5), Color(0xFF0288D1))
    ),
    topLeft = Offset(riverStartX, 0f),
    size = Size(riverW, size.height)
  )

  // Terrain grid / paths for Krafteers aesthetic
  val step = 80f
  val startX = (cameraX % step)
  val startY = (cameraY % step)
  var x = startX
  while (x < screenW) {
    drawLine(color = Color(0x15FFFFFF), start = Offset(x, 0f), end = Offset(x, screenH), strokeWidth = 1f)
    x += step
  }
  var y = startY
  while (y < screenH) {
    drawLine(color = Color(0x15FFFFFF), start = Offset(0f, y), end = Offset(screenW, y), strokeWidth = 1f)
    y += step
  }
}

fun DrawScope.drawResourceNode(type: ResourceNodeType, sx: Float, sy: Float, health: Int, maxHealth: Int) {
  when (type) {
    ResourceNodeType.OAK_TREE -> {
      drawCircle(color = Color(0x40000000), radius = 28f, center = Offset(sx + 4f, sy + 6f))
      drawCircle(color = Color(0xFF2E6333), radius = 24f, center = Offset(sx, sy))
      drawCircle(color = Color(0xFF3B7E41), radius = 18f, center = Offset(sx - 3f, sy - 3f))
      drawCircle(color = Color(0xFF4C9653), radius = 12f, center = Offset(sx - 5f, sy - 5f))
      drawCircle(color = Color(0xFF5D4037), radius = 6f, center = Offset(sx, sy))
    }

    ResourceNodeType.PINE_TREE -> {
      drawCircle(color = Color(0x40000000), radius = 22f, center = Offset(sx + 3f, sy + 5f))
      drawCircle(color = Color(0xFF1B4D24), radius = 20f, center = Offset(sx, sy))
      drawCircle(color = Color(0xFF2E6C38), radius = 14f, center = Offset(sx, sy - 2f))
      drawCircle(color = Color(0xFF458C51), radius = 8f, center = Offset(sx, sy - 4f))
    }

    ResourceNodeType.BOULDER -> {
      drawCircle(color = Color(0x40000000), radius = 18f, center = Offset(sx + 3f, sy + 4f))
      drawRoundRect(
        color = Color(0xFF757575),
        topLeft = Offset(sx - 16f, sy - 14f),
        size = Size(32f, 26f),
        cornerRadius = CornerRadius(8f, 8f)
      )
      drawRoundRect(
        color = Color(0xFF9E9E9E),
        topLeft = Offset(sx - 13f, sy - 12f),
        size = Size(20f, 16f),
        cornerRadius = CornerRadius(6f, 6f)
      )
    }

    ResourceNodeType.FLINT_ROCK -> {
      drawCircle(color = Color(0x40000000), radius = 16f, center = Offset(sx + 3f, sy + 3f))
      drawRoundRect(
        color = Color(0xFF455A64),
        topLeft = Offset(sx - 14f, sy - 12f),
        size = Size(28f, 22f),
        cornerRadius = CornerRadius(6f, 6f)
      )
      drawCircle(color = Color(0xFF80DEEA), radius = 5f, center = Offset(sx - 2f, sy - 2f))
    }

    ResourceNodeType.BERRY_BUSH -> {
      drawCircle(color = Color(0xFF33691E), radius = 14f, center = Offset(sx, sy))
      drawCircle(color = Color(0xFFE53935), radius = 3.5f, center = Offset(sx - 4f, sy - 3f))
      drawCircle(color = Color(0xFFE53935), radius = 3.5f, center = Offset(sx + 4f, sy + 2f))
      drawCircle(color = Color(0xFFE53935), radius = 3.5f, center = Offset(sx + 1f, sy - 5f))
    }

    ResourceNodeType.HERB_PATCH -> {
      drawCircle(color = Color(0xFF2E7D32), radius = 12f, center = Offset(sx, sy))
      drawCircle(color = Color(0xFF81C784), radius = 6f, center = Offset(sx, sy))
      drawCircle(color = Color(0xFFCE93D8), radius = 2.5f, center = Offset(sx + 2f, sy - 2f))
    }

    ResourceNodeType.WATER_SOURCE -> {
      drawCircle(color = Color(0xFF0288D1), radius = 16f, center = Offset(sx, sy))
      drawCircle(color = Color(0xFF81D4FA), radius = 8f, center = Offset(sx, sy), style = Stroke(2f))
    }
  }

  // Health pip if damaged
  if (health < maxHealth) {
    val barW = 28f
    val barH = 4f
    drawRect(color = Color.Black, topLeft = Offset(sx - barW / 2, sy - 32f), size = Size(barW, barH))
    val filledW = (barW * (health.toFloat() / maxHealth)).coerceIn(0f, barW)
    drawRect(color = Color(0xFF66BB6A), topLeft = Offset(sx - barW / 2, sy - 32f), size = Size(filledW, barH))
  }
}

fun DrawScope.drawBaseStructure(structure: CampStructureEntity, sx: Float, sy: Float, fireFlicker: Float) {
  when (structure.structureTypeId) {
    "campfire" -> {
      drawCircle(color = Color(0xFF616161), radius = 22f, center = Offset(sx, sy), style = Stroke(4f))
      drawLine(color = Color(0xFF4E342E), start = Offset(sx - 14f, sy - 14f), end = Offset(sx + 14f, sy + 14f), strokeWidth = 5f)
      drawLine(color = Color(0xFF4E342E), start = Offset(sx - 14f, sy + 14f), end = Offset(sx + 14f, sy - 14f), strokeWidth = 5f)
      if (structure.fuelMinutesRemaining > 0) {
        drawCircle(color = Color(0xFFFF5722), radius = 13f * fireFlicker, center = Offset(sx, sy))
        drawCircle(color = Color(0xFFFFEB3B), radius = 7f * fireFlicker, center = Offset(sx, sy))
      }
    }

    "log_wall" -> {
      drawRoundRect(
        color = Color(0xFF4E342E),
        topLeft = Offset(sx - 20f, sy - 18f),
        size = Size(40f, 36f),
        cornerRadius = CornerRadius(4f, 4f)
      )
      drawRoundRect(
        color = Color(0xFF8D6E63),
        topLeft = Offset(sx - 18f, sy - 16f),
        size = Size(36f, 32f),
        cornerRadius = CornerRadius(3f, 3f)
      )
      drawLine(color = Color(0xFF3E2723), start = Offset(sx - 18f, sy), end = Offset(sx + 18f, sy), strokeWidth = 2f)
    }

    "arrow_tower" -> {
      drawRoundRect(
        color = Color(0xFF3E2723),
        topLeft = Offset(sx - 24f, sy - 24f),
        size = Size(48f, 48f),
        cornerRadius = CornerRadius(6f, 6f)
      )
      drawRoundRect(
        color = Color(0xFF795548),
        topLeft = Offset(sx - 20f, sy - 20f),
        size = Size(40f, 40f),
        cornerRadius = CornerRadius(4f, 4f)
      )
      drawCircle(color = Color(0xFFFFCA28), radius = 8f, center = Offset(sx, sy))
      drawCircle(color = Color(0xFF2E7D32), radius = 5f, center = Offset(sx, sy))
    }

    "workbench" -> {
      drawRoundRect(
        color = Color(0xFF6D4C41),
        topLeft = Offset(sx - 18f, sy - 14f),
        size = Size(36f, 28f),
        cornerRadius = CornerRadius(4f, 4f)
      )
      drawCircle(color = Color(0xFF90A4AE), radius = 5f, center = Offset(sx - 6f, sy - 2f))
    }

    "rain_catcher" -> {
      drawCircle(color = Color(0xFF455A64), radius = 18f, center = Offset(sx, sy))
      drawCircle(color = Color(0xFF0288D1), radius = 14f, center = Offset(sx, sy))
    }

    else -> {
      drawRoundRect(
        color = Color(0xFF5D4037),
        topLeft = Offset(sx - 26f, sy - 22f),
        size = Size(52f, 44f),
        cornerRadius = CornerRadius(6f, 6f)
      )
      drawRoundRect(
        color = Color(0xFF8D6E63),
        topLeft = Offset(sx - 22f, sy - 18f),
        size = Size(44f, 34f),
        cornerRadius = CornerRadius(4f, 4f)
      )
    }
  }
}

fun DrawScope.drawMob(mob: WorldMob, sx: Float, sy: Float) {
  val type = mob.type
  val health = mob.currentHealth
  val maxHealth = mob.type.maxHealth

  when (type) {
    MobType.DEER -> {
      if (mob.isSleeping) {
        // Deer sleeping peacefully under trees
        drawCircle(color = Color(0x35000000), radius = 13f, center = Offset(sx + 2f, sy + 3f))
        drawCircle(color = Color(0xFF795548), radius = 14f, center = Offset(sx, sy))
        drawCircle(color = Color(0xFF8D6E63), radius = 9f, center = Offset(sx + 8f, sy - 2f))
        drawLine(color = Color(0xFF3E2723), start = Offset(sx + 6f, sy - 2f), end = Offset(sx + 10f, sy - 2f), strokeWidth = 1.5f)
        drawLine(color = Color(0xFFD7CCC8), start = Offset(sx + 8f, sy - 4f), end = Offset(sx + 12f, sy - 9f), strokeWidth = 1.5f)
      } else {
        // Awake deer
        drawCircle(color = Color(0x40000000), radius = 14f, center = Offset(sx + 2f, sy + 3f))
        drawRoundRect(
          color = Color(0xFF8D6E63),
          topLeft = Offset(sx - 14f, sy - 9f),
          size = Size(28f, 18f),
          cornerRadius = CornerRadius(8f, 8f)
        )
        drawCircle(color = Color(0xFFA1887F), radius = 7f, center = Offset(sx + 12f, sy - 2f))
        drawLine(color = Color(0xFFD7CCC8), start = Offset(sx + 12f, sy - 4f), end = Offset(sx + 16f, sy - 14f), strokeWidth = 2f)
      }
    }

    MobType.RABBIT -> {
      if (mob.isSleeping) {
        drawCircle(color = Color(0x25000000), radius = 6f, center = Offset(sx + 1f, sy + 2f))
        drawCircle(color = Color(0xFFCCCCCC), radius = 6f, center = Offset(sx, sy))
      } else {
        drawCircle(color = Color(0x30000000), radius = 7f, center = Offset(sx + 1f, sy + 2f))
        drawCircle(color = Color(0xFFE0E0E0), radius = 7f, center = Offset(sx, sy))
        drawCircle(color = Color(0xFFFFFFFF), radius = 4f, center = Offset(sx + 5f, sy - 2f))
      }
    }

    MobType.NIGHT_WOLF -> {
      drawCircle(color = Color(0x60000000), radius = 16f, center = Offset(sx + 2f, sy + 3f))
      drawRoundRect(
        color = Color(0xFF263238),
        topLeft = Offset(sx - 15f, sy - 10f),
        size = Size(30f, 20f),
        cornerRadius = CornerRadius(8f, 8f)
      )
      drawCircle(color = Color(0xFF37474F), radius = 9f, center = Offset(sx + 12f, sy))
      // Predatory glowing red eyes
      drawCircle(color = Color(0xFFFF1744), radius = 2.5f, center = Offset(sx + 15f, sy - 3f))
      drawCircle(color = Color(0xFFFF1744), radius = 2.5f, center = Offset(sx + 15f, sy + 3f))
    }

    MobType.SHADOW_BEAST -> {
      drawCircle(color = Color(0x80000000), radius = 24f, center = Offset(sx, sy + 4f))
      drawCircle(color = Color(0xFF212121), radius = 22f, center = Offset(sx, sy))
      drawCircle(color = Color(0xFF4A148C), radius = 16f, center = Offset(sx, sy))
      // Glowing purple eyes
      drawCircle(color = Color(0xFFE040FB), radius = 3.5f, center = Offset(sx + 8f, sy - 4f))
      drawCircle(color = Color(0xFFE040FB), radius = 3.5f, center = Offset(sx + 8f, sy + 4f))
    }
  }

  // Health bar if damaged
  if (health < maxHealth) {
    val barW = 26f
    val barH = 3.5f
    drawRect(color = Color.Black, topLeft = Offset(sx - barW / 2, sy - 20f), size = Size(barW, barH))
    val filledW = (barW * (health / maxHealth)).coerceIn(0f, barW)
    drawRect(color = Color(0xFFE53935), topLeft = Offset(sx - barW / 2, sy - 20f), size = Size(filledW, barH))
  }
}

fun DrawScope.drawPlayerCharacter(
  cx: Float,
  cy: Float,
  facingAngle: Float,
  equippedTool: ItemType?,
  isSwinging: Boolean,
  swingProgress: Float
) {
  drawCircle(color = Color(0x55000000), radius = 16f, center = Offset(cx + 2f, cy + 4f))
  drawCircle(color = Color(0xFF3E2723), radius = 15f, center = Offset(cx, cy))
  drawCircle(color = Color(0xFF8D6E63), radius = 12f, center = Offset(cx, cy))

  drawCircle(color = Color(0xFFC68B59), radius = 9f, center = Offset(cx, cy))
  drawCircle(color = Color(0xFF2E7D32), radius = 9f, center = Offset(cx, cy), style = Stroke(3f))

  val handDistance = 18f
  val swingOffsetAngle = if (isSwinging) (swingProgress - 0.5f) * 1.5f else 0f
  val totalAngle = facingAngle + swingOffsetAngle

  val handX = cx + cos(totalAngle) * handDistance
  val handY = cy + sin(totalAngle) * handDistance
  drawCircle(color = Color(0xFFC68B59), radius = 4f, center = Offset(handX, handY))

  if (equippedTool != null) {
    val toolTipX = cx + cos(totalAngle) * (handDistance + 14f)
    val toolTipY = cy + sin(totalAngle) * (handDistance + 14f)
    val toolColor = when (equippedTool) {
      ItemType.TORCH -> Color(0xFFFF9800)
      ItemType.AXE_STEEL, ItemType.AXE_STONE -> Color(0xFF90A4AE)
      ItemType.SPEAR_FLINT, ItemType.SPEAR_WOOD -> Color(0xFF80CBC4)
      ItemType.PICKAXE_FLINT -> Color(0xFFB0BEC5)
      else -> Color(0xFFFFD54F)
    }
    drawLine(color = Color(0xFF5D4037), start = Offset(handX, handY), end = Offset(toolTipX, toolTipY), strokeWidth = 3f)
    drawCircle(color = toolColor, radius = 5f, center = Offset(toolTipX, toolTipY))
  }

  if (isSwinging) {
    val arcRadius = 32f
    drawCircle(
      color = Color(0x66FFFFFF),
      radius = arcRadius,
      center = Offset(cx, cy),
      style = Stroke(3f)
    )
  }
}

@Composable
fun MinimapRadar(
  playerX: Float,
  playerY: Float,
  worldManager: OpenWorldManager,
  structures: List<CampStructureEntity>,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(110.dp, 80.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xCC131A14))
      .border(1.dp, Color(0xFF3D5A40), RoundedCornerShape(8.dp))
      .testTag("minimap_radar")
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val mapW = size.width
      val mapH = size.height
      val scaleX = mapW / OpenWorldManager.WORLD_WIDTH
      val scaleY = mapH / OpenWorldManager.WORLD_HEIGHT

      val baseMx = OpenWorldManager.BASE_CENTER_X * scaleX
      val baseMy = OpenWorldManager.BASE_CENTER_Y * scaleY
      drawCircle(color = Color(0x334CAF50), radius = 14f, center = Offset(baseMx, baseMy))

      structures.forEach { s ->
        val mx = s.worldX * scaleX
        val my = s.worldY * scaleY
        drawCircle(color = Color(0xFFFFB74D), radius = 2f, center = Offset(mx, my))
      }

      worldManager.mobs.filter { it.type.isHostile && it.isAlive }.forEach { mob ->
        val mx = mob.x * scaleX
        val my = mob.y * scaleY
        drawCircle(color = Color(0xFFFF1744), radius = 2.5f, center = Offset(mx, my))
      }

      val pmx = playerX * scaleX
      val pmy = playerY * scaleY
      drawCircle(color = Color(0x5500E5FF), radius = 6f, center = Offset(pmx, pmy))
      drawCircle(color = Color(0xFF00E5FF), radius = 3f, center = Offset(pmx, pmy))
    }

    Text(
      text = "MAPA",
      style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF81C784),
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(horizontal = 4.dp, vertical = 2.dp)
    )
  }
}
