package kurou.kodriver.feature.othervoicepitchdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
import kurou.kodriver.domain.model.VOICE_PITCH_MAX
import kurou.kodriver.domain.model.VOICE_PITCH_MIN
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.Res
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.navigate_back
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_description
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_label
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
    OtherVoicePitchDetailPaneContent(
        uiState = uiState,
        onPitchChanged = viewModel::onPitchChanged,
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
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val pitchLabel = stringResource(Res.string.voice_pitch_label)

    DetailPaneScaffold(
        title = stringResource(Res.string.voice_pitch_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            DetailPaneDescription(
                text = stringResource(Res.string.voice_pitch_description),
            )
            ThresholdSlider(
                value = uiState.pitch,
                valueRange = VOICE_PITCH_MIN..VOICE_PITCH_MAX,
                labelFormatter = { pitchLabel.formatSliderLabel(it) },
                onValueChangeFinished = { onPitchChanged(it) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
                steps = 14,
                defaultValue = VOICE_PITCH_DEFAULT,
                onResetToDefault = { onPitchChanged(VOICE_PITCH_DEFAULT) },
                resetContentDescription = stringResource(Res.string.voice_pitch_reset),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherVoicePitchDetailPanePreview() {
    KoDriverTheme {
        OtherVoicePitchDetailPaneContent(uiState = OtherVoicePitchDetailUiState())
    }
}
