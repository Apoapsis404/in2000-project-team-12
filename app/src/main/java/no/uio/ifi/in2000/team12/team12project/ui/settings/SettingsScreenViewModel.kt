package no.uio.ifi.in2000.team12.team12project.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.uio.ifi.in2000.team12.team12project.model.frost.Languages

data class SettingsScreenUiState(
    val lightmode: Boolean = true,
    val language: Languages = Languages.ENGLISH
)
class SettingsScreenViewModel: ViewModel(){
    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleTheme(){
        _uiState.update { it.copy(lightmode = !_uiState.value.lightmode) }
    }

    fun switchLanguage(newLanguage: Languages){
        _uiState.update { it.copy(language = newLanguage) }
    }

}