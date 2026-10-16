package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.DebugStateWheelTemperatureTile
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_tyre_temperature_fl
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_tyre_temperature_fr
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_tyre_temperature_rl
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_tyre_temperature_rr
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TyreTemperatureContent(gt7Ps5Telemetry: Gt7Ps5TelemetryData?) {
    val wheels =
        gt7Ps5Telemetry?.tyreTemperature?.takeUnless { it.isUnavailable() }?.let {
            mapOf(
                WheelIndex.FRONT_LEFT to it.frontLeftCelsius,
                WheelIndex.FRONT_RIGHT to it.frontRightCelsius,
                WheelIndex.REAR_LEFT to it.rearLeftCelsius,
                WheelIndex.REAR_RIGHT to it.rearRightCelsius,
            )
        }
    if (wheels == null) {
        DebugStateUnavailableContent()
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelTemperatureText(
                wheels,
                WheelIndex.FRONT_LEFT,
                Res.string.debug_state_tyre_temperature_fl,
                Modifier.weight(1f),
            )
            WheelTemperatureText(
                wheels,
                WheelIndex.FRONT_RIGHT,
                Res.string.debug_state_tyre_temperature_fr,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelTemperatureText(
                wheels,
                WheelIndex.REAR_LEFT,
                Res.string.debug_state_tyre_temperature_rl,
                Modifier.weight(1f),
            )
            WheelTemperatureText(
                wheels,
                WheelIndex.REAR_RIGHT,
                Res.string.debug_state_tyre_temperature_rr,
                Modifier.weight(1f),
            )
        }
    }
}

// GT7は取得できないタイヤ温度を全輪0fで通知するため、全輪0℃は未取得として扱う。
private fun Gt7Ps5TyreTemperatureData.isUnavailable(): Boolean =
    frontLeftCelsius.value == 0f &&
        frontRightCelsius.value == 0f &&
        rearLeftCelsius.value == 0f &&
        rearRightCelsius.value == 0f

// デバッグ表示専用の固定4輪分Mapのため、ImmutableMap化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun WheelTemperatureText(
    wheels: Map<WheelIndex, CelsiusReading>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
    modifier: Modifier = Modifier,
) {
    val reading = wheels.getValue(wheelIndex)
    DebugStateWheelTemperatureTile(
        text = { stringResource(labelRes, it) },
        formattedCelsius = formatCelsius(reading),
        heatLevel = tyreTemperatureHeatLevel(reading.value.toDouble()),
        modifier = modifier,
    )
}
