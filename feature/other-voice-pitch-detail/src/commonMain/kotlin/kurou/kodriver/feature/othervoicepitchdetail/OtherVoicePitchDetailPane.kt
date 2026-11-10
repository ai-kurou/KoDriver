package kurou.kodriver.feature.othervoicepitchdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.SliderPreviewDetailPaneContent
import kurou.kodriver.core.designsystem.SliderPreviewDetailTexts
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
import kurou.kodriver.domain.model.VOICE_PITCH_MAX
import kurou.kodriver.domain.model.VOICE_PITCH_MIN
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.Res
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_description
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_label
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_preview
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_preview_sample
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_preview_stop
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_reset
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * OtherVoicePitchDetail の画面を表示する Composable。
 */
@Composable
fun OtherVoicePitchDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherVoicePitchDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    OtherVoicePitchDetailPaneContent(
        uiState = uiState,
        onPitchChanged = viewModel::onPitchChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherVoicePitchDetail の画面本体を表示する Composable。
 */
@Composable
fun OtherVoicePitchDetailPaneContent(
    uiState: OtherVoicePitchDetailUiState,
    onPitchChanged: (Float) -> Unit = {},
    onPreviewClicked: (String) -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    SliderPreviewDetailPaneContent(
        texts =
            SliderPreviewDetailTexts(
                title = stringResource(Res.string.voice_pitch_title),
                description = stringResource(Res.string.voice_pitch_description),
                sliderLabel = stringResource(Res.string.voice_pitch_label),
                resetContentDescription = stringResource(Res.string.voice_pitch_reset),
                previewLabel = stringResource(Res.string.voice_pitch_preview),
                previewStopLabel = stringResource(Res.string.voice_pitch_preview_stop),
                previewSample = stringResource(Res.string.voice_pitch_preview_sample),
            ),
        value = uiState.pitch,
        valueRange = VOICE_PITCH_MIN..VOICE_PITCH_MAX,
        steps = 14,
        defaultValue = VOICE_PITCH_DEFAULT,
        isPreviewing = uiState.isPreviewing,
        onValueChangeFinished = onPitchChanged,
        onPreviewClicked = onPreviewClicked,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherVoicePitchDetailPanePreview() {
    KoDriverTheme {
        OtherVoicePitchDetailPaneContent(uiState = OtherVoicePitchDetailUiState())
    }
}
