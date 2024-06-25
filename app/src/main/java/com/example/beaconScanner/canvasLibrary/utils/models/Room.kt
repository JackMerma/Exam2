package com.example.beaconScanner.canvasLibrary.utils.models

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.beaconScanner.canvasLibrary.utils.Point
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Path

@Composable
fun Room(
    modifier: Modifier = Modifier,
    data: ArrayList<Point>
){
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val path = Path().apply {
            moveTo(data[0].x, data[0].y)
            for (point in data) {
                lineTo(point.x, point.y)
            }
            close()
        }
        drawPath(path, Color.DarkGray, style = Stroke(width = 10f))
    }
}