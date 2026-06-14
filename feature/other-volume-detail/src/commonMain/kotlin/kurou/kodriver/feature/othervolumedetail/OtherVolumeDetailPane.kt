package kurou.kodriver.feature.othervolumedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.othervolumedetail.generated.resources.Res
import kodriver.feature.othervolumedetail.generated.resources.volume_description
import kodriver.feature.othervolumedetail.generated.resources.volume_label
import kodriver.feature.othervolumedetail.generated.resources.volume_subtitle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun OtherVolumeDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherVolumeDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherVolumeDetailPaneContent(
        uiState = uiState,
        onVolumeChanged = viewModel::onVolumeChanged,
        modifier = modifier,
    )
}

@Composable
internal fun OtherVolumeDetailPaneContent(
    uiState: OtherVolumeDetailUiState,
    onVolumeChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val volumeLabel = stringResource(Res.string.volume_label)

    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneDescription(text = stringResource(Res.string.volume_description))
        DetailPaneSubtitle(text = stringResource(Res.string.volume_subtitle))
        ThresholdSlider(
            value = uiState.volume.toFloat(),
            valueRange = 0f..100f,
            labelFormatter = { volumeLabel.format(it.roundToInt()) },
            onValueChangeFinished = { onVolumeChanged(it.roundToInt()) },
            steps = 99,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherVolumeDetailPanePreview() {
    OtherVolumeDetailPaneContent(uiState = OtherVolumeDetailUiState())
}
