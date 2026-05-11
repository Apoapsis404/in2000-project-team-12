package no.uio.ifi.in2000.team12.team12project.data.regression

import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoints
import kotlin.math.abs

/**
 * A linear regression model for climate data.
 * Fits a line to the given [years] and [temps] arrays.
 */
class LinearRegressionModel(private val years: DoubleArray, private val temps: DoubleArray) {

    init {
        require(years.isNotEmpty()) { "Years array must not be empty" }
        require(years.size == temps.size) { "Years and temps arrays must have the same size" }
    }

    private val startYear = years.first()

    /**
     * The rate of change (slope) per year.
     */
    val rate: Double

    /**
     * The y-intercept of the regression line at [startYear].
     */
    val intercept: Double

    init {
        val obs = WeightedObservedPoints()
        years.forEachIndexed { i, year ->
            obs.add(year - startYear, temps[i])
        }

        // Degree 1 = Linear Regression
        // PolynomialCurveFitter.fit returns coefficients in increasing order of degree: [intercept, slope]
        val coefficients = PolynomialCurveFitter.create(1).fit(obs.toList())

        intercept = coefficients[0]
        rate = coefficients[1]
    }

    /**
     * Predicts the temperature for a given year.
     */
    fun predict(year: Double): Double {
        return intercept + (rate * (year - startYear))
    }

    /**
     * Predicts the temperature for a given year.
     */
    fun predict(year: Int): Double = predict(year.toDouble())

    /**
     * Returns a summary of the climate trend.
     */
    fun getSummary(): String {
        val direction = if (rate >= 0) "warming" else "cooling"
        return "The data shows a $direction trend of %.3f°C per year.".format(abs(rate))
    }

    /**
     * Returns the fitted values for the given list of years.
     */
    fun fitToYears(years: List<Double>): List<Double> {
        return years.map { predict(it) }
    }
}
