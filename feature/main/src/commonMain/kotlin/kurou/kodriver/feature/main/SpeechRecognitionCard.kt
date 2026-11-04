package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.main.generated.resources.Res
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_idle_stopped
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_listening
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_permission_denied
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_starting
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_stopped
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_unavailable
import kurou.kodriver.feature.main.generated.resources.speech_recognition_switch_description
import kurou.kodriver.feature.main.generated.resources.speech_recognition_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** 認識途中の文字列と履歴を表示する領域の最大の高さ。超えた分はスクロールする。 */
private val HISTORY_MAX_HEIGHT = 120.dp

/** 音声認識の聞き取りの開始・停止と、認識状況を確認するための簡易カード。 */
@Composable
internal fun SpeechRecognitionCard(
    uiState: SpeechRecognitionUiState,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val switchDescription = stringResource(Res.string.speech_recognition_switch_description)
    Surface(
        modifier =
            modifier.fillMaxWidth().padding(
                horizontal = KoDriverSpacing.small,
                vertical = KoDriverSpacing.small,
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(KoDriverSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint =
                        if (uiState.enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.speech_recognition_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(uiState.status.label()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.enabled,
                    onCheckedChange = onEnabledChange,
                    modifier = Modifier.semantics { contentDescription = switchDescription },
                )
            }
            if (uiState.partialText.isNotEmpty() || uiState.recognized.isNotEmpty()) {
                // 履歴が長くなっても下のセット一覧の領域を奪わないよう、高さを抑えてスクロールさせる。
                Column(
                    modifier =
                        Modifier
                            .heightIn(max = HISTORY_MAX_HEIGHT)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                ) {
                    if (uiState.partialText.isNotEmpty()) {
                        Text(
                            text = uiState.partialText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    uiState.recognized.forEach { candidates ->
                        Text(
                            text = candidates.joinToString(separator = " / "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

private fun SpeechRecognitionStatus.label(): StringResource =
    when (this) {
        SpeechRecognitionStatus.Stopped -> Res.string.speech_recognition_status_stopped
        SpeechRecognitionStatus.Starting -> Res.string.speech_recognition_status_starting
        SpeechRecognitionStatus.Listening -> Res.string.speech_recognition_status_listening
        SpeechRecognitionStatus.PermissionDenied -> Res.string.speech_recognition_status_permission_denied
        SpeechRecognitionStatus.Unavailable -> Res.string.speech_recognition_status_unavailable
        SpeechRecognitionStatus.IdleStopped -> Res.string.speech_recognition_status_idle_stopped
    }
