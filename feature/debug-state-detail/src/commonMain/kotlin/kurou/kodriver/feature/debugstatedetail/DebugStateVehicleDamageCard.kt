package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_damage_overheating
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_damage_part_detached
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_damage_tyre_detached_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_damage_tyre_detached_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_damage_tyre_detached_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_damage_tyre_detached_rr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_no
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_yes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
private fun booleanDisplayName(value: Boolean): String =
    if (value) {
        stringResource(Res.string.debug_state_vehicle_location_yes)
    } else {
        stringResource(Res.string.debug_state_vehicle_location_no)
    }

@Composable
internal fun VehicleDamageContent(
    vehicleDamage: LmuWindowsVehicleDamageData?,
    tyreDetached: LmuWindowsTyreDetachedData?,
) {
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
        TyreDetachedText(tyreDetached, WheelIndex.FRONT_LEFT, Res.string.debug_state_vehicle_damage_tyre_detached_fl)
        TyreDetachedText(tyreDetached, WheelIndex.FRONT_RIGHT, Res.string.debug_state_vehicle_damage_tyre_detached_fr)
        TyreDetachedText(tyreDetached, WheelIndex.REAR_LEFT, Res.string.debug_state_vehicle_damage_tyre_detached_rl)
        TyreDetachedText(tyreDetached, WheelIndex.REAR_RIGHT, Res.string.debug_state_vehicle_damage_tyre_detached_rr)
    }
}

@Composable
private fun TyreDetachedText(
    tyreDetached: LmuWindowsTyreDetachedData?,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
) {
    val detached = tyreDetached?.wheels?.get(wheelIndex) ?: false
    Text(text = stringResource(labelRes, booleanDisplayName(detached)))
}
