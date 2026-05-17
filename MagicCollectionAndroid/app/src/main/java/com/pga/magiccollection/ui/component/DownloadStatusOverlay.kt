package com.pga.magiccollection.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.R
import com.pga.magiccollection.ui.screen.DownloadResult
import com.pga.magiccollection.ui.theme.LocalAppDimens
import com.pga.magiccollection.ui.theme.LocalAppSpacing
import com.pga.magiccollection.ui.theme.LocalMtgSemanticColors
import kotlinx.coroutines.delay

private const val SUCCESS_DISMISS_MS = 3_000L
private const val FAILURE_DISMISS_MS = 5_000L

/**
 * Floating toast pinned to the top-right of the screen that reflects the state of an
 * ongoing language-index download.
 *
 *  - **Downloading**: spinner + language code + percentage. Persists until the worker
 *    reaches a terminal state.
 *  - **Success**: check icon + completion message. Auto-dismisses after [SUCCESS_DISMISS_MS].
 *  - **Failed**: error icon + error message. Auto-dismisses after [FAILURE_DISMISS_MS].
 *
 * @param downloadingLangCode language code currently being downloaded, or null when idle
 * @param progress fractional progress 0.0..1.0 reported by the worker
 * @param result terminal outcome of the most recent download; null while idle or in flight
 * @param onDismissResult invoked when the auto-dismiss timer for [result] elapses
 */
@Composable
fun DownloadStatusOverlay(
    downloadingLangCode: String?,
    progress: Float,
    result: DownloadResult?,
    onDismissResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = downloadingLangCode != null || result != null

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
    ) {
        when {
            downloadingLangCode != null -> DownloadingChip(
                langCode = downloadingLangCode,
                progress = progress
            )
            result is DownloadResult.Success -> {
                LaunchedEffect(result) {
                    delay(SUCCESS_DISMISS_MS)
                    onDismissResult()
                }
                SuccessChip(langCode = result.langCode)
            }
            result is DownloadResult.Failed -> {
                LaunchedEffect(result) {
                    delay(FAILURE_DISMISS_MS)
                    onDismissResult()
                }
                FailedChip(langCode = result.langCode, error = result.error)
            }
        }
    }
}

@Composable
private fun DownloadingChip(langCode: String, progress: Float) {
    val dimens = LocalAppDimens.current
    val percent = (progress * 100).toInt().coerceIn(0, 100)

    StatusChip {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.size(dimens.iconMd),
            strokeWidth = 2.5.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "${langCode.uppercase()} · $percent%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SuccessChip(langCode: String) {
    val dimens = LocalAppDimens.current
    val semantic = LocalMtgSemanticColors.current

    StatusChip {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = semantic.success,
            modifier = Modifier.size(dimens.iconMd)
        )
        Text(
            text = "${langCode.uppercase()} · ${stringResource(id = R.string.lang_download_success)}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FailedChip(langCode: String, error: String) {
    val dimens = LocalAppDimens.current

    StatusChip {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(dimens.iconMd)
        )
        Text(
            text = "${langCode.uppercase()} · ${stringResource(id = R.string.lang_download_error, error)}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusChip(content: @Composable () -> Unit) {
    val spacing = LocalAppSpacing.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .padding(spacing.sm)
            .widthIn(min = 140.dp, max = 280.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            content()
        }
    }
}
