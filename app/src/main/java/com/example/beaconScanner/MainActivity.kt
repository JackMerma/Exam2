package com.example.beaconScanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.beaconScanner.canvasLibrary.ui.theme.PolygonoTheme
import com.example.beaconScanner.canvasLibrary.utils.Point
import com.example.beaconScanner.canvasLibrary.utils.models.Person
import com.example.beaconScanner.canvasLibrary.utils.models.Room

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolygonoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { /* Acción Start */ }) {
                                Text("Start")
                            }
                            Button(onClick = { /* Acción Stop */ }) {
                                Text("Stop")
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Room(
                                data = loadData(),
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                            Person(
                                modifier = Modifier,
                                positionX = 100,
                                positionY = 100
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun loadData(): ArrayList<Point> {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = with(density) { configuration.screenHeightDp.dp.roundToPx() }
    val factor = screenHeight * 0.05f

    return arrayListOf(
        Point(1 * factor, 1 * factor),
        Point(8 * factor, 1 * factor),
        Point(8 * factor, 18 * factor),
        Point(1 * factor, 18 * factor),
        Point(1 * factor, 1 * factor)
    )
}