package no.uio.ifi.in2000.team12.team12project.data.nve

import kotlinx.io.Buffer
import no.uio.ifi.in2000.team12.team12project.model.nve.FloodWarning

class NVERepository(private val nveDataSource: NVEDataSource) {
    suspend fun getFloodWarnings(): List<Pair<Double, Double>>   {     //<FloodWarning> {
        return nveDataSource.getFloodWarning()
    }

//    suspend fun  getFloodWarnings(): List<FloodWarning> {
//            val warnings = nveDataSource.getFloodWarning()
//            return warnings.map { warning ->
//                warning.copy(
//                    MunicipalityList = warning.MunicipalityList.map { municipality ->
//                        val coordinates = municipalityCoordinates[municipality.Id]
//                        if (coordinates != null) {
//                            municipality.copy(lat = coordinates.first, lon = coordinates.second)
//                        } else {
//                            municipality
//                        }
//                    }
//                )
//            }
//        }

}