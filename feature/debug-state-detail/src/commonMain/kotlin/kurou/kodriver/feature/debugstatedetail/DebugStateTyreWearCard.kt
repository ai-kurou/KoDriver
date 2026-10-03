package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_wear_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_wear_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_wear_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_wear_rr
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TyreWearContent(
    selectedSimulator: Simulator,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
) {
    val wheels = lmuWindowsTelemetry?.tyres?.wheels
    if (selectedSimulator !is Simulator.LmuWindows || wheels == null) {
        DebugStateUnavailableContent()
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium)) {
            WheelWearText(wheels, WheelIndex.FRONT_LEFT, Res.string.debug_state_tyre_wear_fl, Modifier.weight(1f))
            WheelWearText(wheels, WheelIndex.FRONT_RIGHT, Res.string.debug_state_tyre_wear_fr, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium)) {
            WheelWearText(wheels, WheelIndex.REAR_LEFT, Res.string.debug_state_tyre_wear_rl, Modifier.weight(1f))
            WheelWearText(wheels, WheelIndex.REAR_RIGHT, Res.string.debug_state_tyre_wear_rr, Modifier.weight(1f))
        }
    }
}

// デバッグ表示専用の固定4輪分Mapのため、ImmutableMap化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun WheelWearText(
    wheels: Map<WheelIndex, LmuWindowsTyreWheelData>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
    modifier: Modifier = Modifier,
) {
    DebugStateWearMeter(
        text = stringResource(labelRes, wheelWearPercentText(wheels, wheelIndex)),
        remainingPercent = wheels[wheelIndex]?.wear?.value?.times(100.0),
        modifier = modifier,
    )
}
