package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.AceWindowsBrakeWearData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_disc_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_disc_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_disc_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_disc_rr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_pad_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_pad_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_pad_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_wear_pad_rr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val WHEEL_COLUMN_WIDTH = 110.dp

@Composable
internal fun BrakeWearContent(
    selectedSimulator: Simulator,
    aceWindowsBrakeWear: AceWindowsBrakeWearData?,
) {
    if (selectedSimulator !is Simulator.AceWindows || aceWindowsBrakeWear == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Column {
        Row {
            WheelLifeText(aceWindowsBrakeWear.padLife, WheelIndex.FRONT_LEFT, Res.string.debug_state_brake_wear_pad_fl)
            WheelLifeText(aceWindowsBrakeWear.padLife, WheelIndex.FRONT_RIGHT, Res.string.debug_state_brake_wear_pad_fr)
        }
        Row {
            WheelLifeText(aceWindowsBrakeWear.padLife, WheelIndex.REAR_LEFT, Res.string.debug_state_brake_wear_pad_rl)
            WheelLifeText(aceWindowsBrakeWear.padLife, WheelIndex.REAR_RIGHT, Res.string.debug_state_brake_wear_pad_rr)
        }
        Row {
            WheelLifeText(
                aceWindowsBrakeWear.discLife,
                WheelIndex.FRONT_LEFT,
                Res.string.debug_state_brake_wear_disc_fl,
            )
            WheelLifeText(
                aceWindowsBrakeWear.discLife,
                WheelIndex.FRONT_RIGHT,
                Res.string.debug_state_brake_wear_disc_fr,
            )
        }
        Row {
            WheelLifeText(
                aceWindowsBrakeWear.discLife,
                WheelIndex.REAR_LEFT,
                Res.string.debug_state_brake_wear_disc_rl,
            )
            WheelLifeText(
                aceWindowsBrakeWear.discLife,
                WheelIndex.REAR_RIGHT,
                Res.string.debug_state_brake_wear_disc_rr,
            )
        }
    }
}

// デバッグ表示専用の固定4輪分Mapのため、ImmutableMap化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun WheelLifeText(
    wheels: Map<WheelIndex, Double>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
) {
    Text(
        text = stringResource(labelRes, wheelLifeText(wheels, wheelIndex)),
        modifier = Modifier.width(WHEEL_COLUMN_WIDTH),
        softWrap = false,
        style = koDriverNumericTextStyle(),
    )
}
