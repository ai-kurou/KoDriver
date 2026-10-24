package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_fuel_remaining_liters
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_fuel_remaining_percent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FuelContent(telemetry: LmuWindowsTelemetryData?) {
    val fuel = telemetry?.fuel
    // Android 経由のテレメトリは燃料をダミー値（残量・容量とも0）で埋めるため、容量0は未取得として扱う。
    if (fuel == null || fuel.capacityLiters.value <= 0.0) {
        DebugStateUnavailableContent()
        return
    }
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_fuel_remaining_liters,
                    formatOneDecimal(fuel.currentLiters.value),
                ),
            style = koDriverNumericTextStyle(MaterialTheme.typography.headlineMedium),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_fuel_remaining_percent,
                    formatOneDecimal(fuel.currentLiters.value / fuel.capacityLiters.value * 100.0),
                ),
            style = koDriverNumericTextStyle(),
        )
    }
}
