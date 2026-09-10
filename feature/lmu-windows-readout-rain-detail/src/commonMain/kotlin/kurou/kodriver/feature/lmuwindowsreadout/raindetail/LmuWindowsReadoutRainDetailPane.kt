package kurou.kodriver.feature.lmuwindowsreadout.raindetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.feature.lmuwindowsreadout.raindetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.raindetail.generated.resources.rain_description
import kurou.kodriver.feature.lmuwindowsreadout.raindetail.generated.resources.rain_start_switch_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * LmuWindowsReadoutRainDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutRainDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutRainDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutRainDetailPaneContent(
        uiState = uiState,
        onStartReadoutEnabledChanged = viewModel::onStartReadoutEnabledChanged,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutRainDetailPaneContent(
    uiState: LmuWindowsReadoutRainDetailUiState = LmuWindowsReadoutRainDetailUiState(),
    onStartReadoutEnabledChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailPaneDescription(
            text = stringResource(Res.string.rain_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.rain_start_switch_label),
            checked = uiState.startReadoutEnabled,
            onCheckedChange = onStartReadoutEnabledChanged,
            bottomContent = {},
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutRainDetailPanePreview() {
    LmuWindowsReadoutRainDetailPaneContent()
}
