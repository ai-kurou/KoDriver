package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_rr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_unavailable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BrakeWearContent(
    selectedSimulator: Simulator,
    brakeWear: LmuWindowsBrakeWearRemainingData?,
) {
    val wheels =
        when (selectedSimulator) {
            is Simulator.LmuWindows -> brakeWear?.wheels
            is Simulator.AceWindows, is Simulator.Gt7Ps5 -> null
        }
    if (wheels == null) {
        Text(text = stringResource(Res.string.debug_state_brake_wear_unavailable), style = koDriverNumericTextStyle())
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelBrakeWearText(
                wheels,
                WheelIndex.FRONT_LEFT,
                Res.string.debug_state_brake_wear_fl,
                Modifier.weight(1f),
            )
            WheelBrakeWearText(
                wheels,
                WheelIndex.FRONT_RIGHT,
                Res.string.debug_state_brake_wear_fr,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelBrakeWearText(
                wheels,
                WheelIndex.REAR_LEFT,
                Res.string.debug_state_brake_wear_rl,
                Modifier.weight(1f),
            )
            WheelBrakeWearText(
                wheels,
                WheelIndex.REAR_RIGHT,
                Res.string.debug_state_brake_wear_rr,
                Modifier.weight(1f),
            )
        }
    }
}

// デバッグ表示専用の固定4輪分Mapのため、ImmutableMap化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun WheelBrakeWearText(
    wheels: Map<WheelIndex, LmuWindowsBrakeWearWheelRemaining>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
    modifier: Modifier = Modifier,
) {
    val wheel = wheels[wheelIndex]
    val value =
        wheel?.let {
            "${formatBrakeWearPercent(it.remainingPercent)}(${formatBrakeThicknessMillimeters(it.thickness)}mm)"
        } ?: "--"
    Text(
        text = stringResource(labelRes, value),
        modifier = modifier,
        style = koDriverNumericTextStyle(),
    )
}
