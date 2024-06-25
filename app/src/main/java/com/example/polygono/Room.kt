package com.example.polygono

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.polygono.utils.Point
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.imageResource

@Composable
fun Room(
    modifier: Modifier = Modifier,
    data: ArrayList<Point>
){
    Spacer(
        modifier = modifier
            .drawWithCache {
                val path = Path()
                path.moveTo(data[0].x, data[0].y)
                for (point in data) path.lineTo(point.x, point.y)
                path.close()
                onDrawBehind {
                    drawPath(path, Color.DarkGray, style = Stroke(width = 10f))
                }
            }
            .fillMaxSize()
    )
}