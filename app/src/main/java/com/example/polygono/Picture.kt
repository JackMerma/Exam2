package com.example.polygono

import android.icu.text.Transliterator.Position
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import com.example.polygono.utils.Point



@Composable
fun Picture(
    modifier: Modifier = Modifier,
    image: ImageBitmap,
    positionX: Int,
    positionY: Int,
){
    val imageScale = remember {
        mutableFloatStateOf(0.40f)
    }
    val posX = remember {
        mutableStateOf(positionX.dp)
    }
    val posY = remember {
        mutableStateOf(positionY.dp)
    }
    val isPressed = remember {
        mutableStateOf(false)
    }
    Canvas(
        modifier = modifier
            .size(100.dp, 100.dp)
            .offset(posX.value, posY.value)
            .border(0.5.dp, Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        try {
                            isPressed.value = true
                            imageScale.floatValue = 1.70f
                            posX.value = 174.dp
                            posY.value = 415.dp
                            awaitRelease()
                        } finally {
                            isPressed.value = false
                            imageScale.floatValue = 0.40f
                            posX.value = positionX.dp
                            posY.value = positionY.dp
                        }
                    },
                )
            },
    ) {
        withTransform({
            scale(imageScale.floatValue)
            clipRect(450f, right = -160f,bottom = 500f, top = -650f)
        }) {
            drawImage(image, topLeft = Offset(-575f,-160f))
        }

    }
}
