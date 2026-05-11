package no.uio.ifi.in2000.team12.team12project.ui.graphs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateDataRepository
import no.uio.ifi.in2000.team12.team12project.data.plants.PlantsRepository

class GraphScreenViewModelFactory(
    private val climateDataRepository: ClimateDataRepository,
    private val plantsRepository: PlantsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GraphScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GraphScreenViewModel(climateDataRepository, plantsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
