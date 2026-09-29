package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.ItemType
import com.example.ui.SurvivalUiState

@Composable
fun InventoryDialog(
  state: SurvivalUiState,
  onUseItem: (InventoryItemEntity) -> Unit,
  onDropItem: (InventoryItemEntity) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableStateOf("TODOS") }
  val tabs = listOf("TODOS", "COLETÁVEIS", "FERRAMENTAS", "ALIMENTOS")

  val filteredItems = state.inventory.filter { entity ->
    val type = ItemType.values().firstOrNull { it.id == entity.itemTypeId }
    when (selectedTab) {
      "COLETÁVEIS" -> !entity.isCrafted && type?.category != ItemCategory.ALIMENTO
      "FERRAMENTAS" -> entity.isCrafted || type?.category == ItemCategory.FERRAMENTA || type?.category == ItemCategory.SOBREVIVENCIA
      "ALIMENTOS" -> type?.category == ItemCategory.ALIMENTO
      else -> true
    }
  }

  var selectedItem by remember { mutableStateOf<InventoryItemEntity?>(filteredItems.firstOrNull() ?: state.inventory.firstOrNull()) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color(0xF7151E16),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384F3C)),
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.88f)
        .testTag("inventory_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Inventário Room • Itens Coletáveis & Ferramentas",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "${state.inventory.count { !it.isCrafted }} Coletáveis",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF81C784),
                fontSize = 11.sp
              )
              Text(
                text = "•",
                color = Color(0xFF888888),
                fontSize = 11.sp
              )
              Text(
                text = "${state.inventory.count { it.isCrafted }} Ferramentas Fabricadas",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFB74D),
                fontSize = 11.sp
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_inventory_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs Row (Todos, Coletáveis, Ferramentas, Alimentos)
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(tabs) { tab ->
            val isSelected = selectedTab == tab
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFF2E6936) else Color(0xFF1E2A20))
                .border(
                  1.dp,
                  if (isSelected) Color(0xFF81C784) else Color(0xFF334637),
                  RoundedCornerShape(10.dp)
                )
                .clickable {
                  selectedTab = tab
                  selectedItem = state.inventory.firstOrNull { entity ->
                    val type = ItemType.values().firstOrNull { it.id == entity.itemTypeId }
                    when (tab) {
                      "COLETÁVEIS" -> !entity.isCrafted && type?.category != ItemCategory.ALIMENTO
                      "FERRAMENTAS" -> entity.isCrafted || type?.category == ItemCategory.FERRAMENTA || type?.category == ItemCategory.SOBREVIVENCIA
                      "ALIMENTOS" -> type?.category == ItemCategory.ALIMENTO
                      else -> true
                    }
                  }
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("tab_$tab")
            ) {
              Text(
                text = tab,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFFAAAAAA)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Inventory Grid
        if (filteredItems.isEmpty()) {
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = when (selectedTab) {
                "COLETÁVEIS" -> "Nenhum item coletável encontrado.\nCorte árvores ou minere rochas no mapa!"
                "FERRAMENTAS" -> "Nenhuma ferramenta fabricada.\nUse a Bancada de Trabalho para forjar!"
                "ALIMENTOS" -> "Sem alimentos na mochila.\nColha frutas silvestres ou cace cervos!"
                else -> "Mochila vazia."
              },
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF9E9E9E),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        } else {
          LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
          ) {
            items(filteredItems) { itemEntity ->
              val itemType = ItemType.values().firstOrNull { it.id == itemEntity.itemTypeId }
              val isSelected = selectedItem?.id == itemEntity.id
              val isEquipped = state.profile.equippedItemId == itemEntity.itemTypeId ||
                  state.profile.equippedClothingId == itemEntity.itemTypeId

              ItemGridCell(
                itemEntity = itemEntity,
                itemType = itemType,
                isSelected = isSelected,
                isEquipped = isEquipped,
                onClick = { selectedItem = itemEntity }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Item Details Panel
        val currentSelected = selectedItem
        val currentType = currentSelected?.let { sel ->
          ItemType.values().firstOrNull { it.id == sel.itemTypeId }
        }

        if (currentSelected != null && currentType != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E2A20),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF435D48)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = currentSelected.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  // Tag [Fabricado] ou [Coletável]
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(if (currentSelected.isCrafted) Color(0xFFE65100) else Color(0xFF1B5E20))
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = if (currentSelected.isCrafted) "FABRICADO" else "COLETÁVEL",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                }

                Text(
                  text = "Qtd: ${currentSelected.quantity}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFFD54F)
                )
              }

              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = currentType.description,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = Color(0xFFCCCCCC)
              )

              // Durability bar if applicable
              if (currentSelected.maxDurability > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "Durabilidade da Ferramenta",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = Color(0xFFAAAAAA)
                  )
                  Text(
                    text = "${currentSelected.durability} / ${currentSelected.maxDurability}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentSelected.durability < 10) Color(0xFFFF5252) else Color.White
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                  progress = { (currentSelected.durability.toFloat() / currentSelected.maxDurability).coerceIn(0f, 1f) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                  color = if (currentSelected.durability < 10) Color(0xFFFF5252) else Color(0xFF81C784),
                  trackColor = Color(0xFF2E3D30)
                )
              }

              // Action buttons
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val actionLabel = when (currentType.category) {
                  ItemCategory.ALIMENTO -> "Comer / Beber"
                  ItemCategory.MEDICINA -> "Aplicar Tratamento"
                  ItemCategory.FERRAMENTA, ItemCategory.SOBREVIVENCIA -> {
                    if (state.profile.equippedItemId == currentType.id) "Desequipar" else "Equipar na Mão"
                  }
                  ItemCategory.VESTUARIO -> {
                    if (state.profile.equippedClothingId == currentType.id) "Tirar Roupa" else "Vestir"
                  }
                  else -> null
                }

                if (actionLabel != null) {
                  Button(
                    onClick = {
                      onUseItem(currentSelected)
                    },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("btn_use_item"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6936))
                  ) {
                    Text(text = actionLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }

                OutlinedButton(
                  onClick = {
                    onDropItem(currentSelected)
                    selectedItem = null
                  },
                  modifier = Modifier.testTag("btn_drop_item"),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373))
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Descartar", modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Descartar", fontSize = 11.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ItemGridCell(
  itemEntity: InventoryItemEntity,
  itemType: ItemType?,
  isSelected: Boolean,
  isEquipped: Boolean,
  onClick: () -> Unit
) {
  val borderColor = when {
    isSelected -> Color(0xFFFFD54F)
    isEquipped -> Color(0xFF81C784)
    else -> Color(0xFF334637)
  }

  val itemIcon = when (itemType?.category) {
    ItemCategory.ALIMENTO -> Icons.Default.Restaurant
    ItemCategory.FERRAMENTA -> Icons.Default.Build
    ItemCategory.SOBREVIVENCIA -> Icons.Default.LocalFireDepartment
    ItemCategory.VESTUARIO -> Icons.Default.Security
    ItemCategory.RECURSO -> Icons.Default.Terrain
    else -> Icons.Default.Grass
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (isSelected) Color(0xFF283B2C) else Color(0xFF19241B))
      .border(if (isSelected || isEquipped) 2.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(4.dp)
      .testTag("inventory_item_${itemEntity.itemTypeId}")
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = itemIcon,
          contentDescription = itemEntity.name,
          tint = if (isEquipped) Color(0xFFA5E6A9) else Color(0xFFDDDDDD),
          modifier = Modifier.size(24.dp)
        )
        if (isEquipped) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .size(7.dp)
              .clip(CircleShape)
              .background(Color(0xFF66BB6A))
          )
        }
      }

      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = itemEntity.name,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.sp,
        maxLines = 1,
        color = Color.White
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (itemEntity.isCrafted) "🛠️" else "🌿",
          fontSize = 8.sp
        )
        Text(
          text = "x${itemEntity.quantity}",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFFCA28)
        )
      }

      // Mini durability bar if tool
      if (itemEntity.maxDurability > 0) {
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
          progress = { (itemEntity.durability.toFloat() / itemEntity.maxDurability).coerceIn(0f, 1f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp)),
          color = Color(0xFF81C784),
          trackColor = Color(0xFF2E3D30)
        )
      }
    }
  }
}
