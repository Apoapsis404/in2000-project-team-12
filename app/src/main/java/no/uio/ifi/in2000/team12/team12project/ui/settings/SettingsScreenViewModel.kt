package no.uio.ifi.in2000.team12.team12project.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.local.SettingsEntity
import no.uio.ifi.in2000.team12.team12project.data.local.dao.SettingsDao
import no.uio.ifi.in2000.team12.team12project.model.frost.Languages

data class SettingsScreenUiState(
    val lightMode: Boolean = true,
    val language: Languages = Languages.ENGLISH,
    val showDownpour: Boolean = true,
    val showExtremeWind: Boolean = true,
    val showFlood: Boolean = true
)


// Viewmodel for SettingsScreen. Håndterer logikken for å bytte tema og språk.
class SettingsScreenViewModel(private val dao: SettingsDao): ViewModel(){

    val uiState : StateFlow<SettingsScreenUiState> = dao.getSettings()
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsScreenUiState()
        )

    //Endrer mellom mørk og lys tema i appen.
    fun toggleTheme(){
        updateConfig { currentState ->
            currentState.copy(lightMode = !currentState.lightMode)
        }
    }

    //Endrer på språket til appen.
    fun switchLanguage(newLanguage: Languages){
        updateConfig { currentState ->
            currentState.copy(language = newLanguage)
        }
    }

    // Metoden oppdaterer config i databasen.
    private fun updateConfig(transform: (SettingsScreenUiState) -> SettingsScreenUiState) {
        viewModelScope.launch {
            val current = uiState.value
            dao.saveSettings(transform(current).toEntity())
        }
    }
}

// Hjelpefunksjon for å konvertere SettingsEntity fra databasen til SettingsScreenUiState.
fun SettingsEntity?.toUiState(): SettingsScreenUiState {
    val languageEnum = this?.language ?: Languages.ENGLISH
    return SettingsScreenUiState(
        lightMode = this?.lightMode ?: true,
        language = languageEnum,
        showDownpour = this?.showDownpour ?: true,
        showExtremeWind = this?.showExtremeWind ?: true,
        showFlood = this?.showFlood ?: true
    )
}

// Hjelpefunksjon for å konvertere SettingScreenUiState til SettingsEntity i databasen.
fun SettingsScreenUiState.toEntity(): SettingsEntity {
    return SettingsEntity(
        id = 0,
        lightMode = this.lightMode,
        language = this.language,
        showDownpour = this.showDownpour,
        showExtremeWind = this.showExtremeWind,
        showFlood = this.showFlood
    )
}