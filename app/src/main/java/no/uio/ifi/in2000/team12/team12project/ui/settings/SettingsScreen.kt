package no.uio.ifi.in2000.team12.team12project.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    settingsScreenViewModel: SettingsScreenViewModel = viewModel()
) {
    Column {
        Text("SettingsScreen")

        Button(onClick = {settingsScreenViewModel.toggleTheme()}) {

        }
    }

}