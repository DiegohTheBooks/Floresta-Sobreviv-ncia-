package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ActiveScreenDialog
import com.example.ui.SurvivalUiState

@Composable
fun QuickAccessBar(
  state: SurvivalUiState,
  onOpenDialog: (ActiveScreenDialog) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    color = Color(0xF2131C14),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E4031)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("quick_access_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      QuickNavButton(
        label = "Mochila",
        icon = Icons.Default.Inventory2,
        badgeCount = state.inventory.size,
        color = Color(0xFF81C784),
        testTag = "btn_inventory",
        onClick = { onOpenDialog(ActiveScreenDialog.INVENTORY) }
      )

      QuickNavButton(
        label = "Fabricar",
        icon = Icons.Default.Build,
        color = Color(0xFFFFB74D),
        testTag = "btn_crafting",
        onClick = { onOpenDialog(ActiveScreenDialog.CRAFTING) }
      )

      QuickNavButton(
        label = "Construir",
        icon = Icons.Default.Home,
        color = Color(0xFF80DEEA),
        testTag = "btn_building",
        onClick = { onOpenDialog(ActiveScreenDialog.BUILDING) }
      )

      QuickNavButton(
        label = "Acampamento",
        icon = Icons.Default.LocalFireDepartment,
        color = if (state.hasCampfireBurning) Color(0xFFFF7043) else Color(0xFF90A4AE),
        testTag = "btn_camp_station",
        onClick = { onOpenDialog(ActiveScreenDialog.CAMP_STATION) }
      )

      QuickNavButton(
        label = "Diário",
        icon = Icons.Default.Book,
        color = Color(0xFFCE93D8),
        testTag = "btn_journal",
        onClick = { onOpenDialog(ActiveScreenDialog.JOURNAL) }
      )
    }
  }
}

@Composable
fun QuickNavButton(
  label: String,
  icon: ImageVector,
  color: Color,
  badgeCount: Int = 0,
  testTag: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .testTag(testTag)
  ) {
    BadgedBox(
      badge = {
        if (badgeCount > 0) {
          Badge(
            containerColor = Color(0xFF2E7D32),
            contentColor = Color.White
          ) {
            Text(text = badgeCount.toString(), fontSize = 9.sp)
          }
        }
      }
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.15f))
          .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = color,
          modifier = Modifier.size(20.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = Color(0xFFDDDDDD)
    )
  }
}
