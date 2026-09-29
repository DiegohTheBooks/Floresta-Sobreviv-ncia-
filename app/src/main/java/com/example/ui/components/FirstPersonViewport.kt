package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ForestZone
import com.example.data.model.ItemType
import com.example.data.model.WeatherType
import com.example.ui.SurvivalUiState
import kotlin.math.roundToInt
import kotlin.random.Random

data class ForestInteractableNode(
  val id: String,
  val name: String,
  val category: String,
  val icon: ImageVector,
  val actionPrompt: String,
  val xPositionPercent: Float, // 0.15f to 0.85f across camera view
  val yPositionPercent: Float, // 0.35f to 0.75f in field
  val toolNeededDescription: String
)

@Composable
fun FirstPersonViewport(
  state: SurvivalUiState,
  onHarvestNode: (String) -> Unit,
  onChangeZone: (ForestZone) -> Unit,
  onOpenStation: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Panoramic camera pan offset
  var cameraPanX by remember { mutableFloatStateOf(0f) }

  // Tool swing animation
  val swingAngle = remember { Animatable(0f) }
  val swingOffset = remember { Animatable(0f) }

  LaunchedEffect(state.isSwingingTool) {
    if (state.isSwingingTool) {
      swingAngle.animateTo(-45f, tween(100, easing = FastOutSlowInEasing))
      swingOffset.animateTo(25f, tween(100, easing = FastOutSlowInEasing))
      swingAngle.animateTo(20f, tween(120, easing = FastOutSlowInEasing))
      swingAngle.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
      swingOffset.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
    }
  }

  // Atmospheric ambient animations (wind foliage sway, campfire flicker, fireflies)
  val infiniteTransition = rememberInfiniteTransition(label = "ambient")
  val foliageSway by infiniteTransition.animateFloat(
    initialValue = -3f,
    targetValue = 3f,
    animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Reverse),
    label = "foliageSway"
  )
  val fireFlickerAlpha by infiniteTransition.animateFloat(
    initialValue = 0.75f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(250, easing = LinearEasing), RepeatMode.Reverse),
    label = "fireFlicker"
  )
  val rainDrift by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 100f,
    animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart),
    label = "rainDrift"
  )

  // Determine realistic background asset based on weather and time of day
  val backgroundRes = when {
    state.currentWeatherEnum == WeatherType.TEMPESTADE_CHUVA || state.currentWeatherEnum == WeatherType.NEBLINA ->
      R.drawable.img_forest_rain
    state.isNight ->
      R.drawable.img_forest_night
    else ->
      R.drawable.img_forest_day
  }

  // Sky lighting overlay colors
  val ambientOverlayColor = when {
    state.isNight -> Color(0x99050C16) // deep dark moonlit blue
    state.isDusk -> Color(0x66FF7043)  // golden amber dusk
    state.isDawn -> Color(0x44FFE082)  // pale dawn mist
    state.currentWeatherEnum == WeatherType.TEMPESTADE_CHUVA -> Color(0x661A2B3C) // storm gray
    else -> Color.Transparent
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          cameraPanX = (cameraPanX + dragAmount.x * 0.4f).coerceIn(-140f, 140f)
        }
      }
      .testTag("first_person_viewport")
  ) {
    val screenWidth = maxWidth
    val screenHeight = maxHeight

    // 1. Realistic First-Person Environment Background (Scaled and Panned)
    Image(
      painter = painterResource(id = backgroundRes),
      contentDescription = "Cenário da floresta em primeira pessoa",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .offset { IntOffset(x = cameraPanX.roundToInt(), y = 0) }
        .scale(1.22f)
        .rotate(foliageSway * 0.2f)
    )

    // 2. Dynamic Lighting & Atmospheric Color Grade Overlay
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(ambientOverlayColor)
    )

    // 3. Campfire Glow Aura (if in camp with active fire)
    if (state.currentZoneEnum == ForestZone.ACAMPAMENTO && state.hasCampfireBurning) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .alpha(fireFlickerAlpha * 0.35f)
          .background(
            Brush.radialGradient(
              colors = listOf(
                Color(0x88FF7043),
                Color(0x33FFB300),
                Color.Transparent
              ),
              radius = 900f
            )
          )
      )
    }

    // 4. Rain & Fog Particle Simulation (if raining or foggy)
    if (state.currentWeatherEnum == WeatherType.TEMPESTADE_CHUVA) {
      RainAtmosphereOverlay(rainDrift = rainDrift)
    } else if (state.currentWeatherEnum == WeatherType.NEBLINA) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0x44B0BEC5),
                Color(0x66CFD8DC),
                Color(0x22ECEFF1)
              )
            )
          )
      )
    }

    // 5. Frost Vignette if shivering / hypothermic (< 35.5°C)
    if (state.profile.isShivering || state.profile.bodyTemp < 35.5f) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.radialGradient(
              colors = listOf(
                Color.Transparent,
                Color.Transparent,
                Color(0x5580DEEA),
                Color(0xAA0288D1)
              )
            )
          )
      )
    }

    // 6. Interactive 3D Nodes in the First-Person Field
    val interactables = getInteractablesForZone(state)
    interactables.forEach { node ->
      val nodeX = (screenWidth.value * node.xPositionPercent + cameraPanX * 0.7f).dp
      val nodeY = (screenHeight.value * node.yPositionPercent).dp

      Box(
        modifier = Modifier
          .offset(x = nodeX, y = nodeY)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xD9162118))
          .border(1.dp, Color(0xFF4A684E), RoundedCornerShape(12.dp))
          .clickable {
            if (node.id == "camp_station") {
              onOpenStation()
            } else {
              onHarvestNode(node.id)
            }
          }
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .testTag("node_${node.id}")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(30.dp)
              .clip(CircleShape)
              .background(Color(0xFF283A2A)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = node.icon,
              contentDescription = node.name,
              tint = Color(0xFFA5E6A9),
              modifier = Modifier.size(18.dp)
            )
          }
          Column {
            Text(
              text = node.name,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = node.actionPrompt,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = Color(0xFFFFCA28)
            )
          }
        }
      }
    }

    // 7. Zone Fast Travel Compass Bar (Top-Center of Viewport)
    Row(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 10.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xCC101811))
        .border(1.dp, Color(0xFF324634), RoundedCornerShape(20.dp))
        .padding(horizontal = 6.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      ForestZone.values().forEach { zone ->
        val isSelected = state.currentZoneEnum == zone
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF2E5A34) else Color.Transparent)
            .clickable { onChangeZone(zone) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("zone_button_${zone.name}")
        ) {
          Text(
            text = when (zone) {
              ForestZone.ACAMPAMENTO -> "Acampamento"
              ForestZone.FLORESTA_PROFUNDA -> "Mata Fechada"
              ForestZone.RIACHO -> "Riacho"
              ForestZone.CLAREIRA_ROCHOSA -> "Rochedos"
            },
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else Color(0xFF9E9E9E),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
          )
        }
      }
    }

    // 8. Floating Resource Loot Texts
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .offset(y = (-60).dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      state.floatingMessages.forEach { msg ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (msg.isPositive) Color(0xEE1E3E23) else Color(0xEE4E1D1D),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (msg.isPositive) Color(0xFF66BB6A) else Color(0xFFEF5350)
          ),
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = msg.text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    // 9. Realistic First-Person Hand & Equipped Tool (Bottom-Right)
    FirstPersonEquippedHand(
      equippedItem = state.equippedItem,
      isSwinging = state.isSwingingTool,
      swingAngle = swingAngle.value,
      swingOffset = swingOffset.value,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .offset(x = 10.dp, y = 20.dp)
    )

    // 10. Screen Edge Hint for Panning
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 70.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0x88000000))
        .padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.ChevronLeft,
        contentDescription = null,
        tint = Color(0xFFAAAAAA),
        modifier = Modifier.size(14.dp)
      )
      Text(
        text = "Arraste para olhar ao redor • Toque nos nós para interagir",
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = Color(0xFFDDDDDD)
      )
      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = Color(0xFFAAAAAA),
        modifier = Modifier.size(14.dp)
      )
    }
  }
}

@Composable
fun FirstPersonEquippedHand(
  equippedItem: ItemType?,
  isSwinging: Boolean,
  swingAngle: Float,
  swingOffset: Float,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(180.dp)
      .offset { IntOffset(x = (-swingOffset).roundToInt(), y = (swingOffset).roundToInt()) }
      .rotate(swingAngle)
      .testTag("equipped_hand_view"),
    contentAlignment = Alignment.BottomEnd
  ) {
    // Hand Base Graphic
    Box(
      modifier = Modifier
        .size(130.dp, 100.dp)
        .rotate(-25f)
        .offset(x = 25.dp, y = 25.dp)
        .clip(RoundedCornerShape(30.dp))
        .background(
          Brush.linearGradient(
            colors = listOf(
              Color(0xFFC68B59), // skin tone
              Color(0xFF8D5524),
              Color(0xFF5A3515)
            )
          )
        )
    )

    // Tool Render in Hand
    if (equippedItem != null) {
      val toolIcon = when (equippedItem) {
        ItemType.AXE_STONE, ItemType.AXE_STEEL -> Icons.Default.Build
        ItemType.TORCH -> Icons.Default.LocalFireDepartment
        ItemType.PICKAXE_FLINT -> Icons.Default.Terrain
        ItemType.SPEAR_WOOD, ItemType.SPEAR_FLINT -> Icons.Default.Nature
        ItemType.LEATHER_CANTEEN -> Icons.Default.Water
        else -> Icons.Default.PanTool
      }

      val toolTint = when (equippedItem) {
        ItemType.TORCH -> Color(0xFFFF9800)
        ItemType.AXE_STEEL -> Color(0xFF90CAF9)
        ItemType.AXE_STONE -> Color(0xFFBCAAA4)
        ItemType.SPEAR_FLINT -> Color(0xFF80CBC4)
        else -> Color(0xFFFFD54F)
      }

      Box(
        modifier = Modifier
          .size(90.dp)
          .offset(x = (-20).dp, y = (-20).dp)
          .clip(CircleShape)
          .background(Color(0xCC1A231C))
          .border(2.dp, toolTint, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = toolIcon,
          contentDescription = equippedItem.displayName,
          tint = toolTint,
          modifier = Modifier.size(54.dp)
        )
      }
    }
  }
}

@Composable
fun RainAtmosphereOverlay(rainDrift: Float) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0x551C2C3D),
            Color(0x77233B54),
            Color(0x33101E2B)
          )
        )
      )
  ) {
    // Subtle animated rain streaks
    Column(
      modifier = Modifier
        .fillMaxSize()
        .offset { IntOffset(x = 0, y = (rainDrift * 3).roundToInt()) }
        .alpha(0.35f),
      verticalArrangement = Arrangement.SpaceAround
    ) {
      repeat(12) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          repeat(8) {
            Box(
              modifier = Modifier
                .width(1.5.dp)
                .height(18.dp)
                .rotate(15f)
                .background(Color(0xFFB0E0E6))
            )
          }
        }
      }
    }
  }
}

fun getInteractablesForZone(state: SurvivalUiState): List<ForestInteractableNode> {
  return when (state.currentZoneEnum) {
    ForestZone.ACAMPAMENTO -> {
      val nodes = mutableListOf<ForestInteractableNode>()
      // Built structures
      nodes.add(
        ForestInteractableNode(
          id = "camp_station",
          name = "Gerenciar Acampamento",
          category = "Estrutura",
          icon = Icons.Default.LocalFireDepartment,
          actionPrompt = if (state.hasCampfireBurning) "Fogo Ativo • Cozinhar & Descansar" else "Fogo Apagado • Alimentar",
          xPositionPercent = 0.40f,
          yPositionPercent = 0.52f,
          toolNeededDescription = "Madeira ou gravetos para acender"
        )
      )
      nodes.add(
        ForestInteractableNode(
          id = "tree_pine",
          name = "Pinheiro do Acampamento",
          category = "Madeira & Resina",
          icon = Icons.Default.Forest,
          actionPrompt = "Coletar Troncos & Resina",
          xPositionPercent = 0.12f,
          yPositionPercent = 0.38f,
          toolNeededDescription = "Machado acelera o corte"
        )
      )
      nodes.add(
        ForestInteractableNode(
          id = "bush_berry",
          name = "Arbusto de Amoras",
          category = "Alimento",
          icon = Icons.Default.Grass,
          actionPrompt = "Colher Frutas & Fibras",
          xPositionPercent = 0.72f,
          yPositionPercent = 0.44f,
          toolNeededDescription = "Colheita manual livre"
        )
      )
      nodes
    }

    ForestZone.FLORESTA_PROFUNDA -> {
      listOf(
        ForestInteractableNode(
          id = "tree_oak",
          name = "Carvalho Centenário",
          category = "Madeira Nobre",
          icon = Icons.Default.Forest,
          actionPrompt = "Derrubar Árvore",
          xPositionPercent = 0.22f,
          yPositionPercent = 0.40f,
          toolNeededDescription = "Requer Machado para maior rendimento"
        ),
        ForestInteractableNode(
          id = "hunt_deer",
          name = "Cervo da Floresta",
          category = "Caça & Peles",
          icon = Icons.Default.DirectionsWalk,
          actionPrompt = "Abater Presa (Lança/Arco)",
          xPositionPercent = 0.55f,
          yPositionPercent = 0.50f,
          toolNeededDescription = "Exige Lança ou Arco equipado"
        ),
        ForestInteractableNode(
          id = "bush_berry",
          name = "Arbusto Selvagem",
          category = "Alimento",
          icon = Icons.Default.Grass,
          actionPrompt = "Colher Frutas Silvestres",
          xPositionPercent = 0.78f,
          yPositionPercent = 0.42f,
          toolNeededDescription = "Livre"
        )
      )
    }

    ForestZone.RIACHO -> {
      listOf(
        ForestInteractableNode(
          id = "river",
          name = "Margem do Riacho",
          category = "Água & Argila",
          icon = Icons.Default.Water,
          actionPrompt = "Coletar Água & Argila",
          xPositionPercent = 0.30f,
          yPositionPercent = 0.54f,
          toolNeededDescription = "Ferva a água na fogueira antes de beber!"
        ),
        ForestInteractableNode(
          id = "boulder",
          name = "Seixos e Pedregulhos",
          category = "Minerais",
          icon = Icons.Default.Terrain,
          actionPrompt = "Minerar Pedras e Sílex",
          xPositionPercent = 0.65f,
          yPositionPercent = 0.48f,
          toolNeededDescription = "Picareta de Sílex para maior extração"
        )
      )
    }

    ForestZone.CLAREIRA_ROCHOSA -> {
      listOf(
        ForestInteractableNode(
          id = "boulder",
          name = "Paredão de Sílex",
          category = "Minerais Raros",
          icon = Icons.Default.Terrain,
          actionPrompt = "Quebrar Sílex e Rochas",
          xPositionPercent = 0.25f,
          yPositionPercent = 0.42f,
          toolNeededDescription = "Use Picareta de Sílex"
        ),
        ForestInteractableNode(
          id = "herb_patch",
          name = "Canteiro de Ervas Medicinais",
          category = "Medicina",
          icon = Icons.Default.Grass,
          actionPrompt = "Colher Ervas & Raízes",
          xPositionPercent = 0.68f,
          yPositionPercent = 0.52f,
          toolNeededDescription = "Usado para curar ferimentos e fazer chá"
        )
      )
    }
  }
}
