package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.SurvivalUiState

@Composable
fun GameOverDialog(
  state: SurvivalUiState,
  onRestartGame: () -> Unit
) {
  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
  ) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = Color(0xF21C1212),
      border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFB71C1C)),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .testTag("game_over_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFF3E1A1A)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFEF5350),
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "VOCÊ NÃO RESISTIU",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Black,
          color = Color(0xFFFF5252),
          letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = state.profile.deathCause ?: "A floresta densa cobriu seu rastro com folhas e neve.",
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          color = Color(0xFFE0E0E0)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Card
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF281919),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4E2626)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatRow("Dias Sobrevividos", "${state.profile.daysSurvived} dias")
            StatRow("Madeira Coletada", "${state.profile.totalWoodGathered} troncos")
            StatRow("Pedras Mineradas", "${state.profile.totalStoneMined} pedras")
            StatRow("Animais Caçados", "${state.profile.totalAnimalsHunted} presas")
            StatRow("Itens Fabricados", "${state.profile.totalItemsCrafted} itens")
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onRestartGame,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_restart_game")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Despertar Novamente (Novo Jogo)", fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }
  }
}

@Composable
fun StatRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
    Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
  }
}
