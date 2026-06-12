package kurou.kodriver.feature.readout.flag

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
import kodriver.feature.readout.flag.generated.resources.Res
import kodriver.feature.readout.flag.generated.resources.flag_blue
import kodriver.feature.readout.flag.generated.resources.flag_description
import kodriver.feature.readout.flag.generated.resources.flag_full_course_yellow
import kodriver.feature.readout.flag.generated.resources.flag_red
import kodriver.feature.readout.flag.generated.resources.flag_session_stop
import kodriver.feature.readout.flag.generated.resources.flag_switch_subtitle
import kodriver.feature.readout.flag.generated.resources.flag_title
import kodriver.feature.readout.flag.generated.resources.flag_yellow
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.DetailPaneTitle
import kurou.kodriver.domain.model.ReadoutItemKey
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private data class FlagSwitchItem(
    val key: String,
    val labelRes: StringResource,
    val chipLabelRes: StringResource,
)

private val flagSwitchItems = listOf(
    FlagSwitchItem(ReadoutItemKey.BLUE_FLAG, Res.string.flag_blue, Res.string.flag_blue),
    FlagSwitchItem(ReadoutItemKey.SECTOR_YELLOW_FLAG, Res.string.flag_yellow, Res.string.flag_yellow),
    FlagSwitchItem(
        ReadoutItemKey.FULL_COURSE_YELLOW,
        Res.string.flag_full_course_yellow,
        Res.string.flag_full_course_yellow,
    ),
    FlagSwitchItem(ReadoutItemKey.RED_FLAG, Res.string.flag_red, Res.string.flag_session_stop),
)

@Composable
fun FlagDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: FlagViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FlagDetailPaneContent(
        uiState = uiState,
        onFlagEnabledChanged = viewModel::onFlagEnabledChanged,
        modifier = modifier,
    )
}

@Composable
internal fun FlagDetailPaneContent(
    uiState: FlagUiState,
    onFlagEnabledChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneTitle(title = stringResource(Res.string.flag_title))
        DetailPaneDescription(text = stringResource(Res.string.flag_description))
        DetailPaneSubtitle(text = stringResource(Res.string.flag_switch_subtitle))
        flagSwitchItems.forEach { item ->
            DetailPaneCard(
                title = stringResource(item.labelRes),
                checked = uiState.enabledStates[item.key] ?: true,
                chipLabels = listOf(stringResource(item.chipLabelRes)),
                onCheckedChange = { enabled -> onFlagEnabledChanged(item.key, enabled) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FlagDetailPanePreview() {
    FlagDetailPaneContent(
        uiState = FlagUiState(
            enabledStates = mapOf(
                ReadoutItemKey.BLUE_FLAG to true,
                ReadoutItemKey.SECTOR_YELLOW_FLAG to true,
                ReadoutItemKey.FULL_COURSE_YELLOW to true,
                ReadoutItemKey.RED_FLAG to true,
            ),
        ),
        onFlagEnabledChanged = { _, _ -> },
    )
}
