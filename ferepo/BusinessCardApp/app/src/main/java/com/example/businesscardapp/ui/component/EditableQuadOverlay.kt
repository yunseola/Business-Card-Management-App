// 위치: ui/component/EditableQuadOverlay.kt
package com.example.businesscardapp.ui.component

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun EditableQuadOverlay(
    corners: List<Offset>,
    onCornerMoved: (index: Int, newOffset: Offset) -> Unit,
    imageWidth: Int,
    imageHeight: Int,
    canvasWidth: Int,
    canvasHeight: Int,
    imageOffsetX: Int,
    imageOffsetY: Int
) {
    val scaleX = canvasWidth.toFloat() / imageWidth.toFloat()
    val scaleY = canvasHeight.toFloat() / imageHeight.toFloat()
    val scale = minOf(scaleX, scaleY)
    val scaledCorners = corners.map {
        Offset(it.x * scale + imageOffsetX, it.y * scale + imageOffsetY)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (scaledCorners.size == 4) {
                drawLine(Color.Yellow, scaledCorners[0], scaledCorners[1], strokeWidth = 4f)
                drawLine(Color.Yellow, scaledCorners[1], scaledCorners[2], strokeWidth = 4f)
                drawLine(Color.Yellow, scaledCorners[2], scaledCorners[3], strokeWidth = 4f)
                drawLine(Color.Yellow, scaledCorners[3], scaledCorners[0], strokeWidth = 4f)
            }
        }

        corners.forEachIndexed { index, point ->
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (point.x * scale + imageOffsetX - 24).roundToInt(),
                            y = (point.y * scale + imageOffsetY - 24).roundToInt()
                        )
                    }
                    .size(48.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newOffset = point + dragAmount / scale
                            onCornerMoved(index, newOffset)
                            Log.d("EditableQuadOverlay", "Moved corner $index → x=${newOffset.x}, y=${newOffset.y}")
                        }
                    }
            )
        }
    }
}
