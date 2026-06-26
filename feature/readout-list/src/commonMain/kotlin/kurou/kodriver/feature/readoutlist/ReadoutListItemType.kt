package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey

sealed class ReadoutListItemType(val id: ReadoutItemKey) {
    sealed class LmuWindows(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object VehicleApproach : LmuWindows(ReadoutItemKey.VEHICLE_APPROACH)
        data object Flag : LmuWindows(ReadoutItemKey.FLAG)
        data object VehicleDamage : LmuWindows(ReadoutItemKey.VEHICLE_DAMAGE)
    }

    sealed class Gt7Ps5(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object MyBestLap : Gt7Ps5(ReadoutItemKey.MY_BEST_LAP)
        data object RemainingFuelLaps : Gt7Ps5(ReadoutItemKey.REMAINING_FUEL_LAPS)
    }

    companion object {
        fun fromId(simulatorId: String, id: ReadoutItemKey): ReadoutListItemType? = when (simulatorId) {
            "lmu_windows" -> when (id) {
                ReadoutItemKey.VEHICLE_APPROACH -> LmuWindows.VehicleApproach
                ReadoutItemKey.FLAG -> LmuWindows.Flag
                ReadoutItemKey.VEHICLE_DAMAGE -> LmuWindows.VehicleDamage
                else -> null
            }
            "gt7_ps5" -> when (id) {
                ReadoutItemKey.MY_BEST_LAP -> Gt7Ps5.MyBestLap
                ReadoutItemKey.REMAINING_FUEL_LAPS -> Gt7Ps5.RemainingFuelLaps
                else -> null
            }
            else -> null
        }
    }
}
