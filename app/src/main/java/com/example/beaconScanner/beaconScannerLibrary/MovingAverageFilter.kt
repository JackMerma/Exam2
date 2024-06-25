package com.example.beaconScanner.beaconScannerLibrary

import android.util.Log
import java.util.LinkedList

class MovingAverageFilter(private val size: Int) {
    val distanceQueue = LinkedList<Double>()
    private val TAG = "EMA Filter";
    private var x: Float? = null
    private var cov: Float = 0.0f


    fun calculateDistance(txPower:Int, rssi: Int): Double {
        val rssi = filter(signal= rssi.toFloat(), R=0.01f,Q=3.0f);
        val n = 3.0
        //val factor = (-1 * txPower - rssi) / (10 * n)
        val factor = (txPower - rssi) / (10 * n)

        val distance = Math.pow(10.0, factor)
        Log.d(TAG, "distance: ${distance}")

        var EMA = distance

        if (distanceQueue.size == size) {
            val alpha = 0.99 // Factor de suavizado (0 < alpha < 1)
            val previousAverage = distanceQueue.last
            EMA = alpha * distance + (1 - alpha) * previousAverage
            Log.d(TAG, "Using EMA filter: $EMA")
        }
        Log.d(TAG, "movingAverage: ${EMA}")

        distanceQueue.add(distance)
        if (distanceQueue.size > size) {
            distanceQueue.remove()
        }
        return EMA
    }


    fun filter(signal: Float, u: Float = 0.0f,R: Float,
               Q: Float,
               A: Float = 1.0f,
               B: Float = 0.0f,
               C: Float = 1.0f): Float {
        fun square(x: Float) = x * x

        fun predict(x: Float, u: Float): Float = (A * x) + (B * u)

        fun uncertainty(): Float = (square(A) * cov) + R
        val x: Float? = this.x

        if (x == null) {
            this.x = (1 / C) * signal
            cov = square(1 / C) * Q
        } else {
            val prediction: Float = predict(x, u)
            val uncertainty: Float = uncertainty()

            // kalman gain
            val k_gain: Float = uncertainty * C * (1 / ((square(C) * uncertainty) + Q))

            // correction
            this.x = prediction + k_gain * (signal - (C * prediction))
            cov = uncertainty - (k_gain * C * uncertainty)
        }

        return this.x!!
    }
}