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
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_description
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_enabled
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_voice_type_casual
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_voice_type_formal
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * LmuWindowsReadoutMyBestLapDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutMyBestLapDetailPane(modifier: Modifier = Modifier) {
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
    val voiceTypeLabels = MyBestLapVoiceType.entries.map { type -> type to type.displayName() }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.my_best_lap_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.my_best_lap_enabled),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                DetailPaneCardChips(
                    chipLabels = voiceTypeLabels.map { (_, label) -> label },
                    selectedChipLabels =
                        voiceTypeLabels
                            .filter { (type, _) -> type == uiState.voiceType }
                            .map { (_, label) -> label }
                            .toSet(),
                    chipEnabled = true,
                    onChipClick = { label ->
                        val type = voiceTypeLabels.first { (_, typeLabel) -> typeLabel == label }.first
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

@Composable
private fun MyBestLapVoiceType.displayName(): String =
    when (this) {
        MyBestLapVoiceType.FORMAL -> stringResource(Res.string.my_best_lap_voice_type_formal)
        MyBestLapVoiceType.CASUAL -> stringResource(Res.string.my_best_lap_voice_type_casual)
    }
