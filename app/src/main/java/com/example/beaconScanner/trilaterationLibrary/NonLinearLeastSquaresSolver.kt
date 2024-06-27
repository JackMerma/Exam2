package com.example.beaconScanner.trilaterationLibrary

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.DiagonalMatrix


class NonLinearLeastSquaresSolver(
    function: TrilaterationFunction,
    leastSquaresOptimizer: LeastSquaresOptimizer
) {
    // Solucion no lineal al problema de reduccion de minimos cuadrados
    // (reduccion de error) en la funcion de trilateracion
    // No lineal significa que usara curvas para ajustar la funcion
    protected val function: TrilaterationFunction = function
    protected val leastSquaresOptimizer: LeastSquaresOptimizer = leastSquaresOptimizer

    fun solve(
        target: DoubleArray?,
        weights: DoubleArray?,
        initialPoint: DoubleArray?,
        debugInfo: Boolean
    ): Optimum {
        if (debugInfo) {
            println("Max Number of Iterations : " + MAXNUMBEROFITERATIONS)
        }

        val leastSquaresProblem: LeastSquaresProblem =
            LeastSquaresFactory.create( // function to be optimized
                function,  // target values at optimal point in least square equation
                // (x0+xi)^2 + (y0+yi)^2 + ri^2 = target[i]
                ArrayRealVector(target, false),
                ArrayRealVector(initialPoint, false),
                DiagonalMatrix(weights),
                null,
                MAXNUMBEROFITERATIONS,
                MAXNUMBEROFITERATIONS
            )

        return leastSquaresOptimizer.optimize(leastSquaresProblem)
    }

    fun solve(target: DoubleArray?, weights: DoubleArray?, initialPoint: DoubleArray?): Optimum {
        return solve(target, weights, initialPoint, false)
    }

    fun solve(debugInfo: Boolean): Optimum {
        val numberOfPositions: Int = function.positions.size
        val positionDimension: Int = function.positions.get(0).size

        val initialPoint = DoubleArray(positionDimension)
        // initial point, use average of the vertices
        for (i in 0 until function.positions.size) {
            val vertex: DoubleArray = function.positions[i]
            for (j in vertex.indices) {
                initialPoint[j] += vertex[j]
            }
        }
        for (j in initialPoint.indices) {
            initialPoint[j] /= numberOfPositions.toDouble()
        }

        if (debugInfo) {
            val output = StringBuilder("initialPoint: ")
            for (i in initialPoint.indices) {
                output.append(initialPoint[i]).append(" ")
            }
            println(output.toString())
        }

        val target = DoubleArray(numberOfPositions)
        val distances: DoubleArray = function.distances
        val weights = DoubleArray(target.size)
        for (i in target.indices) {
            target[i] = 0.0
            weights[i] = inverseSquareLaw(distances[i])
        }

        return solve(target, weights, initialPoint, debugInfo)
    }

    private fun inverseSquareLaw(distance: Double): Double {
        return 1 / (distance * distance)
    }

    fun solve(): Optimum {
        return solve(false)
    }

    companion object {
        protected const val MAXNUMBEROFITERATIONS: Int = 5000
    }
}