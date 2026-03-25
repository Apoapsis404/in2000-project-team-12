package no.uio.ifi.in2000.team12.team12project.ui.settings

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsScreenUiState(
    val lightmode: Boolean = true

)
class SettingsScreenViewModel: ViewModel(){
    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleTheme(){
        _uiState.update { it.copy(lightmode = !_uiState.value.lightmode) }
    }

}