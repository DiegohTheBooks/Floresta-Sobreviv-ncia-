package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.ItemType
import com.example.data.model.WeatherType
import com.example.ui.ActiveScreenDialog
import com.example.ui.SurvivalUiState
import com.example.ui.vitals.VitalsProgressBarsView
import com.example.ui.vitals.VitalsUiState
import com.example.ui.theme.FrostCold80
import com.example.ui.theme.VitalHealth
import com.example.ui.theme.VitalHunger
import com.example.ui.theme.VitalStamina
import com.example.ui.theme.VitalTempNormal
import com.example.ui.theme.VitalThirst

@Composable
fun LandscapeGameHud(
  state: SurvivalUiState,
  onOpenDialog: (ActiveScreenDialog) -> Unit,
  onAttackAction: () -> Unit,
  onConfirmPlacement: () -> Unit,
  onCancelPlacement: () -> Unit,
  onUseItem: (InventoryItemEntity) -> Unit,
  onMoveJoystick: (Float, Float, Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "hudPulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
    label = "pulseAlpha"
  )

  Box(modifier = modifier.fillMaxSize()) {
    // 1. TOP-LEFT: Vitals Progress Bars HUD (Fome, Sede, Energia, Saúde, Temperatura)
    val vitalsState = VitalsUiState(
      health = state.profile.health,
      hunger = state.profile.hunger,
      thirst = state.profile.thirst,
      energy = state.profile.stamina,
      bodyTemp = state.profile.bodyTemp,
      isStarving = state.profile.hunger < 20f,
      isDehydrated = state.profile.thirst < 20f,
      isExhausted = state.profile.stamina < 20f,
      isCriticallyInjured = state.profile.health < 25f,
      isShivering = state.profile.isShivering
    )

    VitalsProgressBarsView(
      vitals = vitalsState,
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(top = 8.dp, start = 10.dp)
    )

    // 2. TOP-CENTER: Celestial Sun/Moon Dial & Threat Warning
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 6.dp)
    ) {
      CelestialDialView(
        timeMinutes = state.profile.timeMinutes,
        dayNumber = state.profile.daysSurvived
      )

      // Night Threat Warning
      if (state.isNight) {
        Spacer(modifier = Modifier.height(3.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCC3E1A1A))
            .border(1.dp, Color(0xFFE53935).copy(alpha = pulseAlpha), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "🌙 Noite: Feras rondam nas sombras! Fique perto da fogueira.",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = Color(0xFFFF8A80),
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // 3. TOP-RIGHT: Menu Navigation Buttons
    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 10.dp, end = 125.dp) // Leave space for minimap
    ) {
      HudMenuButton(
        icon = Icons.Default.Inventory2,
        label = "Mochila",
        badgeCount = state.inventory.size,
        color = Color(0xFF81C784),
        testTag = "hud_btn_inventory",
        onClick = { onOpenDialog(ActiveScreenDialog.INVENTORY) }
      )
      HudMenuButton(
        icon = Icons.Default.Build,
        label = "Fabricar",
        color = Color(0xFFFFB74D),
        testTag = "hud_btn_craft",
        onClick = { onOpenDialog(ActiveScreenDialog.CRAFTING) }
      )
      HudMenuButton(
        icon = Icons.Default.Home,
        label = "Construir",
        color = Color(0xFF80DEEA),
        testTag = "hud_btn_build",
        onClick = { onOpenDialog(ActiveScreenDialog.BUILDING) }
      )
      HudMenuButton(
        icon = Icons.Default.LocalFireDepartment,
        label = "Fogo",
        color = if (state.hasCampfireBurning) Color(0xFFFF7043) else Color(0xFF90A4AE),
        testTag = "hud_btn_camp",
        onClick = { onOpenDialog(ActiveScreenDialog.CAMP_STATION) }
      )
      HudMenuButton(
        icon = Icons.Default.Book,
        label = "Guia",
        color = Color(0xFFCE93D8),
        testTag = "hud_btn_journal",
        onClick = { onOpenDialog(ActiveScreenDialog.JOURNAL) }
      )
    }

    // 4. BOTTOM-LEFT: Virtual Analog Joystick
    VirtualJoystick(
      onMove = onMoveJoystick,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(bottom = 14.dp, start = 16.dp)
    )

    // 5. BOTTOM-CENTER: Quick Hotbar
    LandscapeQuickHotbar(
      state = state,
      onUseItem = onUseItem,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 10.dp)
    )

    // 6. BOTTOM-RIGHT: Action & Attack Controls / Build Confirmation
    if (state.selectedBuildingToPlace != null) {
      // Build Placement Controls
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(bottom = 16.dp, end = 20.dp)
      ) {
        Button(
          onClick = onCancelPlacement,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
          modifier = Modifier.testTag("btn_cancel_placement")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Cancelar", modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Cancelar", fontSize = 11.sp)
        }

        Button(
          onClick = onConfirmPlacement,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
          modifier = Modifier.testTag("btn_confirm_placement")
        ) {
          Icon(Icons.Default.Check, contentDescription = "Construir", modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Erguer Estrutura", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
      }
    } else {
      // Action / Attack Button
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(bottom = 16.dp, end = 20.dp)
      ) {
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32), Color(0xFF1B5E20))
              )
            )
            .border(2.dp, Color.White, CircleShape)
            .clickable { onAttackAction() }
            .testTag("action_attack_button"),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val actionIcon = when (state.equippedItem) {
              ItemType.AXE_STONE, ItemType.AXE_STEEL -> Icons.Default.Build
              ItemType.PICKAXE_FLINT -> Icons.Default.Terrain
              ItemType.TORCH -> Icons.Default.LocalFireDepartment
              else -> Icons.Default.PanTool
            }
            Icon(
              imageVector = actionIcon,
              contentDescription = "Golpear / Coletar",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
            Text(
              text = "AÇÃO",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 9.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}

@Composable
fun CompactLandscapeVitals(
  state: SurvivalUiState,
  pulseAlpha: Float,
  modifier: Modifier = Modifier
) {
  val p = state.profile
  val tempColor = when {
    p.bodyTemp < 35.0f -> Color(0xFF29B6F6)
    p.bodyTemp < 36.0f -> Color(0xFF81D4FA)
    p.bodyTemp in 36.0f..37.4f -> VitalTempNormal
    else -> Color(0xFFFF7043)
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xDD121A13),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E4031)),
    modifier = modifier.testTag("landscape_vitals")
  ) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
      // Row 1: Health & Temp
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        MiniVitalBar(
          label = "Vida",
          fraction = p.health / 100f,
          color = VitalHealth,
          icon = Icons.Default.Favorite,
          valueText = "${p.health.toInt()}%",
          isWarning = p.health < 25f,
          pulseAlpha = pulseAlpha
        )
        // Body Temp
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Thermostat,
            contentDescription = null,
            tint = tempColor,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "%.1f°C".format(p.bodyTemp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = tempColor,
            fontSize = 11.sp
          )
          if (p.isShivering) {
            Spacer(modifier = Modifier.width(3.dp))
            Icon(Icons.Default.AcUnit, contentDescription = null, tint = FrostCold80, modifier = Modifier.size(11.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(3.dp))

      // Row 2: Hunger, Thirst, Stamina
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        MiniVitalBar(
          label = "Fome",
          fraction = p.hunger / 100f,
          color = VitalHunger,
          icon = Icons.Default.Restaurant,
          valueText = "${p.hunger.toInt()}%"
        )
        MiniVitalBar(
          label = "Sede",
          fraction = p.thirst / 100f,
          color = VitalThirst,
          icon = Icons.Default.WaterDrop,
          valueText = "${p.thirst.toInt()}%"
        )
        MiniVitalBar(
          label = "Estam",
          fraction = p.stamina / 100f,
          color = VitalStamina,
          icon = Icons.Default.FlashOn,
          valueText = "${p.stamina.toInt()}%"
        )
      }
    }
  }
}

@Composable
fun MiniVitalBar(
  label: String,
  fraction: Float,
  color: Color,
  icon: ImageVector,
  valueText: String,
  isWarning: Boolean = false,
  pulseAlpha: Float = 1.0f
) {
  val barColor = if (isWarning) color.copy(alpha = pulseAlpha) else color

  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, contentDescription = label, tint = barColor, modifier = Modifier.size(11.dp))
    Spacer(modifier = Modifier.width(3.dp))
    LinearProgressIndicator(
      progress = { fraction.coerceIn(0f, 1f) },
      modifier = Modifier
        .width(42.dp)
        .height(4.dp)
        .clip(RoundedCornerShape(2.dp)),
      color = barColor,
      trackColor = Color(0xFF263328)
    )
  }
}

@Composable
fun HudMenuButton(
  icon: ImageVector,
  label: String,
  color: Color,
  badgeCount: Int = 0,
  testTag: String,
  onClick: () -> Unit
) {
  BadgedBox(
    badge = {
      if (badgeCount > 0) {
        Badge(containerColor = Color(0xFF2E7D32), contentColor = Color.White) {
          Text(text = badgeCount.toString(), fontSize = 8.sp)
        }
      }
    }
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xDD151F16))
        .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        .clickable { onClick() }
        .testTag(testTag),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
    }
  }
}

@Composable
fun LandscapeQuickHotbar(
  state: SurvivalUiState,
  onUseItem: (InventoryItemEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val quickItems = state.inventory.take(6)

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xD9121B13),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3E2F)),
    modifier = modifier.testTag("landscape_quick_hotbar")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      quickItems.forEach { itemEntity ->
        val itemType = ItemType.values().firstOrNull { it.id == itemEntity.itemTypeId }
        val isEquipped = state.profile.equippedItemId == itemEntity.itemTypeId ||
            state.profile.equippedClothingId == itemEntity.itemTypeId

        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isEquipped) Color(0xFF2E5E36) else Color(0xFF1E2A20))
            .border(
              if (isEquipped) 1.5.dp else 1.dp,
              if (isEquipped) Color(0xFFA5E6A9) else Color(0xFF384D3C),
              RoundedCornerShape(8.dp)
            )
            .clickable { onUseItem(itemEntity) }
            .padding(2.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            val icon = when (itemType?.category) {
              ItemCategory.ALIMENTO -> Icons.Default.Restaurant
              ItemCategory.FERRAMENTA -> Icons.Default.Build
              ItemCategory.SOBREVIVENCIA -> Icons.Default.LocalFireDepartment
              ItemCategory.VESTUARIO -> Icons.Default.Security
              else -> Icons.Default.Terrain
            }
            Icon(
              imageVector = icon,
              contentDescription = itemType?.displayName ?: "Item",
              tint = if (isEquipped) Color(0xFF81C784) else Color.White,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "x${itemEntity.quantity}",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFFCA28)
            )
          }
        }
      }
    }
  }
}
