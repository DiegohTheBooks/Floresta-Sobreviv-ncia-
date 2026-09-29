package com.example.ui.vitals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VitalsProgressBarsView(
  vitals: VitalsUiState,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "vitalPulse")
  val warningPulse by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(450, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "warningPulse"
  )

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xE6111A13),
    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF334B38)),
    shadowElevation = 6.dp,
    modifier = modifier.testTag("vitals_hud_container")
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Row 1: Saúde & Temperatura Corporal
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        VitalProgressItem(
          label = "SAÚDE",
          currentValue = vitals.health,
          maxValue = vitals.maxHealth,
          icon = Icons.Default.Favorite,
          primaryColor = Color(0xFFEF5350),
          warningColor = Color(0xFFFF1744),
          isWarning = vitals.isCriticallyInjured,
          pulseAlpha = warningPulse,
          barWidth = 65,
          testTag = "vital_bar_health"
        )

        // Termômetro Corporal (°C)
        val tempColor = when {
          vitals.bodyTemp < 35.0f -> Color(0xFF29B6F6)
          vitals.bodyTemp < 36.0f -> Color(0xFF81D4FA)
          vitals.bodyTemp in 36.0f..37.4f -> Color(0xFF81C784)
          else -> Color(0xFFFF7043)
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.testTag("vital_badge_temperature")
        ) {
          Icon(
            imageVector = Icons.Default.Thermostat,
            contentDescription = "Temperatura",
            tint = tempColor,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "%.1f°C".format(vitals.bodyTemp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = tempColor,
            fontSize = 11.sp
          )
          if (vitals.isShivering) {
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
              imageVector = Icons.Default.AcUnit,
              contentDescription = "Hipotermia",
              tint = Color(0xFF80D8FF),
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }

      // Row 2: Fome, Sede e Energia (Barras de Progresso Principais)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // 1. Barra de Fome
        VitalProgressItem(
          label = "FOME",
          currentValue = vitals.hunger,
          maxValue = vitals.maxHunger,
          icon = Icons.Default.Restaurant,
          primaryColor = Color(0xFFFFA726),
          warningColor = Color(0xFFFF3D00),
          isWarning = vitals.isStarving,
          pulseAlpha = warningPulse,
          barWidth = 55,
          testTag = "vital_bar_hunger"
        )

        // 2. Barra de Sede
        VitalProgressItem(
          label = "SEDE",
          currentValue = vitals.thirst,
          maxValue = vitals.maxThirst,
          icon = Icons.Default.WaterDrop,
          primaryColor = Color(0xFF29B6F6),
          warningColor = Color(0xFFD50000),
          isWarning = vitals.isDehydrated,
          pulseAlpha = warningPulse,
          barWidth = 55,
          testTag = "vital_bar_thirst"
        )

        // 3. Barra de Energia (Estamina)
        VitalProgressItem(
          label = "ENERGIA",
          currentValue = vitals.energy,
          maxValue = vitals.maxEnergy,
          icon = Icons.Default.Bolt,
          primaryColor = Color(0xFFFFD600),
          warningColor = Color(0xFFFF9100),
          isWarning = vitals.isExhausted,
          pulseAlpha = warningPulse,
          barWidth = 55,
          testTag = "vital_bar_energy"
        )
      }

      // Alert Warning Strip if any vital is critically depleted
      val activeAlert = when {
        vitals.isCriticallyInjured -> "⚠️ Saúde Crítica: Trate ferimentos imediatamente!"
        vitals.isStarving -> "⚠️ Fome Extrema: Coma alimentos ou caça assada!"
        vitals.isDehydrated -> "⚠️ Sede Extrema: Beba água purificada ou colete chuva!"
        vitals.isExhausted -> "⚠️ Energia Esgotada: O sobrevivente está exausto!"
        vitals.isShivering -> "⚠️ Frio Intenso: Aqueça-se na fogueira para evitar hipotermia!"
        else -> null
      }

      AnimatedVisibility(
        visible = activeAlert != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        activeAlert?.let { alertText ->
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xD93E1A1A))
              .border(0.8.dp, Color(0xFFFF5252).copy(alpha = warningPulse), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(10.dp)
              )
              Text(
                text = alertText,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFCDD2)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun VitalProgressItem(
  label: String,
  currentValue: Float,
  maxValue: Float,
  icon: ImageVector,
  primaryColor: Color,
  warningColor: Color,
  isWarning: Boolean,
  pulseAlpha: Float,
  barWidth: Int,
  testTag: String
) {
  val animatedProgress by animateFloatAsState(
    targetValue = (currentValue / maxValue).coerceIn(0f, 1f),
    animationSpec = tween(300),
    label = "$label progress"
  )

  val activeColor = if (isWarning) warningColor.copy(alpha = pulseAlpha) else primaryColor

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.testTag(testTag)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = activeColor,
      modifier = Modifier.size(12.dp)
    )
    Spacer(modifier = Modifier.width(3.dp))
    Column {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.width(barWidth.dp)
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 8.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFAAAAAA)
        )
        Text(
          text = "${currentValue.toInt()}%",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 8.sp,
          fontWeight = FontWeight.Bold,
          color = if (isWarning) Color(0xFFFF5252) else Color.White
        )
      }
      Spacer(modifier = Modifier.height(1.dp))
      LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
          .width(barWidth.dp)
          .height(5.dp)
          .clip(RoundedCornerShape(2.5.dp)),
        color = activeColor,
        trackColor = Color(0xFF263328)
      )
    }
  }
}
