package kurou.kodriver.feature.othervoicespeeddetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.SliderPreviewDetailPaneContent
import kurou.kodriver.core.designsystem.SliderPreviewDetailTexts
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT
import kurou.kodriver.domain.model.VOICE_SPEED_MAX
import kurou.kodriver.domain.model.VOICE_SPEED_MIN
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.Res
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.voice_speed_description
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.voice_speed_label
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.voice_speed_preview
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.voice_speed_preview_sample
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.voice_speed_preview_stop
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.voice_speed_reset
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.voice_speed_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * OtherVoiceSpeedDetail の画面を表示する Composable。
 */
@Composable
fun OtherVoiceSpeedDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherVoiceSpeedDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    OtherVoiceSpeedDetailPaneContent(
        uiState = uiState,
        onSpeedChanged = viewModel::onSpeedChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherVoiceSpeedDetail の画面本体を表示する Composable。
 */
@Composable
fun OtherVoiceSpeedDetailPaneContent(
    uiState: OtherVoiceSpeedDetailUiState,
    onSpeedChanged: (Float) -> Unit = {},
    onPreviewClicked: (String) -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    SliderPreviewDetailPaneContent(
        texts =
            SliderPreviewDetailTexts(
                title = stringResource(Res.string.voice_speed_title),
                description = stringResource(Res.string.voice_speed_description),
                sliderLabel = stringResource(Res.string.voice_speed_label),
                resetContentDescription = stringResource(Res.string.voice_speed_reset),
                previewLabel = stringResource(Res.string.voice_speed_preview),
                previewStopLabel = stringResource(Res.string.voice_speed_preview_stop),
                previewSample = stringResource(Res.string.voice_speed_preview_sample),
            ),
        value = uiState.speed,
        valueRange = VOICE_SPEED_MIN..VOICE_SPEED_MAX,
        steps = 14,
        defaultValue = VOICE_SPEED_DEFAULT,
        isPreviewing = uiState.isPreviewing,
        onValueChangeFinished = onSpeedChanged,
        onPreviewClicked = onPreviewClicked,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherVoiceSpeedDetailPanePreview() {
    KoDriverTheme {
        OtherVoiceSpeedDetailPaneContent(uiState = OtherVoiceSpeedDetailUiState())
    }
}
