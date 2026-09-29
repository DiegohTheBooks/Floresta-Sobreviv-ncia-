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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.JournalEntryEntity
import com.example.ui.SurvivalUiState

@Composable
fun JournalAndGuideDialog(
  state: SurvivalUiState,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableStateOf("DIARIO") }

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
        .testTag("journal_dialog")
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFFCE93D8))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Registro de Sobrevivência",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_journal_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (selectedTab == "DIARIO") Color(0xFF2E6936) else Color(0xFF1E2A20))
              .clickable { selectedTab = "DIARIO" }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Diário de Bordo (${state.journalEntries.size})",
              fontWeight = if (selectedTab == "DIARIO") FontWeight.Bold else FontWeight.Normal,
              color = Color.White,
              fontSize = 12.sp
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (selectedTab == "GUIA") Color(0xFF2E6936) else Color(0xFF1E2A20))
              .clickable { selectedTab = "GUIA" }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Guia Prático da Floresta",
              fontWeight = if (selectedTab == "GUIA") FontWeight.Bold else FontWeight.Normal,
              color = Color.White,
              fontSize = 12.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == "DIARIO") {
          if (state.journalEntries.isEmpty()) {
            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Nenhum registro ainda.",
                color = Color(0xFF9E9E9E)
              )
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
              items(state.journalEntries) { entry ->
                JournalEntryCard(entry = entry)
              }
            }
          }
        } else {
          // Guia de Campo
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
          ) {
            item {
              SurvivalGuideTip(
                title = "Temperatura Corporal & Hipotermia",
                icon = Icons.Default.AcUnit,
                iconTint = Color(0xFF81D4FA),
                content = "A floresta se torna mortal à noite e durante tempestades. Se a temperatura corporal cair abaixo de 35.5°C, você começará a tremer. Abaixo de 34.5°C, a perda de vida é contínua. Para se aquecer: permaneça próximo à fogueira acesa (+16°C), vista casaco de peles (+8°C) e evite ficar encharcado na chuva."
              )
            }
            item {
              SurvivalGuideTip(
                title = "Purificação de Água & Sede",
                icon = Icons.Default.Water,
                iconTint = Color(0xFF4FC3F7),
                content = "Água direta do riacho possui 40% de chance de causar infecção estomacal severa. Para beber com total segurança: construa um Coletor de Chuva que filtra a água pluvial, ou ferva a água suja na fogueira com carvão vegetal para transformá-la em Água Pura Fervida."
              )
            }
            item {
              SurvivalGuideTip(
                title = "Alimentação & Nutrição",
                icon = Icons.Default.LocalFireDepartment,
                iconTint = Color(0xFFFFB74D),
                content = "Nunca coma carne crua obtida na caça! Asse-a na fogueira para obter Carne Assada Suculenta (+55% Fome, +15% Vida) ou use o Canteiro de Secagem para transformá-la em Carne Seca que não estraga. Frutas silvestres saciam fome e sede simultaneamente sem riscos."
              )
            }
            item {
              SurvivalGuideTip(
                title = "Ferramentas & Construções",
                icon = Icons.Default.Nature,
                iconTint = Color(0xFFA5E6A9),
                content = "Construa primeiro a Fogueira de Pedra e o Abrigo de Folhas. Em seguida, erga a Bancada de Trabalho para desbloquear ferramentas forjadas, arco de caça e a Cabana de Troncos Reforçada, que confere isolamento térmico definitivo contra tempestades."
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun JournalEntryCard(entry: JournalEntryEntity) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = Color(0xFF1D281F),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334637)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = entry.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFFCA28)
        )
        Text(
          text = "Dia ${entry.day} • ${entry.timeString}",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          color = Color(0xFFAAAAAA)
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = entry.message,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = Color(0xFFE0E0E0)
      )
    }
  }
}

@Composable
fun SurvivalGuideTip(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  content: String
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF1E281F),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384D3C)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = content,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = Color(0xFFCCCCCC)
      )
    }
  }
}
