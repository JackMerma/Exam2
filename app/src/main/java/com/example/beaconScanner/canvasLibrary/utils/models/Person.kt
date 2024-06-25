package com.example.beaconScanner.canvasLibrary.utils.models

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun Person(
    modifier: Modifier = Modifier,
    positionX: Double,
    positionY: Double,
){
//    val posX = remember { mutableStateOf(positionX.dp) }
//    val posY = remember { mutableStateOf(positionY.dp) }
//
//    fun updatePosition(X: Double, Y: Double) {
//        posX.value = X.dp
//        posY.value = Y.dp
//    }

    Canvas(
        modifier = modifier
            .size(50.dp)
            .offset(positionX.dp, positionY.dp)
            .border(1.dp, Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
//                    updatePosition()
                }
            },
    ) {
        drawCircle(Color.Magenta)
    }
//    updatePosition(positionX, positionY)
}