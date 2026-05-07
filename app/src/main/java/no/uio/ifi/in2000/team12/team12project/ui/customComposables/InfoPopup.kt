package no.uio.ifi.in2000.team12.team12project.ui.customComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InfoPopup(
    currentScreen: String
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (currentScreen == "home") {
            // INFO SCREEN FOR MAP
            Text("This is the home screen")
            Text("Her kan vi skrive hvordan kart-skjermen fungerer, hva symboler betyr og hvilke terskelverdier vi bruker får å vise symbolene på kart osv")
        } else if (currentScreen == "graph") {
            // INFO SCREEN FOR GRAPH
            Text("This is the graph")
            Text("Her kan vi skrive om hva grafen viser, hva dataen betyr, og hvordan planteprognosen fungerer")
        }
    }
}