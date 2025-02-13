// DPad.kt
package com.example.pacmanclone.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun DPad(onDirectionChange: (Direction) -> Unit) {
    // Store the size of the DPad for calculating its center.
    var boxSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = Modifier
            .size(200.dp)
            .onSizeChanged { newSize ->
                boxSize = Size(newSize.width.toFloat(), newSize.height.toFloat())
            }
            // Draw a custom D-pad (a cross formed by two rounded rectangles).
            .drawBehind {
                val armThickness = size.minDimension / 3
                // Horizontal arm: full width, centered vertically.
                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset(0f, (size.height - armThickness) / 2),
                    size = Size(size.width, armThickness),
                    cornerRadius = CornerRadius(armThickness / 2, armThickness / 2)
                )
                // Vertical arm: full height, centered horizontally.
                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset((size.width - armThickness) / 2, 0f),
                    size = Size(armThickness, size.height),
                    cornerRadius = CornerRadius(armThickness / 2, armThickness / 2)
                )
            }
            .background(Color.Transparent)
            // First, use detectTapGestures to register immediate touch.
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        if (boxSize != Size.Zero) {
                            // Immediately determine direction based on the initial touch.
                            val center = Offset(boxSize.width / 2, boxSize.height / 2)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val newDirection = if (abs(dx) > abs(dy)) {
                                if (dx > 0) Direction.RIGHT else Direction.LEFT
                            } else {
                                if (dy > 0) Direction.DOWN else Direction.UP
                            }
                            onDirectionChange(newDirection)
                            // Wait for release; once released, reset the direction.
                            try {
                                awaitRelease()
                            } finally {
                                onDirectionChange(Direction.NONE)
                            }
                        }
                    }
                )
            }
            // Also attach a drag detector for continuous updates.
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        if (boxSize != Size.Zero) {
                            val center = Offset(boxSize.width / 2, boxSize.height / 2)
                            val pos = change.position
                            val dx = pos.x - center.x
                            val dy = pos.y - center.y
                            val newDirection = if (abs(dx) > abs(dy)) {
                                if (dx > 0) Direction.RIGHT else Direction.LEFT
                            } else {
                                if (dy > 0) Direction.DOWN else Direction.UP
                            }
                            onDirectionChange(newDirection)
                        }
                    },
                    onDragEnd = {
                        onDirectionChange(Direction.NONE)
                    },
                    onDragCancel = {
                        onDirectionChange(Direction.NONE)
                    }
                )
            }
    )
}
