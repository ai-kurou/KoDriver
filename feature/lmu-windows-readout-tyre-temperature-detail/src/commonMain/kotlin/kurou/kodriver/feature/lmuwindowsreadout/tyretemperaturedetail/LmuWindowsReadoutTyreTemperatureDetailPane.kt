package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_description
import kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_label
import kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_reset
import kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_subtitle
import kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_threshold_help_description
import kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_threshold_help_icon_content_description
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val HIGH_THRESHOLD_MIN = 90f
private const val HIGH_THRESHOLD_MAX = 100f
private const val HIGH_THRESHOLD_DEFAULT = 90f

@Composable
fun LmuWindowsReadoutTyreTemperatureDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: LmuWindowsReadoutTyreTemperatureDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
        uiState = uiState,
        onHighThresholdChanged = viewModel::onHighThresholdChanged,
        onHighThresholdReset = viewModel::onHighThresholdReset,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LmuWindowsReadoutTyreTemperatureDetailPaneContent(
    uiState: LmuWindowsReadoutTyreTemperatureDetailUiState,
    onHighThresholdChanged: (Int) -> Unit = {},
    onHighThresholdReset: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
        ) {
            TyreTemperatureThresholdHelpSheetContent()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(text = stringResource(Res.string.tyre_temperature_description))
        val helpIconContentDescription =
            stringResource(Res.string.tyre_temperature_threshold_help_icon_content_description)
        DetailPaneSubtitle(
            text = stringResource(Res.string.tyre_temperature_high_threshold_subtitle),
            trailingContent = {
                IconButton(onClick = { showHelpSheet = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = helpIconContentDescription,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            },
        )
        DetailPaneDescription(text = stringResource(Res.string.tyre_temperature_high_threshold_description))
        val labelTemplate = stringResource(Res.string.tyre_temperature_high_threshold_label)
        ThresholdSlider(
            value = uiState.highThresholdCelsius.toFloat(),
            valueRange = HIGH_THRESHOLD_MIN..HIGH_THRESHOLD_MAX,
            steps = (HIGH_THRESHOLD_MAX - HIGH_THRESHOLD_MIN).toInt() - 1,
            labelFormatter = { labelTemplate.format(it.roundToInt()) },
            onValueChangeFinished = { onHighThresholdChanged(it.roundToInt()) },
            defaultValue = HIGH_THRESHOLD_DEFAULT,
            onResetToDefault = onHighThresholdReset,
            resetContentDescription = stringResource(Res.string.tyre_temperature_high_threshold_reset),
        )
    }
}

@Composable
internal fun TyreTemperatureThresholdHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.tyre_temperature_threshold_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutTyreTemperatureDetailPanePreview() {
    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
        uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
    )
}
