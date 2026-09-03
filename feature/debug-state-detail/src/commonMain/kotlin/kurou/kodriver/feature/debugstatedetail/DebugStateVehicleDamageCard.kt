package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_damage_overheating
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_damage_part_detached
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_no
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_yes
import org.jetbrains.compose.resources.stringResource

@Composable
private fun booleanDisplayName(value: Boolean): String =
    if (value) {
        stringResource(Res.string.debug_state_vehicle_location_yes)
    } else {
        stringResource(Res.string.debug_state_vehicle_location_no)
    }

@Composable
internal fun VehicleDamageContent(vehicleDamage: LmuWindowsVehicleDamageData?) {
    if (vehicleDamage == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_vehicle_damage_overheating,
                    booleanDisplayName(vehicleDamage.overheating),
                ),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_vehicle_damage_part_detached,
                    booleanDisplayName(vehicleDamage.partDetached),
                ),
        )
    }
}
