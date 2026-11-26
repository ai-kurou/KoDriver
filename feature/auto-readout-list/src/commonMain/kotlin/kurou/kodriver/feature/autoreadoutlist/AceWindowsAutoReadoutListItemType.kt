package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

public sealed class AceWindowsAutoReadoutListItemType(
    id: ReadoutItemKey,
) : AutoReadoutListItemType(id, Simulator.AceWindows) {
    public data object VehicleApproach :
        AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.VehicleApproach.Root)

    public data object Flag : AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.Flag.Root)

    public data object TyreTemperature :
        AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.TyreTemperature.Root)

    public data object BrakeTemperature :
        AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.BrakeTemperature.Root)

    public data object RemainingFuel : AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.RemainingFuel.Root)

    public data object RemainingFuelLaps :
        AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.RemainingFuelLaps.Root)

    public data object MyBestLap : AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.MyBestLap.Root)

    public companion object {
        public fun fromId(id: ReadoutItemKey): AceWindowsAutoReadoutListItemType? =
            if (id is AceWindowsReadoutItemKey.TopLevel) fromTopLevel(id) else null

        private fun fromTopLevel(id: AceWindowsReadoutItemKey.TopLevel): AceWindowsAutoReadoutListItemType =
            when (id) {
                AceWindowsReadoutItemKey.VehicleApproach.Root -> VehicleApproach
                AceWindowsReadoutItemKey.Flag.Root -> Flag
                AceWindowsReadoutItemKey.TyreTemperature.Root -> TyreTemperature
                AceWindowsReadoutItemKey.BrakeTemperature.Root -> BrakeTemperature
                AceWindowsReadoutItemKey.RemainingFuel.Root -> RemainingFuel
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root -> RemainingFuelLaps
                AceWindowsReadoutItemKey.MyBestLap.Root -> MyBestLap
            }
    }
}
