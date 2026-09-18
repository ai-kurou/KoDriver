package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

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
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.feature.otheroverlaybackgroundopacitydetail.generated.resources.Res
import kurou.kodriver.feature.otheroverlaybackgroundopacitydetail.generated.resources.navigate_back
import kurou.kodriver.feature.otheroverlaybackgroundopacitydetail.generated.resources.opacity_description
import kurou.kodriver.feature.otheroverlaybackgroundopacitydetail.generated.resources.opacity_label
import kurou.kodriver.feature.otheroverlaybackgroundopacitydetail.generated.resources.opacity_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * OtherOverlayBackgroundOpacityDetail の画面を表示する Composable。
 */
@Composable
fun OtherOverlayBackgroundOpacityDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherOverlayBackgroundOpacityDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherOverlayBackgroundOpacityDetailPaneContent(
        uiState = uiState,
        onOpacityChanged = viewModel::onOpacityChanged,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherOverlayBackgroundOpacityDetail の画面本体を表示する Composable。
 */
@Composable
fun OtherOverlayBackgroundOpacityDetailPaneContent(
    uiState: OtherOverlayBackgroundOpacityDetailUiState,
    onOpacityChanged: (Int) -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val opacityLabel = stringResource(Res.string.opacity_label)

    DetailPaneScaffold(
        title = stringResource(Res.string.opacity_title),
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
                text = stringResource(Res.string.opacity_description),
            )
            ThresholdSlider(
                value = uiState.opacity.toFloat(),
                valueRange = 0f..100f,
                labelFormatter = { opacityLabel.formatSliderLabel(it.roundToInt()) },
                onValueChangeFinished = { onOpacityChanged(it.roundToInt()) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
                steps = 99,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherOverlayBackgroundOpacityDetailPanePreview() {
    OtherOverlayBackgroundOpacityDetailPaneContent(uiState = OtherOverlayBackgroundOpacityDetailUiState())
}
