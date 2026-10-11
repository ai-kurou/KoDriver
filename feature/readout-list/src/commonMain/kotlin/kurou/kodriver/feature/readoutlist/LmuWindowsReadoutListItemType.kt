package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

sealed class LmuWindowsReadoutListItemType(
    id: ReadoutItemKey,
) : ReadoutListItemType(id) {
    data object VehicleApproach : LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.VehicleApproach.Root)

    data object Flag : LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.Flag.Root)

    data object VehicleDamage : LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.VehicleDamage.Root)

    data object TyreTemperature : LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.TyreTemperature.Root)

    data object PitTiming : LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.PitTiming.Root)

    data object RemainingVirtualEnergy :
        LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

    data object TyreWear : LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.TyreWear.Root)

    data object BrakeTemperature : LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.BrakeTemperature.Root)

    data object BrakeWear : LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.BrakeWear.Root)

    data object MyBestLap : LmuWindowsReadoutListItemType(LmuWindowsReadoutItemKey.MyBestLap.Root)

    companion object {
        fun fromId(id: ReadoutItemKey): LmuWindowsReadoutListItemType? =
            when (id) {
                LmuWindowsReadoutItemKey.VehicleApproach.Root -> VehicleApproach
                LmuWindowsReadoutItemKey.Flag.Root -> Flag
                LmuWindowsReadoutItemKey.VehicleDamage.Root -> VehicleDamage
                LmuWindowsReadoutItemKey.TyreTemperature.Root -> TyreTemperature
                LmuWindowsReadoutItemKey.PitTiming.Root -> PitTiming
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root -> RemainingVirtualEnergy
                LmuWindowsReadoutItemKey.TyreWear.Root -> TyreWear
                LmuWindowsReadoutItemKey.BrakeTemperature.Root -> BrakeTemperature
                LmuWindowsReadoutItemKey.BrakeWear.Root -> BrakeWear
                LmuWindowsReadoutItemKey.MyBestLap.Root -> MyBestLap
                else -> null
            }
    }
}
