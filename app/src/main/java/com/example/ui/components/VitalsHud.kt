package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.model.WeatherType
import com.example.ui.SurvivalUiState
import com.example.ui.theme.FrostCold80
import com.example.ui.theme.SurvivalDarkSurface
import com.example.ui.theme.VitalHealth
import com.example.ui.theme.VitalHunger
import com.example.ui.theme.VitalStamina
import com.example.ui.theme.VitalTempCold
import com.example.ui.theme.VitalTempHot
import com.example.ui.theme.VitalTempNormal
import com.example.ui.theme.VitalThirst

@Composable
fun VitalsHud(
  state: SurvivalUiState,
  modifier: Modifier = Modifier
) {
  val profile = state.profile
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(600),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    // Top Bar: Day, Time, Weather, Zone
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = SurvivalDarkSurface.copy(alpha = 0.88f),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384A3B)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp)
        .testTag("top_status_bar")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Day & Time
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (state.isNight) Icons.Default.Bedtime else Icons.Default.WbSunny,
            contentDescription = "Tempo",
            tint = if (state.isNight) Color(0xFF90CAF9) else Color(0xFFFFD54F),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Dia ${profile.daysSurvived} • ${state.formattedTime}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        // Zone Name
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF233626))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = state.currentZoneEnum.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFA5E6A9),
            fontWeight = FontWeight.SemiBold
          )
        }

        // Weather Icon & Name
        Row(verticalAlignment = Alignment.CenterVertically) {
          val weatherIcon = when (state.currentWeatherEnum) {
            WeatherType.ENSOLARADO -> Icons.Default.WbSunny
            WeatherType.NEBLINA -> Icons.Default.AcUnit
            WeatherType.TEMPESTADE_CHUVA -> Icons.Default.WaterDrop
          }
          Icon(
            imageVector = weatherIcon,
            contentDescription = "Clima",
            tint = when (state.currentWeatherEnum) {
              WeatherType.ENSOLARADO -> Color(0xFFFFCA28)
              WeatherType.NEBLINA -> Color(0xFFB0BEC5)
              WeatherType.TEMPESTADE_CHUVA -> Color(0xFF4FC3F7)
            },
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = when (state.currentWeatherEnum) {
              WeatherType.ENSOLARADO -> "Ensolarado"
              WeatherType.NEBLINA -> "Neblina"
              WeatherType.TEMPESTADE_CHUVA -> "Chuva"
            },
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFE0E0E0)
          )
        }
      }
    }

    // Vitals Gauges Box
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = SurvivalDarkSurface.copy(alpha = 0.92f),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3D30)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("vitals_gauges_card")
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        // Row 1: Health & Stamina
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          VitalGaugeItem(
            modifier = Modifier.weight(1f),
            label = "Saúde",
            value = profile.health,
            maxValue = 100f,
            displayValue = "${profile.health.toInt()}%",
            color = VitalHealth,
            icon = Icons.Default.Favorite,
            isWarning = profile.health <= 25f,
            pulseAlpha = pulseAlpha
          )
          VitalGaugeItem(
            modifier = Modifier.weight(1f),
            label = "Estamina",
            value = profile.stamina,
            maxValue = 100f,
            displayValue = "${profile.stamina.toInt()}%",
            color = VitalStamina,
            icon = Icons.Default.FlashOn,
            isWarning = profile.stamina <= 15f,
            pulseAlpha = pulseAlpha
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 2: Hunger & Thirst
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          VitalGaugeItem(
            modifier = Modifier.weight(1f),
            label = "Fome",
            value = profile.hunger,
            maxValue = 100f,
            displayValue = "${profile.hunger.toInt()}%",
            color = VitalHunger,
            icon = Icons.Default.Restaurant,
            isWarning = profile.hunger <= 20f,
            pulseAlpha = pulseAlpha
          )
          VitalGaugeItem(
            modifier = Modifier.weight(1f),
            label = "Sede",
            value = profile.thirst,
            maxValue = 100f,
            displayValue = "${profile.thirst.toInt()}%",
            color = VitalThirst,
            icon = Icons.Default.WaterDrop,
            isWarning = profile.thirst <= 20f,
            pulseAlpha = pulseAlpha
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 3: Body Temperature (Foco especial com medidor em °C)
        BodyTemperatureGauge(
          bodyTemp = profile.bodyTemp,
          isShivering = profile.isShivering,
          wetness = profile.wetness,
          hasFireNear = state.hasCampfireBurning && state.currentZoneEnum.name == "ACAMPAMENTO",
          pulseAlpha = pulseAlpha
        )

        // Warnings / Active Status Alerts Row
        if (profile.hasInfection || profile.wetness > 10f || profile.isShivering || state.hasCampfireBurning) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (profile.hasInfection) {
              StatusBadge(
                text = "Infecção!",
                color = Color(0xFFD32F2F),
                icon = Icons.Default.Warning
              )
            }
            if (profile.wetness > 15f) {
              StatusBadge(
                text = "Molhado ${profile.wetness.toInt()}%",
                color = Color(0xFF0288D1),
                icon = Icons.Default.WaterDrop
              )
            }
            if (profile.isShivering) {
              StatusBadge(
                text = "Tremendo de Frio!",
                color = FrostCold80,
                icon = Icons.Default.AcUnit
              )
            }
            if (state.hasCampfireBurning && state.currentZoneEnum.name == "ACAMPAMENTO") {
              StatusBadge(
                text = "Fogo Aconchegante",
                color = Color(0xFFFF9800),
                icon = Icons.Default.LocalFireDepartment
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun VitalGaugeItem(
  modifier: Modifier = Modifier,
  label: String,
  value: Float,
  maxValue: Float,
  displayValue: String,
  color: Color,
  icon: ImageVector,
  isWarning: Boolean,
  pulseAlpha: Float
) {
  val barProgress = (value / maxValue).coerceIn(0f, 1f)
  val indicatorColor = if (isWarning) color.copy(alpha = pulseAlpha) else color

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF131914))
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = indicatorColor,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 11.sp,
          color = Color(0xFFCCCCCC)
        )
      }
      Text(
        text = displayValue,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = if (isWarning) Color(0xFFFF5252) else Color.White
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { barProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = indicatorColor,
      trackColor = Color(0xFF263328)
    )
  }
}

@Composable
fun BodyTemperatureGauge(
  bodyTemp: Float,
  isShivering: Boolean,
  wetness: Float,
  hasFireNear: Boolean,
  pulseAlpha: Float
) {
  val tempColor = when {
    bodyTemp < 35.0f -> Color(0xFF29B6F6) // Severe Hypothermia
    bodyTemp < 36.0f -> Color(0xFF81D4FA) // Mild Hypothermia
    bodyTemp in 36.0f..37.4f -> VitalTempNormal // Healthy
    else -> VitalTempHot // Hyperthermia
  }

  val statusDescription = when {
    bodyTemp < 34.6f -> "Hipotermia Grave! (Dano à Saúde)"
    bodyTemp < 35.5f -> "Frio Severo (Tremendo)"
    bodyTemp < 36.3f -> "Frio Moderado"
    bodyTemp in 36.3f..37.2f -> "Temperatura Ideal"
    else -> "Calor Excessivo"
  }

  val tempFraction = ((bodyTemp - 33.5f) / (38.5f - 33.5f)).coerceIn(0f, 1f)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF131914))
      .border(
        width = if (isShivering) 1.dp else 0.dp,
        color = if (isShivering) Color(0xFF29B6F6).copy(alpha = pulseAlpha) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
      )
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Thermostat,
          contentDescription = "Temperatura Corporal",
          tint = tempColor,
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Temperatura Corporal",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 11.sp,
          color = Color(0xFFDDDDDD),
          fontWeight = FontWeight.SemiBold
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "%.1f°C".format(bodyTemp),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.ExtraBold,
          color = tempColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "($statusDescription)",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          color = tempColor.copy(alpha = 0.9f)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Multi-color temperature bar gradient
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(Color(0xFF263328))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(tempFraction)
          .height(6.dp)
          .background(
            Brush.horizontalGradient(
              colors = listOf(
                Color(0xFF0288D1),
                Color(0xFF4CAF50),
                Color(0xFFFF9800),
                Color(0xFFE53935)
              )
            )
          )
      )
    }
  }
}

@Composable
fun StatusBadge(
  text: String,
  color: Color,
  icon: ImageVector
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(color.copy(alpha = 0.2f))
      .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = text,
        tint = color,
        modifier = Modifier.size(11.dp)
      )
      Spacer(modifier = Modifier.width(3.dp))
      Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}
