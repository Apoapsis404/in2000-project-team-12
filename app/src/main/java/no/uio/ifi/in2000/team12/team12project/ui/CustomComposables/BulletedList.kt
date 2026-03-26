package no.uio.ifi.in2000.team12.team12project.ui.CustomComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BulletedList(items : List<String>, modifier: Modifier){
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.padding(start = 16.dp)
            ){
                Box(
                    modifier = modifier
                        .padding(end = 8.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
                Text(text = item,style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}