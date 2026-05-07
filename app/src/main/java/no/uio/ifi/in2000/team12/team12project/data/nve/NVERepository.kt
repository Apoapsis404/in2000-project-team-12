package no.uio.ifi.in2000.team12.team12project.data.nve

import no.uio.ifi.in2000.team12.team12project.model.nve.FloodWarning

class NVERepository(private val nveDataSource: NVEDataSource) {
    suspend fun getFloodWarnings(): List<FloodWarning> {
        return nveDataSource.getFloodWarning()
    }
}