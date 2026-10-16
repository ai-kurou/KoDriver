package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.DebugStateWheelTemperatureTile
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_wheel_temperature_fl
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_wheel_temperature_fr
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_wheel_temperature_rl
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_wheel_temperature_rr
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TyreTemperatureContent(lmuWindowsTelemetry: LmuWindowsTelemetryData?) {
    val wheels = lmuWindowsTelemetry?.tyres?.wheels?.mapValues { it.value.surfaceTemperature }
    if (wheels == null) {
        DebugStateUnavailableContent()
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelTemperatureText(
                wheels,
                WheelIndex.FRONT_LEFT,
                Res.string.debug_state_wheel_temperature_fl,
                Modifier.weight(1f),
            )
            WheelTemperatureText(
                wheels,
                WheelIndex.FRONT_RIGHT,
                Res.string.debug_state_wheel_temperature_fr,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            WheelTemperatureText(
                wheels,
                WheelIndex.REAR_LEFT,
                Res.string.debug_state_wheel_temperature_rl,
                Modifier.weight(1f),
            )
            WheelTemperatureText(
                wheels,
                WheelIndex.REAR_RIGHT,
                Res.string.debug_state_wheel_temperature_rr,
                Modifier.weight(1f),
            )
        }
    }
}

// デバッグ表示専用の固定4輪分Mapのため、ImmutableMap化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun WheelTemperatureText(
    wheels: Map<WheelIndex, CelsiusReading>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
    modifier: Modifier = Modifier,
) {
    val reading = wheels[wheelIndex]
    DebugStateWheelTemperatureTile(
        text = { stringResource(labelRes, it) },
        formattedCelsius = reading?.let { formatCelsius(it) },
        heatLevel = reading?.value?.toDouble()?.let { temperatureHeatLevel(it) },
        modifier = modifier,
    )
}
