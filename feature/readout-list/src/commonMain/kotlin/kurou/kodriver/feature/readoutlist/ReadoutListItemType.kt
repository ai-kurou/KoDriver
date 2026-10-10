package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.defaultReadoutOrder

sealed class ReadoutListItemType(
    val id: ReadoutItemKey,
) {
    sealed class LmuWindows(
        id: ReadoutItemKey,
    ) : ReadoutListItemType(id) {
        data object VehicleApproach : LmuWindows(LmuWindowsReadoutItemKey.VehicleApproach.Root)

        data object Flag : LmuWindows(LmuWindowsReadoutItemKey.Flag.Root)

        data object VehicleDamage : LmuWindows(LmuWindowsReadoutItemKey.VehicleDamage.Root)

        data object TyreTemperature : LmuWindows(LmuWindowsReadoutItemKey.TyreTemperature.Root)

        data object PitTiming : LmuWindows(LmuWindowsReadoutItemKey.PitTiming.Root)

        data object RemainingVirtualEnergy :
            LmuWindows(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

        data object TyreWear : LmuWindows(LmuWindowsReadoutItemKey.TyreWear.Root)

        data object BrakeTemperature : LmuWindows(LmuWindowsReadoutItemKey.BrakeTemperature.Root)

        data object BrakeWear : LmuWindows(LmuWindowsReadoutItemKey.BrakeWear.Root)

        data object MyBestLap : LmuWindows(LmuWindowsReadoutItemKey.MyBestLap.Root)
    }

    sealed class Gt7Ps5(
        id: ReadoutItemKey,
    ) : ReadoutListItemType(id) {
        data object MyBestLap : Gt7Ps5(Gt7Ps5ReadoutItemKey.MyBestLap.Root)

        data object RemainingFuelLaps : Gt7Ps5(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root)

        data object RemainingFuel : Gt7Ps5(Gt7Ps5ReadoutItemKey.RemainingFuel.Root)

        data object TyreTemperature : Gt7Ps5(Gt7Ps5ReadoutItemKey.TyreTemperature.Root)
    }

    sealed class AceWindows(
        id: ReadoutItemKey,
    ) : ReadoutListItemType(id) {
        data object VehicleApproach : AceWindows(AceWindowsReadoutItemKey.VehicleApproach.Root)

        data object Flag : AceWindows(AceWindowsReadoutItemKey.Flag.Root)

        data object TyreTemperature : AceWindows(AceWindowsReadoutItemKey.TyreTemperature.Root)

        data object RemainingFuel : AceWindows(AceWindowsReadoutItemKey.RemainingFuel.Root)

        data object RemainingFuelLaps : AceWindows(AceWindowsReadoutItemKey.RemainingFuelLaps.Root)

        data object MyBestLap : AceWindows(AceWindowsReadoutItemKey.MyBestLap.Root)
    }

    fun belongsTo(simulator: Simulator): Boolean =
        when (simulator) {
            is Simulator.LmuWindows -> this is LmuWindows
            is Simulator.Gt7Ps5 -> this is Gt7Ps5
            is Simulator.AceWindows -> this is AceWindows
        }

    companion object {
        fun fromId(
            simulator: Simulator,
            id: ReadoutItemKey,
        ): ReadoutListItemType? =
            when (simulator) {
                is Simulator.LmuWindows -> lmuWindowsFromId(id)
                is Simulator.Gt7Ps5 -> gt7Ps5FromId(id)
                is Simulator.AceWindows -> aceWindowsFromId(id)
            }

        private fun lmuWindowsFromId(id: ReadoutItemKey): LmuWindows? =
            when (id) {
                LmuWindowsReadoutItemKey.VehicleApproach.Root -> LmuWindows.VehicleApproach
                LmuWindowsReadoutItemKey.Flag.Root -> LmuWindows.Flag
                LmuWindowsReadoutItemKey.VehicleDamage.Root -> LmuWindows.VehicleDamage
                LmuWindowsReadoutItemKey.TyreTemperature.Root -> LmuWindows.TyreTemperature
                LmuWindowsReadoutItemKey.PitTiming.Root -> LmuWindows.PitTiming
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root -> LmuWindows.RemainingVirtualEnergy
                LmuWindowsReadoutItemKey.TyreWear.Root -> LmuWindows.TyreWear
                LmuWindowsReadoutItemKey.BrakeTemperature.Root -> LmuWindows.BrakeTemperature
                LmuWindowsReadoutItemKey.BrakeWear.Root -> LmuWindows.BrakeWear
                LmuWindowsReadoutItemKey.MyBestLap.Root -> LmuWindows.MyBestLap
                else -> null
            }

        private fun gt7Ps5FromId(id: ReadoutItemKey): Gt7Ps5? =
            when (id) {
                Gt7Ps5ReadoutItemKey.MyBestLap.Root -> Gt7Ps5.MyBestLap
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root -> Gt7Ps5.RemainingFuelLaps
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root -> Gt7Ps5.RemainingFuel
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root -> Gt7Ps5.TyreTemperature
                else -> null
            }

        private fun aceWindowsFromId(id: ReadoutItemKey): AceWindows? =
            when (id) {
                AceWindowsReadoutItemKey.VehicleApproach.Root -> AceWindows.VehicleApproach
                AceWindowsReadoutItemKey.Flag.Root -> AceWindows.Flag
                AceWindowsReadoutItemKey.TyreTemperature.Root -> AceWindows.TyreTemperature
                AceWindowsReadoutItemKey.RemainingFuel.Root -> AceWindows.RemainingFuel
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root -> AceWindows.RemainingFuelLaps
                AceWindowsReadoutItemKey.MyBestLap.Root -> AceWindows.MyBestLap
                else -> null
            }

        fun defaultOrder(simulator: Simulator): List<ReadoutItemKey> = defaultReadoutOrder(simulator)
    }
}
