package com.example.pacmanclone.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun DPad(onDirectionChange: (Direction) -> Unit) {
    var boxSize by remember { mutableStateOf(Size.Zero) }
    Box(
        modifier = Modifier
            .size(200.dp)
            .onSizeChanged { newSize ->
                boxSize = Size(newSize.width.toFloat(), newSize.height.toFloat())
            }
            .drawBehind {
                val armThickness = size.minDimension / 3
                // Horizontal arm
                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset(0f, (size.height - armThickness) / 2),
                    size = Size(size.width, armThickness),
                    cornerRadius = CornerRadius(armThickness / 2, armThickness / 2)
                )
                // Vertical arm
                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset((size.width - armThickness) / 2, 0f),
                    size = Size(armThickness, size.height),
                    cornerRadius = CornerRadius(armThickness / 2, armThickness / 2)
                )
            }
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        if (boxSize != Size.Zero) {
                            val center = Offset(boxSize.width / 2, boxSize.height / 2)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val newDirection = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                if (dx > 0) Direction.RIGHT else Direction.LEFT
                            } else {
                                if (dy > 0) Direction.DOWN else Direction.UP
                            }
                            onDirectionChange(newDirection)
                            try {
                                awaitRelease()
                            } finally {
                                onDirectionChange(Direction.NONE)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        if (boxSize != Size.Zero) {
                            val center = Offset(boxSize.width / 2, boxSize.height / 2)
                            val pos = change.position
                            val dx = pos.x - center.x
                            val dy = pos.y - center.y
                            val newDirection = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                if (dx > 0) Direction.RIGHT else Direction.LEFT
                            } else {
                                if (dy > 0) Direction.DOWN else Direction.UP
                            }
                            onDirectionChange(newDirection)
                        }
                    },
                    onDragEnd = { onDirectionChange(Direction.NONE) },
                    onDragCancel = { onDirectionChange(Direction.NONE) }
                )
            }
    )
}
