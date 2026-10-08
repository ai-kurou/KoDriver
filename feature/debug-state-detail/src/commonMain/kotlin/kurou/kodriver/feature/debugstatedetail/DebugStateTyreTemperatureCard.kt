package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_rr
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TyreTemperatureContent(
    selectedSimulator: Simulator,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
) {
    val wheels =
        when (selectedSimulator) {
            is Simulator.LmuWindows -> {
                lmuWindowsTelemetry?.tyres?.wheels?.mapValues { it.value.surfaceTemperature }
            }

            is Simulator.Gt7Ps5 -> {
                gt7Ps5Telemetry?.tyreTemperature?.let {
                    mapOf(
                        WheelIndex.FRONT_LEFT to it.frontLeftCelsius,
                        WheelIndex.FRONT_RIGHT to it.frontRightCelsius,
                        WheelIndex.REAR_LEFT to it.rearLeftCelsius,
                        WheelIndex.REAR_RIGHT to it.rearRightCelsius,
                    )
                }
            }

            is Simulator.AceWindows -> {
                null
            }
        }
    if (wheels == null) {
        DebugStateUnavailableContent()
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelTemperatureText(
                selectedSimulator,
                wheels,
                WheelIndex.FRONT_LEFT,
                Res.string.debug_state_tyre_temperature_fl,
                Modifier.weight(1f),
            )
            WheelTemperatureText(
                selectedSimulator,
                wheels,
                WheelIndex.FRONT_RIGHT,
                Res.string.debug_state_tyre_temperature_fr,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelTemperatureText(
                selectedSimulator,
                wheels,
                WheelIndex.REAR_LEFT,
                Res.string.debug_state_tyre_temperature_rl,
                Modifier.weight(1f),
            )
            WheelTemperatureText(
                selectedSimulator,
                wheels,
                WheelIndex.REAR_RIGHT,
                Res.string.debug_state_tyre_temperature_rr,
                Modifier.weight(1f),
            )
        }
    }
}

// デバッグ表示専用の固定4輪分Mapのため、ImmutableMap化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun WheelTemperatureText(
    selectedSimulator: Simulator,
    wheels: Map<WheelIndex, CelsiusReading>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
    modifier: Modifier = Modifier,
) {
    DebugStateHeatTile(
        text = stringResource(labelRes, wheels[wheelIndex]?.let { formatCelsius(it) } ?: "-"),
        celsius = wheels[wheelIndex]?.value?.toDouble(),
        modifier = modifier,
        heatLevel = wheels[wheelIndex]?.value?.toDouble()?.let { tyreTemperatureHeatLevel(it, selectedSimulator) },
    )
}
