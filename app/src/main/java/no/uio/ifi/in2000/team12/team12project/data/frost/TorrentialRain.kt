package no.uio.ifi.in2000.team12.team12project.data.frost

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.ThreatLevel
import kotlin.math.min

@Serializable
private data class StationData(
    val coordinates: List<Double>,
    val event: Int
)

class TorrentialRain(
    private val climateDataRepository: ClimateDataRepository = ClimateDataRepository()
) {
    companion object {
        private const val DURATION_ELEMENT = "sum(duration_of_precipitation PT1H)"
        private const val AMOUNT_ELEMENT = "sum(precipitation_amount P1D)"

        /** mm/hr (source: https://www.baranidesign.com/faq-articles/2020/1/19/rain-rate-intensity-classification) */
        private const val TORRENTIAL_RAIN_RATE_THRESHOLD = 7.5

        /** Minimum instances per year to be considered a threat */
        private const val INSTANCES_PER_YEAR_THRESHOLD = 5

        /** Minimum duration (minutes) to avoid noise from low-duration observations */
        private const val MIN_PRECIPITATION_DURATION_MINUTES = 30

        private const val MAX_MONTHS_BACK = 120u
        private const val DATE_STRING_LENGTH = 10
    }

    /**
     * Checks if the station has a history of torrential rain that exceeds the threshold.
     */
    suspend fun isTorrentialRainThreat(sourceId: String): ThreatLevel {
        val (durationStart, amountStart) = try {
            val dStart = climateDataRepository.getClimateTimeSeries(sourceId, DURATION_ELEMENT).take(DATE_STRING_LENGTH)
            val aStart = climateDataRepository.getClimateTimeSeries(sourceId, AMOUNT_ELEMENT).take(DATE_STRING_LENGTH)
            dStart to aStart
        } catch (e: Exception) {
            println(e)
            return ThreatLevel.NONE
        }

        val months = min(min(monthsSince(durationStart), monthsSince(amountStart)).toUInt(), MAX_MONTHS_BACK)

        val durationData = getClimateData(sourceId, months, DURATION_ELEMENT)
        val amountData = getClimateData(sourceId, months, AMOUNT_ELEMENT)

        val totalInstances = calculateTorrentialRainInstances(durationData, amountData)
        val yearsCount = if (months >= 12u) (months / 12u).toInt() else 1
        val averageInstancesPerYear = totalInstances / yearsCount

        return when (averageInstancesPerYear) {
            in 0 until INSTANCES_PER_YEAR_THRESHOLD -> ThreatLevel.NONE
            in INSTANCES_PER_YEAR_THRESHOLD until 2 * INSTANCES_PER_YEAR_THRESHOLD -> ThreatLevel.LOW
            in 2 * INSTANCES_PER_YEAR_THRESHOLD until 3 * INSTANCES_PER_YEAR_THRESHOLD -> ThreatLevel.MEDIUM
            else -> ThreatLevel.HIGH
        }
    }

    suspend fun getClimateData(sourceId: String, monthsBack: UInt, elementId: String): List<ClimateObs> {
        return climateDataRepository.getClimateData(sourceId, elementId, constructTimeSeries(monthsBack))
    }

    /**
     * Calculates total instances of torrential rain by comparing daily precipitation amount
     * against total rain duration for that day.
     */
    fun calculateTorrentialRainInstances(durationObs: List<ClimateObs>, amountObs: List<ClimateObs>): Int {
        // Group hourly duration observations by date for efficient lookup
        val durationByDate = durationObs
            .groupBy { it.referenceTime.take(DATE_STRING_LENGTH) }
            .mapValues { (_, obsList) ->
                obsList.sumOf { it.observations.firstOrNull()?.value ?: 0.0 }
            }

        return amountObs.count { dailyAmount ->
            val date = dailyAmount.referenceTime.take(DATE_STRING_LENGTH)
            val precipAmount = findMaxObs(dailyAmount)
            val totalDurationMinutes = durationByDate[date] ?: 0.0

            if (totalDurationMinutes < MIN_PRECIPITATION_DURATION_MINUTES || precipAmount <= 0.0) {
                false
            } else {
                val rateMmPerHour = precipAmount / (totalDurationMinutes / 60.0)
                rateMmPerHour > TORRENTIAL_RAIN_RATE_THRESHOLD
            }
        }
    }

    /**
     * Reads station coordinates from a JSON file in the raw resources.
     *
     * @param context The application context.
     * @param fileName The name of the JSON file in res/raw (e.g., "torrential_rain_stations.json").
     * @return A list of LatLng coordinates.
     */
    fun readLatLngFromJson(context: Context, fileName: String): List<LatLng> {
        val resourceName = fileName.substringBeforeLast(".")
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)

        if (resourceId == 0) return emptyList()

        val jsonString = context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
        return parseLatLngFromJson(jsonString)
    }

    /**
     * Parses a list of LatLng coordinates from a JSON string.
     *
     * @param jsonString The JSON string to parse.
     * @return A list of LatLng coordinates.
     */
    fun parseLatLngFromJson(jsonString: String): List<LatLng> {
        val stations = Json.decodeFromString<List<StationData>>(jsonString)
        // The coordinates are stored as [Longitude, Latitude]
        return stations.map { LatLng(it.coordinates[1], it.coordinates[0]) }
    }
}
/*
Torrential rain stations:
[([10.72, 59.9423], 9), ([20.0930685, 69.5887], 7), ([7.9957, 61.5648], 736), ([6.5046, 58.5072], 7), ([20.1387, 69.8915], 6), ([9.666, 62.6027], 6), ([8.6978, 59.8655], 11), ([19.6278, 69.0935], 6), ([10.6692319, 59.9847986], 9), ([8.7754074, 62.721888], 8), ([8.5473, 62.6785], 6), ([6.4733, 60.8655], 17), ([6.3758, 61.4202], 73), ([6.9097, 58.923], 10), ([7.2088, 62.756], 10), ([8.0767, 58.2], 9), ([8.1951, 60.5299], 31), ([6.4347, 60.2263], 8), ([6.4697, 59.4128], 11), ([6.6502, 62.0033], 13)]

Strong wind speed stations:
[([5.2007, 61.6788], 1752), ([11.3475, 78.9633], 517), ([10.72, 59.9423], 8), ([16.1312, 69.3073], 1068), ([7.5273388, 59.1971158], 32), ([13.646, 66.9035], 608), ([10.2150378, 59.6193334], 7), ([10.1206179, 59.7538274], 2), ([16.0828758, 68.0484437], 48), ([6.7108, 59.8357], 1759), ([11.0793677, 61.4272678], 14), ([12.1038, 67.5267], 1537), ([5.268, 62.3293], 2216), ([10.4762, 61.0917], 4), ([13.9067, 66.8303], 1012), ([6.2877, 62.16], 673), ([9.4798, 61.8883], 142), ([8.5263, 60.4188], 98), ([17.0442, 78.0648], 345), ([11.0075, 63.2248], 48), ([7.8173, 59.7675], 1785), ([7.9957, 61.5648], 621), ([13.6192, 78.0625], 1581), ([25.5023, 69.4635], 5), ([24.4315, 69.3696], 32), ([28.159, 70.2122], 103), ([7.4805, 60.1833], 1107), ([14.1679, 67.094383], 1567), ([6.5046, 58.5072], 8), ([18.9978, 74.5035], 159), ([8.9225701, 61.2509419], 179), ([17.8708, 69.5015], 40), ([10.9305, 63.4597], 589), ([6.8717, 62.5158], 1085), ([10.148, 58.9688], 1309), ([10.5493, 64.7977], 2240), ([6.40585, 60.223533], 1092), ([20.2823, 69.3875], 9), ([5.4122, 59.0705], 1184), ([9.392, 62.6352], 1153), ([6.8817, 59.3335], 1301), ([20.1518, 69.5598], 1059), ([5.9636631, 60.3889174], 215), ([15.943, 68.368], 1513), ([6.1065, 61.8902], 2086), ([11.1127995, 59.2850933], 21), ([10.798, 59.3742], 92), ([13.5978, 64.4595], 57), ([4.715, 60.7525], 1701), ([19.4997, 70.2452], 1344), ([15.831, 78.2022], 616), ([7.803, 58.6863], 120), ([9.666, 62.6027], 33), ([18.9368, 69.6537], 75), ([13.61214, 68.1557873], 527), ([5.0668, 59.907], 1244), ([18.77, 78.035], 228), ([10.12662056, 61.46474255], 881), ([16.2433, 80.0555], 1477), ([23.4403, 69.2187], 355), ([8.7893, 58.3988], 1699), ([17.4802, 68.421], 1983), ([16.6818, 78.3313], 395), ([29.1003, 70.306], 695), ([22.8177, 70.4107], 1828), ([10.82829919, 59.15154567], 1087), ([5.5545609, 58.6585044], 1620), ([11.0697, 60.818], 16), ([15.5015, 78.2453], 743), ([12.611, 66.3705], 1267), ([11.0383, 59.9708], 7), ([6.5675, 58.109], 1493), ([9.5745, 58.8565], 661), ([10.5242, 59.0272], 1668), ([10.4538, 63.4107], 26), ([5.3327, 60.383], 186), ([10.6692319, 59.9847986], 155), ([9.1123236, 63.5184178], 489), ([22.3413, 70.1728], 1266), ([14.6485, 68.1535], 1355), ([9.352, 62.2958], 1618), ([6.1835, 61.7873], 24), ([22.8225, 78.2508], 1092), ([15.378, 78.2278], 781), ([7.9482, 63.5176], 1508), ([7.3896454, 59.5765946], 38), ([23.6772, 70.6807], 828), ([22.4428, 70.607], 2482), ([23.0349, 68.9964], 24), ([10.1082, 61.6513], 76), ([10.9965, 65.2023], 2154), ([8.5473, 62.6785], 295), ([9.1147, 62.07165], 108), ([25.0133, 76.5097], 836), ([6.2805, 60.3632], 436), ([5.87183, 58.43517], 2042), ([20.5795, 69.2235], 1575), ([10.7352666, 62.2696934], 8), ([6.849, 62.3065], 1030), ([12.486, 66.7628], 1979), ([14.7578, 77.6873], 1259), ([13.3667, 65.1662], 11), ([28.9648, 70.3357], 1024), ([11.7279, 63.41129], 13), ([7.6637358, 62.0164692], 49), ([9.8495, 63.3153], 58), ([12.2758327, 61.2955653], 3), ([31.4585, 80.1035], 1877), ([8.0532, 58.0732], 1248), ([20.11451, 70.10426], 2118), ([11.8562, 65.7017], 790), ([18.2248, 78.3002], 891), ([23.5395347, 68.7559654], 182), ([7.2143, 59.8118], 622), ([6.9097, 58.923], 11), ([11.3747, 61.1847], 13), ([6.096, 61.4573], 1359), ([20.431, 70.075], 1568), ([9.2128, 59.3822], 5), ([17.83117, 69.6005], 1453), ([13.8958, 67.4048], 1651), ([12.1887, 61.3188], 1630), ([7.3653533, 59.7663671], 20), ([30.368, 70.2467], 485), ([31.0962, 70.3707], 1027), ([23.3582, 69.9775], 263), ([4.9865, 62.034], 2384), ([28.9937, 69.8043], 471), ([9.4052, 64.1727], 1858), ([9.224, 60.9785], 62), ([8.0767, 58.2], 158), ([-8.669, 70.9394], 1690), ([16.003, 69.2413], 1752), ([11.9263, 78.6568], 404), ([6.7552, 62.6512], 329), ([10.2478, 60.1665], 2), ([6.7642989, 61.4234562], 12), ([28.217, 71.0888], 1645), ([11.9928, 65.2193], 2114), ([19.5442, 69.1025], 933), ([5.2265, 60.2892], 425), ([25.346, 69.3003], 1773), ([8.1951, 60.5299], 46), ([7.925, 62.93], 1935), ([27.8298, 71.033], 1220), ([7.2803489, 60.4080734], 289), ([25.0048, 80.6518], 1229), ([28.834, 78.918], 1279), ([13.4483, 65.3143], 1362), ([14.4347, 68.6072], 1055), ([4.8723, 59.3065], 1958), ([4.6817, 61.5717], 1861), ([14.1942, 65.8255], 181), ([10.578, 59.4352], 746), ([15.3112, 68.7183], 1724), ([6.4347, 60.2263], 1171), ([25.9782, 71.0105], 1377), ([16.3608, 68.5863], 1536), ([11.49939, 61.37635], 76), ([17.7905, 68.1905], 666), ([14.3826294, 67.2726458], 1006), ([13.218, 65.7842], 49), ([18.1288, 69.2897], 1855), ([6.5378, 62.8585], 1798), ([12.846, 64.7422], 21), ([7.527, 60.5938], 1108), ([8.4968, 60.8397], 1590), ([21.8958, 69.8362], 540), ([9.2862, 62.1133], 585), ([15.2108, 78.0092], 174), ([28.3773, 69.9363], 535), ([8.6562, 59.8497], 725), ([7.4218, 62.2305], 130), ([8.1785, 59.8397], 512), ([11.5802, 59.9118], 21), ([23.5345, 69.5797], 239), ([16.5428, 76.4733], 1982), ([16.5362, 68.8029], 45), ([13.948, 66.8275], 664), ([18.6208, 69.4229], 891), ([6.0365, 59.16], 78), ([28.9563, 70.227], 161), ([5.078, 59.4382], 1283), ([10.804, 59.923], 18), ([8.4543, 63.8429], 1369), ([6.4697, 59.4128], 143), ([7.3892, 61.5972], 1076), ([7.048, 57.9815], 2070), ([17.0715, 69.2805], 93), ([26.998, 70.58117], 45), ([30.07, 70.7057], 1324), ([6.6452, 61.1724], 38), ([6.4675, 60.8603], 862), ([18.6938, 69.9903], 1173), ([11.4493, 64.0217], 25), ([18.998, 74.5035], 1452), ([5.637, 58.8843], 576), ([9.6104, 63.7044], 1213), ([8.6338445, 58.6554085], 28), ([6.8112, 58.9112], 798), ([9.1478, 58.6362], 514), ([30.0648, 70.3222], 213), ([23.9837577, 71.094447], 2035), ([10.455, 64.4013], 1845), ([8.1125, 61.178], 275), ([11.9325, 78.9218], 584), ([8.5175, 60.8705], 265), ([9.4020874, 61.5309793], 3), ([18.5437, 69.0577], 69), ([16.4035, 78.18098], 695), ([8.369, 61.6775], 2020)]
 */