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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.ItemCategory
import com.example.domain.CraftingCatalog
import com.example.domain.CraftingRecipe
import com.example.ui.SurvivalUiState

@Composable
fun CraftingDialog(
  state: SurvivalUiState,
  onCraftRecipe: (CraftingRecipe) -> Unit,
  onSelectCategory: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val categories = listOf("TODAS", "FERRAMENTAS", "SOBREVIVÊNCIA", "ALIMENTOS", "MEDICINA", "VESTUÁRIO")

  val filteredRecipes = CraftingCatalog.RECIPES.filter { recipe ->
    when (state.selectedCraftingCategory) {
      "FERRAMENTAS" -> recipe.category == ItemCategory.FERRAMENTA
      "SOBREVIVÊNCIA" -> recipe.category == ItemCategory.SOBREVIVENCIA
      "ALIMENTOS" -> recipe.category == ItemCategory.ALIMENTO
      "MEDICINA" -> recipe.category == ItemCategory.MEDICINA
      "VESTUÁRIO" -> recipe.category == ItemCategory.VESTUARIO
      else -> true
    }
  }

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
        .testTag("crafting_dialog")
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
              text = "Bancada de Fabricação",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = if (state.hasWorkbench) "Bancada: Pronta ✔" else "Sem Bancada (Básico)",
                style = MaterialTheme.typography.bodySmall,
                color = if (state.hasWorkbench) Color(0xFF81C784) else Color(0xFFFFB74D),
                fontSize = 11.sp
              )
              Text(
                text = if (state.hasCampfireBurning) "Fogueira: Acesa 🔥" else "Fogueira: Apagada",
                style = MaterialTheme.typography.bodySmall,
                color = if (state.hasCampfireBurning) Color(0xFFFF8A65) else Color(0xFF90A4AE),
                fontSize = 11.sp
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_crafting_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(categories) { cat ->
            val isSelected = state.selectedCraftingCategory == cat
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0xFF2E6936) else Color(0xFF1E2A20))
                .border(
                  1.dp,
                  if (isSelected) Color(0xFF81C784) else Color(0xFF334637),
                  RoundedCornerShape(12.dp)
                )
                .clickable { onSelectCategory(cat) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = cat,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFFAAAAAA)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recipe List
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          items(filteredRecipes) { recipe ->
            CraftingRecipeCard(
              state = state,
              recipe = recipe,
              onCraft = { onCraftRecipe(recipe) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun CraftingRecipeCard(
  state: SurvivalUiState,
  recipe: CraftingRecipe,
  onCraft: () -> Unit
) {
  val hasWorkbenchReq = !recipe.requiresWorkbench || state.hasWorkbench
  val hasCampfireReq = !recipe.requiresCampfire || state.hasCampfireBurning
  val hasIngredients = recipe.ingredients.all { ing ->
    state.getItemCount(ing.itemType) >= ing.amount
  }
  val canCraft = hasWorkbenchReq && hasCampfireReq && hasIngredients && !state.isCrafting

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF1D281F),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (canCraft) Color(0xFF45694B) else Color(0xFF2E3B30)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("recipe_${recipe.id}")
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
              .size(36.dp)
              .clip(CircleShape)
              .background(Color(0xFF2B3D2E)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (recipe.category) {
                ItemCategory.FERRAMENTA -> Icons.Default.Build
                ItemCategory.ALIMENTO -> Icons.Default.Restaurant
                ItemCategory.SOBREVIVENCIA -> Icons.Default.LocalFireDepartment
                else -> Icons.Default.Build
              },
              contentDescription = recipe.resultItem.displayName,
              tint = Color(0xFFFFCA28),
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = if (recipe.resultAmount > 1) "${recipe.resultAmount}x ${recipe.resultItem.displayName}" else recipe.resultItem.displayName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = recipe.category.displayName,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = Color(0xFF9E9E9E)
            )
          }
        }

        // Craft button
        Button(
          onClick = onCraft,
          enabled = canCraft,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2E6936),
            disabledContainerColor = Color(0xFF1F2B21)
          ),
          modifier = Modifier.testTag("btn_craft_${recipe.id}")
        ) {
          if (state.isCrafting) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            Text(
              text = "Fabricar",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = if (canCraft) Color.White else Color(0xFF6E7E70)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = recipe.description,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = Color(0xFFB0BEC5)
      )

      // Station requirement tags
      if (recipe.requiresWorkbench || recipe.requiresCampfire) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (recipe.requiresWorkbench) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (hasWorkbenchReq) Color(0x334CAF50) else Color(0x33F44336))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = if (hasWorkbenchReq) "Exige Bancada ✔" else "Exige Bancada (Não construída) ✖",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (hasWorkbenchReq) Color(0xFF81C784) else Color(0xFFE57373)
              )
            }
          }
          if (recipe.requiresCampfire) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (hasCampfireReq) Color(0x33FF9800) else Color(0x33F44336))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = if (hasCampfireReq) "Exige Fogueira Acesa ✔" else "Exige Fogueira Acesa (Apagada) ✖",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (hasCampfireReq) Color(0xFFFFB74D) else Color(0xFFE57373)
              )
            }
          }
        }
      }

      // Ingredients Checklist
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Materiais Necessários:",
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFAAAAAA)
      )
      Spacer(modifier = Modifier.height(3.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        recipe.ingredients.forEach { ing ->
          val currentAmount = state.getItemCount(ing.itemType)
          val isSufficient = currentAmount >= ing.amount

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isSufficient) Color(0xFF223625) else Color(0xFF381F1F),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isSufficient) Color(0xFF388E3C) else Color(0xFFC62828)
            ),
            modifier = Modifier.padding(vertical = 2.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${ing.itemType.displayName}: ",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color.White
              )
              Text(
                text = "$currentAmount/${ing.amount}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSufficient) Color(0xFF81C784) else Color(0xFFEF9A9A)
              )
            }
          }
        }
      }
    }
  }
}
