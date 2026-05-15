package no.uio.ifi.in2000.team12.team12project.data.regression

import org.junit.Test

class TestRegressionAnalysis {

    private val value = doubleArrayOf(3.6, -2.55, 2.4, 3.0, 3.3, 2.9, -0.4, 2.65, 4.1, 3.6)
    private val years = doubleArrayOf(2017.0, 2018.0, 2019.0, 2020.0, 2021.0, 2022.0, 2023.0, 2024.0, 2025.0, 2026.0)

    @Test
    fun testLinearRegression() {
        val model = LinearRegressionModel(years, value)
        val predictedValue = model.predict(2027)
        println("Predicted value for 2027: $predictedValue")
        println(model.getSummary())
    }
}