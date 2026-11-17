package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

public sealed class LmuWindowsAutoReadoutListItemType(
    id: ReadoutItemKey,
) : AutoReadoutListItemType(id, Simulator.LmuWindows) {
    public data object VehicleApproach :
        LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.VehicleApproach.Root)

    public data object Flag : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.Flag.Root)

    public data object VehicleDamage : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.VehicleDamage.Root)

    public data object TyreTemperature :
        LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.TyreTemperature.Root)

    public data object PitTiming : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.PitTiming.Root)

    public data object RemainingVirtualEnergy :
        LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

    public data object RemainingFuel : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.RemainingFuel.Root)

    public data object TyreWear : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.TyreWear.Root)

    public data object BrakeTemperature :
        LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.BrakeTemperature.Root)

    public data object BrakeWear : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.BrakeWear.Root)

    public data object MyBestLap : LmuWindowsAutoReadoutListItemType(LmuWindowsReadoutItemKey.MyBestLap.Root)

    public companion object {
        public fun fromId(id: ReadoutItemKey): LmuWindowsAutoReadoutListItemType? =
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
