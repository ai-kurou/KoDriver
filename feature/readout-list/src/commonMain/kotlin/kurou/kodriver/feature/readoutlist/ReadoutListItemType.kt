package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

sealed class ReadoutListItemType(val id: ReadoutItemKey) {
    sealed class LmuWindows(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object VehicleApproach : LmuWindows(ReadoutItemKey.LmuWindows.VehicleApproach)
        data object Flag : LmuWindows(ReadoutItemKey.LmuWindows.Flag)
        data object VehicleDamage : LmuWindows(ReadoutItemKey.LmuWindows.VehicleDamage)
        data object TyreTemperature : LmuWindows(ReadoutItemKey.LmuWindows.TyreTemperature)
        data object MyBestLap : LmuWindows(ReadoutItemKey.LmuWindows.MyBestLap)
    }

    sealed class Gt7Ps5(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object MyBestLap : Gt7Ps5(ReadoutItemKey.Gt7Ps5.MyBestLap)
        data object RemainingFuelLaps : Gt7Ps5(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps)
    }

    companion object {
        fun fromId(simulator: Simulator, id: ReadoutItemKey): ReadoutListItemType? = when (simulator) {
            is Simulator.LmuWindows -> when (id) {
                ReadoutItemKey.LmuWindows.VehicleApproach -> LmuWindows.VehicleApproach
                ReadoutItemKey.LmuWindows.Flag -> LmuWindows.Flag
                ReadoutItemKey.LmuWindows.VehicleDamage -> LmuWindows.VehicleDamage
                ReadoutItemKey.LmuWindows.TyreTemperature -> LmuWindows.TyreTemperature
                ReadoutItemKey.LmuWindows.MyBestLap -> LmuWindows.MyBestLap
                else -> null
            }
            is Simulator.Gt7Ps5 -> when (id) {
                ReadoutItemKey.Gt7Ps5.MyBestLap -> Gt7Ps5.MyBestLap
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps -> Gt7Ps5.RemainingFuelLaps
                else -> null
            }
        }
    }
}
