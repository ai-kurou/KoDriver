package kurou.kodriver.feature.othervoicespeeddetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT
import kurou.kodriver.domain.model.VOICE_SPEED_MAX
import kurou.kodriver.domain.model.VOICE_SPEED_MIN
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.Res
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.navigate_back
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
    val speedLabel = stringResource(Res.string.voice_speed_label)
    val previewSample = stringResource(Res.string.voice_speed_preview_sample)

    DetailPaneScaffold(
        title = stringResource(Res.string.voice_speed_title),
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
                text = stringResource(Res.string.voice_speed_description),
            )
            ThresholdSlider(
                value = uiState.speed,
                valueRange = VOICE_SPEED_MIN..VOICE_SPEED_MAX,
                labelFormatter = { speedLabel.formatSliderLabel(it) },
                onValueChangeFinished = { onSpeedChanged(it) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
                steps = 14,
                defaultValue = VOICE_SPEED_DEFAULT,
                onResetToDefault = { onSpeedChanged(VOICE_SPEED_DEFAULT) },
                resetContentDescription = stringResource(Res.string.voice_speed_reset),
            )
            FilledTonalButton(
                onClick = { onPreviewClicked(previewSample) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            ) {
                Icon(
                    imageVector = if (uiState.isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                )
                Spacer(Modifier.width(KoDriverSpacing.small))
                Text(
                    stringResource(
                        if (uiState.isPreviewing) {
                            Res.string.voice_speed_preview_stop
                        } else {
                            Res.string.voice_speed_preview
                        },
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherVoiceSpeedDetailPanePreview() {
    KoDriverTheme {
        OtherVoiceSpeedDetailPaneContent(uiState = OtherVoiceSpeedDetailUiState())
    }
}
