package no.uio.ifi.in2000.team12.team12project

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import no.uio.ifi.in2000.team12.team12project.ui.graphs.ComposeAITestScores
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            //HomeScreen()
            //TestLineChart()
            ComposeAITestScores()
        }
    }
}

