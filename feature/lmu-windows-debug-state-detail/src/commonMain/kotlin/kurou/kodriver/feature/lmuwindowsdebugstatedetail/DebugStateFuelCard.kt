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
    if (fuel == null) {
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
        if (fuel.capacityLiters.value > 0.0) {
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
}
