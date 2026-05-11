package no.uio.ifi.in2000.team12.team12project.ui.customComposables


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team12.team12project.R


@Composable
fun FavoriteCard(
    icon: ImageVector = Icons.Default.AddLocation,
    title: String,
    subtitle: String,
    selected: Boolean = false,
    onIconClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .animateContentSize()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
    Row(
        modifier =Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically

    ){
        // Icon container
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.onPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text block
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Circular button
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary)
                .clickable { onIconClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DataThresholding,
                contentDescription = stringResource(R.string.cont_desc_favorite_graph_icon),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(26.dp)
            )
        }
    }

        if(selected){
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick= onEditClick
                ) {
                    Text(stringResource(R.string.favorite_edit_name))
                }

                Button(
                    onClick = onDeleteClick
                ){
                    Text(stringResource(R.string.favorite_delete))
                }
            }
        }

}
}

