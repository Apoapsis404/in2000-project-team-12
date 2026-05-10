package no.uio.ifi.in2000.team12.team12project.ui.settings

import android.app.Activity
import android.content.Context
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
import androidx.core.content.edit

data class SettingsScreenUiState(
    val lightMode: Boolean = true,
    val language: Languages = Languages.ENGLISH
)
class SettingsScreenViewModel(private val dao: SettingsDao): ViewModel(){

    val uiState : StateFlow<SettingsScreenUiState> = dao.getSettings()
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsScreenUiState()
        )

    fun toggleTheme(){
        updateConfig { currentState ->
            currentState.copy(lightMode = !currentState.lightMode)
        }
    }

    fun switchLanguage(newLanguage: Languages){
        updateConfig { currentState ->
            currentState.copy(language = newLanguage)
        }
    }

    private fun updateConfig(transform: (SettingsScreenUiState) -> SettingsScreenUiState) {
        viewModelScope.launch {
            val current = uiState.value
            dao.saveSettings(transform(current).toEntity())
        }
    }
}

fun SettingsEntity?.toUiState(): SettingsScreenUiState {
    val languageEnum = try{
        this?.language?.let {Languages.valueOf(it)} ?: Languages.ENGLISH
    } catch (e: IllegalArgumentException){
        Languages.ENGLISH
    }
    return SettingsScreenUiState(
        lightMode = this?.lightMode ?: true,
        language = languageEnum
    )
}

fun SettingsScreenUiState.toEntity(): SettingsEntity {
    return SettingsEntity(
        id = 0,
        lightMode = this.lightMode,
        language = this.language.name
    )
}