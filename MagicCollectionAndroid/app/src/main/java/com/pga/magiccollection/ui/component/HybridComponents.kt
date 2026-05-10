package com.pga.magiccollection.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.ui.theme.LocalGuild
import com.pga.magiccollection.util.hybridBackground

/**
 * A custom button that uses the current guild's hybrid colors in a gradient.
 * Fallbacks to primary/secondary if no guild is present.
 */
@Composable
fun HybridButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val guild = LocalGuild.current
    val color1 = guild?.primary ?: MaterialTheme.colorScheme.primary
    val color2 = guild?.secondary ?: MaterialTheme.colorScheme.secondary

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .hybridBackground(color1, color2)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black // We use black for contrast on our pastel colors
            )
        }
    }
}

/**
 * A decorative divider that uses the hybrid gradient
 */
@Composable
fun HybridDivider(modifier: Modifier = Modifier) {
    val guild = LocalGuild.current
    val color1 = guild?.primary ?: MaterialTheme.colorScheme.primary
    val color2 = guild?.secondary ?: MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .hybridBackground(color1, color2)
    )
}
