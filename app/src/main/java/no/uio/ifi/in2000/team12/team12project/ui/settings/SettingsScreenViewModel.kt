package no.uio.ifi.in2000.team12.team12project.ui.settings

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.local.SettingsEntity
import no.uio.ifi.in2000.team12.team12project.data.local.dao.SettingsDao
import no.uio.ifi.in2000.team12.team12project.model.frost.Languages

data class SettingsScreenUiState(
    val lightmode: Boolean = true,
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

    private fun updateConfig(transform: (SettingsScreenUiState) -> SettingsScreenUiState) {
        viewModelScope.launch {
            val current = uiState.value
            dao.saveSettings(transform(current).toEntity())
        }
    }

    fun toggleTheme(){
        updateConfig {
            it.copy(lightmode = !it.lightmode)
        }
    }

    fun switchLanguage(newLanguage: Languages, activity: Activity){
        val localeCode = when (newLanguage) {
            Languages.ENGLISH -> "en"
            Languages.NORSK -> "nb"
            Languages.中國人 -> "zh"
        }

        val prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("language", localeCode).apply()
        updateConfig {
            it.copy(language = newLanguage)
        }

        // To see changes in the app instant
        activity.recreate()
    }

}

fun SettingsEntity?.toUiState(): SettingsScreenUiState {
    return SettingsScreenUiState(
        lightmode = this?.lightMode ?: true,
        language = this?.language?.let { Languages.valueOf(it) } ?: Languages.ENGLISH
        //Add additional settings here
    )
}

fun SettingsScreenUiState.toEntity(): SettingsEntity {
    return SettingsEntity(
        id = 0,
        lightMode = this.lightmode,
        language = this.language.name
        //Add additional settings here
    )
}