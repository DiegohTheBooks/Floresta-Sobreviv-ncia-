package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
  onMove: (Float, Float, Float) -> Unit, // dx, dy, angle
  modifier: Modifier = Modifier
) {
  val baseRadius = 55f // dp
  var thumbOffsetX by remember { mutableFloatStateOf(0f) }
  var thumbOffsetY by remember { mutableFloatStateOf(0f) }

  Box(
    modifier = modifier
      .size(110.dp)
      .clip(CircleShape)
      .background(Color(0x55000000))
      .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = {},
          onDragEnd = {
            thumbOffsetX = 0f
            thumbOffsetY = 0f
            onMove(0f, 0f, 0f)
          },
          onDragCancel = {
            thumbOffsetX = 0f
            thumbOffsetY = 0f
            onMove(0f, 0f, 0f)
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val newX = thumbOffsetX + dragAmount.x
            val newY = thumbOffsetY + dragAmount.y
            val dist = sqrt(newX * newX + newY * newY)
            val maxDist = 90f

            if (dist > maxDist) {
              val angle = atan2(newY, newX)
              thumbOffsetX = cos(angle) * maxDist
              thumbOffsetY = sin(angle) * maxDist
            } else {
              thumbOffsetX = newX
              thumbOffsetY = newY
            }

            val normalizedX = (thumbOffsetX / maxDist).coerceIn(-1f, 1f)
            val normalizedY = (thumbOffsetY / maxDist).coerceIn(-1f, 1f)
            val facingAngle = atan2(thumbOffsetY, thumbOffsetX)
            onMove(normalizedX, normalizedY, facingAngle)
          }
        )
      }
      .testTag("virtual_joystick"),
    contentAlignment = Alignment.Center
  ) {
    // Thumb knob
    Box(
      modifier = Modifier
        .offset { IntOffset(thumbOffsetX.roundToInt(), thumbOffsetY.roundToInt()) }
        .size(46.dp)
        .clip(CircleShape)
        .background(Color(0xCC81C784))
        .border(1.5.dp, Color.White, CircleShape)
    )
  }
}
