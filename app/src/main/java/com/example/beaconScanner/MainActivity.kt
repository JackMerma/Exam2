package com.example.beaconScanner

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.location.LocationManagerCompat
import com.example.beaconScanner.beaconScannerLibrary.Beacon
import com.example.beaconScanner.canvasLibrary.ui.theme.PolygonoTheme
import com.example.beaconScanner.canvasLibrary.utils.Point
import com.example.beaconScanner.canvasLibrary.utils.models.Person
import com.example.beaconScanner.canvasLibrary.utils.models.Room
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val TAG: String = "MainActivityBLE"
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var btScanner: BluetoothLeScanner
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var scanCallback: ScanCallback
    private lateinit var alertDialog: AlertDialog

    private lateinit var txtMessage: TextView
    private lateinit var txtMessage2: TextView
    private lateinit var txtMessage3: TextView
    private lateinit var trilateration: TextView


    private lateinit var permissionManager: PermissionManager
    private val beacons = HashMap<String, Beacon>();
    private val _resultBeacons = MutableStateFlow("No beacons Detected")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        BTPermissions(this).check()
        initBluetooth()

        setContent {
            PolygonoTheme {
                // Permissions
                val context = LocalContext.current
                // Initialize PermissionManager here

                permissionManager = PermissionManager.from(LocalContext.current as Activity)

                // Drawing
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
                            Button(onClick = { handleStartClick(scanCallback) }) {
                                Text("Start")
                            }
                            Button(onClick = { handleStopClick(scanCallback) }) {
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

    private fun initBluetooth() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter != null) {
            btScanner = bluetoothAdapter.bluetoothLeScanner
        } else {
            Log.d(TAG, "BluetoothAdapter is null")
        }
    }

    private fun handleStartClick(bleScanCallback: ScanCallback) {
        Log.i(TAG, "Press start scan button")
        if (!isLocationEnabled() || !isBluetoothEnabled()) {
            showPermissionDialog("Servicios no activados", "La localizacion y el Bluetooth tienen que estar activos")
            return
        }
        bluetoothScanStart(bleScanCallback)
    }

    private fun handleStopClick(bleScanCallback: ScanCallback) {
        Log.i(TAG, "Press stop scan button")
        bluetoothScanStop(bleScanCallback)
    }

    private fun isLocationEnabled(): Boolean {
        Log.i(TAG, "Verificando localizacion activo")
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        Log.d(TAG, "Localizacion activado: " + LocationManagerCompat.isLocationEnabled(locationManager))
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun isBluetoothEnabled(): Boolean {
        Log.i(TAG, "Verificando Bluetooth activo")
        Log.d(TAG, "Bluetooth activado: " + bluetoothAdapter.isEnabled)
        return bluetoothAdapter.isEnabled
    }

    private fun bluetoothScanStart(bleScanCallback: ScanCallback) {
        Log.d(TAG, "Starting Bluetooth scan...")
        if (btScanner != null) {
            permissionManager
                .request(Permission.Bluetooth)
                .rationale("Bluetooth permission is needed")
                .checkPermission { isGranted ->
                    if (isGranted) {
                        Log.d(TAG, "Permissions granted, starting scan.")
//                        val manufacturerId = 0x0118
                        val manufacturerId = 0x004C
                        val macAddress = "57:7C:45:AC:ED:FE"
                        val scanFilter = ScanFilter.Builder()
                            .setManufacturerData(manufacturerId, null) // Ejemplo para iBeacon
//                            .setDeviceAddress(macAddress)
                            .build()
                        val scanSettings = ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build()
                        btScanner.startScan(listOf(scanFilter), scanSettings,bleScanCallback)
                    } else {
                        Log.d(TAG, "Bluetooth permission not granted.")
                    }
                }
        } else {
            Log.d(TAG, "BluetoothLeScanner is null")
        }
    }

    private fun bluetoothScanStop(bleScanCallback: ScanCallback) {
        Log.d(TAG, "Stopping Bluetooth scan...")
        if (btScanner != null) {
            permissionManager
                .request(Permission.Bluetooth)
                .rationale("Bluetooth permission is needed")
                .checkPermission { isGranted ->
                    if (isGranted) {
                        Log.d(TAG, "Permissions granted, stop scan.")
                        btScanner.stopScan(bleScanCallback)
                    } else {
                        Log.d(TAG, "Bluetooth permission not granted.")
                    }
                }
        } else {
            Log.d(TAG, "BluetoothLeScanner is null")
        }
    }

    private fun showPermissionDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }

        if (alertDialog == null) {
            alertDialog = builder.create()
        }

        if (!alertDialog!!.isShowing) {
            alertDialog!!.show()
        }
    }

    private fun createBleScanCallback(): ScanCallback {
        return object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                // Implement your logic
            }

            override fun onBatchScanResults(results: List<ScanResult?>?) {
                // Implement your logic
            }

            override fun onScanFailed(errorCode: Int) {
                // Implement your logic
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