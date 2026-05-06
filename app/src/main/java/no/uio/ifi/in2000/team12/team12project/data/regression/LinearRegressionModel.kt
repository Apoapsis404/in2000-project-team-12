package no.uio.ifi.in2000.team12.team12project.data.regression
import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoints

class LinearRegressionModel(val years: DoubleArray, val temps: DoubleArray) {
    private val startYear = years.first()
    private val coefficients: DoubleArray // [intercept, slope]

    val rate: Double
    val intercept: Double

    init {
        val obs = WeightedObservedPoints()
        years.forEachIndexed { i, year ->
            obs.add(year - startYear, temps[i])
        }

        // Degree 1 = Linear Regression
        coefficients = PolynomialCurveFitter.create(1).fit(obs.toList())

        intercept = coefficients[0]
        rate = coefficients[1]
    }

    /**
     * Predicts the temperature for a future year.
     */
    fun predict(year: Int): Double {
        return intercept + (rate * (year - startYear))
    }

    /**
     * Returns a summary of the climate trend.
     */
    fun getSummary(): String {
        val direction = if (rate > 0) "warming" else "cooling"
        return "The data shows a $direction trend of %.3f°C per year.".format(rate)
    }

    fun fitToYears(years: List<Double>): List<Double> {
        return years.map { predict(it.toInt()) }
    }
}
