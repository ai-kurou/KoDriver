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
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneOverview
import kurou.kodriver.domain.model.ReadoutItemKey
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
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutFlagDetailPaneContent(
    uiState: LmuWindowsReadoutFlagDetailUiState,
    onFlagEnabledChanged: (FlagReadoutItem, Boolean) -> Unit,
    onPreviewClicked: (FlagReadoutItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneOverview(text = stringResource(Res.string.flag_description))
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
        ),
        onFlagEnabledChanged = { _, _ -> },
        onPreviewClicked = {},
    )
}
