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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun Person(
    modifier: Modifier = Modifier,
    positionX: Int,
    positionY: Int,
){
    val posX = remember {
        mutableStateOf(positionX.dp)
    }
    val posY = remember {
        mutableStateOf(positionY.dp)
    }
    fun updatePosition() {
        posX.value = Random.nextInt(60, 174).dp
        posY.value = Random.nextInt(85, 830).dp
    }
    Canvas(
        modifier = modifier
            .size(50.dp, 50.dp)
            .offset(posX.value, posY.value)
            .border(1.dp, Color.Black)
            .pointerInput(Unit) {
                detectTapGestures (
                    onTap = {
                        updatePosition()
                    }
                )
            },
    ) {
        withTransform({
            scale(0.7f)
            clipRect(450f, right = -160f,bottom = 500f, top = -650f)
        }) {
            drawCircle(Color.Magenta)
        }
    }
}
