package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_description
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_remaining_title
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_front_left
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_front_right
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_rear_left
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_rear_right
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_row
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * ブレーキ摩耗（4輪の残量%と厚さ）を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutBrakeWearDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutBrakeWearDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutBrakeWearDetailPaneContent(uiState = uiState, modifier = modifier)
}

@Composable
internal fun LmuWindowsReadoutBrakeWearDetailPaneContent(
    uiState: LmuWindowsReadoutBrakeWearDetailUiState = LmuWindowsReadoutBrakeWearDetailUiState(),
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(text = stringResource(Res.string.brake_wear_description))
        DetailPaneCard(
            title = stringResource(Res.string.brake_wear_remaining_title),
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = KoDriverSpacing.small)) {
                    val remaining = uiState.remaining
                    if (remaining == null) {
                        DetailPaneBodyText(text = stringResource(Res.string.brake_wear_unavailable))
                    } else {
                        WheelIndex.entries.forEach { wheel ->
                            remaining.wheels[wheel]?.let { WheelRemainingRow(wheel, it) }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun WheelRemainingRow(
    wheel: WheelIndex,
    remaining: LmuWindowsBrakeWearWheelRemaining,
) {
    Text(
        text =
            stringResource(
                Res.string.brake_wear_wheel_row,
                wheelLabel(wheel),
                formatBrakeWearPercent(remaining.remainingPercent),
                formatBrakeThicknessMillimeters(remaining.thickness),
            ),
        style = koDriverNumericTextStyle(MaterialTheme.typography.bodyMedium),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = KoDriverSpacing.extraSmall),
    )
}

@Composable
private fun wheelLabel(wheel: WheelIndex): String =
    when (wheel) {
        WheelIndex.FRONT_LEFT -> stringResource(Res.string.brake_wear_wheel_front_left)
        WheelIndex.FRONT_RIGHT -> stringResource(Res.string.brake_wear_wheel_front_right)
        WheelIndex.REAR_LEFT -> stringResource(Res.string.brake_wear_wheel_rear_left)
        WheelIndex.REAR_RIGHT -> stringResource(Res.string.brake_wear_wheel_rear_right)
    }

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutBrakeWearDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutBrakeWearDetailPaneContent(
            uiState =
                LmuWindowsReadoutBrakeWearDetailUiState(
                    remaining =
                        LmuWindowsBrakeWearRemainingData(
                            wheels =
                                mapOf(
                                    WheelIndex.FRONT_LEFT to
                                        LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.0305f), 50),
                                    WheelIndex.FRONT_RIGHT to
                                        LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.0310f), 55),
                                    WheelIndex.REAR_LEFT to
                                        LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.0330f), 73),
                                    WheelIndex.REAR_RIGHT to
                                        LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.0335f), 77),
                                ),
                        ),
                ),
        )
    }
}
