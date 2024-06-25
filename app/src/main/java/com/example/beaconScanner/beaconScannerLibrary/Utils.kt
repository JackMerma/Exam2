package com.example.beaconScanner.beaconScannerLibrary

object Utils {
    @OptIn(ExperimentalStdlibApi::class)
    fun toHexString(bytes:ByteArray):String{
        return bytes.toHexString()
    }
}