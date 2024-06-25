package com.example.polygono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.polygono.ui.theme.PolygonoTheme
import com.example.polygono.utils.Point

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolygonoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Room(
                        data = loadData(),
                        modifier = Modifier.padding(innerPadding)
                    )
                    Picture(
                        image = loadImage(image = R.drawable.momia),
                        positionX = 60,
                        positionY = 85
                    )
                    Picture(
                        image = loadImage(image = R.drawable.momia),
                        positionX = 174,
                        positionY = 85
                    )
                    Picture(
                        image = loadImage(image = R.drawable.momia),
                        positionX = 290,
                        positionY = 85
                    )
                    Picture(
                        image = loadImage(image = R.drawable.momia),
                        positionX = 60,
                        positionY = 195
                    )
                    Person(
                        modifier = Modifier,
                        positionX = 174,
                        positionY = 415
                    )
                }
            }
        }
    }
}

@Composable
fun loadData(): ArrayList<Point>{
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = with(density){configuration.screenHeightDp.dp.roundToPx()}
    val factor = screenHeight*(0.05).toFloat()

    val points = ArrayList<Point>()
    points.add(Point(1 * factor, 1 * factor))
    points.add(Point(8 * factor, 1 * factor))
    points.add(Point(8 * factor, 18 * factor))
    points.add(Point(1 * factor, 18 * factor))
    points.add(Point(1 * factor, 1 * factor))

    return points
}

@Composable
fun loadImage(image: Int): ImageBitmap{
    return ImageBitmap.imageResource(id = image)
}


