package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_temperature_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_temperature_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_temperature_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_temperature_rr
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BrakeTemperatureContent(
    selectedSimulator: Simulator,
    brakeTemperature: LmuWindowsBrakeTemperatureData?,
) {
    val wheels =
        when (selectedSimulator) {
            is Simulator.LmuWindows -> brakeTemperature?.wheels
            is Simulator.AceWindows, is Simulator.Gt7Ps5 -> null
        }
    if (wheels == null) {
        DebugStateUnavailableContent()
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelBrakeTemperatureText(
                wheels,
                WheelIndex.FRONT_LEFT,
                Res.string.debug_state_brake_temperature_fl,
                Modifier.weight(1f),
            )
            WheelBrakeTemperatureText(
                wheels,
                WheelIndex.FRONT_RIGHT,
                Res.string.debug_state_brake_temperature_fr,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelBrakeTemperatureText(
                wheels,
                WheelIndex.REAR_LEFT,
                Res.string.debug_state_brake_temperature_rl,
                Modifier.weight(1f),
            )
            WheelBrakeTemperatureText(
                wheels,
                WheelIndex.REAR_RIGHT,
                Res.string.debug_state_brake_temperature_rr,
                Modifier.weight(1f),
            )
        }
    }
}

// デバッグ表示専用の固定4輪分Mapのため、ImmutableMap化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun WheelBrakeTemperatureText(
    wheels: Map<WheelIndex, CelsiusReading>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
    modifier: Modifier = Modifier,
) {
    DebugStateHeatTile(
        text = stringResource(labelRes, wheelCarcassTemperatureText(wheels, wheelIndex)),
        celsius = wheels[wheelIndex]?.value?.toDouble(),
        modifier = modifier,
        heatLevel = wheels[wheelIndex]?.value?.toDouble()?.let { brakeTemperatureHeatLevel(it) },
    )
}
