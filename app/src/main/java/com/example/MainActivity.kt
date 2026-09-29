package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.WeatherType
import com.example.ui.ActiveScreenDialog
import com.example.ui.SurvivalViewModel
import com.example.ui.components.BuildDialog
import com.example.ui.components.CampManagementDialog
import com.example.ui.components.CraftingDialog
import com.example.ui.components.GameOverDialog
import com.example.ui.components.InventoryDialog
import com.example.ui.components.JournalAndGuideDialog
import com.example.ui.components.LandscapeGameHud
import com.example.ui.components.OpenWorldGameView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        OpenWorldSurvivalGameScreen()
      }
    }
  }
}

@Composable
fun OpenWorldSurvivalGameScreen(
  viewModel: SurvivalViewModel = viewModel()
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  // Handle back press to close any open dialog or cancel placement
  BackHandler(enabled = state.activeDialog != ActiveScreenDialog.NONE || state.selectedBuildingToPlace != null) {
    if (state.selectedBuildingToPlace != null) {
      viewModel.cancelPlacement()
    } else {
      viewModel.setDialog(ActiveScreenDialog.NONE)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0D140E))
  ) {
    // 1. Open-World Canvas (Krafteers-style exploration, building, harvesting, mob AI)
    OpenWorldGameView(
      worldManager = viewModel.worldManager,
      playerX = state.playerX,
      playerY = state.playerY,
      playerFacingAngle = state.playerFacingAngle,
      equippedTool = state.equippedItem,
      structures = state.structures,
      timeMinutes = state.profile.timeMinutes,
      isNight = state.isNight,
      isDusk = state.isDusk,
      isRaining = state.currentWeatherEnum == WeatherType.TEMPESTADE_CHUVA,
      isSwinging = state.isSwingingTool,
      swingProgress = state.swingProgress,
      selectedBuildingToPlace = state.selectedBuildingToPlace,
      placementX = state.placementX,
      placementY = state.placementY,
      canPlaceHere = state.canPlaceHere,
      onTapWorld = { _, _ ->
        if (state.selectedBuildingToPlace != null) {
          viewModel.confirmPlacement()
        } else {
          viewModel.onActionAttack()
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // 2. Horizontal Game HUD (Compact Vitals, Virtual Joystick, Hotbar, Actions)
    LandscapeGameHud(
      state = state,
      onOpenDialog = { dialog -> viewModel.setDialog(dialog) },
      onAttackAction = { viewModel.onActionAttack() },
      onConfirmPlacement = { viewModel.confirmPlacement() },
      onCancelPlacement = { viewModel.cancelPlacement() },
      onUseItem = { item -> viewModel.useItem(item) },
      onMoveJoystick = { dx, dy, angle -> viewModel.onJoystickMove(dx, dy, angle) }
    )

    // 3. Floating Resource Loot Texts
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(bottom = 70.dp)
    ) {
      androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
      ) {
        state.floatingMessages.forEach { msg ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (msg.isPositive) Color(0xEE1E3E23) else Color(0xEE4E1D1D),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (msg.isPositive) Color(0xFF66BB6A) else Color(0xFFEF5350)
            )
          ) {
            Text(
              text = msg.text,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }
    }

    // 4. Notification Banner Bar (Day/Night events, weather, alerts)
    AnimatedVisibility(
      visible = state.bannerNotification != null,
      enter = slideInVertically() + fadeIn(),
      exit = slideOutVertically() + fadeOut(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 44.dp)
        .padding(horizontal = 40.dp)
    ) {
      state.bannerNotification?.let { bannerText ->
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xF21C2B1F),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF66BB6A)),
          shadowElevation = 6.dp
        ) {
          Text(
            text = bannerText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
          )
        }
      }
    }

    // 5. Modals & Dialogs
    when (state.activeDialog) {
      ActiveScreenDialog.INVENTORY -> {
        InventoryDialog(
          state = state,
          onUseItem = { item -> viewModel.useItem(item) },
          onDropItem = { item -> viewModel.dropItem(item) },
          onDismiss = { viewModel.setDialog(ActiveScreenDialog.NONE) }
        )
      }

      ActiveScreenDialog.CRAFTING -> {
        CraftingDialog(
          state = state,
          onCraftRecipe = { recipe -> viewModel.craftItem(recipe) },
          onSelectCategory = { cat -> viewModel.setSelectedCategory(cat) },
          onDismiss = { viewModel.setDialog(ActiveScreenDialog.NONE) }
        )
      }

      ActiveScreenDialog.BUILDING -> {
        BuildDialog(
          state = state,
          onBuildStructure = { recipe -> viewModel.buildStructure(recipe) },
          onDismiss = { viewModel.setDialog(ActiveScreenDialog.NONE) }
        )
      }

      ActiveScreenDialog.CAMP_STATION -> {
        CampManagementDialog(
          state = state,
          onAddFuel = { fuelItem -> viewModel.addFuelToCampfire(fuelItem) },
          onDrinkWater = { viewModel.drinkFromRainCatcher() },
          onSleep = { hours -> viewModel.sleep(hours) },
          onDismiss = { viewModel.setDialog(ActiveScreenDialog.NONE) }
        )
      }

      ActiveScreenDialog.JOURNAL, ActiveScreenDialog.SURVIVAL_GUIDE -> {
        JournalAndGuideDialog(
          state = state,
          onDismiss = { viewModel.setDialog(ActiveScreenDialog.NONE) }
        )
      }

      else -> {}
    }

    // 6. Game Over Dialog
    if (state.profile.isGameOver) {
      GameOverDialog(
        state = state,
        onRestartGame = { viewModel.restartGame() }
      )
    }
  }
}
