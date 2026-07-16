package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

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
import kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_description
import kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_enabled
import kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_voice_type_casual
import kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_voice_type_formal
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneOverview
import kurou.kodriver.domain.model.MyBestLapVoiceType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LmuWindowsReadoutMyBestLapDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: LmuWindowsReadoutMyBestLapDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutMyBestLapDetailPaneContent(
        uiState = uiState,
        onVoiceTypeChanged = viewModel::onVoiceTypeChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutMyBestLapDetailPaneContent(
    uiState: LmuWindowsReadoutMyBestLapDetailUiState = LmuWindowsReadoutMyBestLapDetailUiState(),
    onVoiceTypeChanged: (MyBestLapVoiceType) -> Unit = {},
    onPreviewClicked: (MyBestLapVoiceType) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val formalLabel = stringResource(Res.string.my_best_lap_voice_type_formal)
    val casualLabel = stringResource(Res.string.my_best_lap_voice_type_casual)
    val chipLabels = listOf(formalLabel, casualLabel)
    val selectedLabel = when (uiState.voiceType) {
        MyBestLapVoiceType.FORMAL -> formalLabel
        MyBestLapVoiceType.CASUAL -> casualLabel
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneOverview(text = stringResource(Res.string.my_best_lap_description))
        DetailPaneCard(
            title = stringResource(Res.string.my_best_lap_enabled),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                DetailPaneCardChips(
                    chipLabels = chipLabels,
                    selectedChipLabels = setOf(selectedLabel),
                    chipEnabled = true,
                    onChipClick = { label ->
                        val type = when (label) {
                            casualLabel -> MyBestLapVoiceType.CASUAL
                            else -> MyBestLapVoiceType.FORMAL
                        }
                        onVoiceTypeChanged(type)
                        onPreviewClicked(type)
                    },
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutMyBestLapDetailPanePreview() {
    LmuWindowsReadoutMyBestLapDetailPaneContent(
        uiState = LmuWindowsReadoutMyBestLapDetailUiState(voiceType = MyBestLapVoiceType.FORMAL),
    )
}
