package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_brake_info_title
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_clear_baseline
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_description
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_set_baseline
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wearables_title
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_front_left
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_front_right
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_other
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_rear_left
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_rear_right
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_row
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_row_with_delta
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * ブレーキ摩耗の調査用に、REST API の生の配列値を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutBrakeWearDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutBrakeWearDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutBrakeWearDetailPaneContent(
        uiState = uiState,
        onBaselineSet = viewModel::onBaselineSet,
        onBaselineCleared = viewModel::onBaselineCleared,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutBrakeWearDetailPaneContent(
    uiState: LmuWindowsReadoutBrakeWearDetailUiState = LmuWindowsReadoutBrakeWearDetailUiState(),
    onBaselineSet: () -> Unit = {},
    onBaselineCleared: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(text = stringResource(Res.string.brake_wear_description))
        Row(
            modifier = Modifier.padding(horizontal = KoDriverSpacing.large, vertical = KoDriverSpacing.extraSmall),
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            Button(onClick = onBaselineSet) {
                Text(stringResource(Res.string.brake_wear_set_baseline))
            }
            OutlinedButton(onClick = onBaselineCleared, enabled = uiState.baseline != null) {
                Text(stringResource(Res.string.brake_wear_clear_baseline))
            }
        }
        BrakeWearValuesCard(
            title = stringResource(Res.string.brake_wear_wearables_title),
            values = uiState.current.wearablesBrakes,
            baseline = uiState.baseline?.wearablesBrakes,
        )
        BrakeWearValuesCard(
            title = stringResource(Res.string.brake_wear_brake_info_title),
            values = uiState.current.brakeInfo,
            baseline = uiState.baseline?.brakeInfo,
        )
    }
}

// 4輪分（数要素）の配列のため、ImmutableList化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun BrakeWearValuesCard(
    title: String,
    values: List<Double>?,
    baseline: List<Double>?,
) {
    DetailPaneCard(
        title = title,
        modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
        bottomContent = {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = KoDriverSpacing.small)) {
                if (values == null) {
                    DetailPaneBodyText(text = stringResource(Res.string.brake_wear_unavailable))
                } else {
                    values.forEachIndexed { index, value ->
                        val label = wheelLabel(index)
                        val base = baseline?.getOrNull(index)
                        DetailPaneBodyText(
                            text =
                                if (base == null) {
                                    stringResource(Res.string.brake_wear_wheel_row, label, formatBrakeWearValue(value))
                                } else {
                                    stringResource(
                                        Res.string.brake_wear_wheel_row_with_delta,
                                        label,
                                        formatBrakeWearValue(value),
                                        formatBrakeWearDelta(value - base),
                                    )
                                },
                        )
                    }
                }
            }
        },
    )
}

/** 配列は FL, FR, RL, RR の順と推測されている。4輪を超える要素は番号で表示する。 */
@Composable
private fun wheelLabel(index: Int): String =
    when (index) {
        0 -> stringResource(Res.string.brake_wear_wheel_front_left)
        1 -> stringResource(Res.string.brake_wear_wheel_front_right)
        2 -> stringResource(Res.string.brake_wear_wheel_rear_left)
        3 -> stringResource(Res.string.brake_wear_wheel_rear_right)
        else -> stringResource(Res.string.brake_wear_wheel_other, index + 1)
    }

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutBrakeWearDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutBrakeWearDetailPaneContent(
            uiState =
                LmuWindowsReadoutBrakeWearDetailUiState(
                    current =
                        LmuWindowsBrakeWearInvestigationData(
                            wearablesBrakes = listOf(0.036, 0.035, 0.032, 0.031),
                            brakeInfo = listOf(0.036, 0.036, 0.032, 0.032),
                        ),
                ),
        )
    }
}
