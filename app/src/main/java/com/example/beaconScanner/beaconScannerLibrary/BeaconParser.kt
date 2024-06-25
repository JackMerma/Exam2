package com.example.beaconScanner.beaconScannerLibrary

import android.bluetooth.BluetoothDevice

class BeaconParser {
    companion object {
        private const val TAG: String = "BeaconParser"

        fun parseIBeacon(data: ByteArray, mac: BluetoothDevice?, rssi: Int?): Beacon {
//            val dataLen = Integer.parseInt(Utils.toHexString(data.copyOfRange(0, 1)), 16)
//            val dataType = Integer.parseInt(Utils.toHexString(data.copyOfRange(1, 2)), 16)
//            val leFlag = Integer.parseInt(Utils.toHexString(data.copyOfRange(2, 3)), 16)
//            val adv_header = Integer.parseInt(Utils.toHexString(data.copyOfRange(0, 1)), 16)
            val adv_header = Integer.parseInt(Utils.toHexString(data.copyOfRange(0, 1)), 16)
            val companyId = Utils.toHexString(data.copyOfRange(1, 3))
            val iBeaconType = Integer.parseInt(Utils.toHexString(data.copyOfRange(4, 5)), 16)
            val iBeaconLen = Integer.parseInt(Utils.toHexString(data.copyOfRange(5, 6)), 16)
            val iBeaconUUID = Utils.toHexString(data.copyOfRange(6, 22))
            val major = Integer.parseInt(Utils.toHexString(data.copyOfRange(22, 24)), 16)
            val minor = Integer.parseInt(Utils.toHexString(data.copyOfRange(24, 26)), 16)
            //val txPower = Integer.parseInt(Utils.toHexString(data.copyOfRange(26, 29)), 16)
            val txPower = data[26].toInt()


            //val factor = (-1 * txPower - rssi!!) / (10 * 4.0)
            //val distance = Math.pow(10.0, factor)

            /*Log.d(
                TAG,
                "DECODE dataLen:$dataLen dataType:$dataType leFlag:$leFlag len:$len type:$type subtype:$subtype subtypeLen:$subtypeLen company:$company UUID:$iBeaconUUID major:$major minor:$minor txPower:$txPower"
            )*/

            return Beacon(
                macAddress = mac?.address,
                manufacturer = companyId,
                type = iBeaconType,
                uuid = iBeaconUUID,
                major = major,
                minor = minor,
                rssi = rssi,
                txPower = txPower,
            )
        }
    }
}