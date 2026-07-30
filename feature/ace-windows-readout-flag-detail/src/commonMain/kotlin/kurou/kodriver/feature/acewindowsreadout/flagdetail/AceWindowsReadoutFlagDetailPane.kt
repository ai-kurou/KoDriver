package kurou.kodriver.feature.acewindowsreadout.flagdetail

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
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.Res
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_description
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AceWindowsReadoutFlagDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: AceWindowsReadoutFlagDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AceWindowsReadoutFlagDetailPaneContent(
        uiState = uiState,
        onFlagEnabledChanged = viewModel::onFlagEnabledChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun AceWindowsReadoutFlagDetailPaneContent(
    uiState: AceWindowsReadoutFlagDetailUiState = AceWindowsReadoutFlagDetailUiState(),
    onFlagEnabledChanged: (FlagReadoutItem, Boolean) -> Unit = { _, _ -> },
    onPreviewClicked: (FlagReadoutItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.flag_description),
        )
        FlagReadoutItem.entries.forEach { item ->
            val chipLabel = stringResource(item.labelRes)
            val checked = uiState.enabledStates[item.key] ?: true
            DetailPaneCard(
                title = chipLabel,
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
private fun AceWindowsReadoutFlagDetailPanePreview() {
    AceWindowsReadoutFlagDetailPaneContent(
        uiState = AceWindowsReadoutFlagDetailUiState(
            enabledStates = FlagReadoutItem.entries.associate { it.key to true },
        ),
    )
}
