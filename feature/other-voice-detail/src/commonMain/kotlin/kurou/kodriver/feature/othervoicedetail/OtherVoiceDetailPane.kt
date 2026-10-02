package kurou.kodriver.feature.othervoicedetail

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.koDriverMonospaceTextStyle
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.feature.othervoicedetail.generated.resources.Res
import kurou.kodriver.feature.othervoicedetail.generated.resources.navigate_back
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_count
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_default_section
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_description
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_empty
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_japanese_section
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_loading
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_preview_description
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_preview_sample
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_preview_stop_description
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_retry
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_saved_missing
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_system_default
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_system_default_description
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val TWO_COLUMN_MIN_WIDTH = 640.dp
private val AVATAR_SIZE = 48.dp
private val CHECK_BADGE_SIZE = 18.dp
private val CARD_BORDER_WIDTH = 1.dp
private val CHECK_BADGE_PADDING = 2.dp
private val EQUALIZER_HEIGHT = 16.dp
private val EQUALIZER_BAR_WIDTH = 3.dp
private val EQUALIZER_BAR_SPACING = 3.dp
private val EQUALIZER_MIN_HEIGHT = 4.dp
private const val EQUALIZER_HALF_CYCLE_MS = 400
private const val EQUALIZER_BAR_DELAY_MS = 150

/**
 * OtherVoiceDetail の画面を表示する Composable。
 */
@Composable
fun OtherVoiceDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherVoiceDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherVoiceDetailPaneContent(
        uiState = uiState,
        onVoiceSelected = viewModel::onVoiceSelected,
        onRetryClicked = viewModel::onRetryClicked,
        onPreviewClicked = viewModel::onPreviewClicked,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherVoiceDetail の画面本体を表示する Composable。
 */
@Composable
fun OtherVoiceDetailPaneContent(
    uiState: OtherVoiceDetailUiState,
    modifier: Modifier = Modifier,
    onVoiceSelected: (String) -> Unit = {},
    onRetryClicked: () -> Unit = {},
    onPreviewClicked: (String, String) -> Unit = { _, _ -> },
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
) {
    val previewSample = stringResource(Res.string.voice_preview_sample)
    val selectedId = if (uiState.savedVoiceMissing) VOICE_ID_UNSPECIFIED else uiState.selectedVoiceId
    DetailPaneScaffold(
        title = stringResource(Res.string.voice_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            DetailPaneDescription(text = stringResource(Res.string.voice_description))
            Column(
                modifier = Modifier.selectableGroup().padding(horizontal = KoDriverSpacing.large),
            ) {
                if (uiState.isLoading) {
                    DetailPaneBodyText(text = stringResource(Res.string.voice_loading))
                } else {
                    DetailPaneSubtitle(text = stringResource(Res.string.voice_default_section))
                    VoiceCards(
                        voices =
                            listOf(
                                TextToSpeechVoice(
                                    VOICE_ID_UNSPECIFIED,
                                    stringResource(Res.string.voice_system_default),
                                    stringResource(Res.string.voice_system_default_description),
                                ),
                            ),
                        selectedId = selectedId,
                        previewingId = uiState.previewingVoiceId,
                        onVoiceSelected = onVoiceSelected,
                        onPreviewClicked = { onPreviewClicked(it, previewSample) },
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        DetailPaneSubtitle(
                            text = stringResource(Res.string.voice_japanese_section),
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(Res.string.voice_count, uiState.voices.size),
                            style = koDriverMonospaceTextStyle(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    VoiceCards(
                        voices = uiState.voices,
                        selectedId = selectedId,
                        previewingId = uiState.previewingVoiceId,
                        onVoiceSelected = onVoiceSelected,
                        onPreviewClicked = { onPreviewClicked(it, previewSample) },
                    )
                    if (uiState.voices.isEmpty()) {
                        DetailPaneBodyText(text = stringResource(Res.string.voice_empty))
                        TextButton(onClick = onRetryClicked) {
                            Text(stringResource(Res.string.voice_retry))
                        }
                    }
                }
                if (uiState.savedVoiceMissing) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.fillMaxWidth().padding(vertical = KoDriverSpacing.large),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
                            modifier = Modifier.padding(KoDriverSpacing.large),
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                            Text(
                                stringResource(Res.string.voice_saved_missing),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 狭いペインでは1列、広いペインでは2列で音声カードを配置する。 */
@Suppress("UnstableCollections")
@Composable
private fun VoiceCards(
    voices: List<TextToSpeechVoice>,
    selectedId: String,
    previewingId: String?,
    onVoiceSelected: (String) -> Unit,
    onPreviewClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cardWidth =
            if (maxWidth >= TWO_COLUMN_MIN_WIDTH + KoDriverSpacing.medium) {
                (maxWidth - KoDriverSpacing.medium) / 2
            } else {
                maxWidth
            }
        FlowRow(
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
        ) {
            voices.forEach { voice ->
                VoiceCard(
                    voice = voice,
                    selected = selectedId == voice.id,
                    previewing = previewingId == voice.id,
                    onSelect = { onVoiceSelected(voice.id) },
                    onPreview = { onPreviewClicked(voice.id) },
                    modifier = Modifier.width(cardWidth),
                )
            }
        }
    }
}

@Composable
private fun VoiceCard(
    voice: TextToSpeechVoice,
    selected: Boolean,
    previewing: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = BorderStroke(CARD_BORDER_WIDTH, if (selected) colors.primary else colors.outlineVariant),
        modifier =
            modifier.selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onSelect()
                },
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
            modifier = Modifier.padding(KoDriverSpacing.medium),
        ) {
            VoiceAvatar(voice = voice, selected = selected)
            Column(modifier = Modifier.weight(1f)) {
                Text(voice.displayName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    voice.cultureName,
                    style = koDriverMonospaceTextStyle(),
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                )
            }
            FilledTonalIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onPreview()
                },
                shape = MaterialTheme.shapes.extraLarge,
                colors =
                    IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (selected) colors.primary else colors.surfaceContainerHighest,
                        contentColor = if (selected) colors.onPrimary else colors.primary,
                    ),
            ) {
                if (previewing) {
                    PlayingEqualizer(
                        contentDescription =
                            stringResource(Res.string.voice_preview_stop_description, voice.displayName),
                    )
                } else {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = stringResource(Res.string.voice_preview_description, voice.displayName),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayingEqualizer(contentDescription: String) {
    val transition = rememberInfiniteTransition(label = "voicePreview")
    val contentColor = LocalContentColor.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(EQUALIZER_BAR_SPACING),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(EQUALIZER_HEIGHT).semantics { this.contentDescription = contentDescription },
    ) {
        repeat(3) { index ->
            val fraction by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(EQUALIZER_HALF_CYCLE_MS),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * EQUALIZER_BAR_DELAY_MS),
                    ),
                label = "bar$index",
            )
            Box(
                Modifier
                    .width(EQUALIZER_BAR_WIDTH)
                    .height(EQUALIZER_MIN_HEIGHT + (EQUALIZER_HEIGHT - EQUALIZER_MIN_HEIGHT) * fraction)
                    .background(contentColor, MaterialTheme.shapes.extraSmall),
            )
        }
    }
}

@Composable
private fun VoiceAvatar(
    voice: TextToSpeechVoice,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier.size(AVATAR_SIZE)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = if (selected) colors.primary else colors.surfaceContainerHighest,
            contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (voice.id == VOICE_ID_UNSPECIFIED) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                } else {
                    Text(voice.displayName.take(1), style = MaterialTheme.typography.titleMedium)
                }
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
                    modifier =
                        Modifier.padding(CHECK_BADGE_PADDING),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherVoiceDetailPanePreview() {
    KoDriverTheme {
        OtherVoiceDetailPaneContent(uiState = OtherVoiceDetailUiState())
    }
}
