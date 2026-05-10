package com.pga.magiccollection.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.R

/**
 * Pill-shaped search field with a guild-tinted accent (border, separator, magnifier).
 * The trailing area renders a vertical divider followed by the magnifier — matching the
 * reference design — and switches to a clear button when the field has content.
 *
 * Use this everywhere the app needs an inline "filter this list" input.
 *
 * Behavior:
 *  - Single line, IME action `Search`. The keyboard's search key fires [onSearch].
 *  - Trailing icon is the magnifier (taps fire [onSearch]) when [value] is empty, and the
 *    clear glyph when [value] has content (taps wipe the field — caller can override the
 *    side effect via [onClear], otherwise it just emits an empty string through
 *    [onValueChange]).
 */
@Composable
fun GuildSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    onClear: (() -> Unit)? = null,
    onFocusChange: ((Boolean) -> Unit)? = null
) {
    val accent = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val height = 48.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(surface)
            .border(width = 1.5.dp, color = accent.copy(alpha = 0.6f), shape = CircleShape)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp, end = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                cursorBrush = SolidColor(accent),
                textStyle = LocalTextStyle.current.copy(color = onSurface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .let { base ->
                        if (onFocusChange != null) {
                            base.onFocusChanged { state -> onFocusChange(state.isFocused) }
                        } else base
                    }
            )
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = LocalTextStyle.current.copy(color = onSurfaceVariant)
                )
            }
        }

        // Vertical separator that matches the reference image
        Box(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .width(1.dp)
                .fillMaxHeight()
                .background(accent.copy(alpha = 0.55f))
        )

        // Trailing action: magnifier (search) when empty, clear when typed in
        val trailingClickLabel: String
        val trailingIcon: androidx.compose.ui.graphics.vector.ImageVector
        val trailingAction: () -> Unit
        if (value.isEmpty()) {
            trailingClickLabel = stringResource(R.string.action_search)
            trailingIcon = Icons.Default.Search
            trailingAction = onSearch
        } else {
            trailingClickLabel = stringResource(R.string.action_clear)
            trailingIcon = Icons.Default.Close
            trailingAction = { (onClear ?: { onValueChange("") })() }
        }

        Box(
            modifier = Modifier
                .size(height)
                .clickable(onClickLabel = trailingClickLabel, onClick = trailingAction),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = trailingClickLabel,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
