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
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_switch_subtitle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
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
        DetailPaneDescription(text = stringResource(Res.string.flag_description))
        DetailPaneSubtitle(text = stringResource(Res.string.flag_switch_subtitle))
        FlagReadoutItem.entries.forEach { item ->
            val chipLabel = stringResource(item.chipLabelRes)
            DetailPaneCard(
                title = stringResource(item.labelRes),
                checked = uiState.enabledStates[item.key] ?: true,
                chipLabels = listOf(chipLabel),
                selectedChipLabels = setOf(chipLabel),
                onCheckedChange = { enabled -> onFlagEnabledChanged(item, enabled) },
                onChipClick = { onPreviewClicked(item) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
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
                ReadoutItemKey.BlueFlag to true,
                ReadoutItemKey.SectorYellowFlag to true,
                ReadoutItemKey.FullCourseYellow to true,
                ReadoutItemKey.RedFlag to true,
            ),
        ),
        onFlagEnabledChanged = { _, _ -> },
        onPreviewClicked = {},
    )
}
