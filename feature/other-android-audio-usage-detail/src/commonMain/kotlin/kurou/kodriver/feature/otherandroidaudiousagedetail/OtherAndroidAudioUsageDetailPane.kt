package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.Res
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_alarm
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_alarm_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_assistant
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_assistant_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_communication
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_communication_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_media
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_media_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_notification
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_notification_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_ringtone
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_ringtone_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** 音声の用途設定を表示する詳細ペイン。 */
@Composable
fun OtherAndroidAudioUsageDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherAndroidAudioUsageDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherAndroidAudioUsageDetailPaneContent(
        uiState = uiState,
        onUsageSelected = viewModel::onUsageSelected,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/** 音声の用途設定の画面本体。 */
@Composable
fun OtherAndroidAudioUsageDetailPaneContent(
    uiState: OtherAndroidAudioUsageDetailUiState,
    onUsageSelected: (AndroidAudioUsage) -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    DetailPaneScaffold(
        title = stringResource(Res.string.audio_usage_title),
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            DetailPaneDescription(text = stringResource(Res.string.audio_usage_description))
            Column(Modifier.selectableGroup()) {
                AndroidAudioUsage.entries.forEach { usage ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = uiState.usage == usage,
                                    role = Role.RadioButton,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        onUsageSelected(usage)
                                    },
                                ).padding(KoDriverSpacing.medium),
                    ) {
                        RadioButton(selected = uiState.usage == usage, onClick = null)
                        Column(Modifier.padding(start = KoDriverSpacing.medium)) {
                            Text(stringResource(audioUsageLabel(usage)), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                stringResource(audioUsageDescription(usage)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun audioUsageLabel(usage: AndroidAudioUsage) =
    when (usage) {
        AndroidAudioUsage.MEDIA -> Res.string.audio_usage_media
        AndroidAudioUsage.COMMUNICATION -> Res.string.audio_usage_communication
        AndroidAudioUsage.RINGTONE -> Res.string.audio_usage_ringtone
        AndroidAudioUsage.NOTIFICATION -> Res.string.audio_usage_notification
        AndroidAudioUsage.ALARM -> Res.string.audio_usage_alarm
        AndroidAudioUsage.ASSISTANT -> Res.string.audio_usage_assistant
    }

private fun audioUsageDescription(usage: AndroidAudioUsage) =
    when (usage) {
        AndroidAudioUsage.MEDIA -> Res.string.audio_usage_media_description
        AndroidAudioUsage.COMMUNICATION -> Res.string.audio_usage_communication_description
        AndroidAudioUsage.RINGTONE -> Res.string.audio_usage_ringtone_description
        AndroidAudioUsage.NOTIFICATION -> Res.string.audio_usage_notification_description
        AndroidAudioUsage.ALARM -> Res.string.audio_usage_alarm_description
        AndroidAudioUsage.ASSISTANT -> Res.string.audio_usage_assistant_description
    }

@Preview(showBackground = true)
@Composable
private fun OtherAndroidAudioUsageDetailPanePreview() {
    KoDriverTheme {
        OtherAndroidAudioUsageDetailPaneContent(uiState = OtherAndroidAudioUsageDetailUiState())
    }
}
