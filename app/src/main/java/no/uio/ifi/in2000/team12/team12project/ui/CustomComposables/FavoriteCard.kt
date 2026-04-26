package no.uio.ifi.in2000.team12.team12project.ui.CustomComposables

//import androidx.compose.material3.Card
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.google.android.gms.maps.model.LatLng
//import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation
//
//@Composable
//fun FavoriteCard(
//    favorite : FavoriteLocation,
//    modifier : Modifier = Modifier
//){
//    Card(modifier = modifier) {
//
//    }
//}

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val MintGreen = Color(0xFF6ECFB8)
val LightMint = Color(0xFFE8F8F5)
val DarkText = Color(0xFF1A1A2E)
val SubtleText = Color(0xFF8A9BB0)

@Composable
fun FavoriteCard(
    icon: ImageVector = Icons.Default.Grass,
    title: String,
    subtitle: String,
    buttonIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
    onButtonClick: () -> Unit = {},

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(LightMint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MintGreen,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text block
                Column(modifier = Modifier.weight(1f)) {
                Text( //name
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text( //address
                    text = subtitle,
                    fontSize = 13.sp,
                    color = SubtleText
                )

        }
        Spacer(modifier = Modifier.width(12.dp))

        // Circular button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MintGreen)
                .clickable { onButtonClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = buttonIcon,
                contentDescription = "Action",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// --- Preview / Usage ---
@Preview
@Composable
fun CardPreview() {
    FavoriteCard(
        title = "Sea Freight",
        subtitle = "3 active shipments",
        onButtonClick = { /* navigate */ }
    )
}