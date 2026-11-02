package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

sealed class AceWindowsAutoReadoutListItemType(
    id: ReadoutItemKey,
) : AutoReadoutListItemType(id, Simulator.AceWindows) {
    data object VehicleApproach : AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.VehicleApproach.Root)

    data object Flag : AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.Flag.Root)

    data object TyreTemperature : AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.TyreTemperature.Root)

    data object RemainingFuel : AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.RemainingFuel.Root)

    data object RemainingFuelLaps : AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.RemainingFuelLaps.Root)

    data object MyBestLap : AceWindowsAutoReadoutListItemType(AceWindowsReadoutItemKey.MyBestLap.Root)

    companion object {
        fun fromId(id: ReadoutItemKey): AceWindowsAutoReadoutListItemType? =
            if (id is AceWindowsReadoutItemKey.TopLevel) fromTopLevel(id) else null

        private fun fromTopLevel(id: AceWindowsReadoutItemKey.TopLevel): AceWindowsAutoReadoutListItemType =
            when (id) {
                AceWindowsReadoutItemKey.VehicleApproach.Root -> VehicleApproach
                AceWindowsReadoutItemKey.Flag.Root -> Flag
                AceWindowsReadoutItemKey.TyreTemperature.Root -> TyreTemperature
                AceWindowsReadoutItemKey.RemainingFuel.Root -> RemainingFuel
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root -> RemainingFuelLaps
                AceWindowsReadoutItemKey.MyBestLap.Root -> MyBestLap
            }
    }
}
