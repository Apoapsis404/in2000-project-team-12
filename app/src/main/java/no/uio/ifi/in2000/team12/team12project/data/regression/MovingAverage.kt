package no.uio.ifi.in2000.team12.team12project.data.regression

fun calculateWeightedMovingAverage(values: List<Double>, windowSize: Int): List<Double> {
    val weightSum = (1..windowSize).sum().toDouble()

    return values.indices.map { i ->
        (if (i < windowSize - 1) 0.0
        else {
            var sum = 0.0
            for (j in 0 until windowSize) {
                val weight = j + 1
                val value = values[i - (windowSize - 1) + j]
                sum += value * weight
            }
            sum / weightSum
        }) as Double
    }
}
