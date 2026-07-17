package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

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
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_description
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_red
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_session_stop
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LmuWindowsReadoutFlagDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: LmuWindowsReadoutFlagDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutFlagDetailPaneContent(
        uiState = uiState,
        onFlagEnabledChanged = viewModel::onFlagEnabledChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        onRedFlagEnabledChanged = viewModel::onRedFlagEnabledChanged,
        onRedFlagVoiceTypeChanged = viewModel::onRedFlagVoiceTypeChanged,
        onRedFlagPreviewClicked = viewModel::onRedFlagPreviewClicked,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsReadoutFlagDetailPaneContent(
    uiState: LmuWindowsReadoutFlagDetailUiState,
    onFlagEnabledChanged: (FlagReadoutItem, Boolean) -> Unit,
    onPreviewClicked: (FlagReadoutItem) -> Unit,
    onRedFlagEnabledChanged: (Boolean) -> Unit,
    onRedFlagVoiceTypeChanged: (RedFlagVoiceType) -> Unit,
    onRedFlagPreviewClicked: (RedFlagVoiceType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneBodyText(
            text = stringResource(Res.string.flag_description),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        FlagReadoutItem.entries.forEach { item ->
            val chipLabel = stringResource(item.chipLabelRes)
            val checked = uiState.enabledStates[item.key] ?: true
            DetailPaneCard(
                title = stringResource(item.labelRes),
                checked = checked,
                onCheckedChange = { enabled -> onFlagEnabledChanged(item, enabled) },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                bottomContent = {
                    DetailPaneCardChips(
                        chipLabels = listOf(chipLabel),
                        selectedChipLabels = setOf(chipLabel),
                        chipEnabled = checked,
                        onChipClick = { onPreviewClicked(item) },
                    )
                },
            )
        }
        val redFlagLabel = stringResource(Res.string.flag_red)
        val sessionStopLabel = stringResource(Res.string.flag_session_stop)
        val redFlagChecked = uiState.enabledStates[ReadoutItemKey.LmuWindows.Flag.RedFlag] ?: true
        val selectedRedFlagLabel = when (uiState.redFlagVoiceType) {
            RedFlagVoiceType.RED_FLAG -> redFlagLabel
            RedFlagVoiceType.SESSION_STOP -> sessionStopLabel
        }
        DetailPaneCard(
            title = redFlagLabel,
            checked = redFlagChecked,
            onCheckedChange = onRedFlagEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                DetailPaneCardChips(
                    chipLabels = listOf(redFlagLabel, sessionStopLabel),
                    selectedChipLabels = setOf(selectedRedFlagLabel),
                    chipEnabled = redFlagChecked,
                    onChipClick = { label ->
                        val type = if (label == redFlagLabel) {
                            RedFlagVoiceType.RED_FLAG
                        } else {
                            RedFlagVoiceType.SESSION_STOP
                        }
                        onRedFlagVoiceTypeChanged(type)
                        onRedFlagPreviewClicked(type)
                    },
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutFlagDetailPanePreview() {
    LmuWindowsReadoutFlagDetailPaneContent(
        uiState = LmuWindowsReadoutFlagDetailUiState(
            enabledStates = mapOf(
                ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
                ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to true,
                ReadoutItemKey.LmuWindows.Flag.FullCourseYellow to true,
                ReadoutItemKey.LmuWindows.Flag.RedFlag to true,
            ),
            redFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
        ),
        onFlagEnabledChanged = { _, _ -> },
        onPreviewClicked = {},
        onRedFlagEnabledChanged = {},
        onRedFlagVoiceTypeChanged = {},
        onRedFlagPreviewClicked = {},
    )
}
