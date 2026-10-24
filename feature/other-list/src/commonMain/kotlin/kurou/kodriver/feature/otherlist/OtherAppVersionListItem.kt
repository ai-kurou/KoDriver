package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private const val DEBUG_STATE_TAP_THRESHOLD = 5
private val DEBUG_STATE_TAP_TIMEOUT = 1.seconds

@Composable
internal fun OtherAppVersionListItem(
    appVersionLabel: String,
    appVersion: String,
    onAppVersionTapped: () -> Unit,
) {
    if (appVersionLabel.isBlank() || appVersion.isBlank()) return

    val haptic = LocalHapticFeedback.current
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapMark by remember { mutableStateOf<TimeMark?>(null) }

    ListItem(
        headlineContent = { Text(appVersionLabel) },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
            )
        },
        trailingContent = {
            Text(
                text = appVersion,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                headlineColor = MaterialTheme.colorScheme.onSurface,
                leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    val now = TimeSource.Monotonic.markNow()
                    val elapsedSinceLastTap = lastTapMark?.elapsedNow()
                    tapCount =
                        if (elapsedSinceLastTap != null && elapsedSinceLastTap < DEBUG_STATE_TAP_TIMEOUT) {
                            tapCount + 1
                        } else {
                            1
                        }
                    lastTapMark = now
                    if (tapCount >= DEBUG_STATE_TAP_THRESHOLD) {
                        tapCount = 0
                        onAppVersionTapped()
                    }
                },
    )
}
