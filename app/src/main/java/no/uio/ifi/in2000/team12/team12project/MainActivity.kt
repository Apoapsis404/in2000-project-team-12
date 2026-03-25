package no.uio.ifi.in2000.team12.team12project

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults.containerColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreen
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreen()
            //GraphScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)     // må vi forsvare denne?
@Composable
fun Header(modifier: Modifier = Modifier){
    TopAppBar(
        {Text("BondeknølApp")},
        modifier = Modifier
            .fillMaxWidth(),
        navigationIcon = { Text("Back") },
        colors = TopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = Color.Blue,
            navigationIconContentColor = Color.Green,
            titleContentColor = Color.DarkGray,
            actionIconContentColor = Color.Magenta,

        )

    )

}

@Preview
@Composable
fun HeaderPreview(modifier: Modifier = Modifier.fillMaxSize()){
    Header()
}