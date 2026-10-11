package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

sealed class AceWindowsReadoutListItemType(
    id: ReadoutItemKey,
) : ReadoutListItemType(id) {
    data object VehicleApproach : AceWindowsReadoutListItemType(AceWindowsReadoutItemKey.VehicleApproach.Root)

    data object Flag : AceWindowsReadoutListItemType(AceWindowsReadoutItemKey.Flag.Root)

    data object TyreTemperature : AceWindowsReadoutListItemType(AceWindowsReadoutItemKey.TyreTemperature.Root)

    data object RemainingFuel : AceWindowsReadoutListItemType(AceWindowsReadoutItemKey.RemainingFuel.Root)

    data object RemainingFuelLaps : AceWindowsReadoutListItemType(AceWindowsReadoutItemKey.RemainingFuelLaps.Root)

    data object MyBestLap : AceWindowsReadoutListItemType(AceWindowsReadoutItemKey.MyBestLap.Root)

    companion object {
        fun fromId(id: ReadoutItemKey): AceWindowsReadoutListItemType? =
            when (id) {
                AceWindowsReadoutItemKey.VehicleApproach.Root -> VehicleApproach
                AceWindowsReadoutItemKey.Flag.Root -> Flag
                AceWindowsReadoutItemKey.TyreTemperature.Root -> TyreTemperature
                AceWindowsReadoutItemKey.RemainingFuel.Root -> RemainingFuel
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root -> RemainingFuelLaps
                AceWindowsReadoutItemKey.MyBestLap.Root -> MyBestLap
                else -> null
            }
    }
}
