package kurou.kodriver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.low_volume_both
import kurou.kodriver.app.shared.generated.resources.low_volume_device
import kurou.kodriver.app.shared.generated.resources.low_volume_dismiss
import kurou.kodriver.app.shared.generated.resources.low_volume_sound
import kurou.kodriver.feature.main.LowVolumeWarningUiState
import org.jetbrains.compose.resources.stringResource

private val LowVolumeBannerActionWidth = 40.dp

@Composable
internal fun LowVolumeBanner(
    uiState: LowVolumeWarningUiState,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val contentColor = AppExtendedColors.current.onWarningContainer
    val message =
        when {
            uiState.deviceVolume != null && uiState.soundVolume != null -> Res.string.low_volume_both
            uiState.deviceVolume != null -> Res.string.low_volume_device
            else -> Res.string.low_volume_sound
        }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(AppExtendedColors.current.warningContainer)
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(LowVolumeBannerActionWidth)
                        .fillMaxHeight()
                        .clickable(role = Role.Button) {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onDismiss()
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.low_volume_dismiss),
                    tint = contentColor,
                    modifier = Modifier.size(14.dp),
                )
            }
            Box(
                modifier =
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .padding(vertical = AppSpacing.small)
                        .background(contentColor.copy(alpha = 0.35f)),
            )
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = AppSpacing.small),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.testTag("lowVolumeBannerMessage"),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector =
                            if (uiState.deviceVolume != null) {
                                Icons.AutoMirrored.Filled.VolumeDown
                            } else {
                                Icons.AutoMirrored.Filled.VolumeOff
                            },
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(stringResource(message), style = MaterialTheme.typography.labelMedium, color = contentColor)
                }
            }
            Box(
                modifier = Modifier.width(LowVolumeBannerActionWidth).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp).testTag("lowVolumeBannerChevron"),
                )
            }
        }
    }
}
