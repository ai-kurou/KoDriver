package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

sealed class LmuWindowsAutoReadoutListItemType(
    id: ReadoutItemKey,
) : AutoReadoutListItemType(id) {
    data object VehicleApproach : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.VehicleApproach.Root)

    data object Flag : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.Flag.Root)

    data object VehicleDamage : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.VehicleDamage.Root)

    data object TyreTemperature : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.TyreTemperature.Root)

    data object PitTiming : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.PitTiming.Root)

    data object RemainingVirtualEnergy :
        LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

    data object RemainingFuel : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.RemainingFuel.Root)

    data object TyreWear : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.TyreWear.Root)

    data object BrakeTemperature : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.BrakeTemperature.Root)

    data object BrakeWear : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.BrakeWear.Root)

    data object MyBestLap : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.MyBestLap.Root)

    companion object {
        fun fromId(id: ReadoutItemKey): LmuWindowsAutoReadoutListItemType? =
            if (id is LmuWindowsReadoutItemKey.TopLevel) fromTopLevel(id) else null

        private fun fromTopLevel(id: LmuWindowsReadoutItemKey.TopLevel): LmuWindowsAutoReadoutListItemType =
            when (id) {
                LmuWindowsReadoutItemKey.VehicleApproach.Root -> VehicleApproach
                LmuWindowsReadoutItemKey.Flag.Root -> Flag
                LmuWindowsReadoutItemKey.VehicleDamage.Root -> VehicleDamage
                LmuWindowsReadoutItemKey.TyreTemperature.Root -> TyreTemperature
                LmuWindowsReadoutItemKey.PitTiming.Root -> PitTiming
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root -> RemainingVirtualEnergy
                LmuWindowsReadoutItemKey.RemainingFuel.Root -> RemainingFuel
                LmuWindowsReadoutItemKey.TyreWear.Root -> TyreWear
                LmuWindowsReadoutItemKey.BrakeTemperature.Root -> BrakeTemperature
                LmuWindowsReadoutItemKey.BrakeWear.Root -> BrakeWear
                LmuWindowsReadoutItemKey.MyBestLap.Root -> MyBestLap
            }
    }
}
