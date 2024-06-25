package com.example.beaconScanner

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.location.LocationManagerCompat
import com.example.beaconScanner.beaconScannerLibrary.Beacon
import com.example.beaconScanner.beaconScannerLibrary.BeaconParser
import com.example.beaconScanner.beaconScannerLibrary.BleScanCallback
import com.example.beaconScanner.canvasLibrary.ui.theme.PolygonoTheme
import com.example.beaconScanner.canvasLibrary.utils.Point
import com.example.beaconScanner.canvasLibrary.utils.models.Person
import com.example.beaconScanner.canvasLibrary.utils.models.Room
import com.example.beaconScanner.trilaterationLibrary.LinearLeastSquaresSolver
import com.example.beaconScanner.trilaterationLibrary.NonLinearLeastSquaresSolver
import com.example.beaconScanner.trilaterationLibrary.TrilaterationFunction
import kotlinx.coroutines.flow.MutableStateFlow
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private val TAG: String = "MainActivityBLE"
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var btScanner: BluetoothLeScanner
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var scanCallback: BleScanCallback
    private lateinit var alertDialog: AlertDialog
    private var FACTOR_X = 0.10
    private var FACTOR_Y = 0.10

    //    private lateinit var permissionManager: PermissionManager
    private val beacons = HashMap<String, Beacon>();
    private val _resultBeacons = MutableStateFlow("No beacons Detected")
    val trilateration = mutableStateOf("-")
    val pointX_position = mutableStateOf(100.0)
    val pointY_position = mutableStateOf(100.0)
    val ROOM_LENGTH = 600.0
    val ROOM_HEIGHT = 545.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        BTPermissions(this).check()
        scanCallback = createBleScanCallback()
        initBluetooth()
        
        setContent {
            PolygonoTheme {
                // Permissions
//                val context = LocalContext.current
                // Initialize PermissionManager here

//                permissionManager = PermissionManager.from(LocalContext.current as Activity)
//                BeaconScanPermissionsScreen()
                // Drawing
                val points = loadData()
                val canvas_length = distanciaEntrePuntos(points[0],points[1])
                val canvas_height = distanciaEntrePuntos(points[1],points[3])

                FACTOR_X = canvas_length / ROOM_LENGTH
                FACTOR_Y = canvas_height / ROOM_HEIGHT

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
                                .padding(2.dp),
                        ){
                            Text(text = trilateration.value)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                        ) {
                            Button(
                                onClick = { handleStartClick(scanCallback) },
                            ) {
                                Text("Start")
                            }
                            Button(
                                onClick = { handleStopClick(scanCallback) }
                            ) {
                                Text("Stop")
                            }
                            BeaconScanPermissionsScreen()
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                        ) {
                            Room(
                                data = points,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                            Person(
                                modifier = Modifier,
                                positionX = pointX_position.value,
                                positionY = pointY_position.value
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

    @SuppressLint("MissingPermission")
    private fun bluetoothScanStart(bleScanCallback: ScanCallback) {
        Log.d(TAG, "Starting Bluetooth scan...")
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
    }

    @SuppressLint("MissingPermission")
    private fun bluetoothScanStop(bleScanCallback: ScanCallback) {
        Log.d(TAG, "Stopping Bluetooth scan...")
        btScanner.stopScan(bleScanCallback)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBleScanCallback(): BleScanCallback {
        return BleScanCallback(
            onScanResultAction,
            onBatchScanResultAction,
            onScanFailedAction
        )
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    private val onScanResultAction: (ScanResult?) -> Unit = { result ->
        Log.d(TAG, "onScanResultAction")

        val mac = result?.device
        val scanRecord = result?.scanRecord

//        if (scanRecord != null) {
//            Log.d(TAG, "Scan: ${scanRecord.getServiceData()}")
//        }
//
//        if (scanRecord != null) {
//            Log.d(TAG, "Scan: ${toString(scanRecord.getManufacturerSpecificData())}")
//        }

        val rssi = result?.rssi

        if (scanRecord != null) {
            scanRecord.bytes?.let {
                val parserBeacon = BeaconParser.parseIBeacon(it, mac, rssi)

                // Definir una lista de UUIDs permitidos
                val allowedUUIDs = listOf(
                    "2f234454cf6d4a0fadf2f4911ba9ffb6",
                    "2f234454cf6d4a0fadf2f4911ba9ffb7",
                    "2f234454cf6d4a0fadf2f4911ba9fff8"
                    // Añade más UUIDs aquí
                )

                // Verificar si el UUID del beacon está en la lista de UUIDs permitidos
                if (allowedUUIDs.contains(parserBeacon.uuid)) {
                    // Si el beacon no está ya en el mapa de beacons, lo agrega.
                    if (!beacons.containsKey(parserBeacon.uuid)) {
                        parserBeacon.uuid?.let { it1 -> beacons[it1] = parserBeacon }
                    }

                    // Recupera el beacon del mapa de beacons.
                    val beaconSave = beacons[parserBeacon.uuid]

                    if (beaconSave != null) {
                        // Actualiza el RSSI del beacon guardado.
                        beaconSave.rssi = parserBeacon.rssi

                        Log.d(TAG, "uuid ${beaconSave.uuid}")

                        // Calcula la distancia usando el txPower y el RSSI del beacon.
                        val distance = parserBeacon.txPower?.let { txPower ->
                            parserBeacon.rssi?.let { rssi ->
                                beaconSave.calculateDistance(txPower = txPower, rssi = rssi)
                            }
                        }
                        beaconSave.distance = distance?.toFloat()
                        Log.d(TAG, beaconSave.toString() + " distance en el beacon " + beaconSave.distance)

                        // Actualiza el LiveData _resultBeacons con los detalles del beacon y la distancia.
                        _resultBeacons.value = beaconSave.toString() + " distance " + distance

                        Log.d(TAG, beaconSave.uuid + " distance " + distance)
                        
                        var txtMessage = ""
                        var txtMessage2 = ""
                        var txtMessage3 = ""
                        
                        // Formatea la distancia a dos decimales.
                        val rounded_distance = String.format("%.2f", distance).toDouble()
                        when (allowedUUIDs.indexOf(parserBeacon.uuid)) {
                            0 -> txtMessage = beaconSave.toString() + " distance " + rounded_distance
                            1 -> txtMessage2 = beaconSave.toString() + " distance " + rounded_distance
                            2 -> txtMessage3 = beaconSave.toString() + " distance " + rounded_distance
                        }
                        Log.d("Beacons", txtMessage)
                        Log.d("Beacons", txtMessage2)
                        Log.d("Beacons", txtMessage3)
//                        val positions = arrayOf(
//                            doubleArrayOf(0.0, 0.0),
//                            doubleArrayOf(730.0, 0.0),
//                            doubleArrayOf(730.0, 775.0)
//                            )
                        val positions = arrayOf(
                            doubleArrayOf(0.0, 0.0),
                            doubleArrayOf(ROOM_LENGTH, 0.0),
                            doubleArrayOf(ROOM_LENGTH, ROOM_HEIGHT)
                        )

                        val b1 = "2f234454cf6d4a0fadf2f4911ba9ffb6"
                        val b2 = "2f234454cf6d4a0fadf2f4911ba9ffb7"
                        val b3 = "2f234454cf6d4a0fadf2f4911ba9fff8"
                        if(beacons.get(b1)?.distance != null &&
                            beacons.get(b2)?.distance != null &&
                            beacons.get(b3)?.distance != null){
                            var distances= doubleArrayOf(
                                beacons.get(b1)?.distance!!.toDouble(),
                                beacons.get(b2)?.distance!!.toDouble(),
                                beacons.get(b3)?.distance!!.toDouble()
                            )
//                            Log.d("distances", beacons.get(b1)?.distance.toString() + "/" + beacons.get(b2)?.distance.toString() + "/  "/* beacons.get(b3)?.distance.toString()*/)
                            trilateration2DZeroDistance(positions, distances)
                        }


                        // Actualiza el mensaje de texto con los detalles del beacon y la distancia redondeada.
                        //txtMessage.text = beaconSave.toString() + " distance " + rounded_distance
                    }
                }
            }
        }
    }

    private val onBatchScanResultAction: (MutableList<ScanResult>?) -> Unit = {
        Log.d(TAG, "BatchScanResult: ${it.toString()}")
    }

    private val onScanFailedAction: (Int) -> Unit = {
        Log.d(TAG, "ScanFailed: $it")
    }

    fun trilateration2DZeroDistance(positions: Array<DoubleArray>, distance: DoubleArray){
        var trilaterationFunction = TrilaterationFunction(positions, distance)
        var lineal = LinearLeastSquaresSolver(trilaterationFunction)
        var nolineal = NonLinearLeastSquaresSolver(trilaterationFunction, LevenbergMarquardtOptimizer())

        var linealSolve = lineal.solve()
        var nolinealSolve =  nolineal.solve()

        var lineals = printDoubleArray(linealSolve.toArray())
        var nonlinea = printDoubleArray(nolinealSolve.getPoint().toArray())

//        Log.d("solutions", "linealSolve ${linealSolve}")
        Log.d("solutions", "no linealSolve ${nolinealSolve.point}")
        val input = nolinealSolve.point.toString()
        val numbers = input.substring(1, input.length - 1) // Elimina las llaves
            .split("[; ]".toRegex()) // Divide por punto y coma o espacio
//            .map { it.toDouble() } // Convierte cada parte a Double
        Log.d("solutions", "x: ${numbers[0].toDouble()}"+" y: ${numbers[2].toDouble()}")
        trilateration.value = " trilateracion no lineal " + nolinealSolve.point + "\n" + "lineal " + linealSolve
        pointX_position.value = numbers[0].toDouble() * FACTOR_X
        pointY_position.value = numbers[2].toDouble() * FACTOR_Y
    }


    private fun printDoubleArray(values: DoubleArray) : String {
        var output = ""
        for (p in values) {
            output = output + "p -- "
        }
        output = "\n"
        return output
    }
}

fun distanciaEntrePuntos(punto1: Point, punto2: Point): Float {
    val dx = punto2.x - punto1.x
    val dy = punto2.y - punto1.y
    return sqrt(dx * dx + dy * dy)
}

@Composable
fun loadData(): ArrayList<Point> {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = with(density) { configuration.screenHeightDp.dp.roundToPx() }
    val factor = screenHeight * 0.05f

    return arrayListOf(
        Point(1 * factor, 1 * factor), // inicio
        Point(8 * factor, 1 * factor), // x
        Point(8 * factor, 15 * factor),
        Point(1 * factor, 15 * factor), // y
        Point(1 * factor, 1 * factor)
    )
}