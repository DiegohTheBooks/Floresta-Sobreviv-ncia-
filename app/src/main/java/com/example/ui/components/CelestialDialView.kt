package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DayNightCycleManager
import com.example.domain.DayPhase
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CelestialDialView(
  timeMinutes: Int,
  dayNumber: Int,
  modifier: Modifier = Modifier
) {
  val phase = DayNightCycleManager.getDayPhase(timeMinutes)
  val celestialAngle = DayNightCycleManager.getCelestialAngle(timeMinutes)
  val hours = (timeMinutes / 60) % 24
  val minutes = timeMinutes % 60
  val formattedTime = "%02d:%02d".format(hours, minutes)

  val infiniteTransition = rememberInfiniteTransition(label = "celestialAnim")
  val starTwinkle by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
    label = "starTwinkle"
  )

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xE6101712),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334B38)),
    shadowElevation = 4.dp,
    modifier = modifier.testTag("celestial_dial_view")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // 1. Mini Sky Arc with Sun / Moon
      Box(
        modifier = Modifier
          .size(34.dp, 28.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(
            if (phase.isDark) Color(0xFF0D1726) else Color(0xFF81D4FA).copy(alpha = 0.4f)
          )
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val cx = w / 2f
          val cy = h * 0.95f
          val arcRadius = w * 0.44f

          // Celestial trajectory arc
          drawArc(
            color = Color(0x33FFFFFF),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - arcRadius, cy - arcRadius),
            size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(1.5f)
          )

          // Position of Sun or Moon along the arc
          // Convert minutes into semi-circle radians
          val mDay = (timeMinutes % 1440).toFloat()
          // 06:00 (360) is start (left), 12:00 (720) is top, 18:00 (1080) is right
          val angleRad = when {
            mDay in 360f..1080f -> {
              // Day arc (Sun)
              val t = (mDay - 360f) / 720f
              Math.PI * (1.0 - t)
            }
            else -> {
              // Night arc (Moon)
              val nightM = if (mDay > 1080f) mDay - 1080f else mDay + 360f
              val t = nightM / 720f
              Math.PI * (1.0 - t)
            }
          }

          val orbX = cx + cos(angleRad).toFloat() * arcRadius
          val orbY = cy - sin(angleRad).toFloat() * arcRadius

          if (!phase.isDark) {
            // Sun (bright golden orb with glow)
            drawCircle(color = Color(0x66FFD54F), radius = 6f, center = Offset(orbX, orbY))
            drawCircle(color = Color(0xFFFFD54F), radius = 3.5f, center = Offset(orbX, orbY))
          } else {
            // Moon (soft silvery-cyan crescent/orb)
            drawCircle(color = Color(0x5581D4FA), radius = 5f, center = Offset(orbX, orbY))
            drawCircle(color = Color(0xFFE0F7FA), radius = 3f, center = Offset(orbX, orbY))
          }
        }
      }

      // 2. Day & Clock Text
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${phase.iconEmoji} Dia $dayNumber • $formattedTime",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 11.sp
          )
        }
        Text(
          text = phase.displayName,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          fontWeight = FontWeight.SemiBold,
          color = when (phase) {
            DayPhase.ALVORADA, DayPhase.ENTARDECER -> Color(0xFFFFCC80)
            DayPhase.MANHA, DayPhase.MEIO_DIA -> Color(0xFFFFF59D)
            DayPhase.CREPUSCULO -> Color(0xFFCE93D8)
            else -> Color(0xFF80D8FF)
          }
        )
      }
    }
  }
}
