package com.example.beaconScanner.trilaterationLibrary

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector

class LinearLeastSquaresSolver(function: TrilaterationFunction) {
    // Solucion lineal al problema de reduccion de minimos cuadrados
    // (reduccion de error) en la funcion de trilateracion
    // lineal significa que usara vectores (lineas rectas) para ajustar la funcion
    protected val function: TrilaterationFunction = function

    @JvmOverloads
    fun solve(debugInfo: Boolean = false): RealVector {
        val numberOfPositions: Int = function.positions.size
        val positionDimension: Int = function.positions[0].size

        val Ad = Array(numberOfPositions - 1) {
            DoubleArray(
                positionDimension
            )
        }

        for (i in 1 until numberOfPositions) {
            val Adi = DoubleArray(positionDimension)
            for (j in 0 until positionDimension) {
                Adi[j] =
                    function.positions.get(i).get(j) - function.positions.get(0).get(j)
            }
            Ad[i - 1] = Adi
        }
        if (debugInfo) {
            println(Array2DRowRealMatrix(Ad))
        }

        // reference point is function.positions[0], with distance function.distances[0]
        val referenceDistance: Double = function.distances.get(0)
        val r0squared = referenceDistance * referenceDistance
        val bd = DoubleArray(numberOfPositions - 1)
        for (i in 1 until numberOfPositions) {
            val ri: Double = function.distances.get(i)
            val risquared = ri * ri

            // find distance between ri and r0
            var di0squared = 0.0
            for (j in 0 until positionDimension) {
                val dij0j: Double =
                    function.positions.get(i).get(j) - function.positions.get(0).get(j)
                di0squared += dij0j * dij0j
            }
            bd[i - 1] = 0.5 * (r0squared - risquared + di0squared)
        }
        if (debugInfo) {
            println(ArrayRealVector(bd))
        }

        val A: RealMatrix = Array2DRowRealMatrix(Ad, false)
        val b: RealVector = ArrayRealVector(bd, false)
        val solver = QRDecomposition(A).solver
        val x = if (!solver.isNonSingular) {
            // bummer...
            ArrayRealVector(DoubleArray(positionDimension))
        } else {
            solver.solve(b)
        }

        return x.add(ArrayRealVector(function.positions.get(0)))
    }
}