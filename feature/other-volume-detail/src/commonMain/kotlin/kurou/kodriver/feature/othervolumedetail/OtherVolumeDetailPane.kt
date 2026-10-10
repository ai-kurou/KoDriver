package kurou.kodriver.feature.othervolumedetail

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
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.feature.othervolumedetail.generated.resources.Res
import kurou.kodriver.feature.othervolumedetail.generated.resources.device_volume_description
import kurou.kodriver.feature.othervolumedetail.generated.resources.device_volume_subtitle
import kurou.kodriver.feature.othervolumedetail.generated.resources.navigate_back
import kurou.kodriver.feature.othervolumedetail.generated.resources.volume_description
import kurou.kodriver.feature.othervolumedetail.generated.resources.volume_formula
import kurou.kodriver.feature.othervolumedetail.generated.resources.volume_label
import kurou.kodriver.feature.othervolumedetail.generated.resources.volume_low_warning
import kurou.kodriver.feature.othervolumedetail.generated.resources.volume_preview
import kurou.kodriver.feature.othervolumedetail.generated.resources.volume_preview_stop
import kurou.kodriver.feature.othervolumedetail.generated.resources.volume_subtitle
import kurou.kodriver.feature.othervolumedetail.generated.resources.volume_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * OtherVolumeDetail の画面を表示する Composable。
 */
@Composable
fun OtherVolumeDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherVolumeDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    OtherVolumeDetailPaneContent(
        uiState = uiState,
        onVolumeChanged = viewModel::onVolumeChanged,
        onDeviceVolumeChanged = viewModel::onDeviceVolumeChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherVolumeDetail の画面本体を表示する Composable。
 */
@Composable
fun OtherVolumeDetailPaneContent(
    uiState: OtherVolumeDetailUiState,
    onVolumeChanged: (Int) -> Unit = {},
    onDeviceVolumeChanged: (Int) -> Unit = {},
    onPreviewClicked: () -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val volumeLabel = stringResource(Res.string.volume_label)

    DetailPaneScaffold(
        title = stringResource(Res.string.volume_title),
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
                text = stringResource(Res.string.volume_description),
            )
            DetailPaneSubtitle(
                text = stringResource(Res.string.volume_subtitle),
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            )
            Column(modifier = Modifier.padding(horizontal = KoDriverSpacing.large, vertical = KoDriverSpacing.small)) {
                DetailPaneBodyText(text = stringResource(Res.string.volume_formula))
                DetailPaneBodyText(text = stringResource(Res.string.volume_low_warning))
            }
            ThresholdSlider(
                value = uiState.volume.toFloat(),
                valueRange = 0f..100f,
                labelFormatter = { volumeLabel.formatSliderLabel(it.roundToInt()) },
                onValueChangeFinished = { onVolumeChanged(it.roundToInt()) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
                steps = 99,
            )
            FilledTonalButton(
                onClick = onPreviewClicked,
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            ) {
                Icon(
                    imageVector = if (uiState.isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                )
                Spacer(Modifier.width(KoDriverSpacing.small))
                Text(
                    stringResource(
                        if (uiState.isPreviewing) Res.string.volume_preview_stop else Res.string.volume_preview,
                    ),
                )
            }
            DetailPaneSubtitle(
                text = stringResource(Res.string.device_volume_subtitle),
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            )
            Column(modifier = Modifier.padding(horizontal = KoDriverSpacing.large, vertical = KoDriverSpacing.small)) {
                DetailPaneBodyText(text = stringResource(Res.string.device_volume_description))
            }
            ThresholdSlider(
                value = uiState.deviceVolume.toFloat(),
                valueRange = 0f..100f,
                labelFormatter = { volumeLabel.formatSliderLabel(it.roundToInt()) },
                onValueChangeFinished = { onDeviceVolumeChanged(it.roundToInt()) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
                steps = 99,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherVolumeDetailPanePreview() {
    KoDriverTheme {
        OtherVolumeDetailPaneContent(uiState = OtherVolumeDetailUiState())
    }
}
