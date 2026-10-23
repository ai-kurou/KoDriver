package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.PlayingEqualizer
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
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_preview_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_preview_sample
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_preview_stop_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_ringtone
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_ringtone_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val AVATAR_SIZE = 48.dp
private val CARD_BORDER_WIDTH = 1.dp
private val CHECK_BADGE_SIZE = 18.dp
private val CHECK_BADGE_PADDING = 2.dp

/** 音声の用途設定を表示する詳細ペイン。 */
@Composable
fun OtherAndroidAudioUsageDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherAndroidAudioUsageDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) { onDispose { viewModel.onPreviewStopped() } }
    OtherAndroidAudioUsageDetailPaneContent(
        uiState = uiState,
        onUsageSelected = viewModel::onUsageSelected,
        onPreviewClicked = viewModel::onPreviewClicked,
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
    onPreviewClicked: (AndroidAudioUsage, String) -> Unit = { _, _ -> },
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val previewSample = stringResource(Res.string.audio_usage_preview_sample)
    DetailPaneScaffold(
        title = stringResource(Res.string.audio_usage_title),
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            DetailPaneDescription(text = stringResource(Res.string.audio_usage_description))
            Column(Modifier.selectableGroup().padding(horizontal = KoDriverSpacing.large)) {
                AndroidAudioUsage.entries.forEach { usage ->
                    val label = stringResource(audioUsageLabel(usage))
                    AndroidAudioUsageCard(
                        usage = usage,
                        label = label,
                        description = stringResource(audioUsageDescription(usage)),
                        selected = uiState.usage == usage,
                        previewing = uiState.previewingUsage == usage,
                        previewSample = previewSample,
                        onUsageSelected = onUsageSelected,
                        onPreviewClicked = onPreviewClicked,
                    )
                }
            }
        }
    }
}

@Composable
private fun AndroidAudioUsageCard(
    usage: AndroidAudioUsage,
    label: String,
    description: String,
    selected: Boolean,
    previewing: Boolean,
    previewSample: String,
    onUsageSelected: (AndroidAudioUsage) -> Unit,
    onPreviewClicked: (AndroidAudioUsage, String) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = BorderStroke(CARD_BORDER_WIDTH, if (selected) colors.primary else colors.outlineVariant),
        modifier =
            Modifier.fillMaxWidth().padding(vertical = KoDriverSpacing.small).selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onUsageSelected(usage)
                },
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
            modifier = Modifier.padding(KoDriverSpacing.medium),
        ) {
            Box(modifier = Modifier.size(AVATAR_SIZE)) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = if (selected) colors.primary else colors.surfaceContainerHighest,
                    contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(label.take(1), style = MaterialTheme.typography.titleMedium)
                    }
                }
                if (selected) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = colors.onPrimaryContainer,
                        contentColor = colors.primaryContainer,
                        modifier = Modifier.size(CHECK_BADGE_SIZE).align(Alignment.BottomEnd),
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(CHECK_BADGE_PADDING),
                        )
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                )
            }
            FilledTonalIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onPreviewClicked(usage, previewSample)
                },
                shape = MaterialTheme.shapes.extraLarge,
                colors =
                    IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (selected) colors.primary else colors.surfaceContainerHighest,
                        contentColor = if (selected) colors.onPrimary else colors.primary,
                    ),
            ) {
                if (previewing) {
                    PlayingEqualizer(stringResource(Res.string.audio_usage_preview_stop_description, label))
                } else {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = stringResource(Res.string.audio_usage_preview_description, label),
                    )
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
