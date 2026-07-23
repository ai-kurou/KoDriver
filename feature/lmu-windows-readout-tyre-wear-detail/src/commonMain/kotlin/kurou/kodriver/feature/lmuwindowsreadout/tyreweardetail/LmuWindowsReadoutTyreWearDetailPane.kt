package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_description
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_warning_chip
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_warning_title
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LmuWindowsReadoutTyreWearDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: LmuWindowsReadoutTyreWearDetailViewModel = koinViewModel()
    LmuWindowsReadoutTyreWearDetailPaneContent(
        onWarningChipClicked = viewModel::onWarningChipClicked,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutTyreWearDetailPaneContent(
    onWarningChipClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneBodyText(
            text = stringResource(Res.string.tyre_wear_description),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        val warningChipLabel = stringResource(Res.string.tyre_wear_warning_chip)
        DetailPaneCard(
            title = stringResource(Res.string.tyre_wear_warning_title),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                DetailPaneCardChips(
                    chipLabels = listOf(warningChipLabel),
                    selectedChipLabels = setOf(warningChipLabel),
                    chipEnabled = true,
                    onChipClick = { onWarningChipClicked() },
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutTyreWearDetailPanePreview() {
    LmuWindowsReadoutTyreWearDetailPaneContent()
}
