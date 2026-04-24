package no.uio.ifi.in2000.team12.team12project.data.nve

import kotlinx.io.Buffer
import no.uio.ifi.in2000.team12.team12project.model.nve.FloodWarning
import java.io.BufferedReader
import java.io.InputStreamReader

class NVERepository(val nveDataSource: NVEDataSource = NVEDataSource()) {
    val municipalityCoordinates = mutableMapOf<Int, Pair<Double, Double>>()

    fun loadCoordinates() {
        try {
            val inputStream = javaClass.getResourceAsStream("kommuner.csv")
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.forEachLine { line ->
                    val part = line.split("\t")
                    if (part.size >= 4) {
                        val id = part[0].toInt()
                        val name = part[1]
                        val lat = part[2].toDouble()
                        val lon = part[3].toDouble()

                        municipalityCoordinates[id] = Pair(lat, lon)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun  getFloodWarnings(): List<FloodWarning> {
            val warnings = nveDataSource.getFloodWarning()
            return warnings.map { warning ->
                warning.copy(
                    MunicipalityList = warning.MunicipalityList.map { municipality ->
                        val coordinates = municipalityCoordinates[municipality.Id]
                        if (coordinates != null) {
                            municipality.copy(lat = coordinates.first, lon = coordinates.second)
                        } else {
                            municipality
                        }
                    }
                )
            }
        }

}