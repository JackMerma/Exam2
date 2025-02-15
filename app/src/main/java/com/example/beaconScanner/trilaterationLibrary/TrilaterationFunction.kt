package com.example.beaconScanner.trilaterationLibrary

import android.util.Log
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.util.Pair
import kotlin.math.max

class TrilaterationFunction(positions: Array<DoubleArray>, distances: DoubleArray) :
    MultivariateJacobianFunction {
        // Funcion que va representar el comportamiento de
        // la matriz de posiciones a partir de 3 puntos estaticos en
        // coordenadas X e Y
    /**
     * Known positions of static nodes
     */
    val positions: Array<DoubleArray>

    /**
     * Euclidean distances from static nodes to mobile node
     */
    val distances: DoubleArray


    init {
        require(positions.size >= 2) { "Need at least two positions." }

        require(positions.size == distances.size) { "The number of positions you provided, " + positions.size + ", does not match the number of distances, " + distances.size + "." }

        // bound distances to strictly positive domain
        for (i in distances.indices) {
            distances[i] = max(distances[i], epsilon)
        }

        val positionDimension = positions[0].size
        for (i in 1 until positions.size) {
            require(positionDimension == positions[i].size) { "The dimension of all positions should be the same." }
        }

        this.positions = positions
        this.distances = distances
        Log.d("positions",positions[0][1].toString())
        Log.d("distances",distances[0].toString() +" / " + distances[1].toString())
    }

    /**
     * Calculate and return Jacobian function Actually return initialized function
     *
     * Jacobian matrix, [i][j] at
     * J[i][0] = delta_[(x0-xi)^2 + (y0-yi)^2 - ri^2]/delta_[x0] at
     * J[i][1] = delta_[(x0-xi)^2 + (y0-yi)^2 - ri^2]/delta_[y0] partial derivative with respect to the parameters passed to value() method
     *
     * @param point for which to calculate the slope
     * @return Jacobian matrix for point
     */
    fun jacobian(point: RealVector): RealMatrix {
        val pointArray = point.toArray()

        val jacobian = Array(distances.size) {
            DoubleArray(
                pointArray.size
            )
        }
        for (i in jacobian.indices) {
            for (j in pointArray.indices) {
                jacobian[i][j] = 2 * pointArray[j] - 2 * positions[i][j]
            }
        }

        return Array2DRowRealMatrix(jacobian)
    }

    override fun value(point: RealVector): Pair<RealVector, RealMatrix> {
        // input

        val pointArray = point.toArray()

        // output
        val resultPoint = DoubleArray(distances.size)

        // compute least squares
        for (i in resultPoint.indices) {
            resultPoint[i] = 0.0
            // calculate sum, add to overall
            for (j in pointArray.indices) {
                resultPoint[i] += (pointArray[j] - positions[i][j]) * (pointArray[j] - positions[i][j])
            }
            resultPoint[i] -= (distances[i]) * (distances[i])
        }

        val jacobian = jacobian(point)
        return Pair(ArrayRealVector(resultPoint), jacobian)
    }

    companion object {
        protected const val epsilon: Double = 1E-7
    }
}