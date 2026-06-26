package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

sealed class ReadoutListItemType(val id: ReadoutItemKey) {
    sealed class LmuWindows(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object VehicleApproach : LmuWindows(ReadoutItemKey.VehicleApproach)
        data object Flag : LmuWindows(ReadoutItemKey.Flag)
        data object VehicleDamage : LmuWindows(ReadoutItemKey.VehicleDamage)
    }

    sealed class Gt7Ps5(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object MyBestLap : Gt7Ps5(ReadoutItemKey.MyBestLap)
        data object RemainingFuelLaps : Gt7Ps5(ReadoutItemKey.RemainingFuelLaps)
    }

    companion object {
        fun fromId(simulator: Simulator, id: ReadoutItemKey): ReadoutListItemType? = when (simulator) {
            is Simulator.LmuWindows -> when (id) {
                ReadoutItemKey.VehicleApproach -> LmuWindows.VehicleApproach
                ReadoutItemKey.Flag -> LmuWindows.Flag
                ReadoutItemKey.VehicleDamage -> LmuWindows.VehicleDamage
                else -> null
            }
            is Simulator.Gt7Ps5 -> when (id) {
                ReadoutItemKey.MyBestLap -> Gt7Ps5.MyBestLap
                ReadoutItemKey.RemainingFuelLaps -> Gt7Ps5.RemainingFuelLaps
                else -> null
            }
        }
    }
}
