package kurou.kodriver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.tts_engine_title
import kurou.kodriver.app.shared.generated.resources.tts_language_title
import kurou.kodriver.app.shared.generated.resources.tts_windows_title
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import org.jetbrains.compose.resources.stringResource

/** 接続状態とは独立して、TTSの利用不可理由と対処方法への導線を表示する。 */
@Composable
internal fun TtsUnavailableBanner(
    reason: TtsUnavailableGuidance,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val title =
        when (reason) {
            TtsUnavailableGuidance.EngineMissing -> Res.string.tts_engine_title
            TtsUnavailableGuidance.LanguageDataMissing -> Res.string.tts_language_title
            TtsUnavailableGuidance.WindowsSpeechUnavailable -> Res.string.tts_windows_title
        }
    val contentColor = MaterialTheme.colorScheme.onErrorContainer
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                }.padding(horizontal = AppSpacing.large, vertical = AppSpacing.small),
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center).padding(end = AppSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(stringResource(title), style = MaterialTheme.typography.labelMedium, color = contentColor)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.align(Alignment.CenterEnd).size(18.dp),
        )
    }
}

/** 利用不可の間はバナーを残し、タップした場合だけ案内を開く。理由が変わると案内を閉じる。 */
@Composable
internal fun TtsUnavailableBannerHost(
    reason: TtsUnavailableGuidance?,
    onAction: () -> Unit,
) {
    var dialogVisible by rememberSaveable(reason) { mutableStateOf(false) }
    if (reason != null) {
        TtsUnavailableBanner(reason = reason, onClick = { dialogVisible = true })
        if (dialogVisible) {
            TtsUnavailableDialog(
                reason = reason,
                onPrimaryClick = onAction,
                onDismiss = { dialogVisible = false },
            )
        }
    }
}
