package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StructureType
import com.example.domain.CraftingCatalog
import com.example.domain.StructureRecipe
import com.example.ui.SurvivalUiState

@Composable
fun BuildDialog(
  state: SurvivalUiState,
  onBuildStructure: (StructureRecipe) -> Unit,
  onDismiss: () -> Unit
) {
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
        .testTag("build_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Construção do Acampamento",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "Erga estruturas sólidas para se proteger do frio, chuva e feras",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = Color(0xFF81C784)
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_build_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Structures List
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          items(CraftingCatalog.STRUCTURE_RECIPES) { recipe ->
            val structureEntity = state.structures.firstOrNull { it.structureTypeId == recipe.structureType.id }
            val isAlreadyBuilt = structureEntity?.isBuilt == true
            val hasWorkbenchReq = !recipe.requiresWorkbench || state.hasWorkbench
            val hasIngredients = recipe.ingredients.all { ing ->
              state.getItemCount(ing.itemType) >= ing.amount
            }
            val canBuild = !isAlreadyBuilt && hasWorkbenchReq && hasIngredients

            StructureCard(
              state = state,
              recipe = recipe,
              isAlreadyBuilt = isAlreadyBuilt,
              canBuild = canBuild,
              hasWorkbenchReq = hasWorkbenchReq,
              onBuild = { onBuildStructure(recipe) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun StructureCard(
  state: SurvivalUiState,
  recipe: StructureRecipe,
  isAlreadyBuilt: Boolean,
  canBuild: Boolean,
  hasWorkbenchReq: Boolean,
  onBuild: () -> Unit
) {
  val structure = recipe.structureType
  val icon = when (structure) {
    StructureType.FOGUEIRA -> Icons.Default.LocalFireDepartment
    StructureType.ABRIGO_FOLHAS, StructureType.CABANA_MADEIRA -> Icons.Default.Home
    StructureType.COLETOR_CHUVA -> Icons.Default.Water
    StructureType.BANCADA_TRABALHO -> Icons.Default.Build
    StructureType.BAU_ARMAZENAMENTO -> Icons.Default.Inventory2
    StructureType.CERCA_ESTACAS -> Icons.Default.Shield
    StructureType.CAMA_PELES -> Icons.Default.Bed
    else -> Icons.Default.Home
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF1D281F),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isAlreadyBuilt) Color(0xFF388E3C) else if (canBuild) Color(0xFF45694B) else Color(0xFF2E3B30)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("structure_card_${structure.id}")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (isAlreadyBuilt) Color(0xFF1B5E20) else Color(0xFF2B3D2E)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = structure.displayName,
              tint = if (isAlreadyBuilt) Color(0xFFA5E6A9) else Color(0xFFFFCA28),
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = structure.displayName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = if (isAlreadyBuilt) "Status: No Acampamento (Ativo)" else "Ainda não construído",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 11.sp,
              color = if (isAlreadyBuilt) Color(0xFF81C784) else Color(0xFFFFA726)
            )
          }
        }

        if (isAlreadyBuilt) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2E7D32)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text("Pronto", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        } else {
          Button(
            onClick = onBuild,
            enabled = canBuild,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF2E6936),
              disabledContainerColor = Color(0xFF1F2B21)
            ),
            modifier = Modifier.testTag("btn_build_${structure.id}")
          ) {
            Text(
              text = "Construir",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = if (canBuild) Color.White else Color(0xFF6E7E70)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = recipe.perkDescription,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = Color(0xFFCCCCCC)
      )

      if (!isAlreadyBuilt) {
        if (recipe.requiresWorkbench) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (hasWorkbenchReq) "Exige Bancada de Trabalho ✔" else "Exige Bancada de Trabalho (Construa antes) ✖",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = if (hasWorkbenchReq) Color(0xFF81C784) else Color(0xFFE57373)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Recursos para Construir:",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFAAAAAA)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          recipe.ingredients.forEach { ing ->
            val have = state.getItemCount(ing.itemType)
            val enough = have >= ing.amount
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (enough) Color(0xFF223625) else Color(0xFF381F1F),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (enough) Color(0xFF388E3C) else Color(0xFFC62828))
            ) {
              Text(
                text = "${ing.itemType.displayName}: $have/${ing.amount}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enough) Color(0xFF81C784) else Color(0xFFEF9A9A),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}
