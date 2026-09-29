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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WbSunny
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
import com.example.data.model.ItemType
import com.example.ui.SurvivalUiState

@Composable
fun CampManagementDialog(
  state: SurvivalUiState,
  onAddFuel: (ItemType) -> Unit,
  onDrinkWater: () -> Unit,
  onSleep: (Int) -> Unit,
  onDismiss: () -> Unit
) {
  val campfire = state.structures.firstOrNull { it.structureTypeId == "campfire" }
  val rainCatcher = state.structures.firstOrNull { it.structureTypeId == "rain_catcher" }
  val hasShelter = state.hasShelter
  val fuelMinutes = campfire?.fuelMinutesRemaining ?: 0

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
        .fillMaxHeight(0.85f)
        .testTag("camp_management_dialog")
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
              text = "Gestão do Acampamento",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "Controle o fogo, água pura e descanso seguro",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = Color(0xFF81C784)
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_camp_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // 1. Campfire Station Card
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E271F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF425545)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (fuelMinutes > 0) Color(0xFFFF7043) else Color(0xFF9E9E9E),
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Fogueira de Pedra",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
                Text(
                  text = if (fuelMinutes > 0) "$fuelMinutes min restantes" else "Apagada",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (fuelMinutes > 0) Color(0xFFFFB74D) else Color(0xFFEF5350)
                )
              }

              Spacer(modifier = Modifier.height(6.dp))
              LinearProgressIndicator(
                progress = { (fuelMinutes / 180f).coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFFF7043),
                trackColor = Color(0xFF2B3A2E)
              )

              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Alimentar com combustível do inventário:",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = Color(0xFFAAAAAA)
              )

              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val stickCount = state.getItemCount(ItemType.STICK)
                val woodCount = state.getItemCount(ItemType.WOOD)
                val resinCount = state.getItemCount(ItemType.RESIN)

                Button(
                  onClick = { onAddFuel(ItemType.STICK) },
                  enabled = stickCount > 0,
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38553B)),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("btn_fuel_stick")
                ) {
                  Text(text = "Graveto ($stickCount)\n+15m", fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }

                Button(
                  onClick = { onAddFuel(ItemType.WOOD) },
                  enabled = woodCount > 0,
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3E2C)),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("btn_fuel_wood")
                ) {
                  Text(text = "Tronco ($woodCount)\n+45m", fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }

                Button(
                  onClick = { onAddFuel(ItemType.RESIN) },
                  enabled = resinCount > 0,
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B451A)),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("btn_fuel_resin")
                ) {
                  Text(text = "Resina ($resinCount)\n+30m", fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
              }
            }
          }

          // 2. Rain Catcher Station Card
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E271F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF425545)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.Water,
                    contentDescription = null,
                    tint = Color(0xFF4FC3F7),
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Coletor de Chuva",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
                Text(
                  text = if (rainCatcher != null && rainCatcher.isBuilt) "%.1f / 10.0 L".format(rainCatcher.waterStoredLiters) else "Não Construído",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF81D4FA)
                )
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Enche automaticamente durante tempestades de chuva na floresta. Água 100% filtrada e livre de bactérias.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = Color(0xFFB0BEC5)
              )

              if (rainCatcher != null && rainCatcher.isBuilt) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = onDrinkWater,
                  enabled = rainCatcher.waterStoredLiters >= 0.5f,
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_drink_rain")
                ) {
                  Icon(Icons.Default.Water, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = "Beber Água Pura (+40 Sede)", fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          // 3. Sleep & Rest Card
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E271F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF425545)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Bed,
                  contentDescription = null,
                  tint = Color(0xFFB39DDB),
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Descanso & Passagem do Tempo",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (hasShelter) "Abrigo disponível! Dormir recupera estamina e saúde caso esteja aquecido." else "ATENÇÃO: Construa um Abrigo de Folhas ou Cabana para poder dormir sem riscos!",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = if (hasShelter) Color(0xFFA5E6A9) else Color(0xFFFFB74D)
              )

              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = { onSleep(2) },
                  enabled = hasShelter,
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334A38)),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("btn_sleep_2h")
                ) {
                  Text(text = "Descansar 2h", fontSize = 11.sp)
                }

                Button(
                  onClick = { onSleep(6) },
                  enabled = hasShelter,
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6936)),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("btn_sleep_6h")
                ) {
                  Text(text = "Dormir 6h (Noite)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }
  }
}
